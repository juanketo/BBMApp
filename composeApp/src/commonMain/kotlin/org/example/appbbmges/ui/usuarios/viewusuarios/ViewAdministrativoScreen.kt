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
import org.example.appbbmges.AdministrativeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAdministrativoScreen(
    administrativeId: Long,
    repository: Repository,
    navController: SimpleNavController
) {
    val administrative by produceState<AdministrativeEntity?>(null) {
        value = repository.getAdministrativeById(administrativeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil Administrativo") },
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
            administrative?.let { admin ->
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${admin.first_name} ${admin.last_name_paternal}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = admin.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Puesto: ${admin.position}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Sección de Información Personal
                SectionTitle("INFORMACIÓN PERSONAL")
                InfoTable(
                    items = listOf(
                        Pair("Nombre completo", "${admin.first_name} ${admin.last_name_paternal ?: ""} ${admin.last_name_maternal ?: ""}"),
                        Pair("Género", admin.gender ?: "N/A"),
                        Pair("Fecha Nacimiento", admin.birth_date ?: "N/A"),
                        Pair("Nacionalidad", admin.nationality ?: "N/A"),
                        Pair("RFC", admin.tax_id ?: "N/A"),
                        Pair("NSS", admin.nss ?: "N/A")
                    )
                )

                // Sección de Contacto
                SectionTitle("INFORMACIÓN DE CONTACTO")
                InfoTable(
                    items = listOf(
                        Pair("Teléfono", admin.phone ?: "N/A"),
                        Pair("Email", admin.email ?: "N/A"),
                        Pair("Contacto Emergencia", "${admin.emergency_contact_name ?: "N/A"} - ${admin.emergency_contact_phone ?: ""}")
                    )
                )

                // Sección de Dirección
                SectionTitle("DIRECCIÓN")
                InfoTable(
                    items = listOf(
                        Pair("Calle", admin.address_street ?: "N/A"),
                        Pair("Código Postal", admin.address_zip ?: "N/A")
                    )
                )

                // Sección Laboral
                SectionTitle("INFORMACIÓN LABORAL")
                InfoTable(
                    items = listOf(
                        Pair("Puesto", admin.position),
                        Pair("Salario", "$${admin.salary}"),
                        Pair("Fecha Inicio", admin.start_date),
                        Pair("Estado", if (admin.active == 1L) "Activo" else "Inactivo")
                    )
                )

                // Sección de Franquicia
                SectionTitle("FRANQUICIA ASIGNADA")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = repository.getFranchiseById(admin.franchise_id)?.name ?: "Franquicia no encontrada",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cargando información administrativa...")
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