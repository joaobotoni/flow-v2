package com.botoni.flow.utils.pdf;

import android.graphics.Canvas;

public interface PdfBand {

    float getHeight();
    void draw(Canvas canvas, float x, float y, float contentWidth);
}
