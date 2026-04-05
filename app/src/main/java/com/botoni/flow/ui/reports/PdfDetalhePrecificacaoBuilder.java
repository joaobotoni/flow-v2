package com.botoni.flow.ui.reports;

import android.content.Context;

import androidx.annotation.NonNull;

import com.botoni.flow.ui.helpers.NumberHelper;
import com.botoni.flow.ui.state.DetalhePrecoBezerroUiState;
import com.botoni.flow.utils.pdf.PdfGenerator;
import com.botoni.flow.utils.pdf.PdfPageConfig;
import com.botoni.flow.utils.pdf.TextAlignment;
import com.botoni.flow.utils.pdf.bands.FooterBand;
import com.botoni.flow.utils.pdf.bands.RowBand;
import com.botoni.flow.utils.pdf.bands.SpacerBand;
import com.botoni.flow.utils.pdf.bands.TextBand;
import com.botoni.flow.utils.pdf.bands.TitleBand;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfDetalhePrecificacaoBuilder {

    private PdfDetalhePrecificacaoBuilder() {
    }

    @NonNull
    public static File gerarRelatorioPrecificacao(@NonNull Context context,
                                                  @NonNull List<DetalhePrecoBezerroUiState> itens,
                                                  @NonNull BigDecimal valorTotal
    ) throws IOException {

        String dataGeracao = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR")).format(new Date());
        PdfGenerator generator = new PdfGenerator(PdfPageConfig.a4Portrait());
        generator.setFooter(new FooterBand("Flow — Precificação  •  " + dataGeracao));

        generator.addBand(new TitleBand("Relatório de Precificação de Bezerros"));
        generator.addBand(new SpacerBand(10f));
        generator.addBand(new TextBand("Data: " + dataGeracao, 10f, TextAlignment.LEFT));
        generator.addBand(new TextBand("Total de animais: " + itens.size(), 10f, TextAlignment.LEFT));
        generator.addBand(new SpacerBand(14f));

        generator.addBand(new RowBand(11f, 26f,
                new RowBand.Column("#", 0.5f, TextAlignment.CENTER),
                new RowBand.Column("Peso (kg)", 1.5f, TextAlignment.CENTER),
                new RowBand.Column("R$/kg", 1.5f, TextAlignment.RIGHT),
                new RowBand.Column("Total (R$)", 1.5f, TextAlignment.RIGHT)
        ).asHeader());

        int numero = 1;
        for (DetalhePrecoBezerroUiState item : itens) {
            generator.addBand(new RowBand(10f, 22f,
                    new RowBand.Column(String.valueOf(numero), 0.5f, TextAlignment.CENTER),
                    new RowBand.Column(item.getPeso().toPlainString() + " kg", 1.5f, TextAlignment.CENTER),
                    new RowBand.Column(NumberHelper.formatCurrency(item.getValorPorKg()), 1.5f, TextAlignment.RIGHT),
                    new RowBand.Column(NumberHelper.formatCurrency(item.getValorTotal()), 1.5f, TextAlignment.RIGHT)
            ));
            numero++;
        }

        generator.addBand(new SpacerBand(10f));
        generator.addBand(new RowBand(12f, 26f,
                new RowBand.Column("TOTAL GERAL", 3.5f, TextAlignment.RIGHT),
                new RowBand.Column(NumberHelper.formatCurrency(valorTotal), 1.5f, TextAlignment.RIGHT)
        ));

        String nomeArquivo = "precificacao_" + System.currentTimeMillis() + ".pdf";
        return generator.generate(context, nomeArquivo);
    }
}
