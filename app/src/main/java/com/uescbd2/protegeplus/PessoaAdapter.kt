package com.uescbd2.protegeplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PessoaAdapter(
    private val pessoas: List<Usuario>,
    private val onClick: (Usuario) -> Unit,
    private val onLongClick: (Usuario) -> Unit
) : RecyclerView.Adapter<PessoaAdapter.PessoaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PessoaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_pessoa, parent, false)
        return PessoaViewHolder(view)
    }

    override fun getItemCount(): Int = pessoas.size

    override fun onBindViewHolder(holder: PessoaViewHolder, position: Int) {
        holder.bind(pessoas[position])
    }

    inner class PessoaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvNomePessoa)
        private val tvCargo: TextView = itemView.findViewById(R.id.tvCargoPessoa)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmailPessoa)

        fun bind(pessoa: Usuario) {
            tvNome.text = pessoa.nome
            tvCargo.text = "${pessoa.cargo ?: "Sem cargo"} | ${pessoa.empresa ?: ""}"
            tvEmail.text = pessoa.email

            itemView.setOnClickListener { onClick(pessoa) }
            itemView.setOnLongClickListener {
                onLongClick(pessoa)
                true
            }
        }
    }
}