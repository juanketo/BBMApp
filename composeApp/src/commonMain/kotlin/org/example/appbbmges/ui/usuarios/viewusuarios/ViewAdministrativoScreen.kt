package org.example.appbbmges.ui.usuarios.viewusuarios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.appbbmges.data.Repository
import org.example.appbbmges.navigation.SimpleNavController
import org.example.appbbmges.AdministrativeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAdministrativoScreen(
    administrativeId: Long,
    repository: Repository,
    navController: SimpleNavController,
    onDismiss: () -> Unit = { navController.navigateBack() }
) {
    val administrative by produceState<AdministrativeEntity?>(null) {
        value = repository.getAdministrativeById(administrativeId)
    }

    var selectedSection by remember { mutableStateOf("personal") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil Administrativo") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color(0xFF374151)
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { /* Exportar */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6366F1)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF6366F1)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Export")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { /* Guardar */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1F2937)
                )
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        administrative?.let { admin ->
            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Panel izquierdo - Perfil y navegación
                Column(
                    modifier = Modifier.width(280.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta de perfil
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6366F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${admin.first_name} ${admin.last_name_paternal}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = admin.position,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Última actualización: ${admin.start_date ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Menú de navegación
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            listOf(
                                "personal" to "Información Personal",
                                "laboral" to "Datos Laborales",
                                "permisos" to "Permisos y Accesos"
                            ).forEach { (id, title) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selectedSection == id) Color(0xFF6366F1).copy(alpha = 0.1f)
                                            else Color.Transparent
                                        )
                                        .padding(vertical = 12.dp, horizontal = 12.dp)
                                        .clickable { selectedSection = id },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            title.contains("Personal") -> Icons.Default.Person
                                            title.contains("Laboral") -> Icons.Default.Work
                                            else -> Icons.Default.Security
                                        },
                                        contentDescription = null,
                                        tint = if (selectedSection == id) Color(0xFF6366F1) else Color(0xFF9CA3AF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedSection == id) Color(0xFF6366F1) else Color(0xFF374151),
                                        fontWeight = if (selectedSection == id) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Panel derecho - Contenido
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    when (selectedSection) {
                        "personal" -> {
                            // Información Personal
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "INFORMACIÓN PERSONAL",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF374151)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Nombre",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = admin.first_name,
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Apellidos",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = "${admin.last_name_paternal ?: ""} ${admin.last_name_maternal ?: ""}".trim(),
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Teléfono",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = admin.phone ?: "N/A",
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Email",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = admin.email ?: "N/A",
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Dirección",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF374151)
                                        )
                                        OutlinedTextField(
                                            value = "${admin.address_street ?: ""}, ${admin.address_zip ?: ""}",
                                            onValueChange = {},
                                            readOnly = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = Color(0xFF374151),
                                                disabledBorderColor = Color(0xFFD1D5DB)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }

                            // Contacto de Emergencia
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "CONTACTO DE EMERGENCIA",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF374151)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Nombre",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = admin.emergency_contact_name ?: "N/A",
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Teléfono",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = admin.emergency_contact_phone ?: "N/A",
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "laboral" -> {
                            // Datos Laborales
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "INFORMACIÓN LABORAL",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF374151)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Puesto",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = admin.position,
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Salario",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = "$${admin.salary}",
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Fecha de Inicio",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = admin.start_date,
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Estado",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            OutlinedTextField(
                                                value = if (admin.active == 1L) "Activo" else "Inactivo",
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = Color(0xFF374151),
                                                    disabledBorderColor = Color(0xFFD1D5DB)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Franquicia Asignada",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF374151)
                                        )
                                        OutlinedTextField(
                                            value = repository.getFranchiseById(admin.franchise_id)?.name ?: "N/A",
                                            onValueChange = {},
                                            readOnly = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = Color(0xFF374151),
                                                disabledBorderColor = Color(0xFFD1D5DB)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        "permisos" -> {
                            // Permisos y Accesos
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "PERMISOS Y ACCESOS",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF374151)
                                    )
                                    // ... (implementar lista de permisos)
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6366F1))
            }
        }
    }
}