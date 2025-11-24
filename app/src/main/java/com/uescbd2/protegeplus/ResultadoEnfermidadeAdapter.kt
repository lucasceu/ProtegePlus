package com.uescbd2.protegeplus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultadoEnfermidadeAdapter(
    private val resultados: List<EnfermidadeResultado>,
    private val totalSintomasSelecionados: Int,
    private val onItemClick: (EnfermidadeResultado) -> Unit
) : RecyclerView.Adapter<ResultadoEnfermidadeAdapter.ResultadoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultadoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_resultado_enfermidade, parent, false)
        return ResultadoViewHolder(view)
    }

    override fun getItemCount(): Int = resultados.size

    override fun onBindViewHolder(holder: ResultadoViewHolder, position: Int) {
        holder.bind(resultados[position])
    }

    inner class ResultadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvEnfermidadeNome)
        private val tvPontuacao: TextView = itemView.findViewById(R.id.tvEnfermidadePontuacao)

        init {
            itemView.setOnClickListener { onItemClick(resultados[adapterPosition]) }
        }

        fun bind(res: EnfermidadeResultado) {
            tvNome.text = res.enfermidade.nome ?: "Desconhecido"

            // Lógica visual de ranking
            val score = res.pontuacao
            tvPontuacao.text = "Compatibilidade: $score de $totalSintomasSelecionados sintomas"

            // Destaque visual se a pontuação for alta (opcional)
            if (score == totalSintomasSelecionados && score > 0) {
                tvPontuacao.setTextColor(Color.parseColor("#D32F2F")) // Vermelho (Match perfeito)
                tvPontuacao.text = "Alta Compatibilidade ($score/$totalSintomasSelecionados)"
            } else {
                tvPontuacao.setTextColor(Color.WHITE)
            }
        }
    }
}