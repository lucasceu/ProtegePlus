package com.uescbd2.protegeplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultadoEnfermidadeAdapter(
    private val resultados: List<EnfermidadeResultado>,
    private val totalSintomasSelecionados: Int,
    private val onItemClick: (ItemCiap) -> Unit
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
            itemView.setOnClickListener {
                // Ao clicar, passa o ItemCiap (a enfermidade) para abrir os detalhes
                onItemClick(resultados[adapterPosition].enfermidade)
            }
        }

        fun bind(resultado: EnfermidadeResultado) {
            tvNome.text = resultado.enfermidade.nome ?: "Enfermidade desconhecida"

            // Texto do ranking (ex: "Compatível com 2 de 3 sintomas")
            tvPontuacao.text = "Compatível com ${resultado.pontuacao} de $totalSintomasSelecionados sintomas"
        }
    }
}