package pro.sketchware.activities.main.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import pro.sketchware.R;
import pro.sketchware.activities.main.adapters.ScreenshotsAdapter;
import pro.sketchware.activities.main.fragments.loja.AppItem;
import pro.sketchware.activities.main.utils.FileUploadAPI;

public class PublishAppActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_SCREENSHOT_PICK = 1002;
    private static final int REQUEST_APK_PICK = 1003;
    private static final int PERMISSION_REQUEST_CODE = 1004;

    // Views
    private TextInputEditText etAppName, etShortDescription, etLongDescription, etVersion, etDownloadUrl;
    private AutoCompleteTextView spinnerCategory, spinnerDownloadType;
    private ImageView ivAppIcon;
    private RecyclerView rvScreenshots;
    private MaterialButton btnSelectIcon, btnAddScreenshot, btnUploadApk, btnSaveDraft, btnPublish;
    private MaterialTextView tvApkStatus;
    private CircularProgressIndicator progressBar;

    // Data
    private String selectedIconUrl;
    private String selectedApkUrl;
    private Map<String, String> screenshots = new HashMap<>();
    private ScreenshotsAdapter screenshotsAdapter;
    private FileUploadAPI fileUploader;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_app);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = firebaseAuth.getCurrentUser();
        
        // Initialize File Upload API
        fileUploader = new FileUploadAPI("https://rootapi.site/api_upload.php");

        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado para publicar um app", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        initViews();
        setupToolbar();
        setupSpinners();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        etAppName = findViewById(R.id.et_app_name);
        etShortDescription = findViewById(R.id.et_short_description);
        etLongDescription = findViewById(R.id.et_long_description);
        etVersion = findViewById(R.id.et_version);
        etDownloadUrl = findViewById(R.id.et_download_url);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerDownloadType = findViewById(R.id.spinner_download_type);
        ivAppIcon = findViewById(R.id.iv_app_icon);
        rvScreenshots = findViewById(R.id.rv_screenshots);
        btnSelectIcon = findViewById(R.id.btn_select_icon);
        btnAddScreenshot = findViewById(R.id.btn_add_screenshot);
        btnUploadApk = findViewById(R.id.btn_upload_apk);
        btnSaveDraft = findViewById(R.id.btn_save_draft);
        btnPublish = findViewById(R.id.btn_publish);
        tvApkStatus = findViewById(R.id.tv_apk_status);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Publicar Aplicativo");
        }
    }

    private void setupSpinners() {
        // Categorias
        String[] categories = {
            "Comunicação", "Jogos", "Entretenimento", "Produtividade", "Educação", 
            "Social", "Ferramentas", "Estilo de Vida", "Música", "Vídeo", 
            "Fotografia", "Notícias", "Clima", "Finanças", "Saúde", 
            "Esportes", "Viagem", "Compras", "Comida", "Utilitários"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // Tipos de Download
        String[] downloadTypes = {"PlayStore", "APK Direto", "GitHub", "Outro"};
        ArrayAdapter<String> downloadTypeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, downloadTypes);
        spinnerDownloadType.setAdapter(downloadTypeAdapter);
    }

    private void setupRecyclerView() {
        screenshotsAdapter = new ScreenshotsAdapter(screenshots, new ScreenshotsAdapter.OnScreenshotClickListener() {
            @Override
            public void onScreenshotClick(int position) {
                // Implementar visualização da screenshot
            }

            @Override
            public void onScreenshotDelete(int position) {
                String key = "s" + (position + 1);
                screenshots.remove(key);
                screenshotsAdapter.notifyDataSetChanged();
            }
        });
        rvScreenshots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvScreenshots.setAdapter(screenshotsAdapter);
    }

    private void setupListeners() {
        btnSelectIcon.setOnClickListener(v -> selectImage(REQUEST_IMAGE_PICK));
        btnAddScreenshot.setOnClickListener(v -> selectImage(REQUEST_SCREENSHOT_PICK));
        btnUploadApk.setOnClickListener(v -> selectSwbFile());
        btnSaveDraft.setOnClickListener(v -> saveDraft());
        btnPublish.setOnClickListener(v -> publishApp());
    }

    private void selectImage(int requestCode) {
        if (checkPermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, requestCode);
        } else {
            requestPermission();
        }
    }

    private void selectSwbFile() {
        if (checkPermission()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // Filtrar apenas arquivos .swb
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream"});
            startActivityForResult(Intent.createChooser(intent, "Selecionar arquivo .swb"), REQUEST_APK_PICK);
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
                Toast.makeText(this, "Permissão concedida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão necessária para selecionar imagens", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                if (requestCode == REQUEST_IMAGE_PICK) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                        String base64 = convertBitmapToBase64(bitmap);
                        uploadIcon(fileUri);
                    } catch (IOException e) {
                        Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == REQUEST_SCREENSHOT_PICK) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                        String base64 = convertBitmapToBase64(bitmap);
                        uploadScreenshot(fileUri);
                    } catch (IOException e) {
                        Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == REQUEST_APK_PICK) {
                    uploadSwbFile(fileUri);
                }
            }
        }
    }

    private void uploadIcon(Uri fileUri) {
        showProgress(true);
        fileUploader.uploadFile(fileUri, this, new FileUploadAPI.FileUploadCallback() {
            @Override
            public void onSuccess(List<FileUploadAPI.UploadResult> results) {
                runOnUiThread(() -> {
                    showProgress(false);
                    if (!results.isEmpty()) {
                        selectedIconUrl = results.get(0).getFileUrl();
                        // Carregar e exibir a imagem
                        loadImageFromUrl(selectedIconUrl, ivAppIcon);
                        Toast.makeText(PublishAppActivity.this, "Ícone enviado com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(PublishAppActivity.this, "Erro ao enviar ícone: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void uploadScreenshot(Uri fileUri) {
        showProgress(true);
        fileUploader.uploadFile(fileUri, this, new FileUploadAPI.FileUploadCallback() {
            @Override
            public void onSuccess(List<FileUploadAPI.UploadResult> results) {
                runOnUiThread(() -> {
                    showProgress(false);
                    if (!results.isEmpty()) {
                        String key = "s" + (screenshots.size() + 1);
                        screenshots.put(key, results.get(0).getFileUrl());
                        screenshotsAdapter.notifyDataSetChanged();
                        Toast.makeText(PublishAppActivity.this, "Screenshot enviada com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(PublishAppActivity.this, "Erro ao enviar screenshot: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void uploadSwbFile(Uri fileUri) {
        // Verificar se o arquivo tem extensão .swb
        String fileName = getFileName(this, fileUri);
        if (fileName != null && !fileName.toLowerCase().endsWith(".swb")) {
            Toast.makeText(this, "Por favor, selecione apenas arquivos .swb", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        tvApkStatus.setText("Enviando arquivo .swb...");
        
        fileUploader.uploadFile(fileUri, this, new FileUploadAPI.FileUploadCallback() {
            @Override
            public void onSuccess(List<FileUploadAPI.UploadResult> results) {
                runOnUiThread(() -> {
                    showProgress(false);
                    if (!results.isEmpty()) {
                        selectedApkUrl = results.get(0).getFileUrl();
                        tvApkStatus.setText("Arquivo .swb enviado: " + results.get(0).getFilename());
                        Toast.makeText(PublishAppActivity.this, "Arquivo .swb enviado com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    tvApkStatus.setText("Erro ao enviar arquivo .swb");
                    Toast.makeText(PublishAppActivity.this, "Erro ao enviar arquivo .swb: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void loadImageFromUrl(String imageUrl, ImageView imageView) {
        // Implementação simples para carregar imagem da URL
        // Em produção, use Glide ou Picasso
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> imageView.setImageResource(R.drawable.sketch_app_icon));
            }
        }).start();
    }

    private void saveDraft() {
        if (validateFields()) {
            showProgress(true);
            saveAppToFirebase("Rascunho");
        }
    }

    private void publishApp() {
        if (validateFields()) {
            showProgress(true);
            saveAppToFirebase("Publicado");
        }
    }

    private boolean validateFields() {
        if (etAppName.getText().toString().trim().isEmpty()) {
            etAppName.setError("Nome do aplicativo é obrigatório");
            return false;
        }
        if (etShortDescription.getText().toString().trim().isEmpty()) {
            etShortDescription.setError("Descrição curta é obrigatória");
            return false;
        }
        if (etLongDescription.getText().toString().trim().isEmpty()) {
            etLongDescription.setError("Descrição detalhada é obrigatória");
            return false;
        }
        if (spinnerCategory.getText().toString().trim().isEmpty()) {
            spinnerCategory.setError("Categoria é obrigatória");
            return false;
        }
        // Verificar se há URL de download ou arquivo .swb enviado
        if (etDownloadUrl.getText().toString().trim().isEmpty() && selectedApkUrl == null) {
            etDownloadUrl.setError("URL de download ou arquivo .swb é obrigatório");
            return false;
        }
        if (selectedIconUrl == null || selectedIconUrl.trim().isEmpty()) {
            Toast.makeText(this, "Selecione um ícone para o aplicativo", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveAppToFirebase(String status) {
        String appId = generateAppId();
        String packageName = generatePackageName();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        AppItem app = new AppItem();
        app.setId(appId);
        app.setPackageName(packageName);
        app.setNomeApp(etAppName.getText().toString().trim());
        app.setDescricaoCurta(etShortDescription.getText().toString().trim());
        app.setDescricaoLonga(etLongDescription.getText().toString().trim());
        app.setCategoria(spinnerCategory.getText().toString().trim());
        app.setIcone(selectedIconUrl);
        app.setVersao(etVersion.getText().toString().trim());
        app.setDataPublicacao(currentDate);
        
        // Usar URL do arquivo .swb se disponível, senão usar URL manual
        String downloadUrl = selectedApkUrl != null ? selectedApkUrl : etDownloadUrl.getText().toString().trim();
        app.setLocationDownload(downloadUrl);
        app.setScreenshots(screenshots);
        app.setDownloads(0);
        app.setAvaliacaoMedia(0.0);
        app.setNumeroAvaliacoes(0);

        // Criar autor
        AppItem.Autor autor = new AppItem.Autor();
        autor.setId(currentUser.getUid());
        autor.setNome(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário");
        app.setAutor(autor);

        // Salvar no Firebase
        databaseReference.child("apps").child(appId).setValue(app)
            .addOnSuccessListener(aVoid -> {
                showProgress(false);
                String message = status.equals("Publicado") ? "Aplicativo publicado com sucesso!" : "Rascunho salvo com sucesso!";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                showProgress(false);
                Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private String generateAppId() {
        return "com." + currentUser.getUid().substring(0, Math.min(8, currentUser.getUid().length())) + "." + 
               System.currentTimeMillis();
    }

    private String generatePackageName() {
        String appName = etAppName.getText().toString().trim().toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        return "com." + currentUser.getUid().substring(0, Math.min(8, currentUser.getUid().length())) + "." + appName;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveDraft.setEnabled(!show);
        btnPublish.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}