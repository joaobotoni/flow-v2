    package com.botoni.flow.ui.fragments;

    import static com.botoni.flow.ui.helpers.ViewHelper.getBigDecimal;
    import static com.botoni.flow.ui.helpers.ViewHelper.orElse;

    import android.app.Dialog;
    import android.os.Bundle;
    import android.view.LayoutInflater;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.fragment.app.DialogFragment;

    import com.botoni.flow.R;
    import com.botoni.flow.databinding.FragmentDialogEdicaoPesoBinding;
    import com.botoni.flow.ui.state.DetalhePrecoBezerroUiState;
    import com.google.android.material.dialog.MaterialAlertDialogBuilder;

    import java.math.BigDecimal;
    import java.util.Locale;

    public class DialogEdicaoPesoFragment extends DialogFragment {
        public interface OnConfirmListener {
            void confirm(BigDecimal value);
        }
        private static final String ARG_ID = "id";
        private static final String ARG_PESO = "peso";

        private OnConfirmListener onConfirmListener;
        private FragmentDialogEdicaoPesoBinding binding;

        public static DialogEdicaoPesoFragment newInstance(DetalhePrecoBezerroUiState state) {
            DialogEdicaoPesoFragment fragment = new DialogEdicaoPesoFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(ARG_ID, state.getId());
            bundle.putString(ARG_PESO, state.getPeso().toPlainString());
            fragment.setArguments(bundle);
            return fragment;
        }

        public void setOnConfirmListener(OnConfirmListener onConfirmListener) {
            this.onConfirmListener = onConfirmListener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            binding = FragmentDialogEdicaoPesoBinding.inflate(getLayoutInflater());
            configurarView();
            return builder
                    .setView(binding.getRoot())
                    .setNegativeButton(R.string.cancelar, null)
                    .setPositiveButton(R.string.confirmar, (dialogInterface, i) -> onConfirmarClicado())
                    .create();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }

        private void configurarView() {
            Bundle args = getArguments();
            if (args == null || binding == null) return;
            binding.textoIdentificador.setText(String.format(Locale.getDefault(), "%04d", args.getInt(ARG_ID)));
            binding.entradaPesoAnimal.setText(args.getString(ARG_PESO));
        }

        private void onConfirmarClicado() {
            if (onConfirmListener != null) onConfirmListener.confirm(lerPeso());
        }

        private BigDecimal lerPeso() {
            if (binding == null) return BigDecimal.ZERO;
            return orElse(getBigDecimal(binding.entradaPesoAnimal), BigDecimal.ZERO);
        }
    }