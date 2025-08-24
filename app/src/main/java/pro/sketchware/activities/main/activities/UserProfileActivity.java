package pro.sketchware.activities.main.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.main.adapters.UserProfilePagerAdapter;
import pro.sketchware.activities.main.fragments.loja.AppItem;
import pro.sketchware.activities.main.fragments.loja.Usuario;
import pro.sketchware.activities.main.utils.FileUploadAPI;

public class UserProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;

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
    private Usuario currentUsuario;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    // File Upload API
    private FileUploadAPI fileUploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = firebaseAuth.getCurrentUser();

        // Initialize File Upload API
        fileUploader = new FileUploadAPI("https://rootapi.site/api_upload.php");

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
            if (checkPermission()) {
                selectImage();
            } else {
                requestPermission();
            }
        });
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
               == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
            PERMISSION_REQUEST_CODE);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null && requestCode == REQUEST_IMAGE_PICK) {
                uploadProfilePhoto(fileUri);
            }
        }
    }

    private void uploadProfilePhoto(Uri fileUri) {
        showProgress(true);
        
        fileUploader.uploadFile(fileUri, this, new FileUploadAPI.FileUploadCallback() {
            @Override
            public void onSuccess(List<FileUploadAPI.UploadResult> results) {
                if (!results.isEmpty()) {
                    String photoUrl = results.get(0).getFileUrl();
                    updateProfilePhotoInDatabase(photoUrl);
                } else {
                    showProgress(false);
                    Toast.makeText(UserProfileActivity.this, "Erro no upload da foto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                showProgress(false);
                Toast.makeText(UserProfileActivity.this, "Erro no upload: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfilePhotoInDatabase(String photoUrl) {
        databaseReference.child("usuarios").child(currentUser.getUid()).child("foto_perfil").setValue(photoUrl)
            .addOnSuccessListener(aVoid -> {
                showProgress(false);
                loadProfilePhoto(photoUrl);
                Toast.makeText(this, "Foto de perfil atualizada com sucesso!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(this, "Erro ao atualizar foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadProfilePhoto(String photoUrl) {
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            if (photoUrl.startsWith("data:image")) {
                // Processar imagem base64
                try {
                    String base64Data = photoUrl.substring(photoUrl.indexOf(",") + 1);
                    byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    
                    if (bitmap != null) {
                        ivProfilePhoto.setImageBitmap(bitmap);
                    } else {
                        ivProfilePhoto.setImageResource(R.drawable.sketch_app_icon);
                    }
                } catch (Exception e) {
                    ivProfilePhoto.setImageResource(R.drawable.sketch_app_icon);
                }
            } else if (photoUrl.startsWith("http")) {
                // Usar Picasso para carregar imagem via URL
                Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.sketch_app_icon)
                    .error(R.drawable.sketch_app_icon)
                    .fit()
                    .centerCrop()
                    .into(ivProfilePhoto);
            } else {
                ivProfilePhoto.setImageResource(R.drawable.sketch_app_icon);
            }
        } else {
            ivProfilePhoto.setImageResource(R.drawable.sketch_app_icon);
        }
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
                        currentUsuario = dataSnapshot.getValue(Usuario.class);
                        if (currentUsuario != null) {
                            currentUsuario.setId(currentUser.getUid());
                            
                            String nomeCompleto = currentUsuario.getNome_completo();
                            if (nomeCompleto != null && !nomeCompleto.isEmpty()) {
                                tvUserName.setText(nomeCompleto);
                            }
                            
                            // Carregar foto de perfil
                            String fotoPerfil = currentUsuario.getFoto_perfil();
                            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                                loadProfilePhoto(fotoPerfil);
                            }
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
        currentUsuario = new Usuario(
            currentUser.getUid(),
            currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário",
            currentUser.getEmail()
        );
        currentUsuario.setData_cadastro(java.time.LocalDateTime.now().toString());
        
        databaseReference.child("usuarios").child(currentUser.getUid()).setValue(currentUsuario)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Perfil criado com sucesso!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao criar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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
                updateViewPagerData();
                showProgress(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgress(false);
                Toast.makeText(UserProfileActivity.this, "Erro ao carregar aplicativos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateViewPagerData() {
        if (viewPager.getAdapter() instanceof UserProfilePagerAdapter) {
            UserProfilePagerAdapter adapter = (UserProfilePagerAdapter) viewPager.getAdapter();
            adapter.updateData(userApps, userDrafts);
        }
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
        btnEditProfile.setEnabled(!show);
        btnPublishApp.setEnabled(!show);
        fabEditPhoto.setEnabled(!show);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permissão necessária para selecionar foto", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
