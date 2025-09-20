package pro.sketchware.activities.chat;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import pro.sketchware.R;

public class ChatSettingsActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    private RecyclerView settingsRecyclerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        settingsRecyclerView = findViewById(R.id.settings_recycler_view);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chat Settings");
        }
    }
    
    private void setupRecyclerView() {
        // Por enquanto, apenas mostrar uma mensagem
        // Em uma implementação completa, você criaria um adapter para configurações
        Toast.makeText(this, "Chat settings will be implemented here", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
