package com.example.studkompas.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.github.chrisbanes.photoview.PhotoView;

public class CustomPhotoView extends PhotoView {
    private Paint testPaint;

    public CustomPhotoView(Context context) {
        super(context);
        init();
    }

    public CustomPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        testPaint = new Paint();
        testPaint.setColor(Color.RED);
        testPaint.setStyle(Paint.Style.STROKE);
        testPaint.setStrokeWidth(8f);
        testPaint.setAntiAlias(true);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        // TODO

        /*
        if (showTestLine) {
            Drawable drawable = getDrawable();
            if (drawable != null) {
                int imgWidth = drawable.getIntrinsicWidth();
                int imgHeight = drawable.getIntrinsicHeight();
                canvas.save();

                Matrix imageMatrix = getImageMatrix();
                canvas.concat(imageMatrix);

                drawLinesOnImage(canvas, imgWidth, imgHeight);

                canvas.restore();
            }
        }
        */
    }

    private void drawLinesOnImage(Canvas canvas, float imgWidth, float imgHeight) {
        // TODO

        /*
        testPaint.setColor(Color.RED);
        canvas.drawLine(0, 0, imgWidth, imgHeight, testPaint);

        testPaint.setColor(Color.BLUE);
        canvas.drawLine(0, imgHeight / 2, imgWidth, imgHeight / 2, testPaint);

        testPaint.setColor(Color.GREEN);
        canvas.drawLine(imgWidth / 2, 0, imgWidth / 2, imgHeight, testPaint);

        testPaint.setColor(Color.YELLOW);
        testPaint.setStrokeWidth(3f);
        canvas.drawRect(0, 0, imgWidth, imgHeight, testPaint);
        */
    }

    public boolean getShowTestLine() {
        // TODO
        return false;
    }

    public void setShowTestLine(boolean show) {
        // TODO
        // invalidate();
    }
}