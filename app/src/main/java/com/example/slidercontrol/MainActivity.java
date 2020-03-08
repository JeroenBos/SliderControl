package com.example.slidercontrol;

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
                    int orientation;
                    switch (Settings.Global.getInt(getContentResolver(), "three_Key_mode")) {
                        case 1:
                            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                            break;
                        case 2:
                            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                            break;
                        case 3:
                            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            break;
                        default:
                            return;
                    }
                    textView.setText(textView.getText() + String.valueOf(orientation));
                    self.setRequestedOrientation(orientation);
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

}
