package com.botoni.flow.ui.state;


import com.botoni.flow.data.source.local.entities.CategoriaFrete;

import java.util.Collections;
import java.util.List;

public class CategoryUiState {

    private List<CategoriaFrete> categories;
    private CategoriaFrete selected;

    public CategoryUiState(List<CategoriaFrete> categories, CategoriaFrete selected) {
        this.categories = categories;
        this.selected = selected;
    }

    public CategoryUiState() {
    }

    public List<CategoriaFrete> getCategories() {
        return categories;
    }

    public CategoriaFrete getSelected() {
        return selected;
    }

    public boolean hasSelected() {
        return selected != null;
    }
}