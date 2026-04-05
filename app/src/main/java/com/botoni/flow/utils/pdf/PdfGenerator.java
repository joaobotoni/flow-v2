package com.botoni.flow.utils.pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfGenerator {
    private final PdfPageConfig config;
    private final List<PdfBand> bands = new ArrayList<>();

    @Nullable
    private PdfBand footer;

    public PdfGenerator(@NonNull PdfPageConfig config) {
        this.config = config;
    }

    public PdfGenerator addBand(@NonNull PdfBand band) {
        bands.add(band);
        return this;
    }

    public PdfGenerator setFooter(@Nullable PdfBand footer) {
        this.footer = footer;
        return this;
    }

    @NonNull
    public File generate(@NonNull Context context, @NonNull String fileName) throws IOException {
        PdfDocument document = new PdfDocument();

        int totalPages    = countTotalPages();
        float footerHeight = footer != null ? footer.getHeight() : 0f;
        float usableBottom = config.pageHeight - config.marginBottom - footerHeight;

        int pageNumber = 1;
        PdfDocument.Page currentPage = startPage(document, pageNumber);
        Canvas canvas = currentPage.getCanvas();
        float currentY = config.marginTop;

        for (PdfBand band : bands) {
            if (currentY + band.getHeight() > usableBottom) {
                drawFooterOnPage(canvas, footerHeight, pageNumber, totalPages);
                document.finishPage(currentPage);

                pageNumber++;
                currentPage = startPage(document, pageNumber);
                canvas = currentPage.getCanvas();
                currentY = config.marginTop;
            }

            band.draw(canvas, config.marginLeft, currentY, config.contentWidth());
            currentY += band.getHeight();
        }

        drawFooterOnPage(canvas, footerHeight, pageNumber, totalPages);
        document.finishPage(currentPage);

        File outputFile = resolveOutputFile(context, fileName);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            document.writeTo(fos);
        } finally {
            document.close();
        }

        return outputFile;
    }

    private int countTotalPages() {
        float footerHeight = footer != null ? footer.getHeight() : 0f;
        float usableHeight = config.pageHeight - config.marginTop - config.marginBottom - footerHeight;

        int pages = 1;
        float currentY = 0f;
        for (PdfBand band : bands) {
            if (currentY + band.getHeight() > usableHeight) {
                pages++;
                currentY = 0f;
            }
            currentY += band.getHeight();
        }
        return pages;
    }

    private PdfDocument.Page startPage(PdfDocument document, int pageNumber) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                config.pageWidth, config.pageHeight, pageNumber
        ).create();
        return document.startPage(pageInfo);
    }

    private void drawFooterOnPage(Canvas canvas, float footerHeight,
                                   int currentPage, int totalPages) {
        if (footer == null || footerHeight == 0f) return;
        if (footer instanceof PageAware) {
            ((PageAware) footer).setPageInfo(currentPage, totalPages);
        }
        float footerY = config.pageHeight - config.marginBottom - footerHeight;
        footer.draw(canvas, config.marginLeft, footerY, config.contentWidth());
    }

    private File resolveOutputFile(Context context, String fileName) throws IOException {
        File dir = new File(context.getFilesDir(), "pdfs");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Não foi possível criar o diretório: " + dir.getAbsolutePath());
        }
        return new File(dir, fileName);
    }
}