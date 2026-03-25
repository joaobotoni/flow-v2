package com.botoni.flow.tests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.botoni.flow.data.models.PrecificacaoBezerro;
import com.botoni.flow.data.repositories.FreteRepository;
import com.botoni.flow.data.repositories.PrecificacaoBezerroRepository;
import com.botoni.flow.data.repositories.TransporteRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class PrecificacaoBezerroTest {

    @Inject
    FreteRepository freteRepository;
    @Inject
    TransporteRepository transporteRepository;
    @Inject
    PrecificacaoBezerroRepository precificacaoBezerroRepository;
    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(LOCALE_BR);
    public static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00", SYMBOLS);
    @NonNull
    public static String formatCurrency(@Nullable BigDecimal value) {
        if (value == null) return "0,00";
        return CURRENCY_FORMAT.format(value);
    }

    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Before
    public void init() { hiltRule.inject(); }

    @Test
    public void calcularNegociacaoBezerro_deveRetornarValoresCorretos() {
        BigDecimal peso = new BigDecimal("180");
        BigDecimal arroba = new BigDecimal("310");
        BigDecimal percent = new BigDecimal("30");
        Integer quantidade = 10;

        PrecificacaoBezerro result = precificacaoBezerroRepository.calcularNegociacaoBezerro(peso, arroba, percent, quantidade);
        System.out.println("\nPRECIFICAÇÃO BEZERRO");
        System.out.printf("\nPor kg: R$ %s%n", formatCurrency(result.getValorPorKg()));
        System.out.printf("\nPor cabeça: R$ %s%n", formatCurrency(result.getValorPorCabeca()));
        System.out.printf("\nTotal: R$ %s%n", formatCurrency(result.getValorTotal()));
    }
}
