package com.example.privacyshield;

import android.graphics.Rect;

public class DetectResult {
    private final String text;
    private final String type;
    private final boolean sensitive;
    private final Rect boundingBox;

    public DetectResult(String type, String text, boolean sensitive, Rect boundingBox) {
        this.type = type;
        this.text = text;
        this.sensitive = sensitive;
        this.boundingBox = boundingBox;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }
}
