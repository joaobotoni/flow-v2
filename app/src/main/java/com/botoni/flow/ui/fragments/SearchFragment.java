package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.AlertHelper.showSnackBar;
import static com.botoni.flow.ui.helpers.TextWatcherHelper.SearchTextWatcher;
import static com.botoni.flow.ui.helpers.ViewHelper.requireText;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.botoni.flow.R;
import com.botoni.flow.databinding.FragmentSearchBinding;
import com.botoni.flow.ui.adapters.LocationAdapter;
import com.botoni.flow.ui.libs.BottomSheetFragment;
import com.botoni.flow.ui.viewmodel.RouteViewModel;
import com.botoni.flow.ui.viewmodel.SearchViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends BottomSheetFragment {
    private static final String KEY_KEYBOARD_VISIBLE = "state_keyboard_visible";
    private FragmentSearchBinding binding;
    private LocationAdapter locationAdapter;
    private SearchViewModel searchViewModel;
    private RouteViewModel routeViewModel;
    private boolean keyboardVisible = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            keyboardVisible = savedInstanceState.getBoolean(KEY_KEYBOARD_VISIBLE, false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModels();
        initViews();
        observeViewModel();
        routeViewModel.reset();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_KEYBOARD_VISIBLE, keyboardVisible);
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
        setupBehavior(behavior);
        setupKeyboardListener(behavior);
    }

    @Override
    protected void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN || (newState == BottomSheetBehavior.STATE_DRAGGING && keyboardVisible)) {
            hideKeyboard();
        }
    }

    @Override
    protected void onSlide(@NonNull View bottomSheet, float slideOffset) {
        updateBackgroundAlpha(slideOffset);
        if (slideOffset < 0.5f && keyboardVisible) hideKeyboard();
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void initViews() {
        bindRecycleView();
        initInputWatchers();
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void initInputWatchers() {
        binding.textInputEditText.addTextChangedListener(SearchTextWatcher(this::onQueryChanged));
    }

    private void initViewModels() {
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        routeViewModel = new ViewModelProvider(requireActivity()).get(RouteViewModel.class);
    }

    private void observeViewModel() {
        observeSearch();
        observeRoute();
    }

    private void observeSearch() {
        searchViewModel.getUiState().observe(getViewLifecycleOwner(), state ->
                locationAdapter.submitList(state.getLocations()));

        searchViewModel.getErrorEvent().observe(getViewLifecycleOwner(), error -> {
            locationAdapter.submitList(null);
            showSnackBar(requireView(), getString(R.string.error_search_address));
        });
    }

    private void observeRoute() {
        routeViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state.isVisible()) dismiss();
        });

        routeViewModel.getErrorEvent().observe(getViewLifecycleOwner(), error ->
                showSnackBar(requireView(), getString(R.string.error_search_address)));
    }

    private void bindRecycleView() {
        locationAdapter = new LocationAdapter(routeViewModel::selectLocation);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(locationAdapter);
    }

    private void setupBehavior(@NonNull BottomSheetBehavior<View> behavior) {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        behavior.setHideable(true);
        behavior.setFitToContents(false);
        behavior.setPeekHeight((int) (screenHeight * 0.4f));
        behavior.setHalfExpandedRatio(0.8f);
        behavior.setExpandedOffset((int) (screenHeight * 0.2f));
    }


    private void setupKeyboardListener(@NonNull BottomSheetBehavior<View> behavior) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            onKeyboardVisibilityChanged(insets.isVisible(WindowInsetsCompat.Type.ime()), behavior);
            return ViewCompat.onApplyWindowInsets(v, insets);
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void onQueryChanged() {
        String query = requireText(binding.textInputEditText);
        locationAdapter.submitList(null);
        if (query.isEmpty()) searchViewModel.reset();
        else searchViewModel.search(query);
    }

    private void onKeyboardVisibilityChanged(boolean visible, BottomSheetBehavior<View> behavior) {
        if (visible == keyboardVisible) return;
        keyboardVisible = visible;
        if (visible && behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    private void updateBackgroundAlpha(float slideOffset) {
        float alpha = Math.max(0f, Math.min(1f, slideOffset));
        binding.getRoot().setBackgroundColor(Color.argb((int) (alpha * 150), 0, 0, 0));
    }

    private void hideKeyboard() {
        WindowCompat.getInsetsController(requireActivity().getWindow(), binding.getRoot())
                .hide(WindowInsetsCompat.Type.ime());
    }
}