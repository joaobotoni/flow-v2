package com.botoni.flow.ui.mappers.domain;

import com.botoni.flow.data.models.Transporte;
import com.botoni.flow.ui.mappers.BiMapper;
import com.botoni.flow.ui.mappers.Mapper;
import com.botoni.flow.ui.state.TransporteUiState;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
public class TransporteMapper implements BiMapper<List<TransporteUiState>, List<Transporte>> {
    @Inject
    public TransporteMapper() {
    }
    @Override
    public List<Transporte> mapTo(List<TransporteUiState> transporteUiStates) {
        return transporteUiStates.stream().map(transporteUiState -> new Transporte(
                        transporteUiState.getId(),
                        transporteUiState.getNomeVeiculo(),
                        transporteUiState.getQuantidade(),
                        transporteUiState.getCapacidade(),
                        transporteUiState.getOcupacao()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransporteUiState> mapFrom(List<Transporte> transportes) {
        return transportes.stream().map(t -> new TransporteUiState(
                        t.getId(),
                        t.getNomeVeiculo(),
                        t.getQuantidade(),
                        t.getCapacidade(),
                        t.getOcupacao()))
                .collect(Collectors.toList());
    }
}
