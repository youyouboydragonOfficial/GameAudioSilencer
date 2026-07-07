package com.youyouboydragon.gameaudiosilencer;

import android.graphics.drawable.Drawable;

class AppEntry {
    final String label;
    final String packageName;
    final Drawable icon;
    boolean muted;

    AppEntry(String label, String packageName, Drawable icon, boolean muted) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
        this.muted = muted;
    }
}
