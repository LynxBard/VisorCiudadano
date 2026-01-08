package com.example.visorciudadano.data.model

import com.google.firebase.Timestamp
import java.io.Serializable

/**
 * Representa un reporte ciudadano en la base de datos.
 * Implementa Serializable para poder pasarse entre Activities/Fragments si es necesario.
 */
data class Reporte(
    // ID único del documento en Firestore (se asigna al crear)
    var id: String = "",

    // Ubicación geográfica
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,

    // Evidencia visual (URL de descarga de Firebase Storage)
    var urlImagen: String = "",

    // Información del usuario (Opcional, puede ser "Anónimo")
    var alias: String? = null,

    // Categoría del incidente (Debe coincidir con los valores de CategoriaReporte)
    var categoria: String = "",

    // Descripción general del reporte
    var descripcion: String = "",

    // Fecha y hora exacta del registro (Firestore Timestamp)
    var fechaRegistro: Timestamp = Timestamp.now(),

    // Mapa flexible para datos específicos de cada categoría
    // Ej: {"objetos_sustraidos": "Cartera y celular", "violencia": true}
    var detalles: Map<String, Any> = emptyMap()
) : Serializable

/**
 * Enumeración para controlar las categorías obligatorias y evitar errores de texto.
 */
enum class CategoriaReporte(val titulo: String) {
    SERVICIOS_PUBLICOS("Servicios Públicos"), // Baches, luz, agua
    ROBO_ASALTO("Robo o Asalto"),             // Incidentes delictivos
    CORRUPCION("Corrupción u Omisión"),       // Denuncias a funcionarios
    VIOLENCIA_GENERO("Violencia de Género"),  // Alertas de género
    NARCOMENUDEO("Narcomenudeo"),             // Actividad sospechosa
    REPORTE_GENERAL("Reporte General")        // Otros
}