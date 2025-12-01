package com.uescbd2.protegeplus

/**
 * Representa uma questão pronta para ser exibida,
 * contendo o cabeçalho (pergunta) e a lista de opções (alternativas).
 */
data class QuestaoCompleta(
    val questao: Questao,
    val alternativas: List<Alternativa>
)