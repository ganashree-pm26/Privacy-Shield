package com.example.privacyshield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

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
    }
}
