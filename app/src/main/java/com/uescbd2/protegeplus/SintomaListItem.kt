package com.uescbd2.protegeplus

sealed class SintomaListItem {
    // Cabeçalho: Nome, Estado (Aberto/Fechado) e Ícone
    data class Separator(
        val letter: String, // Nome do Capítulo Completo
        var isExpanded: Boolean = false,
        val iconResId: Int // ID do Drawable
    ) : SintomaListItem()

    // Item: O sintoma em si
    data class Item(val data: SintomaCheckbox) : SintomaListItem()
}