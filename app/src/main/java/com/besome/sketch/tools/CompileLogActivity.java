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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompileLogActivity extends BaseAppCompatActivity {

    private static final String PREFERENCE_WRAPPED_TEXT = "wrapped_text";
    private static final String PREFERENCE_USE_MONOSPACED_FONT = "use_monospaced_font";
    private static final String PREFERENCE_FONT_SIZE = "font_size";
    private CompileErrorSaver compileErrorSaver;
    private SharedPreferences logViewerPreferences;

    private CompileLogBinding binding;
    private String lastLogRaw;

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
        binding.aiExplainButton.setOnClickListener(v -> explainWithAi());

        applyLogViewerPreferences();

        setErrorText();
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

    private void explainWithAi() {
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

                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (reply == null || reply.trim().isEmpty()) {
                        SketchwareUtil.toastError("AI returned no content");
                        return;
                    }
                    String plain = stripMarkdown(reply);
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("AI explanation")
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

    private String stripMarkdown(String input) {
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
}
