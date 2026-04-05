package com.botoni.flow.utils.pdf;

public class PdfPageConfig {

    public final int pageWidth;
    public final int pageHeight;
    public final float marginLeft;
    public final float marginRight;
    public final float marginTop;
    public final float marginBottom;

    public PdfPageConfig(int pageWidth, int pageHeight,
                         float marginLeft, float marginRight,
                         float marginTop, float marginBottom) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
    }

    public static PdfPageConfig a4Portrait() {
        return new PdfPageConfig(595, 842, 50f, 50f, 50f, 50f);
    }

    public float contentWidth() {
        return pageWidth - marginLeft - marginRight;
    }
}
