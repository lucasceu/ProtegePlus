package com.uescbd2.protegeplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VerificadorSintomasAdapter(
    private var itensExibidos: List<SintomaListItem>,
    private val onHeaderClick: (SintomaListItem.Separator) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header_sintoma, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_sintoma_checkbox, parent, false)
            SintomaViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itensExibidos[position]
        if (holder is HeaderViewHolder && item is SintomaListItem.Separator) {
            holder.bind(item)
        } else if (holder is SintomaViewHolder && item is SintomaListItem.Item) {
            holder.bind(item.data)
        }
    }

    override fun getItemCount(): Int = itensExibidos.size

    override fun getItemViewType(position: Int): Int {
        return when (itensExibidos[position]) {
            is SintomaListItem.Separator -> TYPE_HEADER
            is SintomaListItem.Item -> TYPE_ITEM
        }
    }

    fun updateList(novaLista: List<SintomaListItem>) {
        itensExibidos = novaLista
        notifyDataSetChanged()
    }

    fun getSelectedCodes(): ArrayList<String> {
        val selected = ArrayList<String>()
        for (obj in itensExibidos) {
            if (obj is SintomaListItem.Item && obj.data.isChecked) {
                selected.add(obj.data.item.codigo)
            }
        }
        return selected
    }

    // --- ViewHolders ---

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeaderLetra)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivHeaderIcon)
        private val ivChevron: ImageView = itemView.findViewById(R.id.ivChevron)

        fun bind(separator: SintomaListItem.Separator) {
            // Exibe apenas o nome (sem números)
            tvHeader.text = separator.letter

            // Define o ícone correto
            ivIcon.setImageResource(separator.iconResId)

            // Gira a seta
            if (separator.isExpanded) {
                ivChevron.rotation = 180f
            } else {
                ivChevron.rotation = 0f
            }

            itemView.setOnClickListener {
                onHeaderClick(separator)
            }
        }
    }

    inner class SintomaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvSintomaNome)
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvSintomaCodigo)
        private val cbSintoma: CheckBox = itemView.findViewById(R.id.cbSintoma)

        fun bind(sintomaCheck: SintomaCheckbox) {
            tvNome.text = sintomaCheck.item.nome
            tvCodigo.text = "(${sintomaCheck.item.codigo})"

            cbSintoma.setOnCheckedChangeListener(null)
            cbSintoma.isChecked = sintomaCheck.isChecked

            val clickListener = View.OnClickListener {
                sintomaCheck.isChecked = !sintomaCheck.isChecked
                cbSintoma.isChecked = sintomaCheck.isChecked
            }
            itemView.setOnClickListener(clickListener)
            cbSintoma.setOnClickListener { sintomaCheck.isChecked = cbSintoma.isChecked }
        }
    }
}