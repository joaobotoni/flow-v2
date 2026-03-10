package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.TextWatcherHelper.SearchTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.getTexto;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.botoni.flow.R;
import com.botoni.flow.data.repositories.network.LocationRepository;
import com.botoni.flow.libs.BottomSheetFragment;
import com.botoni.flow.ui.adapters.LocationAdapter;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.botoni.flow.databinding.FragmentSearchBottomSheetBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchBottonSheetFragment extends BottomSheetFragment {

    private static final String TAG = "SearchBottonSheetFragment";
    private static final String STATE_KEYBOARD_VISIBLE = "state_keyboard_visible";
    private static final double[] DEFAULT_LAT_LNG = {34.06266637826144, -118.20323412642546};
    @Inject
    TaskHelper taskHelper;
    @Inject
    LocationRepository locationRepository;
    private FragmentSearchBottomSheetBinding binding;
    private boolean isKeyboardVisible = false;
    private LocationAdapter locationAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureRecyclerView();
        setupTextWatchers();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_KEYBOARD_VISIBLE, isKeyboardVisible);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @NonNull
    @Override
    protected View getBackgroundView() {
        return binding.getRoot();
    }

    @NonNull
    @Override
    protected View getBottomSheetView() {
        return binding.bottom;
    }

    @Override
    protected void onBind(@NonNull BottomSheetBehavior<View> behavior) {
        if (binding == null) return;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        behavior.setHideable(true);
        behavior.setFitToContents(false);
        behavior.setPeekHeight((int) (screenHeight * 0.4f));
        behavior.setHalfExpandedRatio(0.8f);
        behavior.setExpandedOffset((int) (screenHeight * 0.2f));
        registerKeyboardInsetsListener(behavior);
    }

    @Override
    protected void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            hideKeyboard();
        }
        if (newState == BottomSheetBehavior.STATE_DRAGGING && isKeyboardVisible) {
            hideKeyboard();
        }
    }

    @Override
    protected void onSlide(@NonNull View bottomSheet, float slideOffset) {
        if (binding == null) return;
        float alpha = Math.max(0f, Math.min(1f, slideOffset));
        binding.getRoot().setBackgroundColor(Color.argb((int) (alpha * 150), 0, 0, 0));
        if (slideOffset < 0.5f && isKeyboardVisible) {
            hideKeyboard();
        }
    }

    private void restoreInstanceState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        isKeyboardVisible = savedInstanceState.getBoolean(STATE_KEYBOARD_VISIBLE, false);
    }

    private void registerKeyboardInsetsListener(@NonNull BottomSheetBehavior<View> behavior) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            if (binding == null || !isAdded()) return insets;
            boolean visible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (visible != isKeyboardVisible) {
                isKeyboardVisible = visible;
                if (visible && behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }
            }
            return insets;
        });
    }

    private void hideKeyboard() {
        if (binding != null && getActivity() != null) {
            WindowCompat.getInsetsController(getActivity().getWindow(), binding.getRoot())
                    .hide(WindowInsetsCompat.Type.ime());
        }
    }

    private void setupTextWatchers() {
        if (binding == null) return;
        TextWatcher searchWatcher = SearchTextWatcher(this::performAddressSearch);
        binding.textInputEditText.addTextChangedListener(searchWatcher);
    }

    private void configureRecyclerView() {
        if (binding == null || !isAdded()) return;
        locationAdapter = new LocationAdapter(this::fetchRouteFrom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(locationAdapter);
    }

    private void fetchRouteFrom(Address origin) {
        taskHelper.execute(() -> {
            try {
                Address destination = resolveDestinationAddress();
                double distance = locationRepository.parseDistance(
                        locationRepository.fetchRoute(origin, destination)
                );
                return buildRouteResult(origin, destination, distance);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, result -> {
            if (binding == null || !isAdded()) return;
            getParentFragmentManager().setFragmentResult(TAG, result);
            dismiss();
        }, error -> {
            if (binding == null || !isAdded()) return;
            showSnackbar(requireView(), getString(R.string.error_search_address));
        });
    }

    private Address resolveDestinationAddress() throws IOException {
        List<Address> results = new Geocoder(requireContext())
                .getFromLocation(DEFAULT_LAT_LNG[0], DEFAULT_LAT_LNG[1], 1);

        if (results == null || results.isEmpty())
            throw new IOException(getString(R.string.erro_endereco_nao_encontrado));

        return results.get(0);
    }

    private Bundle buildRouteResult(Address origin, Address destination, double distance) {
        Bundle result = new Bundle();
        result.putStringArrayList("points", new ArrayList<>(Arrays.asList(
                String.format("%s, %s", destination.getLocality(), destination.getAdminArea()),
                String.format("%s, %s", origin.getLocality(), origin.getAdminArea())
        )));
        result.putDouble("distance", distance);
        return result;
    }

    @SuppressLint("MissingPermission")
    private void performAddressSearch() {
        if (binding == null) return;
        String query = getTexto(binding.textInputEditText);

        if (query.isEmpty()) {
            clearLocationList();
            return;
        }

        taskHelper.execute(
                () -> locationRepository.searchCityAndState(query),
                results -> {
                    if (isAdded() && locationAdapter != null) {
                        locationAdapter.submitList(results);
                    }
                },
                e -> {
                    if (binding != null && isAdded()) {
                        showSnackbar(binding.getRoot(), getString(R.string.error_search_address));
                    }
                    clearLocationList();
                }
        );
    }

    private void clearLocationList() {
        if (isAdded() && locationAdapter != null) {
            locationAdapter.submitList(new ArrayList<>());
        }
    }

    private void showSnackbar(View view, String message) {
        if (view != null && message != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }
}