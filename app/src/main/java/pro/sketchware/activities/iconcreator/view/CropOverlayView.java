package pro.sketchware.activities.iconcreator.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropOverlayView extends View {

    private final Paint dimPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final RectF cropRect = new RectF();
    private int shape = 0;
    private static final float HANDLE_SIZE = 48f;
    private static final float MIN_SIZE = 120f;

    private enum DragMode { NONE, RESIZE_LEFT, RESIZE_TOP, RESIZE_RIGHT, RESIZE_BOTTOM, RESIZE_LT, RESIZE_RT, RESIZE_LB, RESIZE_RB }
    private DragMode dragMode = DragMode.NONE;
    private float lastX, lastY;

    public CropOverlayView(Context context) {
        super(context);
        init();
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        dimPaint.setColor(Color.parseColor("#66000000"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(4f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(Color.WHITE);
        gridPaint.setAlpha(160);
        gridPaint.setStrokeWidth(2f);
    }

    public void setShape(int shape) {
        this.shape = shape;
        invalidate();
    }

    public RectF getCropRect() {
        return new RectF(cropRect);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float pad = 48f;
        float size = Math.min(w, h) - pad * 2;
        float left = (w - size) / 2f;
        float top = (h - size) / 2f;
        cropRect.set(left, top, left + size, top + size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Dim layer
        canvas.drawColor(dimPaint.getColor());

        // Clear inside crop shape by drawing path with DST_OUT
        int save = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        canvas.drawColor(dimPaint.getColor());
        Paint clear = new Paint();
        clear.setAntiAlias(true);
        clear.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
        Path path = shapePath();
        canvas.drawPath(path, clear);
        canvas.restoreToCount(save);

        // Border
        canvas.drawPath(shapePath(), borderPaint);

        // Grid (rule of thirds)
        float l = cropRect.left;
        float t = cropRect.top;
        float r = cropRect.right;
        float b = cropRect.bottom;
        float hStep = (r - l) / 3f;
        float vStep = (b - t) / 3f;
        for (int i = 1; i < 3; i++) {
            canvas.drawLine(l + hStep * i, t, l + hStep * i, b, gridPaint);
            canvas.drawLine(l, t + vStep * i, r, t + vStep * i, gridPaint);
        }
    }

    private Path shapePath() {
        Path p = new Path();
        if (shape == 1) {
            float s = Math.min(cropRect.width(), cropRect.height()) / 2f;
            p.addCircle(cropRect.centerX(), cropRect.centerY(), s, Path.Direction.CW);
        } else if (shape == 2) {
            p.addOval(cropRect, Path.Direction.CW);
        } else if (shape == 3) {
            float r = Math.min(cropRect.width(), cropRect.height()) * 0.4f;
            p.addRoundRect(cropRect, r, r, Path.Direction.CW);
        } else if (shape == 4) {
            float r = Math.min(cropRect.width(), cropRect.height()) * 0.25f;
            p.addRoundRect(cropRect, r, r, Path.Direction.CW);
        } else {
            p.addRect(cropRect, Path.Direction.CW);
        }
        return p;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                dragMode = detectDragMode(x, y);
                lastX = x;
                lastY = y;
                // Only consume if user started on a resize handle/edge
                return dragMode != DragMode.NONE;
            }
            case MotionEvent.ACTION_MOVE -> {
                float dx = x - lastX;
                float dy = y - lastY;
                if (dragMode != DragMode.NONE) {
                    applyDrag(dx, dy);
                    lastX = x;
                    lastY = y;
                    invalidate();
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragMode = DragMode.NONE;
                return false;
            }
        }
        return false;
    }

    private DragMode detectDragMode(float x, float y) {
        if (near(x, cropRect.left) && near(y, cropRect.top)) return DragMode.RESIZE_LT;
        if (near(x, cropRect.right) && near(y, cropRect.top)) return DragMode.RESIZE_RT;
        if (near(x, cropRect.left) && near(y, cropRect.bottom)) return DragMode.RESIZE_LB;
        if (near(x, cropRect.right) && near(y, cropRect.bottom)) return DragMode.RESIZE_RB;
        if (near(x, cropRect.left)) return DragMode.RESIZE_LEFT;
        if (near(x, cropRect.right)) return DragMode.RESIZE_RIGHT;
        if (near(y, cropRect.top)) return DragMode.RESIZE_TOP;
        if (near(y, cropRect.bottom)) return DragMode.RESIZE_BOTTOM;
        return DragMode.NONE;
    }

    private boolean near(float a, float b) {
        return Math.abs(a - b) <= HANDLE_SIZE;
    }

    private void applyDrag(float dx, float dy) {
        RectF r = new RectF(cropRect);
        switch (dragMode) {
            case RESIZE_LEFT -> r.left += dx;
            case RESIZE_RIGHT -> r.right += dx;
            case RESIZE_TOP -> r.top += dy;
            case RESIZE_BOTTOM -> r.bottom += dy;
            case RESIZE_LT -> { r.left += dx; r.top += dy; }
            case RESIZE_RT -> { r.right += dx; r.top += dy; }
            case RESIZE_LB -> { r.left += dx; r.bottom += dy; }
            case RESIZE_RB -> { r.right += dx; r.bottom += dy; }
            case NONE -> { return; }
        }

        // Constrain to view bounds
        if (r.width() < MIN_SIZE) {
            if (dragMode == DragMode.RESIZE_LEFT || dragMode == DragMode.RESIZE_LT || dragMode == DragMode.RESIZE_LB) r.left = r.right - MIN_SIZE;
            if (dragMode == DragMode.RESIZE_RIGHT || dragMode == DragMode.RESIZE_RT || dragMode == DragMode.RESIZE_RB) r.right = r.left + MIN_SIZE;
        }
        if (r.height() < MIN_SIZE) {
            if (dragMode == DragMode.RESIZE_TOP || dragMode == DragMode.RESIZE_LT || dragMode == DragMode.RESIZE_RT) r.top = r.bottom - MIN_SIZE;
            if (dragMode == DragMode.RESIZE_BOTTOM || dragMode == DragMode.RESIZE_LB || dragMode == DragMode.RESIZE_RB) r.bottom = r.top + MIN_SIZE;
        }

        if (r.left < 0) r.offset(-r.left, 0);
        if (r.top < 0) r.offset(0, -r.top);
        if (r.right > getWidth()) r.offset(getWidth() - r.right, 0);
        if (r.bottom > getHeight()) r.offset(0, getHeight() - r.bottom);

        // If circle, keep square aspect
        if (shape == 1) {
            float size = Math.min(r.width(), r.height());
            r.right = r.left + size;
            r.bottom = r.top + size;
        }

        cropRect.set(r);
    }
}


