package pro.sketchware.activities.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.main.adapters.UserProfilePagerAdapter;
import pro.sketchware.activities.main.fragments.loja.AppItem;

public class UserProfileActivity extends AppCompatActivity {

    // Views
    private ImageView ivProfilePhoto;
    private MaterialTextView tvUserName, tvUserEmail;
    private MaterialTextView tvAppsCount, tvDownloadsCount, tvRatingAvg;
    private MaterialButton btnEditProfile, btnPublishApp;
    private FloatingActionButton fabEditPhoto;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private CircularProgressIndicator progressBar;

    // Data
    private List<AppItem> userApps = new ArrayList<>();
    private List<AppItem> userDrafts = new ArrayList<>();

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        initViews();
        setupToolbar();
        setupViewPager();
        setupListeners();
        loadUserData();
        loadUserApps();
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvAppsCount = findViewById(R.id.tv_apps_count);
        tvDownloadsCount = findViewById(R.id.tv_downloads_count);
        tvRatingAvg = findViewById(R.id.tv_rating_avg);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnPublishApp = findViewById(R.id.btn_publish_app);
        fabEditPhoto = findViewById(R.id.fab_edit_photo);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Meu Perfil");
        }
    }

    private void setupViewPager() {
        UserProfilePagerAdapter pagerAdapter = new UserProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Meus Apps");
                    break;
                case 1:
                    tab.setText("Rascunhos");
                    break;
                case 2:
                    tab.setText("Configurações");
                    break;
            }
        }).attach();
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            // Implementar edição de perfil
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });

        btnPublishApp.setOnClickListener(v -> {
            Intent intent = new Intent(this, PublishAppActivity.class);
            startActivity(intent);
        });

        fabEditPhoto.setOnClickListener(v -> {
            // Implementar seleção de foto
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserData() {
        // Carregar dados do usuário atual
        tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário");
        tvUserEmail.setText(currentUser.getEmail());

        // Carregar dados do usuário do Firebase se existir
        databaseReference.child("usuarios").child(currentUser.getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Usuário já existe no banco, carregar dados completos
                        String nomeCompleto = dataSnapshot.child("nome_completo").getValue(String.class);
                        if (nomeCompleto != null && !nomeCompleto.isEmpty()) {
                            tvUserName.setText(nomeCompleto);
                        }
                    } else {
                        // Criar usuário no banco
                        createUserInDatabase();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UserProfileActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void createUserInDatabase() {
        // Criar usuário no banco de dados
        databaseReference.child("usuarios").child(currentUser.getUid()).child("nome_completo").setValue(currentUser.getDisplayName());
        databaseReference.child("usuarios").child(currentUser.getUid()).child("email").setValue(currentUser.getEmail());
        databaseReference.child("usuarios").child(currentUser.getUid()).child("status").setValue("ativo");
        databaseReference.child("usuarios").child(currentUser.getUid()).child("data_cadastro").setValue(java.time.LocalDateTime.now().toString());
    }

    private void loadUserApps() {
        showProgress(true);
        
        databaseReference.child("apps").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userApps.clear();
                userDrafts.clear();
                
                for (DataSnapshot appSnapshot : dataSnapshot.getChildren()) {
                    try {
                        AppItem app = appSnapshot.getValue(AppItem.class);
                        if (app != null && app.getAutor() != null && currentUser.getUid().equals(app.getAutor().getId())) {
                            app.setAppId(appSnapshot.getKey());
                            
                            if ("Publicado".equals(app.getStatus())) {
                                userApps.add(app);
                            } else if ("Rascunho".equals(app.getStatus())) {
                                userDrafts.add(app);
                            }
                        }
                    } catch (Exception e) {
                        // Ignorar apps com erro
                    }
                }
                
                updateStatistics();
                showProgress(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgress(false);
                Toast.makeText(UserProfileActivity.this, "Erro ao carregar aplicativos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatistics() {
        // Atualizar contadores
        tvAppsCount.setText(String.valueOf(userApps.size()));
        
        // Calcular total de downloads
        long totalDownloads = 0;
        double totalRating = 0;
        int appsWithRating = 0;
        
        for (AppItem app : userApps) {
            totalDownloads += app.getDownloads();
            if (app.getAvaliacao_media() > 0) {
                totalRating += app.getAvaliacao_media();
                appsWithRating++;
            }
        }
        
        tvDownloadsCount.setText(formatNumber(totalDownloads));
        
        if (appsWithRating > 0) {
            double averageRating = totalRating / appsWithRating;
            tvRatingAvg.setText(String.format("%.1f", averageRating));
        } else {
            tvRatingAvg.setText("0.0");
        }
    }

    private String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public List<AppItem> getUserApps() {
        return userApps;
    }

    public List<AppItem> getUserDrafts() {
        return userDrafts;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar dados quando voltar da tela de publicação
        loadUserApps();
    }
}
