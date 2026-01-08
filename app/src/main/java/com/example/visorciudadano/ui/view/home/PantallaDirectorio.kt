package com.example.visorciudadano.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visorciudadano.data.model.Institucion
import com.example.visorciudadano.domain.model.DirectorioViewModel

@Composable
fun PantallaDirectorio(viewModel: DirectorioViewModel = viewModel()) {
    val instituciones by viewModel.instituciones.collectAsState()
    val filtroActual by viewModel.filtroActual.collectAsState()

    val categorias = listOf("Todas", "Seguridad", "Salud", "Emergencia", "Género", "Legal", "General")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("Directorio de Ayuda", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Filtros
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categorias) { cat ->
                FilterChip(
                    selected = filtroActual == cat,
                    onClick = { viewModel.filtrarPorCategoria(cat) },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lista
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el BottomBar
        ) {
            items(instituciones) { inst ->
                ItemInstitucion(inst)
            }
        }
    }
}

@Composable
fun ItemInstitucion(institucion: Institucion) {
    val context = LocalContext.current

    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(institucion.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(institucion.categoria, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(institucion.direccion, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Botones de Acción
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                BotonAccion(Icons.Default.Call, "Llamar") {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${institucion.telefono}")
                    }
                    context.startActivity(intent)
                }

                BotonAccion(Icons.Default.Map, "Mapa") {
                    // Abre Google Maps en la ubicación
                    val gmmIntentUri = Uri.parse("geo:${institucion.latitud},${institucion.longitud}?q=${Uri.encode(institucion.nombre)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    // Verificamos si hay app de mapas (opcional, pero recomendado)
                    context.startActivity(mapIntent)
                }

                BotonAccion(Icons.Default.Language, "Web") {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(institucion.sitioWeb))
                    context.startActivity(browserIntent)
                }
            }
        }
    }
}

@Composable
fun BotonAccion(icon: ImageVector, text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null)
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}