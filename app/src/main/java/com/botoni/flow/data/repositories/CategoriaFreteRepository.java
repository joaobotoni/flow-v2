package com.botoni.flow.data.repositories;

import com.botoni.flow.data.source.local.dao.CategoriaFreteDao;
import com.botoni.flow.data.source.local.entities.CategoriaFrete;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class CategoriaFreteRepository {
    private final CategoriaFreteDao dao;
    @Inject
    public CategoriaFreteRepository(CategoriaFreteDao dao) {
        this.dao = dao;
    }

    public List<CategoriaFrete> getAll() {
        return dao.getAll();
    }

    public CategoriaFrete findById(long id) {
        return dao.findById(id);
    }

    public long insert(CategoriaFrete categoriaFrete) {
        return dao.insert(categoriaFrete);
    }

    public void insertAll(List<CategoriaFrete> categorias) {
        dao.insertAll(categorias);
    }

    public int update(CategoriaFrete categoriaFrete) {
        return dao.update(categoriaFrete);
    }

    public int delete(CategoriaFrete categoriaFrete) {
        return dao.delete(categoriaFrete);
    }

    public void deleteAll() {
        dao.deleteAll();
    }

    public List<String> getDescricoes() {
        return dao.getAll().stream()
                .map(CategoriaFrete::getDescricao)
                .collect(Collectors.toList());
    }

}