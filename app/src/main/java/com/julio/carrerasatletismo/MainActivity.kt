package com.julio.carrerasatletismo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.julio.carrerasatletismo.ui.CarrerasViewModel
import com.julio.carrerasatletismo.ui.pantallas.PantallaCarreras
import com.julio.carrerasatletismo.ui.pantallas.PantallaCorredores
import com.julio.carrerasatletismo.ui.pantallas.PantallaDetalleCarrera
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarrerasAtletismoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppCarreras()
                }
            }
        }
    }
}

@Composable
fun AppCarreras(
    // El ViewModel se obtiene con su Factory (inyección de dependencias)
    vm: CarrerasViewModel = viewModel(factory = CarrerasViewModel.Factory)
) {
    var ruta by rememberSaveable { mutableStateOf("inicio") }

    BackHandler(enabled = ruta != "inicio") {
        ruta = if (ruta.startsWith("detalle/")) "carreras" else "inicio"
    }

    when {
        ruta == "inicio" -> PantallaInicio(
            onVerCarreras = { ruta = "carreras" },
            onVerCorredores = { ruta = "corredores" }
        )
        ruta == "carreras" -> PantallaCarreras(
            vm = vm,
            onRegresar = { ruta = "inicio" },
            onAbrirCarrera = { id -> ruta = "detalle/$id" }
        )
        ruta.startsWith("detalle/") -> PantallaDetalleCarrera(
            vm = vm,
            idCarrera = ruta.removePrefix("detalle/"),
            onRegresar = { ruta = "carreras" }
        )
        ruta == "corredores" -> PantallaCorredores(
            vm = vm,
            onRegresar = { ruta = "inicio" }
        )
    }
}

@Composable
fun PantallaInicio(
    onVerCarreras: () -> Unit = {},
    onVerCorredores: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.logo_desc),
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.titulo),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.subtitulo),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onVerCarreras,
            modifier = Modifier.fillMaxWidth(0.75f)
        ) {
            Text(stringResource(R.string.ver_carreras))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onVerCorredores,
            modifier = Modifier.fillMaxWidth(0.75f)
        ) {
            Text(stringResource(R.string.ver_corredores))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaInicioPreview() {
    CarrerasAtletismoTheme {
        PantallaInicio()
    }
}
