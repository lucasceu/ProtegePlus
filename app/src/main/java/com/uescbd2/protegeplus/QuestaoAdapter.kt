package com.uescbd2.protegeplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuestaoAdapter(
    private val questoes: List<Questao>,
    private val onClick: (Questao) -> Unit,
    private val onLongClick: (Questao) -> Unit
) : RecyclerView.Adapter<QuestaoAdapter.QuestaoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestaoViewHolder {
        // Usando o layout de pessoa emprestado, ou crie um list_item_questao.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_pessoa, parent, false)
        return QuestaoViewHolder(view)
    }

    override fun getItemCount(): Int = questoes.size

    override fun onBindViewHolder(holder: QuestaoViewHolder, position: Int) {
        holder.bind(questoes[position])
    }

    inner class QuestaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ajuste os IDs conforme o seu layout list_item_pessoa.xml
        private val tvDescricao: TextView = itemView.findViewById(R.id.tvNomePessoa)
        private val tvResposta: TextView = itemView.findViewById(R.id.tvCargoPessoa)
        // private val tvExtra: TextView = itemView.findViewById(R.id.tvEmailPessoa)

        fun bind(q: Questao) {
            tvDescricao.text = q.descricao
            tvDescricao.maxLines = 2
            tvResposta.text = "Resp: ${q.respostaCorreta}"

            // Se tiver o terceiro campo, esconda:
            // tvExtra.visibility = View.GONE

            itemView.setOnClickListener { onClick(q) }
            itemView.setOnLongClickListener {
                onLongClick(q)
                true
            }
        }
    }
}