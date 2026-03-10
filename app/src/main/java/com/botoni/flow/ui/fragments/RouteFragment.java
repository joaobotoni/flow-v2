package com.botoni.flow.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.botoni.flow.databinding.FragmentRouteBinding;
import com.botoni.flow.ui.adapters.LocationAdapter;
import com.botoni.flow.ui.adapters.TransportAdapter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RouteFragment extends Fragment {
    private static final String TAG = "RouteFragment";
    private static final String STATE_DISTANCE = "state_distance";
    private static final String STATE_ROUTE = "state_route";
    private static final String STATE_IS_LINKED = "state_is_linked";
    private FragmentRouteBinding binding;
    private Double distance;
    private String[] route;
    private boolean isLinked;
    private TransportAdapter transportAdapter;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreInstanceState(savedInstanceState);
        registerFragmentResultListener();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRouteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureRecyclerView();
        if (isDataReady()) {
            bindRouteData();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (distance != null) {
            outState.putDouble(STATE_DISTANCE, distance);
        }
        if (route != null) {
            outState.putStringArray(STATE_ROUTE, route);
        }
        outState.putBoolean(STATE_IS_LINKED, isLinked);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void configureRecyclerView() {
        if (binding == null || !isAdded()) return;
        transportAdapter = new TransportAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(transportAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void registerFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener("DealFragment", this, (key, result) -> {
            distance = result.getDouble("distance");
            Optional.ofNullable(result.getStringArrayList("points"))
                    .ifPresent(list -> route = list.toArray(String[]::new));
            if (binding != null) {
                bindRouteData();
            }
        });
    }

    private void restoreInstanceState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        distance = savedInstanceState.getDouble(STATE_DISTANCE);
        route = savedInstanceState.getStringArray(STATE_ROUTE);
        isLinked = savedInstanceState.getBoolean(STATE_IS_LINKED, false);
    }

    private boolean isDataReady() {
        return distance != null && route != null;
    }

    private void bindRouteData() {
        String[] origin = parseRoutePoint(route[0]);
        String[] destination = parseRoutePoint(route[1]);

        binding.textoCidadeOrigem.setText(origin[0]);
        binding.textoEstadoOrigem.setText(origin[1]);

        binding.textoCidadeDestino.setText(destination[0]);
        binding.textoEstadoDestino.setText(destination[1]);

        binding.textoValorDistancia.setText(String.format(Locale.getDefault(), "%.2f", distance));

        if (!isLinked) {
            notifyLinkedState();
        }
    }

    private String[] parseRoutePoint(String point) {
        return Arrays.stream(point.split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }

    private void notifyLinkedState() {
        isLinked = true;
        Bundle result = new Bundle();
        result.putBoolean("linked", isLinked);
        getParentFragmentManager().setFragmentResult(TAG, result);
    }
}