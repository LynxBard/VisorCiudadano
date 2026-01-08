package com.example.visorciudadano.ui.view.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.visorciudadano.domain.model.MapaViewModel
import com.example.visorciudadano.domain.model.ReporteViewModel
import com.example.visorciudadano.ui.home.PantallaDirectorio
import com.example.visorciudadano.ui.view.map.PantallaMapa

// Definimos las rutas de la App
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Mapa : Screen("mapa", "Mapa", Icons.Default.Map)
    object NuevoReporte : Screen("reporte", "Reportar", Icons.Default.AddCircle)
    object Directorio : Screen("directorio", "Ayuda", Icons.Default.List)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionPrincipal() {
    val navController = rememberNavController()

    // Lista de pantallas para el menú inferior
    val items = listOf(
        Screen.Mapa,
        Screen.NuevoReporte,
        Screen.Directorio
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Evita acumular pantallas en el "Back Stack" al navegar
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Contenedor que cambia de pantalla
        NavHost(
            navController = navController,
            startDestination = Screen.Mapa.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Pantalla del Mapa
            composable(Screen.Mapa.route) {
                // Inyectamos el ViewModel aquí para que sobreviva a la configuración
                val mapaViewModel: MapaViewModel = viewModel()
                PantallaMapa(viewModel = mapaViewModel)
            }

            // 2. Pantalla de Nuevo Reporte
            composable(Screen.NuevoReporte.route) {
                val reporteViewModel: ReporteViewModel = viewModel()
                PantallaReporte(viewModel = reporteViewModel)
            }

            // 3. Pantalla Directorio (Fase 4 - Placeholder)
            composable(Screen.Directorio.route) {
                PantallaDirectorio() // Usamos el nuevo componente real()
            }
        }
    }
}