package com.youyouboydragon.gameaudiosilencer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String PREFS = "game_audio_silencer";
    private static final String KEY_MUTED = "muted_packages";
    private static final String KEY_TUTORIAL = "tutorial_seen";
    private static final String REPO = "youyouboydragonOfficial/GameAudioSilencer";
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final String RELEASES_PAGE = "https://github.com/" + REPO + "/releases/latest";

    private SharedPreferences prefs;
    private AppListAdapter adapter;
    private TextView status;
    private TextView rootStatus;
    private long downloadId = -1L;

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
            if (id == downloadId) {
                openDownloadedApk(id);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        buildUi();
        loadApps();
        if (!prefs.getBoolean(KEY_TUTORIAL, false)) {
            showTutorial();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("Ready");
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(downloadReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(downloadReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(downloadReceiver);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF7FAFC);
        root.setPadding(dp(18), dp(54), dp(18), dp(14));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView beta = text("BETA", 12, 0xFFFFFFFF, Typeface.BOLD);
        beta.setGravity(Gravity.CENTER);
        beta.setBackgroundColor(0xFF2F7D6D);
        header.addView(beta, new LinearLayout.LayoutParams(dp(76), dp(28)));

        TextView title = text("Game Audio Silencer", 25, 0xFF17212B, Typeface.BOLD);
        title.setPadding(0, dp(10), 0, dp(4));
        header.addView(title);

        TextView subtitle = text("root の appops で対象アプリだけ PLAY_AUDIO を止めます。音楽アプリはそのまま再生できます。", 13, 0xFF52616F, Typeface.NORMAL);
        subtitle.setPadding(0, 0, 0, dp(10));
        header.addView(subtitle);

        status = text("", 13, 0xFF1F3440, Typeface.NORMAL);
        header.addView(status);

        rootStatus = text("", 13, 0xFF52616F, Typeface.NORMAL);
        rootStatus.setPadding(0, dp(4), 0, dp(10));
        header.addView(rootStatus);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(buttons, new LinearLayout.LayoutParams(-1, -2));
        buttons.addView(button("Tutorial", v -> showTutorial()), new LinearLayout.LayoutParams(0, dp(46), 1f));
        buttons.addView(spacer(8, 1));
        buttons.addView(button("Check update", v -> checkForUpdate()), new LinearLayout.LayoutParams(0, dp(46), 1f));

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setHint("対象アプリを検索");
        search.setPadding(dp(12), 0, dp(12), 0);
        search.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(-1, dp(48));
        searchLp.setMargins(0, dp(12), 0, dp(8));
        header.addView(search, searchLp);

        adapter = new AppListAdapter(this, this::toggleApp);
        ListView list = new ListView(this);
        list.setAdapter(adapter);
        list.setDividerHeight(1);
        list.setBackgroundColor(0xFFFFFFFF);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1f));

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        setContentView(root);
    }

    private void showTutorial() {
        ScrollView scroll = new ScrollView(this);
        TextView body = text(
                "1. 音を消したいゲームやアプリを一覧から選びます。\n\n"
                        + "2. ON にすると root 権限を使って、そのアプリの PLAY_AUDIO を ignore にします。音楽アプリは選ばなければ再生されたままです。\n\n"
                        + "3. OFF に戻すと PLAY_AUDIO を allow に戻します。\n\n"
                        + "4. 非 root 端末では Android の仕様で他アプリ単位の音量制御はできません。この Beta では root 端末向けに実用化しています。\n\n"
                        + "5. 更新は Check update から GitHub Releases を確認し、APK をダウンロードしてインストールできます。",
                15, 0xFF22313B, Typeface.NORMAL);
        int pad = dp(18);
        body.setPadding(pad, pad, pad, pad);
        scroll.addView(body);
        new AlertDialog.Builder(this)
                .setTitle("Beta tutorial")
                .setView(scroll)
                .setPositiveButton("OK", (d, w) -> prefs.edit().putBoolean(KEY_TUTORIAL, true).apply())
                .show();
    }

    private void loadApps() {
        new AsyncTask<Void, Void, List<AppEntry>>() {
            @Override
            protected List<AppEntry> doInBackground(Void... voids) {
                PackageManager pm = getPackageManager();
                Set<String> muted = getMutedPackages();
                List<AppEntry> result = new ArrayList<>();
                List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo info : apps) {
                    if (getPackageName().equals(info.packageName)) continue;
                    if (pm.getLaunchIntentForPackage(info.packageName) == null) continue;
                    String label = pm.getApplicationLabel(info).toString();
                    result.add(new AppEntry(label, info.packageName, pm.getApplicationIcon(info), muted.contains(info.packageName)));
                }
                Collections.sort(result, (a, b) -> a.label.compareToIgnoreCase(b.label));
                return result;
            }

            @Override
            protected void onPostExecute(List<AppEntry> apps) {
                adapter.setApps(apps);
                updateStatus("Loaded " + apps.size() + " apps");
            }
        }.execute();
    }

    private void toggleApp(AppEntry entry) {
        boolean nextMuted = !entry.muted;
        String mode = nextMuted ? "ignore" : "allow";
        updateStatus((nextMuted ? "Muting " : "Restoring ") + entry.label + "...");
        new RootTask(success -> {
            if (success) {
                entry.muted = nextMuted;
                saveMuted(entry.packageName, nextMuted);
                adapter.notifyDataSetChanged();
                updateStatus(entry.label + (nextMuted ? " muted" : " restored"));
            } else {
                updateStatus("Root command failed");
                showRootHelp();
            }
        }).execute("cmd appops set " + safePackage(entry.packageName) + " PLAY_AUDIO " + mode);
    }

    private void checkForUpdate() {
        updateStatus("Checking GitHub Releases...");
        new AsyncTask<Void, Void, UpdateInfo>() {
            @Override
            protected UpdateInfo doInBackground(Void... voids) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(LATEST_RELEASE_API).openConnection();
                    conn.setRequestProperty("Accept", "application/vnd.github+json");
                    conn.setConnectTimeout(12000);
                    conn.setReadTimeout(12000);
                    int code = conn.getResponseCode();
                    if (code < 200 || code >= 300) return null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) json.append(line);
                    JSONObject root = new JSONObject(json.toString());
                    String tag = root.optString("tag_name", "");
                    String page = root.optString("html_url", RELEASES_PAGE);
                    String apk = "";
                    JSONArray assets = root.optJSONArray("assets");
                    if (assets != null) {
                        for (int i = 0; i < assets.length(); i++) {
                            JSONObject asset = assets.getJSONObject(i);
                            String name = asset.optString("name", "");
                            if (name.endsWith(".apk")) {
                                apk = asset.optString("browser_download_url", "");
                                break;
                            }
                        }
                    }
                    return new UpdateInfo(tag, page, apk);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(UpdateInfo info) {
                if (info == null || info.tag.isEmpty()) {
                    updateStatus("No release found yet");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_PAGE)));
                    return;
                }
                boolean newer = !("v" + BuildConfig.VERSION_NAME).equalsIgnoreCase(info.tag)
                        && !BuildConfig.VERSION_NAME.equalsIgnoreCase(info.tag);
                String message = "Current: " + BuildConfig.VERSION_NAME + "\nLatest: " + info.tag;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(newer ? "Update available" : "Already current")
                        .setMessage(message)
                        .setNegativeButton("Release page", (d, w) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(info.page))))
                        .setPositiveButton(info.apk.isEmpty() ? "OK" : "Download APK", (d, w) -> {
                            if (!info.apk.isEmpty()) downloadApk(info.apk);
                        });
                builder.show();
                updateStatus("Update check complete");
            }
        }.execute();
    }

    private void downloadApk(String url) {
        try {
            if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
                startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())));
                Toast.makeText(this, "Allow installs from this app, then tap Check update again.", Toast.LENGTH_LONG).show();
                return;
            }
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Game Audio Silencer update");
            request.setDescription("Downloading latest beta APK");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "GameAudioSilencer-latest.apk");
            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadId = manager.enqueue(request);
            updateStatus("Downloading APK...");
        } catch (Exception e) {
            updateStatus("Download failed");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    private void openDownloadedApk(long id) {
        try {
            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));
            if (cursor != null && cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                    Uri uri = manager.getUriForDownloadedFile(id);
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(uri, "application/vnd.android.package-archive");
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(install);
                }
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            updateStatus("Open downloaded APK failed");
        }
    }

    private void showRootHelp() {
        new AlertDialog.Builder(this)
                .setTitle("Root permission required")
                .setMessage("この Beta の実ミュート機能は root の su 権限が必要です。root 端末で su ダイアログを許可してください。非 root 端末では Android の通常 API だけでは他アプリ単位の音声停止ができません。")
                .setPositiveButton("OK", null)
                .show();
    }

    private Set<String> getMutedPackages() {
        return new HashSet<>(prefs.getStringSet(KEY_MUTED, new HashSet<>()));
    }

    private void saveMuted(String packageName, boolean muted) {
        Set<String> set = getMutedPackages();
        if (muted) set.add(packageName); else set.remove(packageName);
        prefs.edit().putStringSet(KEY_MUTED, set).apply();
    }

    private String safePackage(String packageName) {
        if (!packageName.matches("[A-Za-z0-9_\\.]+")) throw new IllegalArgumentException("Bad package");
        return packageName;
    }

    private void updateStatus(String message) {
        if (status != null) status.setText(message);
        if (rootStatus != null) rootStatus.setText("Mode: root appops PLAY_AUDIO / Version: " + BuildConfig.VERSION_NAME);
    }

    private Button button(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(14);
        button.setOnClickListener(listener);
        return button;
    }

    private View spacer(int widthDp, int heightDp) {
        View view = new View(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(dp(widthDp), dp(heightDp)));
        return view;
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

    private static class UpdateInfo {
        final String tag;
        final String page;
        final String apk;

        UpdateInfo(String tag, String page, String apk) {
            this.tag = tag;
            this.page = page;
            this.apk = apk;
        }
    }

    private static class RootTask extends AsyncTask<String, Void, Boolean> {
        interface Callback {
            void onDone(boolean success);
        }

        private final Callback callback;

        RootTask(Callback callback) {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... commands) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());
                out.writeBytes(commands[0] + "\n");
                out.writeBytes("exit\n");
                out.flush();
                return process.waitFor() == 0;
            } catch (Exception e) {
                return false;
            } finally {
                if (process != null) process.destroy();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            callback.onDone(success);
        }
    }
}
