package com.example.studkompas.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.studkompas.model.Campus;
import com.example.studkompas.model.GraphNode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.appmetrica.analytics.AppMetrica;

public class AnalyticsHelper {

    private static final String TAG = "AnalyticsHelper";
    private static final String PREFS_NAME = "route_metrics";
    private static final String KEY_ROUTE_START_TIME = "last_route_start_time";
    private static final String KEY_ROUTE_ID = "last_route_id";
    private static final String KEY_CAMPUS_ID = "last_campus_id";
    private static final String KEY_START_NODE_NAME = "last_start_node";
    private static final String KEY_END_NODE_NAME = "last_end_node";

    // Имена событий
    private static final String EVENT_ROUTE_STARTED = "route_started";
    private static final String EVENT_ROUTE_COMPLETED = "route_completed";
    private static final String EVENT_ROUTE_CANCELLED_QUICK = "route_cancelled_quick";
    private static final String EVENT_ROUTE_AUTO_COMPLETED = "route_auto_completed";

    // ==================== ОСНОВНЫЕ ПУБЛИЧНЫЕ МЕТОДЫ ====================

    /**
     * Записать начало маршрута (когда пользователь нажал "Построить маршрут")
     */
    public static void logRouteStart(Context context, Campus campus, GraphNode startNode, GraphNode endNode) {
        // Генерируем уникальный ID для этого маршрута
        String routeId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        // Сохраняем в SharedPreferences
        saveRouteData(context, routeId, startTime, campus, startNode, endNode);

        // Отправляем событие в AppMetrica
        Map<String, Object> params = createRouteStartParams(routeId, campus, startNode, endNode, startTime);
        sendToAppMetrica(EVENT_ROUTE_STARTED, params);

        Log.d(TAG, "Маршрут начат: " + startNode.name + " -> " + endNode.name +
                " (ID: " + routeId + ", время: " + startTime + ")");

        // Запускаем проверку на авто-завершение через 10 минут
        scheduleAutoCompletionCheck(context, routeId, campus);
    }

    /**
     * Обработать нажатие кнопки "Завершить маршрут"
     * Возвращает true если нажатие засчитано (прошло >30 секунд)
     */
    public static boolean logRouteButtonClick(Context context) {
        RouteData data = loadRouteData(context);

        if (!data.isValid()) {
            Log.w(TAG, "Нажата кнопка завершения, но нет активного маршрута");
            return false;
        }

        long duration = System.currentTimeMillis() - data.startTime;

        // Очищаем сохраненные данные
        clearRouteData(context);


        if (duration > 20000) { // 20 секунд = 20000 мс
            // ЗАСЧИТЫВАЕМ нажатие как успешное завершение
            Map<String, Object> params = createRouteCompleteParams(
                    data.routeId, data.campusId, duration, true);

            sendToAppMetrica(EVENT_ROUTE_COMPLETED, params);

            Log.d(TAG, "✅ Нажатие ЗАСЧИТАНО: маршрут завершен за " +
                    (duration / 1000) + " секунд (ID: " + data.routeId + ")");
            return true;
        } else {
            // НЕ ЗАСЧИТЫВАЕМ нажатие (слишком быстро)
            Map<String, Object> params = createRouteCancelledParams(
                    data.routeId, duration);

            sendToAppMetrica(EVENT_ROUTE_CANCELLED_QUICK, params);

            Log.d(TAG, "❌ Нажатие НЕ ЗАСЧИТАНО: прошло только " +
                    duration + " мс (<30 сек) (ID: " + data.routeId + ")");
            return false;
        }
    }

    /**
     * Автоматически засчитать маршрут как завершенный через 10 минут
     */
    public static void logRouteAutoComplete(Context context) {
        RouteData data = loadRouteData(context);

        if (!data.isValid()) {
            return; // Нет активного маршрута
        }

        long duration = System.currentTimeMillis() - data.startTime;

        // Автоматически засчитываем как завершенный
        Map<String, Object> params = createRouteCompleteParams(
                data.routeId, data.campusId, duration, false);
        params.put("auto_completed", true);

        sendToAppMetrica(EVENT_ROUTE_AUTO_COMPLETED, params);

        // Очищаем данные
        clearRouteData(context);

        Log.d(TAG, "Маршрут АВТОМАТИЧЕСКИ завершен через " +
                (duration / 1000) + " секунд (ID: " + data.routeId + ")");
    }

    // ==================== ПАРАМЕТРЫ ====================

    /**
     * Создает параметры для события "начало маршрута"
     */
    private static Map<String, Object> createRouteStartParams(
            String routeId, Campus campus, GraphNode startNode,
            GraphNode endNode, long timestamp) {

        Map<String, Object> params = new HashMap<>();
        params.put("route_id", routeId);
        params.put("campus_id", campus.Id);
        params.put("campus_name", campus.Name);
        params.put("from_node", startNode.name);
        params.put("to_node", endNode.name);
        params.put("from_floor", startNode.floor);
        params.put("to_floor", endNode.floor);
        params.put("timestamp", timestamp);

        return params;
    }

    /**
     * Создает параметры для события "завершение маршрута"
     * @param manuallyCompleted true - пользователь нажал кнопку, false - авто-завершение
     */
    private static Map<String, Object> createRouteCompleteParams(
            String routeId, String campusId, long duration, boolean manuallyCompleted) {

        Map<String, Object> params = new HashMap<>();
        params.put("route_id", routeId);
        params.put("campus_id", campusId);
        params.put("duration_ms", duration);
        params.put("duration_seconds", duration / 1000);
        params.put("completed_manually", manuallyCompleted);

        return params;
    }

    /**
     * Создает параметры для события "отмена маршрута (слишком быстро)"
     */
    private static Map<String, Object> createRouteCancelledParams(
            String routeId, long duration) {

        Map<String, Object> params = new HashMap<>();
        params.put("route_id", routeId);
        params.put("duration_ms", duration);
        params.put("reason", "too_quick");

        return params;
    }

    // ==================== РАБОТА С ХРАНИЛИЩЕМ ====================

    /**
     * Вспомогательный класс для хранения данных маршрута
     */
    private static class RouteData {
        String routeId;
        long startTime;
        String campusId;
        String startNodeName;
        String endNodeName;

        boolean isValid() {
            return routeId != null && startTime > 0;
        }
    }

    /**
     * Сохраняет данные маршрута в SharedPreferences
     */
    private static void saveRouteData(Context context, String routeId, long startTime,
                                      Campus campus, GraphNode startNode, GraphNode endNode) {

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_ROUTE_START_TIME, startTime)
                .putString(KEY_ROUTE_ID, routeId)
                .putString(KEY_CAMPUS_ID, campus.Id)
                .putString(KEY_START_NODE_NAME, startNode.name)
                .putString(KEY_END_NODE_NAME, endNode.name)
                .apply();
    }

    /**
     * Загружает данные маршрута из SharedPreferences
     */
    private static RouteData loadRouteData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        RouteData data = new RouteData();
        data.startTime = prefs.getLong(KEY_ROUTE_START_TIME, 0);
        data.routeId = prefs.getString(KEY_ROUTE_ID, null);
        data.campusId = prefs.getString(KEY_CAMPUS_ID, null);
        data.startNodeName = prefs.getString(KEY_START_NODE_NAME, null);
        data.endNodeName = prefs.getString(KEY_END_NODE_NAME, null);

        return data;
    }

    /**
     * Очищает данные маршрута
     */
    private static void clearRouteData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_ROUTE_START_TIME)
                .remove(KEY_ROUTE_ID)
                .remove(KEY_CAMPUS_ID)
                .remove(KEY_START_NODE_NAME)
                .remove(KEY_END_NODE_NAME)
                .apply();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Проверить, есть ли активный маршрут, который нужно авто-завершить
     */
    public static void checkForAutoCompletion(Context context) {
        RouteData data = loadRouteData(context);

        if (data.isValid()) {
            long duration = System.currentTimeMillis() - data.startTime;

            // Если прошло больше 10 минут (600000 мс)
            if (duration > 600000) {
                logRouteAutoComplete(context);
            }
        }
    }

    /**
     * Получить время начала текущего маршрута (для отладки)
     */
    public static long getCurrentRouteDuration(Context context) {
        RouteData data = loadRouteData(context);

        if (data.isValid()) {
            return System.currentTimeMillis() - data.startTime;
        }
        return 0;
    }

    /**
     * Запланировать проверку на авто-завершение через 10 минут
     */
    private static void scheduleAutoCompletionCheck(Context context, String routeId, Campus campus) {
        // Используем Handler для простоты
        new android.os.Handler().postDelayed(() -> {
            // Проверяем, все ли еще этот маршрут активен
            RouteData data = loadRouteData(context);

            if (data.isValid() && routeId.equals(data.routeId)) {
                logRouteAutoComplete(context);
            }
        }, 600000); // 10 минут = 600000 мс
    }

    /**
     * Отправка события в AppMetrica
     */
    private static void sendToAppMetrica(String eventName, Map<String, Object> parameters) {
        // TODO: Раскомментировать после настройки AppMetrica

        if (parameters != null && !parameters.isEmpty()) {
            AppMetrica.reportEvent(eventName, parameters);
        } else {
            AppMetrica.reportEvent(eventName);
        }

        // Временный вывод в лог для отладки
        Log.i("METRICS", "📊 Событие: " + eventName +
                (parameters != null ? ", Параметры: " + parameters : ""));
    }

    /**
     * Для отладки: получить информацию о текущем маршруте
     */
    @SuppressLint("DefaultLocale")
    public static String getCurrentRouteInfo(Context context) {
        RouteData data = loadRouteData(context);

        if (data.isValid()) {
            long duration = System.currentTimeMillis() - data.startTime;
            return String.format("Маршрут: %s → %s, длительность: %d сек",
                    data.startNodeName, data.endNodeName, duration / 1000);
        }
        return "Нет активного маршрута";
    }
}