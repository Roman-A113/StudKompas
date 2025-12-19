package com.example.studkompas.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.example.studkompas.model.GraphNode;
import com.example.studkompas.model.TransitionPoint;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;
import java.util.Map;

public class FloorMapView extends PhotoView {
    private Paint nodePaint;
    private Paint edgePaint;
    private Paint pathPaint;

    private Map<String, GraphNode> floorGraph;
    private List<List<GraphNode>> floorPathSegments;
    private List<TransitionPoint> transitionNodes;
    private boolean isGraphVisible = true;

    private String selectedFloor;


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

    public void clearPath() {
        this.floorPathSegments = null;
        invalidate();
    }

    public void clearTransitionNodes() {
        this.transitionNodes = null;
        invalidate();
    }


    public void setTransitionNodes(List<TransitionPoint> nodes) {
        this.transitionNodes = nodes;
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
        drawTransitionIndicators(canvas, imageWidth, imageHeight);

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

            canvas.drawCircle(cx, cy, 20f, nodePaint);
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


    private void drawTransitionIndicators(Canvas canvas, int imageWidth, int imageHeight) {
        if (transitionNodes == null || transitionNodes.isEmpty()) return;


        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(80f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setFakeBoldText(true);


        Paint arrowPaint = new Paint();
        arrowPaint.setAntiAlias(true);
        arrowPaint.setColor(Color.BLUE);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(12f);
        arrowPaint.setStrokeJoin(Paint.Join.ROUND);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);


        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.GRAY);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        borderPaint.setAntiAlias(true);

        for (TransitionPoint tp : transitionNodes) {
            if (!tp.fromNode.floor.equals(selectedFloor)) continue;
            if (tp.fromNode.location == null || tp.fromNode.location.length < 2) continue;

            float cx = tp.fromNode.location[0] * imageWidth;
            float cy = tp.fromNode.location[1] * imageHeight;

            float blockX = cx - 60;
            float blockY = cy - 60;

            String floorLabel = tp.targetFloor;
            float textWidth = textPaint.measureText(floorLabel);
            float textHeight = -textPaint.ascent() + textPaint.descent();


            float arrowWidth = 30f;
            float arrowHeight = 100f;
            float padding = 20f;


            float totalWidth = textWidth + arrowWidth + padding * 3;
            float totalHeight = Math.max(textHeight, arrowHeight) + padding * 2;


            float right = blockX + totalWidth;
            float bottom = blockY + totalHeight;


            canvas.drawRect(blockX, blockY, right, bottom, bgPaint);


            canvas.drawRect(blockX, blockY, right, bottom, borderPaint);


            float textX = blockX + padding;
            float textY = blockY + padding + textHeight;
            canvas.drawText(floorLabel, textX, textY, textPaint);


            float arrowStartX = textX + textWidth + padding;
            float arrowCenterY = blockY + totalHeight / 2;

            Path arrow = new Path();

            arrow.moveTo(arrowStartX, arrowCenterY);
            arrow.lineTo(arrowStartX, arrowCenterY - arrowHeight / 2);


            float headSize = 15f;
            arrow.lineTo(arrowStartX - headSize, arrowCenterY - arrowHeight / 2 + headSize);
            arrow.moveTo(arrowStartX, arrowCenterY - arrowHeight / 2);
            arrow.lineTo(arrowStartX + headSize, arrowCenterY - arrowHeight / 2 + headSize);

            canvas.drawPath(arrow, arrowPaint);
        }
    }

    private void drawFloorSegmentsPath(Canvas canvas, int imageWidth, int imageHeight) {
        if (floorPathSegments == null || floorPathSegments.isEmpty()) return;

        Path path = new Path();
        for (List<GraphNode> segment : floorPathSegments) {
            if (segment.size() < 2)
                continue;

            path.reset();
            GraphNode start = segment.get(0);

            float startX = start.location[0] * imageWidth;
            float startY = start.location[1] * imageHeight;
            path.moveTo(startX, startY);

            for (int i = 1; i < segment.size(); i++) {
                GraphNode prev = segment.get(i - 1);
                GraphNode curr = segment.get(i);

                float prevX = prev.location[0] * imageWidth;
                float prevY = prev.location[1] * imageHeight;

                float x = curr.location[0] * imageWidth;
                float y = curr.location[1] * imageHeight;

                float midX = (prevX + x) / 2f;
                float midY = (prevY + y) / 2f;

                path.quadTo(prevX, prevY, midX, midY);
            }

            GraphNode last = segment.get(segment.size() - 1);
            float lastX = last.location[0] * imageWidth;
            float lastY = last.location[1] * imageHeight;
            path.lineTo(lastX, lastY);

            canvas.drawPath(path, pathPaint);
        }
    }

    public void setFloor(String selectedFloor) {
        this.selectedFloor = selectedFloor;
    }
}
