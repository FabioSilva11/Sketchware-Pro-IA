package pro.sketchware.activities.main.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pro.sketchware.R;

public class ScreenshotsAdapter extends RecyclerView.Adapter<ScreenshotsAdapter.ScreenshotViewHolder> {

    private final List<String> screenshots;
    private final OnScreenshotClickListener listener;

    public interface OnScreenshotClickListener {
        void onScreenshotClick(int position);
        void onScreenshotDelete(int position);
    }

    // Construtor para List<String> (compatibilidade)
    public ScreenshotsAdapter(List<String> screenshots, OnScreenshotClickListener listener) {
        this.screenshots = screenshots;
        this.listener = listener;
    }

    // Construtor para Map<String, String> (novo formato)
    public ScreenshotsAdapter(Map<String, String> screenshotsMap, OnScreenshotClickListener listener) {
        this.screenshots = new ArrayList<>();
        this.listener = listener;
        
        if (screenshotsMap != null) {
            // Converter Map para List mantendo a ordem
            for (int i = 1; i <= screenshotsMap.size(); i++) {
                String key = "s" + i;
                String screenshot = screenshotsMap.get(key);
                if (screenshot != null) {
                    this.screenshots.add(screenshot);
                }
            }
        }
    }

    @NonNull
    @Override
    public ScreenshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.screenshot_card_item, parent, false);
        return new ScreenshotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreenshotViewHolder holder, int position) {
        String screenshotUrl = screenshots.get(position);
        
        if (screenshotUrl != null && !screenshotUrl.isEmpty()) {
            // Carregar imagem da URL
            loadImageFromUrl(screenshotUrl, holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.sketch_app_icon);
        }

        holder.imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onScreenshotClick(position);
            }
        });

    }

    private void loadImageFromUrl(String imageUrl, ImageView imageView) {
        // Implementação simples para carregar imagem da URL
        // Em produção, use Glide ou Picasso
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                imageView.post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
                imageView.post(() -> imageView.setImageResource(R.drawable.sketch_app_icon));
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return screenshots.size();
    }

    static class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        ScreenshotViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.screenshot_image);
        }
    }
}
