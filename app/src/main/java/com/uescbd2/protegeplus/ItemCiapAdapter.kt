package com.uescbd2.protegeplus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemCiapAdapter(
    private val context: Context,
    private val itens: List<ItemCiap>,
    private val onItemClick: (ItemCiap) -> Unit
) : RecyclerView.Adapter<ItemCiapAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ciap, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = itens.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itens[position])
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvItemNome)
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvItemCodigo)

        init {
            itemView.setOnClickListener {
                onItemClick(itens[adapterPosition])
            }
        }

        fun bind(item: ItemCiap) {
            tvNome.text = item.nome ?: "Item não informado"
            tvCodigo.text = item.codigo
        }
    }
}