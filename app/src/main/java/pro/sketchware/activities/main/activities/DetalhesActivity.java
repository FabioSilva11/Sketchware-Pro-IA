package pro.sketchware.activities.main.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pro.sketchware.R;
import pro.sketchware.activities.main.adapters.ComentariosAdapter;
import pro.sketchware.activities.main.adapters.ScreenshotsAdapter;

public class DetalhesActivity extends AppCompatActivity {
    
    private ProgressBar progressBar;
    private LinearLayout linearDetalhes;
    private ImageView appIcon;
    private MaterialTextView appName;
    private MaterialTextView appDescriptionCurta;
    private MaterialTextView downloadCount;
    private MaterialButton buttonDownload;
    private MaterialTextView textoDescriptionLonga;
    
    // Dados do app recebidos via Intent
    private String appId;
    private String appTitle;
    private String appDescription;
    private String appCategory;
    private int appDownloads;
    private String appIconUrl;
    
    // Firebase
    private DatabaseReference databaseReference;
    
    // Classes de dados do Firebase
    public static class AppData {
        public String app_id;
        public String nome;
        public String descricao_curta;
        public String descricao_longa;
        public String icone;
        public String url_download;
        public String data_publicacao;
        public Publisher publisher;
        public Estatisticas estatisticas;
        public Map<String, Boolean> likes;
        public Map<String, Comentario> comentarios;
        public List<String> screenshots;
        
        public AppData() {}
    }
    
    public static class Publisher {
        public String usuario_id;
        
        public Publisher() {}
    }
    
    public static class Estatisticas {
        public int comentarios;
        public int downloads;
        public int likes;
        
        public Estatisticas() {}
    }
    
    public static class Comentario {
        public String comentario;
        public long timestamp;
        public String usuario_id;
        
        public Comentario() {}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalhes_activity);
        
        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("apps");
        
        // Configurar toolbar
        setupToolbar();
        
        // Inicializar views
        initViews();
        
        // Receber dados da Intent
        receiveIntentData();
        
        // Configurar listeners
        setupListeners();
        
        // Carregar dados do app do Firebase
        loadAppDataFromFirebase();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Detalhes do App");
            }
        }
    }
    
    private void initViews() {
        progressBar = findViewById(R.id.progressBar2);
        linearDetalhes = findViewById(R.id.linear_detalhes);
        appIcon = findViewById(R.id.app_icon);
        appName = findViewById(R.id.app_name);
        appDescriptionCurta = findViewById(R.id.app_description_curta);
        downloadCount = findViewById(R.id.dowload_cont);
        buttonDownload = findViewById(R.id.button_dowload);
        textoDescriptionLonga = findViewById(R.id.texto_description_longa);
    }
    
    private void receiveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            appId = intent.getStringExtra("app_id");
            appTitle = intent.getStringExtra("app_title");
            appDescription = intent.getStringExtra("app_description");
            appCategory = intent.getStringExtra("app_category");
            appDownloads = intent.getIntExtra("app_downloads", 0);
            appIconUrl = intent.getStringExtra("app_icon_url");
        }
    }
    
    private void setupListeners() {
        buttonDownload.setOnClickListener(v -> handleDownloadClick());
    }
    
    private void loadAppDataFromFirebase() {
        if (appId == null) {
            Toast.makeText(this, "Erro: ID do app não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Mostrar progress bar inicialmente
        progressBar.setVisibility(View.VISIBLE);
        linearDetalhes.setVisibility(View.GONE);
        
        // Buscar dados do app no Firebase
        databaseReference.child(appId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    AppData appData = dataSnapshot.getValue(AppData.class);
                    if (appData != null) {
                        populateAppData(appData);
                    } else {
                        showError("Erro ao carregar dados do app");
                    }
                } else {
                    showError("App não encontrado");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Erro ao carregar dados: " + databaseError.getMessage());
            }
        });
    }
    
    private void populateAppData(AppData appData) {
        try {
            // Preencher dados básicos
            if (appData.nome != null) {
                appName.setText(appData.nome);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(appData.nome);
                }
            }
            
            if (appData.descricao_curta != null) {
                appDescriptionCurta.setText(appData.descricao_curta);
            }
            
            if (appData.descricao_longa != null) {
                textoDescriptionLonga.setText(appData.descricao_longa);
            }
            
            // Configurar ícone
            if (appData.icone != null && appData.icone.startsWith("data:image")) {
                try {
                    String base64Data = appData.icone.substring(appData.icone.indexOf(",") + 1);
                    byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    
                    if (bitmap != null) {
                        appIcon.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    // Em caso de erro, manter ícone padrão
                }
            }
            
            // Configurar estatísticas
            if (appData.estatisticas != null) {
                if (appData.estatisticas.downloads > 0) {
                    downloadCount.setText(formatDownloadCount(appData.estatisticas.downloads));
                }
            }
            
            // Configurar ViewStubs
            setupViewStubs(appData);
            
            // Esconder progress bar e mostrar conteúdo
            progressBar.setVisibility(View.GONE);
            linearDetalhes.setVisibility(View.VISIBLE);
            
        } catch (Exception e) {
            showError("Erro ao processar dados: " + e.getMessage());
        }
    }
    
    private void setupViewStubs(AppData appData) {
        // Configurar screenshots
        if (appData.screenshots != null && !appData.screenshots.isEmpty()) {
            ViewStub stubScreenshots = findViewById(R.id.stub_screenshots);
            if (stubScreenshots != null) {
                View screenshotsView = stubScreenshots.inflate();
                setupScreenshots(screenshotsView, appData.screenshots);
            }
        }
        

        
        // Configurar comentários
        if (appData.comentarios != null && !appData.comentarios.isEmpty()) {
            ViewStub stubComentarios = findViewById(R.id.stub_comentarios);
            if (stubComentarios != null) {
                View comentariosView = stubComentarios.inflate();
                setupComentarios(comentariosView, appData.comentarios);
            }
        }
    }
    
    private void setupScreenshots(View view, List<String> screenshots) {
        if (screenshots == null || screenshots.isEmpty()) return;
        
        // Configurar RecyclerView de screenshots
        androidx.recyclerview.widget.RecyclerView recyclerView = view.findViewById(R.id.recyclerview_screenshots);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(
                    this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false
            ));
            
            ScreenshotsAdapter adapter = new ScreenshotsAdapter(this, screenshots);
            recyclerView.setAdapter(adapter);
        }
    }
    

    
    private void setupComentarios(View view, Map<String, Comentario> comentarios) {
        if (comentarios == null || comentarios.isEmpty()) return;
        
        // Configurar RecyclerView de comentários
        androidx.recyclerview.widget.RecyclerView recyclerView = view.findViewById(R.id.recyclerview_comentarios);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            
            ComentariosAdapter adapter = new ComentariosAdapter(comentarios);
            recyclerView.setAdapter(adapter);
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
        finish();
    }
    
    private void handleDownloadClick() {
        if (appId != null) {
            // Buscar URL de download no Firebase
            databaseReference.child(appId).child("url_download").get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String downloadUrl = dataSnapshot.getValue(String.class);
                    if (downloadUrl != null && !downloadUrl.isEmpty()) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(DetalhesActivity.this, "Erro ao abrir link de download", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DetalhesActivity.this, "Link de download não disponível", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetalhesActivity.this, "Link de download não encontrado", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(DetalhesActivity.this, "Erro ao buscar link de download", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Erro: ID do app não encontrado", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String formatDownloadCount(int downloads) {
        if (downloads >= 1000000) {
            return String.format("%.1fM", downloads / 1000000.0);
        } else if (downloads >= 1000) {
            return String.format("%.1fK", downloads / 1000.0);
        } else {
            return String.valueOf(downloads);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
