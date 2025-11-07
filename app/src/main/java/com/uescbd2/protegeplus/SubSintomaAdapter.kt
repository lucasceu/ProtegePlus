package com.uescbd2.protegeplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubSintomaAdapter(
    private val subSintomas: List<SubSintoma>
) : RecyclerView.Adapter<SubSintomaAdapter.SubSintomaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubSintomaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_sub_sintoma, parent, false)
        return SubSintomaViewHolder(view)
    }

    override fun getItemCount(): Int = subSintomas.size

    override fun onBindViewHolder(holder: SubSintomaViewHolder, position: Int) {
        holder.bind(subSintomas[position])
    }

    inner class SubSintomaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvSubSintomaNome)

        fun bind(subSintoma: SubSintoma) {
            tvNome.text = subSintoma.nome ?: "Sintoma não informado"
        }
    }
}