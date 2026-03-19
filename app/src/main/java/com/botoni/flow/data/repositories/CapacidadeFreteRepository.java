package com.botoni.flow.data.repositories;

import com.botoni.flow.data.source.local.dao.CapacidadeFreteDao;
import com.botoni.flow.data.source.local.entities.CapacidadeFrete;

import java.util.List;

import javax.inject.Inject;

public class CapacidadeFreteRepository {
    private final CapacidadeFreteDao dao;
    @Inject
    public CapacidadeFreteRepository(CapacidadeFreteDao dao) {
        this.dao = dao;
    }

    public List<CapacidadeFrete> getAll() {
        return dao.getAll();
    }

    public CapacidadeFrete findById(long id) {
        return dao.findById(id);
    }

    public List<CapacidadeFrete> findByCategoria(long id) {
        return dao.findByCategoria(id);
    }

    public long insert(CapacidadeFrete capacidadeFrete) {
        return dao.insert(capacidadeFrete);
    }

    public void insertAll(List<CapacidadeFrete> capacidades) {
        dao.insertAll(capacidades);
    }

    public int update(CapacidadeFrete capacidadeFrete) {
        return dao.update(capacidadeFrete);
    }

    public int delete(CapacidadeFrete capacidadeFrete) {
        return dao.delete(capacidadeFrete);
    }

    public void deleteAll() {
        dao.deleteAll();
    }
}