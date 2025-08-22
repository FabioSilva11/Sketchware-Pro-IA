package pro.sketchware.activities.main.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
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
    private RecyclerView recyclerViewScreenshots;
    private RecyclerView recyclerViewComentarios;
    
    // Views da seção de avaliações
    private MaterialTextView averageRating;
    private MaterialRatingBar ratingStars;
    private MaterialTextView totalReviews;
    private LinearProgressIndicator progress5Stars, progress4Stars, progress3Stars, progress2Stars, progress1Stars;
    
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
        public double rating; // Novo campo para rating
        
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
        recyclerViewScreenshots = findViewById(R.id.recyclerview_screenshots);
        recyclerViewComentarios = findViewById(R.id.recyclerview_comentarios);
        
        // Inicializar views da seção de avaliações
        View ratingSection = findViewById(R.id.rating_summary_section);
        if (ratingSection != null) {
            averageRating = ratingSection.findViewById(R.id.average_rating);
            ratingStars = ratingSection.findViewById(R.id.rating_stars);
            totalReviews = ratingSection.findViewById(R.id.total_reviews);
            progress5Stars = ratingSection.findViewById(R.id.progress_5_stars);
            progress4Stars = ratingSection.findViewById(R.id.progress_4_stars);
            progress3Stars = ratingSection.findViewById(R.id.progress_3_stars);
            progress2Stars = ratingSection.findViewById(R.id.progress_2_stars);
            progress1Stars = ratingSection.findViewById(R.id.progress_1_stars);
        }
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
        
        // Listener para adicionar avaliação - verificar se usuário não é o autor
        View ratingSection = findViewById(R.id.rating_summary_section);
        if (ratingSection != null) {
            ratingSection.findViewById(R.id.btn_view_all_reviews).setOnClickListener(v -> {
                // Verificar se usuário está logado
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(this, "Você precisa estar logado para enviar uma avaliação", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Verificar se o usuário é o autor do app
                databaseReference.child(appId).child("publisher").child("usuario_id").get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        String authorId = dataSnapshot.getValue(String.class);
                        if (authorId != null && authorId.equals(currentUser.getUid())) {
                            Toast.makeText(this, "Você não pode avaliar seu próprio app", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    
                    // Se não é o autor, mostrar o dialog
                    showReviewDialog();
                    
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao verificar autor do app", Toast.LENGTH_SHORT).show();
                });
            });
        }
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
            
            // Configurar screenshots
            setupScreenshots(appData.screenshots);
            
            // Configurar avaliações
            setupRatings(appData.comentarios);
            
            // Configurar comentários
            setupComentarios(appData.comentarios);
            
            // Esconder progress bar e mostrar conteúdo
            progressBar.setVisibility(View.GONE);
            linearDetalhes.setVisibility(View.VISIBLE);
            
        } catch (Exception e) {
            showError("Erro ao processar dados: " + e.getMessage());
        }
    }
    
    private void setupScreenshots(List<String> screenshots) {
        if (screenshots == null || screenshots.isEmpty()) {
            recyclerViewScreenshots.setVisibility(View.GONE);
            return;
        }
        
        recyclerViewScreenshots.setVisibility(View.VISIBLE);
        
        // Configurar layout horizontal explicitamente
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewScreenshots.setLayoutManager(layoutManager);
        
        ScreenshotsAdapter adapter = new ScreenshotsAdapter(this, screenshots);
        recyclerViewScreenshots.setAdapter(adapter);
        
        // Log para debug
        System.out.println("Screenshots configurados: " + screenshots.size() + " itens, layout horizontal");
    }
    
    private void setupRatings(Map<String, Comentario> comentarios) {
        RatingManager.RatingStats stats = RatingManager.calculateRatingStats(comentarios);
        
        if (stats.totalReviews == 0) {
            // Esconder seção de avaliações se não há avaliações
            View ratingSection = findViewById(R.id.rating_summary_section);
            if (ratingSection != null) {
                ratingSection.setVisibility(View.GONE);
            }
            return;
        }
        
        // Mostrar seção de avaliações
        View ratingSection = findViewById(R.id.rating_summary_section);
        if (ratingSection != null) {
            ratingSection.setVisibility(View.VISIBLE);
        }
        
        // Configurar rating médio
        if (averageRating != null) {
            averageRating.setText(String.format("%.1f", stats.averageRating));
        }
        
        if (ratingStars != null) {
            ratingStars.setRating((float) stats.averageRating);
        }
        
        if (totalReviews != null) {
            totalReviews.setText(RatingManager.formatReviewCount(stats.totalReviews) + " avaliações");
        }
        
        // Configurar barras de progresso
        if (progress5Stars != null) {
            progress5Stars.setProgress(RatingManager.calculateProgressPercentage(stats.starDistribution[4], stats.totalReviews));
        }
        if (progress4Stars != null) {
            progress4Stars.setProgress(RatingManager.calculateProgressPercentage(stats.starDistribution[3], stats.totalReviews));
        }
        if (progress3Stars != null) {
            progress3Stars.setProgress(RatingManager.calculateProgressPercentage(stats.starDistribution[2], stats.totalReviews));
        }
        if (progress2Stars != null) {
            progress2Stars.setProgress(RatingManager.calculateProgressPercentage(stats.starDistribution[1], stats.totalReviews));
        }
        if (progress1Stars != null) {
            progress1Stars.setProgress(RatingManager.calculateProgressPercentage(stats.starDistribution[0], stats.totalReviews));
        }
    }
    
    private void setupComentarios(Map<String, Comentario> comentarios) {
        if (comentarios == null || comentarios.isEmpty()) {
            recyclerViewComentarios.setVisibility(View.GONE);
            return;
        }
        
        recyclerViewComentarios.setVisibility(View.VISIBLE);
        recyclerViewComentarios.setLayoutManager(new LinearLayoutManager(this));
            
            ComentariosAdapter adapter = new ComentariosAdapter(comentarios);
        recyclerViewComentarios.setAdapter(adapter);
    }
    
    private void showReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_review_dialog, null);
        
        MaterialRatingBar ratingBar = dialogView.findViewById(R.id.user_rating_bar);
        TextInputEditText commentInput = dialogView.findViewById(R.id.review_comment);
        MaterialButton cancelButton = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton submitButton = dialogView.findViewById(R.id.btn_submit);
        
        // Habilitar botão de envio apenas quando há rating
        ratingBar.setOnRatingChangeListener((ratingBar1, rating) -> {
            submitButton.setEnabled(rating > 0);
        });
        
        AlertDialog dialog = builder.setView(dialogView).create();
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        submitButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = commentInput.getText().toString().trim();
            
            if (rating > 0) {
                submitReview(rating, comment);
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
    
    private void submitReview(float rating, String comment) {
        if (appId == null) return;
        
        // Verificar se usuário está logado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado para enviar uma avaliação", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Criar novo comentário com rating
        Comentario newComment = new Comentario();
        newComment.rating = rating;
        newComment.comentario = comment;
        newComment.usuario_id = currentUser.getUid();
        newComment.timestamp = System.currentTimeMillis();
        
        // Salvar no Firebase
        String commentId = databaseReference.child(appId).child("comentarios").push().getKey();
        if (commentId != null) {
            databaseReference.child(appId).child("comentarios").child(commentId).setValue(newComment)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Avaliação enviada com sucesso!", Toast.LENGTH_SHORT).show();
                        // Recarregar dados para atualizar as estatísticas
                        loadAppDataFromFirebase();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao enviar avaliação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
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


