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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        orientationChanger = new LinearLayout(this);
        orientationChanger.setClickable(false);
        orientationChanger.setFocusable(false);
        orientationChanger.setFocusableInTouchMode(false);
        orientationChanger.setLongClickable(false);


// Using TYPE_SYSTEM_OVERLAY is crucial to make your window appear on top
// You'll need the permission android.permission.SYSTEM_ALERT_WINDOW
        LayoutParams orientationLayout = new LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                0,
                PixelFormat.RGBA_8888);
// Use whatever constant you need for your desired rotation
        orientationLayout.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

        WindowManager wm = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);
        wm.addView(orientationChanger, orientationLayout);
        orientationChanger.setVisibility(View.VISIBLE);

    }

    void n() {

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
//        Intent mode = new Intent("com.oem.intent.action.THREE_KEY_MODE");
        FileSystem fs = FileSystems.getDefault();
        Path path = fs.getPath("/sys/class/switch/tri-state-key/state");
        String contents;
        try {
            contents = Files.readAllLines(path).get(0);
        } catch (IOException ex) {
            contents = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
        setContentView(R.layout.activity_main);
//        String s = "";
//        try {
//            Settings.System.putInt(getContentResolver(), "user_rotation", 1);
//        } catch (Exception e) {
//            s += e.getClass().getSimpleName() + " " + e.getMessage();
//
//        }
//        //String s = Settings.Global.getString(getContentResolver(), "USER_ROTATION");
//        s += "\nUSER_ROTATION: " + Settings.System.getString(getContentResolver(), "user_rotation");


        //   TextView textView = (TextView) findViewById(R.id.textview);
        // textView.setText(contents + "\n" + s + "\n" + String.valueOf(this.current.getTime()));


        //  this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);


        ContentResolver contentResolver = getContentResolver();
        Uri setting = Settings.Global.getUriFor("three_Key_mode");
        final Activity self = this;
        // Make a listener
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                TextView textView = (TextView) findViewById(R.id.textview);
                try {
                    int rotation;
                    switch (Settings.Global.getInt(getContentResolver(), "three_Key_mode")) {
                        case 1:
                            rotation = Surface.ROTATION_0;
                            break;
                        case 2:
                            rotation = Surface.ROTATION_270;
                            break;
                        case 3:
                            rotation = Surface.ROTATION_90;
                            break;
                        default:
                            return;
                    }
                    textView.setText(textView.getText() + "\n" + String.valueOf(rotation));
                    // Ensure Android screen auto-rotation is disabled
                    Settings.System.putInt(
                            getContentResolver(),
                            Settings.System.ACCELEROMETER_ROTATION,
                            0
                    );
                    //
                    Settings.System.putInt(
                            getContentResolver(),
                            Settings.System.USER_ROTATION,
                            rotation //Or a different ROTATION_ constant
                    );
//                    self.setRequestedOrientation(orientation);
                    // self.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                } catch (Settings.SettingNotFoundException e) {
                    textView.setText("Setting not found");
                }

            }

            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }
        };

// Start listening

        contentResolver.registerContentObserver(setting, false, observer);

// Stop listening
//        contentResolver.unregisterContentObserver(observer);

    }

    public void exit(View view) {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }
}
