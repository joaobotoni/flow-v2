package com.botoni.flow.ui.fragments;

import static com.botoni.flow.ui.helpers.FormatHelper.formatCurrency;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.botoni.flow.databinding.FragmentResumoValoresBinding;
import com.botoni.flow.ui.viewmodel.ResumoValoresViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResumoValoresFragment extends Fragment {
    private static final String ARG_CHAVE = "chave";
    private static final String ARG_TITULO = "titulo";
    private static final String ARG_ROTULO_PRINCIPAL = "rotuloPrincipal";
    private static final String ARG_ROTULO_SECUNDARIO = "rotuloSecundario";

    private FragmentResumoValoresBinding binding;
    private ResumoValoresViewModel viewModel;

    public static ResumoValoresFragment newInstance(String chave, String titulo,
                                                    String rotuloPrincipal, String rotuloSecundario) {
        ResumoValoresFragment fragment = new ResumoValoresFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAVE, chave);
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_ROTULO_PRINCIPAL, rotuloPrincipal);
        args.putString(ARG_ROTULO_SECUNDARIO, rotuloSecundario);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResumoValoresBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configurarTextosIniciais();
        configurarViewModelEObservadores();
    }

    private void configurarTextosIniciais() {
        if (getArguments() != null) {
            binding.textoTituloSecao.setText(getArguments().getString(ARG_TITULO));
            binding.textoRotuloPrincipal.setText(getArguments().getString(ARG_ROTULO_PRINCIPAL));
            binding.textoRotuloSecundario.setText(getArguments().getString(ARG_ROTULO_SECUNDARIO));
        }
    }

    private void configurarViewModelEObservadores() {
        String chave = getArguments() != null ? getArguments().getString(ARG_CHAVE) : null;
        if (chave != null) {
            viewModel = new ViewModelProvider(requireActivity()).get(chave, ResumoValoresViewModel.class);
            viewModel.getState().observe(getViewLifecycleOwner(), state -> {
                if (state != null) {
                    binding.textoValorPrincipal.setText(formatCurrency(state.getValorPrincipal()));
                    binding.textoValorSecundario.setText(formatCurrency(state.getValorSecundario()));
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}