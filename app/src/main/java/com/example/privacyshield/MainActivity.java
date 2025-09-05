package com.example.privacyshield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

public class MainActivity extends AppCompatActivity {

    private Interpreter yoloInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigate to Face + OCR detection
        Button btnFaceOcr = findViewById(R.id.btnFaceOcr);
        btnFaceOcr.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FaceOcrActivity.class);
            startActivity(intent);
        });

        try {
            YoloV8Helper helper = new YoloV8Helper(this);
            yoloInterpreter = helper.getInterpreter();
            Log.d("YOLO", "✅ YOLOv8 model loaded successfully!");
        } catch (Exception e) {
            Log.e("YOLO", "❌ Failed to load YOLOv8 model", e);
        }
    }
}

