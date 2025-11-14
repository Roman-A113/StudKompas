package com.example.studkompas.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.example.studkompas.utils.GraphManager;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Map;

public class CustomPhotoView extends PhotoView {
    private Paint nodePaint;
    private Paint edgePaint;
    private Map<String, GraphNode> campusGraph;
    private boolean isGraphVisible = true;

    public CustomPhotoView(Context context) {
        super(context);
        init();
    }

    public CustomPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private static void drawNodeName(@NonNull Canvas canvas, GraphNode node, float cx, float cy) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(node.name, cx, cy - 40, textPaint);
    }

    private void init() {
        nodePaint = new Paint();
        nodePaint.setColor(Color.RED);
        nodePaint.setStyle(Paint.Style.FILL);
        nodePaint.setAntiAlias(true);

        edgePaint = new Paint();
        edgePaint.setColor(Color.RED);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(20f);
        edgePaint.setAntiAlias(true);
    }

    public void loadGraphForCampus(String campusKey, String floor) {
        campusGraph = GraphManager.Graphs.get(campusKey).get(floor);
        invalidate();
    }

    public void setGraphVisible(boolean visible) {
        this.isGraphVisible = visible;
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!isGraphVisible) return;

        Drawable drawable = getDrawable();
        if (drawable == null) return;

        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();

        // Получаем матрицу, которая преобразует координаты ИЗОБРАЖЕНИЯ → в координаты ЭКРАНА
        Matrix imageMatrix = getImageMatrix();
        if (imageMatrix.isIdentity()) {
            // Если матрица тождественная — можно рисовать напрямую
            drawGraphInImageCoords(canvas, imageWidth, imageHeight);
        } else {
            // Сохраняем текущее состояние canvas
            canvas.save();

            // Применяем ту же трансформацию, что и к изображению
            canvas.concat(imageMatrix);

            // Теперь рисуем в координатах ИСХОДНОГО изображения
            drawGraphInImageCoords(canvas, imageWidth, imageHeight);

            // Восстанавливаем canvas
            canvas.restore();
        }
    }

    private void drawGraphInImageCoords(Canvas canvas, int imageWidth, int imageHeight) {
        // Рисуем узлы
        for (GraphNode node : campusGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;

            // node.location — это относительные координаты (0.0–1.0)
            float cx = node.location[0] * imageWidth;
            float cy = node.location[1] * imageHeight;

            canvas.drawCircle(cx, cy, 40f, nodePaint);
            drawNodeName(canvas, node, cx, cy);
        }

        // Рисуем рёбра
        for (GraphNode node : campusGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;

            float x1 = node.location[0] * imageWidth;
            float y1 = node.location[1] * imageHeight;

            for (String neighborId : node.edges) {
                GraphNode neighbor = campusGraph.get(neighborId);
                if (neighbor == null || neighbor.location == null || neighbor.location.length < 2)
                    continue;

                float x2 = neighbor.location[0] * imageWidth;
                float y2 = neighbor.location[1] * imageHeight;

                canvas.drawLine(x1, y1, x2, y2, edgePaint);
            }
        }
    }
}