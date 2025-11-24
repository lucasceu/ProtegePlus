package com.uescbd2.protegeplus

sealed class SintomaListItem {
    // Agora o Separator sabe se está aberto ou fechado
    data class Separator(
        val letter: String,
        var isExpanded: Boolean = false,
        val count: Int // Opcional: mostrar quantos itens tem (ex: "A (5)")
    ) : SintomaListItem()

    data class Item(val data: SintomaCheckbox) : SintomaListItem()
}