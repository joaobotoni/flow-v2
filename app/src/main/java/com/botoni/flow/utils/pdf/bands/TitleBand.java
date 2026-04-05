package com.botoni.flow.utils.pdf.bands;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;

import com.botoni.flow.utils.pdf.PdfColors;
import com.botoni.flow.utils.pdf.TextAlignment;

public class TitleBand extends TextBand {

    private static final float DEFAULT_FONT_SIZE  = 18f;
    private static final float SEPARATOR_PADDING  = 5f;
    private static final float SEPARATOR_HEIGHT   = 0.5f;

    private final boolean showSeparator;

    public TitleBand(String text) {
        this(text, TextAlignment.LEFT, true);
    }

    public TitleBand(String text, TextAlignment alignment, boolean showSeparator) {
        super(text, DEFAULT_FONT_SIZE, alignment,
                6f,
                showSeparator ? SEPARATOR_PADDING + SEPARATOR_HEIGHT + 8f : 6f);
        this.showSeparator = showSeparator;
    }

    @Override
    protected TextPaint buildPaint() {
        TextPaint paint = super.buildPaint();
        paint.setFakeBoldText(true);
        paint.setLetterSpacing(0.05f);
        return paint;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, float contentWidth) {
        super.draw(canvas, x, y, contentWidth);

        if (showSeparator) {
            float lineY = y + paddingTop + fontSize + SEPARATOR_PADDING;
            Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(PdfColors.RULE);
            linePaint.setStrokeWidth(SEPARATOR_HEIGHT);
            canvas.drawLine(x, lineY, x + contentWidth, lineY, linePaint);
        }
    }
}