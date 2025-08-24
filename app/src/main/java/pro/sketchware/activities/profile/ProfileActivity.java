package pro.sketchware.activities.profile;

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
import com.google.android.material.appbar.MaterialToolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
import pro.sketchware.activities.main.activities.PublishAppActivity;
import pro.sketchware.activities.main.activities.UserProfileActivity;
import pro.sketchware.activities.main.fragments.loja.AppItem;
import pro.sketchware.activities.main.utils.FileUploadAPI;

public class ProfileActivity extends AppCompatActivity {
    
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;
    
    // Views
    private ImageView ivProfilePhoto;
    private MaterialTextView tvUserName, tvUserEmail;
    private MaterialTextView tvAppsCount, tvDownloadsCount, tvRatingAvg;
    private MaterialTextView tvAccountStatus, tvAccountType;
    private MaterialButton btnEditProfile, btnPublishApp;
    private FloatingActionButton fabEditPhoto;
    private CircularProgressIndicator progressBar;
    
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    
    // File Upload API
    private FileUploadAPI fileUploader;
    
    // Data
    private List<AppItem> userApps = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        
        // Inicializar File Upload API
        fileUploader = new FileUploadAPI("https://rootapi.site/api_upload.php");
        
        // Verificar se usuário está logado
        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Inicializar views
        initViews();
        
        // Configurar toolbar
        setupToolbar();
        
        // Configurar listeners
        setupListeners();
        
        // Carregar dados do usuário
        loadUserData();
        
        // Carregar apps do usuário
        loadUserApps();
    }
    
    private void initViews() {
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvAppsCount = findViewById(R.id.tv_apps_count);
        tvDownloadsCount = findViewById(R.id.tv_downloads_count);
        tvRatingAvg = findViewById(R.id.tv_rating_avg);
        tvAccountStatus = findViewById(R.id.tv_account_status);
        tvAccountType = findViewById(R.id.tv_account_type);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnPublishApp = findViewById(R.id.btn_publish_app);
        fabEditPhoto = findViewById(R.id.fab_edit_photo);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupListeners() {
        // Botão editar foto
        fabEditPhoto.setOnClickListener(v -> selectImage());
        
        // Botão editar perfil
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        });
        
        // Botão publicar app
        btnPublishApp.setOnClickListener(v -> {
            Intent intent = new Intent(this, PublishAppActivity.class);
            startActivity(intent);
        });
        
        // Funcionalidades
        findViewById(R.id.btn_publish_projects).setOnClickListener(v -> {
            Intent intent = new Intent(this, PublishAppActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.btn_manage_projects).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.btn_account_settings).setOnClickListener(v -> {
            // Implementar tela de configurações da conta
            Toast.makeText(this, "Configurações da conta em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btn_activity_history).setOnClickListener(v -> {
            // Implementar histórico de atividades
            Toast.makeText(this, "Histórico de atividades em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadUserData() {
        if (currentUser == null) return;
        
        showProgress(true);
        
        // Carregar dados básicos do usuário
        tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário");
        tvUserEmail.setText(currentUser.getEmail());
        
        // Carregar foto do perfil se existir
        if (currentUser.getPhotoUrl() != null) {
            loadProfilePhoto(currentUser.getPhotoUrl().toString());
        }
        
        // Carregar dados adicionais do Firebase
        mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String displayName = snapshot.child("displayName").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);
                    String accountType = snapshot.child("accountType").getValue(String.class);
                    
                    if (displayName != null) {
                        tvUserName.setText(displayName);
                    }
                    
                    if (photoUrl != null) {
                        loadProfilePhoto(photoUrl);
                    }
                    
                    if (accountType != null) {
                        tvAccountType.setText(accountType);
                    }
                }
                
                showProgress(false);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgress(false);
                Toast.makeText(ProfileActivity.this, "Erro ao carregar dados: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadUserApps() {
        if (currentUser == null) return;
        
        mDatabase.child("apps").orderByChild("autor/id").equalTo(currentUser.getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userApps.clear();
                    long totalDownloads = 0;
                    double totalRating = 0;
                    int appsWithRating = 0;
                    
                    for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                        AppItem app = appSnapshot.getValue(AppItem.class);
                        if (app != null) {
                            userApps.add(app);
                            totalDownloads += app.getDownloads();
                            
                            if (app.getAvaliacao_media() > 0) {
                                totalRating += app.getAvaliacao_media();
                                appsWithRating++;
                            }
                        }
                    }
                    
                    // Atualizar estatísticas
                    updateStatistics(userApps.size(), totalDownloads, totalRating, appsWithRating);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Erro ao carregar apps: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void updateStatistics(int appsCount, long totalDownloads, double totalRating, int appsWithRating) {
        tvAppsCount.setText(String.valueOf(appsCount));
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
    
    private void selectImage() {
        if (checkPermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        } else {
            requestPermission();
        }
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
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permissão necessária para selecionar imagem", Toast.LENGTH_SHORT).show();
            }
        }
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
                runOnUiThread(() -> {
                    showProgress(false);
                    if (!results.isEmpty()) {
                        String photoUrl = results.get(0).getFileUrl();
                        updateProfilePhoto(photoUrl);
                        Toast.makeText(ProfileActivity.this, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ProfileActivity.this, "Erro ao enviar foto: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void updateProfilePhoto(String photoUrl) {
        // Carregar imagem na UI
        loadProfilePhoto(photoUrl);
        
        // Salvar URL no Firebase
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid()).child("photoUrl").setValue(photoUrl);
        }
    }
    
    private void loadProfilePhoto(String photoUrl) {
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            Picasso.get()
                .load(photoUrl)
                .placeholder(R.drawable.sketch_app_icon)
                .error(R.drawable.sketch_app_icon)
                .fit()
                .centerCrop()
                .into(ivProfilePhoto);
        }
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnEditProfile.setEnabled(!show);
        btnPublishApp.setEnabled(!show);
        fabEditPhoto.setEnabled(!show);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar dados quando retornar à tela
        loadUserData();
        loadUserApps();
    }
}

