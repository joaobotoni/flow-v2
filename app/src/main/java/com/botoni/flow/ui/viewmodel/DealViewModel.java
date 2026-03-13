package com.botoni.flow.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.data.repositories.local.CategoriaFreteRepository;
import com.botoni.flow.data.source.local.entities.CategoriaFrete;
import com.botoni.flow.domain.entities.Category;
import com.botoni.flow.domain.usecases.AvaliacaoPrecoAnimalUseCase;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.DealUiState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DealViewModel extends ViewModel {

    private static final BigDecimal PRICE_ARROBA = new BigDecimal("310.0");
    private static final BigDecimal MARGIN_PERCENT = new BigDecimal("30.0");
    private final CategoriaFreteRepository freightRepo;
    private final TaskHelper taskExecutor;
    private final AvaliacaoPrecoAnimalUseCase avaliacaoPrecoAnimalUseCase;
    private final MutableLiveData<DealUiState> uiState = new MutableLiveData<>(new DealUiState());
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Category> selectedCategory = new MutableLiveData<>(new Category());
    private final MutableLiveData<Exception> errorEvent = new MutableLiveData<>();
    @Inject
    public DealViewModel(AvaliacaoPrecoAnimalUseCase avaliacaoPrecoAnimalUseCase , CategoriaFreteRepository freightRepo, TaskHelper taskExecutor) {
        this.avaliacaoPrecoAnimalUseCase = avaliacaoPrecoAnimalUseCase;
        this.freightRepo = freightRepo;
        this.taskExecutor = taskExecutor;
        loadCategories();
    }

    public LiveData<DealUiState> getUiState() {
        return uiState;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Category> getSelectedCategory() {
        return selectedCategory;
    }

    public LiveData<Exception> getErrorEvent() {
        return errorEvent;
    }

    public void calculate(BigDecimal weight, int quantity) {
        BigDecimal pricePerKg = avaliacaoPrecoAnimalUseCase.calcularValorTotalPorKg(weight, PRICE_ARROBA, MARGIN_PERCENT);
        BigDecimal pricePerHead = avaliacaoPrecoAnimalUseCase.calcularValorTotalBezerro(weight, PRICE_ARROBA, MARGIN_PERCENT);
        BigDecimal totalPrice = avaliacaoPrecoAnimalUseCase.calcularValorTotalTodosBezerros(pricePerHead, quantity);
        uiState.setValue(new DealUiState(pricePerKg, pricePerHead, totalPrice, true));
    }

    public void select(Category category) {
        selectedCategory.setValue(category);
        categories.setValue(marked(category));
    }

    private void loadCategories() {
        taskExecutor.execute(
                this::fetchCategories,
                categories::setValue,
                errorEvent::setValue
        );
    }

    private List<Category> fetchCategories() {
        List<Category> list = new ArrayList<>();
        for (CategoriaFrete entity : freightRepo.getAll()) {
            list.add(new Category(entity.getId(),entity.getDescricao(), false));
        }
        return list;
    }

    private List<Category> marked(Category selected) {
        List<Category> current = categories.getValue();
        List<Category> newList = new ArrayList<>();
        if (current != null) {
            for (Category item : current) {
                boolean isSelected = item.getDescription().equals(selected.getDescription());
                newList.add(new Category(item.getId(), item.getDescription(), isSelected));
            }
        }
        return newList;
    }

    public void reset() {
        uiState.setValue(new DealUiState());
    }
}