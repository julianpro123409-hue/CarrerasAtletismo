package com.julio.carrerasatletismo.domain.repositorio

import com.julio.carrerasatletismo.domain.modelo.Carrera
import com.julio.carrerasatletismo.domain.modelo.Categoria
import com.julio.carrerasatletismo.domain.modelo.Corredor
import com.julio.carrerasatletismo.domain.modelo.CorredorConCarreras
import com.julio.carrerasatletismo.domain.modelo.InscritoEnCarrera
import kotlinx.coroutines.flow.Flow

// Contrato del repositorio (capa domain).
// El ViewModel depende de esta interfaz, no de Room ni de Retrofit.
interface CarrerasRepository {
    val carreras: Flow<List<Carrera>>
    val corredores: Flow<List<Corredor>>
    val corredoresConCarreras: Flow<List<CorredorConCarreras>>
    val categorias: Flow<List<Categoria>>

    fun carrera(id: String): Flow<Carrera?>
    fun inscritos(idCarrera: String): Flow<List<InscritoEnCarrera>>

    suspend fun hayCarreras(): Boolean
    suspend fun actualizarCarreras(): Int
    suspend fun cargarDetalle(id: String)

    suspend fun agregarCorredor(nombre: String, edad: Int)
    suspend fun borrarCorredor(idCorredor: Long)

    suspend fun inscribir(idCorredor: Long, idCarrera: String, dorsal: Int, tiempo: String): Boolean
    suspend fun quitarInscripcion(idCorredor: Long, idCarrera: String)

    suspend fun guardarResultado(idCorredor: Long, idCarrera: String, tiempoReal: String)
    suspend fun borrarResultado(idCorredor: Long, idCarrera: String)
}
