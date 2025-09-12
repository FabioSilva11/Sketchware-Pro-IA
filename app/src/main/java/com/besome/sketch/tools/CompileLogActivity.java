package com.besome.sketch.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import mod.hey.studios.util.CompileLogHelper;
import mod.hey.studios.util.Helper;
import mod.jbk.diagnostic.CompileErrorSaver;
import mod.jbk.util.AddMarginOnApplyWindowInsetsListener;
import pro.sketchware.databinding.CompileLogBinding;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.ai.GroqClient;

import org.json.JSONException;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class CompileLogActivity extends BaseAppCompatActivity {

    private static final String PREFERENCE_WRAPPED_TEXT = "wrapped_text";
    private static final String PREFERENCE_USE_MONOSPACED_FONT = "use_monospaced_font";
    private static final String PREFERENCE_FONT_SIZE = "font_size";
    private String LOGIC_PATH;

    private CompileErrorSaver compileErrorSaver;
    private SharedPreferences logViewerPreferences;

    private CompileLogBinding binding;
    private String lastLogRaw;

    // language labels and codes (add/remove as you like)
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);
        binding = CompileLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.optionsLayout,
                new AddMarginOnApplyWindowInsetsListener(WindowInsetsCompat.Type.navigationBars(), WindowInsetsCompat.CONSUMED));

        logViewerPreferences = getPreferences(Context.MODE_PRIVATE);

        binding.topAppBar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));

        if (getIntent().getBooleanExtra("showingLastError", false)) {
            binding.topAppBar.setTitle("Last compile log");
        } else {
            binding.topAppBar.setTitle("Compile log");
        }

        String sc_id = getIntent().getStringExtra("sc_id");
        if (sc_id == null) {
            finish();
            return;
        }

        compileErrorSaver = new CompileErrorSaver(sc_id);

        if (compileErrorSaver.logFileExists()) {
            binding.clearButton.setOnClickListener(v -> {
                if (compileErrorSaver.logFileExists()) {
                    compileErrorSaver.deleteSavedLogs();
                    getIntent().removeExtra("error");
                    SketchwareUtil.toast("Compile logs have been cleared.");
                } else {
                    SketchwareUtil.toast("No compile logs found.");
                }

                setErrorText();
            });
        }

        final String wrapTextLabel = "Wrap text";
        final String monospacedFontLabel = "Monospaced font";
        final String fontSizeLabel = "Font size";

        PopupMenu options = new PopupMenu(this, binding.formatButton);
        options.getMenu().add(wrapTextLabel).setCheckable(true).setChecked(getWrappedTextPreference());
        options.getMenu().add(monospacedFontLabel).setCheckable(true).setChecked(getMonospacedFontPreference());
        options.getMenu().add(fontSizeLabel);

        options.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getTitle().toString()) {
                case wrapTextLabel -> {
                    menuItem.setChecked(!menuItem.isChecked());
                    toggleWrapText(menuItem.isChecked());
                }
                case monospacedFontLabel -> {
                    menuItem.setChecked(!menuItem.isChecked());
                    toggleMonospacedText(menuItem.isChecked());
                }
                case fontSizeLabel -> changeFontSizeDialog();
                default -> {
                    return false;
                }
            }

            return true;
        });

        binding.formatButton.setOnClickListener(v -> options.show());

        // Explain with AI
        binding.aiExplainButton.setOnClickListener(v -> showLanguagePickerAndExplain());

        applyLogViewerPreferences();

        setErrorText();

        LOGIC_PATH = "/storage/emulated/0/.sketchware/data/" + sc_id + "/logic";

        binding.errorFixButton.setOnClickListener(v -> {
            errorChecker(binding.tvCompileLog.getText().toString());
        });

    }
    private void setErrorText() {
        String error = getIntent().getStringExtra("error");
        if (error == null) error = compileErrorSaver.getLogsFromFile();
        if (error == null) {
            binding.noContentLayout.setVisibility(View.VISIBLE);
            binding.optionsLayout.setVisibility(View.GONE);
            return;
        }

        binding.optionsLayout.setVisibility(View.VISIBLE);
        binding.noContentLayout.setVisibility(View.GONE);

        lastLogRaw = error;
        binding.tvCompileLog.setText(CompileLogHelper.getColoredLogs(this, error));
        binding.tvCompileLog.setTextIsSelectable(true);
    }

    private void applyLogViewerPreferences() {
        toggleWrapText(getWrappedTextPreference());
        toggleMonospacedText(getMonospacedFontPreference());
        binding.tvCompileLog.setTextSize(getFontSizePreference());
    }

    private boolean getWrappedTextPreference() {
        return logViewerPreferences.getBoolean(PREFERENCE_WRAPPED_TEXT, false);
    }

    private boolean getMonospacedFontPreference() {
        return logViewerPreferences.getBoolean(PREFERENCE_USE_MONOSPACED_FONT, true);
    }

    private int getFontSizePreference() {
        return logViewerPreferences.getInt(PREFERENCE_FONT_SIZE, 11);
    }

    private void toggleWrapText(boolean isChecked) {
        logViewerPreferences.edit().putBoolean(PREFERENCE_WRAPPED_TEXT, isChecked).apply();

        if (isChecked) {
            binding.errVScroll.removeAllViews();
            if (binding.tvCompileLog.getParent() != null) {
                ((ViewGroup) binding.tvCompileLog.getParent()).removeView(binding.tvCompileLog);
            }
            binding.errVScroll.addView(binding.tvCompileLog);
        } else {
            binding.errVScroll.removeAllViews();
            if (binding.tvCompileLog.getParent() != null) {
                ((ViewGroup) binding.tvCompileLog.getParent()).removeView(binding.tvCompileLog);
            }
            binding.errHScroll.removeAllViews();
            binding.errHScroll.addView(binding.tvCompileLog);
            binding.errVScroll.addView(binding.errHScroll);
        }
    }

    private void toggleMonospacedText(boolean isChecked) {
        logViewerPreferences.edit().putBoolean(PREFERENCE_USE_MONOSPACED_FONT, isChecked).apply();

        if (isChecked) {
            binding.tvCompileLog.setTypeface(Typeface.MONOSPACE);
        } else {
            binding.tvCompileLog.setTypeface(Typeface.DEFAULT);
        }
    }

    private void changeFontSizeDialog() {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(10); //Must not be less than setValue(), which is currently 11 in compile_log.xml
        picker.setMaxValue(70);
        picker.setWrapSelectorWheel(false);
        picker.setValue(getFontSizePreference());

        LinearLayout layout = new LinearLayout(this);
        layout.addView(picker, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select font size")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    logViewerPreferences.edit().putInt(PREFERENCE_FONT_SIZE, picker.getValue()).apply();

                    binding.tvCompileLog.setTextSize((float) picker.getValue());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Show a language picker dialog first, then run explainWithAi flow.
     */
    private void showLanguagePickerAndExplain() {
        // default selection = 0 => Original (no translation)
        final int[] selectedIndex = {0};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select language")
                .setSingleChoiceItems(LANG_NAMES, selectedIndex[0], (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    int idx = selectedIndex[0];
                    String target = LANG_CODES[Math.max(0, Math.min(idx, LANG_CODES.length - 1))];
                    explainWithAi(target);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Main explain flow. If targetLang == "orig" => don't translate.
     * Otherwise try to translate AI reply into targetLang.
     */
    private void explainWithAi(String targetLang) {
        if (lastLogRaw == null || lastLogRaw.trim().isEmpty()) {
            SketchwareUtil.toastError("No log to explain.");
            return;
        }

        var loadingDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Analyzing log with AI")
                .setMessage("Please wait…")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        new Thread(() -> {
            try {
                String scId = getIntent().getStringExtra("sc_id");
                String fileContext = tryGetRelatedFileContent(lastLogRaw, scId);

                String system = "You are an expert Android build and Gradle error explainer. " +
                        "Given a raw compile log, identify the root cause in plain language, " +
                        "then provide clear, step-by-step fixes. If multiple issues exist, list them. " +
                        "Prefer actionable instructions (what file to open, code lines to change, Gradle dependencies to add).";
                String user = "Here is the compile log:\n\n" + lastLogRaw + "\n\n" +
                        (fileContext != null ? ("Related file content for context:\n" + fileContext + "\n\n") : "") +
                        "Return in this format:\n" +
                        "1) Summary of cause\n2) How to fix (steps)\n3) Extra tips (if any).";

                var client = new GroqClient();
                var messages = GroqClient.Message.of(system, user);
                String reply = client.chat(messages);

                // default to original if no reply
                String toShow = reply;

                if (reply != null && !reply.trim().isEmpty() && !"orig".equalsIgnoreCase(targetLang)) {
                    try {
                        String translated = translateText(reply, targetLang);
                        if (translated != null && !translated.trim().isEmpty()) {
                            toShow = translated;
                        } // else keep original
                    } catch (Exception e) {
                        // translation failed, we'll show original
                        toShow = reply;
                    }
                }

                final String finalToShow = toShow;
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (finalToShow == null || finalToShow.trim().isEmpty()) {
                        SketchwareUtil.toastError("AI returned no content");
                        return;
                    }
                    String plain = stripMarkdown(finalToShow);
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("AI explanation (" + (("orig".equalsIgnoreCase(targetLang)) ? "original" : getLangNameForCode(targetLang)) + ")")
                            .setMessage(plain)
                            .setPositiveButton("Copy", (d, w) -> {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                if (cm != null) {
                                    cm.setPrimaryClip(ClipData.newPlainText("ai_explanation", plain));
                                    SketchwareUtil.toast("Copied to clipboard");
                                }
                            })
                            .setNegativeButton("Close", null)
                            .show();
                });
            } catch (JSONException | java.io.IOException e) {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    String msg = String.valueOf(e.getMessage());
                    if (msg != null && (msg.contains("Missing Groq API key") || msg.contains("Groq API error"))) {
                        new MaterialAlertDialogBuilder(this)
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
     * Translate text using Google Translate "translate_a/single" endpoint.
     * Returns translated text or null on failure.
     *
     * Warning: This endpoint is unofficial and might be rate-limited or blocked.
     */
    private String translateText(String original, String targetLang) throws Exception {
        if (original == null || original.trim().isEmpty()) return null;
        if (targetLang == null || targetLang.trim().isEmpty() || "orig".equalsIgnoreCase(targetLang)) return original;

        String encoded = URLEncoder.encode(original, StandardCharsets.UTF_8.name());
        // sl=auto to detect source language automatically
        String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + URLEncoder.encode(targetLang, "UTF-8") + "&dt=t&q=" + encoded;

        HttpURLConnection conn = null;
        BufferedReader br = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                return null;
            }

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String resp = sb.toString();
            // Parse JSON array response: [[["translated segment", "orig", ...], ...], ...]
            JSONArray root = new JSONArray(resp);
            if (root.length() > 0) {
                JSONArray sentences = root.optJSONArray(0);
                if (sentences != null) {
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < sentences.length(); i++) {
                        JSONArray seg = sentences.optJSONArray(i);
                        if (seg != null && seg.length() > 0) {
                            String part = seg.optString(0, "");
                            result.append(part);
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
    }

    private String getLangNameForCode(String code) {
        if (code == null) return "unknown";
        for (int i = 0; i < LANG_CODES.length; i++) {
            if (code.equalsIgnoreCase(LANG_CODES[i])) return LANG_NAMES[i];
        }
        return code;
    }

    private String stripMarkdown(String input) {
        if (input == null) return "";
        // Remove code fences
        String out = input.replaceAll("(?s)```+\\w*\\n", "");
        out = out.replace("```", "");
        // Remove inline code backticks
        out = out.replace("`", "");
        // Replace headings with plain text
        out = out.replaceAll("(?m)^#{1,6}\\s*", "");
        // Bold/italic markers
        out = out.replace("**", "").replace("__", "").replace("*", "").replace("_", "");
        // Links: [text](url) -> text (url)
        out = out.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "$1 ($2)");
        // Bullets: keep dashes
        return out.trim();
    }

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

    private List<String> extractFilePathCandidates(String log, String scId) {
        List<String> paths = new ArrayList<>();
        // 1) Absolute paths in log
        Matcher abs = Pattern.compile("(/[^\\s:]+\\.(?:java|kt|xml))", Pattern.CASE_INSENSITIVE).matcher(log);
        while (abs.find()) paths.add(abs.group(1));

        // 2) javac format: SomeFile.java:123:
        Matcher javac = Pattern.compile("([A-Za-z0-9_./\\\\-]+\\.(?:java|kt|xml)):\\d+", Pattern.CASE_INSENSITIVE).matcher(log);
        while (javac.find()) paths.add(javac.group(1));

        // 3) Stacktrace: (Class.java:123) with package
        Matcher stack = Pattern.compile("at\\s+([a-zA-Z_][\\w.]+)\\([A-Za-z0-9_]+\\.(java|kt):\\d+\\)").matcher(log);
        if (stack.find()) {
            String pkg = stack.group(1);
            String file = stack.group(2) != null ? stack.group(2) : "";
        }

        // 4) If relative path: try common Sketchware project locations
        List<String> normalized = new ArrayList<>();
        for (String p : paths) {
            if (p == null) continue;
            String np = p.replace("\\\\", "/");
            normalized.add(np);
        }

        // 5) Try known roots
        if (scId != null) {
            String base = "/storage/emulated/0/.sketchware/mysc/" + scId + "/app/src/main/";
            List<String> tryRoots = new ArrayList<>();
            tryRoots.add(base + "java/");
            tryRoots.add(base + "kotlin/");
            tryRoots.add(base + "res/");

            // If we only have filename, try searching under these roots
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


    private void errorChecker(String errorLog) {
        // parse error log -> find candidate names
        Set<String> errorVars = collectNamesFromErrorLog(errorLog);

        if (errorVars.isEmpty()) {
            Toast.makeText(this, "No unresolved names found in error log", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String decrypted = decryptLogicFile(LOGIC_PATH);
            if (decrypted == null || decrypted.trim().isEmpty()) {
                Toast.makeText(this, "Logic file empty or cannot be read", Toast.LENGTH_LONG).show();
                return;
            }

            List<BlockMatch> matches = findMatchingBlocks(decrypted, errorVars);

            if (matches.isEmpty()) {
                // show friendly dialog explaining scanned names (Material3)
                String scanned = errorVars.toString();
                new MaterialAlertDialogBuilder(this)
                        .setTitle("No matches")
                        .setMessage("Scanned Name " + scanned + "\nNo matching blocks found in logic.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // show list dialog of matches (NO code/JSON shown)
            showMatchesDialog(matches);

        } catch (Exception e) {
            Toast.makeText(this, "Error processing logic: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Collect names like textviewkk from the error log
    private Set<String> collectNamesFromErrorLog(String errorLog) {
        Set<String> names = new HashSet<>();
        if (errorLog == null) return names;

        // pattern: NAME cannot be resolved
        Pattern p1 = Pattern.compile("([A-Za-z0-9_]+)\\s+cannot be resolved", Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(errorLog);
        while (m1.find()) names.add(m1.group(1));

        // binding.NAME style
        Pattern p2 = Pattern.compile("binding\\.([A-Za-z0-9_]+)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(errorLog);
        while (m2.find()) names.add(m2.group(1));

        // optional: The method NAME(
        Pattern p3 = Pattern.compile("The method\\s+([A-Za-z0-9_]+)\\s*\\(", Pattern.CASE_INSENSITIVE);
        Matcher m3 = p3.matcher(errorLog);
        while (m3.find()) names.add(m3.group(1));

        return names;
    }

    // Representation of a matched block
    private static class BlockMatch {
        int id;
        int lineIndex; // 0-based index in decrypted lines
        String header; // e.g., @MainActivity.java_button2_onClick
        String rawLine; // original raw json line (kept but we won't show it in dialogs)
        JSONObject obj; // parsed JSON object
    }

    // Find matching blocks for given names; returns list of BlockMatch
    private List<BlockMatch> findMatchingBlocks(String decrypted, Set<String> errorVars) {
        List<BlockMatch> result = new ArrayList<>();
        String[] lines = decrypted.split("\\r?\\n");

        String currentHeader = null;
        for (int i = 0; i < lines.length; i++) {
            String raw = lines[i];
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.startsWith("@")) {
                currentHeader = trimmed; // remember header for subsequent blocks
                continue;
            }

            // try parse JSON
            try {
                JSONObject obj = new JSONObject(trimmed);
                int id = getIntFromJsonField(obj, "id", -1);
                if (id == -1) continue;

                // check parameters array first (strip quotes and compare case-insensitive)
                boolean matched = false;
                JSONArray params = obj.optJSONArray("parameters");
                if (params != null) {
                    for (int k = 0; k < params.length(); k++) {
                        String p = params.optString(k, "");
                        if (p == null) p = "";
                        p = p.replace("\"", "").trim(); // remove quotes if any
                        for (String var : errorVars) {
                            if (p.equalsIgnoreCase(var) || p.equalsIgnoreCase("\"" + var + "\"") || p.contains(var)) {
                                matched = true;
                                break;
                            }
                        }
                        if (matched) break;
                    }
                }

                // fallback: check opCode/spec/whole object with word-boundary
                if (!matched) {
                    String objStr = obj.toString();
                    for (String var : errorVars) {
                        // word-boundary pattern
                        Pattern wp = Pattern.compile("\\b" + Pattern.quote(var) + "\\b", Pattern.CASE_INSENSITIVE);
                        if (wp.matcher(objStr).find()) {
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
                // ignore non-json
            }
        }

        return result;
    }

    // Show a dialog listing matches; DO NOT show JSON code. On selection show confirm-delete dialog (Material3)
    private void showMatchesDialog(final List<BlockMatch> matches) {
        CharSequence[] items = new CharSequence[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            BlockMatch m = matches.get(i);
            // show header and id only — no JSON/code
            items[i] = m.header + "\nID: " + m.id;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Found " + matches.size() + " matching blocks (no code shown)");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final BlockMatch sel = matches.get(which);
                // Build confirm message WITHOUT code/JSON, include header and id and optional line number
                String msg = "Activity: " + sel.header + "\nBlock ID: " + sel.id; // no code

                MaterialAlertDialogBuilder detail = new MaterialAlertDialogBuilder(CompileLogActivity.this);
                detail.setTitle("Delete block?");
                detail.setMessage(msg);
                detail.setPositiveButton("Delete block", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog2, int which2) {
                        // perform deletion of this single block id
                        Set<Integer> toDelete = new HashSet<>();
                        toDelete.add(sel.id);
                        performDeleteBlocksAndSave(toDelete);
                    }
                });
                detail.setNegativeButton("Cancel", null);
                detail.show();
            }
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    // ==============================
    // New robust delete: parse file line-by-line into LineInfo, remove only chosen IDs and fix nextBlock chain
    // ==============================
    private void performDeleteBlocksAndSave(Set<Integer> toDelete) {
        try {
            if (toDelete == null || toDelete.isEmpty()) {
                Toast.makeText(this, "No blocks selected for deletion", Toast.LENGTH_SHORT).show();
                return;
            }

            String decrypted = decryptLogicFile(LOGIC_PATH);
            if (decrypted == null) {
                Toast.makeText(this, "Logic file read failed", Toast.LENGTH_LONG).show();
                return;
            }

            // keep lines exactly (preserve blanks)
            List<String> originalLines = Arrays.asList(decrypted.split("\\r?\\n", -1));

            // line info structure
            class LineInfo {
                String raw;
                boolean isHeader;
                boolean isJson;
                Integer id; // if json
                JSONObject obj; // parsed json if any
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

                // try parse json
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
                    // not a json line
                }

                // fallback: normal non-json line
                lineInfos.add(li);
            }

            if (idToLineInfo.isEmpty()) {
                Toast.makeText(this, "No json blocks to edit", Toast.LENGTH_LONG).show();
                return;
            }

            // filter only those ids that actually exist
            Set<Integer> actuallyFound = new HashSet<>();
            for (Integer id : toDelete) if (idToLineInfo.containsKey(id)) actuallyFound.add(id);

            if (actuallyFound.isEmpty()) {
                Toast.makeText(this, "No matching block ids found in logic (already removed?)", Toast.LENGTH_LONG).show();
                return;
            }

            // backup
            try {
                File src = new File(LOGIC_PATH);
                if (src.exists()) {
                    File bak = new File(LOGIC_PATH + ".bak");
                    copyFile(src, bak);
                }
            } catch (Exception e) { /* ignore backup errors */ }

            // helper to resolve next: skip over deleted ids
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

            // update nextBlock for remaining blocks that point to deleted IDs
            for (Map.Entry<Integer, LineInfo> entry : idToLineInfo.entrySet()) {
                int id = entry.getKey();
                LineInfo li = entry.getValue();
                if (actuallyFound.contains(id)) continue; // skip deleted ones
                if (li.obj == null) continue;
                int next = getIntFromJsonField(li.obj, "nextBlock", -1);
                if (actuallyFound.contains(next)) {
                    int newNext = resolveNext.applyAsInt(next);
                    try {
                        li.obj.put("nextBlock", newNext);
                    } catch (JSONException je) { /* ignore */ }
                }
            }

            // reconstruct content: keep headers and non-json as-is, skip deleted json lines and write updated json for others
            StringBuilder newContent = new StringBuilder();
            for (LineInfo li : lineInfos) {
                if (li.isHeader) {
                    newContent.append(li.raw).append("\n");
                    continue;
                }
                if (li.isJson) {
                    if (li.id != null && actuallyFound.contains(li.id)) {
                        // skip this deleted block (do not append)
                        continue;
                    } else {
                        if (li.obj != null) newContent.append(li.obj.toString()).append("\n");
                        else newContent.append(li.raw).append("\n");
                    }
                } else {
                    // normal line or blank
                    newContent.append(li.raw).append("\n");
                }
            }

            // save
            encryptAndSave(LOGIC_PATH, newContent.toString());

            // done
            Toast.makeText(this, "Deleted blocks: " + actuallyFound.toString(), Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            Toast.makeText(this, "Delete failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    // ==============================
    // resolveNext (unused now, kept for compatibility)
    // ==============================
    private int resolveNext(int startId, Map<Integer, Integer> idToNext, Set<Integer> toDeleteIds, Map<Integer, Integer> cache) {
        if (startId == -1) return -1;
        if (cache.containsKey(startId)) return cache.get(startId);

        int cur = startId;
        Set<Integer> visited = new HashSet<>();
        while (cur != -1 && toDeleteIds.contains(cur) && !visited.contains(cur)) {
            visited.add(cur);
            cur = idToNext.getOrDefault(cur, -1);
        }
        cache.put(startId, cur);
        return cur;
    }

    // ==============================
    // Decrypt & encrypt helpers (AES/CBC/PKCS5Padding, key/iv = "sketchwaresecure")
    // ==============================
    private String decryptLogicFile(String path) throws Exception {
        javax.crypto.Cipher instance = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyIv = "sketchwaresecure".getBytes(StandardCharsets.UTF_8);
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
        byte[] keyIv = "sketchwaresecure".getBytes(StandardCharsets.UTF_8);
        instance.init(javax.crypto.Cipher.ENCRYPT_MODE,
                new javax.crypto.spec.SecretKeySpec(keyIv, "AES"),
                new javax.crypto.spec.IvParameterSpec(keyIv));

        byte[] encrypted = instance.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        RandomAccessFile raf = new RandomAccessFile(path, "rw");
        raf.setLength(0);
        raf.write(encrypted);
        raf.close();
    }

    // safe int extractor
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

    // file copy for backup
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
}