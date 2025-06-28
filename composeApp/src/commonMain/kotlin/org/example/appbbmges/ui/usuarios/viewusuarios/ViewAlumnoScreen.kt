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
import org.example.appbbmges.StudentEntity
import org.example.appbbmges.StudentAuthorizedAdultEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAlumnoScreen(
    studentId: Long,
    repository: Repository,
    navController: SimpleNavController
) {
    val student by produceState<StudentEntity?>(null) {
        value = repository.getStudentById(studentId)
    }
    val adults by produceState<List<StudentAuthorizedAdultEntity>>(emptyList()) {
        value = repository.getStudentAuthorizedAdultsByStudentId(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Alumno") },
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
            student?.let { student ->
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${student.first_name} ${student.last_name_paternal}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = student.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Última actualización: ${student.birth_date ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Sección de Información Personal
                SectionTitle("INFORMACIÓN PERSONAL")
                InfoTable(
                    items = listOf(
                        Pair("Nombre completo", "${student.first_name} ${student.last_name_paternal ?: ""} ${student.last_name_maternal ?: ""}"),
                        Pair("CURP", student.curp ?: "N/A"),
                        Pair("Género", student.gender ?: "N/A"),
                        Pair("Fecha Nacimiento", student.birth_date ?: "N/A"),
                        Pair("Nacionalidad", student.nationality ?: "N/A")
                    )
                )

                // Sección de Contacto
                SectionTitle("INFORMACIÓN DE CONTACTO")
                InfoTable(
                    items = listOf(
                        Pair("Teléfono", student.phone ?: "N/A"),
                        Pair("Email", student.email ?: "N/A"),
                        Pair("Dirección", buildAddress(student))
                    )
                )

                // Sección de Responsables
                SectionTitle("RESPONSABLES")
                InfoTable(
                    items = listOf(
                        Pair("Padre", buildParentName(
                            student.parent_father_first_name,
                            student.parent_father_last_name_paternal,
                            student.parent_father_last_name_maternal
                        )),
                        Pair("Madre", buildParentName(
                            student.parent_mother_first_name,
                            student.parent_mother_last_name_paternal,
                            student.parent_mother_last_name_maternal
                        ))
                    )
                )

                // Adultos autorizados (si existen)
                if (adults.isNotEmpty()) {
                    SectionTitle("ADULTOS AUTORIZADOS (${adults.size})")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            adults.forEachIndexed { index, adult ->
                                Text(
                                    text = "• ${adult.first_name} ${adult.last_name_paternal ?: ""}",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (index < adults.size - 1) {
                                    Divider(modifier = Modifier.padding(horizontal = 8.dp))
                                }
                            }
                        }
                    }
                }

                // Sección Médica
                SectionTitle("INFORMACIÓN MÉDICA")
                InfoTable(
                    items = listOf(
                        Pair("Tipo sanguíneo", student.blood_type ?: "N/A"),
                        Pair("Enfermedad crónica", student.chronic_disease ?: "Ninguna")
                    )
                )

            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cargando alumno...")
                }
            }
        }
    }
}

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

private fun buildAddress(student: StudentEntity): String {
    return listOfNotNull(
        student.address_street,
        student.address_zip
    ).joinToString(", ").ifEmpty { "N/A" }
}

private fun buildParentName(
    firstName: String?,
    lastNameP: String?,
    lastNameM: String?
): String {
    return listOfNotNull(firstName, lastNameP, lastNameM)
        .joinToString(" ")
        .ifEmpty { "N/A" }
}