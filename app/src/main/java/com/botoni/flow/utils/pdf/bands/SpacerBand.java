package com.botoni.flow.utils.pdf.bands;

import android.graphics.Canvas;

import com.botoni.flow.utils.pdf.PdfBand;

public class SpacerBand implements PdfBand {

    private final float height;

    public SpacerBand(float height) {
        this.height = height;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, float contentWidth) {}
}
