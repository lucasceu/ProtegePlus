package com.uescbd2.protegeplus

import java.io.Serializable

data class ItemSbv(
    val id: Int = 0,
    val categoria: String,
    val enfermidade: String,
    val procedimentos: String
) : Serializable