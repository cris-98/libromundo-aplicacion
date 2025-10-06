package com.example.libromundo.model

data class Libro(
    val id: Long = System.currentTimeMillis(),
    val titulo: String,
    val precio: Double,
    val cantidad: Int,
    val categoria: CategoriaLibro
) {
    fun calcularSubtotal(): Double {
        return precio * cantidad
    }
}