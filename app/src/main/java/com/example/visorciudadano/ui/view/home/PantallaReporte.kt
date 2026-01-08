package com.example.visorciudadano.ui.view.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.visorciudadano.data.model.CategoriaReporte
import com.example.visorciudadano.domain.model.ReporteViewModel
import com.google.android.gms.location.LocationServices
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReporte(viewModel: ReporteViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados locales para manejo de cámara
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var displayedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzadores de permisos (igual que antes)
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) obtenerUbicacion(context, viewModel) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) displayedPhotoUri = tempPhotoUri }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (file, uri) = crearArchivoTemporal(context)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(uiState.mensajeUsuario) {
        uiState.mensajeUsuario?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Nuevo Reporte") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. SELECTOR DE CATEGORÍA
            Text("Categoría:", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                CategoriaReporte.values().forEach { cat ->
                    FilterChip(
                        selected = uiState.reporte.categoria == cat.titulo,
                        onClick = { viewModel.actualizarCategoria(cat) },
                        label = { Text(cat.titulo) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Divider()

            // 2. FORMULARIO DINÁMICO (Aquí está la lógica diferenciada)
            Text("Detalles Específicos:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            FormularioDinamico(
                categoria = uiState.reporte.categoria,
                detalles = uiState.reporte.detalles,
                onDetalleChange = { k, v -> viewModel.actualizarDetalle(k, v) }
            )

            Divider()

            // 3. DESCRIPCIÓN GENERAL (Siempre visible)
            OutlinedTextField(
                value = uiState.reporte.descripcion,
                onValueChange = { viewModel.actualizarDescripcion(it) },
                label = { Text("Descripción General / Narrativa de hechos") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // 4. EVIDENCIA Y GPS
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Foto")
                }

                Button(
                    onClick = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    modifier = Modifier.weight(1f),
                    colors = if (uiState.reporte.latitud != 0.0) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors()
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.reporte.latitud != 0.0) "GPS Listo" else "GPS")
                }
            }

            if (displayedPhotoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(displayedPhotoUri),
                    contentDescription = null,
                    modifier = Modifier.height(200.dp).fillMaxWidth()
                )
            }

            // 5. ENVIAR
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { viewModel.enviarReporte(displayedPhotoUri) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.reporteEnviado
                ) {
                    Text("ENVIAR REPORTE CIUDADANO")
                }
            }
        }
    }
}

@Composable
fun FormularioDinamico(
    categoria: String,
    detalles: Map<String, Any>,
    onDetalleChange: (String, String) -> Unit
) {
    // Convertimos el map de 'Any' a Strings para facilitar la UI
    val detallesStr = detalles.mapValues { it.value.toString() }

    when (categoria) {
        CategoriaReporte.SERVICIOS_PUBLICOS.titulo -> {
            Text("Tipo de Falla:")
            val opciones = listOf("Bache", "Luminaria", "Fuga de Agua", "Basura")
            OpcionesRadio(opciones, detallesStr["tipo_servicio"] ?: "") {
                onDetalleChange("tipo_servicio", it)
            }
        }

        CategoriaReporte.ROBO_ASALTO.titulo -> {
            OutlinedTextField(
                value = detallesStr["objetos_sustraidos"] ?: "",
                onValueChange = { onDetalleChange("objetos_sustraidos", it) },
                label = { Text("Objetos Sustraídos") },
                placeholder = { Text("Ej: Celular, Cartera, Reloj") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        CategoriaReporte.CORRUPCION.titulo -> {
            OutlinedTextField(
                value = detallesStr["dependencia"] ?: "",
                onValueChange = { onDetalleChange("dependencia", it) },
                label = { Text("Dependencia / Oficina") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = detallesStr["servidor_publico"] ?: "",
                onValueChange = { onDetalleChange("servidor_publico", it) },
                label = { Text("Nombre del Servidor Público (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        CategoriaReporte.VIOLENCIA_GENERO.titulo -> {
            Text("Tipo de Violencia:")
            val tipos = listOf("Física", "Verbal/Psicológica", "Económica", "Sexual")
            OpcionesRadio(tipos, detallesStr["tipo_violencia"] ?: "") {
                onDetalleChange("tipo_violencia", it)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = detallesStr["relacion_agresor"] ?: "",
                onValueChange = { onDetalleChange("relacion_agresor", it) },
                label = { Text("Relación con el agresor") },
                placeholder = { Text("Ej: Pareja, Ex-pareja, Desconocido") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        CategoriaReporte.NARCOMENUDEO.titulo -> {
            OutlinedTextField(
                value = detallesStr["actividad_sospechosa"] ?: "",
                onValueChange = { onDetalleChange("actividad_sospechosa", it) },
                label = { Text("Actividad Observada") },
                placeholder = { Text("Ej: Intercambio de paquetes, vigilancia") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = detallesStr["descripcion_vehiculos"] ?: "",
                onValueChange = { onDetalleChange("descripcion_vehiculos", it) },
                label = { Text("Descripción Personas/Vehículos") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        else -> {
            Text("Utilice el campo de descripción general para detallar su reporte.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun OpcionesRadio(opciones: List<String>, seleccionado: String, onSeleccion: (String) -> Unit) {
    Column {
        opciones.forEach { opcion ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (opcion == seleccionado),
                        onClick = { onSeleccion(opcion) }
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (opcion == seleccionado),
                    onClick = { onSeleccion(opcion) }
                )
                Text(
                    text = opcion,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

// Funciones auxiliares (mantener las mismas de antes)
@SuppressLint("MissingPermission")
fun obtenerUbicacion(context: Context, viewModel: ReporteViewModel) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    client.lastLocation.addOnSuccessListener { loc ->
        loc?.let { viewModel.actualizarUbicacion(it.latitude, it.longitude) }
    }
}

fun crearArchivoTemporal(context: Context): Pair<File, Uri> {
    val file = File.createTempFile("img_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    return Pair(file, uri)
}