package com.botoni.flow.utils.pdf.bands;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.botoni.flow.utils.pdf.PdfBand;
import com.botoni.flow.utils.pdf.PdfColors;
import com.botoni.flow.utils.pdf.TextAlignment;

import java.util.Arrays;
import java.util.List;

public class RowBand implements PdfBand {

    private static final float COL_PADDING_H = 4f;

    private final float fontSize;
    private final float height;
    private final List<Column> columns;
    private boolean isHeader = false;

    public RowBand(float fontSize, float height, Column... columns) {
        this.fontSize = fontSize;
        this.height = height;
        this.columns = Arrays.asList(columns);
    }

    public RowBand asHeader() {
        this.isHeader = true;
        return this;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, float contentWidth) {
        float totalWeight = totalWeight();
        if (totalWeight == 0f) return;

        if (isHeader) {
            drawBackground(canvas, x, y, contentWidth);
            drawHorizonLine(canvas, x, y, contentWidth);
        }

        float cursorX = x;
        for (Column col : columns) {
            float colWidth = (col.weight / totalWeight) * contentWidth;
            float textWidth = Math.max(0f, colWidth - 2f * COL_PADDING_H);

            TextPaint paint = buildColumnPaint();
            if (isHeader) paint.setFakeBoldText(true);

            StaticLayout layout = StaticLayout.Builder
                    .obtain(col.text, 0, col.text.length(), paint, (int) textWidth)
                    .setAlignment(toLayoutAlignment(col.alignment))
                    .setIncludePad(false)
                    .build();

            float topOffset = (height - layout.getHeight()) / 2f;

            canvas.save();
            canvas.translate(cursorX + COL_PADDING_H, y + Math.max(0f, topOffset));
            layout.draw(canvas);
            canvas.restore();

            cursorX += colWidth;
        }

        drawHorizonLine(canvas, x, y + height, contentWidth);
    }

    private void drawBackground(Canvas canvas, float x, float y, float contentWidth) {
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(PdfColors.SURFACE);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(x, y, x + contentWidth, y + height), 2f, 2f, bgPaint);
    }

    private void drawHorizonLine(Canvas canvas, float x, float lineY, float contentWidth) {
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(PdfColors.RULE);
        linePaint.setStrokeWidth(0.5f);
        canvas.drawLine(x, lineY, x + contentWidth, lineY, linePaint);
    }

    private TextPaint buildColumnPaint() {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setTextSize(fontSize);
        paint.setColor(PdfColors.GRAPHITE);
        return paint;
    }

    private Layout.Alignment toLayoutAlignment(TextAlignment alignment) {
        switch (alignment) {
            case CENTER: return Layout.Alignment.ALIGN_CENTER;
            case RIGHT:  return Layout.Alignment.ALIGN_OPPOSITE;
            default:     return Layout.Alignment.ALIGN_NORMAL;
        }
    }

    private float totalWeight() {
        float total = 0f;
        for (Column col : columns) total += col.weight;
        return total;
    }

    public static class Column {

        final String text;
        final float weight;
        final TextAlignment alignment;

        public Column(String text, float weight, TextAlignment alignment) {
            this.text = text;
            this.weight = weight;
            this.alignment = alignment;
        }
    }
}