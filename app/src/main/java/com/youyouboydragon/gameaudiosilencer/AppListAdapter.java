package com.youyouboydragon.gameaudiosilencer;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class AppListAdapter extends BaseAdapter {
    interface Listener {
        void onToggle(AppEntry entry);
    }

    private final Context context;
    private final Listener listener;
    private final List<AppEntry> all = new ArrayList<>();
    private final List<AppEntry> visible = new ArrayList<>();

    AppListAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    void setApps(List<AppEntry> apps) {
        all.clear();
        all.addAll(apps);
        filter("");
    }

    void filter(String query) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.US).trim();
        visible.clear();
        for (AppEntry entry : all) {
            if (normalized.isEmpty()
                    || entry.label.toLowerCase(Locale.US).contains(normalized)
                    || entry.packageName.toLowerCase(Locale.US).contains(normalized)) {
                visible.add(entry);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return visible.size();
    }

    @Override
    public Object getItem(int position) {
        return visible.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(10), dp(12), dp(10));

            ImageView icon = new ImageView(context);
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(42), dp(42));
            row.addView(icon, iconLp);

            LinearLayout texts = new LinearLayout(context);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setPadding(dp(12), 0, dp(10), 0);
            row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));

            TextView title = new TextView(context);
            title.setTextColor(0xFF17212B);
            title.setTextSize(15);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setSingleLine(true);
            texts.addView(title);

            TextView pkg = new TextView(context);
            pkg.setTextColor(0xFF607080);
            pkg.setTextSize(12);
            pkg.setSingleLine(true);
            texts.addView(pkg);

            TextView action = new TextView(context);
            action.setGravity(Gravity.CENTER);
            action.setTextSize(13);
            action.setTypeface(Typeface.DEFAULT_BOLD);
            action.setMinWidth(dp(76));
            action.setPadding(dp(10), dp(8), dp(10), dp(8));
            row.addView(action, new LinearLayout.LayoutParams(-2, -2));

            holder = new Holder(icon, title, pkg, action);
            row.setTag(holder);
            convertView = row;
        } else {
            holder = (Holder) convertView.getTag();
        }

        AppEntry entry = visible.get(position);
        holder.icon.setImageDrawable(entry.icon);
        holder.title.setText(entry.label);
        holder.pkg.setText(entry.packageName);
        holder.action.setText(entry.muted ? "ON" : "OFF");
        holder.action.setTextColor(entry.muted ? 0xFFFFFFFF : 0xFF2F3A45);
        holder.action.setBackgroundColor(entry.muted ? 0xFFE15241 : 0xFFE6EDF2);
        convertView.setOnClickListener(v -> listener.onToggle(entry));
        return convertView;
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class Holder {
        final ImageView icon;
        final TextView title;
        final TextView pkg;
        final TextView action;

        Holder(ImageView icon, TextView title, TextView pkg, TextView action) {
            this.icon = icon;
            this.title = title;
            this.pkg = pkg;
            this.action = action;
        }
    }
}
