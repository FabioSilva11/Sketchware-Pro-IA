package pro.sketchware.activities.main.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import pro.sketchware.activities.main.fragments.loja.AppItem;
import pro.sketchware.activities.main.fragments.loja.UserInfoManager;
import pro.sketchware.activities.main.fragments.loja.DatabaseManager;
import pro.sketchware.R;
import pro.sketchware.databinding.ItensLojaBinding;

public class LojaAdapter extends RecyclerView.Adapter<LojaAdapter.LojaViewHolder> {
    private final List<AppItem> apps;
    private OnAppClickListener listener;
    private final UserInfoManager userInfoManager;
    private final DatabaseManager databaseManager;

    public interface OnAppClickListener {
        void onAppClick(AppItem app);
        void onDownloadClick(AppItem app);
    }

    public LojaAdapter(List<AppItem> apps) {
        this.apps = apps;
        this.userInfoManager = UserInfoManager.getInstance();
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void setOnAppClickListener(OnAppClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LojaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItensLojaBinding binding = ItensLojaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LojaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LojaViewHolder holder, int position) {
        AppItem app = apps.get(position);
        
        // Configurar nome do app
        holder.binding.appName.setText(app.getNome());
        
        // Configurar categoria usando o novo método
        holder.binding.appCategoria.setText(app.getCategoryDisplay());
        
        // Configurar autor - tentar buscar nome real do usuário se disponível
        setupDeveloperName(holder, app);
        
        // Configurar ícone com suporte a URLs e base64
        setupAppIcon(holder, app);

        // Configurar click no item
        holder.binding.linear1.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppClick(app);
            }
        });

        // Configurar click no ícone para download
        holder.binding.appIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDownloadClick(app);
            }
        });
    }
    
    private void setupDeveloperName(LojaViewHolder holder, AppItem app) {
        // Usar o nome do desenvolvedor do novo formato primeiro
        String developerName = app.getDeveloperName();
        holder.binding.appAutor.setText(developerName);
        
        // Se temos um ID de desenvolvedor, tentar buscar informações mais completas do cache
        String developerId = app.getDeveloperId();
        if (developerId != null && !developerId.trim().isEmpty()) {
            // Tentar buscar do cache de usuários
            pro.sketchware.activities.main.fragments.loja.Usuario usuario = databaseManager.getUsuarioFromCache(developerId);
            if (usuario != null) {
                holder.binding.appAutor.setText(usuario.getDisplayName());
            } else {
                // Fallback para UserInfoManager se não estiver no cache
                if (!developerId.contains(" ") && !developerId.contains("Desenvolvedor")) {
                    userInfoManager.getUserName(developerId, new UserInfoManager.UserInfoCallback() {
                        @Override
                        public void onUserInfoReceived(String userId, String userName) {
                            holder.binding.appAutor.post(() -> {
                                holder.binding.appAutor.setText(userName);
                            });
                        }
                        
                        @Override
                        public void onError(String userId, String error) {
                            // Manter o nome atual em caso de erro
                        }
                    });
                }
            }
        }
    }

    private void setupAppIcon(LojaViewHolder holder, AppItem app) {
        String iconUrl = app.getIcone();
        
        if (iconUrl != null && !iconUrl.trim().isEmpty()) {
            if (iconUrl.startsWith("data:image")) {
                // Processar imagem base64
                try {
                    String base64Data = iconUrl.substring(iconUrl.indexOf(",") + 1);
                    byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    
                    if (bitmap != null) {
                        holder.binding.appIcon.setImageBitmap(bitmap);
                    } else {
                        holder.binding.appIcon.setImageResource(R.drawable.sketch_app_icon);
                    }
                } catch (Exception e) {
                    holder.binding.appIcon.setImageResource(R.drawable.sketch_app_icon);
                }
            } else if (iconUrl.startsWith("http")) {
                // Usar Picasso para carregar imagens via URL
                Picasso.get()
                    .load(iconUrl)
                    .placeholder(R.drawable.sketch_app_icon)
                    .error(R.drawable.sketch_app_icon)
                    .fit()
                    .centerCrop()
                    .into(holder.binding.appIcon);
            } else {
                // Formato desconhecido, usar ícone padrão
                holder.binding.appIcon.setImageResource(R.drawable.sketch_app_icon);
            }
        } else {
            // Sem ícone definido, usar ícone padrão
            holder.binding.appIcon.setImageResource(R.drawable.sketch_app_icon);
        }
    }

    @Override
    public int getItemCount() {
        return apps != null ? apps.size() : 0;
    }

    static class LojaViewHolder extends RecyclerView.ViewHolder {
        final ItensLojaBinding binding;

        LojaViewHolder(ItensLojaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}


