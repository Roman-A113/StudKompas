package com.example.studkompas.utils;

import android.content.Context;
import android.util.Log;

import com.example.studkompas.model.GraphNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GraphManager {
    private static final String TAG = "GraphManager";
    private static final String GRAPH_FILENAME = "graph.json";
    public static Map<String, Map<String, GraphNode>> Graphs = new HashMap<>();

    public static void loadGraphFromAssets(Context context) {
        try (InputStream is = context.getAssets().open("graph.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            Type type = new TypeToken<Map<String, Map<String, GraphNode>>>() {}.getType();
            Graphs = new Gson().fromJson(sb.toString(), type);

            if (Graphs == null) {
                Graphs = new HashMap<>();
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке graph.json из assets", e);
            Graphs = new HashMap<>();
        }
    }

    public static void saveGraphToTempFile(Context context) {
        File file = new File(context.getFilesDir(), GRAPH_FILENAME);
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(Graphs));
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Ошибка сохранения graph.json", e);
        }
    }

    public static void copyAssetGraphToTempFile(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("graph.json");
            File outFile = new File(context.getFilesDir(), "graph.json");
            OutputStream outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось скопировать graph.json из assets", e);
        }
    }

    public static void addNode(Context context, String campusKey, float x, float y, String name) {
        Graphs.putIfAbsent(campusKey, new HashMap<>());
        Map<String, GraphNode> campusMap = Graphs.get(campusKey);

        int nextId = campusMap.size() + 1;
        String idStr = String.valueOf(nextId);
        GraphNode newNode = new GraphNode(idStr, name, new float[]{x, y});
        campusMap.put(idStr, newNode);
        saveGraphToTempFile(context);
    }

    public static void addEdge(Context context, String campusKey, String nodeId1, String nodeId2) {
        Map<String, GraphNode> campusMap = Graphs.get(campusKey);

        GraphNode node1 = campusMap.get(nodeId1);
        GraphNode node2 = campusMap.get(nodeId2);

        node1.edges.add(nodeId2);
        node2.edges.add(nodeId1);
        saveGraphToTempFile(context);
    }
}