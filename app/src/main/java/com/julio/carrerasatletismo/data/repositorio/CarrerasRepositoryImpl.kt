package com.julio.carrerasatletismo.data.repositorio

import androidx.room.withTransaction
import com.julio.carrerasatletismo.data.local.AppDatabase
import com.julio.carrerasatletismo.data.local.CarreraEntity
import com.julio.carrerasatletismo.data.local.Ciudad
import com.julio.carrerasatletismo.data.local.CorredorEntity
import com.julio.carrerasatletismo.data.local.Inscripcion
import com.julio.carrerasatletismo.data.local.Pais
import com.julio.carrerasatletismo.data.local.Resultado
import com.julio.carrerasatletismo.data.mapper.toDomain
import com.julio.carrerasatletismo.data.remote.RunDidaApi
import com.julio.carrerasatletismo.data.remote.comoTexto
import com.julio.carrerasatletismo.domain.modelo.Carrera
import com.julio.carrerasatletismo.domain.modelo.Categoria
import com.julio.carrerasatletismo.domain.modelo.Corredor
import com.julio.carrerasatletismo.domain.modelo.CorredorConCarreras
import com.julio.carrerasatletismo.domain.modelo.InscritoEnCarrera
import com.julio.carrerasatletismo.domain.repositorio.CarrerasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

// Implementación del repositorio: une Room (local) y Retrofit (remoto)
// y entrega modelos de dominio gracias a los mappers.
class CarrerasRepositoryImpl(
    private val db: AppDatabase,
    private val api: RunDidaApi
) : CarrerasRepository {

    private val dao = db.carrerasDao()

    override val carreras: Flow<List<Carrera>> =
        dao.carreras().map { lista -> lista.map { it.toDomain() } }

    override val corredores: Flow<List<Corredor>> =
        dao.corredores().map { lista -> lista.map { it.toDomain() } }

    override val corredoresConCarreras: Flow<List<CorredorConCarreras>> =
        dao.corredoresConCarreras().map { lista -> lista.map { it.toDomain() } }

    override val categorias: Flow<List<Categoria>> =
        dao.categorias().map { lista -> lista.map { it.toDomain() } }

    override fun carrera(id: String): Flow<Carrera?> =
        dao.carrera(id).map { it?.toDomain() }

    override fun inscritos(idCarrera: String): Flow<List<InscritoEnCarrera>> =
        dao.inscritos(idCarrera).map { lista -> lista.map { it.toDomain() } }

    override suspend fun hayCarreras(): Boolean = dao.contarCarreras() > 0

    /** Busca la ciudad (y su país) o la crea si no existe. */
    private suspend fun obtenerIdCiudad(ciudad: String, pais: String): Long? {
        val nombreCiudad = ciudad.trim()
        val nombrePais = pais.trim()
        if (nombreCiudad.isEmpty() && nombrePais.isEmpty()) return null

        val idPais = if (nombrePais.isEmpty()) null
        else dao.idPais(nombrePais) ?: dao.insertarPais(Pais(nombre = nombrePais))

        return dao.idCiudad(nombreCiudad, idPais)
            ?: dao.insertarCiudad(Ciudad(nombre = nombreCiudad, idPais = idPais))
    }

    override suspend fun actualizarCarreras(): Int {
        val respuesta = api.obtenerMaratones()
        val lista = respuesta.active.orEmpty().mapNotNull { dto ->
            val id = dto.id ?: return@mapNotNull null
            id to dto
        }
        db.withTransaction {
            lista.forEach { (id, dto) ->
                val nombre = dto.name.comoTexto().ifBlank { id }
                val fecha = dto.date.orEmpty()
                val url = dto.url.orEmpty()
                val idCiudad = obtenerIdCiudad(dto.city.orEmpty(), dto.country.orEmpty())
                dao.insertarCarreras(listOf(CarreraEntity(id, nombre, fecha, idCiudad, url)))
                dao.actualizarBasico(id, nombre, fecha, idCiudad, url)
            }
        }
        return lista.size
    }

    override suspend fun cargarDetalle(id: String) {
        val detalle = api.obtenerMaraton(id).marathon ?: return
        dao.actualizarDetalle(
            id = id,
            tipo = detalle.course?.type,
            desnivel = detalle.course?.elevationGain?.roundToInt(),
            terreno = detalle.course?.terrain,
            temperatura = detalle.weather?.avgTempC,
            humedad = detalle.weather?.humidity?.roundToInt(),
            condiciones = detalle.weather?.conditions
        )
    }

    /** La categoría se toma de la tabla CATEGORIA según la edad. */
    override suspend fun agregarCorredor(nombre: String, edad: Int) {
        val idCategoria = dao.categoriaParaEdad(edad)
            ?: error("No hay categoría para $edad años")
        dao.insertarCorredor(CorredorEntity(nombre = nombre, edad = edad, idCategoria = idCategoria))
    }

    override suspend fun borrarCorredor(idCorredor: Long) = dao.borrarCorredor(idCorredor)

    /** Devuelve false si el dorsal ya lo usa otro corredor en esa carrera. */
    override suspend fun inscribir(
        idCorredor: Long, idCarrera: String, dorsal: Int, tiempo: String
    ): Boolean = db.withTransaction {
        val dueno = dao.duenoDelDorsal(idCarrera, dorsal)
        if (dueno != null && dueno != idCorredor) return@withTransaction false
        val cambiadas = dao.actualizarInscripcion(idCorredor, idCarrera, dorsal, tiempo)
        if (cambiadas == 0) dao.insertarInscripcion(Inscripcion(idCorredor, idCarrera, dorsal, tiempo))
        true
    }

    override suspend fun quitarInscripcion(idCorredor: Long, idCarrera: String) =
        dao.quitarInscripcion(idCorredor, idCarrera)

    override suspend fun guardarResultado(idCorredor: Long, idCarrera: String, tiempoReal: String) =
        dao.guardarResultado(Resultado(idCorredor, idCarrera, tiempoReal))

    override suspend fun borrarResultado(idCorredor: Long, idCarrera: String) =
        dao.borrarResultado(idCorredor, idCarrera)
}
