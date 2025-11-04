package com.uescbd2.protegeplus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TelefoneAdapter(
    private val context: Context,
    private val telefones: List<TelefoneUtil>,
    private val onItemClick: (TelefoneUtil) -> Unit
) : RecyclerView.Adapter<TelefoneAdapter.TelefoneViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TelefoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_emergencia, parent, false)
        return TelefoneViewHolder(view)
    }

    override fun getItemCount(): Int = telefones.size

    override fun onBindViewHolder(holder: TelefoneViewHolder, position: Int) {
        holder.bind(telefones[position])
    }

    inner class TelefoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUnidade: TextView = itemView.findViewById(R.id.tvUnidadeSaude)
        private val tvNumero: TextView = itemView.findViewById(R.id.tvNumeroTelefone)

        init {
            itemView.setOnClickListener {
                onItemClick(telefones[adapterPosition])
            }
        }

        fun bind(telefone: TelefoneUtil) {
            tvUnidade.text = telefone.unidadeSaude ?: "Unidade não informada"
            tvNumero.text = telefone.numero ?: "Número não informado"
        }
    }
}