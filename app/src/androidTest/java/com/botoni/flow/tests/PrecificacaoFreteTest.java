package com.botoni.flow.tests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.botoni.flow.data.models.PrecificacaoBezerro;
import com.botoni.flow.data.models.Transporte;
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
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;


@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class PrecificacaoFreteTest {
    @Rule
    public HiltAndroidRule hiltRule = new HiltAndroidRule(this);
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

    @Before
    public void setup() {
        hiltRule.inject();
    }

    @Test
    public void calcularFrete_deveRetornarValorDoFrete() {
        List<Transporte> transportes = transporteRepository.recomendarTransportes(1L, 20);
        double distancia = 110.00;

        BigDecimal valorTotalFrete = freteRepository.calcularFreteTotal(transportes, distancia);
        System.out.println("RESULTADO DO FRETE");
        System.out.println("Transportes: " + transportes.size());
        System.out.println("Valor Calculado: R$ " + formatCurrency(valorTotalFrete));
    }

    @Test
    public void calcularIncidenciaFrete_DeveRetornarRelacaoEntreCustoFreteEValorDoGado(){
        BigDecimal peso = new BigDecimal("300");
        BigDecimal arroba = new BigDecimal("310");
        BigDecimal percent = new BigDecimal("30");
        int quantidade = 10;
        double distancia = 110.00;
        List<Transporte> transportes = transporteRepository.recomendarTransportes(1L, quantidade);

        PrecificacaoBezerro result = precificacaoBezerroRepository.calcularNegociacaoBezerro(peso, arroba, percent, quantidade);
        BigDecimal valorTotalFrete = freteRepository.calcularFreteTotal(transportes, distancia);
        BigDecimal valorIncidenete = freteRepository.calcularIncidenciaFretePorAnimal(valorTotalFrete, result.getValorTotal());

        System.out.println("RESULTADO DO FRETE");
        System.out.println("Transportes: " + transportes.size());
        System.out.println("Valor total bezerro: " + formatCurrency(result.getValorTotal()));
        System.out.println("Valor total frete: " + formatCurrency(valorTotalFrete));
        System.out.println("Valor Calculado: R$ " + formatCurrency(valorIncidenete));
    }
}
