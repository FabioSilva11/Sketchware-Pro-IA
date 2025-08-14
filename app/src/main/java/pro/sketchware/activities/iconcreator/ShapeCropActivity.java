package pro.sketchware.activities.iconcreator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import pro.sketchware.R;

public class ShapeCropActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_SHAPE = "shape";

    public static final int SHAPE_RECT = 0;
    public static final int SHAPE_CIRCLE = 1;
    public static final int SHAPE_OVAL = 2;
    public static final int SHAPE_SQUIRCLE_LIGHT = 3;
    public static final int SHAPE_SQUIRCLE_DARK = 4;

    private pro.sketchware.activities.iconcreator.view.ZoomableImageView imageView;
    private pro.sketchware.activities.iconcreator.view.CropOverlayView overlayView;
    private Bitmap sourceBitmap;
    private int currentShape = SHAPE_RECT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape_crop);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        imageView = findViewById(R.id.image);
        overlayView = findViewById(R.id.overlay);

        Uri imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        currentShape = getIntent().getIntExtra(EXTRA_SHAPE, SHAPE_RECT);
        overlayView.setShape(currentShape);

        if (imageUri != null) {
            try (InputStream is = getContentResolver().openInputStream(imageUri)) {
                Bitmap src = BitmapFactory.decodeStream(is);
                sourceBitmap = src;
                imageView.setImageBitmap(src);
                imageView.post(imageView::fitToScreen);
            } catch (Exception ignored) {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shape_crop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            onSave();
            return true;
        } else if (id == R.id.shape_rect) {
            currentShape = SHAPE_RECT;
            overlayView.setShape(currentShape);
            return true;
        } else if (id == R.id.shape_circle) {
            currentShape = SHAPE_CIRCLE;
            overlayView.setShape(currentShape);
            return true;
        } else if (id == R.id.shape_oval) {
            currentShape = SHAPE_OVAL;
            overlayView.setShape(currentShape);
            return true;
        } else if (id == R.id.shape_squircle_light) {
            currentShape = SHAPE_SQUIRCLE_LIGHT;
            overlayView.setShape(currentShape);
            return true;
        } else if (id == R.id.shape_squircle_dark) {
            currentShape = SHAPE_SQUIRCLE_DARK;
            overlayView.setShape(currentShape);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSave() {
        if (sourceBitmap == null) {
            finish();
            return;
        }

        RectF viewRect = overlayView.getCropRect();
        if (viewRect.width() <= 0 || viewRect.height() <= 0) {
            finish();
            return;
        }

        // Map overlay rect in view coords to bitmap coords (ImageView FIT_CENTER)
        // Map overlay rect from view space to bitmap space using inverse of the image matrix
        android.graphics.Matrix matrix = imageView.getImageMatrixCopy();
        android.graphics.Matrix inverse = new android.graphics.Matrix();
        matrix.invert(inverse);
        float[] pts = new float[]{viewRect.left, viewRect.top, viewRect.right, viewRect.bottom};
        inverse.mapPoints(pts);
        float left = Math.min(pts[0], pts[2]);
        float top = Math.min(pts[1], pts[3]);
        float right = Math.max(pts[0], pts[2]);
        float bottom = Math.max(pts[1], pts[3]);

        int bw = sourceBitmap.getWidth();
        int bh = sourceBitmap.getHeight();
        int il = Math.max(0, Math.round(left));
        int it = Math.max(0, Math.round(top));
        int ir = Math.min(bw, Math.round(right));
        int ib = Math.min(bh, Math.round(bottom));

        int cw = Math.max(1, ir - il);
        int ch = Math.max(1, ib - it);

        Bitmap cropped = Bitmap.createBitmap(sourceBitmap, il, it, cw, ch);
        Bitmap output = applyMaskIfNeeded(cropped, currentShape);

        try {
            File out = new File(getCacheDir(), "shape_cropped_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                output.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            Intent result = new Intent();
            result.setData(Uri.fromFile(out));
            setResult(RESULT_OK, result);
        } catch (Exception ignored) {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private Bitmap applyMaskIfNeeded(Bitmap src, int shape) {
        if (shape == SHAPE_RECT) return src;
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF r = new RectF(0, 0, w, h);

        switch (shape) {
            case SHAPE_CIRCLE -> {
                float s = Math.min(w, h) / 2f;
                canvas.drawCircle(w / 2f, h / 2f, s, paint);
            }
            case SHAPE_OVAL -> canvas.drawOval(r, paint);
            case SHAPE_SQUIRCLE_LIGHT -> canvas.drawRoundRect(r, Math.min(w, h) * 0.4f, Math.min(w, h) * 0.4f, paint);
            case SHAPE_SQUIRCLE_DARK -> canvas.drawRoundRect(r, Math.min(w, h) * 0.25f, Math.min(w, h) * 0.25f, paint);
        }
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);
        return out;
    }
}


