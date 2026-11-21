package com.julio.carrerasatletismo.data.local

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CorredorConCarreras(
    @Embedded val corredor: Corredor,
    @Relation(
        parentColumn = "idCorredor",
        entityColumn = "idCarrera",
        associateBy = Junction(Inscripcion::class)
    )
    val carreras: List<Carrera>
)

data class CarreraConCorredores(
    @Embedded val carrera: Carrera,
    @Relation(
        parentColumn = "idCarrera",
        entityColumn = "idCorredor",
        associateBy = Junction(Inscripcion::class)
    )
    val corredores: List<Corredor>
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
