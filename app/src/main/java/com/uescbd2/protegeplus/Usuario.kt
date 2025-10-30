package com.uescbd2.protegeplus

/**
 * Representa um usuário (pessoa) no banco de dados.
 * Nomes dos campos correspondem às colunas da tabela 'pessoa' no .db.
 */
data class Usuario(
    val IdPessoa: Int = 0, // Chave primária - NOME CORRIGIDO (maiúsculas)
    val nome: String,
    val cpf: String?,
    val cargo: String?,
    val telefone: String?,
    val empresa: String?,
    val email: String,
    val senhaPlana: String // INSEGURO!
)