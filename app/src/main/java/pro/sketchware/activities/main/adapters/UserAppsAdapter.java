package pro.sketchware.activities.main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.List;

import pro.sketchware.R;
import pro.sketchware.activities.main.fragments.loja.AppItem;

public class UserAppsAdapter extends RecyclerView.Adapter<UserAppsAdapter.UserAppViewHolder> {
    
    private final List<AppItem> userApps;
    private final OnUserAppClickListener listener;
    
    public interface OnUserAppClickListener {
        void onAppClick(AppItem app);
        void onEditApp(AppItem app);
        void onDeleteApp(AppItem app);
        void onPublishApp(AppItem app);
    }
    
    public UserAppsAdapter(List<AppItem> userApps, OnUserAppClickListener listener) {
        this.userApps = userApps;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public UserAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_app, parent, false);
        return new UserAppViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserAppViewHolder holder, int position) {
        AppItem app = userApps.get(position);
        holder.bind(app);
    }
    
    @Override
    public int getItemCount() {
        return userApps.size();
    }
    
    public void updateApps(List<AppItem> newApps) {
        userApps.clear();
        userApps.addAll(newApps);
        notifyDataSetChanged();
    }
    
    class UserAppViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView ivAppIcon;
        private final TextView tvAppName;
        private final TextView tvAppDescription;
        private final TextView tvAppStatus;
        private final TextView tvDownloads;
        private final TextView tvRating;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;
        private final MaterialButton btnPublish;
        
        public UserAppViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_app);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvAppDescription = itemView.findViewById(R.id.tv_app_description);
            tvAppStatus = itemView.findViewById(R.id.tv_app_status);
            tvDownloads = itemView.findViewById(R.id.tv_downloads);
            tvRating = itemView.findViewById(R.id.tv_rating);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnPublish = itemView.findViewById(R.id.btn_publish);
        }
        
        public void bind(AppItem app) {
            // Carregar ícone do app
            if (app.getIcone() != null && !app.getIcone().isEmpty()) {
                Picasso.get()
                    .load(app.getIcone())
                    .placeholder(R.drawable.sketch_app_icon)
                    .error(R.drawable.sketch_app_icon)
                    .fit()
                    .centerCrop()
                    .into(ivAppIcon);
            } else {
                ivAppIcon.setImageResource(R.drawable.sketch_app_icon);
            }
            
            // Informações do app
            tvAppName.setText(app.getNome());
            tvAppDescription.setText(app.getDescricaoCurta());
            
            // Status do app
            String status = app.getStatus() != null ? app.getStatus() : "Rascunho";
            tvAppStatus.setText(status);
            
            // Configurar cor do status
            if ("Publicado".equals(status)) {
                tvAppStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                tvAppStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            }
            
            // Downloads e avaliação
            tvDownloads.setText(formatNumber(app.getDownloads()) + " downloads");
            tvRating.setText(String.format("%.1f", app.getAvaliacao_media()) + " ★");
            
            // Configurar visibilidade dos botões baseado no status
            if ("Publicado".equals(status)) {
                btnPublish.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                btnPublish.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
            }
            
            // Listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppClick(app);
                }
            });
            
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditApp(app);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteApp(app);
                }
            });
            
            btnPublish.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPublishApp(app);
                }
            });
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
    }
}
