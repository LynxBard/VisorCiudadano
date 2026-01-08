package com.example.visorciudadano.domain.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visorciudadano.data.model.CategoriaReporte
import com.example.visorciudadano.data.model.Reporte
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ReporteUiState(
    val reporte: Reporte = Reporte(),
    val isLoading: Boolean = false,
    val mensajeUsuario: String? = null,
    val reporteEnviado: Boolean = false
)

class ReporteViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReporteUiState())
    val uiState: StateFlow<ReporteUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun actualizarCategoria(categoria: CategoriaReporte) {
        _uiState.update {
            it.copy(
                reporte = it.reporte.copy(
                    categoria = categoria.titulo,
                    // IMPORTANTE: Limpiamos los detalles al cambiar de categoría
                    detalles = emptyMap()
                )
            )
        }
    }

    fun actualizarDescripcion(texto: String) {
        _uiState.update { it.copy(reporte = it.reporte.copy(descripcion = texto)) }
    }

    // NUEVA FUNCIÓN: Para guardar datos específicos (ej: "objetos_robados", "tipo_bache")
    fun actualizarDetalle(clave: String, valor: String) {
        val detallesActuales = _uiState.value.reporte.detalles.toMutableMap()
        detallesActuales[clave] = valor

        _uiState.update {
            it.copy(reporte = it.reporte.copy(detalles = detallesActuales))
        }
    }

    fun actualizarUbicacion(lat: Double, lon: Double) {
        _uiState.update { it.copy(reporte = it.reporte.copy(latitud = lat, longitud = lon)) }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeUsuario = null) }
    }

    fun enviarReporte(uriImagen: Uri?) {
        val estadoActual = _uiState.value

        // Validación básica
        if (estadoActual.reporte.descripcion.isBlank() || uriImagen == null) {
            _uiState.update { it.copy(mensajeUsuario = "Falta descripción general o foto") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // 1. Subir Foto
                val filename = UUID.randomUUID().toString()
                val ref = storage.reference.child("evidencia/$filename.jpg")
                val uploadTask = ref.putFile(uriImagen).await()
                val downloadUrl = ref.downloadUrl.await()

                // 2. Preparar datos finales
                val reporteFinal = estadoActual.reporte.copy(
                    id = UUID.randomUUID().toString(),
                    urlImagen = downloadUrl.toString(),
                    fechaRegistro = Timestamp.now()
                )

                // 3. Guardar en Firestore
                db.collection("reportes").document(reporteFinal.id).set(reporteFinal).await()

                _uiState.update {
                    it.copy(isLoading = false, reporteEnviado = true, mensajeUsuario = "Reporte Exitoso")
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, mensajeUsuario = "Error: ${e.message}") }
            }
        }
    }
}