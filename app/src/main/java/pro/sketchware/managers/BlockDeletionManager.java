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

/**
 * Manager responsável por gerenciar a exclusão de blocos do arquivo de lógica
 */
public class BlockDeletionManager {
    
    private final Context context;
    private final String logicPath;
    
    public BlockDeletionManager(Context context, String logicPath) {
        this.context = context;
        this.logicPath = logicPath;
    }
    
    /**
     * Analisa o log de erro e extrai palavras-chave inteligentes para busca de blocos
     */
    public List<String> extractSmartKeywords(String errorLog) {
        List<String> keywords = new ArrayList<>();
        if (errorLog == null) return keywords;
        
        String lowerError = errorLog.toLowerCase();
        
        // Para erros de sintaxe, extrai elementos problemáticos específicos
        if (lowerError.contains("string literal is not properly closed")) {
            keywords.addAll(extractStringLiteralKeywords(errorLog));
        }
        
        // Para erros de variável, extrai os nomes das variáveis
        if (lowerError.contains("cannot be resolved")) {
            keywords.addAll(extractVariableKeywords(errorLog));
        }
        
        // Para erros XML, extrai nomes de layout/view
        if (lowerError.contains("xml") && lowerError.contains("error")) {
            keywords.addAll(extractXMLKeywords(errorLog));
        }
        
        // Para erros de método, extrai nomes de métodos
        if (lowerError.contains("method") && lowerError.contains("not found")) {
            keywords.addAll(extractMethodKeywords(errorLog));
        }
        
        // Para erros de importação, extrai nomes de pacotes/classes
        if (lowerError.contains("package") || lowerError.contains("import")) {
            keywords.addAll(extractImportKeywords(errorLog));
        }
        
        // Extração geral inteligente - procura por identificadores que podem ser relevantes
        keywords.addAll(extractGeneralIdentifiers(errorLog));
        
        // Remove duplicatas e strings vazias
        return keywords.stream()
                .distinct()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Analisa o tipo de erro no log
     */
    public ErrorType analyzeErrorType(String errorLog) {
        if (errorLog == null) return ErrorType.UNKNOWN;
        
        String lowerError = errorLog.toLowerCase();
        
        // Erros de sintaxe
        if (lowerError.contains("string literal is not properly closed") ||
            lowerError.contains("missing semicolon") ||
            lowerError.contains("unexpected token") ||
            lowerError.contains("syntax error")) {
            return ErrorType.SYNTAX_ERROR;
        }
        
        // Erros de variável
        if (lowerError.contains("cannot be resolved") ||
            lowerError.contains("symbol not found")) {
            return ErrorType.VARIABLE_ERROR;
        }
        
        // Erros XML
        if (lowerError.contains("xml") && lowerError.contains("error")) {
            return ErrorType.XML_ERROR;
        }
        
        // Erros de importação
        if (lowerError.contains("package") && lowerError.contains("does not exist") ||
            lowerError.contains("import") && lowerError.contains("not found")) {
            return ErrorType.IMPORT_ERROR;
        }
        
        // Erros de método
        if (lowerError.contains("method") && lowerError.contains("not found")) {
            return ErrorType.METHOD_ERROR;
        }
        
        return ErrorType.UNKNOWN;
    }
    
    /**
     * Encontra blocos correspondentes para os nomes dados
     */
    public List<BlockMatch> findMatchingBlocks(String decrypted, Set<String> errorVars, ErrorType errorType) {
        List<BlockMatch> result = new ArrayList<>();
        String[] lines = decrypted.split("\\r?\\n");

        String currentHeader = null;
        for (int i = 0; i < lines.length; i++) {
            String raw = lines[i];
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.startsWith("@")) {
                currentHeader = trimmed; // lembra o cabeçalho para blocos subsequentes
                continue;
            }

            // tenta fazer parse do JSON
            try {
                JSONObject obj = new JSONObject(trimmed);
                int id = getIntFromJsonField(obj, "id", -1);
                if (id == -1) continue;

                // verifica array de parâmetros primeiro (remove aspas e compara sem case-sensitive)
                boolean matched = false;
                JSONArray params = obj.optJSONArray("parameters");
                if (params != null) {
                    for (int k = 0; k < params.length(); k++) {
                        String p = params.optString(k, "");
                        if (p == null) p = "";
                        p = p.replace("\"", "").trim(); // remove aspas se houver
                        for (String var : errorVars) {
                            if (isErrorMatch(p, var)) {
                                matched = true;
                                break;
                            }
                        }
                        if (matched) break;
                    }
                }

                // fallback: verifica opCode/spec/objeto inteiro com word-boundary
                if (!matched) {
                    String objStr = obj.toString();
                    for (String var : errorVars) {
                        if (isErrorMatch(objStr, var)) {
                            matched = true;
                            break;
                        }
                    }
                }

                if (matched) {
                    BlockMatch bm = new BlockMatch();
                    bm.id = id;
                    bm.lineIndex = i;
                    bm.header = currentHeader != null ? currentHeader : "(no-header)";
                    bm.rawLine = raw;
                    bm.obj = obj;
                    result.add(bm);
                }

            } catch (JSONException je) {
                // ignora não-json
            }
        }

        return result;
    }
    
    /**
     * Executa a exclusão de blocos e salva
     */
    public void performDeleteBlocksAndSave(Set<Integer> toDelete) {
        try {
            if (toDelete == null || toDelete.isEmpty()) {
                Toast.makeText(context, "No blocks selected for deletion", Toast.LENGTH_SHORT).show();
                return;
            }

            String decrypted = decryptLogicFile(logicPath);
            if (decrypted == null) {
                Toast.makeText(context, "Logic file read failed", Toast.LENGTH_LONG).show();
                return;
            }

            // mantém linhas exatamente (preserva espaços em branco)
            List<String> originalLines = Arrays.asList(decrypted.split("\\r?\\n", -1));

            // estrutura de informação de linha
            class LineInfo {
                String raw;
                boolean isHeader;
                boolean isJson;
                Integer id; // se json
                JSONObject obj; // json parseado se houver
                String headerForLine;
            }

            List<LineInfo> lineInfos = new ArrayList<>();
            Map<Integer, LineInfo> idToLineInfo = new LinkedHashMap<>();
            Map<Integer, Integer> originalNextMap = new HashMap<>();

            String currentHeader = null;
            for (String ln : originalLines) {
                LineInfo li = new LineInfo();
                li.raw = ln;
                li.isHeader = false;
                li.isJson = false;
                li.id = null;
                li.obj = null;
                li.headerForLine = currentHeader;

                String trimmed = ln.trim();
                if (trimmed.startsWith("@")) {
                    currentHeader = trimmed;
                    li.isHeader = true;
                    li.headerForLine = currentHeader;
                    lineInfos.add(li);
                    continue;
                }

                if (trimmed.isEmpty()) {
                    lineInfos.add(li);
                    continue;
                }

                // tenta fazer parse do json
                try {
                    JSONObject obj = new JSONObject(trimmed);
                    int id = getIntFromJsonField(obj, "id", -1);
                    int next = getIntFromJsonField(obj, "nextBlock", -1);
                    if (id != -1) {
                        li.isJson = true;
                        li.id = id;
                        li.obj = obj;
                        idToLineInfo.put(id, li);
                        originalNextMap.put(id, next);
                        lineInfos.add(li);
                        continue;
                    }
                } catch (JSONException je) {
                    // não é uma linha json
                }

                // fallback: linha normal não-json
                lineInfos.add(li);
            }

            if (idToLineInfo.isEmpty()) {
                Toast.makeText(context, "No json blocks to edit", Toast.LENGTH_LONG).show();
                return;
            }

            // filtra apenas os ids que realmente existem
            Set<Integer> actuallyFound = new HashSet<>();
            for (Integer id : toDelete) if (idToLineInfo.containsKey(id)) actuallyFound.add(id);

            if (actuallyFound.isEmpty()) {
                Toast.makeText(context, "No matching block ids found in logic (already removed?)", Toast.LENGTH_LONG).show();
                return;
            }

            // backup
            try {
                File src = new File(logicPath);
                if (src.exists()) {
                    File bak = new File(logicPath + ".bak");
                    copyFile(src, bak);
                }
            } catch (Exception e) { /* ignora erros de backup */ }

            // helper para resolver next: pula sobre ids deletados
            java.util.function.IntUnaryOperator resolveNext = new java.util.function.IntUnaryOperator() {
                @Override
                public int applyAsInt(int startId) {
                    int cur = startId;
                    Set<Integer> visited = new HashSet<>();
                    while (cur != -1 && actuallyFound.contains(cur) && !visited.contains(cur)) {
                        visited.add(cur);
                        cur = originalNextMap.getOrDefault(cur, -1);
                    }
                    return cur;
                }
            };

            // atualiza nextBlock para blocos restantes que apontam para IDs deletados
            for (Map.Entry<Integer, LineInfo> entry : idToLineInfo.entrySet()) {
                int id = entry.getKey();
                LineInfo li = entry.getValue();
                if (actuallyFound.contains(id)) continue; // pula os deletados
                if (li.obj == null) continue;
                int next = getIntFromJsonField(li.obj, "nextBlock", -1);
                if (actuallyFound.contains(next)) {
                    int newNext = resolveNext.applyAsInt(next);
                    try {
                        li.obj.put("nextBlock", newNext);
                    } catch (JSONException je) { /* ignora */ }
                }
            }

            // reconstrói conteúdo: mantém cabeçalhos e não-json como estão, pula linhas json deletadas e escreve json atualizado para outros
            StringBuilder newContent = new StringBuilder();
            for (LineInfo li : lineInfos) {
                if (li.isHeader) {
                    newContent.append(li.raw).append("\n");
                    continue;
                }
                if (li.isJson) {
                    if (li.id != null && actuallyFound.contains(li.id)) {
                        // pula este bloco deletado (não anexa)
                        continue;
                    } else {
                        if (li.obj != null) newContent.append(li.obj.toString()).append("\n");
                        else newContent.append(li.raw).append("\n");
                    }
                } else {
                    // linha normal ou em branco
                    newContent.append(li.raw).append("\n");
                }
            }

            // salva
            encryptAndSave(logicPath, newContent.toString());

            // feito
            Toast.makeText(context, "Deleted blocks: " + actuallyFound.toString(), Toast.LENGTH_SHORT).show();
            
            // Atualizar projeto automaticamente
            refreshProject();

        } catch (Exception ex) {
            Toast.makeText(context, "Delete failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
    
    // Métodos auxiliares privados
    
    private List<String> extractStringLiteralKeywords(String errorLog) {
        List<String> keywords = new ArrayList<>();
        
        // Procura por nomes de variáveis próximos ao erro
        Pattern varPattern = Pattern.compile("([A-Za-z][A-Za-z0-9_]*)\\s*=", Pattern.CASE_INSENSITIVE);
        Matcher varMatcher = varPattern.matcher(errorLog);
        while (varMatcher.find()) {
            keywords.add(varMatcher.group(1));
        }
        
        // Procura por chamadas de método que podem conter a string problemática
        Pattern methodPattern = Pattern.compile("([A-Za-z][A-Za-z0-9_]*)\\s*\\(", Pattern.CASE_INSENSITIVE);
        Matcher methodMatcher = methodPattern.matcher(errorLog);
        while (methodMatcher.find()) {
            keywords.add(methodMatcher.group(1));
        }
        
        return keywords;
    }
    
    private List<String> extractVariableKeywords(String errorLog) {
        List<String> keywords = new ArrayList<>();
        
        // Extrai nomes de variáveis que não podem ser resolvidos
        Pattern varPattern = Pattern.compile("([A-Za-z][A-Za-z0-9_]*)\\s+cannot be resolved", Pattern.CASE_INSENSITIVE);
        Matcher varMatcher = varPattern.matcher(errorLog);
        while (varMatcher.find()) {
            keywords.add(varMatcher.group(1));
        }
        
        // Extrai referências de binding
        Pattern bindingPattern = Pattern.compile("binding\\.([A-Za-z][A-Za-z0-9_]*)", Pattern.CASE_INSENSITIVE);
        Matcher bindingMatcher = bindingPattern.matcher(errorLog);
        while (bindingMatcher.find()) {
            keywords.add(bindingMatcher.group(1));
        }
        
        return keywords;
    }
    
    private List<String> extractXMLKeywords(String errorLog) {
        List<String> keywords = new ArrayList<>();
        
        // Extrai IDs de view do XML
        Pattern viewIdPattern = Pattern.compile("android:id=\"@\\+id/([A-Za-z][A-Za-z0-9_]*)", Pattern.CASE_INSENSITIVE);
        Matcher viewIdMatcher = viewIdPattern.matcher(errorLog);
        while (viewIdMatcher.find()) {
            keywords.add(viewIdMatcher.group(1));
        }
        
        // Extrai nomes de layout
        Pattern layoutPattern = Pattern.compile("layout/([A-Za-z][A-Za-z0-9_]*)", Pattern.CASE_INSENSITIVE);
        Matcher layoutMatcher = layoutPattern.matcher(errorLog);
        while (layoutMatcher.find()) {
            keywords.add(layoutMatcher.group(1));
        }
        
        return keywords;
    }
    
    private List<String> extractMethodKeywords(String errorLog) {
        List<String> keywords = new ArrayList<>();
        
        // Extrai nomes de métodos
        Pattern methodPattern = Pattern.compile("method\\s+([A-Za-z][A-Za-z0-9_]*)\\s*\\(", Pattern.CASE_INSENSITIVE);
        Matcher methodMatcher = methodPattern.matcher(errorLog);
        while (methodMatcher.find()) {
            keywords.add(methodMatcher.group(1));
        }
        
        return keywords;
    }
    
    private List<String> extractImportKeywords(String errorLog) {
        List<String> keywords = new ArrayList<>();
        
        // Extrai nomes de pacotes
        Pattern packagePattern = Pattern.compile("package\\s+([A-Za-z][A-Za-z0-9_.]*)", Pattern.CASE_INSENSITIVE);
        Matcher packageMatcher = packagePattern.matcher(errorLog);
        while (packageMatcher.find()) {
            String pkg = packageMatcher.group(1);
            // Divide o pacote e adiciona partes individuais
            String[] parts = pkg.split("\\.");
            for (String part : parts) {
                if (part.length() > 2) { // Apenas adiciona partes significativas
                    keywords.add(part);
                }
            }
        }
        
        return keywords;
    }
    
    private List<String> extractGeneralIdentifiers(String errorLog) {
        List<String> keywords = new ArrayList<>();
        
        // Extrai identificadores Java (variáveis, métodos, classes)
        Pattern identifierPattern = Pattern.compile("\\b([A-Za-z][A-Za-z0-9_]{2,})\\b");
        Matcher identifierMatcher = identifierPattern.matcher(errorLog);
        while (identifierMatcher.find()) {
            String identifier = identifierMatcher.group(1);
            // Filtra palavras-chave Java comuns e identificadores curtos
            if (!isJavaKeyword(identifier) && identifier.length() > 2) {
                keywords.add(identifier);
            }
        }
        
        return keywords;
    }
    
    private boolean isJavaKeyword(String word) {
        String[] keywords = {
            "public", "private", "protected", "static", "final", "abstract", "class", "interface",
            "extends", "implements", "if", "else", "for", "while", "do", "switch", "case",
            "break", "continue", "return", "try", "catch", "finally", "throw", "throws",
            "new", "this", "super", "null", "true", "false", "int", "long", "short", "byte",
            "char", "boolean", "float", "double", "void", "String", "Object", "System"
        };
        
        for (String keyword : keywords) {
            if (keyword.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isErrorMatch(String text, String errorVar) {
        if (text == null || errorVar == null) return false;
        
        // Lida com tipos de erro especiais dos padrões CompileLogHelper
        if (errorVar.startsWith("ERROR_") || errorVar.startsWith("WARNING_")) {
            // Para erros/avisos numerados, verifica se o texto contém o número do erro
            String errorNum = errorVar.substring(errorVar.indexOf("_") + 1);
            return text.contains(errorNum);
        }
        
        if (errorVar.equals("XML_ERROR")) {
            // Para erros XML, verifica se o texto contém indicadores de erro relacionados ao XML
            return text.toLowerCase().contains("error") && 
                   (text.toLowerCase().contains("xml") || text.toLowerCase().contains("layout"));
        }
        
        // Para nomes de variáveis regulares, usa correspondência melhorada
        String cleanText = text.replace("\"", "").trim();
        String cleanVar = errorVar.replace("\"", "").trim();
        
        // Correspondência exata (sem case-sensitive)
        if (cleanText.equalsIgnoreCase(cleanVar)) return true;
        
        // Correspondência com aspas
        if (cleanText.equalsIgnoreCase("\"" + cleanVar + "\"")) return true;
        
        // Correspondência de contém para correspondências parciais
        if (cleanText.toLowerCase().contains(cleanVar.toLowerCase())) return true;
        
        // Correspondência de word boundary para correspondência mais precisa
        Pattern wp = Pattern.compile("\\b" + Pattern.quote(cleanVar) + "\\b", Pattern.CASE_INSENSITIVE);
        if (wp.matcher(cleanText).find()) return true;
        
        // Lida com nomes de pacote/classe com pontos
        if (cleanVar.contains(".")) {
            String[] parts = cleanVar.split("\\.");
            boolean allPartsMatch = true;
            for (String part : parts) {
                if (!cleanText.toLowerCase().contains(part.toLowerCase())) {
                    allPartsMatch = false;
                    break;
                }
            }
            if (allPartsMatch) return true;
        }
        
        return false;
    }
    
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
    
    private int getIntFromJsonField(JSONObject obj, String key, int def) {
        if (obj == null || !obj.has(key)) return def;
        try {
            Object o = obj.get(key);
            if (o instanceof Number) return ((Number) o).intValue();
            String s = obj.optString(key, "");
            if (s == null || s.length() == 0) return def;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                return def;
            }
        } catch (JSONException je) {
            return def;
        }
    }
    
    /**
     * Atualiza o projeto automaticamente após aplicar correções
     */
    private void refreshProject() {
        try {
            // Enviar broadcast para atualizar o projeto
            android.content.Intent refreshIntent = new android.content.Intent("pro.sketchware.refresh_project");
            refreshIntent.putExtra("refresh_type", "logic_updated");
            refreshIntent.putExtra("logic_path", logicPath);
            context.sendBroadcast(refreshIntent);
            
            // Também tentar atualizar via intent local
            android.content.Intent localRefreshIntent = new android.content.Intent("com.besome.sketch.refresh_project");
            localRefreshIntent.putExtra("refresh_type", "logic_updated");
            localRefreshIntent.putExtra("logic_path", logicPath);
            context.sendBroadcast(localRefreshIntent);
            
        } catch (Exception e) {
            // Falha silenciosa - o usuário pode recarregar manualmente se necessário
        }
    }
    
    private void copyFile(File src, File dst) throws Exception {
        if (!src.exists()) return;
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
        }
    }
    
    // Classes internas
    
    public enum ErrorType {
        SYNTAX_ERROR,      // String literal não fechada, ponto e vírgula ausente, etc.
        VARIABLE_ERROR,    // Variável não encontrada, não pode ser resolvida
        XML_ERROR,         // Erros de parsing XML
        IMPORT_ERROR,      // Erros de importação/pacote
        METHOD_ERROR,      // Método não encontrado
        UNKNOWN           // Outros tipos
    }
    
    public static class BlockMatch {
        public int id;
        public int lineIndex; // índice baseado em 0 nas linhas descriptografadas
        public String header; // ex: @MainActivity.java_button2_onClick
        public String rawLine; // linha json original (mantida mas não mostramos em diálogos)
        public JSONObject obj; // objeto JSON parseado
    }
}
