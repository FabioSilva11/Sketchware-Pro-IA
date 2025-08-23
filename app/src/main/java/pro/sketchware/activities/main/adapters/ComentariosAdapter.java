package pro.sketchware.activities.main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import pro.sketchware.R;
import pro.sketchware.activities.main.fragments.loja.AppItem;
import pro.sketchware.activities.main.fragments.loja.Comentario;
import pro.sketchware.activities.main.fragments.loja.DatabaseManager;
import pro.sketchware.activities.main.fragments.loja.Usuario;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ComentarioViewHolder> {
    
    private final List<ComentarioItem> comentarios;
    private final DatabaseManager databaseManager;
    
    // Construtor para o formato Map (compatibilidade)
    public ComentariosAdapter(Map<String, Comentario> comentariosMap) {
        this.comentarios = new ArrayList<>();
        this.databaseManager = DatabaseManager.getInstance();
        
        if (comentariosMap != null) {
            for (Map.Entry<String, Comentario> entry : comentariosMap.entrySet()) {
                Comentario comentario = entry.getValue();
                this.comentarios.add(new ComentarioItem(
                        comentario.getIdAutor(),
                        comentario.getNomeAutor(),
                        comentario.getIconeUsuario(),
                        comentario.getComentario(),
                        0, // timestamp não disponível no novo formato
                        comentario.getEstrelas()
                ));
            }
        }
    }
    
    // Construtor para o novo formato (top-level comentarios)
    public ComentariosAdapter(List<Comentario> comentariosList) {
        this.comentarios = new ArrayList<>();
        this.databaseManager = DatabaseManager.getInstance();
        
        if (comentariosList != null) {
            for (Comentario comentario : comentariosList) {
                this.comentarios.add(new ComentarioItem(
                        comentario.getIdAutor(),
                        comentario.getNomeAutor(),
                        comentario.getIconeUsuario(),
                        comentario.getComentario(),
                        0, // timestamp não disponível no novo formato
                        comentario.getEstrelas()
                ));
            }
        }
    }
    
    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comentario_item, parent, false);
        return new ComentarioViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        ComentarioItem comentario = comentarios.get(position);
        
        // Configurar nome do usuário
        setupUserName(holder, comentario);
        
        // Configurar comentário
        holder.comentario.setText(comentario.comentario);
        
        // Configurar rating
        if (comentario.rating > 0) {
            holder.ratingBar.setRating((float) comentario.rating);
            holder.ratingBar.setVisibility(View.VISIBLE);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
        
        // Formatar timestamp (se disponível)
        if (comentario.timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
            String dataFormatada = sdf.format(new Date(comentario.timestamp));
            holder.timestamp.setText(dataFormatada);
        } else {
            holder.timestamp.setText("Data não disponível");
        }
    }
    
    private void setupUserName(ComentarioViewHolder holder, ComentarioItem comentario) {
        // Usar nome do autor se disponível (novo formato)
        if (comentario.nomeAutor != null && !comentario.nomeAutor.trim().isEmpty()) {
            holder.usuarioId.setText(comentario.nomeAutor);
        } else if (comentario.usuarioId != null && !comentario.usuarioId.trim().isEmpty()) {
            // Tentar buscar nome do usuário no cache
            Usuario usuario = databaseManager.getUsuarioFromCache(comentario.usuarioId);
            if (usuario != null) {
                holder.usuarioId.setText(usuario.getDisplayName());
            } else {
                // Usar ID do usuário como fallback
                holder.usuarioId.setText(comentario.usuarioId);
            }
        } else {
            holder.usuarioId.setText("Usuário Anônimo");
        }
    }
    
    @Override
    public int getItemCount() {
        return comentarios.size();
    }
    
    static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        final TextView usuarioId;
        final TextView comentario;
        final TextView timestamp;
        final MaterialRatingBar ratingBar;
        
        ComentarioViewHolder(View itemView) {
            super(itemView);
            usuarioId = itemView.findViewById(R.id.user_name);
            comentario = itemView.findViewById(R.id.comentario_description);
            timestamp = itemView.findViewById(R.id.data_publish);
            ratingBar = itemView.findViewById(R.id.ratingbar1);
        }
    }
    
    private static class ComentarioItem {
        final String usuarioId;
        final String nomeAutor;
        final String iconeUsuario;
        final String comentario;
        final long timestamp;
        final double rating;
        
        ComentarioItem(String usuarioId, String nomeAutor, String iconeUsuario, String comentario, long timestamp, double rating) {
            this.usuarioId = usuarioId;
            this.nomeAutor = nomeAutor;
            this.iconeUsuario = iconeUsuario;
            this.comentario = comentario;
            this.timestamp = timestamp;
            this.rating = rating;
        }
    }
}
