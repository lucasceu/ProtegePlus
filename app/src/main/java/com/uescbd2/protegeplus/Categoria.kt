package com.uescbd2.protegeplus

/**
 * Representa uma Categoria de Sintoma, vinda da tabela 'categoria'.
 */
data class Categoria(
    val id: Int,
    val nome: String,
    val icone: String? // O nome do ícone, ex: "ic_categoria_gerais"
)