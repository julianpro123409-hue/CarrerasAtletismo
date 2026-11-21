package com.julio.carrerasatletismo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CarrerasDao {

    // ---------- Carreras ----------
    @Query("SELECT * FROM carrera_vista ORDER BY fecha")
    fun carreras(): Flow<List<Carrera>>

    @Query("SELECT * FROM carrera_vista WHERE idCarrera = :id")
    fun carrera(id: String): Flow<Carrera?>

    @Query("SELECT COUNT(*) FROM carrera")
    suspend fun contarCarreras(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCarreras(carreras: List<CarreraEntity>)

    @Query(
        """UPDATE carrera SET nombre = :nombre, fecha = :fecha, idCiudad = :idCiudad,
           url = :url WHERE idCarrera = :id"""
    )
    suspend fun actualizarBasico(
        id: String, nombre: String, fecha: String, idCiudad: Long?, url: String
    )

    @Query(
        """UPDATE carrera SET tipoRecorrido = :tipo, desnivelM = :desnivel, terreno = :terreno,
           temperaturaC = :temperatura, humedad = :humedad, condiciones = :condiciones
           WHERE idCarrera = :id"""
    )
    suspend fun actualizarDetalle(
        id: String, tipo: String?, desnivel: Int?, terreno: String?,
        temperatura: Double?, humedad: Int?, condiciones: String?
    )

    // ---------- País y ciudad ----------
    @Query("SELECT idPais FROM pais WHERE nombre = :nombre")
    suspend fun idPais(nombre: String): Long?

    @Insert
    suspend fun insertarPais(pais: Pais): Long

    @Query("SELECT idCiudad FROM ciudad WHERE nombre = :nombre AND idPais IS :idPais")
    suspend fun idCiudad(nombre: String, idPais: Long?): Long?

    @Insert
    suspend fun insertarCiudad(ciudad: Ciudad): Long

    // ---------- Categorías ----------
    @Query("SELECT * FROM categoria ORDER BY edadMin")
    fun categorias(): Flow<List<Categoria>>

    @Query(
        """SELECT idCategoria FROM categoria
           WHERE :edad BETWEEN edadMin AND edadMax
           ORDER BY edadMin LIMIT 1"""
    )
    suspend fun categoriaParaEdad(edad: Int): Long?

    // ---------- Corredores ----------
    @Insert
    suspend fun insertarCorredor(corredor: CorredorEntity): Long

    @Query("DELETE FROM corredor WHERE idCorredor = :idCorredor")
    suspend fun borrarCorredor(idCorredor: Long)

    @Query("SELECT * FROM corredor_vista ORDER BY nombre")
    fun corredores(): Flow<List<Corredor>>

    @Transaction
    @Query("SELECT * FROM corredor_vista ORDER BY nombre")
    fun corredoresConCarreras(): Flow<List<CorredorConCarreras>>

    // ---------- Inscripciones ----------
    @Query("SELECT idCorredor FROM inscripcion WHERE idCarrera = :idCarrera AND dorsal = :dorsal")
    suspend fun duenoDelDorsal(idCarrera: String, dorsal: Int): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarInscripcion(inscripcion: Inscripcion)

    @Query(
        """UPDATE inscripcion SET dorsal = :dorsal, tiempoObjetivo = :tiempo
           WHERE idCorredor = :idCorredor AND idCarrera = :idCarrera"""
    )
    suspend fun actualizarInscripcion(idCorredor: Long, idCarrera: String, dorsal: Int, tiempo: String): Int

    @Query("DELETE FROM inscripcion WHERE idCorredor = :idCorredor AND idCarrera = :idCarrera")
    suspend fun quitarInscripcion(idCorredor: Long, idCarrera: String)

    @Query(
        """SELECT c.idCorredor AS idCorredor, c.nombre AS nombre, c.categoria AS categoria,
                  i.dorsal AS dorsal, i.tiempoObjetivo AS tiempoObjetivo,
                  r.tiempoReal AS tiempoReal,
                  CASE WHEN r.tiempoReal IS NULL THEN NULL
                       ELSE (SELECT COUNT(*) + 1 FROM resultado r2
                             WHERE r2.idCarrera = i.idCarrera AND r2.tiempoReal < r.tiempoReal)
                  END AS posicion
           FROM inscripcion i
           INNER JOIN corredor_vista c ON c.idCorredor = i.idCorredor
           LEFT JOIN resultado r ON r.idCorredor = i.idCorredor AND r.idCarrera = i.idCarrera
           WHERE i.idCarrera = :idCarrera
           ORDER BY (r.tiempoReal IS NULL), r.tiempoReal, i.dorsal"""
    )
    fun inscritos(idCarrera: String): Flow<List<InscritoEnCarrera>>

    // ---------- Resultados ----------
    @Upsert
    suspend fun guardarResultado(resultado: Resultado)

    @Query("DELETE FROM resultado WHERE idCorredor = :idCorredor AND idCarrera = :idCarrera")
    suspend fun borrarResultado(idCorredor: Long, idCarrera: String)
}
