package com.example.slidercontrol;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Date current = new Date();

    LinearLayout orientationChanger;
    LayoutParams orientationLayout;
    WindowManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createOverlayView();

        n();
    }

    void createOverlayView() {
        orientationChanger = new LinearLayout(this);
        orientationChanger.setClickable(false);
        orientationChanger.setFocusable(false);
        orientationChanger.setFocusableInTouchMode(false);
        orientationChanger.setLongClickable(false);


// Using TYPE_SYSTEM_OVERLAY is crucial to make your window appear on top
// You'll need the permission android.permission.SYSTEM_ALERT_WINDOW
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
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            case 3:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            default:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    void n() {

        // Make a listener
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                int threeKeyMode = readThreeKeyMode();
                int rotation = mapThreeKeyModeToRotation(threeKeyMode);

                setRotation(rotation);
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
