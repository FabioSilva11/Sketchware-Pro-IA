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
import pro.sketchware.activities.main.activities.DetalhesActivity;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ComentarioViewHolder> {
    
    private final List<ComentarioItem> comentarios;
    
    public ComentariosAdapter(Map<String, DetalhesActivity.Comentario> comentariosMap) {
        this.comentarios = new ArrayList<>();
        
        if (comentariosMap != null) {
            for (Map.Entry<String, DetalhesActivity.Comentario> entry : comentariosMap.entrySet()) {
                DetalhesActivity.Comentario comentario = entry.getValue();
                this.comentarios.add(new ComentarioItem(
                        comentario.usuario_id,
                        comentario.comentario,
                        comentario.timestamp,
                        comentario.rating
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
        
        holder.usuarioId.setText(comentario.usuarioId);
        holder.comentario.setText(comentario.comentario);
        
        // Configurar rating
        if (comentario.rating > 0) {
            holder.ratingBar.setRating((float) comentario.rating);
            holder.ratingBar.setVisibility(View.VISIBLE);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
        
        // Formatar timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
        String dataFormatada = sdf.format(new Date(comentario.timestamp));
        holder.timestamp.setText(dataFormatada);
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
        final String comentario;
        final long timestamp;
        final double rating;
        
        ComentarioItem(String usuarioId, String comentario, long timestamp, double rating) {
            this.usuarioId = usuarioId;
            this.comentario = comentario;
            this.timestamp = timestamp;
            this.rating = rating;
        }
    }
}
