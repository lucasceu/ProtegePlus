package com.uescbd2.protegeplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

sealed class SbvListItem {
    data class Separator(val categoria: String, var isExpanded: Boolean = false) : SbvListItem()
    data class Item(val dados: ItemSbv) : SbvListItem()
}

class SbvAdapter(
    private var itensExibidos: List<SbvListItem>,
    private val onHeaderClick: (SbvListItem.Separator) -> Unit,
    private val onItemClick: (ItemSbv) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_sintoma, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sbv, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itensExibidos[position]
        if (holder is HeaderViewHolder && item is SbvListItem.Separator) {
            holder.bind(item)
        } else if (holder is ItemViewHolder && item is SbvListItem.Item) {
            holder.bind(item.dados)
        }
    }

    override fun getItemCount(): Int = itensExibidos.size

    override fun getItemViewType(position: Int): Int {
        return when (itensExibidos[position]) {
            is SbvListItem.Separator -> TYPE_HEADER
            is SbvListItem.Item -> TYPE_ITEM
        }
    }

    fun updateList(novaLista: List<SbvListItem>) {
        itensExibidos = novaLista
        notifyDataSetChanged()
    }

    // --- Header (Vermelho) ---
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeaderLetra)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivHeaderIcon) // Ícone da categoria (Ambulância)
        private val ivChevron: ImageView = itemView.findViewById(R.id.ivChevron) // A Seta que gira

        fun bind(separator: SbvListItem.Separator) {
            tvHeader.text = separator.categoria
            ivIcon.setImageResource(R.drawable.ic_ambulancia)

            // Lógica de Rotação CORRIGIDA
            // Assumindo que o ícone no XML (item_header_sintoma) já aponta para a DIREITA por padrão.
            if (separator.isExpanded) {
                ivChevron.rotation = 90f // Gira 90 graus para BAIXO
            } else {
                ivChevron.rotation = 0f  // Fica em 0 graus (DIREITA)
            }

            itemView.setOnClickListener { onHeaderClick(separator) }
        }
    }

    // --- Item Interno (Branco) ---
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvEnfermidadeNome)
        // A seta aqui é estática no XML, não mexemos nela via código.

        fun bind(item: ItemSbv) {
            tvNome.text = item.enfermidade
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}