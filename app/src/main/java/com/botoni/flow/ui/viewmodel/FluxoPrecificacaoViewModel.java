package com.botoni.flow.ui.viewmodel;

import android.location.Address;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.models.PrecificacaoBezerro;
import com.botoni.flow.data.models.Rota;
import com.botoni.flow.data.models.Transporte;
import com.botoni.flow.data.repositories.CategoriaFreteRepository;
import com.botoni.flow.data.repositories.FreteRepository;
import com.botoni.flow.data.repositories.LocalizacaoRepository;
import com.botoni.flow.data.repositories.PrecificacaoBezerroRepository;
import com.botoni.flow.data.repositories.TransporteRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.BuscaLocalizacaoUiState;
import com.botoni.flow.ui.state.CategoriaUiState;
import com.botoni.flow.ui.state.PrecificacaoBezerroUiState;
import com.botoni.flow.ui.state.RotaUiState;
import com.botoni.flow.ui.state.TransporteUiState;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FluxoPrecificacaoViewModel extends ViewModel {

    protected final TaskHelper taskHelper;
    private static final String DESTINO_QUERY = "Cuiabá";
    private final CategoriaFreteRepository categoriaFreteRepository;
    private final PrecificacaoBezerroRepository precificacaoBezerroRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final FreteRepository freteRepository;
    private final TransporteRepository transporteRepository;
    private final MutableLiveData<List<CategoriaUiState>> listCategoriaMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<PrecificacaoBezerroUiState> precificacaoBezerroUiStateMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<BuscaLocalizacaoUiState> buscaLocalizacaoUiStateMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<RotaUiState> rotaUiStateMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TransporteUiState>> listTransporteMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exception> error = new MutableLiveData<>(null);
    @Inject
    public FluxoPrecificacaoViewModel(TaskHelper taskHelper, CategoriaFreteRepository categoriaFreteRepository, PrecificacaoBezerroRepository precificacaoBezerroRepository, LocalizacaoRepository localizacaoRepository, FreteRepository freteRepository, TransporteRepository transporteRepository) {
        this.taskHelper = taskHelper;
        this.categoriaFreteRepository = categoriaFreteRepository;
        this.precificacaoBezerroRepository = precificacaoBezerroRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.freteRepository = freteRepository;
        this.transporteRepository = transporteRepository;
        listarCategoria();
    }

    public LiveData<List<CategoriaUiState>> getListCategoriaMutableLiveData() {
        return listCategoriaMutableLiveData;
    }

    public LiveData<PrecificacaoBezerroUiState> getPrecificacaoBezerroUiStateMutableLiveData() {
        return precificacaoBezerroUiStateMutableLiveData;
    }

    public LiveData<BuscaLocalizacaoUiState> getBuscaLocalizacaoUiStateMutableLiveData() {
        return buscaLocalizacaoUiStateMutableLiveData;
    }

    public LiveData<RotaUiState> getRotaUiStateMutableLiveData() {
        return rotaUiStateMutableLiveData;
    }

    public LiveData<List<TransporteUiState>> getListTransporteMutableLiveData() {
        return listTransporteMutableLiveData;
    }



    public LiveData<Exception> getError() {
        return error;
    }

    private void listarCategoria() {
        taskHelper.execute(
                () -> categoriaFreteRepository.getAll().stream()
                        .map(e -> new CategoriaUiState(
                                e.getId(),
                                e.getDescricao(),
                                false))
                        .collect(Collectors.toList()),
                listCategoriaMutableLiveData::postValue,
                error::postValue
        );
    }

    public void selecionarCategoria(CategoriaUiState selecionada) {
        List<CategoriaUiState> atual = listCategoriaMutableLiveData.getValue();
        if (atual == null) return;
        List<CategoriaUiState> novaLista = atual.stream()
                .map(item -> new CategoriaUiState(
                        item.getId(),
                        item.getDescricao(),
                        item.getId() == selecionada.getId()))
                .collect(Collectors.toList());
        listCategoriaMutableLiveData.setValue(novaLista);
    }


    public void calcularValoresBezerro(BigDecimal peso, BigDecimal arroba, BigDecimal percent, Integer quantidade) {
        taskHelper.execute(
                () -> {
                    PrecificacaoBezerro result = precificacaoBezerroRepository.calcularNegociacaoBezerro(peso, arroba, percent, quantidade);
                    return new PrecificacaoBezerroUiState(result.getValorPorKg(), result.getValorPorCabeca(), result.getValorTotal());
                },
                precificacaoBezerroUiStateMutableLiveData::postValue,
                error::postValue
        );
    }

    public void buscarEndereco(String consulta, double latitude, double longitude) {
        taskHelper.execute(
                () -> {
                    String codigo = localizacaoRepository.paisDeCoordenadas(latitude, longitude).orElseThrow(() ->
                            new RuntimeException("Código do pais não encontrado"));
                    return new BuscaLocalizacaoUiState(localizacaoRepository.enderecosPorTexto(consulta, codigo), false);
                },
                buscaLocalizacaoUiStateMutableLiveData::postValue,
                error::postValue
        );
    }

    public void selecionarEndereco(Address origem) {
        taskHelper.execute(
                () -> {
                    Address destino = localizacaoRepository.enderecoPorNome(DESTINO_QUERY)
                            .orElseThrow(() -> new IllegalArgumentException("Destino não encontrado para a busca: " + DESTINO_QUERY));
                    Rota rota = localizacaoRepository.calcularRota(origem, destino);
                    return new RotaUiState(
                            rota.getCidadeOrigem(),
                            rota.getEstadoOrigem(),
                            rota.getCidadeDestino(),
                            rota.getEstadoDestino(),
                            rota.getDistancia()
                    );
                },
                rotaUiStateMutableLiveData::postValue,
                error::postValue
        );
    }

    public void recomendarTransporte(long categoria, int quantidade) {
        taskHelper.execute(
                () -> transporteRepository.recomendarTransportes(categoria, quantidade)
                        .stream()
                        .map(t -> new TransporteUiState(
                                t.getId(),
                                t.getNomeVeiculo(),
                                t.getQuantidade(),
                                t.getCapacidade(),
                                t.getOcupacao()))
                        .collect(Collectors.toList()),
                listTransporteMutableLiveData::postValue,
                error::postValue
        );
    }

//    public void calcularFrete(List<Transporte> transportes, double distancia, int totalAnimais) {
//        taskHelper.execute(
//                () -> {
//                    PrecificacaoFrete result = freteRepository.calcularFrete(transportes, distancia, totalAnimais);
//                    return new PrecificacaoFreteUiState(result.getValorTotal(), result.getValorPorAnimal());
//                },
//                precificacaoFreteUiStateMutableLiveData::postValue,
//                error::postValue
//        );
//    }

    @Override
    protected void onCleared() {
        super.onCleared();
        taskHelper.cancelAll();
    }
}
