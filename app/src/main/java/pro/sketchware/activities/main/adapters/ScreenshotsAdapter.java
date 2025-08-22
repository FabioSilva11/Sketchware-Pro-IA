package pro.sketchware.activities.main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import pro.sketchware.R;

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
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 0, 8, 0);
        
        return new ScreenshotViewHolder(imageView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ScreenshotViewHolder holder, int position) {
        String screenshotUrl = screenshots.get(position);
        
        // Usar Glide para carregar a imagem
        Glide.with(context)
                .load(screenshotUrl)
                .placeholder(R.drawable.sketch_app_icon)
                .error(R.drawable.sketch_app_icon)
                .into(holder.imageView);
    }
    
    @Override
    public int getItemCount() {
        return screenshots != null ? screenshots.size() : 0;
    }
    
    static class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        
        ScreenshotViewHolder(ImageView imageView) {
            super(imageView);
            this.imageView = imageView;
        }
    }
}
