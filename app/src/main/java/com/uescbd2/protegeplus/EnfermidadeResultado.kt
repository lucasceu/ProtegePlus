package com.uescbd2.protegeplus

/**
 * Classe para armazenar o resultado do algoritmo de ranking.
 */
data class EnfermidadeResultado(
    val enfermidade: ItemCiap,
    var pontuacao: Int = 0, // Quantos sintomas bateram
    val sintomasCoincidentes: List<String> = emptyList() // Quais sintomas bateram
)