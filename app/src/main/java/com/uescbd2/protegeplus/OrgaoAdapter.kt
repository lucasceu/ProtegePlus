package com.uescbd2.protegeplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrgaoAdapter(
    private val orgaos: List<ItemCiap>,
    private val onItemClick: (ItemCiap) -> Unit
) : RecyclerView.Adapter<OrgaoAdapter.OrgaoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrgaoViewHolder {
        // Reutiliza o layout de grupo, que é visualmente compatível
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_grupo_ciap, parent, false)
        return OrgaoViewHolder(view)
    }

    override fun getItemCount(): Int = orgaos.size

    override fun onBindViewHolder(holder: OrgaoViewHolder, position: Int) {
        holder.bind(orgaos[position])
    }

    inner class OrgaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvGrupoNome)

        init {
            itemView.setOnClickListener { onItemClick(orgaos[adapterPosition]) }
        }

        fun bind(item: ItemCiap) {
            // Exibe: "F - Olhos"
            tvNome.text = "${item.codigo} - ${item.nome}"
        }
    }
}