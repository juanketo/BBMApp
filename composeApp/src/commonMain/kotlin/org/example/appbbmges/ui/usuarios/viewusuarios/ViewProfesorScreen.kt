package org.example.appbbmges.ui.usuarios.viewusuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.appbbmges.data.Repository
import org.example.appbbmges.navigation.SimpleNavController
import org.example.appbbmges.TeacherEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProfesorScreen(
    teacherId: Long,
    repository: Repository,
    navController: SimpleNavController
) {
    val teacher by produceState<TeacherEntity?>(null) {
        value = repository.getTeacherById(teacherId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Profesor") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF333333)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            // Header
            teacher?.let { teacher ->
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${teacher.first_name} ${teacher.last_name_paternal}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = teacher.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Última actualización: ${teacher.start_date ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Sección de Información Personal
                SectionTitle("INFORMACIÓN PERSONAL")
                InfoTable(
                    items = listOf(
                        Pair("Nombre", "${teacher.first_name} ${teacher.last_name_paternal} ${teacher.last_name_maternal ?: ""}"),
                        Pair("Estado", if (teacher.active == 1L) "Activo" else "Inactivo"),
                        Pair("Fecha Nacimiento", teacher.birth_date ?: "N/A"),
                        Pair("Género", teacher.gender ?: "N/A"),
                        Pair("RFC", teacher.tax_id ?: "N/A")
                    )
                )

                // Sección de Contacto
                SectionTitle("INFORMACIÓN DE CONTACTO")
                InfoTable(
                    items = listOf(
                        Pair("Email", teacher.email ?: "N/A"),
                        Pair("Teléfono", teacher.phone ?: "N/A"),
                        Pair("Contacto Emergencia", "${teacher.emergency_contact_name ?: "N/A"} - ${teacher.emergency_contact_phone ?: ""}")
                    )
                )

                // Sección de Dirección
                SectionTitle("DIRECCIÓN")
                InfoTable(
                    items = listOf(
                        Pair("Calle", teacher.address_street ?: "N/A"),
                        Pair("Código Postal", teacher.address_zip ?: "N/A")
                    )
                )

                // Sección Laboral
                SectionTitle("INFORMACIÓN LABORAL")
                InfoTable(
                    items = listOf(
                        Pair("Salario/hora", teacher.salary_per_hour?.toString() ?: "N/A"),
                        Pair("Fecha Inicio", teacher.start_date ?: "N/A"),
                        Pair("Vetado", if (teacher.vetoed == 1L) "Sí" else "No")
                    )
                )
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cargando profesor...")
                }
            }
        }
    }
}

// Componentes reutilizables
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF555555)
    )
}

@Composable
private fun InfoTable(items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (label != items.last().first) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}