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

import java.util.ArrayList;
import java.util.Objects;

public class CategoryAdapter extends ListAdapter<CategoriaFrete, CategoryAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(CategoriaFrete categoria);
    }
    private final OnClickListener listener;
    private CategoriaFrete selected;

    public CategoryAdapter(OnClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelected(CategoriaFrete selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), selected, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoriaFrete categoria, CategoriaFrete selected, OnClickListener listener) {
            setText(binding.chipText, categoria.getDescricao());
            binding.chipCard.setChecked(Objects.equals(categoria, selected));
            binding.chipCard.setOnClickListener(v -> listener.onClick(categoria));
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<CategoriaFrete> {
        @Override
        public boolean areItemsTheSame(@NonNull CategoriaFrete oldItem, @NonNull CategoriaFrete newItem) {
            return Objects.equals(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull CategoriaFrete oldItem, @NonNull CategoriaFrete newItem) {
            return Objects.equals(oldItem, newItem);
        }
    }
}