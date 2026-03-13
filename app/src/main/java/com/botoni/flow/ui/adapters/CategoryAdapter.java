package com.botoni.flow.ui.adapters;

import static com.botoni.flow.ui.helpers.ViewHelper.setText;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.botoni.flow.databinding.ItemCategoryBinding;
import com.botoni.flow.domain.entities.Category;

import java.util.Objects;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(Category category);
    }

    private final OnClickListener listener;

    public CategoryAdapter(OnClickListener listener) {
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

        void bind(Category category, OnClickListener listener) {
            setText(binding.chipText, category.getDescription());
            binding.chipCard.setChecked(category.isCheck());
            binding.chipCard.setOnClickListener(v -> listener.onClick(category));
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<Category> {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId() == newItem.getId()
                    && Objects.equals(oldItem.getDescription(), newItem.getDescription())
                    && oldItem.isCheck() == newItem.isCheck();
        }
    }
}