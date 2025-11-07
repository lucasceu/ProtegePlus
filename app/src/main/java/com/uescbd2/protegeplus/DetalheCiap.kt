package com.uescbd2.protegeplus

/**
 * Representa os campos de detalhe de um item da 'tb_ciap'.
 */
data class DetalheCiap(
    val codigo: String, // codigo_ciap2
    val nomeLeigo: String?, // enfermidade_leigo
    val sintomasInclusos: String?,
    val sintomasExclusao: String?,
    val possiveisCid10: String?,
    val outrosSintomas: String?
)