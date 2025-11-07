package com.uescbd2.protegeplus

/**
 * Um modelo de dados genérico para representar um item
 * tanto da 'tb_ciap' quanto da 'procedimento_clinico'.
 */
data class ItemCiap(
    val codigo: String, // (ex: "A01" da tb_ciap ou "31" da procedimento_clinico)
    val nome: String?, // (o campo 'enfermidade_leigo' de ambas)
    val idGrupo: Int, // (o campo 'id_grupo_fk' de ambas)
    val textoSintomasInclusos: String? = null,
    val textoOutrosSintomas: String? = null
)