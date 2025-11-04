package com.uescbd2.protegeplus

/**
 * Representa um usuário (pessoa) no banco de dados.
 * Nomes dos campos NÃO PRECISAM bater com o banco, pois o DatabaseHelper faz a tradução.
 */
data class Usuario(
    val IdPessoa: Int = 0, // O padrão 0 será ignorado pelo DatabaseHelper
    val nome: String,
    val cpf: String?,
    val cargo: String?,
    val telefone: String?,
    val empresa: String?,
    val email: String,
    val senhaPlana: String
)