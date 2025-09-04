package com.example.privacyshield;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.InputStream;

public class OCRActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private TextView resultTextView;
    private Button uploadButton, copyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        resultTextView = findViewById(R.id.resultTextView);
        uploadButton = findViewById(R.id.uploadButton);
        copyButton = findViewById(R.id.buttonCopy);

        uploadButton.setOnClickListener(v -> showImagePickerOptions());

        copyButton.setOnClickListener(v -> {
            String text = resultTextView.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("OCR Text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show();
        });
    }

    private void showImagePickerOptions() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap bitmap = null;
            Uri imageUri = data.getData();

            if (imageUri != null) {
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(imageStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (bitmap == null) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.test_face);
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }

            runTextRecognitionFromBitmap(bitmap);
        }
    }

    private void runTextRecognitionFromBitmap(Bitmap bitmap) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String resultText = visionText.getText();
                        if (resultText.isEmpty()) {
                            resultTextView.setText("No text detected");
                        } else {
                            resultTextView.setText(resultText);
                            copyButton.setVisibility(Button.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> resultTextView.setText("OCR failed: " + e.getMessage()));
        } catch (Exception e) {
            resultTextView.setText("Error: " + e.getMessage());
        }
    }
}
