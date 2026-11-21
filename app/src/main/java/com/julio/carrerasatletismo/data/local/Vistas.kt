package com.julio.carrerasatletismo.data.local

import androidx.room.DatabaseView

// Las vistas juntan las tablas normalizadas para que la interfaz
// siga recibiendo la carrera con su ciudad/país y el corredor con su categoría.

const val CARRERA_VISTA_SQL =
    "SELECT c.idCarrera AS idCarrera, c.nombre AS nombre, c.fecha AS fecha, " +
        "COALESCE(ci.nombre, '') AS ciudad, COALESCE(p.nombre, '') AS pais, c.url AS url, " +
        "c.tipoRecorrido AS tipoRecorrido, c.desnivelM AS desnivelM, c.terreno AS terreno, " +
        "c.temperaturaC AS temperaturaC, c.humedad AS humedad, c.condiciones AS condiciones " +
        "FROM carrera c " +
        "LEFT JOIN ciudad ci ON ci.idCiudad = c.idCiudad " +
        "LEFT JOIN pais p ON p.idPais = ci.idPais"

const val CORREDOR_VISTA_SQL =
    "SELECT co.idCorredor AS idCorredor, co.nombre AS nombre, co.edad AS edad, " +
        "ca.nombre AS categoria, co.idCategoria AS idCategoria " +
        "FROM corredor co " +
        "INNER JOIN categoria ca ON ca.idCategoria = co.idCategoria"

@DatabaseView(viewName = "carrera_vista", value = CARRERA_VISTA_SQL)
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

@DatabaseView(viewName = "corredor_vista", value = CORREDOR_VISTA_SQL)
data class Corredor(
    val idCorredor: Long,
    val nombre: String,
    val edad: Int,
    val categoria: String,
    val idCategoria: Long = 0
)
