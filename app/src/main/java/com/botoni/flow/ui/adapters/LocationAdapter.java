package com.botoni.flow.ui.adapters;

import static com.botoni.flow.ui.helpers.ViewHelper.isNotEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.noneEmpty;
import static com.botoni.flow.ui.helpers.ViewHelper.setText;

import android.location.Address;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.botoni.flow.databinding.ItemEnderecoBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocationAdapter extends ListAdapter<Address, LocationAdapter.ViewHolder> {

    @FunctionalInterface
    public interface OnClickListener {
        void onClick(Address address);
    }

    private final OnClickListener listener;

    public LocationAdapter(OnClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @Override
    public void submitList(@Nullable List<Address> list) {
        if (list == null) {
            super.submitList(null);
            return;
        }
        List<Address> filtered = new ArrayList<>();
        for (Address address : list) {
            if (noneEmpty(address.getLocality(), address.getAdminArea())) {
                filtered.add(address);
            }
        }
        super.submitList(filtered);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemEnderecoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemEnderecoBinding binding;

        ViewHolder(ItemEnderecoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Address address, OnClickListener listener) {
            setText(binding.textoNomeCidade, address.getLocality());
            setText(binding.textoNomeEstado, address.getAdminArea());
            itemView.setOnClickListener(v -> listener.onClick(address));
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<Address> {
        @Override
        public boolean areItemsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
            return oldItem.getLatitude() == newItem.getLatitude()
                    && oldItem.getLongitude() == newItem.getLongitude();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Address oldItem, @NonNull Address newItem) {
            return Objects.equals(oldItem.getLocality(), newItem.getLocality())
                    && Objects.equals(oldItem.getAdminArea(), newItem.getAdminArea());
        }
    }
}