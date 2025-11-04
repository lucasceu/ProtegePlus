package com.uescbd2.protegeplus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoriaAdapter(
    private val context: Context,
    private val categorias: List<Categoria>,
    private val onItemClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun getItemCount(): Int = categorias.size

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.bind(categoria)
    }

    inner class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivCategoriaIcon)
        private val tvNome: TextView = itemView.findViewById(R.id.tvCategoriaNome)

        init {
            itemView.setOnClickListener {
                onItemClick(categorias[adapterPosition])
            }
        }

        fun bind(categoria: Categoria) {
            tvNome.text = categoria.nome

            // --- Lógica para carregar o ícone dinamicamente ---
            if (categoria.icone != null) {
                // Pega o ID do drawable usando o nome (string) vindo do banco
                val resourceId = context.resources.getIdentifier(
                    categoria.icone,
                    "drawable",
                    context.packageName
                )

                if (resourceId != 0) { // 0 significa "não encontrado"
                    ivIcon.setImageResource(resourceId)
                } else {
                    ivIcon.setImageResource(R.drawable.ic_categoria_gerais) // Ícone padrão
                }
            } else {
                ivIcon.setImageResource(R.drawable.ic_categoria_gerais) // Ícone padrão
            }
        }
    }
}