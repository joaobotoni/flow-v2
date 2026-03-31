package com.botoni.flow.ui.mappers.domain;

import com.botoni.flow.data.models.Rota;
import com.botoni.flow.ui.mappers.BiMapper;
import com.botoni.flow.ui.state.RotaUiState;

import javax.inject.Inject;

public class RotaMapper implements BiMapper<RotaUiState, Rota> {

    @Inject
    public RotaMapper() {
    }

    @Override
    public Rota mapTo(RotaUiState rotaUiState) {
        return new Rota(
                rotaUiState.getCidadeOrigem(),
                rotaUiState.getEstadoOrigem(),
                rotaUiState.getCidadeDestino(),
                rotaUiState.getEstadoDestino(),
                rotaUiState.getDistancia());
    }

    @Override
    public RotaUiState mapFrom(Rota rota) {
        return new RotaUiState(
                rota.getCidadeOrigem(),
                rota.getEstadoOrigem(),
                rota.getCidadeDestino(),
                rota.getEstadoDestino(),
                rota.getDistancia());
    }
}
