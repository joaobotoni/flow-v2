package com.botoni.flow.ui.state;

import java.math.BigDecimal;

public class DealUiState {
    private boolean freteVisible;
    private RouteState routeState;
    private String selectedCategory;
    private BigDecimal valorPorKg;
    private BigDecimal valorPorCabeca;
    private BigDecimal valorTotal;
    private boolean isVisible;

    public DealUiState() {
    }

    public DealUiState(boolean freteVisible, RouteState routeState, String selectedCategory, BigDecimal valorPorKg, BigDecimal valorPorCabeca, BigDecimal valorTotal, boolean resultadosVisible) {
        this.freteVisible = freteVisible;
        this.routeState = routeState;
        this.selectedCategory = selectedCategory;
        this.valorPorKg = valorPorKg;
        this.valorPorCabeca = valorPorCabeca;
        this.valorTotal = valorTotal;
        this.isVisible = resultadosVisible;
    }

    public boolean isFreteVisible() {
        return freteVisible;
    }

    public RouteState getRouteState() {
        return routeState;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public BigDecimal getValorPorKg() {
        return valorPorKg;
    }

    public BigDecimal getValorPorCabeca() {
        return valorPorCabeca;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setFreteVisible(boolean freteVisible) {
        this.freteVisible = freteVisible;
    }

    public void setRouteState(RouteState routeState) {
        this.routeState = routeState;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public void setValorPorKg(BigDecimal valorPorKg) {
        this.valorPorKg = valorPorKg;
    }

    public void setValorPorCabeca(BigDecimal valorPorCabeca) {
        this.valorPorCabeca = valorPorCabeca;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}