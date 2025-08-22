package pro.sketchware.activities.main.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pro.sketchware.activities.main.fragments.loja.AppItem;
import pro.sketchware.R;
import pro.sketchware.databinding.ItensLojaBinding;

public class LojaAdapter extends RecyclerView.Adapter<LojaAdapter.LojaViewHolder> {
    private final List<AppItem> apps;
    private OnAppClickListener listener;

    public interface OnAppClickListener {
        void onAppClick(AppItem app);
        void onDownloadClick(AppItem app);
    }

    public LojaAdapter(List<AppItem> apps) {
        this.apps = apps;
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
        
        // Configurar categoria (usar descrição curta como categoria)
        String categoria = app.getDescricaoCurta() != null ? app.getDescricaoCurta() : "Categoria não disponível";
        holder.binding.appCategoria.setText(categoria);
        

        
        // Configurar autor (usar publisher se disponível)
        if (app.getPublisher() != null && app.getPublisher().getUsuarioId() != null) {
            holder.binding.appAutor.setText(app.getPublisher().getUsuarioId());
        } else {
            holder.binding.appAutor.setText("Autor desconhecido");
        }
        
        // Configurar ícone se disponível
        if (app.getIcone() != null && app.getIcone().startsWith("data:image")) {
            try {
                // Extrair base64 da string data:image
                String base64Data = app.getIcone().substring(app.getIcone().indexOf(",") + 1);
                byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                
                if (bitmap != null) {
                    holder.binding.appIcon.setImageBitmap(bitmap);
                } else {
                    // Fallback para ícone padrão
                    holder.binding.appIcon.setImageResource(R.drawable.sketch_app_icon);
                }
            } catch (Exception e) {
                // Em caso de erro, usar ícone padrão
                holder.binding.appIcon.setImageResource(R.drawable.sketch_app_icon);
            }
        } else {
            // Usar ícone padrão se não houver ícone
            holder.binding.appIcon.setImageResource(R.drawable.sketch_app_icon);
        }

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


