package com.example.libromundo.model

data class CompraLibros(
    val libros: List<Libro>,
    val subtotal: Double,
    val descuento: Double,
    val porcentaje: Int,
    val total: Double
)