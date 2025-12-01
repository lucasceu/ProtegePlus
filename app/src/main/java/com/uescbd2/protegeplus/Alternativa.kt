package com.uescbd2.protegeplus

data class Alternativa(
    val id: Int = 0,
    val letra: String, // "A", "B", "C"
    val descricao: String,
    val idQuestaoFk: Int
)