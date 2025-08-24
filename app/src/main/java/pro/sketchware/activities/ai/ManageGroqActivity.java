package pro.sketchware.activities.ai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Arrays;
import java.util.List;

import mod.hilal.saif.activities.tools.ConfigActivity;
import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.ai.GroqClient;

public class ManageGroqActivity extends BaseAppCompatActivity {

    public static final String SETTINGS_KEY_LAYOUT_LANGUAGE = "ai-groq-layout-language";

    private MaterialSwitch enableSwitch;
    private EditText apiKeyInput;
    private Spinner modelSpinner;
    private Spinner languageSpinner;
    private Button testButton;
    private TextView getApiLink;

    // Available Groq models
    private static final List<String> AVAILABLE_MODELS = Arrays.asList(
            "llama-3.3-70b-versatile",      // Default model, most versatile
            "llama-3.1-8b-instant",         // Faster, smaller
            "llama-3.1-405b-reasoning",     // Better for reasoning
            "llama-3.1-8b-chat",            // Optimized for chat
            "llama-3.1-70b-versatile",      // Previous version of versatile
            "mixtral-8x7b-32768",           // Mixtral, good for code
            "gemma-7b-it"                   // Google model, fast
    );

    // Available languages for layout generation
    private static final List<String> AVAILABLE_LANGUAGES = Arrays.asList(
            "English (Default)",
            "Portuguese (Brazil)",
            "Spanish",
            "French",
            "German",
            "Italian",
            "Japanese",
            "Korean",
            "Chinese (Simplified)",
            "Russian",
            "Arabic",
            "Hindi"
    );

    // Language codes for AI prompts
    private static final List<String> LANGUAGE_CODES = Arrays.asList(
            "en", "pt-BR", "es", "fr", "de", "it", "ja", "ko", "zh-CN", "ru", "ar", "hi"
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_ai_groq);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AI Settings (Groq)");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        enableSwitch = findViewById(R.id.lib_switch);
        apiKeyInput = findViewById(R.id.ed_api_key);
        modelSpinner = findViewById(R.id.spinner_model);
        languageSpinner = findViewById(R.id.spinner_language);
        testButton = findViewById(R.id.btn_test);
        getApiLink = findViewById(R.id.tv_get_api_link);

        // Setup spinners
        setupModelSpinner();
        setupLanguageSpinner();

        // Load saved settings
        loadSavedSettings();

        // Setup listeners
        setupListeners();
    }

    private void setupModelSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                AVAILABLE_MODELS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                AVAILABLE_LANGUAGES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
    }

    private void loadSavedSettings() {
        var ds = ConfigActivity.DataStore.getInstance();
        String savedApiKey = ds.getString(GroqClient.SETTINGS_KEY_API_KEY, "");
        String savedModel = ds.getString(GroqClient.SETTINGS_KEY_MODEL, "llama-3.3-70b-versatile");
        String savedLanguage = ds.getString(SETTINGS_KEY_LAYOUT_LANGUAGE, "en");

        apiKeyInput.setText(savedApiKey);
        
        // Select saved model in spinner
        int modelIndex = AVAILABLE_MODELS.indexOf(savedModel);
        if (modelIndex >= 0) {
            modelSpinner.setSelection(modelIndex);
        }

        // Select saved language in spinner
        int languageIndex = LANGUAGE_CODES.indexOf(savedLanguage);
        if (languageIndex >= 0) {
            languageSpinner.setSelection(languageIndex);
        }

        // Configure switch based on API key presence
        boolean hasApiKey = !savedApiKey.trim().isEmpty();
        enableSwitch.setChecked(hasApiKey);
        updateUIState(hasApiKey);
    }

    private void setupListeners() {
        // Get API key link
        getApiLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"));
            startActivity(intent);
        });

        // Enable switch
        enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUIState(isChecked);
            if (!isChecked) {
                // Clear API key when disabled
                apiKeyInput.setText("");
                saveSettings();
            }
        });

        // Test button
        testButton.setOnClickListener(v -> testConnection());

        // Save button
        Button saveButton = findViewById(R.id.btn_save);
        saveButton.setText(Helper.getResString(R.string.common_word_save));
        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void updateUIState(boolean enabled) {
        apiKeyInput.setEnabled(enabled);
        modelSpinner.setEnabled(enabled);
        languageSpinner.setEnabled(enabled);
        testButton.setEnabled(enabled);
        getApiLink.setEnabled(enabled);
    }

    private void testConnection() {
        String apiKey = apiKeyInput.getText().toString().trim();
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please enter an API key first", Toast.LENGTH_SHORT).show();
            return;
        }

        testButton.setEnabled(false);
        testButton.setText("Testing...");

        // Test in background
        new Thread(() -> {
            try {
                // Create temporary client for testing
                GroqClient testClient = new GroqClient();
                
                // Temporarily save API key for testing
            var ds = ConfigActivity.DataStore.getInstance();
                String originalKey = ds.getString(GroqClient.SETTINGS_KEY_API_KEY, "");
                ds.putString(GroqClient.SETTINGS_KEY_API_KEY, apiKey);
                
                // Simple test
                List<GroqClient.Message> testMessages = GroqClient.Message.of(
                    "You are a helpful assistant. Respond with 'OK' if you receive this message.",
                    "Test message"
                );
                
                String response = testClient.chat(testMessages);
                
                // Restore original API key
                ds.putString(GroqClient.SETTINGS_KEY_API_KEY, originalKey);
                
                runOnUiThread(() -> {
                    testButton.setEnabled(true);
                    testButton.setText("Test Connection");
                    
                    if (response != null && response.contains("OK")) {
                        Toast.makeText(this, "✅ Connection successful!", Toast.LENGTH_LONG).show();
                        enableSwitch.setChecked(true);
                        updateUIState(true);
                    } else {
                        Toast.makeText(this, "❌ Connection failed. Check your API key.", Toast.LENGTH_LONG).show();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    testButton.setEnabled(true);
                    testButton.setText("Test Connection");
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void saveSettings() {
        var ds = ConfigActivity.DataStore.getInstance();
        String apiKey = apiKeyInput.getText().toString().trim();
        String selectedModel = (String) modelSpinner.getSelectedItem();
        String selectedLanguageCode = LANGUAGE_CODES.get(languageSpinner.getSelectedItemPosition());
        
        ds.putString(GroqClient.SETTINGS_KEY_API_KEY, apiKey);
        ds.putString(GroqClient.SETTINGS_KEY_MODEL, selectedModel);
        ds.putString(SETTINGS_KEY_LAYOUT_LANGUAGE, selectedLanguageCode);
        
        // Update switch state
        boolean hasApiKey = !apiKey.isEmpty();
        enableSwitch.setChecked(hasApiKey);
        updateUIState(hasApiKey);
        
        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}


