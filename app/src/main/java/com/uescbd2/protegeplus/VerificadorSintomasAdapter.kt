package com.uescbd2.protegeplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class VerificadorSintomasAdapter(
    // A lista mestra, com TODOS os sintomas
    private val allSintomas: List<SintomaCheckbox>
) : RecyclerView.Adapter<VerificadorSintomasAdapter.SintomaViewHolder>() {

    // A lista que o usuário realmente vê (que será filtrada)
    private var displayedSintomas: MutableList<SintomaCheckbox> = allSintomas.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SintomaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_sintoma_checkbox, parent, false)
        return SintomaViewHolder(view)
    }

    override fun getItemCount(): Int = displayedSintomas.size

    override fun onBindViewHolder(holder: SintomaViewHolder, position: Int) {
        holder.bind(displayedSintomas[position])
    }

    // --- NOVA FUNÇÃO: Limpar seleção ---
    fun clearSelections() {
        // Limpa a seleção na lista mestra
        allSintomas.forEach { it.isChecked = false }
        // Avisa o adapter para redesenhar a tela
        notifyDataSetChanged()
    }

    // --- NOVA FUNÇÃO: Filtrar a lista ---
    fun filter(query: String) {
        displayedSintomas.clear()

        if (query.isEmpty()) {
            displayedSintomas.addAll(allSintomas)
        } else {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (item in allSintomas) {
                // Filtra pelo nome do sintoma
                if (item.item.nome?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery) == true) {
                    displayedSintomas.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    // --- FUNÇÃO ATUALIZADA: Pega os códigos da lista mestra ---
    fun getSelectedSymptomCodes(): ArrayList<String> {
        val selectedCodes = ArrayList<String>()
        // Lê da lista mestra (allSintomas)
        for (item in allSintomas) {
            if (item.isChecked) {
                selectedCodes.add(item.item.codigo)
            }
        }
        return selectedCodes
    }

    inner class SintomaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvSintomaNome)
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvSintomaCodigo)
        private val cbSintoma: CheckBox = itemView.findViewById(R.id.cbSintoma)

        init {
            itemView.setOnClickListener {
                // Pega o item da lista VISÍVEL
                val item = displayedSintomas[adapterPosition]
                item.isChecked = !item.isChecked
                cbSintoma.isChecked = item.isChecked
            }
        }

        fun bind(sintoma: SintomaCheckbox) {
            tvNome.text = sintoma.item.nome ?: "Sintoma desconhecido"
            tvCodigo.text = "(${sintoma.item.codigo})"
            cbSintoma.isChecked = sintoma.isChecked
        }
    }
}