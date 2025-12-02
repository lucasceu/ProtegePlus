package com.uescbd2.protegeplus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class TelefoneAdapter(
    private val context: Context,
    listaInicial: List<TelefoneUtil>, // Recebe a lista do banco
    private val onClick: (TelefoneUtil) -> Unit,
    private val onLongClick: (TelefoneUtil) -> Unit
) : RecyclerView.Adapter<TelefoneAdapter.TelefoneViewHolder>() {

    // Lista imutável com TODOS os telefones (Cópia de segurança)
    private val listaCompleta: List<TelefoneUtil> = ArrayList(listaInicial)

    // Lista mutável que será exibida na tela (pode ser filtrada)
    private var listaExibida: MutableList<TelefoneUtil> = ArrayList(listaInicial)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TelefoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_emergencia, parent, false)
        return TelefoneViewHolder(view)
    }

    override fun getItemCount(): Int = listaExibida.size

    override fun onBindViewHolder(holder: TelefoneViewHolder, position: Int) {
        holder.bind(listaExibida[position])
    }

    // --- NOVA FUNÇÃO: Lógica de Filtragem ---
    fun filtrar(texto: String) {
        val busca = texto.lowercase(Locale.getDefault())

        listaExibida.clear()

        if (busca.isEmpty()) {
            // Se a busca estiver vazia, restaura a lista completa
            listaExibida.addAll(listaCompleta)
        } else {
            // Se tiver texto, filtra
            for (item in listaCompleta) {
                val nome = item.unidadeSaude?.lowercase(Locale.getDefault()) ?: ""
                val numero = item.numero ?: ""

                // Verifica se o nome OU o número contém o texto digitado
                if (nome.contains(busca) || numero.contains(busca)) {
                    listaExibida.add(item)
                }
            }
        }
        notifyDataSetChanged() // Avisa a tela para atualizar
    }

    inner class TelefoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUnidade: TextView = itemView.findViewById(R.id.tvUnidadeSaude)
        private val tvNumero: TextView = itemView.findViewById(R.id.tvNumeroTelefone)

        fun bind(telefone: TelefoneUtil) {
            tvUnidade.text = telefone.unidadeSaude ?: "Local desconhecido"
            tvNumero.text = telefone.numero ?: ""

            itemView.setOnClickListener { onClick(telefone) }
            itemView.setOnLongClickListener {
                onLongClick(telefone)
                true
            }
        }
    }
}