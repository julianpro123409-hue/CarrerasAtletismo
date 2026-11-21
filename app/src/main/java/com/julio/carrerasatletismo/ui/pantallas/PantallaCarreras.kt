package com.julio.carrerasatletismo.ui.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.modelo.Carrera
import com.julio.carrerasatletismo.ui.CarrerasViewModel
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme

@Composable
fun PantallaCarreras(
    vm: CarrerasViewModel,
    onRegresar: () -> Unit,
    onAbrirCarrera: (String) -> Unit
) {
    val carreras by vm.carreras.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.cargarSiEstaVacio() }
    LaunchedEffect(vm.mensaje) {
        vm.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    ContenidoCarreras(
        carreras = carreras,
        cargando = vm.cargando,
        snackbar = snackbar,
        onRegresar = onRegresar,
        onActualizar = { vm.actualizarCarreras() },
        onAbrirCarrera = onAbrirCarrera
    )
}

@Composable
fun ContenidoCarreras(
    carreras: List<Carrera>,
    cargando: Boolean,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    onRegresar: () -> Unit = {},
    onActualizar: () -> Unit = {},
    onAbrirCarrera: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.carreras),
                onRegresar = onRegresar,
                acciones = {
                    IconButton(onClick = onActualizar, enabled = !cargando) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.actualizar))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())

            if (carreras.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    if (cargando) CircularProgressIndicator()
                    else Text(
                        stringResource(R.string.sin_carreras),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                ) {
                    items(carreras, key = { it.idCarrera }) { carrera ->
                        TarjetaCarrera(carrera) { onAbrirCarrera(carrera.idCarrera) }
                    }
                    item {
                        Text(
                            stringResource(R.string.atribucion),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaCarrera(carrera: Carrera, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val partes = formatearFecha(carrera.fecha).split(" ")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            partes.getOrElse(0) { "" },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            partes.getOrElse(1) { "" }.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(carrera.nombre, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        listOf(carrera.ciudad, carrera.pais).filter { it.isNotBlank() }.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    formatearFecha(carrera.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaCarrerasPreview() {
    CarrerasAtletismoTheme {
        ContenidoCarreras(
            carreras = listOf(
                Carrera("tokyo2027", "Tokyo Marathon", "2027-03-07", "Tokyo", "Japan", ""),
                Carrera("boston2027", "Boston Marathon", "2027-04-19", "Boston", "USA", ""),
                Carrera("cdmx2027", "Maratón de la Ciudad de México", "2027-08-29", "Ciudad de México", "Mexico", "")
            ),
            cargando = false
        )
    }
}
