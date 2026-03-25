package com.botoni.flow.data.repositories;

import com.botoni.flow.data.models.PrecificacaoFrete;
import com.botoni.flow.data.models.Transporte;
import com.botoni.flow.data.source.local.dao.FreteDao;
import com.botoni.flow.data.source.local.entities.Frete;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

public class FreteRepository {
    private static final int ESCALA_CALCULO = 15;
    private static final int ESCALA_RESULTADO = 2;
    private final FreteDao dao;
    @Inject
    public FreteRepository(FreteDao dao) {
        this.dao = dao;
    }

    public List<Frete> listar() {
        return dao.getAll();
    }

    public Optional<Frete> buscarPorId(long id) {
        return Optional.ofNullable(dao.findById(id));
    }

    public Optional<Frete> buscarPorVeiculoEDistancia(long idVeiculo, double distancia) {
        return Optional.ofNullable(dao.findByVehicleAndDistance(idVeiculo, distancia));
    }

    public long inserir(Frete frete) {
        return dao.insert(frete);
    }

    public void inserirTodos(List<Frete> fretes) {
        dao.insertAll(fretes);
    }

    public int atualizar(Frete frete) {
        return dao.update(frete);
    }

    public int remover(Frete frete) {
        return dao.delete(frete);
    }

    public void removerTodos() {
        dao.deleteAll();
    }

    public PrecificacaoFrete calcularFrete(List<Transporte> transportes, double distancia, BigDecimal valorTotalAnimal) {
        BigDecimal totalFrete = calcularFreteTotal(transportes, distancia);
        BigDecimal valorParcial = calcularIncidenciaFretePorAnimal(totalFrete, valorTotalAnimal);
        return new PrecificacaoFrete(totalFrete, valorParcial);
    }

    public BigDecimal calcularFreteTotal(List<Transporte> transportes, double distancia) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transporte transporte : transportes) {
            BigDecimal valorUnitarioVeiculo = buscarPorVeiculoEDistancia(transporte.getId(), distancia)
                    .map(frete -> BigDecimal.valueOf(frete.getValor()))
                    .orElse(BigDecimal.ZERO);
            BigDecimal subtotal = valorUnitarioVeiculo.multiply(BigDecimal.valueOf(transporte.getQuantidade()));
            total = total.add(subtotal);
        }
        return total.setScale(ESCALA_RESULTADO, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularIncidenciaFretePorAnimal(BigDecimal valorTotalFrete, BigDecimal valorTotalAnimal) {
        return valorTotalFrete.divide(valorTotalAnimal, ESCALA_CALCULO, RoundingMode.HALF_UP)
                .setScale(ESCALA_RESULTADO, RoundingMode.HALF_UP);
    }
}