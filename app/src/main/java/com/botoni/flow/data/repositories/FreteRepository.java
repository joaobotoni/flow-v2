package com.botoni.flow.data.repositories;

import com.botoni.flow.data.source.local.dao.FreteDao;
import com.botoni.flow.data.source.local.entities.Frete;
import com.botoni.flow.data.model.Transporte;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.inject.Inject;

public class FreteRepository {
    private final FreteDao dao;

    @Inject
    public FreteRepository(FreteDao dao) {
        this.dao = dao;
    }

    public List<Frete> getAll() {
        return dao.getAll();
    }

    public Frete findById(long id) {
        return dao.findById(id);
    }

    public Frete findByValueInRange(long id, double range) {
        return dao.findByVehicleAndDistance(id, range);
    }

    public long insert(Frete frete) {
        return dao.insert(frete);
    }

    public void insertAll(List<Frete> fretes) {
        dao.insertAll(fretes);
    }

    public int update(Frete frete) {
        return dao.update(frete);
    }

    public int delete(Frete frete) {
        return dao.delete(frete);
    }

    public void deleteAll() {
        dao.deleteAll();
    }

    public BigDecimal calcularTotal(List<Transporte> transportes, double distancia) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transporte transporte : transportes) {
            Frete frete = dao.findByVehicleAndDistance(transporte.getId(), distancia);
            if (frete == null) continue;
            BigDecimal valor = BigDecimal.valueOf(frete.getValor());
            BigDecimal quantidade = BigDecimal.valueOf(transporte.getQuantidade());
            total = total.add(valor.multiply(quantidade));
        }
        return total;
    }

    public BigDecimal calcularPorCabeca(List<Transporte> transportes, double distancia, int totalAnimais) {
        return calcularTotal(transportes, distancia)
                .divide(BigDecimal.valueOf(totalAnimais), 2, RoundingMode.HALF_UP);
    }
}