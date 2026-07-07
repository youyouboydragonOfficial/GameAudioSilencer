package com.youyouboydragon.gameaudiosilencer;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFFF7FAFC);
        root.setPadding(dp(22), dp(72), dp(22), dp(28));

        TextView badge = text("\u63d0\u4f9b\u7d42\u4e86", 13, 0xFFFFFFFF, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundColor(0xFF52616F);
        root.addView(badge, new LinearLayout.LayoutParams(dp(112), dp(32)));

        TextView title = text("Game Audio Silencer", 26, 0xFF17212B, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(18), 0, dp(18));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView message = text(
                "\u3053\u306e\u30a2\u30d7\u30ea\u306f\u5b9f\u73fe\u304c\u53b3\u3057\u3044\u3068\u5224\u65ad\u3057\u305f\u305f\u3081\u3001\u63d0\u4f9b\u3092\u7d42\u4e86\u3057\u307e\u3057\u305f\u3002\n\n"
                        + "\u901a\u5e38\u306eAndroid\u7aef\u672b\u3067\u306f\u3001\u4ed6\u306e\u30a2\u30d7\u30ea\u3060\u3051\u3092\u500b\u5225\u306b\u7121\u97f3\u5316\u3059\u308b\u305f\u3081\u306e\u516c\u5f0f\u306a\u6a29\u9650\u304c\u63d0\u4f9b\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002\n\n"
                        + "\u305d\u306e\u305f\u3081\u3001\u3053\u306e\u30a2\u30d7\u30ea\u306e\u914d\u5e03\u3068\u6a5f\u80fd\u63d0\u4f9b\u306f\u7d42\u4e86\u3057\u307e\u3059\u3002",
                17,
                0xFF22313B,
                Typeface.NORMAL
        );
        message.setGravity(Gravity.CENTER);
        message.setLineSpacing(dp(3), 1.0f);
        root.addView(message, new LinearLayout.LayoutParams(-1, -2));

        TextView version = text("\u7d42\u4e86\u7248 / " + BuildConfig.VERSION_NAME, 13, 0xFF6B7A89, Typeface.NORMAL);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, dp(26), 0, 0);
        root.addView(version, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
