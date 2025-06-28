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
import org.example.appbbmges.FranchiseeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFranquiciatarioScreen(
    franchiseeId: Long,
    repository: Repository,
    navController: SimpleNavController
) {
    val franchisee by produceState<FranchiseeEntity?>(null) {
        value = repository.getFranchiseeById(franchiseeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Franquiciatario") },
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
            franchisee?.let { franchisee ->
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${franchisee.first_name} ${franchisee.last_name_paternal}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = franchisee.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Franquicia: ${repository.getFranchiseById(franchisee.franchise_id)?.name ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Sección de Información Personal
                SectionTitle("INFORMACIÓN PERSONAL")
                InfoTable(
                    items = listOf(
                        Pair("Nombre completo", "${franchisee.first_name} ${franchisee.last_name_paternal ?: ""} ${franchisee.last_name_maternal ?: ""}"),
                        Pair("Género", franchisee.gender ?: "N/A"),
                        Pair("Fecha Nacimiento", franchisee.birth_date ?: "N/A"),
                        Pair("Nacionalidad", franchisee.nationality ?: "N/A"),
                        Pair("RFC", franchisee.tax_id ?: "N/A")
                    )
                )

                // Sección de Contacto
                SectionTitle("INFORMACIÓN DE CONTACTO")
                InfoTable(
                    items = listOf(
                        Pair("Teléfono", franchisee.phone ?: "N/A"),
                        Pair("Email", franchisee.email ?: "N/A"),
                        Pair("Contacto Emergencia", "${franchisee.emergency_contact_name ?: "N/A"} - ${franchisee.emergency_contact_phone ?: ""}")
                    )
                )

                // Sección de Dirección
                SectionTitle("DIRECCIÓN")
                InfoTable(
                    items = listOf(
                        Pair("Calle", franchisee.address_street ?: "N/A"),
                        Pair("Código Postal", franchisee.address_zip ?: "N/A")
                    )
                )

                // Sección de Franquicia
                SectionTitle("INFORMACIÓN FRANQUICIA")
                InfoTable(
                    items = listOf(
                        Pair("Fecha Inicio", franchisee.start_date ?: "N/A"),
                        Pair("Estado", if (franchisee.active == 1L) "Activo" else "Inactivo")
                    )
                )

            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cargando franquiciatario...")
                }
            }
        }
    }
}

// Componentes reutilizables (los mismos que en las otras vistas)
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