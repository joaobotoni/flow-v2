package com.botoni.flow.domain.usecases;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

public class AvaliacaoPrecoAnimalUseCase {
    private final BigDecimal PESO_ARROBA_KG = new BigDecimal("30.0");
    private final BigDecimal ARROBAS_ABATE_ESPERADAS = new BigDecimal("21.00");
    private final BigDecimal PESO_BASE_KG = new BigDecimal("180.0");
    private final BigDecimal TAXA_FIXA_ABATE = new BigDecimal("69.70");
    private final BigDecimal IMPOSTO_FUNRURAL = new BigDecimal("0.015");
    private final BigDecimal CEM = new BigDecimal("100");
    private final int ESCALA_CALCULO = 15;
    private final int ESCALA_RESULTADO = 2;
    private final RoundingMode MODO_ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    public BigDecimal calcularValorTotalTodosBezerros(BigDecimal valor, Integer quantidade) {
        if (valor == null && quantidade == null) {
            throw new IllegalArgumentException("O valor não pode ser nulo");
        }
        return valor.multiply(new BigDecimal(quantidade)).setScale(ESCALA_RESULTADO, MODO_ARREDONDAMENTO);
    }

    public BigDecimal calcularValorTotalBezerro(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return calcularValorBasePorPeso(pesoKg, precoPorArroba).add(calcularValorTotalAgio(pesoKg, precoPorArroba, inputPercentualAgio)).setScale(ESCALA_RESULTADO, MODO_ARREDONDAMENTO);
    }

    public BigDecimal calcularValorTotalPorKg(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        if (pesoKg.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return calcularValorTotalBezerro(pesoKg, precoPorArroba, inputPercentualAgio).divide(pesoKg, ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal calcularValorBasePorPeso(BigDecimal pesoKg, BigDecimal precoPorArroba) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        return converterKgParaArrobas(pesoKg).multiply(precoPorArroba);
    }

    private BigDecimal calcularValorTotalAgio(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return estaNoOuAcimaPesoBase(pesoKg) ? calcularAgioAcimaPesoBase(pesoKg, precoPorArroba, inputPercentualAgio) : calcularAgioAbaixoPesoBase(pesoKg, precoPorArroba, inputPercentualAgio);
    }

    private BigDecimal calcularAgioAcimaPesoBase(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return obterArrobasRestantesParaAbate(pesoKg).multiply(calcularAgioPorArrobaNoPesoBase(pesoKg, precoPorArroba, inputPercentualAgio));
    }

    private BigDecimal calcularAgioPorArrobaNoPesoBase(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return obterValorReferenciaAgioNoPesoBase(precoPorArroba, inputPercentualAgio).subtract(calcularTaxaPorArrobaRestante(pesoKg, precoPorArroba));
    }

    private BigDecimal calcularAgioAbaixoPesoBase(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        BigDecimal acumulado = BigDecimal.ZERO;
        BigDecimal pesoAtual = pesoKg;

        while (pesoAtual.compareTo(PESO_BASE_KG) < 0) {
            acumulado = acumulado.add(calcularDiferencaAgioNoPeso(pesoAtual, precoPorArroba, inputPercentualAgio));
            pesoAtual = calcularProximoPesoSuperior(pesoAtual);
        }
        return acumulado.add(calcularAgioAcimaPesoBase(PESO_BASE_KG, precoPorArroba, inputPercentualAgio));
    }

    private BigDecimal calcularDiferencaAgioNoPeso(BigDecimal pesoKg, BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return obterValorReferenciaAgioNoPesoBase(precoPorArroba, inputPercentualAgio).subtract(calcularTaxaPorArrobaRestante(pesoKg, precoPorArroba));
    }

    private BigDecimal calcularProximoPesoSuperior(BigDecimal pesoKg) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        BigDecimal proximoPeso = arredondarArrobasParaCima(converterKgParaArrobas(pesoKg)).multiply(PESO_ARROBA_KG);
        return limitarPesoAoPesoBase(proximoPeso);
    }

    private BigDecimal converterKgParaArrobas(BigDecimal pesoKg) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        return pesoKg.divide(PESO_ARROBA_KG, ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal obterArrobasRestantesParaAbate(BigDecimal pesoKg) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        return ARROBAS_ABATE_ESPERADAS.subtract(converterKgParaArrobas(pesoKg));
    }

    private BigDecimal calcularTotalTaxasAbate(BigDecimal precoPorArroba) {
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        return ARROBAS_ABATE_ESPERADAS.multiply(precoPorArroba).multiply(IMPOSTO_FUNRURAL).add(TAXA_FIXA_ABATE);
    }

    private BigDecimal calcularTaxaPorArrobaRestante(BigDecimal pesoKg, BigDecimal precoPorArroba) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        BigDecimal restantes = obterArrobasRestantesParaAbate(pesoKg);
        if (restantes.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return calcularTotalTaxasAbate(precoPorArroba).divide(restantes, ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal obterValorReferenciaAgioNoPesoBase(BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return calcularTaxaPorArrobaRestante(PESO_BASE_KG, precoPorArroba).add(calcularAgioPorArrobaExatamenteNoPesoBase(precoPorArroba, inputPercentualAgio));
    }

    private BigDecimal calcularAgioPorArrobaExatamenteNoPesoBase(BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return calcularValorAgioNoPesoBase(precoPorArroba, inputPercentualAgio).divide(obterArrobasRestantesParaAbate(PESO_BASE_KG), ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal calcularValorAgioNoPesoBase(BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return calcularValorPesoBaseComAgio(precoPorArroba, inputPercentualAgio).subtract(converterKgParaArrobas(PESO_BASE_KG).multiply(precoPorArroba));
    }

    private BigDecimal calcularValorPesoBaseComAgio(BigDecimal precoPorArroba, BigDecimal inputPercentualAgio) {
        if (precoPorArroba == null) {
            throw new IllegalArgumentException("O preço por arroba não pode ser nulo");
        }
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return converterKgParaArrobas(PESO_BASE_KG).multiply(precoPorArroba).divide(obterFatorMultiplicadorAgio(inputPercentualAgio), ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal obterFatorMultiplicadorAgio(BigDecimal inputPercentualAgio) {
        if (inputPercentualAgio == null) {
            throw new IllegalArgumentException("O percentual de ágio não pode ser nulo");
        }
        return CEM.subtract(inputPercentualAgio).divide(CEM, ESCALA_CALCULO, MODO_ARREDONDAMENTO);
    }

    private BigDecimal arredondarArrobasParaCima(BigDecimal arrobas) {
        if (arrobas == null) {
            throw new IllegalArgumentException("O valor de arrobas não pode ser nulo");
        }
        BigDecimal arredondado = arrobas.setScale(0, RoundingMode.CEILING);
        return arredondado.compareTo(arrobas) == 0 ? arredondado.add(BigDecimal.ONE) : arredondado;
    }

    private BigDecimal limitarPesoAoPesoBase(BigDecimal pesoKg) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        return pesoKg.compareTo(PESO_BASE_KG) > 0 ? PESO_BASE_KG : pesoKg;
    }

    private boolean estaNoOuAcimaPesoBase(BigDecimal pesoKg) {
        if (pesoKg == null) {
            throw new IllegalArgumentException("O peso em kg não pode ser nulo");
        }
        return pesoKg.compareTo(PESO_BASE_KG) >= 0;
    }
}