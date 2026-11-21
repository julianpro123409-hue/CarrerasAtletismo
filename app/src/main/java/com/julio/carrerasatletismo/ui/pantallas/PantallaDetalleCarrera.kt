package com.julio.carrerasatletismo.ui.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.modelo.Carrera
import com.julio.carrerasatletismo.domain.modelo.Corredor
import com.julio.carrerasatletismo.domain.modelo.InscritoEnCarrera
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme
import com.julio.carrerasatletismo.ui.CarrerasViewModel

@Composable
fun PantallaDetalleCarrera(
    vm: CarrerasViewModel,
    idCarrera: String,
    onRegresar: () -> Unit
) {
    val carreraFlow = remember(idCarrera) { vm.carrera(idCarrera) }
    val inscritosFlow = remember(idCarrera) { vm.inscritos(idCarrera) }
    val carrera by carreraFlow.collectAsState(initial = null)
    val inscritos by inscritosFlow.collectAsState(initial = emptyList())
    val corredores by vm.corredores.collectAsState()

    var mostrarDialogo by remember { mutableStateOf(false) }
    var resultadoDe by remember { mutableStateOf<InscritoEnCarrera?>(null) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(idCarrera) { vm.cargarDetalle(idCarrera) }
    LaunchedEffect(vm.mensaje) {
        vm.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    ContenidoDetalleCarrera(
        carrera = carrera,
        inscritos = inscritos,
        snackbar = snackbar,
        onRegresar = onRegresar,
        onInscribir = { mostrarDialogo = true },
        onQuitar = { idCorredor -> vm.quitarInscripcion(idCorredor, idCarrera) },
        onResultado = { resultadoDe = it }
    )

    resultadoDe?.let { ins ->
        DialogoResultado(
            inscrito = ins,
            onCancelar = { resultadoDe = null },
            onGuardar = { tiempo ->
                vm.guardarResultado(ins.idCorredor, idCarrera, tiempo)
                resultadoDe = null
            },
            onBorrar = {
                vm.borrarResultado(ins.idCorredor, idCarrera)
                resultadoDe = null
            }
        )
    }

    if (mostrarDialogo) {
        DialogoInscripcion(
            corredores = corredores,
            onCancelar = { mostrarDialogo = false },
            onGuardar = { idCorredor, dorsal, tiempo ->
                vm.inscribir(idCorredor, idCarrera, dorsal, tiempo)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun ContenidoDetalleCarrera(
    carrera: Carrera?,
    inscritos: List<InscritoEnCarrera>,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    onRegresar: () -> Unit = {},
    onInscribir: () -> Unit = {},
    onQuitar: (Long) -> Unit = {},
    onResultado: (InscritoEnCarrera) -> Unit = {}
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = carrera?.nombre ?: stringResource(R.string.detalle),
                onRegresar = onRegresar
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onInscribir,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.inscribir)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val uriHandler = LocalUriHandler.current
        val c = carrera
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (c != null) {
                item { TarjetaInfo(c) }
                item { TarjetaClimaRecorrido(c) }
                if (c.url.isNotBlank()) {
                    item {
                        TextButton(onClick = { uriHandler.openUri(c.url) }) {
                            Text(stringResource(R.string.mas_info))
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.inscritos_conteo, inscritos.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (inscritos.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.sin_inscritos),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(inscritos, key = { it.idCorredor }) { ins ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.dorsal_formato, ins.dorsal),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ins.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.categoria_meta, ins.categoria, ins.tiempoObjetivo),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (ins.tiempoReal != null) {
                                Text(
                                    stringResource(R.string.resultado_formato, ins.posicion ?: 0, ins.tiempoReal),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = { onResultado(ins) }) {
                            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.registrar_resultado))
                        }
                        IconButton(onClick = { onQuitar(ins.idCorredor) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.quitar_inscripcion))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaInfo(c: Carrera) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(c.nombre, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.fecha_formato, formatearFecha(c.fecha)))
            Text(stringResource(R.string.lugar_formato, listOf(c.ciudad, c.pais).filter { it.isNotBlank() }.joinToString(", ")))
        }
    }
}

@Composable
private fun TarjetaClimaRecorrido(c: Carrera) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.clima), style = MaterialTheme.typography.titleMedium)
            val sinDato = stringResource(R.string.sin_dato)
            val temperatura = c.temperaturaC?.let { stringResource(R.string.temperatura_valor, it) } ?: sinDato
            val humedad = c.humedad?.let { stringResource(R.string.humedad_valor, it) } ?: sinDato
            Text(stringResource(R.string.clima_formato, temperatura, humedad))
            Text(stringResource(R.string.condiciones_formato, c.condiciones ?: sinDato))
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.recorrido), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.tipo_formato, c.tipoRecorrido ?: sinDato))
            Text(stringResource(R.string.desnivel_formato, c.desnivelM?.let { stringResource(R.string.desnivel_valor, it) } ?: sinDato))
            Text(stringResource(R.string.terreno_formato, c.terreno ?: sinDato))
        }
    }
}

@Composable
private fun DialogoInscripcion(
    corredores: List<Corredor>,
    onCancelar: () -> Unit,
    onGuardar: (idCorredor: Long, dorsal: Int, tiempo: String) -> Unit
) {
    var seleccionado by remember { mutableStateOf(corredores.firstOrNull()?.idCorredor) }
    var dorsal by remember { mutableStateOf("") }
    var tiempo by remember { mutableStateOf("04:00:00") }
    val valido = seleccionado != null && dorsal.toIntOrNull() != null && tiempo.isNotBlank()

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.inscribir)) },
        text = {
            if (corredores.isEmpty()) {
                Text(stringResource(R.string.sin_corredores_inscribir))
            } else {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    corredores.forEach { corredor ->
                        Row(
                            Modifier.fillMaxWidth().clickable { seleccionado = corredor.idCorredor },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = seleccionado == corredor.idCorredor,
                                onClick = { seleccionado = corredor.idCorredor }
                            )
                            Text(stringResource(R.string.corredor_categoria, corredor.nombre, corredor.categoria))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dorsal,
                        onValueChange = { dorsal = it.filter(Char::isDigit).take(5) },
                        label = { Text(stringResource(R.string.dorsal)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tiempo,
                        onValueChange = { tiempo = it.take(8) },
                        label = { Text(stringResource(R.string.tiempo_objetivo)) },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido,
                onClick = { onGuardar(seleccionado!!, dorsal.toInt(), tiempo) }
            ) { Text(stringResource(R.string.guardar)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) }
        }
    )
}

@Composable
private fun DialogoResultado(
    inscrito: InscritoEnCarrera,
    onCancelar: () -> Unit,
    onGuardar: (String) -> Unit,
    onBorrar: () -> Unit
) {
    var tiempo by remember { mutableStateOf(inscrito.tiempoReal ?: "") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.registrar_resultado)) },
        text = {
            Column {
                Text(inscrito.nombre, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = tiempo,
                    onValueChange = { tiempo = it.filter { c -> c.isDigit() || c == ':' }.take(8) },
                    label = { Text(stringResource(R.string.tiempo_real)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(enabled = tiempo.isNotBlank(), onClick = { onGuardar(tiempo) }) {
                Text(stringResource(R.string.guardar))
            }
        },
        dismissButton = {
            Row {
                if (inscrito.tiempoReal != null) {
                    TextButton(onClick = onBorrar) { Text(stringResource(R.string.borrar_resultado)) }
                }
                TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PantallaDetalleCarreraPreview() {
    CarrerasAtletismoTheme {
        ContenidoDetalleCarrera(
            carrera = Carrera(
                idCarrera = "cdmx2027",
                nombre = "Maratón de la Ciudad de México",
                fecha = "2027-08-29",
                ciudad = "Ciudad de México",
                pais = "Mexico",
                url = "",
                tipoRecorrido = "loop",
                desnivelM = 120,
                terreno = "road",
                temperaturaC = 14.5,
                humedad = 70,
                condiciones = "cool"
            ),
            inscritos = listOf(
                InscritoEnCarrera(1, "Ana López", "Libre", 101, "03:45:00", "03:41:22", 1),
                InscritoEnCarrera(2, "Luis Pérez", "Máster", 102, "04:10:00")
            )
        )
    }
}
