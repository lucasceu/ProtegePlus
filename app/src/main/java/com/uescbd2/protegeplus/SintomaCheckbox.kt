package com.uescbd2.protegeplus

/**
 * Modelo para a lista da tela VerificadorSintomasActivity.
 * Guarda o item CIAP (sintoma) e seu estado (marcado/não marcado).
 */
data class SintomaCheckbox(
    val item: ItemCiap, // O sintoma (ex: A03, Febre)
    var isChecked: Boolean = false
)