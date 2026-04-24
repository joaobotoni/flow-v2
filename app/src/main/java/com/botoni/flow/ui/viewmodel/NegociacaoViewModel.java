package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.ui.state.EmpresaUiState;
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class NegociacaoViewModel extends ViewModel {
    private final MutableLiveData<PrecificacaoBezerroUiState> override = new MutableLiveData<>();
    private final MutableLiveData<BigDecimal> totalOriginal = new MutableLiveData<>();
    private final MutableLiveData<List<EmpresaUiState>> empresas = new MutableLiveData<>();

    public LiveData<PrecificacaoBezerroUiState> getOverride() {
        return override;
    }

    public void setOverride(PrecificacaoBezerroUiState estado) {
        override.setValue(estado);
    }

    public void limparOverride() {
        override.setValue(null);
    }

    public LiveData<BigDecimal> getTotalOriginal() {
        return totalOriginal;
    }

    public void setTotalOriginal(BigDecimal valor) {
        totalOriginal.setValue(valor);
    }

    public LiveData<List<EmpresaUiState>> getEmpresas() {
        return empresas;
    }

    public void setEmpresas(List<EmpresaUiState> listaEmpresas) {
        empresas.setValue(listaEmpresas);
    }

    public void selecionarEmpresa(EmpresaUiState selecionada) {
        if (empresas.getValue() == null) return;

        List<EmpresaUiState> atualizados = empresas.getValue().stream()
                .map(empresa -> new EmpresaUiState(
                        empresa.getId(),
                        empresa.getNome(),
                        empresa.getIniciais(),
                        Objects.equals(empresa.getId(), selecionada.getId())))
                .collect(Collectors.toList());

        empresas.setValue(atualizados);
    }
}
