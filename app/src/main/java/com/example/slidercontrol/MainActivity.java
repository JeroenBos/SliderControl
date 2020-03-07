package com.example.slidercontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileSystem fs = FileSystems.getDefault();
        Path path = fs.getPath("/sys/class/switch/tri-state-key/state");
        String contents;
        try {
            contents = Files.readAllLines(path).get(0);
        } catch (IOException ex) {
            contents = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }

        setContentView(R.layout.activity_main);


        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(contents);

    }
}
