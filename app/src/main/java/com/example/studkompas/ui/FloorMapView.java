package com.example.studkompas.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.example.studkompas.model.GraphNode;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;
import java.util.Map;

public class FloorMapView extends PhotoView {
    private Paint nodePaint;
    private Paint edgePaint;
    private Paint pathPaint;

    private Map<String, GraphNode> floorGraph;
    private List<List<GraphNode>> floorPathSegments;
    private boolean isGraphVisible = true;

    public FloorMapView(Context context) {
        super(context);
        init();
    }

    public FloorMapView(Context context, AttributeSet attrs) {
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

    public void updatePath(List<List<GraphNode>> pathSegments) {
        this.floorPathSegments = pathSegments;
        invalidate();
    }

    public void loadFloorGraphForCampus(Map<String, GraphNode> floorGraph) {
        this.floorGraph = floorGraph;
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
        if (drawable == null)
            return;

        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();

        Matrix imageMatrix = getImageMatrix();
        boolean hasMatrix = !imageMatrix.isIdentity();

        if (hasMatrix) {
            canvas.save();
            canvas.concat(imageMatrix);
        }

        drawFloorSegmentsPath(canvas, imageWidth, imageHeight);

        if (isGraphVisible && floorGraph != null) {
            drawWholeGraph(canvas, imageWidth, imageHeight);
        }

        if (hasMatrix) {
            canvas.restore();
        }
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
        pathPaint.setStrokeWidth(50f);
        pathPaint.setAntiAlias(true);
    }

    private void drawWholeGraph(Canvas canvas, int imageWidth, int imageHeight) {
        for (GraphNode node : floorGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;

            float cx = node.location[0] * imageWidth;
            float cy = node.location[1] * imageHeight;

            canvas.drawCircle(cx, cy, 40f, nodePaint);
            drawNodeName(canvas, node, cx, cy);
        }

        for (GraphNode node : floorGraph.values()) {
            if (node.location == null || node.location.length < 2) continue;

            float x1 = node.location[0] * imageWidth;
            float y1 = node.location[1] * imageHeight;

            for (String neighborId : node.edges) {
                GraphNode neighbor = floorGraph.get(neighborId);
                if (neighbor == null || neighbor.location == null || neighbor.location.length < 2)
                    continue;

                float x2 = neighbor.location[0] * imageWidth;
                float y2 = neighbor.location[1] * imageHeight;

                canvas.drawLine(x1, y1, x2, y2, edgePaint);
            }
        }
    }


    private void drawFloorSegmentsPath(Canvas canvas, int imageWidth, int imageHeight) {
        if (floorPathSegments == null || floorPathSegments.isEmpty())
            return;

        for (List<GraphNode> segment : floorPathSegments) {
            if (segment.size() < 2) continue;

            for (int i = 0; i < segment.size() - 1; i++) {
                GraphNode a = segment.get(i);
                GraphNode b = segment.get(i + 1);

                if (a.location == null || b.location == null) continue;

                float x1 = a.location[0] * imageWidth;
                float y1 = a.location[1] * imageHeight;
                float x2 = b.location[0] * imageWidth;
                float y2 = b.location[1] * imageHeight;

                canvas.drawLine(x1, y1, x2, y2, pathPaint);
            }
        }
    }
}
