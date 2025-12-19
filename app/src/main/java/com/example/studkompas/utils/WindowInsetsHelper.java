package com.example.studkompas.utils;

import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

import androidx.appcompat.app.AppCompatActivity;

public class WindowInsetsHelper {
    public static void ApplySystemWindowInsets(AppCompatActivity activity, int rootLayoutId) {
        View root = activity.findViewById(rootLayoutId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets systemBars = null;
                systemBars = insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );
                return insets;
            });
        }
    }
}
