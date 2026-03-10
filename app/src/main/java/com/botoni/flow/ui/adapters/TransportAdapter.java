package com.botoni.flow.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.botoni.flow.R;
import com.botoni.flow.databinding.ItemTransportBinding;
import com.botoni.flow.domain.entities.Transport;

import java.util.Objects;

public class TransportAdapter extends ListAdapter<Transport, TransportAdapter.ViewHolder> {
    public TransportAdapter() {
        super(new DiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransportBinding binding = ItemTransportBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       holder.bind(getItem(position));
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTransportBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(Transport transport) {
            setText(binding.textoTipoVeiculo, transport.getName());
            setText(binding.textoQuantidadeVeiculos, String.valueOf(transport.getQuantity()));
            String textCapacity = formatCapacity(transport);
            setText(binding.textoCapacidadeCabecas, textCapacity);
        }

        private void setText(TextView textView, String text) {
            textView.setText(text != null ? text.trim() : "");
        }

        private String formatCapacity(Transport transport) {
            if (transport == null) return "";
            int min = transport.getInitialCapacity();
            int max = transport.getFinalCapacity();
            return itemView.getContext().getString(R.string.capacidade_cabecas, min, max);
        }
    }


    private static class DiffCallback extends DiffUtil.ItemCallback<Transport> {
        @Override
        public boolean areItemsTheSame(@NonNull Transport oldItem, @NonNull Transport newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName()) &&
                    Objects.equals(oldItem.getQuantity(), newItem.getQuantity());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transport oldItem, @NonNull Transport newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName()) &&
                    Objects.equals(oldItem.getQuantity(), newItem.getQuantity()) &&
                    Objects.equals(oldItem.getInitialCapacity(), newItem.getInitialCapacity()) &&
                    Objects.equals(oldItem.getFinalCapacity(), newItem.getFinalCapacity());
        }
    }
}
