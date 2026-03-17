package com.botoni.flow.ui.viewmodel;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.botoni.flow.R;
import com.botoni.flow.data.repositories.network.LocationRepository;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.state.RouteUiState;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class RouteViewModel extends ViewModel {
    private static final double DESTINATION_LAT = -15.574583992567458;
    private static final double DESTINATION_LNG = -56.090944897864865;
    private final TaskHelper taskExecutor;
    private final LocationRepository locationRepository;
    private final Context context;
    private final MutableLiveData<RouteUiState> uiState = new MutableLiveData<>(new RouteUiState());
    private final MutableLiveData<Exception> errorEvent = new MutableLiveData<>();

    @Inject
    public RouteViewModel(@ApplicationContext Context context, LocationRepository locationRepository, TaskHelper taskExecutor) {
        this.context = context;
        this.locationRepository = locationRepository;
        this.taskExecutor = taskExecutor;
    }

    public void selectLocation(Address origin) {
        taskExecutor.execute(
                () -> {
                    try {
                        return setResult(origin);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                uiState::setValue,
                errorEvent::setValue
        );
    }

    public void reset() {
        uiState.setValue(new RouteUiState());
    }

    public LiveData<RouteUiState> getUiState() {
        return uiState;
    }

    public LiveData<Exception> getErrorEvent() {
        return errorEvent;
    }

    private RouteUiState setState(List<String> points, double distance) {
        return new RouteUiState(points, distance, true);
    }

    private RouteUiState setResult(Address origin) throws IOException {
        Address destination = fetchDestination();
        double distance = locationRepository.parseDistance(locationRepository.fetchRoute(origin, destination));
        return new RouteUiState(Arrays.asList(format(origin), format(destination)), distance, true);
    }

    private Address fetchDestination() throws IOException {
        List<Address> results = new Geocoder(context).getFromLocation(DESTINATION_LAT, DESTINATION_LNG, 1);
        if (results == null || results.isEmpty()) throw new IOException(context.getString(R.string.erro_endereco_nao_encontrado));
        return results.get(0);
    }

    private String format(Address address) {
        String city = address.getLocality() != null ? address.getLocality() : address.getSubAdminArea();
        String state = address.getAdminArea();
        return city != null && state != null ? String.format("%s, %s", city, state) : address.getAddressLine(0);
    }
}