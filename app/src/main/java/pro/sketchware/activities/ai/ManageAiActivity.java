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

public class ManageAiActivity extends BaseAppCompatActivity {


    private MaterialSwitch enableSwitch;
    
    // Groq components
    private MaterialSwitch switchGroq;
    private EditText edGroqApiKey;
    private Spinner spinnerGroqModel;
    private TextView tvGroqGetApiLink;
    
    // OpenAI components
    private MaterialSwitch switchOpenAI;
    private EditText edOpenAIApiKey;
    private Spinner spinnerOpenAIModel;
    private TextView tvOpenAIGetApiLink;
    
    // Claude components
    private MaterialSwitch switchClaude;
    private EditText edClaudeApiKey;
    private Spinner spinnerClaudeModel;
    private TextView tvClaudeGetApiLink;
    
    private Button testButton;

    // Available models for each provider
    private static final List<String> GROQ_MODELS = Arrays.asList(
            "llama-3.3-70b-versatile",      // Default model, most versatile
            "llama-3.1-8b-instant",         // Faster, smaller
            "llama-3.1-405b-reasoning",     // Better for reasoning
            "llama-3.1-8b-chat",            // Optimized for chat
            "llama-3.1-70b-versatile",      // Previous version of versatile
            "mixtral-8x7b-32768",           // Mixtral, good for code
            "gemma-7b-it"                   // Google model, fast
    );
    
    private static final List<String> OPENAI_MODELS = Arrays.asList(
            "gpt-5",                        // Main GPT-5 model
            "gpt-5-mini",                   // Lightweight version
            "gpt-5-nano",                   // Ultra-fast, low latency
            "gpt-5-chat",                   // Conversational optimized
            "gpt-5-auto",                   // Auto-selects best submodel
            "gpt-5-pro",                    // Pro version with ultra-precise reasoning
            "chatgpt-instant",              // Fast text generation
            "chatgpt-thinking"              // Complex reasoning mode
    );
    
    private static final List<String> CLAUDE_MODELS = Arrays.asList(
            "claude-3-5-sonnet-20241022",   // Claude Sonnet 3.5
            "claude-3-5-haiku-20241022",    // Claude Haiku 3.5
            "claude-3-opus-20240229"        // Claude Opus 3
    );


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_ai);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AI Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        enableSwitch = findViewById(R.id.lib_switch);
        
        // Groq components
        switchGroq = findViewById(R.id.switch_groq);
        edGroqApiKey = findViewById(R.id.ed_groq_api_key);
        spinnerGroqModel = findViewById(R.id.spinner_groq_model);
        tvGroqGetApiLink = findViewById(R.id.tv_groq_get_api_link);
        
        // OpenAI components
        switchOpenAI = findViewById(R.id.switch_openai);
        edOpenAIApiKey = findViewById(R.id.ed_openai_api_key);
        spinnerOpenAIModel = findViewById(R.id.spinner_openai_model);
        tvOpenAIGetApiLink = findViewById(R.id.tv_openai_get_api_link);
        
        // Claude components
        switchClaude = findViewById(R.id.switch_claude);
        edClaudeApiKey = findViewById(R.id.ed_claude_api_key);
        spinnerClaudeModel = findViewById(R.id.spinner_claude_model);
        tvClaudeGetApiLink = findViewById(R.id.tv_claude_get_api_link);
        
        testButton = findViewById(R.id.btn_test);

        // Setup spinners
        setupSpinners();

        // Load saved settings
        loadSavedSettings();

        // Setup listeners
        setupListeners();
    }

    private void setupSpinners() {
        // Setup Groq spinner
        ArrayAdapter<String> groqAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                GROQ_MODELS
        );
        groqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroqModel.setAdapter(groqAdapter);
        
        // Setup OpenAI spinner
        ArrayAdapter<String> openaiAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                OPENAI_MODELS
        );
        openaiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOpenAIModel.setAdapter(openaiAdapter);
        
        // Setup Claude spinner
        ArrayAdapter<String> claudeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CLAUDE_MODELS
        );
        claudeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClaudeModel.setAdapter(claudeAdapter);
    }


    private void loadSavedSettings() {
        var ds = ConfigActivity.DataStore.getInstance();
        
        // Load Groq settings
        String groqApiKey = ds.getString(GroqClient.SETTINGS_KEY_API_KEY, "");
        String groqModel = ds.getString(GroqClient.SETTINGS_KEY_MODEL, "llama-3.3-70b-versatile");
        boolean groqEnabled = ds.getBoolean("ai-groq-enabled", false);
        
        edGroqApiKey.setText(groqApiKey);
        switchGroq.setChecked(groqEnabled);
        int groqModelIndex = GROQ_MODELS.indexOf(groqModel);
        if (groqModelIndex >= 0) {
            spinnerGroqModel.setSelection(groqModelIndex);
        }
        
        // Load OpenAI settings
        String openAIApiKey = ds.getString(pro.sketchware.ai.OpenAIClient.SETTINGS_KEY_API_KEY, "");
        String openAIModel = ds.getString(pro.sketchware.ai.OpenAIClient.SETTINGS_KEY_MODEL, "gpt-5");
        boolean openAIEnabled = ds.getBoolean("ai-openai-enabled", false);
        
        edOpenAIApiKey.setText(openAIApiKey);
        switchOpenAI.setChecked(openAIEnabled);
        int openAIModelIndex = OPENAI_MODELS.indexOf(openAIModel);
        if (openAIModelIndex >= 0) {
            spinnerOpenAIModel.setSelection(openAIModelIndex);
        }
        
        // Load Claude settings
        String claudeApiKey = ds.getString(pro.sketchware.ai.ClaudeClient.SETTINGS_KEY_API_KEY, "");
        String claudeModel = ds.getString(pro.sketchware.ai.ClaudeClient.SETTINGS_KEY_MODEL, "claude-3-5-sonnet-20241022");
        boolean claudeEnabled = ds.getBoolean("ai-claude-enabled", false);
        
        edClaudeApiKey.setText(claudeApiKey);
        switchClaude.setChecked(claudeEnabled);
        int claudeModelIndex = CLAUDE_MODELS.indexOf(claudeModel);
        if (claudeModelIndex >= 0) {
            spinnerClaudeModel.setSelection(claudeModelIndex);
        }

        // Configure main switch based on any provider being enabled
        boolean hasAnyProvider = groqEnabled || openAIEnabled || claudeEnabled;
        enableSwitch.setChecked(hasAnyProvider);
        updateUIState(hasAnyProvider);
    }

    private void setupListeners() {
        // Groq API key link
        tvGroqGetApiLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://console.groq.com/keys"));
            startActivity(intent);
        });
        
        // OpenAI API key link
        tvOpenAIGetApiLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.openai.com/api-keys"));
            startActivity(intent);
        });
        
        // Claude API key link
        tvClaudeGetApiLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://console.anthropic.com/"));
            startActivity(intent);
        });

        // Main enable switch
        enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUIState(isChecked);
            if (!isChecked) {
                // Disable all providers when main switch is off
                switchGroq.setChecked(false);
                switchOpenAI.setChecked(false);
                switchClaude.setChecked(false);
                saveSettings();
            }
        });
        
        // Provider switches
        switchGroq.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateProviderUIState("groq", isChecked);
            updateMainSwitchState();
        });
        
        switchOpenAI.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateProviderUIState("openai", isChecked);
            updateMainSwitchState();
        });
        
        switchClaude.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateProviderUIState("claude", isChecked);
            updateMainSwitchState();
        });

        // Test button
        testButton.setOnClickListener(v -> testAllConnections());

        // Save button
        Button saveButton = findViewById(R.id.btn_save);
        saveButton.setText(Helper.getResString(R.string.common_word_save));
        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void updateUIState(boolean enabled) {
        // Enable/disable all provider components
        switchGroq.setEnabled(enabled);
        edGroqApiKey.setEnabled(enabled && switchGroq.isChecked());
        spinnerGroqModel.setEnabled(enabled && switchGroq.isChecked());
        tvGroqGetApiLink.setEnabled(enabled);
        
        switchOpenAI.setEnabled(enabled);
        edOpenAIApiKey.setEnabled(enabled && switchOpenAI.isChecked());
        spinnerOpenAIModel.setEnabled(enabled && switchOpenAI.isChecked());
        tvOpenAIGetApiLink.setEnabled(enabled);
        
        switchClaude.setEnabled(enabled);
        edClaudeApiKey.setEnabled(enabled && switchClaude.isChecked());
        spinnerClaudeModel.setEnabled(enabled && switchClaude.isChecked());
        tvClaudeGetApiLink.setEnabled(enabled);
        
        testButton.setEnabled(enabled);
    }
    
    private void updateProviderUIState(String provider, boolean enabled) {
        switch (provider) {
            case "groq":
                edGroqApiKey.setEnabled(enabled);
                spinnerGroqModel.setEnabled(enabled);
                break;
            case "openai":
                edOpenAIApiKey.setEnabled(enabled);
                spinnerOpenAIModel.setEnabled(enabled);
                break;
            case "claude":
                edClaudeApiKey.setEnabled(enabled);
                spinnerClaudeModel.setEnabled(enabled);
                break;
        }
    }
    
    private void updateMainSwitchState() {
        boolean hasAnyProvider = switchGroq.isChecked() || switchOpenAI.isChecked() || switchClaude.isChecked();
        enableSwitch.setChecked(hasAnyProvider);
    }

    private void testAllConnections() {
        testButton.setEnabled(false);
        testButton.setText("Testing...");

        // Test in background
        new Thread(() -> {
            StringBuilder results = new StringBuilder();
            int successCount = 0;
            int totalTests = 0;
            
            try {
                // Test Groq
                if (switchGroq.isChecked()) {
                    totalTests++;
                    String groqApiKey = edGroqApiKey.getText().toString().trim();
                    if (!groqApiKey.isEmpty()) {
                        if (testGroqConnection(groqApiKey)) {
                            results.append("✅ Groq: OK\n");
                            successCount++;
                        } else {
                            results.append("❌ Groq: Failed\n");
                        }
                    } else {
                        results.append("⚠️ Groq: No API key\n");
                    }
                }
                
                // Test OpenAI
                if (switchOpenAI.isChecked()) {
                    totalTests++;
                    String openAIApiKey = edOpenAIApiKey.getText().toString().trim();
                    if (!openAIApiKey.isEmpty()) {
                        if (testOpenAIConnection(openAIApiKey)) {
                            results.append("✅ OpenAI: OK\n");
                            successCount++;
                        } else {
                            results.append("❌ OpenAI: Failed\n");
                        }
                    } else {
                        results.append("⚠️ OpenAI: No API key\n");
                    }
                }
                
                // Test Claude
                if (switchClaude.isChecked()) {
                    totalTests++;
                    String claudeApiKey = edClaudeApiKey.getText().toString().trim();
                    if (!claudeApiKey.isEmpty()) {
                        if (testClaudeConnection(claudeApiKey)) {
                            results.append("✅ Claude: OK\n");
                            successCount++;
                        } else {
                            results.append("❌ Claude: Failed\n");
                        }
                    } else {
                        results.append("⚠️ Claude: No API key\n");
                    }
                }
                
                final String finalResults = results.toString();
                final int finalSuccessCount = successCount;
                final int finalTotalTests = totalTests;
                
                runOnUiThread(() -> {
                    testButton.setEnabled(true);
                    testButton.setText("Test Connections");
                    
                    if (finalTotalTests == 0) {
                        Toast.makeText(this, "No providers enabled for testing", Toast.LENGTH_SHORT).show();
                    } else if (finalSuccessCount == finalTotalTests) {
                        Toast.makeText(this, "✅ All connections successful!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, finalResults, Toast.LENGTH_LONG).show();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    testButton.setEnabled(true);
                    testButton.setText("Test Connections");
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void saveSettings() {
        var ds = ConfigActivity.DataStore.getInstance();
        
        // Save Groq settings
        String groqApiKey = edGroqApiKey.getText().toString().trim();
        String groqModel = (String) spinnerGroqModel.getSelectedItem();
        boolean groqEnabled = switchGroq.isChecked();
        
        ds.putString(GroqClient.SETTINGS_KEY_API_KEY, groqApiKey);
        ds.putString(GroqClient.SETTINGS_KEY_MODEL, groqModel);
        ds.putBoolean("ai-groq-enabled", groqEnabled);
        
        // Save OpenAI settings
        String openAIApiKey = edOpenAIApiKey.getText().toString().trim();
        String openAIModel = (String) spinnerOpenAIModel.getSelectedItem();
        boolean openAIEnabled = switchOpenAI.isChecked();
        
        ds.putString(pro.sketchware.ai.OpenAIClient.SETTINGS_KEY_API_KEY, openAIApiKey);
        ds.putString(pro.sketchware.ai.OpenAIClient.SETTINGS_KEY_MODEL, openAIModel);
        ds.putBoolean("ai-openai-enabled", openAIEnabled);
        
        // Save Claude settings
        String claudeApiKey = edClaudeApiKey.getText().toString().trim();
        String claudeModel = (String) spinnerClaudeModel.getSelectedItem();
        boolean claudeEnabled = switchClaude.isChecked();
        
        ds.putString(pro.sketchware.ai.ClaudeClient.SETTINGS_KEY_API_KEY, claudeApiKey);
        ds.putString(pro.sketchware.ai.ClaudeClient.SETTINGS_KEY_MODEL, claudeModel);
        ds.putBoolean("ai-claude-enabled", claudeEnabled);
        
        // Update main switch state
        boolean hasAnyProvider = groqEnabled || openAIEnabled || claudeEnabled;
        enableSwitch.setChecked(hasAnyProvider);
        updateUIState(hasAnyProvider);
        
        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    /**
     * Determine provider from model name
     */
    private String getProviderFromModel(String model) {
        if (model.startsWith("gpt-") || model.startsWith("chatgpt-")) {
            return "openai";
        } else if (model.startsWith("claude-")) {
            return "claude";
        } else {
            return "groq";
        }
    }
    
    private boolean testGroqConnection(String apiKey) throws Exception {
        var ds = ConfigActivity.DataStore.getInstance();
        String originalKey = ds.getString(GroqClient.SETTINGS_KEY_API_KEY, "");
        ds.putString(GroqClient.SETTINGS_KEY_API_KEY, apiKey);
        
        try {
            GroqClient testClient = new GroqClient();
            List<GroqClient.Message> testMessages = GroqClient.Message.of(
                "You are a helpful assistant. Respond with 'OK' if you receive this message.",
                "Test message"
            );
            String response = testClient.chat(testMessages);
            return response != null && response.contains("OK");
        } finally {
            ds.putString(GroqClient.SETTINGS_KEY_API_KEY, originalKey);
        }
    }
    
    private boolean testOpenAIConnection(String apiKey) throws Exception {
        var ds = ConfigActivity.DataStore.getInstance();
        String originalKey = ds.getString(pro.sketchware.ai.OpenAIClient.SETTINGS_KEY_API_KEY, "");
        ds.putString(pro.sketchware.ai.OpenAIClient.SETTINGS_KEY_API_KEY, apiKey);
        
        try {
            pro.sketchware.ai.OpenAIClient testClient = new pro.sketchware.ai.OpenAIClient();
            return testClient.testConnection();
        } finally {
            ds.putString(pro.sketchware.ai.OpenAIClient.SETTINGS_KEY_API_KEY, originalKey);
        }
    }
    
    private boolean testClaudeConnection(String apiKey) throws Exception {
        var ds = ConfigActivity.DataStore.getInstance();
        String originalKey = ds.getString(pro.sketchware.ai.ClaudeClient.SETTINGS_KEY_API_KEY, "");
        ds.putString(pro.sketchware.ai.ClaudeClient.SETTINGS_KEY_API_KEY, apiKey);
        
        try {
            pro.sketchware.ai.ClaudeClient testClient = new pro.sketchware.ai.ClaudeClient();
            return testClient.testConnection();
        } finally {
            ds.putString(pro.sketchware.ai.ClaudeClient.SETTINGS_KEY_API_KEY, originalKey);
        }
    }
    
    private String getApiKeyUrl(String provider) {
        switch (provider) {
            case "openai":
                return "https://platform.openai.com/api-keys";
            case "claude":
                return "https://console.anthropic.com/";
            case "groq":
            default:
                return "https://console.groq.com/keys";
        }
    }
}


