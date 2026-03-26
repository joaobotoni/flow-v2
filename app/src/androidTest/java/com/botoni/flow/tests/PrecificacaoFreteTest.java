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
        int quantidadeCabecas = 100;
        double distanciaKm = 300.00;

        List<Transporte> transportes = transporteRepository.recomendarTransportes(1L, quantidadeCabecas);
        BigDecimal valorTotalFrete = freteRepository.calcularFreteTotal(transportes, distanciaKm);

        System.out.println("TRANSPORTES");
        transportes.forEach(t -> System.out.println("- " + t.getQuantidade() + "x " + t.getNomeVeiculo()));

        System.out.println("\nVALOR DO FRETE");
        System.out.println("Valor Calculado: R$ " + formatCurrency(valorTotalFrete));
    }

    @Test
    public void calcularIncidenciaFrete_DeveRetornarRelacaoEntreCustoFreteEValorDoGado() {
        BigDecimal peso = new BigDecimal("300");
        BigDecimal arroba = new BigDecimal("310");
        BigDecimal percentual = new BigDecimal("30");
        int quantidadeCabecas = 100;
        double distanciaKm = 300.00;
        int pesoTotal = quantidadeCabecas * peso.intValue();

        List<Transporte> transportes = transporteRepository.recomendarTransportes(1L, quantidadeCabecas);
        PrecificacaoBezerro bezerro = precificacaoBezerroRepository.calcularNegociacaoBezerro(peso, arroba, percentual, quantidadeCabecas);
        BigDecimal valorTotalFrete = freteRepository.calcularFreteTotal(transportes, distanciaKm);
        BigDecimal incidenciaFrete = freteRepository.calcularIncidenciaFretePorAnimal(valorTotalFrete, pesoTotal);

        System.out.println("TRANSPORTES (" + transportes.size() + " veículos usados)");
        transportes.forEach(t -> System.out.println("- " + t.getQuantidade() + "x " + t.getNomeVeiculo()));

        System.out.println("\nRESULTADO DO BEZERRO");
        System.out.println("Valor total bezerro: R$ " + formatCurrency(bezerro.getValorTotal()));

        System.out.println("\nRESULTADO DO FRETE");
        System.out.println("Valor total frete: R$ " + formatCurrency(valorTotalFrete));
        System.out.println("Valor da Incidência: R$ " + formatCurrency(incidenciaFrete));
    }
}
