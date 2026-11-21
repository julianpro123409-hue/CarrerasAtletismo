package com.julio.carrerasatletismo.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.modelo.Carrera
import com.julio.carrerasatletismo.domain.modelo.Corredor
import com.julio.carrerasatletismo.domain.modelo.CorredorConCarreras
import com.julio.carrerasatletismo.ui.CarrerasViewModel
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme

@Composable
fun PantallaCorredores(
    vm: CarrerasViewModel,
    onRegresar: () -> Unit
) {
    val corredores by vm.corredoresConCarreras.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.mensaje) {
        vm.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    ContenidoCorredores(
        corredores = corredores,
        snackbar = snackbar,
        onRegresar = onRegresar,
        onAgregar = { nombre, edad -> vm.agregarCorredor(nombre, edad) },
        onBorrar = { vm.borrarCorredor(it) }
    )
}

@Composable
fun ContenidoCorredores(
    corredores: List<CorredorConCarreras>,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    onRegresar: () -> Unit = {},
    onAgregar: (String, Int) -> Unit = { _, _ -> },
    onBorrar: (Corredor) -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.corredores), onRegresar) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text(stringResource(R.string.nombre)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = edad,
                            onValueChange = { edad = it.filter(Char::isDigit).take(3) },
                            label = { Text(stringResource(R.string.edad)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onAgregar(nombre.trim(), edad.toInt())
                                nombre = ""
                                edad = ""
                            },
                            enabled = nombre.isNotBlank() && edad.toIntOrNull() != null,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.agregar)) }
                    }
                }
            }

            if (corredores.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.sin_corredores),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(corredores, key = { it.corredor.idCorredor }) { item ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.corredor.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.edad_categoria, item.corredor.edad, item.corredor.categoria),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (item.carreras.isEmpty()) stringResource(R.string.sin_inscripciones)
                                else stringResource(R.string.inscrito_en, item.carreras.joinToString { it.nombre }),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(onClick = { onBorrar(item.corredor) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.borrar_corredor))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaCorredoresPreview() {
    CarrerasAtletismoTheme {
        ContenidoCorredores(
            corredores = listOf(
                CorredorConCarreras(
                    Corredor(1, "Ana López", 28, "Libre"),
                    listOf(Carrera("cdmx2027", "Maratón de la Ciudad de México", "2027-08-29", "Ciudad de México", "Mexico", ""))
                ),
                CorredorConCarreras(Corredor(2, "Luis Pérez", 45, "Máster"), emptyList())
            )
        )
    }
}
