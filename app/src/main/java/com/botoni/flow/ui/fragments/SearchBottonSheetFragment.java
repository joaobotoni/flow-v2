package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SearchTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.getTexto;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.botoni.flow.databinding.FragmentSearchBottomSheetBinding;
import com.botoni.flow.ui.adapters.LocationAdapter;
import com.botoni.flow.ui.helpers.TaskHelper;
import com.botoni.flow.ui.libs.BottomSheetFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchBottonSheetFragment extends BottomSheetFragment {

    private static final String TAG = "SearchBottomSheetFragment";
    private static final String KEY_KEYBOARD_VISIBLE = "state_keyboard_visible";
    private static final double DESTINATION_LAT = -15.554707386143166;
    private static final double DESTINATION_LNG = -55.650201110432235;

    @Inject
    TaskHelper taskHelper;
    @Inject
    LocationRepository locationRepository;

    private FragmentSearchBottomSheetBinding binding;
    private LocationAdapter locationAdapter;
    private boolean keyboardVisible = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            keyboardVisible = savedInstanceState.getBoolean(KEY_KEYBOARD_VISIBLE, false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupLocationList();
        setupSearchWatcher();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_KEYBOARD_VISIBLE, keyboardVisible);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacksAndMessages(null);
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
        setupKeyboardListener(behavior);
    }

    @Override
    protected void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) hideKeyboard();
        if (newState == BottomSheetBehavior.STATE_DRAGGING && keyboardVisible) hideKeyboard();
    }

    @Override
    protected void onSlide(@NonNull View bottomSheet, float slideOffset) {
        if (binding == null) return;
        float alpha = Math.max(0f, Math.min(1f, slideOffset));
        binding.getRoot().setBackgroundColor(Color.argb((int) (alpha * 150), 0, 0, 0));
        if (slideOffset < 0.5f && keyboardVisible) hideKeyboard();
    }

    private void setupLocationList() {
        if (binding == null || !isAdded()) return;
        locationAdapter = new LocationAdapter(this::onLocationSelected);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(locationAdapter);
    }

    private void setupSearchWatcher() {
        if (binding == null) return;
        binding.textInputEditText.addTextChangedListener(SearchTextWatcher(this::onSearchChanged));
    }

    private void setupKeyboardListener(@NonNull BottomSheetBehavior<View> behavior) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            if (binding == null || !isAdded()) return insets;
            boolean visible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (visible != keyboardVisible) {
                keyboardVisible = visible;
                if (visible && behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }
            }
            return insets;
        });
    }

    private void hideKeyboard() {
        if (binding == null || getActivity() == null) return;
        WindowCompat.getInsetsController(getActivity().getWindow(), binding.getRoot())
                .hide(WindowInsetsCompat.Type.ime());
    }

    private void onLocationSelected(Address origin) {
        taskHelper.execute(
                () -> {
                    try {
                        return buildRouteResult(origin, fetchDestination());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                result -> mainHandler.post(() -> {
                    if (binding == null || !isAdded()) return;
                    getParentFragmentManager().setFragmentResult(TAG, result);
                    dismiss();
                }),
                error -> mainHandler.post(() -> {
                    if (binding == null || !isAdded()) return;
                    showSnackBar(requireView(), getString(R.string.error_search_address));
                })
        );
    }

    private Address fetchDestination() throws IOException {
        List<Address> results = new Geocoder(requireContext())
                .getFromLocation(DESTINATION_LAT, DESTINATION_LNG, 1);
        if (results == null || results.isEmpty()) {
            throw new IOException(getString(R.string.erro_endereco_nao_encontrado));
        }
        return results.get(0);
    }

    private Bundle buildRouteResult(Address origin, Address destination) throws IOException {
        double distance = locationRepository.parseDistance(
                locationRepository.fetchRoute(origin, destination));
        Bundle result = new Bundle();
        result.putStringArrayList("points", new ArrayList<>(Arrays.asList(toAddressString(origin), toAddressString(destination))));
        result.putDouble("distance", distance);
        return result;
    }

    private String toAddressString(Address address) {
        if (address == null) return "";
        String city = address.getLocality() != null ? address.getLocality() : address.getSubAdminArea();
        String state = address.getAdminArea();
        if (city != null && state != null) return String.format("%s, %s", city, state);
        return address.getMaxAddressLineIndex() >= 0 ? address.getAddressLine(0) : "Local desconhecido";
    }

    @SuppressLint("MissingPermission")
    private void onSearchChanged() {
        if (binding == null) return;
        String query = getTexto(binding.textInputEditText);
        if (query.isEmpty()) {
            clearLocationList();
            return;
        }
        taskHelper.execute(
                () -> locationRepository.searchCityAndState(query),
                results -> {
                    if (isAdded() && locationAdapter != null) locationAdapter.submitList(results);
                },
                error -> {
                    showSnackBar(binding.getRoot(), getString(R.string.error_search_address));
                    clearLocationList();
                }
        );
    }

    private void clearLocationList() {
        if (isAdded() && locationAdapter != null) locationAdapter.submitList(new ArrayList<>());
    }
}