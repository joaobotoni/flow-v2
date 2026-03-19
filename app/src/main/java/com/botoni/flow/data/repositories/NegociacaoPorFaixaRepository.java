package com.botoni.flow.data.repositories;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

public class NegociacaoPorFaixaRepository {
    private static final BigDecimal PESO_ARROBA_KG = new BigDecimal("30.0");
    private static final BigDecimal ARROBAS_ABATE_ESPERADAS = new BigDecimal("21.00");
    private static final BigDecimal PESO_BASE_KG = new BigDecimal("180.0");
    private static final BigDecimal TAXA_FIXA_ABATE = new BigDecimal("69.70");
    private static final BigDecimal IMPOSTO_FUNRURAL = new BigDecimal("0.015");
    private static final BigDecimal CEM = new BigDecimal("100");
    private static final int ESCALA_CALCULO = 15;
    private static final int ESCALA_RESULTADO = 2;
    private static final RoundingMode MODO_ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    @Inject
    public NegociacaoPorFaixaRepository() {
    }


    public BigDecimal calcularValorTotalBezerro(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        validarNulo(pesoKg, "O peso em kg não pode ser nulo");
        validarNulo(precoPorArroba, "O preço por arroba não pode ser nulo");
        validarNulo(percentualAgio, "O percentual de ágio não pode ser nulo");
        return calcularValorBasePorPeso(pesoKg, precoPorArroba)
                .add(calcularValorTotalAgio(pesoKg, precoPorArroba, percentualAgio))
                .setScale(ESCALA_RESULTADO, MODO_ARREDONDAMENTO);
    }

    public BigDecimal calcularValorPorKg(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        validarNulo(pesoKg, "O peso em kg não pode ser nulo");
        validarNulo(precoPorArroba, "O preço por arroba não pode ser nulo");
        validarNulo(percentualAgio, "O percentual de ágio não pode ser nulo");
        if (pesoKg.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return calcularValorTotalBezerro(pesoKg, precoPorArroba, percentualAgio)
                .divide(pesoKg, ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    public BigDecimal calcularValorTotalLote(BigDecimal valorUnitario, Integer quantidade) {
        validarNulo(valorUnitario, "O valor não pode ser nulo");
        validarNulo(quantidade, "A quantidade não pode ser nula");
        return valorUnitario
                .multiply(new BigDecimal(quantidade))
                .setScale(ESCALA_RESULTADO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal calcularValorTotalAgio(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        return estaNoOuAcimaPesoBase(pesoKg)
                ? calcularAgioAcimaPesoBase(pesoKg, precoPorArroba, percentualAgio)
                : calcularAgioAbaixoPesoBase(pesoKg, precoPorArroba, percentualAgio);
    }

    private BigDecimal calcularAgioAcimaPesoBase(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        return obterArrobasRestantesParaAbate(pesoKg)
                .multiply(calcularAgioPorArrobaNoPesoBase(pesoKg, precoPorArroba, percentualAgio));
    }

    private BigDecimal calcularAgioAbaixoPesoBase(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        BigDecimal acumulado = BigDecimal.ZERO;
        BigDecimal pesoAtual = pesoKg;
        while (pesoAtual.compareTo(PESO_BASE_KG) < 0) {
            acumulado = acumulado.add(calcularDiferencaAgioNoPeso(pesoAtual, precoPorArroba, percentualAgio));
            pesoAtual = calcularProximoPesoSuperior(pesoAtual);
        }
        return acumulado.add(calcularAgioAcimaPesoBase(PESO_BASE_KG, precoPorArroba, percentualAgio));
    }

    private BigDecimal calcularAgioPorArrobaNoPesoBase(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        return obterValorReferenciaAgioNoPesoBase(precoPorArroba, percentualAgio)
                .subtract(calcularTaxaPorArrobaRestante(pesoKg, precoPorArroba));
    }

    private BigDecimal calcularDiferencaAgioNoPeso(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        return obterValorReferenciaAgioNoPesoBase(precoPorArroba, percentualAgio)
                .subtract(calcularTaxaPorArrobaRestante(pesoKg, precoPorArroba));
    }

    private BigDecimal obterValorReferenciaAgioNoPesoBase(BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        return calcularTaxaPorArrobaRestante(PESO_BASE_KG, precoPorArroba)
                .add(calcularAgioPorArrobaExatamenteNoPesoBase(precoPorArroba, percentualAgio));
    }

    private BigDecimal calcularAgioPorArrobaExatamenteNoPesoBase(BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        return calcularValorAgioNoPesoBase(precoPorArroba, percentualAgio)
                .divide(obterArrobasRestantesParaAbate(PESO_BASE_KG), ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal calcularValorAgioNoPesoBase(BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        return calcularValorPesoBaseComAgio(precoPorArroba, percentualAgio)
                .subtract(converterKgParaArrobas(PESO_BASE_KG).multiply(precoPorArroba));
    }

    private BigDecimal calcularValorPesoBaseComAgio(BigDecimal precoPorArroba, BigDecimal percentualAgio) {
        return converterKgParaArrobas(PESO_BASE_KG)
                .multiply(precoPorArroba)
                .divide(obterFatorMultiplicadorAgio(percentualAgio), ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }


    private BigDecimal calcularValorBasePorPeso(BigDecimal pesoKg, BigDecimal precoPorArroba) {
        return converterKgParaArrobas(pesoKg).multiply(precoPorArroba);
    }

    private BigDecimal calcularTaxaPorArrobaRestante(BigDecimal pesoKg, BigDecimal precoPorArroba) {
        BigDecimal restantes = obterArrobasRestantesParaAbate(pesoKg);
        if (restantes.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return calcularTotalTaxasAbate(precoPorArroba)
                .divide(restantes, ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal calcularTotalTaxasAbate(BigDecimal precoPorArroba) {
        return ARROBAS_ABATE_ESPERADAS
                .multiply(precoPorArroba)
                .multiply(IMPOSTO_FUNRURAL)
                .add(TAXA_FIXA_ABATE);
    }

    private BigDecimal obterArrobasRestantesParaAbate(BigDecimal pesoKg) {
        return ARROBAS_ABATE_ESPERADAS.subtract(converterKgParaArrobas(pesoKg));
    }

    private BigDecimal converterKgParaArrobas(BigDecimal pesoKg) {
        return pesoKg.divide(PESO_ARROBA_KG, ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal obterFatorMultiplicadorAgio(BigDecimal percentualAgio) {
        return CEM.subtract(percentualAgio)
                .divide(CEM, ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal calcularProximoPesoSuperior(BigDecimal pesoKg) {
        BigDecimal proximoPeso = arredondarArrobasParaCima(converterKgParaArrobas(pesoKg))
                .multiply(PESO_ARROBA_KG);
        return limitarPesoAoPesoBase(proximoPeso);
    }

    private BigDecimal arredondarArrobasParaCima(BigDecimal arrobas) {
        BigDecimal arredondado = arrobas.setScale(0, RoundingMode.CEILING);
        return arredondado.compareTo(arrobas) == 0
                ? arredondado.add(BigDecimal.ONE)
                : arredondado;
    }

    private BigDecimal limitarPesoAoPesoBase(BigDecimal pesoKg) {
        return pesoKg.compareTo(PESO_BASE_KG) > 0 ? PESO_BASE_KG : pesoKg;
    }

    private boolean estaNoOuAcimaPesoBase(BigDecimal pesoKg) {
        return pesoKg.compareTo(PESO_BASE_KG) >= 0;
    }

    private void validarNulo(Object valor, String mensagem) {
        if (valor == null) throw new IllegalArgumentException(mensagem);
    }
}