package pro.sketchware.activities.store.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import pro.sketchware.R;
import pro.sketchware.databinding.ItemCommentBinding;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    
    public interface OnCommentEditListener {
        void onEditComment(String commentId, String currentText);
    }
    
    private final List<CommentItem> comments;
    private final String currentUserId;
    private final OnCommentEditListener editListener;
    
    public static class CommentItem {
        public String commentId;
        public String userId;
        public String userName;
        public String userAvatar;
        public String commentText;
        public long timestamp;
        public int rating;
        
        public CommentItem(String commentId, String userId, String userName, String userAvatar, 
                          String commentText, long timestamp, int rating) {
            this.commentId = commentId;
            this.userId = userId;
            this.userName = userName;
            this.userAvatar = userAvatar;
            this.commentText = commentText;
            this.timestamp = timestamp;
            this.rating = rating;
        }
    }
    
    public CommentsAdapter(List<CommentItem> comments, String currentUserId, OnCommentEditListener editListener) {
        this.comments = comments;
        this.currentUserId = currentUserId;
        this.editListener = editListener;
    }
    
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentItem comment = comments.get(position);
        
        holder.binding.userName.setText(comment.userName != null ? comment.userName : "Usuário");
        holder.binding.commentText.setText(comment.commentText);
        
        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
        String dateStr = sdf.format(new Date(comment.timestamp));
        holder.binding.commentDate.setText(dateStr);
        
        // Show rating stars
        showRatingStars(holder, comment.rating);
        
        // Load user avatar
        if (comment.userAvatar != null && !comment.userAvatar.isEmpty()) {
            try {
                Glide.with(holder.binding.userAvatar.getContext())
                    .load(comment.userAvatar)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.binding.userAvatar);
            } catch (Throwable ignored) {
                holder.binding.userAvatar.setImageResource(R.drawable.ic_profile);
            }
        } else {
            holder.binding.userAvatar.setImageResource(R.drawable.ic_profile);
        }
        
        // Show edit button only for own comments
        if (currentUserId != null && currentUserId.equals(comment.userId)) {
            holder.binding.btnEdit.setVisibility(View.VISIBLE);
            holder.binding.btnEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEditComment(comment.commentId, comment.commentText);
                }
            });
        } else {
            holder.binding.btnEdit.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }
    
    private void showRatingStars(CommentViewHolder holder, int rating) {
        // Show rating bar based on rating
        if (rating > 0) {
            holder.binding.ratingBar.setVisibility(View.VISIBLE);
            holder.binding.ratingBar.setRating(rating);
        } else {
            holder.binding.ratingBar.setVisibility(View.GONE);
        }
    }
    
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public final ItemCommentBinding binding;
        
        public CommentViewHolder(@NonNull ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
