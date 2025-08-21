package pro.sketchware.activities.store.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import pro.sketchware.R;
import pro.sketchware.databinding.ItemScreenshotBinding;

public class ScreenshotsAdapter extends RecyclerView.Adapter<ScreenshotsAdapter.ScreenshotViewHolder> {
    
    private final List<String> screenshots;
    
    public ScreenshotsAdapter(List<String> screenshots) {
        this.screenshots = screenshots;
    }
    
    @NonNull
    @Override
    public ScreenshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScreenshotBinding binding = ItemScreenshotBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ScreenshotViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ScreenshotViewHolder holder, int position) {
        String screenshotUrl = screenshots.get(position);
        
        try {
            Glide.with(holder.binding.screenshot.getContext())
                .load(screenshotUrl)
                .placeholder(R.drawable.sketch_app_icon)
                .into(holder.binding.screenshot);
        } catch (Throwable ignored) {}
    }
    
    @Override
    public int getItemCount() {
        return screenshots != null ? screenshots.size() : 0;
    }
    
    public static class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        public final ItemScreenshotBinding binding;
        
        public ScreenshotViewHolder(@NonNull ItemScreenshotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
