package pro.sketchware.activities.main.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pro.sketchware.R;

public class LojaAdapter extends RecyclerView.Adapter<LojaAdapter.LojaViewHolder> {
    public static class AppItem {
        public String appId;
        public String nome;
        public String categoria;
        public String autor;
        public double rating;
        public String iconeBase64;
    }

    private final List<AppItem> items = new ArrayList<>();

    public void submitList(List<AppItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LojaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false);
        return new LojaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LojaViewHolder holder, int position) {
        AppItem item = items.get(position);
        holder.appName.setText(item.nome != null ? item.nome : "");
        holder.appCategoria.setText(item.categoria != null ? item.categoria : "");
        holder.appAutor.setText(item.autor != null ? item.autor : "");
        holder.appRating.setText(String.valueOf(item.rating));

        if (item.iconeBase64 != null && item.iconeBase64.startsWith("data:image")) {
            try {
                String base64 = item.iconeBase64.substring(item.iconeBase64.indexOf(",") + 1);
                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                holder.appIcon.setImageBitmap(bitmap);
            } catch (Exception ignored) {
                holder.appIcon.setImageResource(R.drawable.ic_mtrl_store);
            }
        } else {
            holder.appIcon.setImageResource(R.drawable.ic_mtrl_store);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LojaViewHolder extends RecyclerView.ViewHolder {
        final ImageView appIcon;
        final TextView appName;
        final TextView appCategoria;
        final TextView appRating;
        final ImageView star;
        final TextView appAutor;

        LojaViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            appCategoria = itemView.findViewById(R.id.app_categoria);
            appRating = itemView.findViewById(R.id.app_rating);
            star = itemView.findViewById(R.id.imageview3);
            appAutor = itemView.findViewById(R.id.app_autor);
        }
    }
}


