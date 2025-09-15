package com.besome.sketch.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.besome.sketch.design.DesignActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import mod.hey.studios.util.CompileLogHelper;
import mod.hey.studios.util.Helper;
import mod.hey.studios.project.ProjectTracker;
import mod.jbk.diagnostic.CompileErrorSaver;
import mod.jbk.util.AddMarginOnApplyWindowInsetsListener;
import pro.sketchware.databinding.CompileLogBinding;
import pro.sketchware.utility.SketchwareUtil;
import pro.sketchware.managers.BlockDeletionManager;
import pro.sketchware.managers.TranslationManager;
import pro.sketchware.managers.AIExplanationManager;
import pro.sketchware.managers.AIErrorFixerManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompileLogActivity extends BaseAppCompatActivity {

    private static final String PREFERENCE_WRAPPED_TEXT = "wrapped_text";
    private static final String PREFERENCE_USE_MONOSPACED_FONT = "use_monospaced_font";
    private static final String PREFERENCE_FONT_SIZE = "font_size";
    private static final String PREFERENCE_SAVED_PROJECT_ID = "saved_project_id";
    private String LOGIC_PATH;

    private CompileErrorSaver compileErrorSaver;
    private SharedPreferences logViewerPreferences;

    private CompileLogBinding binding;
    private String lastLogRaw;
    private String savedProjectId; // ID do projeto que foi salvo quando esta tela foi aberta

    // Managers para funcionalidades específicas
    private BlockDeletionManager blockDeletionManager;
    private TranslationManager translationManager;
    private AIExplanationManager aiExplanationManager;
    private AIErrorFixerManager aiErrorFixerManager;
    private AdView bannerAdView;


    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);
        binding = CompileLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            MobileAds.initialize(this);
            bannerAdView = binding.bannerAdCompile;
            if (bannerAdView != null) {
                AdRequest adRequest = new AdRequest.Builder().build();
                bannerAdView.loadAd(adRequest);
            }
        } catch (Throwable ignored) {}

        ViewCompat.setOnApplyWindowInsetsListener(binding.optionsLayout,
                new AddMarginOnApplyWindowInsetsListener(WindowInsetsCompat.Type.navigationBars(), WindowInsetsCompat.CONSUMED));

        logViewerPreferences = getPreferences(Context.MODE_PRIVATE);

        // Carregar o ID do projeto salvo das preferências
        savedProjectId = logViewerPreferences.getString(PREFERENCE_SAVED_PROJECT_ID, null);

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

        // Salvar o ID do projeto atual
        saveCurrentProject(sc_id);

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
        
        // Inicializar managers
        blockDeletionManager = new BlockDeletionManager(this, LOGIC_PATH);
        translationManager = new TranslationManager(this);
        aiExplanationManager = new AIExplanationManager(this);
        aiErrorFixerManager = new AIErrorFixerManager(this, LOGIC_PATH);

        binding.errorFixButton.setOnClickListener(v -> {
            errorChecker(binding.tvCompileLog.getText().toString());
        });
        
        // AI Auto Fix button
        binding.aiFixButton.setOnClickListener(v -> {
            String scId = getIntent().getStringExtra("sc_id");
            aiErrorFixerManager.showLanguagePickerAndFix(lastLogRaw, scId);
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
        if (lastLogRaw == null || lastLogRaw.trim().isEmpty()) {
            SketchwareUtil.toastError("No log to explain.");
            return;
        }

                String scId = getIntent().getStringExtra("sc_id");
        aiExplanationManager.showLanguagePickerAndExplain(lastLogRaw, scId);
    }





    private void errorChecker(String errorLog) {
        // Use BlockDeletionManager to analyze error and extract smart search keywords
        List<String> smartKeywords = blockDeletionManager.extractSmartKeywords(errorLog);
        BlockDeletionManager.ErrorType errorType = blockDeletionManager.analyzeErrorType(errorLog);

        if (smartKeywords.isEmpty() && errorType == BlockDeletionManager.ErrorType.UNKNOWN) {
            Toast.makeText(this, "No identifiable errors found in log", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String decrypted = blockDeletionManager.decryptLogicFile(LOGIC_PATH);
            if (decrypted == null || decrypted.trim().isEmpty()) {
                Toast.makeText(this, "Logic file empty or cannot be read", Toast.LENGTH_LONG).show();
                return;
            }

            // Convert smart keywords to a set for compatibility
            Set<String> keywordSet = new HashSet<>(smartKeywords);
            List<BlockDeletionManager.BlockMatch> matches = blockDeletionManager.findMatchingBlocks(decrypted, keywordSet, errorType);

            if (matches.isEmpty()) {
                // Show smart guidance with AI-extracted keywords
                showSmartNoMatchesDialog(smartKeywords, errorType, errorLog);
                return;
            }

            // show list dialog of matches (NO code/JSON shown)
            showMatchesDialog(matches);

        } catch (Exception e) {
            Toast.makeText(this, "Error processing logic: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }




    // Show smart guidance with AI-extracted keywords
    private void showSmartNoMatchesDialog(List<String> smartKeywords, BlockDeletionManager.ErrorType errorType, String errorLog) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        
        String title = "Smart Search Suggestions";
        String message;
        
        // Create smart search suggestions based on extracted keywords
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("🔍 **Smart Search Keywords:**\n\n");
        
        if (!smartKeywords.isEmpty()) {
            suggestions.append("Try searching for these specific terms in your blocks:\n\n");
            for (int i = 0; i < Math.min(smartKeywords.size(), 5); i++) {
                suggestions.append("• **").append(smartKeywords.get(i)).append("**\n");
            }
            if (smartKeywords.size() > 5) {
                suggestions.append("• And ").append(smartKeywords.size() - 5).append(" more...\n");
            }
        }
        
        switch (errorType) {
            case SYNTAX_ERROR:
                title = "🔧 Syntax Error - Smart Fix";
                message = suggestions.toString() + "\n" +
                         "**Error Type:** Syntax Error (String literal not closed)\n\n" +
                         "**What to do:**\n" +
                         "1. Look for blocks containing the keywords above\n" +
                         "2. Check for unclosed quotes in string operations\n" +
                         "3. Look for text/string related blocks\n\n" +
                         "**Common blocks to check:**\n" +
                         "• Text/String manipulation blocks\n" +
                         "• Variable assignment blocks\n" +
                         "• Method call blocks with string parameters";
                break;
                
            case XML_ERROR:
                title = "📱 XML Error - Smart Fix";
                message = suggestions.toString() + "\n" +
                         "**Error Type:** XML Layout Error\n\n" +
                         "**What to do:**\n" +
                         "1. Search for blocks with the view IDs above\n" +
                         "2. Check layout-related blocks\n" +
                         "3. Look for view binding or findViewById blocks\n\n" +
                         "**Common blocks to check:**\n" +
                         "• findViewById blocks\n" +
                         "• View binding blocks\n" +
                         "• Layout inflation blocks";
                break;
                
            case VARIABLE_ERROR:
                title = "🔍 Variable Error - Smart Fix";
                message = suggestions.toString() + "\n" +
                         "**Error Type:** Variable Not Found\n\n" +
                         "**What to do:**\n" +
                         "1. Search for blocks using these variable names\n" +
                         "2. Check if variables are properly declared\n" +
                         "3. Look for variable assignment or usage blocks\n\n" +
                         "**Common blocks to check:**\n" +
                         "• Variable declaration blocks\n" +
                         "• Variable assignment blocks\n" +
                         "• Method parameter blocks";
                break;
                
            case IMPORT_ERROR:
                title = "📦 Import Error - Smart Fix";
                message = suggestions.toString() + "\n" +
                         "**Error Type:** Import/Package Error\n\n" +
                         "**What to do:**\n" +
                         "1. Search for blocks using these package names\n" +
                         "2. Check library import blocks\n" +
                         "3. Look for class instantiation blocks\n\n" +
                         "**Common blocks to check:**\n" +
                         "• Library import blocks\n" +
                         "• Class instantiation blocks\n" +
                         "• Method call blocks from external libraries";
                break;
                
            default:
                title = "🔍 Smart Search Suggestions";
                message = suggestions.toString() + "\n" +
                         "**What to do:**\n" +
                         "1. Use the keywords above to search your blocks\n" +
                         "2. Look for blocks that might contain these terms\n" +
                         "3. Check related functionality blocks\n\n" +
                         "**Tip:** The keywords are extracted from your error log and are most likely to help you find the problematic block.";
                break;
        }
        
        builder.setTitle(title);
        builder.setMessage(message);
        
        // Add action buttons
        builder.setPositiveButton("Search Blocks", (dialog, which) -> {
            showBlockSearchDialog(smartKeywords);
        });
        
        if (errorType == BlockDeletionManager.ErrorType.SYNTAX_ERROR || errorType == BlockDeletionManager.ErrorType.XML_ERROR) {
            builder.setNeutralButton("Show Error Details", (dialog, which) -> {
                showErrorDetailsDialog(errorLog);
            });
        }
        
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    // Show a more helpful dialog when no matches are found
    private void showNoMatchesDialog(Set<String> errorVars, BlockDeletionManager.ErrorType errorType, String errorLog) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        
        String title = "No matching blocks found";
        String message;
        
        switch (errorType) {
            case SYNTAX_ERROR:
                title = "Syntax Error Detected";
                message = "This appears to be a syntax error (like unclosed quotes).\n\n" +
                         "These errors usually need to be fixed in the source code, not by deleting blocks.\n\n" +
                         "Check your Java/XML files for syntax issues.";
                break;
                
            case XML_ERROR:
                title = "XML Error Detected";
                message = "This appears to be an XML parsing error.\n\n" +
                         "Check your layout files for XML syntax issues.";
                break;
                
            case IMPORT_ERROR:
                title = "Import Error Detected";
                message = "This appears to be an import/package error.\n\n" +
                         "Check if all required libraries are properly imported.";
                break;
                
            case VARIABLE_ERROR:
                title = "Variable Error Detected";
                message = "Scanned names: " + errorVars.toString() + "\n\n" +
                         "No matching blocks found in logic.\n\n" +
                         "These variables might be defined in source files or need to be created.";
                break;
                
            default:
                message = "Scanned names: " + errorVars.toString() + "\n\n" +
                         "No matching blocks found in logic.";
                break;
        }
        
        builder.setTitle(title);
        builder.setMessage(message);
        
        // Add action buttons based on error type
        if (errorType == BlockDeletionManager.ErrorType.SYNTAX_ERROR || errorType == BlockDeletionManager.ErrorType.XML_ERROR) {
            builder.setPositiveButton("Show Error Details", (dialog, which) -> {
                showErrorDetailsDialog(errorLog);
            });
        } else {
            builder.setPositiveButton("OK", null);
        }
        
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    // Show detailed error information
    private void showErrorDetailsDialog(String errorLog) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Error Details");
        builder.setMessage(errorLog);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // Show dialog for manual block search with smart keywords
    private void showBlockSearchDialog(List<String> smartKeywords) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("🔍 Search Blocks with Smart Keywords");
        
        // Create a list of keywords for selection
        String[] keywordArray = smartKeywords.toArray(new String[0]);
        boolean[] selectedKeywords = new boolean[keywordArray.length];
        
        builder.setMultiChoiceItems(keywordArray, selectedKeywords, (dialog, which, isChecked) -> {
            selectedKeywords[which] = isChecked;
        });
        
        builder.setPositiveButton("Search Selected", (dialog, which) -> {
            // Get selected keywords
            List<String> selected = new ArrayList<>();
            for (int i = 0; i < selectedKeywords.length; i++) {
                if (selectedKeywords[i]) {
                    selected.add(keywordArray[i]);
                }
            }
            
            if (selected.isEmpty()) {
                Toast.makeText(this, "Please select at least one keyword to search", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Perform search with selected keywords
            performSmartBlockSearch(selected);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Perform smart block search with selected keywords
    private void performSmartBlockSearch(List<String> keywords) {
        try {
            String decrypted = blockDeletionManager.decryptLogicFile(LOGIC_PATH);
            if (decrypted == null) {
                Toast.makeText(this, "Logic file empty or cannot be read", Toast.LENGTH_LONG).show();
                return;
            }

            Set<String> keywordSet = new HashSet<>(keywords);
            List<BlockDeletionManager.BlockMatch> matches = blockDeletionManager.findMatchingBlocks(decrypted, keywordSet, BlockDeletionManager.ErrorType.UNKNOWN);

            if (matches.isEmpty()) {
                Toast.makeText(this, "No blocks found with selected keywords", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show found matches
            showMatchesDialog(matches);

        } catch (Exception e) {
            Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Show a dialog listing matches; DO NOT show JSON code. On selection show confirm-delete dialog (Material3)
    private void showMatchesDialog(final List<BlockDeletionManager.BlockMatch> matches) {
        CharSequence[] items = new CharSequence[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            BlockDeletionManager.BlockMatch m = matches.get(i);
            // show header and id only — no JSON/code
            items[i] = m.header + "\nID: " + m.id;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Found " + matches.size() + " matching blocks (no code shown)");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final BlockDeletionManager.BlockMatch sel = matches.get(which);
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
                        blockDeletionManager.performDeleteBlocksAndSave(toDelete);
                    }
                });
                detail.setNegativeButton("Cancel", null);
                detail.show();
            }
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    /**
     * Salva o ID do projeto atual
     */
    private void saveCurrentProject(String sc_id) {
        // Salvar o ID do projeto atual nas preferências
        savedProjectId = sc_id;
        logViewerPreferences.edit()
                .putString(PREFERENCE_SAVED_PROJECT_ID, savedProjectId)
                .apply();
    }

    /**
     * Reabre o projeto que foi salvo quando esta tela foi aberta
     */
    private void reopenSavedProject() {
        if (savedProjectId != null && !savedProjectId.isEmpty()) {
            Intent intent = new Intent(this, DesignActivity.class);
            intent.putExtra("sc_id", savedProjectId);
            intent.putExtra("from_compile_log", true); // Flag para indicar que veio da tela de log
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        // Reabrir o projeto salvo quando o usuário pressionar voltar
        reopenSavedProject();
    }

}
