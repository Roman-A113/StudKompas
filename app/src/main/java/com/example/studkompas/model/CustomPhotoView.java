package com.example.studkompas.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.example.studkompas.utils.GraphManager;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.Map;

public class CustomPhotoView extends PhotoView {
    private Paint nodePaint;
    private Paint edgePaint;
    private boolean showGraph = false;
    private Map<String, GraphNode> campusGraph;

    public CustomPhotoView(Context context) {
        super(context);
        init();
    }

    public CustomPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
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


    public void loadGraphForCampus(String campusKey) {
        campusGraph = GraphManager.Graphs.get(campusKey);
        showGraph = true;
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);

        if (!showGraph || campusGraph == null)
            return;

        canvas.save();
        Matrix matrix = getImageMatrix();
        canvas.concat(matrix);

        for (GraphNode node : campusGraph.values()) {
            if (node.location == null || node.location.length < 2)
                continue;
            float cx = node.location[0];
            float cy = node.location[1];
            canvas.drawCircle(cx, cy, 40f, nodePaint);
            drawNodeName(canvas, node, cx, cy);
        }

        for (GraphNode node : campusGraph.values()) {
            if (node.location == null || node.location.length < 2)
                continue;

            float x1 = node.location[0];
            float y1 = node.location[1];

            for (String neighborId : node.edges) {
                GraphNode neighbor = campusGraph.get(neighborId);
                float x2 = neighbor.location[0];
                float y2 = neighbor.location[1];
                canvas.drawLine(x1, y1, x2, y2, edgePaint);
            }
        }
        canvas.restore();
    }

    private static void drawNodeName(@NonNull Canvas canvas, GraphNode node, float cx, float cy) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(node.name, cx, cy - 40, textPaint);
    }
}