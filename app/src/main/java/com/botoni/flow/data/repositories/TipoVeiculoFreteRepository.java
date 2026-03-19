package com.botoni.flow.data.repositories;

import com.botoni.flow.data.source.local.dao.TipoVeiculoFreteDao;
import com.botoni.flow.data.source.local.entities.TipoVeiculoFrete;

import java.util.List;
import javax.inject.Inject;

public class TipoVeiculoFreteRepository {
    private final TipoVeiculoFreteDao dao;
    @Inject
    public TipoVeiculoFreteRepository(TipoVeiculoFreteDao dao) {
        this.dao = dao;
    }

    public List<TipoVeiculoFrete> getAll() {
        return dao.getAll();
    }

    public TipoVeiculoFrete findById(long id) {
        return dao.findById(id);
    }

    public long insert(TipoVeiculoFrete tipoVeiculo) {
        return dao.insert(tipoVeiculo);
    }

    public void insertAll(List<TipoVeiculoFrete> tiposVeiculo) {
        dao.insertAll(tiposVeiculo);
    }

    public int update(TipoVeiculoFrete tipoVeiculo) {
        return dao.update(tipoVeiculo);
    }

    public int delete(TipoVeiculoFrete tipoVeiculo) {
        return dao.delete(tipoVeiculo);
    }

    public void deleteAll() {
        dao.deleteAll();
    }
}