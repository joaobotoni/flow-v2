package com.botoni.flow.ui.adapters;

import static com.botoni.flow.ui.helpers.ViewHelper.setText;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.botoni.flow.databinding.ItemCategoryBinding;
import com.botoni.flow.data.source.local.entities.CategoriaFrete;
import com.botoni.flow.ui.state.CategoriaUiState;

import java.util.Objects;

public class CategoriaAdapter extends ListAdapter<CategoriaUiState, CategoriaAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(CategoriaUiState categoria);
    }

    private final OnClickListener listener;

    public CategoriaAdapter(OnClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoriaUiState estado, OnClickListener listener) {
            setText(binding.chipText, estado.descricao);
            binding.chipCard.setChecked(estado.selecionada);
            binding.chipCard.setOnClickListener(v -> listener.onClick(estado));
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<CategoriaUiState> {
        @Override
        public boolean areItemsTheSame(@NonNull CategoriaUiState oldItem, @NonNull CategoriaUiState newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CategoriaUiState oldItem, @NonNull CategoriaUiState newItem) {
            return oldItem.id == newItem.id
                    && Objects.equals(oldItem.descricao, newItem.descricao)
                    && oldItem.selecionada == newItem.selecionada;
        }
    }
}