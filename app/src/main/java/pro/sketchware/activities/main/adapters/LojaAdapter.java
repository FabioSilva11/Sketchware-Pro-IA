package pro.sketchware.activities.main.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.HashMap;

public class LojaAdapter extends RecyclerView.Adapter<TemplatesAdapter.TemplateViewHolder> {
    private final List<HashMap<String, Object>> items;

    public LojaAdapter(List<HashMap<String, Object>> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public TemplatesAdapter.TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TemplatesItemBinding binding = TemplatesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TemplatesAdapter.TemplateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplatesAdapter.TemplateViewHolder holder, int position) {
        // Apenas UI estática por enquanto
        holder.binding.imgIcon.setImageResource(pro.sketchware.R.drawable.sketch_app_icon);
        holder.binding.projectName.setText("Item Loja");
        holder.binding.appName.setText("by Sketchware");
        holder.binding.packageName.setText("Em breve");
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
}


