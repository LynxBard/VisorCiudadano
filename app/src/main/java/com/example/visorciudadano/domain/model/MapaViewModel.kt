package com.example.visorciudadano.domain.model

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.visorciudadano.data.model.AlcaldiasRepository
import com.example.visorciudadano.data.model.ZonaAlcaldia
import com.example.visorciudadano.data.model.Reporte
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil // Requiere la librería maps-utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MapaUiState(
    val reportes: List<Reporte> = emptyList(),
    val alcaldias: List<ZonaAlcaldia> = emptyList(), // Lista de polígonos coloreados
    val isLoading: Boolean = false,
    val reporteSeleccionado: Reporte? = null
)

class MapaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    init {
        cargarReportes()
    }

    private fun cargarReportes() {
        _uiState.update { it.copy(isLoading = true) }

        db.collection("reportes").addSnapshotListener { value, error ->
            if (error != null || value == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@addSnapshotListener
            }

            val listaReportes = value.toObjects(Reporte::class.java)

            // Calculamos el semáforo cada vez que llegan datos
            val alcaldiasColoreadas = calcularSemaforoPorAlcaldia(listaReportes)

            _uiState.update {
                it.copy(
                    reportes = listaReportes,
                    alcaldias = alcaldiasColoreadas,
                    isLoading = false
                )
            }
        }
    }

    private fun calcularSemaforoPorAlcaldia(reportes: List<Reporte>): List<ZonaAlcaldia> {
        // 1. Obtener las zonas base
        val zonas = AlcaldiasRepository.obtenerAlcaldiasCDMX()

        // 2. Iterar reportes y asignarlos a una zona
        for (reporte in reportes) {
            val puntoReporte = LatLng(reporte.latitud, reporte.longitud)

            for (zona in zonas) {
                // PolyUtil verifica matemáticamente si el punto está dentro del polígono
                val estaDentro = PolyUtil.containsLocation(puntoReporte, zona.bordes, true)
                if (estaDentro) {
                    zona.cantidadReportes++
                    break // Un reporte solo pertenece a una zona
                }
            }
        }

        // 3. Asignar colores según densidad (Lógica del Semáforo)
        for (zona in zonas) {
            zona.colorEstado = when {
                zona.cantidadReportes >= 10 -> Color(0x66FF0000) // Rojo (Transparente)
                zona.cantidadReportes >= 4 -> Color(0x66FFC107) // Amarillo/Naranja
                else -> Color(0x4400FF00)                       // Verde
            }
        }

        return zonas
    }

    fun seleccionarReporte(reporte: Reporte?) {
        _uiState.update { it.copy(reporteSeleccionado = reporte) }
    }
}