package com.example.slidercontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.function.Supplier;


public class MainActivity extends AppCompatActivity {

    LinearLayout orientationChanger;
    LayoutParams orientationLayout;
    WindowManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ask for permission to start the app on boot:
        askForPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED, 123);
        // ask for permission to draw over other windows:
        boolean hasPermission = askForPermission(Manifest.permission.SYSTEM_ALERT_WINDOW, 1234);
        if (!hasPermission)
            return;

        createOverlayView();

        registerThreeKeyModeListener();

        readAndRotate();
    }

    boolean askForPermission(String permission, int uniqueRequestId) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, uniqueRequestId);

            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1234) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
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
