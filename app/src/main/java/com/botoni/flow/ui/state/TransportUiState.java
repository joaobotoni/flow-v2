package com.botoni.flow.ui.state;

import com.botoni.flow.domain.entities.Transport;

import java.util.List;

public class TransportUiState {
    private List<Transport> transports;
    private boolean isVisible;
    public TransportUiState() {
    }
    public TransportUiState(List<Transport> transports, boolean isVisible) {
        this.transports = transports;
        this.isVisible = isVisible;
    }

    public List<Transport> getTransports() {
        return transports;
    }

    public void setTransports(List<Transport> transports) {
        this.transports = transports;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}
