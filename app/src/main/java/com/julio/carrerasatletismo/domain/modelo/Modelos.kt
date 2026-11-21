package com.julio.carrerasatletismo.domain.modelo

// Modelos de dominio: clases puras de Kotlin, sin anotaciones de Room.
// La interfaz (UI) solo conoce estos modelos.

data class Carrera(
    val idCarrera: String,
    val nombre: String,
    val fecha: String,
    val ciudad: String,
    val pais: String,
    val url: String,
    val tipoRecorrido: String? = null,
    val desnivelM: Int? = null,
    val terreno: String? = null,
    val temperaturaC: Double? = null,
    val humedad: Int? = null,
    val condiciones: String? = null
)

data class Corredor(
    val idCorredor: Long,
    val nombre: String,
    val edad: Int,
    val categoria: String,
    val idCategoria: Long = 0
)

data class Categoria(
    val idCategoria: Long,
    val nombre: String,
    val edadMin: Int,
    val edadMax: Int
)

data class CorredorConCarreras(
    val corredor: Corredor,
    val carreras: List<Carrera>
)

data class InscritoEnCarrera(
    val idCorredor: Long,
    val nombre: String,
    val categoria: String,
    val dorsal: Int,
    val tiempoObjetivo: String,
    val tiempoReal: String? = null,
    val posicion: Int? = null
)
