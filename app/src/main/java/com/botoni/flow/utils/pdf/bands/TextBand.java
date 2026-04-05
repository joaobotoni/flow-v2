package com.botoni.flow.utils.pdf.bands;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.botoni.flow.utils.pdf.PdfBand;
import com.botoni.flow.utils.pdf.PdfColors;
import com.botoni.flow.utils.pdf.TextAlignment;

public class TextBand implements PdfBand {

    protected final String text;
    protected final float fontSize;
    protected final TextAlignment alignment;
    protected final float paddingTop;
    protected final float paddingBottom;

    public TextBand(String text, float fontSize, TextAlignment alignment) {
        this(text, fontSize, alignment, 4f, 4f);
    }

    public TextBand(String text, float fontSize, TextAlignment alignment,
                    float paddingTop, float paddingBottom) {
        this.text = text;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }

    @Override
    public float getHeight() {
        return fontSize + paddingTop + paddingBottom;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, float contentWidth) {
        TextPaint paint = buildPaint();

        StaticLayout layout = StaticLayout.Builder
                .obtain(text, 0, text.length(), paint, (int) contentWidth)
                .setAlignment(toLayoutAlignment(alignment))
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(false)
                .build();

        canvas.save();
        canvas.translate(x, y + paddingTop);
        layout.draw(canvas);
        canvas.restore();
    }

    protected TextPaint buildPaint() {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setTextSize(fontSize);
        paint.setColor(PdfColors.GRAPHITE);
        return paint;
    }

    protected Layout.Alignment toLayoutAlignment(TextAlignment alignment) {
        switch (alignment) {
            case CENTER:
                return Layout.Alignment.ALIGN_CENTER;
            case RIGHT:
                return Layout.Alignment.ALIGN_OPPOSITE;
            default:
                return Layout.Alignment.ALIGN_NORMAL;
        }
    }
}