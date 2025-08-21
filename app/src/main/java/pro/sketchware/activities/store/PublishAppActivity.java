package pro.sketchware.activities.store;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

import pro.sketchware.R;
import pro.sketchware.databinding.ActivityPublishAppBinding;

public class PublishAppActivity extends BaseAppCompatActivity {

    private ActivityPublishAppBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = ActivityPublishAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(getTranslatedString(R.string.store_publish_app));

        // Setup category spinner
        setupCategorySpinner();
        
        // If editing existing app
        String editingAppId = getIntent().getStringExtra("edit_app_id");
        if (editingAppId != null && !editingAppId.isEmpty()) {
            loadForEdit(editingAppId);
        }
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void setupCategorySpinner() {
        String[] categories = getResources().getStringArray(R.array.app_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_category_spinner, categories);
        binding.category.setAdapter(adapter);
        binding.category.setText(categories[0], false); // Set default category
    }

    private void loadForEdit(@NonNull String appId) {
        // Show loading while loading
        binding.content.setVisibility(View.GONE);
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("apps").child(appId);
        ref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot s) {
                binding.name.setText(String.valueOf(s.child("nome").getValue()));
                binding.iconUrl.setText(String.valueOf(s.child("icone").getValue()));
                binding.shortDesc.setText(String.valueOf(s.child("descricao_curta").getValue()));
                binding.longDesc.setText(String.valueOf(s.child("descricao_longa").getValue()));
                StringBuilder shots = new StringBuilder();
                for (com.google.firebase.database.DataSnapshot sc : s.child("screenshots").getChildren()) {
                    String u = String.valueOf(sc.getValue());
                    if (u != null && !u.isEmpty()) shots.append(u).append("\n");
                }
                binding.screenshots.setText(shots.toString().trim());
                binding.downloadUrl.setText(String.valueOf(s.child("url_download").getValue()));
                
                // Load category
                String category = String.valueOf(s.child("categoria").getValue());
                if (category != null && !category.equals("null") && !category.isEmpty()) {
                    binding.category.setText(category, false);
                }
                
                binding.btnSave.setText(getTranslatedString(R.string.store_save));
                binding.btnSave.setOnClickListener(v -> saveEdit(appId));
                
                // Hide loading and show content
                binding.content.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                // Hide loading and show content even on error
                binding.content.setVisibility(View.VISIBLE);
                Toast.makeText(PublishAppActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveEdit(@NonNull String appId) {
        String nome = String.valueOf(binding.name.getText()).trim();
        String icon = String.valueOf(binding.iconUrl.getText()).trim();
        String shortDesc = String.valueOf(binding.shortDesc.getText()).trim();
        String longDesc = String.valueOf(binding.longDesc.getText()).trim();
        String screenshots = String.valueOf(binding.screenshots.getText()).trim();
        String downloadUrl = String.valueOf(binding.downloadUrl.getText()).trim();
        String category = String.valueOf(binding.category.getText()).trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(shortDesc) || TextUtils.isEmpty(downloadUrl)) {
            Toast.makeText(this, getTranslatedString(R.string.store_invalid_required), Toast.LENGTH_LONG).show();
            return;
        }

        java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
        updates.put("nome", nome);
        updates.put("icone", icon);
        updates.put("descricao_curta", shortDesc);
        updates.put("descricao_longa", longDesc);
        updates.put("url_download", downloadUrl);
        updates.put("categoria", category);
        java.util.List<String> shots = new java.util.ArrayList<>();
        if (!screenshots.isEmpty()) {
            for (String line : screenshots.split("\n")) {
                if (!line.trim().isEmpty()) shots.add(line.trim());
            }
        }
        updates.put("screenshots", shots);

        FirebaseDatabase.getInstance().getReference("apps").child(appId).updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, getTranslatedString(R.string.store_saved), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void save() {
        String nome = String.valueOf(binding.name.getText()).trim();
        String icon = String.valueOf(binding.iconUrl.getText()).trim();
        String shortDesc = String.valueOf(binding.shortDesc.getText()).trim();
        String longDesc = String.valueOf(binding.longDesc.getText()).trim();
        String screenshots = String.valueOf(binding.screenshots.getText()).trim();
        String downloadUrl = String.valueOf(binding.downloadUrl.getText()).trim();
        String category = String.valueOf(binding.category.getText()).trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(shortDesc) || TextUtils.isEmpty(downloadUrl)) {
            Toast.makeText(this, getTranslatedString(R.string.store_invalid_required), Toast.LENGTH_LONG).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "Faça login para publicar", Toast.LENGTH_LONG).show();
            return;
        }

        String appId = UUID.randomUUID().toString();
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("app_id", appId);
        payload.put("nome", nome);
        payload.put("icone", icon);
        payload.put("descricao_curta", shortDesc);
        payload.put("descricao_longa", longDesc);
        payload.put("url_download", downloadUrl);
        payload.put("categoria", category);
        HashMap<String, Object> publisher = new HashMap<>();
        publisher.put("usuario_id", uid);
        payload.put("publisher", publisher);
        HashMap<String, Object> stats = new HashMap<>();
        stats.put("likes", 0);
        stats.put("downloads", 0);
        payload.put("estatisticas", stats);
        java.util.List<String> shots = new java.util.ArrayList<>();
        if (!screenshots.isEmpty()) {
            for (String line : screenshots.split("\n")) {
                if (!line.trim().isEmpty()) shots.add(line.trim());
            }
        }
        payload.put("screenshots", shots);
        payload.put("data_publicacao", java.time.Instant.now().toString());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("apps").child(appId);
        ref.setValue(payload)
                .addOnSuccessListener(unused -> {
                    // Update user statistics
                    updateUserStats(uid);
                    Toast.makeText(this, getTranslatedString(R.string.store_saved), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void updateUserStats(String uid) {
        // Get all apps published by this user
        DatabaseReference appsRef = FirebaseDatabase.getInstance().getReference("apps");
        appsRef.orderByChild("publisher/usuario_id").equalTo(uid)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        int totalApps = 0;
                        int totalDownloads = 0;
                        int totalLikes = 0;
                        
                        for (com.google.firebase.database.DataSnapshot appSnapshot : snapshot.getChildren()) {
                            totalApps++;
                            
                            // Get statistics for this app
                            com.google.firebase.database.DataSnapshot statsSnapshot = appSnapshot.child("estatisticas");
                            if (statsSnapshot.exists()) {
                                Object downloads = statsSnapshot.child("downloads").getValue();
                                Object likes = statsSnapshot.child("likes").getValue();
                                
                                try {
                                    if (downloads != null) {
                                        totalDownloads += Integer.parseInt(String.valueOf(downloads));
                                    }
                                    if (likes != null) {
                                        totalLikes += Integer.parseInt(String.valueOf(likes));
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                        
                        // Calculate average likes
                        double avgLikes = totalApps > 0 ? (double) totalLikes / totalApps : 0;
                        
                        // Update user statistics in Firebase
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);
                        java.util.HashMap<String, Object> userStats = new java.util.HashMap<>();
                        userStats.put("total_apps", totalApps);
                        userStats.put("total_downloads", totalDownloads);
                        userStats.put("total_likes", totalLikes);
                        userStats.put("avg_likes", avgLikes);
                        
                        userRef.child("estatisticas").setValue(userStats);
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                    }
                });
    }
}


