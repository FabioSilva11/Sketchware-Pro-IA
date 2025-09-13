package pro.sketchware.managers;

import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import pro.sketchware.utility.SketchwareUtil;

/**
 * Manager responsável por gerenciar traduções de texto usando Google Translate
 */
public class TranslationManager {
    
    private final Context context;
    
    // Labels de idiomas e códigos (adicione/remova conforme desejar)
    private static final String[] LANG_NAMES = new String[]{
            "Original (no translation)",
            "English", "Bangla (বাংলা)", "Hindi (हिन्दी)", "Spanish (Español)",
            "French (Français)", "German (Deutsch)", "Chinese - Simplified (简体中文)",
            "Arabic (العربية)", "Portuguese (Português)", "Russian (Русский)",
            "Japanese (日本語)", "Korean (한국어)", "Italian (Italiano)", "Turkish (Türkçe)",
            "Dutch (Nederlands)", "Português (Brasil)"
    };

    private static final String[] LANG_CODES = new String[]{
            "orig",
            "en", "bn", "hi", "es",
            "fr", "de", "zh-CN",
            "ar", "pt", "ru",
            "ja", "ko", "it", "tr",
            "nl", "pt-BR"
    };
    
    public TranslationManager(Context context) {
        this.context = context;
    }
    
    /**
     * Mostra um seletor de idioma e executa a tradução
     */
    public void showLanguagePickerAndTranslate(String textToTranslate, TranslationCallback callback) {
        if (textToTranslate == null || textToTranslate.trim().isEmpty()) {
            SketchwareUtil.toastError("No text to translate.");
            return;
        }
        
        // seleção padrão = 0 => Original (sem tradução)
        final int[] selectedIndex = {0};

        new MaterialAlertDialogBuilder(context)
                .setTitle("Select language")
                .setSingleChoiceItems(LANG_NAMES, selectedIndex[0], (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    int idx = selectedIndex[0];
                    String target = LANG_CODES[Math.max(0, Math.min(idx, LANG_CODES.length - 1))];
                    translateText(textToTranslate, target, callback);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Traduz texto usando o endpoint "translate_a/single" do Google Translate.
     * Retorna texto traduzido ou null em caso de falha.
     *
     * Aviso: Este endpoint é não oficial e pode ser limitado por taxa ou bloqueado.
     */
    public void translateText(String original, String targetLang, TranslationCallback callback) {
        if (original == null || original.trim().isEmpty()) {
            if (callback != null) callback.onTranslationComplete(null);
            return;
        }
        if (targetLang == null || targetLang.trim().isEmpty() || "orig".equalsIgnoreCase(targetLang)) {
            if (callback != null) callback.onTranslationComplete(original);
            return;
        }

        // Validar tamanho do texto para evitar erro 400
        if (original.length() > 5000) {
            if (callback != null) callback.onTranslationError(new Exception("Text too long for translation (max 5000 characters)"));
            return;
        }

        new Thread(() -> {
            try {
                // Limpar e validar texto de entrada
                String cleanText = original.trim();
                if (cleanText.isEmpty()) {
                    if (callback != null) callback.onTranslationComplete(original);
                    return;
                }

                String encoded = URLEncoder.encode(cleanText, StandardCharsets.UTF_8.name());
                
                // Validar tamanho da URL para evitar erro 400
                String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + URLEncoder.encode(targetLang.trim(), "UTF-8") + "&dt=t&q=" + encoded;
                if (urlStr.length() > 2000) { // Limite de URL
                    if (callback != null) callback.onTranslationError(new Exception("Translation request too large"));
                    return;
                }

                HttpURLConnection conn = null;
                BufferedReader br = null;
                try {
                    URL url = new URL(urlStr);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(20000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

                    int code = conn.getResponseCode();
                    
                    // Tratamento específico para erro 400
                    if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
                        String errorMsg = "Bad request (400) - Invalid translation parameters";
                        try {
                            if (conn.getErrorStream() != null) {
                                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                                StringBuilder errorSb = new StringBuilder();
                                String errorLine;
                                while ((errorLine = errorReader.readLine()) != null) {
                                    errorSb.append(errorLine);
                                }
                                String errorBody = errorSb.toString();
                                if (errorBody.contains("INVALID_ARGUMENT")) {
                                    errorMsg = "Invalid language code or text format";
                                } else if (errorBody.contains("QUOTA_EXCEEDED")) {
                                    errorMsg = "Translation quota exceeded";
                                }
                            }
                        } catch (Exception ignored) {}
                        
                        if (callback != null) callback.onTranslationError(new Exception(errorMsg));
                        return;
                    }
                    
                    if (code != HttpURLConnection.HTTP_OK) {
                        String errorMsg = "Translation failed with HTTP " + code;
                        if (code == 429) {
                            errorMsg = "Translation rate limit exceeded";
                        } else if (code == 403) {
                            errorMsg = "Translation access forbidden";
                        }
                        if (callback != null) callback.onTranslationError(new Exception(errorMsg));
                        return;
                    }

                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    String resp = sb.toString();
                    
                    if (resp == null || resp.trim().isEmpty()) {
                        if (callback != null) callback.onTranslationComplete(null);
                        return;
                    }
                    
                    // Parse da resposta do array JSON: [[["segmento traduzido", "orig", ...], ...], ...]
                    JSONArray root = new JSONArray(resp);
                    if (root.length() > 0) {
                        JSONArray sentences = root.optJSONArray(0);
                        if (sentences != null) {
                            StringBuilder result = new StringBuilder();
                            for (int i = 0; i < sentences.length(); i++) {
                                JSONArray seg = sentences.optJSONArray(i);
                                if (seg != null && seg.length() > 0) {
                                    String part = seg.optString(0, "");
                                    if (part != null && !part.isEmpty()) {
                                        result.append(part);
                                    }
                                }
                            }
                            String translated = result.toString().trim();
                            if (callback != null) callback.onTranslationComplete(translated);
                            return;
                        }
                    }
                    if (callback != null) callback.onTranslationComplete(null);
                } finally {
                    try {
                        if (br != null) br.close();
                        if (conn != null) conn.disconnect();
                    } catch (Exception ignored) { }
                }
            } catch (Exception e) {
                if (callback != null) callback.onTranslationError(e);
            }
        }).start();
    }
    
    /**
     * Remove formatação Markdown do texto
     */
    public String stripMarkdown(String input) {
        if (input == null) return "";
        // Remove cercas de código
        String out = input.replaceAll("(?s)```+\\w*\\n", "");
        out = out.replace("```", "");
        // Remove backticks de código inline
        out = out.replace("`", "");
        // Substitui cabeçalhos por texto simples
        out = out.replaceAll("(?m)^#{1,6}\\s*", "");
        // Marcadores de negrito/itálico
        out = out.replace("**", "").replace("__", "").replace("*", "").replace("_", "");
        // Links: [texto](url) -> texto (url)
        out = out.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "$1 ($2)");
        // Marcadores: mantém traços
        return out.trim();
    }
    
    /**
     * Obtém o nome do idioma para o código fornecido
     */
    public String getLangNameForCode(String code) {
        if (code == null) return "unknown";
        for (int i = 0; i < LANG_CODES.length; i++) {
            if (code.equalsIgnoreCase(LANG_CODES[i])) return LANG_NAMES[i];
        }
        return code;
    }
    
    /**
     * Mostra um diálogo com o texto traduzido e opção de copiar
     */
    public void showTranslationDialog(String originalText, String translatedText, String targetLang) {
        if (translatedText == null || translatedText.trim().isEmpty()) {
            SketchwareUtil.toastError("Translation failed or returned empty result");
            return;
        }
        
        String plain = stripMarkdown(translatedText);
        String title = "Translation (" + (("orig".equalsIgnoreCase(targetLang)) ? "original" : getLangNameForCode(targetLang)) + ")";
        
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(plain)
                .setPositiveButton("Copy", (d, w) -> {
                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("translation", plain));
                        SketchwareUtil.toast("Copied to clipboard");
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }
    
    /**
     * Interface de callback para tradução
     */
    public interface TranslationCallback {
        void onTranslationComplete(String translatedText);
        void onTranslationError(Exception error);
    }
    
    /**
     * Classe estática para facilitar o uso sem instanciar
     */
    public static class StaticHelper {
        /**
         * Traduz texto de forma síncrona (use apenas em threads de background)
         */
        public static String translateTextSync(String original, String targetLang) {
            if (original == null || original.trim().isEmpty()) return null;
            if (targetLang == null || targetLang.trim().isEmpty() || "orig".equalsIgnoreCase(targetLang)) {
                return original;
            }

            // Validar tamanho do texto para evitar erro 400
            if (original.length() > 5000) {
                return null; // Texto muito longo
            }

            try {
                // Limpar e validar texto de entrada
                String cleanText = original.trim();
                if (cleanText.isEmpty()) {
                    return original;
                }

                String encoded = URLEncoder.encode(cleanText, StandardCharsets.UTF_8.name());
                String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + URLEncoder.encode(targetLang.trim(), "UTF-8") + "&dt=t&q=" + encoded;
                
                // Validar tamanho da URL para evitar erro 400
                if (urlStr.length() > 2000) {
                    return null; // URL muito longa
                }

                HttpURLConnection conn = null;
                BufferedReader br = null;
                try {
                    URL url = new URL(urlStr);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(20000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

                    int code = conn.getResponseCode();
                    
                    // Tratamento específico para erro 400
                    if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
                        return null; // Bad request
                    }
                    
                    if (code != HttpURLConnection.HTTP_OK) {
                        return null; // Outros erros HTTP
                    }

                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    String resp = sb.toString();
                    
                    if (resp == null || resp.trim().isEmpty()) {
                        return null;
                    }
                    
                    JSONArray root = new JSONArray(resp);
                    if (root.length() > 0) {
                        JSONArray sentences = root.optJSONArray(0);
                        if (sentences != null) {
                            StringBuilder result = new StringBuilder();
                            for (int i = 0; i < sentences.length(); i++) {
                                JSONArray seg = sentences.optJSONArray(i);
                                if (seg != null && seg.length() > 0) {
                                    String part = seg.optString(0, "");
                                    if (part != null && !part.isEmpty()) {
                                        result.append(part);
                                    }
                                }
                            }
                            return result.toString().trim();
                        }
                    }
                    return null;
                } finally {
                    try {
                        if (br != null) br.close();
                        if (conn != null) conn.disconnect();
                    } catch (Exception ignored) { }
                }
            } catch (Exception e) {
                return null;
            }
        }
        
        /**
         * Remove formatação Markdown do texto
         */
        public static String stripMarkdown(String input) {
            if (input == null) return "";
            String out = input.replaceAll("(?s)```+\\w*\\n", "");
            out = out.replace("```", "");
            out = out.replace("`", "");
            out = out.replaceAll("(?m)^#{1,6}\\s*", "");
            out = out.replace("**", "").replace("__", "").replace("*", "").replace("_", "");
            out = out.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "$1 ($2)");
            return out.trim();
        }
    }
}
