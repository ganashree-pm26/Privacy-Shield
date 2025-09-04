package com.example.privacyshield;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class FaceOcrActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView imageView;
    private TextView resultTextView, faceCountText;
    private Button selectImageBtn, copyTextBtn;
    private Bitmap selectedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_ocr);

        // Bind views from XML
        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        faceCountText = findViewById(R.id.faceCountText);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        copyTextBtn = findViewById(R.id.copyTextBtn);

        selectImageBtn.setOnClickListener(v -> openGallery());

        copyTextBtn.setOnClickListener(v -> {
            String text = resultTextView.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("OCR Text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text copied!", Toast.LENGTH_SHORT).show();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                detectFacesAndText(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void detectFacesAndText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        // Face detector
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build();

        FaceDetector detector = FaceDetection.getClient(options);

        // OCR recognizer
        com.google.mlkit.vision.text.TextRecognizer recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    recognizer.process(image)
                            .addOnSuccessListener(visionText -> {
                                processResults(faces, visionText, bitmap);
                            })
                            .addOnFailureListener(e -> resultTextView.setText("OCR failed: " + e.getMessage()));
                })
                .addOnFailureListener(e -> resultTextView.setText("Face detection failed: " + e.getMessage()));
    }

    private void processResults(List<Face> faces, Text visionText, Bitmap originalBitmap) {
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        StringBuilder sb = new StringBuilder();
        sb.append("Faces detected: ").append(faces.size()).append("\n\n");
        sb.append("Faces + OCR Results:\n\n");

        // Draw face boxes
        for (Face face : faces) {
            RectF rect = new RectF(face.getBoundingBox());
            canvas.drawRect(rect, paint);
        }

        // ✅ Correct way: get text blocks
        List<Text.TextBlock> blocks = visionText.getTextBlocks();
        for (Text.TextBlock block : blocks) {
            String blockText = block.getText();
            sb.append(maskSensitiveData(blockText)).append("\n");
        }

        resultTextView.setText(sb.toString());
        faceCountText.setText("Faces detected: " + faces.size());
        imageView.setImageBitmap(mutableBitmap);
    }

    // 🚨 Sensitive data masking
    private String maskSensitiveData(String text) {
        // Aadhaar, PAN, phone numbers, DOB, OTP
        if (Pattern.matches(".*\\b\\d{12}\\b.*", text)) {
            return text + " [Sensitive: Aadhaar?]";
        } else if (Pattern.matches(".*\\b[A-Z]{5}[0-9]{4}[A-Z]\\b.*", text)) {
            return text + " [Sensitive: PAN?]";
        } else if (Pattern.matches(".*\\b\\d{10}\\b.*", text)) {
            return text + " [Sensitive: Phone?]";
        } else if (Pattern.matches(".*\\b\\d{2}/\\d{2}/\\d{4}\\b.*", text)) {
            return text + " [Sensitive: DOB?]";
        } else if (text.toLowerCase().contains("otp")) {
            return text + " [Sensitive: OTP?]";
        }
        return text + " [Normal]";
    }
}
