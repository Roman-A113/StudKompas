package com.example.studkompas.utils;

import android.content.Context;
import android.util.Log;

import com.example.studkompas.model.CampusGraph;
import com.example.studkompas.model.GraphNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GraphManager {
    private static final String TAG = "GraphManager";
    private static final String GRAPH_FILENAME = "graph.json";

    public static CampusGraph loadGraph(Context context) {
        File file = new File(context.getFilesDir(), GRAPH_FILENAME);
        if (!file.exists()) {
            Log.w(TAG, "graph.json отсутствует даже после ensureGraphFileExists!");
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new Gson().fromJson(sb.toString(), CampusGraph.class);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке graph.json", e);
            return null;
        }
    }

    public static void saveGraph(Context context, CampusGraph graph) {
        File file = new File(context.getFilesDir(), GRAPH_FILENAME);
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(graph));
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Ошибка сохранения graph.json", e);
        }
    }

    public static void addNode(Context context, String campusKey, float x, float y) {
        CampusGraph graph = loadGraph(context);

        Map<String, GraphNode> campusMap;
        switch (campusKey) {
            case "guk":
                campusMap = graph.guk;
                break;
            case "turgeneva":
                campusMap =  graph.turgeneva;
                break;
            case "bio":
                campusMap = graph.bio;
                break;
            default:
                return;
        }

        int nextId = campusMap.size() + 1;
        String idStr = String.valueOf(nextId);
        GraphNode newNode = new GraphNode(String.valueOf(nextId), "Точка " + nextId,  new float[]{x, y});
        campusMap.put(idStr, newNode);
        saveGraph(context, graph);
    }
}