package com.example.visorciudadano.data.model

data class Institucion(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val telefono: String, // Formato string para facilitar intents
    val sitioWeb: String,
    val latitud: Double,
    val longitud: Double,
    val categoria: String // Ej: "Seguridad", "Salud", "Legal"
)