package com.example.studkompas.utils;

import android.content.Context;
import android.util.Log;

import com.example.studkompas.model.GraphNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GraphManager {
    public static Map<String, Map<String, GraphNode>> Graphs = new HashMap<>();
    private static final String TAG = "GraphManager";
    private static final String GRAPH_FILENAME = "graph.json";

    public static void loadGraphFromFile(Context context) {
        File file = new File(context.getFilesDir(), GRAPH_FILENAME);

        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            Type type = new TypeToken<Map<String, Map<String, GraphNode>>>(){}.getType();
            Graphs = new Gson().fromJson(sb.toString(), type);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке graph.json", e);
            Graphs = new HashMap<>(); // fallback
        }
    }

    public static void saveGraphToFile(Context context) {
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

    public static void addNode(Context context, String campusKey, float x, float y) {
        Graphs.putIfAbsent(campusKey, new HashMap<>());
        Map<String, GraphNode> campusMap = Graphs.get(campusKey);

        int nextId = campusMap.size() + 1;
        String idStr = String.valueOf(nextId);
        GraphNode newNode = new GraphNode(idStr, "Точка " + nextId, new float[]{x, y});
        campusMap.put(idStr, newNode);
        saveGraphToFile(context);
    }
}