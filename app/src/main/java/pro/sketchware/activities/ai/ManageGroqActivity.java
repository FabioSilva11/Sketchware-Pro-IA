package pro.sketchware.activities.ai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.materialswitch.MaterialSwitch;

import mod.hilal.saif.activities.tools.ConfigActivity;
import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.analytics.SketchwareAnalytics;

public class ManageGroqActivity extends BaseAppCompatActivity {

    private MaterialSwitch enableSwitch;
    private EditText apiKeyInput;
    private EditText modelInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_ai_groq);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Groq Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        enableSwitch = findViewById(R.id.lib_switch);
        apiKeyInput = findViewById(R.id.ed_api_key);
        modelInput = findViewById(R.id.ed_model);

        ((TextView) findViewById(R.id.tv_title)).setText("Enable AI (Groq)");
        ((TextView) findViewById(R.id.tv_api_key)).setText("API key");
        ((TextView) findViewById(R.id.tv_model)).setText("Model");

        apiKeyInput.setText(ConfigActivity.DataStore.getInstance().getString(pro.sketchware.ai.GroqClient.SETTINGS_KEY_API_KEY, ""));
        modelInput.setText(ConfigActivity.DataStore.getInstance().getString(pro.sketchware.ai.GroqClient.SETTINGS_KEY_MODEL, "llama-3.3-70b-versatile"));

        enableSwitch.setChecked(apiKeyInput.getText().length() > 0);

        Button save = findViewById(R.id.btn_save);
        save.setText(Helper.getResString(R.string.common_word_save));
        save.setOnClickListener(v -> {
            var ds = ConfigActivity.DataStore.getInstance();
            String apiKey = Helper.getText(apiKeyInput);
            String model = Helper.getText(modelInput);
            
            ds.putString(pro.sketchware.ai.GroqClient.SETTINGS_KEY_API_KEY, apiKey);
            ds.putString(pro.sketchware.ai.GroqClient.SETTINGS_KEY_MODEL, model);
            
            // Registrar configuração de IA
            if (!apiKey.isEmpty()) {
                SketchwareAnalytics.getInstance(this).logSettingsChanged("ai_groq_enabled", "true");
                SketchwareAnalytics.getInstance(this).logSettingsChanged("ai_groq_model", model);
            } else {
                SketchwareAnalytics.getInstance(this).logSettingsChanged("ai_groq_enabled", "false");
            }
            
            finish();
        });
    }
}


