package com.example.studkompas;

import android.app.Application;

import com.github.chrisbanes.photoview.BuildConfig;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;

public class App extends Application {
    private static final String APPMETRICA_API_KEY = "cf9292a2-8e55-42d5-bb70-90c6987fda09";

    @Override
    public void onCreate() {
        super.onCreate();

        boolean isDebug = BuildConfig.DEBUG;

        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(APPMETRICA_API_KEY)
                .withCrashReporting(true)
                .withNativeCrashReporting(true)
                .withSessionTimeout(30)
                .withDataSendingEnabled(!isDebug)
                .build();

        // Используем новый класс AppMetrica для активации
        AppMetrica.activate(this, config);

        // Автотрекинг активности (опционально)
        AppMetrica.enableActivityAutoTracking(this);
    }
}
