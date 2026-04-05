package com.botoni.flow.utils.pdf.bands;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;

import com.botoni.flow.utils.pdf.PageAware;
import com.botoni.flow.utils.pdf.PdfBand;
import com.botoni.flow.utils.pdf.PdfColors;

import java.util.Locale;


public class FooterBand implements PdfBand, PageAware {

    private static final float FONT_SIZE = 8f;
    private static final float RULE_MARGIN = 6f;
    private static final float TEXT_PADDING = 5f;
    private static final float TOTAL_HEIGHT = RULE_MARGIN + 0.5f + TEXT_PADDING + FONT_SIZE + 4f;

    private final String documentId;
    private int currentPage = 1;
    private int totalPages = 1;

    public FooterBand(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public void setPageInfo(int currentPage, int totalPages) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    @Override
    public float getHeight() {
        return TOTAL_HEIGHT;
    }

    @Override
    public void draw(Canvas canvas, float x, float y, float contentWidth) {
        float ruleY = y + RULE_MARGIN;

        Paint rulePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rulePaint.setColor(PdfColors.RULE);
        rulePaint.setStrokeWidth(0.5f);
        canvas.drawLine(x, ruleY, x + contentWidth, ruleY, rulePaint);

        float baseline = ruleY + 0.5f + TEXT_PADDING + FONT_SIZE;

        TextPaint leftPaint = buildPaint();
        leftPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(documentId, x, baseline, leftPaint);

        String pageLabel = String.format(Locale.getDefault(),
                "Pág. %02d / %02d", currentPage, totalPages);
        TextPaint rightPaint = buildPaint();
        rightPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(pageLabel, x + contentWidth, baseline, rightPaint);
    }

    private TextPaint buildPaint() {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setTextSize(FONT_SIZE);
        paint.setColor(PdfColors.SECONDARY);
        return paint;
    }
}
