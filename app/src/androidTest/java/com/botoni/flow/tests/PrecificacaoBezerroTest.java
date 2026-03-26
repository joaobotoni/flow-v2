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
    public void init() {
        hiltRule.inject();
    }

    @Test
    public void calcularNegociacaoBezerroComDescontoDoFrete_deveRetornarValoresCorretos() {
        BigDecimal peso = new BigDecimal("330");
        BigDecimal arroba = new BigDecimal("310");
        BigDecimal percentual = new BigDecimal("30");
        int quantidadeCabecas = 100;
        double distanciaKm = 300.00;
        int pesoTotal = quantidadeCabecas * peso.intValue();

        List<Transporte> transportes = transporteRepository.recomendarTransportes(1L, quantidadeCabecas);

        PrecificacaoBezerro bezerroSemFrete = precificacaoBezerroRepository.calcularNegociacaoBezerro(peso, arroba, percentual, quantidadeCabecas);
        BigDecimal valorTotalFrete = freteRepository.calcularFreteTotal(transportes, distanciaKm);
        BigDecimal incidenciaFrete = freteRepository.calcularIncidenciaFretePorAnimal(valorTotalFrete, pesoTotal);

        PrecificacaoBezerro bezerroComFrete = precificacaoBezerroRepository.calcularNegociacaoBezerroComDescontoDoFrete(peso, arroba, percentual, quantidadeCabecas, incidenciaFrete);

        System.out.println("RESUMO DA NEGOCIAÇÃO (ANTES DO FRETE)");
        System.out.println("Veículos Usados: " + transportes.size());
        System.out.println("Valor Total Bezerro: R$ " + formatCurrency(bezerroSemFrete.getValorTotal()));
        System.out.println("Valor por Cabeça: R$ " + formatCurrency(bezerroSemFrete.getValorPorCabeca()));
        System.out.println("Valor por KG: R$ " + formatCurrency(bezerroSemFrete.getValorPorKg()));
        System.out.println("Custo Total Frete: R$ " + formatCurrency(valorTotalFrete));
        System.out.println("Incidência (Desc): R$ " + formatCurrency(incidenciaFrete));

        System.out.println("\nRESULTADO FINAL (COM DESCONTO DO FRETE)");
        System.out.println("Valor Total Líquido: R$ " + formatCurrency(bezerroComFrete.getValorTotal()));
        System.out.println("Valor por Cabeça: R$ " + formatCurrency(bezerroComFrete.getValorPorCabeca()));
        System.out.println("Valor por KG: R$ " + formatCurrency(bezerroComFrete.getValorPorKg()));
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

        System.out.println("CÁLCULO DE INCIDÊNCIA DE FRETE");
        System.out.println("LOGÍSTICA (" + transportes.size() + " veículos usados):");
        transportes.forEach(t -> System.out.println("- " + t.getQuantidade() + "x " + t.getNomeVeiculo()));
        System.out.println("Valor Total Gado: R$ " + formatCurrency(bezerro.getValorTotal()));
        System.out.println("Custo Total Frete: R$ " + formatCurrency(valorTotalFrete));
        System.out.println("Valor da Incidência: R$ " + formatCurrency(incidenciaFrete));
    }
}
