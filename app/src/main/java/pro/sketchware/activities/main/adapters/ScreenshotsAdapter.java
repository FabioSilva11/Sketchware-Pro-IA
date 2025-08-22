package pro.sketchware.activities.main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import pro.sketchware.R;
import pro.sketchware.databinding.ScreenshotCardItemBinding;

public class ScreenshotsAdapter extends RecyclerView.Adapter<ScreenshotsAdapter.ScreenshotViewHolder> {
    
    private final List<String> screenshots;
    private final Context context;
    
    public ScreenshotsAdapter(Context context, List<String> screenshots) {
        this.context = context;
        this.screenshots = screenshots;
    }
    
    @NonNull
    @Override
    public ScreenshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ScreenshotCardItemBinding binding = ScreenshotCardItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ScreenshotViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ScreenshotViewHolder holder, int position) {
        String screenshotUrl = screenshots.get(position);
        
        // Carregar imagem com Glide
        Glide.with(context)
                .load(screenshotUrl)
                .placeholder(R.drawable.sketch_app_icon)
                .error(R.drawable.sketch_app_icon)
                .into(holder.binding.screenshotImage);
    }
    
    @Override
    public int getItemCount() {
        return screenshots != null ? screenshots.size() : 0;
    }
    
    static class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        final ScreenshotCardItemBinding binding;
        
        ScreenshotViewHolder(ScreenshotCardItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
