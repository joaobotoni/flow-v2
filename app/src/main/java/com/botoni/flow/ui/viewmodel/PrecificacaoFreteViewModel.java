//package com.botoni.flow.ui.viewmodel;
//
//import com.botoni.flow.data.models.Transporte;
//import com.botoni.flow.data.repositories.FreteRepository;
//import com.botoni.flow.ui.helpers.TaskHelper;
//import com.botoni.flow.ui.libs.BaseViewModel;
//
//import java.util.List;
//
//import javax.inject.Inject;
//
//import dagger.hilt.android.lifecycle.HiltViewModel;
//
//@HiltViewModel
//public class PrecificacaoFreteViewModel extends BaseViewModel<PrecificacaoFreteUiState> {
//    private final FreteRepository repositorio;
//
//    @Inject
//    public PrecificacaoFreteViewModel(TaskHelper taskHelper, FreteRepository repositorio) {
//        super(taskHelper);
//        this.repositorio = repositorio;
//    }
//
//    public void calcularFrete(List<Transporte> transportes, double distancia, int totalAnimais) {
//        taskHelper.execute(
//                () -> {
//                    PrecificacaoFrete result = repositorio.calcularFrete(transportes, distancia, totalAnimais);
//                    return new PrecificacaoFreteUiState(result.getValorTotal(), result.getValorPorAnimal());
//                },
//                state::postValue,
//                error::postValue
//        );
//    }
//}