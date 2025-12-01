package com.uescbd2.protegeplus

data class Questao(
    val id: Int = 0,
    val descricao: String,
    val respostaCorreta: String?, // Ex: "A", "B"
    val idPessoaFk: Int? = null,
    val idProcedimentoFk: Int? = null
)