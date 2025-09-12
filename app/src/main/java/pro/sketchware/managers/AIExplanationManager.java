package pro.sketchware.managers;

import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pro.sketchware.ai.GroqClient;
import pro.sketchware.utility.SketchwareUtil;

/**
 * Manager responsável por gerenciar explicações de logs de compilação usando IA
 */
public class AIExplanationManager {
    
    private final Context context;
    private final TranslationManager translationManager;
    
    public AIExplanationManager(Context context) {
        this.context = context;
        this.translationManager = new TranslationManager(context);
    }
    
    /**
     * Mostra seletor de idioma e executa explicação com IA
     */
    public void showLanguagePickerAndExplain(String errorLog, String scId) {
        if (errorLog == null || errorLog.trim().isEmpty()) {
            SketchwareUtil.toastError("No log to explain.");
            return;
        }
        
        // Labels de idiomas e códigos (mesmo do TranslationManager)
        String[] LANG_NAMES = new String[]{
                "Original (no translation)",
                "English", "Bangla (বাংলা)", "Hindi (हिन्दी)", "Spanish (Español)",
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
                .setTitle("Select language for AI explanation")
                .setSingleChoiceItems(LANG_NAMES, selectedIndex[0], (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    int idx = selectedIndex[0];
                    String target = LANG_CODES[Math.max(0, Math.min(idx, LANG_CODES.length - 1))];
                    explainWithAi(errorLog, target, scId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Fluxo principal de explicação. Se targetLang == "orig" => não traduz.
     * Caso contrário, tenta traduzir a resposta da IA para targetLang.
     */
    public void explainWithAi(String errorLog, String targetLang, String scId) {
        if (errorLog == null || errorLog.trim().isEmpty()) {
            SketchwareUtil.toastError("No log to explain.");
            return;
        }

        var loadingDialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Analyzing log with AI")
                .setMessage("Please wait…")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        new Thread(() -> {
            try {
                String fileContext = tryGetRelatedFileContent(errorLog, scId);

                String system = "You are an expert Android build and Gradle error explainer. " +
                        "Given a raw compile log, identify the root cause in plain language, " +
                        "then provide clear, step-by-step fixes. If multiple issues exist, list them. " +
                        "Prefer actionable instructions (what file to open, code lines to change, Gradle dependencies to add).";
                String user = "Here is the compile log:\n\n" + errorLog + "\n\n" +
                        (fileContext != null ? ("Related file content for context:\n" + fileContext + "\n\n") : "") +
                        "Return in this format:\n" +
                        "1) Summary of cause\n2) How to fix (steps)\n3) Extra tips (if any).";

                var client = new GroqClient();
                var messages = GroqClient.Message.of(system, user);
                String reply = client.chat(messages);

                // padrão para original se não houver resposta
                String toShow = reply;

                if (reply != null && !reply.trim().isEmpty() && !"orig".equalsIgnoreCase(targetLang)) {
                    try {
                        String translated = TranslationManager.StaticHelper.translateTextSync(reply, targetLang);
                        if (translated != null && !translated.trim().isEmpty()) {
                            toShow = translated;
                        } // senão mantém original
                    } catch (Exception e) {
                        // tradução falhou, mostraremos original
                        toShow = reply;
                    }
                }

                final String finalToShow = toShow;
                ((android.app.Activity) context).runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (finalToShow == null || finalToShow.trim().isEmpty()) {
                        SketchwareUtil.toastError("AI returned no content");
                        return;
                    }
                    String plain = TranslationManager.StaticHelper.stripMarkdown(finalToShow);
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("AI explanation (" + (("orig".equalsIgnoreCase(targetLang)) ? "original" : translationManager.getLangNameForCode(targetLang)) + ")")
                            .setMessage(plain)
                            .setPositiveButton("Copy", (d, w) -> {
                                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                if (cm != null) {
                                    cm.setPrimaryClip(ClipData.newPlainText("ai_explanation", plain));
                                    SketchwareUtil.toast("Copied to clipboard");
                                }
                            })
                            .setNegativeButton("Close", null)
                            .show();
                });
            } catch (JSONException | java.io.IOException e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    String msg = String.valueOf(e.getMessage());
                    if (msg != null && (msg.contains("Missing Groq API key") || msg.contains("Groq API error"))) {
                        new MaterialAlertDialogBuilder(context)
                                .setTitle("AI not configured")
                                .setMessage(msg)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    } else {
                        SketchwareUtil.toastError("AI error: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
    
    /**
     * Tenta obter conteúdo de arquivo relacionado baseado no log
     */
    private String tryGetRelatedFileContent(String log, String scId) {
        try {
            List<String> candidates = extractFilePathCandidates(log, scId);
            for (String p : candidates) {
                if (p == null) continue;
                File f = new File(p);
                if (f.exists() && f.isFile() && f.length() > 0) {
                    return readTextFile(p);
                }
            }
        } catch (Exception ignored) { }
        return null;
    }
    
    /**
     * Extrai candidatos de caminho de arquivo do log
     */
    private List<String> extractFilePathCandidates(String log, String scId) {
        List<String> paths = new ArrayList<>();
        // 1) Caminhos absolutos no log
        Matcher abs = Pattern.compile("(/[^\\s:]+\\.(?:java|kt|xml))", Pattern.CASE_INSENSITIVE).matcher(log);
        while (abs.find()) paths.add(abs.group(1));

        // 2) formato javac: SomeFile.java:123:
        Matcher javac = Pattern.compile("([A-Za-z0-9_./\\\\-]+\\.(?:java|kt|xml)):\\d+", Pattern.CASE_INSENSITIVE).matcher(log);
        while (javac.find()) paths.add(javac.group(1));

        // 3) Stacktrace: (Class.java:123) com pacote
        Matcher stack = Pattern.compile("at\\s+([a-zA-Z_][\\w.]+)\\([A-Za-z0-9_]+\\.(java|kt):\\d+\\)").matcher(log);
        if (stack.find()) {
            String pkg = stack.group(1);
            String file = stack.group(2) != null ? stack.group(2) : "";
        }

        // 4) Se caminho relativo: tenta locais comuns de projeto Sketchware
        List<String> normalized = new ArrayList<>();
        for (String p : paths) {
            if (p == null) continue;
            String np = p.replace("\\\\", "/");
            normalized.add(np);
        }

        // 5) Tenta raízes conhecidas
        if (scId != null) {
            String base = "/storage/emulated/0/.sketchware/mysc/" + scId + "/app/src/main/";
            List<String> tryRoots = new ArrayList<>();
            tryRoots.add(base + "java/");
            tryRoots.add(base + "kotlin/");
            tryRoots.add(base + "res/");

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
     * Lê arquivo de texto
     */
    private String readTextFile(String path) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Interface de callback para explicação com IA
     */
    public interface AIExplanationCallback {
        void onExplanationComplete(String explanation);
        void onExplanationError(Exception error);
    }
    
    /**
     * Classe estática para facilitar o uso sem instanciar
     */
    public static class StaticHelper {
        /**
         * Obtém explicação da IA de forma síncrona (use apenas em threads de background)
         */
        public static String getAIExplanationSync(String errorLog, String scId) {
            if (errorLog == null || errorLog.trim().isEmpty()) return null;
            
            try {
                String fileContext = tryGetRelatedFileContentStatic(errorLog, scId);

                String system = "You are an expert Android build and Gradle error explainer. " +
                        "Given a raw compile log, identify the root cause in plain language, " +
                        "then provide clear, step-by-step fixes. If multiple issues exist, list them. " +
                        "Prefer actionable instructions (what file to open, code lines to change, Gradle dependencies to add).";
                String user = "Here is the compile log:\n\n" + errorLog + "\n\n" +
                        (fileContext != null ? ("Related file content for context:\n" + fileContext + "\n\n") : "") +
                        "Return in this format:\n" +
                        "1) Summary of cause\n2) How to fix (steps)\n3) Extra tips (if any).";

                var client = new GroqClient();
                var messages = GroqClient.Message.of(system, user);
                return client.chat(messages);
            } catch (Exception e) {
                return null;
            }
        }
        
        /**
         * Tenta obter conteúdo de arquivo relacionado (versão estática)
         */
        private static String tryGetRelatedFileContentStatic(String log, String scId) {
            try {
                List<String> candidates = extractFilePathCandidatesStatic(log, scId);
                for (String p : candidates) {
                    if (p == null) continue;
                    File f = new File(p);
                    if (f.exists() && f.isFile() && f.length() > 0) {
                        return readTextFileStatic(p);
                    }
                }
            } catch (Exception ignored) { }
            return null;
        }
        
        /**
         * Extrai candidatos de caminho de arquivo (versão estática)
         */
        private static List<String> extractFilePathCandidatesStatic(String log, String scId) {
            List<String> paths = new ArrayList<>();
            Matcher abs = Pattern.compile("(/[^\\s:]+\\.(?:java|kt|xml))", Pattern.CASE_INSENSITIVE).matcher(log);
            while (abs.find()) paths.add(abs.group(1));

            Matcher javac = Pattern.compile("([A-Za-z0-9_./\\\\-]+\\.(?:java|kt|xml)):\\d+", Pattern.CASE_INSENSITIVE).matcher(log);
            while (javac.find()) paths.add(javac.group(1));

            if (scId != null) {
                String base = "/storage/emulated/0/.sketchware/mysc/" + scId + "/app/src/main/";
                List<String> tryRoots = new ArrayList<>();
                tryRoots.add(base + "java/");
                tryRoots.add(base + "kotlin/");
                tryRoots.add(base + "res/");

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
         * Lê arquivo de texto (versão estática)
         */
        private static String readTextFileStatic(String path) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
