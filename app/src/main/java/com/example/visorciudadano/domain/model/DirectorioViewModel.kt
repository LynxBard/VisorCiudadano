package com.example.visorciudadano.domain.model

import androidx.lifecycle.ViewModel
import com.example.visorciudadano.data.model.Institucion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DirectorioViewModel : ViewModel() {

    private val _instituciones = MutableStateFlow<List<Institucion>>(emptyList())
    val instituciones: StateFlow<List<Institucion>> = _instituciones.asStateFlow()

    // Estado para el filtro
    private val _filtroActual = MutableStateFlow<String>("Todas")
    val filtroActual: StateFlow<String> = _filtroActual.asStateFlow()

    private val datosMaestros = cargarDatosIniciales()

    init {
        _instituciones.value = datosMaestros
    }

    fun filtrarPorCategoria(categoria: String) {
        _filtroActual.value = categoria
        if (categoria == "Todas") {
            _instituciones.value = datosMaestros
        } else {
            _instituciones.value = datosMaestros.filter { it.categoria == categoria }
        }
    }

    private fun cargarDatosIniciales(): List<Institucion> {
        // Lista de ejemplo con datos reales de CDMX (simulados para el ejercicio)
        return listOf(
            Institucion(1, "Cruz Roja Mexicana", "Juan Luis Vives 200, Polanco", "5555575757", "https://www.cruzrojamexicana.org.mx", 19.4362, -99.2085, "Salud"),
            Institucion(2, "Locatel CDMX", "Héroes del 47, San Mateo", "5556581111", "https://locatel.cdmx.gob.mx", 19.3512, -99.1534, "General"),
            Institucion(3, "Fiscalía General de Justicia", "Gral. Gabriel Hernández 56", "5552009000", "https://www.fgjcdmx.gob.mx", 19.4245, -99.1478, "Seguridad"),
            Institucion(4, "Bomberos Estación Central", "Av. Fray Servando 123", "5557683700", "https://www.bomberos.cdmx.gob.mx", 19.4230, -99.1350, "Emergencia"),
            Institucion(5, "Centro de Justicia para Mujeres", "Av. San Pablo 396", "5553455248", "https://semujeres.cdmx.gob.mx", 19.4820, -99.1830, "Género"),
            Institucion(6, "Secretaría de Seguridad Ciudadana", "Liverpool 136, Juárez", "5552425100", "https://www.ssc.cdmx.gob.mx", 19.4258, -99.1624, "Seguridad"),
            Institucion(7, "Protección Civil CDMX", "Abraham González 67", "5556832222", "https://www.proteccioncivil.cdmx.gob.mx", 19.4290, -99.1550, "Emergencia"),
            Institucion(8, "Consejo Ciudadano", "Amberes 54, Juárez", "5555335533", "https://consejociudadanomx.org", 19.4250, -99.1650, "Legal"),
            // ... Puedes agregar más hasta llegar a 30 copiando y pegando
        )
    }
}