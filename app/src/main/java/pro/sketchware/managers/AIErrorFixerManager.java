package pro.sketchware.managers;

import android.content.Context;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pro.sketchware.ai.GroqClient;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.utility.FileUtil;
import mod.hilal.saif.activities.tools.ConfigActivity;
import mod.hilal.saif.blocks.CommandBlock;
import mod.hey.studios.util.Helper;
import a.a.a.wq;
import a.a.a.yq;
import android.content.Intent;
import android.net.Uri;

/**
 * Manager responsável por corrigir automaticamente erros de compilação usando IA
 */
public class AIErrorFixerManager {
    
    private final Context context;
    private final String logicPath;
    
    public AIErrorFixerManager(Context context, String logicPath) {
        this.context = context;
        this.logicPath = logicPath;
    }
    
    /**
     * Mostra seletor de idioma e executa correção com IA
     */
    public void showLanguagePickerAndFix(String errorLog, String scId) {
        if (errorLog == null || errorLog.trim().isEmpty()) {
            SketchwareUtil.toastError("No error log to analyze.");
            return;
        }
        
        // Verificar se Groq está configurado e habilitado
        if (!isGroqApiConfiguredAndEnabled()) {
            showMissingGroqDialog();
            return;
        }
        
        // Labels de idiomas e códigos (mesmo do TranslationManager)
        String[] LANG_NAMES = new String[]{
                "Original (no translation)",
                "English", "Bangla (বাংলা)", "Hindi (हिन्दী)", "Spanish (Español)",
                "French (Français)", "German (Deutsch)", "Chinese - Simplified (简体中文)",
                "Arabic (العربية)", "Portuguese (Português)", "Russian (Русский)",
                "Japanese (日本語)", "Korean (한국어)", "Italian (Italiano)", "Turkish (Türkçe)",
                "Dutch (Nederlands)", "Português (Brasil)"
        };

        String[] LANG_CODES = new String[]{
                "orig",
                "en", "bn", "hi", "es",
                "fr", "de", "zh-CN",
                "ar", "pt", "ru",
                "ja", "ko", "it", "tr",
                "nl", "pt-BR"
        };
        
        // seleção padrão = 0 => Original (sem tradução)
        final int[] selectedIndex = {0};

        new MaterialAlertDialogBuilder(context)
                .setTitle("Select language for AI analysis")
                .setSingleChoiceItems(LANG_NAMES, selectedIndex[0], (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    int idx = selectedIndex[0];
                    String target = LANG_CODES[Math.max(0, Math.min(idx, LANG_CODES.length - 1))];
                    fixErrorWithAI(errorLog, scId, target);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Analisa o erro e tenta corrigi-lo automaticamente usando IA
     */
    public void fixErrorWithAI(String errorLog, String scId, String targetLang) {
        if (errorLog == null || errorLog.trim().isEmpty()) {
            SketchwareUtil.toastError("No error log to analyze.");
            return;
        }
        
        var loadingDialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Analyzing error with AI")
                .setMessage("Please wait while AI analyzes and fixes the error...")
                .setCancelable(false)
                .create();
        loadingDialog.show();
        
        new Thread(() -> {
            try {
                // Obter contexto do arquivo relacionado
                String fileContext = tryGetRelatedFileContent(errorLog, scId);
                
                // Obter lógica atual
                String decryptedLogic = decryptLogicFile(logicPath);
                if (decryptedLogic == null || decryptedLogic.trim().isEmpty()) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        SketchwareUtil.toastError("Cannot read logic file");
                    });
                    return;
                }
                
                // Preparar prompt para IA usando contexto do Sketchware
                String systemPrompt = "You are an expert Android developer and Sketchware block system specialist. " +
                        "You can analyze compile errors and suggest specific block modifications to fix them. " +
                        "You understand Sketchware's block-based programming system and can suggest precise changes. " +
                        "You are familiar with Sketchware's native tools: FileUtil, CommandBlock, SketchwareUtil, and wq path utilities. " +
                        "IMPORTANT: You can ONLY modify existing blocks or add new blocks. You CANNOT delete blocks.";
                
                // Obter informações adicionais do projeto usando ferramentas nativas
                String projectInfo = getProjectContextInfo(scId);
                
                String userPrompt = "Error Log:\n" + errorLog + "\n\n" +
                        "Current Logic File Content:\n" + decryptedLogic + "\n\n" +
                        (fileContext != null ? "Related File Context:\n" + fileContext + "\n\n" : "") +
                        (projectInfo != null ? "Project Context:\n" + projectInfo + "\n\n" : "") +
                        "Please analyze this error and provide a JSON response with the following structure:\n" +
                        "{\n" +
                        "  \"analysis\": \"Brief analysis of the error\",\n" +
                        "  \"fixType\": \"BLOCK_MODIFICATION|BLOCK_ADDITION|NO_FIX_NEEDED\",\n" +
                        "  \"blocksToModify\": [\n" +
                        "    {\n" +
                        "      \"id\": blockId,\n" +
                        "      \"action\": \"MODIFY|ADD\",\n" +
                        "      \"newContent\": \"new JSON content for the block\",\n" +
                        "      \"reason\": \"why this change fixes the error\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"confidence\": 0.0-1.0,\n" +
                        "  \"explanation\": \"Detailed explanation of the fix\"\n" +
                        "}\n\n" +
                        "IMPORTANT RULES:\n" +
                        "1. NEVER suggest DELETE action - only MODIFY or ADD\n" +
                        "2. Only suggest changes you are confident will fix the error\n" +
                        "3. If uncertain, set fixType to NO_FIX_NEEDED\n" +
                        "4. When modifying blocks, preserve the block structure and only change problematic parts\n" +
                        "5. Consider Sketchware's native tools and patterns when suggesting fixes";
                
                // Chamar IA
                var client = new GroqClient();
                var messages = GroqClient.Message.of(systemPrompt, userPrompt);
                String aiResponse = client.chat(messages);
                
                if (aiResponse == null || aiResponse.trim().isEmpty()) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        SketchwareUtil.toastError("AI did not respond");
                    });
                    return;
                }
                
                // Parse da resposta da IA
                AIFixResponse fixResponse = parseAIResponse(aiResponse);
                
                // Traduzir resposta se necessário usando ferramentas nativas do Sketchware
                if (fixResponse != null && !"orig".equalsIgnoreCase(targetLang)) {
                    try {
                        // Usar TranslationManager nativo do Sketchware
                        String translatedAnalysis = TranslationManager.StaticHelper.translateTextSync(fixResponse.analysis, targetLang);
                        if (translatedAnalysis != null && !translatedAnalysis.trim().isEmpty()) {
                            fixResponse.analysis = translatedAnalysis;
                        }
                        
                        String translatedExplanation = TranslationManager.StaticHelper.translateTextSync(fixResponse.explanation, targetLang);
                        if (translatedExplanation != null && !translatedExplanation.trim().isEmpty()) {
                            fixResponse.explanation = translatedExplanation;
                        }
                        
                        // Traduzir razões dos blocos usando ferramentas nativas
                        if (fixResponse.blocksToModify != null) {
                            for (BlockModification mod : fixResponse.blocksToModify) {
                                String translatedReason = TranslationManager.StaticHelper.translateTextSync(mod.reason, targetLang);
                                if (translatedReason != null && !translatedReason.trim().isEmpty()) {
                                    mod.reason = translatedReason;
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Tradução falhou, mantém original - usar SketchwareUtil para log
                        SketchwareUtil.toastError("Translation failed, using original text");
                    }
                }
                
                ((android.app.Activity) context).runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (fixResponse != null) {
                        showFixConfirmationDialog(fixResponse, decryptedLogic, targetLang);
                    } else {
                        SketchwareUtil.toastError("Failed to parse AI response");
                    }
                });
                
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    String msg = String.valueOf(e.getMessage());
                    if (msg != null && (msg.contains("Missing Groq API key") || msg.contains("Groq API error"))) {
                        if (msg.contains("HTTP 413")) {
                            new MaterialAlertDialogBuilder(context)
                                    .setTitle("Request too large")
                                    .setMessage("The error log is too large for AI analysis. Try with a shorter error log or use the manual error checker instead.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else if (msg.contains("Missing Groq API key")) {
                            showMissingGroqDialog();
                        } else {
                            new MaterialAlertDialogBuilder(context)
                                    .setTitle("AI not configured")
                                    .setMessage(msg)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        }
                    } else {
                        SketchwareUtil.toastError("AI error: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
    
    /**
     * Parse da resposta da IA
     */
    private AIFixResponse parseAIResponse(String aiResponse) {
        try {
            // Tentar extrair JSON da resposta
            String jsonStr = extractJSONFromResponse(aiResponse);
            if (jsonStr == null) {
                return null;
            }
            
            JSONObject json = new JSONObject(jsonStr);
            AIFixResponse response = new AIFixResponse();
            
            response.analysis = json.optString("analysis", "");
            response.fixType = json.optString("fixType", "NO_FIX_NEEDED");
            response.confidence = json.optDouble("confidence", 0.0);
            response.explanation = json.optString("explanation", "");
            
            JSONArray blocksArray = json.optJSONArray("blocksToModify");
            if (blocksArray != null) {
                response.blocksToModify = new ArrayList<>();
                for (int i = 0; i < blocksArray.length(); i++) {
                    JSONObject blockObj = blocksArray.getJSONObject(i);
                    BlockModification mod = new BlockModification();
                    mod.id = blockObj.optInt("id", -1);
                    mod.action = blockObj.optString("action", "MODIFY");
                    mod.newContent = blockObj.optString("newContent", "");
                    mod.reason = blockObj.optString("reason", "");
                    response.blocksToModify.add(mod);
                }
            }
            
            return response;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extrai JSON da resposta da IA
     */
    private String extractJSONFromResponse(String response) {
        // Procura por JSON na resposta
        Pattern jsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    /**
     * Mostra diálogo de confirmação para aplicar as correções
     */
    private void showFixConfirmationDialog(AIFixResponse fixResponse, String currentLogic, String targetLang) {
        if (fixResponse.fixType.equals("NO_FIX_NEEDED")) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("AI Analysis Complete")
                    .setMessage("Analysis: " + fixResponse.analysis + "\n\n" +
                               "Explanation: " + fixResponse.explanation + "\n\n" +
                               "The AI determined that no automatic fix is needed for this error.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        
        StringBuilder message = new StringBuilder();
        message.append("Analysis: ").append(fixResponse.analysis).append("\n\n");
        message.append("Confidence: ").append(String.format("%.0f%%", fixResponse.confidence * 100)).append("\n\n");
        message.append("Explanation: ").append(fixResponse.explanation).append("\n\n");
        
        if (fixResponse.blocksToModify != null && !fixResponse.blocksToModify.isEmpty()) {
            message.append("Proposed Changes:\n");
            for (BlockModification mod : fixResponse.blocksToModify) {
                message.append("• Block ID ").append(mod.id).append(": ").append(mod.action).append("\n");
                message.append("  Reason: ").append(mod.reason).append("\n\n");
            }
        }
        
        message.append("Do you want to apply these fixes?");
        
        new MaterialAlertDialogBuilder(context)
                .setTitle("AI Suggested Fixes")
                .setMessage(message.toString())
                .setPositiveButton("Apply Fixes", (dialog, which) -> {
                    applyFixes(fixResponse, currentLogic);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Aplica as correções sugeridas pela IA usando ferramentas nativas do Sketchware
     */
    private void applyFixes(AIFixResponse fixResponse, String currentLogic) {
        try {
            // Fazer backup usando FileUtil nativo
            if (FileUtil.isExistFile(logicPath)) {
                String backupPath = logicPath + ".ai_fix_backup";
                FileUtil.copyFile(logicPath, backupPath);
            }
            
            // Aplicar modificações
            String modifiedLogic = applyBlockModifications(currentLogic, fixResponse.blocksToModify);
            
            // Salvar arquivo modificado
            encryptAndSave(logicPath, modifiedLogic);
            
            SketchwareUtil.toast("AI fixes applied successfully! Backup saved.");
            
            // Atualizar projeto automaticamente usando ferramentas nativas
            refreshProject();
            
        } catch (Exception e) {
            SketchwareUtil.toastError("Failed to apply fixes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Aplica as modificações nos blocos usando ferramentas nativas do Sketchware
     */
    private String applyBlockModifications(String logic, List<BlockModification> modifications) {
        if (modifications == null || modifications.isEmpty()) {
            return logic;
        }
        
        String[] lines = logic.split("\\r?\\n", -1);
        List<String> resultLines = new ArrayList<>(Arrays.asList(lines));
        
        // Mapear blocos por ID usando padrões nativos do Sketchware
        Map<Integer, Integer> blockIdToLineIndex = new HashMap<>();
        String currentHeader = null;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("@")) {
                currentHeader = line;
                continue;
            }
            
            if (!line.isEmpty()) {
                try {
                    JSONObject obj = new JSONObject(line);
                    int id = obj.optInt("id", -1);
                    if (id != -1) {
                        blockIdToLineIndex.put(id, i);
                    }
                } catch (JSONException ignored) {
                    // Não é um bloco JSON válido
                }
            }
        }
        
        // Aplicar modificações usando padrões nativos do Sketchware
        for (BlockModification mod : modifications) {
            Integer lineIndex = blockIdToLineIndex.get(mod.id);
            if (lineIndex != null) {
                switch (mod.action) {
                    case "MODIFY":
                        if (!mod.newContent.isEmpty()) {
                            // Validar JSON antes de aplicar usando Helper nativo
                            try {
                                new JSONObject(mod.newContent); // Validar JSON
                                resultLines.set(lineIndex, mod.newContent);
                            } catch (Exception e) {
                                // Log do erro usando SketchwareUtil nativo
                                SketchwareUtil.toastError("Invalid JSON in block modification: " + e.getMessage());
                            }
                        }
                        break;
                    case "ADD":
                        // Adicionar novo bloco após o bloco atual
                        if (!mod.newContent.isEmpty()) {
                            try {
                                new JSONObject(mod.newContent); // Validar JSON
                                resultLines.add(lineIndex + 1, mod.newContent);
                            } catch (Exception e) {
                                SketchwareUtil.toastError("Invalid JSON in block addition: " + e.getMessage());
                            }
                        }
                        break;
                    case "DELETE":
                        // Ignorar ações DELETE - não permitidas pelo Sketchware
                        break;
                }
            }
        }
        
        return String.join("\n", resultLines);
    }
    
    /**
     * Tenta obter conteúdo de arquivo relacionado baseado no log usando ferramentas nativas do Sketchware
     */
    private String tryGetRelatedFileContent(String log, String scId) {
        try {
            List<String> candidates = extractFilePathCandidates(log, scId);
            for (String p : candidates) {
                if (p == null) continue;
                
                // Usar FileUtil nativo do Sketchware para verificar e ler arquivos
                if (FileUtil.isExistFile(p)) {
                    String content = FileUtil.readFile(p);
                    if (content != null && !content.trim().isEmpty()) {
                        return content;
                    }
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
    
    /**
     * Extrai candidatos de caminho de arquivo do log usando ferramentas nativas do Sketchware
     */
    private List<String> extractFilePathCandidates(String log, String scId) {
        List<String> paths = new ArrayList<>();
        
        // Caminhos absolutos no log
        Matcher abs = Pattern.compile("(/[^\\s:]+\\.(?:java|kt|xml))", Pattern.CASE_INSENSITIVE).matcher(log);
        while (abs.find()) paths.add(abs.group(1));

        // formato javac: SomeFile.java:123:
        Matcher javac = Pattern.compile("([A-Za-z0-9_./\\\\-]+\\.(?:java|kt|xml)):\\d+", Pattern.CASE_INSENSITIVE).matcher(log);
        while (javac.find()) paths.add(javac.group(1));

        // Usar wq (ferramenta nativa) para obter caminhos do projeto
        if (scId != null) {
            String projectPath = wq.b(scId); // Caminho base do projeto
            List<String> tryRoots = new ArrayList<>();
            tryRoots.add(projectPath + "/app/src/main/java/");
            tryRoots.add(projectPath + "/app/src/main/kotlin/");
            tryRoots.add(projectPath + "/app/src/main/res/");

            // Se temos apenas nome do arquivo, tenta buscar sob essas raízes
            Matcher onlyName = Pattern.compile("([A-Za-z0-9_]+\\.(?:java|kt|xml))").matcher(log);
            while (onlyName.find()) {
                String name = onlyName.group(1);
                for (String r : tryRoots) {
                    paths.add(r + name);
                }
            }
        }

        return paths;
    }
    
    /**
     * Lê arquivo de texto usando FileUtil nativo do Sketchware
     */
    private String readTextFile(String path) {
        try {
            return FileUtil.readFile(path);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Descriptografa arquivo de lógica
     */
    public String decryptLogicFile(String path) throws Exception {
        javax.crypto.Cipher instance = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyIv = Arrays.copyOf("sketchwaresecure".getBytes(StandardCharsets.UTF_8), 16);
        instance.init(javax.crypto.Cipher.DECRYPT_MODE,
                new javax.crypto.spec.SecretKeySpec(keyIv, "AES"),
                new javax.crypto.spec.IvParameterSpec(keyIv));

        RandomAccessFile raf = new RandomAccessFile(path, "r");
        byte[] bArr = new byte[(int) raf.length()];
        raf.readFully(bArr);
        raf.close();

        byte[] plain = instance.doFinal(bArr);
        return new String(plain, StandardCharsets.UTF_8);
    }
    
    /**
     * Criptografa e salva arquivo de lógica
     */
    private void encryptAndSave(String path, String plainText) throws Exception {
        javax.crypto.Cipher instance = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyIv = Arrays.copyOf("sketchwaresecure".getBytes(StandardCharsets.UTF_8), 16);
        instance.init(javax.crypto.Cipher.ENCRYPT_MODE,
                new javax.crypto.spec.SecretKeySpec(keyIv, "AES"),
                new javax.crypto.spec.IvParameterSpec(keyIv));

        byte[] encrypted = instance.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        RandomAccessFile raf = new RandomAccessFile(path, "rw");
        raf.setLength(0);
        raf.write(encrypted);
        raf.close();
    }
    
    /**
     * Atualiza o projeto automaticamente após aplicar correções usando ferramentas nativas do Sketchware
     */
    private void refreshProject() {
        try {
            // Usar CommandBlock para limpar cache de comandos
            CommandBlock.x();
            
            // Enviar broadcast para atualizar o projeto usando padrões nativos do Sketchware
            android.content.Intent refreshIntent = new android.content.Intent("pro.sketchware.refresh_project");
            refreshIntent.putExtra("refresh_type", "logic_updated");
            refreshIntent.putExtra("logic_path", logicPath);
            context.sendBroadcast(refreshIntent);
            
            // Também tentar atualizar via intent local usando padrões nativos
            android.content.Intent localRefreshIntent = new android.content.Intent("com.besome.sketch.refresh_project");
            localRefreshIntent.putExtra("refresh_type", "logic_updated");
            localRefreshIntent.putExtra("logic_path", logicPath);
            context.sendBroadcast(localRefreshIntent);
            
            // Notificar sistema de arquivos do Sketchware sobre mudanças
            android.content.Intent fileSystemRefreshIntent = new android.content.Intent("pro.sketchware.filesystem_refresh");
            fileSystemRefreshIntent.putExtra("project_path", wq.b(logicPath.substring(logicPath.lastIndexOf("/") + 1)));
            context.sendBroadcast(fileSystemRefreshIntent);
            
        } catch (Exception e) {
            // Falha silenciosa - o usuário pode recarregar manualmente se necessário
        }
    }
    
    /**
     * Copia arquivo para backup usando FileUtil nativo do Sketchware
     */
    private void copyFile(File src, File dst) throws Exception {
        if (!src.exists()) return;
        FileUtil.copyFile(src.getAbsolutePath(), dst.getAbsolutePath());
    }
    
    /**
     * Obtém informações do contexto do projeto usando ferramentas nativas do Sketchware
     */
    private String getProjectContextInfo(String scId) {
        if (scId == null) return null;
        
        try {
            StringBuilder context = new StringBuilder();
            
            // Obter informações do projeto usando wq
            String projectPath = wq.b(scId);
            context.append("Project Path: ").append(projectPath).append("\n");
            
            // Verificar arquivos importantes do projeto
            String[] importantFiles = {
                "/files/resource/values/strings.xml",
                "/files/resource/values/colors.xml", 
                "/files/resource/values/styles.xml",
                "/files/resource/layout/",
                "/files/java/"
            };
            
            for (String file : importantFiles) {
                String fullPath = projectPath + file;
                if (FileUtil.isExistFile(fullPath)) {
                    context.append("Found: ").append(file).append("\n");
                    
                    // Para arquivos XML, adicionar conteúdo resumido
                    if (file.endsWith(".xml")) {
                        String content = FileUtil.readFile(fullPath);
                        if (content != null && content.length() > 0) {
                            context.append("Content preview: ").append(content.substring(0, Math.min(200, content.length()))).append("...\n");
                        }
                    }
                }
            }
            
            return context.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    // Classes internas para representar dados
    
    public static class AIFixResponse {
        public String analysis;
        public String fixType;
        public List<BlockModification> blocksToModify;
        public double confidence;
        public String explanation;
    }
    
    public static class BlockModification {
        public int id;
        public String action; // MODIFY, DELETE, ADD
        public String newContent;
        public String reason;
    }
    
    /**
     * Verifica se a API do Groq está configurada e habilitada usando ferramentas nativas do Sketchware
     */
    private boolean isGroqApiConfiguredAndEnabled() {
        try {
            ConfigActivity.DataStore dataStore = ConfigActivity.DataStore.getInstance();
            
            // Verificar se a chave da API existe e não está vazia usando padrões nativos
            String apiKey = dataStore.getString(GroqClient.SETTINGS_KEY_API_KEY, "");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return false;
            }
            
            // Verificar se o Groq está habilitado usando configurações nativas
            boolean groqEnabled = dataStore.getBoolean("ai-groq-enabled", false);
            
            // Verificação adicional usando SketchwareUtil para logs
            if (!groqEnabled) {
                SketchwareUtil.toastError("AI (Groq) is not enabled in settings");
            }
            
            return groqEnabled;
        } catch (Exception e) {
            SketchwareUtil.toastError("Failed to check Groq configuration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mostra diálogo quando a API do Groq não está configurada usando ferramentas nativas do Sketchware
     */
    private void showMissingGroqDialog() {
        new MaterialAlertDialogBuilder(context)
                .setTitle("AI (Groq) not configured")
                .setMessage("To use the AI error fixing feature, you need to configure and enable your Groq API key.\n\nYou can get a free API key from the Groq console.")
                .setPositiveButton("Configure AI", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(context, pro.sketchware.activities.ai.ManageAiActivity.class);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        SketchwareUtil.toastError("Failed to open AI configuration: " + e.getMessage());
                    }
                })
                .setNeutralButton("Get free API key", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        SketchwareUtil.toastError("Failed to open browser: " + e.getMessage());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
