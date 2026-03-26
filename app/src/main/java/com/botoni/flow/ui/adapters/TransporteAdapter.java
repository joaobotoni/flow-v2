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
import com.botoni.flow.databinding.ItemTransporteBinding;
import com.botoni.flow.ui.state.TransporteUiState;

import java.util.List;
import java.util.Objects;

public class TransporteAdapter extends ListAdapter<TransporteUiState, TransporteAdapter.ViewHolder> {

    public TransporteAdapter() {
        super(new DiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTransporteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
        holder.applyLayoutForDisplayMode(getItemCount() == 1);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.applyLayoutForDisplayMode(getItemCount() == 1);
    }

    @Override
    public void onCurrentListChanged(@NonNull List<TransporteUiState> previousList,
                                     @NonNull List<TransporteUiState> currentList) {
        super.onCurrentListChanged(previousList, currentList);
        boolean tamanhoMudou = (previousList.size() == 1) != (currentList.size() == 1);
        if (tamanhoMudou) notifyItemRangeChanged(0, currentList.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransporteBinding binding;

        ViewHolder(ItemTransporteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TransporteUiState estado) {
            Context context = itemView.getContext();
            setText(binding.textoTipoVeiculo, estado.nomeVeiculo);
            setPluralText(binding.textoQuantidadeVeiculos, context, R.plurals.quantidade_veiculos, estado.quantidade);
            setText(binding.textoCapacidadeCabecas, context, R.string.capacidade_cabecas, estado.capacidade);
            setText(binding.textoPorcentagemOcupada, context, R.string.percent, estado.ocupacao);
        }

        void applyLayoutForDisplayMode(boolean isSingleItem) {
            applyItemWidthConstraint(isSingleItem);
            applyCardEndMargin(isSingleItem, itemView.getContext());
        }

        private void applyItemWidthConstraint(boolean isSingleItem) {
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            params.width = isSingleItem ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
            itemView.setLayoutParams(params);
        }

        private void applyCardEndMargin(boolean isSingleItem, Context context) {
            ViewGroup.MarginLayoutParams cardParams = (ViewGroup.MarginLayoutParams) binding.getRoot().getLayoutParams();
            int margin = isSingleItem
                    ? context.getResources().getDimensionPixelSize(R.dimen.dimen_0)
                    : context.getResources().getDimensionPixelSize(R.dimen.dimen_12);
            cardParams.setMarginEnd(margin);
            binding.getRoot().setLayoutParams(cardParams);
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<TransporteUiState> {
        @Override
        public boolean areItemsTheSame(@NonNull TransporteUiState oldItem, @NonNull TransporteUiState newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransporteUiState oldItem, @NonNull TransporteUiState newItem) {
            return Objects.equals(oldItem.id, newItem.id)
                    && Objects.equals(oldItem.nomeVeiculo, newItem.nomeVeiculo)
                    && Objects.equals(oldItem.quantidade, newItem.quantidade)
                    && Objects.equals(oldItem.capacidade, newItem.capacidade)
                    && Objects.equals(oldItem.ocupacao, newItem.ocupacao);
        }
    }
}