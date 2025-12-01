package com.uescbd2.protegeplus

/**
 * Representa um usuário (pessoa) no banco de dados.
 * Nomes dos campos NÃO PRECISAM bater com o banco, pois o DatabaseHelper faz a tradução.
 */
data class Usuario(
    val id: Int = 0, // IdPessoa no banco
    val nome: String,
    val cpf: String?,
    val cargo: String?,
    val empresa: String?,
    val email: String,
    val senhaPlana: String,
    val telefone1: String? = null,
    val telefone2: String? = null
)