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

import java.util.List;
import java.util.Map;

public class CustomPhotoView extends PhotoView {
    private Paint nodePaint;
    private Paint edgePaint;
    private Paint pathPaint;

    private Map<String, GraphNode> campusGraph;
    private boolean isGraphVisible = true;
    private List<GraphNode> currentPath;

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

        pathPaint = new Paint();
        pathPaint.setColor(Color.GREEN);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(30f);
        pathPaint.setAntiAlias(true);
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

        Drawable drawable = getDrawable();
        if (drawable == null) return;

        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();

        Matrix imageMatrix = getImageMatrix();
        boolean hasMatrix = !imageMatrix.isIdentity();

        if (hasMatrix) {
            canvas.save();
            canvas.concat(imageMatrix);
        }

        drawCurrentPath(canvas, imageWidth, imageHeight);

        if (isGraphVisible && campusGraph != null) {
            drawGraphInImageCoords(canvas, imageWidth, imageHeight);
        }

        if (hasMatrix) {
            canvas.restore();
        }
    }

    private void drawGraphInImageCoords(Canvas canvas, int imageWidth, int imageHeight) {
        for (GraphNode node : campusGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;

            float cx = node.location[0] * imageWidth;
            float cy = node.location[1] * imageHeight;

            canvas.drawCircle(cx, cy, 40f, nodePaint);
            drawNodeName(canvas, node, cx, cy);
        }

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

    public void updatePath(List<GraphNode> path) {
        this.currentPath = path;
        invalidate();
    }


    private void drawCurrentPath(Canvas canvas, int imageWidth, int imageHeight) {
        if (currentPath == null || currentPath.size() < 2) return;

        for (int i = 0; i < currentPath.size() - 1; i++) {
            GraphNode a = currentPath.get(i);
            GraphNode b = currentPath.get(i + 1);

            float x1 = a.location[0] * imageWidth;
            float y1 = a.location[1] * imageHeight;
            float x2 = b.location[0] * imageWidth;
            float y2 = b.location[1] * imageHeight;

            canvas.drawLine(x1, y1, x2, y2, pathPaint);
        }
    }
}
