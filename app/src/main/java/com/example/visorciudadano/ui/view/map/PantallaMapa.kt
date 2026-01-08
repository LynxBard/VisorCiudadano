package com.example.visorciudadano.ui.view.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.visorciudadano.data.model.Reporte
import com.example.visorciudadano.domain.model.MapaViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(viewModel: MapaViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Centrado en CDMX
    val cdmx = LatLng(19.4326, -99.1332)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cdmx, 11f) // Zoom un poco más lejos para ver alcaldías
    }

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // 1. DIBUJAR LAS ALCALDÍAS (SEMÁFORO)
                uiState.alcaldias.forEach { zona ->
                    Polygon(
                        points = zona.bordes,
                        fillColor = zona.colorEstado,
                        strokeColor = Color.Black,
                        strokeWidth = 2f,
                        tag = zona.nombre,
                        clickable = true,
                        onClick = {
                            // Opcional: Mostrar nombre de la alcaldía al tocarla
                        }
                    )
                }

                // 2. DIBUJAR LOS MARCADORES INDIVIDUALES
                uiState.reportes.forEach { reporte ->
                    Marker(
                        state = MarkerState(position = LatLng(reporte.latitud, reporte.longitud)),
                        title = reporte.categoria,
                        onClick = {
                            viewModel.seleccionarReporte(reporte)
                            true
                        }
                    )
                }
            }

            // Leyenda del Semáforo
            LeyendaSemaforo(modifier = Modifier.align(Alignment.TopCenter))

            // Detalle del Reporte (BottomSheet)
            if (uiState.reporteSeleccionado != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.seleccionarReporte(null) },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    DetalleReporteView(reporte = uiState.reporteSeleccionado!!)
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun LeyendaSemaforo(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemLeyenda(Color(0xFF4CAF50), "Baja")
            ItemLeyenda(Color(0xFFFFC107), "Media")
            ItemLeyenda(Color(0xFFF44336), "Alta")
        }
    }
}

@Composable
fun ItemLeyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(texto, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun DetalleReporteView(reporte: Reporte) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp) // Espacio extra para navegación
    ) {
        Text(
            text = reporte.categoria,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registrado el: ${reporte.fechaRegistro.toDate()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Imagen de evidencia
        if (reporte.urlImagen.isNotEmpty()) {
            AsyncImage(
                model = reporte.urlImagen,
                contentDescription = "Evidencia",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Descripción:", style = MaterialTheme.typography.titleMedium)
        Text(text = reporte.descripcion, style = MaterialTheme.typography.bodyLarge)

        if (!reporte.alias.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reportado por: ${reporte.alias}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}