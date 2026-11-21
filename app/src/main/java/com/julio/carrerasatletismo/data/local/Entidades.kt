package com.julio.carrerasatletismo.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/* ------------------------------------------------------------------
 * Modelo relacional (versión 2 de la base de datos)
 *
 *  PAIS (idPais PK, nombre UNIQUE)
 *  CIUDAD (idCiudad PK, nombre, idPais FK -> PAIS)
 *  CATEGORIA (idCategoria PK, nombre UNIQUE, edadMin, edadMax)
 *  CARRERA (idCarrera PK, nombre, fecha, idCiudad FK -> CIUDAD, url, ...)
 *  CORREDOR (idCorredor PK, nombre, edad, idCategoria FK -> CATEGORIA)
 *  INSCRIPCION (idCorredor PK/FK, idCarrera PK/FK, dorsal, tiempoObjetivo)
 *      UNIQUE (idCarrera, dorsal)
 *  RESULTADO (idCorredor PK, idCarrera PK, tiempoReal)
 *      FK (idCorredor, idCarrera) -> INSCRIPCION
 * ------------------------------------------------------------------ */

@Entity(
    tableName = "pais",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Pais(
    @PrimaryKey(autoGenerate = true) val idPais: Long = 0,
    val nombre: String
)

@Entity(
    tableName = "ciudad",
    foreignKeys = [
        ForeignKey(
            entity = Pais::class,
            parentColumns = ["idPais"],
            childColumns = ["idPais"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("idPais"), Index(value = ["nombre", "idPais"], unique = true)]
)
data class Ciudad(
    @PrimaryKey(autoGenerate = true) val idCiudad: Long = 0,
    val nombre: String,
    val idPais: Long?
)

@Entity(
    tableName = "categoria",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Categoria(
    @PrimaryKey(autoGenerate = true) val idCategoria: Long = 0,
    val nombre: String,
    val edadMin: Int,
    val edadMax: Int
)

@Entity(
    tableName = "carrera",
    foreignKeys = [
        ForeignKey(
            entity = Ciudad::class,
            parentColumns = ["idCiudad"],
            childColumns = ["idCiudad"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("idCiudad")]
)
data class CarreraEntity(
    @PrimaryKey val idCarrera: String,
    val nombre: String,
    val fecha: String,
    val idCiudad: Long?,
    val url: String,

    val tipoRecorrido: String? = null,
    val desnivelM: Int? = null,
    val terreno: String? = null,
    val temperaturaC: Double? = null,
    val humedad: Int? = null,
    val condiciones: String? = null
)

@Entity(
    tableName = "corredor",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["idCategoria"],
            childColumns = ["idCategoria"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("idCategoria")]
)
data class CorredorEntity(
    @PrimaryKey(autoGenerate = true) val idCorredor: Long = 0,
    val nombre: String,
    val edad: Int,
    val idCategoria: Long
)

@Entity(
    tableName = "inscripcion",
    primaryKeys = ["idCorredor", "idCarrera"],
    foreignKeys = [
        ForeignKey(
            entity = CorredorEntity::class,
            parentColumns = ["idCorredor"],
            childColumns = ["idCorredor"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CarreraEntity::class,
            parentColumns = ["idCarrera"],
            childColumns = ["idCarrera"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("idCarrera"),
        // Dos corredores no pueden llevar el mismo dorsal en la misma carrera
        Index(value = ["idCarrera", "dorsal"], unique = true)
    ]
)
data class Inscripcion(
    val idCorredor: Long,
    val idCarrera: String,
    val dorsal: Int,
    val tiempoObjetivo: String
)

@Entity(
    tableName = "resultado",
    primaryKeys = ["idCorredor", "idCarrera"],
    foreignKeys = [
        ForeignKey(
            entity = Inscripcion::class,
            parentColumns = ["idCorredor", "idCarrera"],
            childColumns = ["idCorredor", "idCarrera"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Resultado(
    val idCorredor: Long,
    val idCarrera: String,
    val tiempoReal: String
)
