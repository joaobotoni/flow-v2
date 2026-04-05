package com.botoni.flow.ui.adapters;

import static com.botoni.flow.ui.helpers.NumberHelper.formatCurrency;
import static com.botoni.flow.ui.helpers.ViewHelper.setText;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.botoni.flow.databinding.ItemPrecificacaoBezerroBinding;
import com.botoni.flow.ui.state.DetalhePrecoBezerroUiState;

import java.util.Locale;
import java.util.Objects;

public class DetalhePrecificacaoAdapter extends ListAdapter<DetalhePrecoBezerroUiState, DetalhePrecificacaoAdapter.ViewHolder> {
    public interface OnDetalheActionListener {
        void onEdit(DetalhePrecoBezerroUiState detalhe);
        void onRemove(int id);
    }
    private final OnDetalheActionListener listener;

    public DetalhePrecificacaoAdapter(OnDetalheActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPrecificacaoBezerroBinding binding = ItemPrecificacaoBezerroBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DetalhePrecificacaoAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPrecificacaoBezerroBinding binding;
        public ViewHolder(@NonNull ItemPrecificacaoBezerroBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DetalhePrecoBezerroUiState detalhe, OnDetalheActionListener actionListener) {
            binding.textoIdentificador.setText(String.format(Locale.getDefault(), "%04d", detalhe.getId()));
            setText(binding.textoValorPeso, formatCurrency(detalhe.getPeso()));
            setText(binding.textoValorPorKg, formatCurrency(detalhe.getValorPorKg()));
            setText(binding.textoValorUnitario, formatCurrency(detalhe.getValorTotal()));
            binding.btnEditar.setOnClickListener(v -> actionListener.onEdit(detalhe));
            binding.btnExcluir.setOnClickListener(v -> actionListener.onRemove(detalhe.getId()));
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<DetalhePrecoBezerroUiState> {

        @Override
        public boolean areItemsTheSame(@NonNull DetalhePrecoBezerroUiState oldItem, @NonNull DetalhePrecoBezerroUiState newItem) {
            return Objects.equals(oldItem.getPeso(), newItem.getPeso())
                    && Objects.equals(oldItem.getValorPorKg(), newItem.getValorPorKg())
                    && Objects.equals(oldItem.getValorTotal(), newItem.getValorTotal());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DetalhePrecoBezerroUiState oldItem, @NonNull DetalhePrecoBezerroUiState newItem) {
            return Objects.equals(oldItem.getPeso(), newItem.getPeso())
                    && Objects.equals(oldItem.getValorPorKg(), newItem.getValorPorKg())
                    && Objects.equals(oldItem.getValorTotal(), newItem.getValorTotal());
        }
    }
}
