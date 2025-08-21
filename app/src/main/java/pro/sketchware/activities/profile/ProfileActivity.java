package pro.sketchware.activities.profile;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.text.util.Linkify;
import android.text.method.LinkMovementMethod;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.annotation.NonNull;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pro.sketchware.databinding.ActivityProfileBinding;
import pro.sketchware.R;

public class ProfileActivity extends BaseAppCompatActivity {

    private ActivityProfileBinding binding;
    private java.util.List<java.util.HashMap<String, Object>> myApps = new java.util.ArrayList<>();
    private pro.sketchware.activities.main.adapters.LojaAdapter myAppsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(getTranslatedString(R.string.profile_title));

        loadProfile();

        // Setup user's apps list
        myAppsAdapter = new pro.sketchware.activities.main.adapters.LojaAdapter(myApps, app -> {
            // Allow editing own publication
            String appId = String.valueOf(app.get("app_id"));
            android.content.Intent i = new android.content.Intent(this, pro.sketchware.activities.store.PublishAppActivity.class);
            i.putExtra("edit_app_id", appId);
            startActivity(i);
        });
        
        // Configure RecyclerView for apps list
        try {
            RecyclerView rv = (RecyclerView) findViewById(pro.sketchware.R.id.recycler_my_apps);
            if (rv != null) {
                rv.setLayoutManager(new LinearLayoutManager(this));
                rv.setAdapter(myAppsAdapter);
                rv.setHasFixedSize(true);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_profile) {
            startActivity(new android.content.Intent(this, EditProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            try {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, getTranslatedString(R.string.profile_logout_done), Toast.LENGTH_SHORT).show();
                finish();
            } catch (Throwable t) {
                Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (id == R.id.action_publish_app) {
            startActivity(new android.content.Intent(this, pro.sketchware.activities.store.PublishAppActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProfile() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            finish();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(current.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Stop loading and show content
                binding.content.setVisibility(android.view.View.VISIBLE);

                String nome = snapshot.child("nome").getValue(String.class);
                String username = snapshot.child("username").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String bio = snapshot.child("bio").getValue(String.class);
                String foto = snapshot.child("foto_perfil").getValue(String.class);
                String site = snapshot.child("links").child("site").getValue(String.class);
                String github = snapshot.child("links").child("github").getValue(String.class);
                String linkedin = snapshot.child("links").child("linkedin").getValue(String.class);

                binding.name.setText(nome != null ? nome : "");
                binding.username.setText(username != null ? "@" + username : "");
                binding.email.setText(email != null ? email : "");
                binding.bio.setText(bio != null ? bio : "");
                binding.site.setText(site != null ? site : "");
                binding.github.setText(github != null ? github : "");
                binding.linkedin.setText(linkedin != null ? linkedin : "");

                // Carregar foto do usuário
                if (foto != null && !foto.isEmpty() && !foto.equals("null")) {
                    try {
                        Glide.with(ProfileActivity.this)
                                .load(foto)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .circleCrop()
                                .into(binding.fotoPerfil);
                    } catch (Exception e) {
                        binding.fotoPerfil.setImageResource(R.drawable.ic_profile);
                    }
                } else {
                    binding.fotoPerfil.setImageResource(R.drawable.ic_profile);
                }

                // Make links clickable
                Linkify.addLinks(binding.site, Linkify.WEB_URLS);
                Linkify.addLinks(binding.github, Linkify.WEB_URLS);
                Linkify.addLinks(binding.linkedin, Linkify.WEB_URLS);
                binding.site.setMovementMethod(LinkMovementMethod.getInstance());
                binding.github.setMovementMethod(LinkMovementMethod.getInstance());
                binding.linkedin.setMovementMethod(LinkMovementMethod.getInstance());

                // Load user statistics
                String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                if (uid != null) {
                    loadUserStats(uid);
                    loadUserApps(uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadUserStats(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);
        userRef.child("estatisticas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalApps = 0;
                int totalDownloads = 0;
                double avgLikes = 0;
                
                if (snapshot.exists()) {
                    Object apps = snapshot.child("total_apps").getValue();
                    Object downloads = snapshot.child("total_downloads").getValue();
                    Object avg = snapshot.child("avg_likes").getValue();
                    
                    try {
                        totalApps = apps != null ? Integer.parseInt(String.valueOf(apps)) : 0;
                        totalDownloads = downloads != null ? Integer.parseInt(String.valueOf(downloads)) : 0;
                        avgLikes = avg != null ? Double.parseDouble(String.valueOf(avg)) : 0;
                    } catch (Exception ignored) {}
                }
                
                // Update UI with statistics
                updateStatsDisplay(totalApps, totalDownloads, avgLikes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadUserApps(String uid) {
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("apps")
                .orderByChild("publisher/usuario_id").equalTo(uid)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot s) {
                        myApps.clear();
                        for (com.google.firebase.database.DataSnapshot d : s.getChildren()) {
                            java.util.HashMap<String, Object> map = new java.util.HashMap<>();
                            map.put("app_id", d.child("app_id").getValue());
                            map.put("nome", d.child("nome").getValue());
                            map.put("icone", d.child("icone").getValue());
                            map.put("descricao_curta", d.child("descricao_curta").getValue());
                            map.put("estatisticas", d.child("estatisticas").getValue());
                            myApps.add(map);
                        }
                        if (myAppsAdapter != null) myAppsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                    }
                });
    }

    private void updateStatsDisplay(int totalApps, int totalDownloads, double avgLikes) {
        // This method will be implemented when we add stats display to the profile
        // For now, we'll just log the values
        android.util.Log.d("ProfileActivity", "Stats - Apps: " + totalApps + ", Downloads: " + totalDownloads + ", Avg Likes: " + avgLikes);
    }
}


