package com.example.slidercontrol;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Surface;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        String s = Settings.Global.getString(getContentResolver(), "three_Key_mode");

        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(contents + "\n" + s + "\n" + String.valueOf(this.current.getTime()));
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
                            rotation= Surface.ROTATION_0;
                            break;
                        case 2:
                            rotation= Surface.ROTATION_270;
                            break;
                        case 3:
                            rotation= Surface.ROTATION_90;
                            break;
                        default:
                            return;
                    }
                    textView.setText(textView.getText() + String.valueOf(rotation));
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
