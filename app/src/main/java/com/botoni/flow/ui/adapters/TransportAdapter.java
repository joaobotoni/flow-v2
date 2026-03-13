package com.botoni.flow.ui.adapters;

import static com.botoni.flow.ui.helpers.ViewHelper.setPluralText;
import static com.botoni.flow.ui.helpers.ViewHelper.setText;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

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
        return new ViewHolder(ItemTransportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransportBinding binding;

        ViewHolder(ItemTransportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Transport transport) {
            Context context = itemView.getContext();
            setText(binding.textoTipoVeiculo, transport.getName());
            setPluralText(binding.textoQuantidadeVeiculos, context, R.plurals.quantidade_veiculos, transport.getQuantity());
            setText(binding.textoCapacidadeCabecas, context, R.string.capacidade_cabecas, transport.getCapacity());
            setText(binding.textoPorcentagemOcupada, context, R.string.percent, transport.getPercent());
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<Transport> {
        @Override
        public boolean areItemsTheSame(@NonNull Transport oldItem, @NonNull Transport newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transport oldItem, @NonNull Transport newItem) {
            return oldItem.getId() == newItem.getId()
                    && Objects.equals(oldItem.getName(), newItem.getName())
                    && Objects.equals(oldItem.getQuantity(), newItem.getQuantity())
                    && Objects.equals(oldItem.getPercent(), newItem.getPercent())
                    && Objects.equals(oldItem.getCapacity(), newItem.getCapacity());
        }
    }
}