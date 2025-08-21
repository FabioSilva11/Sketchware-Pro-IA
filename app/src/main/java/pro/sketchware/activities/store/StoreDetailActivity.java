package pro.sketchware.activities.store;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import pro.sketchware.R;
import pro.sketchware.databinding.ActivityStoreDetailBinding;
import pro.sketchware.databinding.BottomSheetCommentBinding;
import pro.sketchware.activities.store.adapters.ScreenshotsAdapter;
import pro.sketchware.activities.store.adapters.CommentsAdapter;

public class StoreDetailActivity extends BaseAppCompatActivity {

    private ActivityStoreDetailBinding binding;
    private String appId;
    private String currentUserId;
    private boolean isLiked = false;
    private int likeCount = 0;
    private ScreenshotsAdapter screenshotsAdapter;
    private CommentsAdapter commentsAdapter;
    private final ArrayList<CommentsAdapter.CommentItem> comments = new ArrayList<>();
    private String userCommentId = null; // Track user's own comment
    private BottomSheetDialog commentDialog;
    private int userRating = 0; // Track user's rating (0-5)
    private double overallRating = 4.8; // Overall app rating
    private int totalRatings = 4042969; // Total number of ratings

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdgeNoContrast();
        super.onCreate(savedInstanceState);

        binding = ActivityStoreDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(getTranslatedString(R.string.store_details));

        appId = getIntent().getStringExtra("app_id");
        if (appId == null || appId.isEmpty()) {
            Toast.makeText(this, getTranslatedString(R.string.store_error_load), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        setupScreenshotsRecyclerView();
        setupCommentsRecyclerView();
        setupButtons();
        loadDetails();
    }

    private void setupScreenshotsRecyclerView() {
        screenshotsAdapter = new ScreenshotsAdapter(new ArrayList<>());
        binding.screenshots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.screenshots.setAdapter(screenshotsAdapter);
        binding.screenshots.setHasFixedSize(true);
    }

    private void setupCommentsRecyclerView() {
        commentsAdapter = new CommentsAdapter(comments, currentUserId, (commentId, currentText) -> {
            showCommentDialog(true, commentId, currentText);
        });
        binding.comments.setLayoutManager(new LinearLayoutManager(this));
        binding.comments.setAdapter(commentsAdapter);
        binding.comments.setHasFixedSize(true);
    }

    private void setupButtons() {
        binding.btnDownload.setOnClickListener(v -> {
            // Download functionality will be implemented in loadDetails
        });

        binding.btnLike.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "Faça login para curtir", Toast.LENGTH_SHORT).show();
                return;
            }
            toggleLike();
        });

        binding.btnAddComment.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "Faça login para comentar", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if user already has a comment
            if (userCommentId != null) {
                Toast.makeText(this, getTranslatedString(R.string.store_comment_limit), Toast.LENGTH_SHORT).show();
                return;
            }
            
            showCommentDialog(false, null, "");
        });

        // Setup rating stars
        setupRatingStars();
        
        // Setup write review button
        binding.btnWriteReview.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "Faça login para avaliar", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if user already has a rating
            if (userRating > 0) {
                Toast.makeText(this, getTranslatedString(R.string.store_rating_limit), Toast.LENGTH_SHORT).show();
                return;
            }
            
            showRatingCommentDialog(0);
        });
    }

    private void showCommentDialog(boolean isEditing, String commentId, String currentText) {
        if (commentDialog != null && commentDialog.isShowing()) {
            commentDialog.dismiss();
        }

        commentDialog = new BottomSheetDialog(this);
        BottomSheetCommentBinding dialogBinding = BottomSheetCommentBinding.inflate(LayoutInflater.from(this));
        commentDialog.setContentView(dialogBinding.getRoot());

        // Set title
        if (isEditing) {
            dialogBinding.title.setText(getTranslatedString(R.string.store_edit));
            dialogBinding.commentInput.setText(currentText);
        } else {
            dialogBinding.title.setText(getTranslatedString(R.string.store_add_comment));
        }

        // Close button
        dialogBinding.btnClose.setOnClickListener(v -> commentDialog.dismiss());

        // Cancel button
        dialogBinding.btnCancel.setOnClickListener(v -> commentDialog.dismiss());

        // Save button
        dialogBinding.btnSave.setOnClickListener(v -> {
            String commentText = dialogBinding.commentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "Digite um comentário", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditing) {
                updateComment(commentId, commentText);
            } else {
                saveComment(commentText);
            }
            commentDialog.dismiss();
        });

        commentDialog.show();
    }

    private void setupRatingStars() {
        // Setup star click listeners
        binding.star1.setOnClickListener(v -> setRating(1));
        binding.star2.setOnClickListener(v -> setRating(2));
        binding.star3.setOnClickListener(v -> setRating(3));
        binding.star4.setOnClickListener(v -> setRating(4));
        binding.star5.setOnClickListener(v -> setRating(5));
        
        // Update star display
        updateStarDisplay();
    }

    private void setRating(int rating) {
        if (currentUserId == null) {
            Toast.makeText(this, "Faça login para avaliar", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (userRating > 0) {
            Toast.makeText(this, getTranslatedString(R.string.store_rating_limit), Toast.LENGTH_SHORT).show();
            return;
        }
        
        userRating = rating;
        showRatingCommentDialog(rating);
    }

    private void updateStarDisplay() {
        // Update individual stars based on user rating
        binding.star1.setImageResource(userRating >= 1 ? R.drawable.ic_star : R.drawable.ic_star_border);
        binding.star2.setImageResource(userRating >= 2 ? R.drawable.ic_star : R.drawable.ic_star_border);
        binding.star3.setImageResource(userRating >= 3 ? R.drawable.ic_star : R.drawable.ic_star_border);
        binding.star4.setImageResource(userRating >= 4 ? R.drawable.ic_star : R.drawable.ic_star_border);
        binding.star5.setImageResource(userRating >= 5 ? R.drawable.ic_star : R.drawable.ic_star_border);
        
        // Update overall rating display
        binding.overallRating.setText(String.format("%.1f", overallRating));
        binding.totalRatings.setText(String.format("%,d", totalRatings));
    }

    private void saveRatingWithComment(int rating, String comment) {
        // Salvar avaliação
        DatabaseReference ratingRef = FirebaseDatabase.getInstance().getReference("apps")
            .child(appId).child("avaliacoes").child(currentUserId);
        
        java.util.HashMap<String, Object> ratingData = new java.util.HashMap<>();
        ratingData.put("rating", rating);
        ratingData.put("timestamp", System.currentTimeMillis());
        
        // Salvar comentário junto com a avaliação
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("apps")
            .child(appId).child("comentarios").child(currentUserId);
        
        java.util.HashMap<String, Object> commentData = new java.util.HashMap<>();
        commentData.put("usuario_id", currentUserId);
        commentData.put("comentario", comment);
        commentData.put("rating", rating);
        commentData.put("timestamp", System.currentTimeMillis());
        
        // Salvar ambos simultaneamente
        ratingRef.setValue(ratingData).addOnSuccessListener(aVoid -> {
            commentRef.setValue(commentData).addOnSuccessListener(aVoid2 -> {
                updateStarDisplay();
                updateOverallRating();
                Toast.makeText(this, "Avaliação e comentário enviados com sucesso!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao salvar comentário", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erro ao salvar avaliação", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateOverallRating() {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("apps")
            .child(appId).child("avaliacoes");
        
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalRating = 0;
                int count = 0;
                
                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Object rating = ratingSnapshot.child("rating").getValue();
                    if (rating != null) {
                        totalRating += Integer.parseInt(String.valueOf(rating));
                        count++;
                    }
                }
                
                if (count > 0) {
                    overallRating = totalRating / count;
                    totalRatings = count;
                    
                    // Update Firebase statistics
                    DatabaseReference appRef = FirebaseDatabase.getInstance().getReference("apps").child(appId);
                    appRef.child("estatisticas").child("rating").setValue(overallRating);
                    appRef.child("estatisticas").child("total_ratings").setValue(totalRatings);
                    
                    updateStarDisplay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showRatingCommentDialog(int rating) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_rating_comment, null);
        dialog.setContentView(dialogView);
        
        // Configurar estrelas no diálogo
        ImageView star1 = dialogView.findViewById(R.id.star1);
        ImageView star2 = dialogView.findViewById(R.id.star2);
        ImageView star3 = dialogView.findViewById(R.id.star3);
        ImageView star4 = dialogView.findViewById(R.id.star4);
        ImageView star5 = dialogView.findViewById(R.id.star5);
        
        // Atualizar estrelas baseado na avaliação
        star1.setImageResource(rating >= 1 ? R.drawable.ic_star : R.drawable.ic_star_border);
        star2.setImageResource(rating >= 2 ? R.drawable.ic_star : R.drawable.ic_star_border);
        star3.setImageResource(rating >= 3 ? R.drawable.ic_star : R.drawable.ic_star_border);
        star4.setImageResource(rating >= 4 ? R.drawable.ic_star : R.drawable.ic_star_border);
        star5.setImageResource(rating >= 5 ? R.drawable.ic_star : R.drawable.ic_star_border);
        
        // Configurar botões
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        
        btnSubmit.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            if (comment.isEmpty()) {
                comment = "Avaliação sem comentário";
            }
            
            saveRatingWithComment(rating, comment);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> {
            userRating = 0; // Reset rating
            updateStarDisplay();
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void hideRatingSection() {
        // Não ocultar mais a seção de avaliações
        // Apenas mostrar uma mensagem de que já foi avaliado
        Toast.makeText(this, "Você já avaliou este app", Toast.LENGTH_SHORT).show();
    }

    private void toggleLike() {
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("apps")
            .child(appId).child("likes").child(currentUserId);
        
        if (isLiked) {
            // Unlike
            likeRef.removeValue().addOnSuccessListener(aVoid -> {
                isLiked = false;
                likeCount--;
                updateLikeButton();
                updateLikeCount();
            });
        } else {
            // Like
            likeRef.setValue(true).addOnSuccessListener(aVoid -> {
                isLiked = true;
                likeCount++;
                updateLikeButton();
                updateLikeCount();
            });
        }
    }

    private void updateLikeButton() {
        if (isLiked) {
            binding.btnLike.setIconResource(R.drawable.ic_favorite);
            binding.btnLike.setIconTintResource(R.color.error);
        } else {
            binding.btnLike.setIconResource(R.drawable.ic_favorite_border);
            binding.btnLike.setIconTintResource(android.R.color.darker_gray);
        }
        binding.btnLike.setText(String.valueOf(likeCount));
    }

    private void updateLikeCount() {
        DatabaseReference appRef = FirebaseDatabase.getInstance().getReference("apps").child(appId);
        appRef.child("estatisticas").child("likes").setValue(likeCount);
    }

    private void saveComment(String commentText) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("apps")
            .child(appId).child("comentarios").push();
        
        String commentId = commentRef.getKey();
        userCommentId = commentId; // Track user's comment ID
        
        java.util.HashMap<String, Object> commentData = new java.util.HashMap<>();
        commentData.put("usuario_id", currentUserId);
        commentData.put("comentario", commentText);
        commentData.put("timestamp", System.currentTimeMillis());
        
        commentRef.setValue(commentData).addOnSuccessListener(aVoid -> {
            updateCommentCount();
            Toast.makeText(this, getTranslatedString(R.string.store_comment_saved), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateComment(String commentId, String newText) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("apps")
            .child(appId).child("comentarios").child(commentId);
        
        commentRef.child("comentario").setValue(newText).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, getTranslatedString(R.string.store_comment_updated), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCommentCount() {
        DatabaseReference appRef = FirebaseDatabase.getInstance().getReference("apps").child(appId);
        appRef.child("estatisticas").child("comentarios").setValue(comments.size());
    }

    private void loadDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("apps").child(appId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                // Stop loading and show content
                binding.content.setVisibility(android.view.View.VISIBLE);

                String nome = String.valueOf(s.child("nome").getValue());
                String icon = String.valueOf(s.child("icone").getValue());
                String shortDesc = String.valueOf(s.child("descricao_curta").getValue());
                String longDesc = String.valueOf(s.child("descricao_longa").getValue());
                String downloadUrl = String.valueOf(s.child("url_download").getValue());
                
                // Carregar informações do autor
                String category = String.valueOf(s.child("categoria").getValue());

                binding.name.setText(nome);
                binding.shortDesc.setText(shortDesc);
                binding.longDesc.setText(longDesc);

                // Load icon
                try {
                    Glide.with(binding.icon.getContext())
                        .load(icon)
                        .placeholder(R.drawable.sketch_app_icon)
                        .into(binding.icon);
                } catch (Throwable ignored) {}

                // Load statistics
                DataSnapshot stats = s.child("estatisticas");
                int downloads = 0;
                likeCount = 0;
                
                if (stats.exists()) {
                    Object d = stats.child("downloads").getValue();
                    Object l = stats.child("likes").getValue();
                    Object r = stats.child("rating").getValue();
                    Object tr = stats.child("total_ratings").getValue();
                    
                    try { downloads = d != null ? Integer.parseInt(String.valueOf(d)) : 0; } catch (Exception ignored) {}
                    try { likeCount = l != null ? Integer.parseInt(String.valueOf(l)) : 0; } catch (Exception ignored) {}
                    try { overallRating = r != null ? Double.parseDouble(String.valueOf(r)) : 4.8; } catch (Exception ignored) {}
                    try { totalRatings = tr != null ? Integer.parseInt(String.valueOf(tr)) : 4042969; } catch (Exception ignored) {}
                }

                binding.downloads.setText(downloads + " downloads");
                binding.likes.setText(likeCount + " likes");

                // Check if user liked this app
                if (currentUserId != null) {
                    isLiked = s.child("likes").child(currentUserId).exists();
                }
                updateLikeButton();

                // Load user's rating
                if (currentUserId != null) {
                    DataSnapshot userRatingSnapshot = s.child("avaliacoes").child(currentUserId);
                    if (userRatingSnapshot.exists()) {
                        Object rating = userRatingSnapshot.child("rating").getValue();
                        if (rating != null) {
                            userRating = Integer.parseInt(String.valueOf(rating));
                        }
                    }
                }
                updateStarDisplay();

                // Setup download button
                binding.btnDownload.setOnClickListener(v -> {
                    if (downloadUrl != null && !downloadUrl.isEmpty() && !downloadUrl.equals("null")) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                        startActivity(i);
                    } else {
                        Toast.makeText(StoreDetailActivity.this, "Link de download não disponível", Toast.LENGTH_SHORT).show();
                    }
                });

                // Load screenshots
                ArrayList<String> screenshots = new ArrayList<>();
                for (DataSnapshot sc : s.child("screenshots").getChildren()) {
                    String url = String.valueOf(sc.getValue());
                    if (url != null && !url.equals("null") && !url.isEmpty()) {
                        screenshots.add(url);
                    }
                }
                
                if (!screenshots.isEmpty()) {
                    screenshotsAdapter = new ScreenshotsAdapter(screenshots);
                    binding.screenshots.setAdapter(screenshotsAdapter);
                }

                // Load comments
                loadComments();
                
                // Carregar informações do autor
                // Carregar categoria
                if (category != null && !category.equals("null") && !category.isEmpty()) {
                    binding.category.setText(category);
                } else {
                    binding.category.setText("Outros");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoreDetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    private void loadComments() {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("apps")
            .child(appId).child("comentarios");
        
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.clear();
                userCommentId = null; // Reset user comment tracking
                
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    String commentId = commentSnapshot.getKey();
                    String userId = String.valueOf(commentSnapshot.child("usuario_id").getValue());
                    String commentText = String.valueOf(commentSnapshot.child("comentario").getValue());
                    Long timestamp = commentSnapshot.child("timestamp").getValue(Long.class);
                    Object rating = commentSnapshot.child("rating").getValue();
                    
                    if (timestamp == null) timestamp = System.currentTimeMillis();
                    
                    int userRating = 0;
                    if (rating != null) {
                        try {
                            userRating = Integer.parseInt(String.valueOf(rating));
                        } catch (Exception ignored) {}
                    }
                    
                    // Track user's own comment
                    if (currentUserId != null && currentUserId.equals(userId)) {
                        userCommentId = commentId;
                    }
                    
                    // Load user info
                    loadUserInfo(userId, commentId, commentText, timestamp, userRating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error silently
            }
        });
    }

    private void loadUserInfo(String userId, String commentId, String commentText, long timestamp, int rating) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String userName = String.valueOf(userSnapshot.child("nome").getValue());
                                    String userAvatar = String.valueOf(userSnapshot.child("foto_perfil").getValue());
                
                if (userName == null || userName.equals("null")) {
                    userName = "Usuário";
                }
                
                CommentsAdapter.CommentItem comment = new CommentsAdapter.CommentItem(
                    commentId, userId, userName, userAvatar, commentText, timestamp, rating
                );
                
                comments.add(comment);
                commentsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Add comment with default user info
                CommentsAdapter.CommentItem comment = new CommentsAdapter.CommentItem(
                    commentId, userId, "Usuário", null, commentText, timestamp, rating
                );
                comments.add(comment);
                commentsAdapter.notifyDataSetChanged();
            }
        });
    }
}



