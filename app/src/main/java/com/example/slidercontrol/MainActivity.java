package com.example.slidercontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;


public class MainActivity extends AppCompatActivity {

    LinearLayout orientationChanger;
    LayoutParams orientationLayout;
    WindowManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addHomeScreenShortcut();

        createOverlayView();

        registerThreeKeyModeListener();

        readAndRotate();
    }

    void addHomeScreenShortcut() {
        try {
            if (!getSharedPreferences("APP_PREFERENCE", Activity.MODE_PRIVATE).getBoolean("IS_ICON_CREATED", false)) {

                Context context = getApplicationContext();
                ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
                if (shortcutManager.isRequestPinShortcutSupported()) {
                    ShortcutInfo pinShortcutInfo = createShortcutInfo(this);
                    Intent resultIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, resultIntent, 0);
                    shortcutManager.requestPinShortcut(pinShortcutInfo, pendingIntent.getIntentSender());
                }

                getSharedPreferences("APP_PREFERENCE", Activity.MODE_PRIVATE).edit().putBoolean("IS_ICON_CREATED", true).apply();
            }
        } catch (Exception e) {
        }
    }

    private ShortcutInfo createShortcutInfo(Context context) {
        return new ShortcutInfo.Builder(context, getString(R.string.app_name))
                .setShortLabel(getString(R.string.app_name))
                .setLongLabel(getString(R.string.app_name))
                .setIcon(Icon.createWithResource(context, R.drawable.ic_launcher_foreground))
                .setIntent(new Intent(this, MainActivity.class).setAction(Intent.ACTION_VIEW))
                .build();
    }

    void createOverlayView() {
        orientationChanger = new LinearLayout(this);
        orientationChanger.setClickable(false);
        orientationChanger.setFocusable(false);
        orientationChanger.setFocusableInTouchMode(false);
        orientationChanger.setLongClickable(false);


        orientationLayout = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888);


        wm = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);
        wm.addView(orientationChanger, orientationLayout);
        orientationChanger.setVisibility(View.VISIBLE);
    }

    void setRotation(int rotation) {
        orientationLayout.screenOrientation = rotation;
        wm.updateViewLayout(orientationChanger, orientationLayout);
    }

    int readThreeKeyMode() {
        try {
            return Settings.Global.getInt(getContentResolver(), "three_Key_mode");
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }

    int mapThreeKeyModeToRotation(int threeKeyMode) {
        switch (threeKeyMode) {
            case 1:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            case 2:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            case 3:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            default:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    void readAndRotate() {
        int MIDDLE_THREE_KEY_MODE = 2;
        int threeKeyMode = readThreeKeyMode();
        if (threeKeyMode == MIDDLE_THREE_KEY_MODE) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threeKeyMode = readThreeKeyMode();
        }

        int rotation = mapThreeKeyModeToRotation(threeKeyMode);

        setRotation(rotation);
    }

    void registerThreeKeyModeListener() {

        // Make a listener
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                readAndRotate();
            }

            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }
        };

        // Start listening
        ContentResolver contentResolver = getContentResolver();
        Uri setting = Settings.Global.getUriFor("three_Key_mode");
        contentResolver.registerContentObserver(setting, false, observer);
    }

}
