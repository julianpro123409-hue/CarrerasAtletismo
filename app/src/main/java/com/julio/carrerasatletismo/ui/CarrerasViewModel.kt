package com.julio.carrerasatletismo.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.julio.carrerasatletismo.CarrerasApp
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.modelo.Corredor
import com.julio.carrerasatletismo.domain.normalizarTiempo
import com.julio.carrerasatletismo.domain.repositorio.CarrerasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// El repositorio llega por el constructor (inyección de dependencias).
class CarrerasViewModel(
    app: Application,
    private val repo: CarrerasRepository
) : AndroidViewModel(app) {

    val carreras = repo.carreras
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val corredores = repo.corredores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val corredoresConCarreras = repo.corredoresConCarreras
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var cargando by mutableStateOf(false)
        private set

    var mensaje by mutableStateOf<String?>(null)
        private set

    private fun texto(id: Int, vararg args: Any): String =
        getApplication<Application>().getString(id, *args)

    fun carrera(id: String) = repo.carrera(id)
    fun inscritos(idCarrera: String) = repo.inscritos(idCarrera)

    fun cargarSiEstaVacio() = viewModelScope.launch {
        if (!repo.hayCarreras()) actualizarCarreras()
    }

    fun actualizarCarreras() = viewModelScope.launch {
        cargando = true
        mensaje = try {
            texto(R.string.msg_carreras_descargadas, repo.actualizarCarreras())
        } catch (e: Exception) {
            texto(R.string.msg_error_api, e.message.orEmpty())
        }
        cargando = false
    }

    fun cargarDetalle(id: String) = viewModelScope.launch {
        try {
            repo.cargarDetalle(id)
        } catch (e: Exception) {
            mensaje = texto(R.string.msg_error_detalle, e.message.orEmpty())
        }
    }

    fun agregarCorredor(nombre: String, edad: Int) = viewModelScope.launch {
        mensaje = try {
            repo.agregarCorredor(nombre, edad)
            texto(R.string.msg_corredor_agregado)
        } catch (e: Exception) {
            e.message.orEmpty()
        }
    }

    fun borrarCorredor(corredor: Corredor) = viewModelScope.launch {
        repo.borrarCorredor(corredor.idCorredor)
    }

    fun inscribir(idCorredor: Long, idCarrera: String, dorsal: Int, tiempo: String) =
        viewModelScope.launch {
            mensaje = if (repo.inscribir(idCorredor, idCarrera, dorsal, tiempo))
                texto(R.string.msg_corredor_inscrito)
            else
                texto(R.string.msg_dorsal_ocupado, dorsal)
        }

    fun quitarInscripcion(idCorredor: Long, idCarrera: String) = viewModelScope.launch {
        repo.quitarInscripcion(idCorredor, idCarrera)
    }

    fun guardarResultado(idCorredor: Long, idCarrera: String, tiempo: String) =
        viewModelScope.launch {
            val normalizado = normalizarTiempo(tiempo)
            mensaje = if (normalizado == null) {
                texto(R.string.msg_tiempo_invalido)
            } else {
                repo.guardarResultado(idCorredor, idCarrera, normalizado)
                texto(R.string.msg_resultado_guardado)
            }
        }

    fun borrarResultado(idCorredor: Long, idCarrera: String) = viewModelScope.launch {
        repo.borrarResultado(idCorredor, idCarrera)
    }

    fun limpiarMensaje() {
        mensaje = null
    }

    companion object {
        // Fábrica: toma el repositorio del contenedor de dependencias
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CarrerasApp
                CarrerasViewModel(app, app.contenedor.repositorio)
            }
        }
    }
}
