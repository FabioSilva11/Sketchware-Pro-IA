package pro.sketchware.activities.iconcreator.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    private final Matrix imageMatrixInternal = new Matrix();
    private final float[] matrixValues = new float[9];
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;

    private float minScale = 1f;
    private float maxScale = 5f;

    private float lastX;
    private float lastY;
    private boolean isDragging;

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                float currentScale = getScale();
                float newScale = currentScale * scaleFactor;
                if (newScale < minScale) scaleFactor = minScale / currentScale;
                if (newScale > maxScale) scaleFactor = maxScale / currentScale;

                imageMatrixInternal.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                setImageMatrix(imageMatrixInternal);
                fixTranslation();
                return true;
            }
        });

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                imageMatrixInternal.postTranslate(-distanceX, -distanceY);
                setImageMatrix(imageMatrixInternal);
                fixTranslation();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float target = getScale() < (minScale * 1.8f) ? Math.min(maxScale, minScale * 2f) : minScale;
                float factor = target / getScale();
                imageMatrixInternal.postScale(factor, factor, e.getX(), e.getY());
                setImageMatrix(imageMatrixInternal);
                fixTranslation();
                return true;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fitToScreen();
    }

    public void fitToScreen() {
        Drawable d = getDrawable();
        if (d == null) return;
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        int vw = getWidth();
        int vh = getHeight();
        if (vw == 0 || vh == 0 || dw <= 0 || dh <= 0) return;

        imageMatrixInternal.reset();
        float scale = Math.min((float) vw / dw, (float) vh / dh);
        minScale = scale;

        float dx = (vw - dw * scale) * 0.5f;
        float dy = (vh - dh * scale) * 0.5f;
        imageMatrixInternal.postScale(scale, scale);
        imageMatrixInternal.postTranslate(dx, dy);
        setImageMatrix(imageMatrixInternal);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean scaleHandled = scaleDetector.onTouchEvent(event);
        boolean gestureHandled = gestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                lastX = event.getX();
                lastY = event.getY();
                isDragging = true;
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            case MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress() && isDragging) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    imageMatrixInternal.postTranslate(dx, dy);
                    setImageMatrix(imageMatrixInternal);
                    fixTranslation();
                    lastX = event.getX();
                    lastY = event.getY();
                }
            }
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
            }
        }

        return scaleHandled || gestureHandled || super.onTouchEvent(event);
    }

    private void fixTranslation() {
        RectF bounds = getImageDisplayRect();
        if (bounds == null) return;
        float dx = 0, dy = 0;

        if (bounds.width() < getWidth()) {
            dx = getWidth() / 2f - bounds.centerX();
        } else {
            if (bounds.left > 0) dx = -bounds.left;
            if (bounds.right < getWidth()) dx = getWidth() - bounds.right;
        }
        if (bounds.height() < getHeight()) {
            dy = getHeight() / 2f - bounds.centerY();
        } else {
            if (bounds.top > 0) dy = -bounds.top;
            if (bounds.bottom < getHeight()) dy = getHeight() - bounds.bottom;
        }
        imageMatrixInternal.postTranslate(dx, dy);
        setImageMatrix(imageMatrixInternal);
    }

    public RectF getImageDisplayRect() {
        Drawable d = getDrawable();
        if (d == null) return null;
        RectF rect = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        Matrix m = new Matrix(imageMatrixInternal);
        m.mapRect(rect);
        return rect;
    }

    public float getScale() {
        imageMatrixInternal.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    public Matrix getImageMatrixCopy() {
        return new Matrix(imageMatrixInternal);
    }
}


