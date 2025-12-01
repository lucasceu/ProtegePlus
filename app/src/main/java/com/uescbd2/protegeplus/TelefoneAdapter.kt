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
    private val onClick: (TelefoneUtil) -> Unit,      // Clique simples (Editar/Ligar)
    private val onLongClick: (TelefoneUtil) -> Unit   // Clique longo (Deletar)
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

        fun bind(telefone: TelefoneUtil) {
            tvUnidade.text = telefone.unidadeSaude ?: "Local desconhecido"
            tvNumero.text = telefone.numero ?: ""

            itemView.setOnClickListener { onClick(telefone) }
            itemView.setOnLongClickListener {
                onLongClick(telefone)
                true
            }
        }
    }
}