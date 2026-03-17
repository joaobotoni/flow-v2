package com.botoni.flow.ui.state;

import android.location.Address;

import java.util.List;

public class SearchUiState {
    private List<Address> locations;
    private boolean loading;

    public SearchUiState() {
    }

    public SearchUiState(List<Address> locations, boolean loading) {
        this.locations = locations;
        this.loading = loading;
    }

    public List<Address> getLocations() {
        return locations;
    }

    public void setLocations(List<Address> locations) {
        this.locations = locations;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
