package com.uescbd2.protegeplus

/**
 * Representa um item na lista de resultados do verificador de sintomas.
 * Guarda a enfermidade (ItemCiap) e sua pontuação no ranking.
 */
data class EnfermidadeResultado(
    val enfermidade: ItemCiap,
    var pontuacao: Int = 0 // Quantos sintomas bateram
)