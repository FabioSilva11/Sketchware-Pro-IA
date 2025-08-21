package pro.sketchware.activities.main.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import pro.sketchware.activities.store.StoreDetailActivity;
import pro.sketchware.databinding.ItemLojaAppBinding;

public class LojaAdapter extends RecyclerView.Adapter<LojaAdapter.AppViewHolder> {
    public interface OnItemClickListener {
        void onClick(HashMap<String, Object> app);
    }

    private final List<HashMap<String, Object>> items;

    private final OnItemClickListener onItemClickListener;

    public LojaAdapter(List<HashMap<String, Object>> items, OnItemClickListener onItemClickListener) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLojaAppBinding binding = ItemLojaAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        HashMap<String, Object> app = items.get(position);
        Object name = app.get("nome");
        Object shortDesc = app.get("descricao_curta");
        Object icon = app.get("icone");
        Object categoria = app.get("categoria");
        Object subcategoria = app.get("subcategoria");
        
        // Calculate rating and size
        double rating = 0.0; // Default rating (no rating yet)
        String size = "169 MB"; // Default size
        
        // Get statistics for rating calculation
        Object estatisticas = app.get("estatisticas");
        if (estatisticas instanceof java.util.Map) {
            Object ratingValue = ((java.util.Map<?, ?>) estatisticas).get("rating");
            if (ratingValue != null) {
                try {
                    rating = Double.parseDouble(String.valueOf(ratingValue));
                } catch (Exception ignored) {
                    rating = 0.0;
                }
            }
        }

        holder.binding.appName.setText(name != null ? String.valueOf(name) : "");
        
        // Short Description
        holder.binding.appShortDesc.setText(shortDesc != null ? String.valueOf(shortDesc) : "");
        
        // Categories
        String categoriesText = "";
        if (categoria != null && !String.valueOf(categoria).equals("null")) {
            categoriesText += String.valueOf(categoria);
        }
        if (subcategoria != null && !String.valueOf(subcategoria).equals("null")) {
            if (!categoriesText.isEmpty()) {
                categoriesText += " • ";
            }
            categoriesText += String.valueOf(subcategoria);
        }
        
        // If no categories are available, show "Outros"
        if (categoriesText.isEmpty()) {
            categoriesText = "Outros";
        }
        
        holder.binding.appCategories.setText(categoriesText);
        
        // Rating
        if (rating > 0) {
            holder.binding.appRating.setText(String.format("%.1f", rating));
            holder.binding.starIcon.setVisibility(View.VISIBLE);
        } else {
            holder.binding.appRating.setText("Sem avaliação");
            holder.binding.starIcon.setVisibility(View.GONE);
        }
        
        // Size (placeholder for now)
        holder.binding.appSize.setText(size);
        
        // Load icon
        if (icon != null) {
            try {
                com.bumptech.glide.Glide.with(holder.binding.appIcon.getContext())
                        .load(String.valueOf(icon))
                        .placeholder(pro.sketchware.R.drawable.sketch_app_icon)
                        .into(holder.binding.appIcon);
            } catch (Throwable ignored) {
                holder.binding.appIcon.setImageResource(pro.sketchware.R.drawable.sketch_app_icon);
            }
        } else {
            holder.binding.appIcon.setImageResource(pro.sketchware.R.drawable.sketch_app_icon);
        }

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) onItemClickListener.onClick(app);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }



    public static class AppViewHolder extends RecyclerView.ViewHolder {
        public final ItemLojaAppBinding binding;
        public AppViewHolder(@NonNull ItemLojaAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}


