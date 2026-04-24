package com.botoni.flow.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.botoni.flow.databinding.ItemEmpresaBinding;
import com.botoni.flow.ui.state.EmpresaUiState;

import java.util.Objects;

public class EmpresaAdapter extends ListAdapter<EmpresaUiState, EmpresaAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(EmpresaUiState empresa);
    }

    private final OnClickListener listener;

    public EmpresaAdapter(OnClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemEmpresaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemEmpresaBinding binding;

        ViewHolder(ItemEmpresaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(EmpresaUiState item, OnClickListener listener) {
            binding.textoNomeEmpresa.setText(item.getNome());
            binding.textoIniciais.setText(item.getIniciais());
            binding.cardEmpresa.setChecked(item.isSelecionada());
            binding.cardEmpresa.setOnClickListener(v -> listener.onClick(item));
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<EmpresaUiState> {
        @Override
        public boolean areItemsTheSame(@NonNull EmpresaUiState o, @NonNull EmpresaUiState n) {
            return Objects.equals(o.getId(), n.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull EmpresaUiState o, @NonNull EmpresaUiState n) {
            return Objects.equals(o.getId(), n.getId())
                    && Objects.equals(o.getNome(), n.getNome())
                    && Objects.equals(o.getIniciais(), n.getIniciais())
                    && o.isSelecionada() == n.isSelecionada();
        }
    }
}