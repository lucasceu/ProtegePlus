package com.uescbd2.protegeplus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GrupoCiapAdapter(
    private val context: Context,
    private val grupos: List<GrupoCiap>,
    private val onItemClick: (GrupoCiap) -> Unit
) : RecyclerView.Adapter<GrupoCiapAdapter.GrupoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_grupo_ciap, parent, false)
        return GrupoViewHolder(view)
    }

    override fun getItemCount(): Int = grupos.size

    override fun onBindViewHolder(holder: GrupoViewHolder, position: Int) {
        holder.bind(grupos[position])
    }

    inner class GrupoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvGrupoNome)

        init {
            itemView.setOnClickListener {
                onItemClick(grupos[adapterPosition])
            }
        }

        fun bind(grupo: GrupoCiap) {
            tvNome.text = grupo.componente ?: "Grupo não informado"
        }
    }
}