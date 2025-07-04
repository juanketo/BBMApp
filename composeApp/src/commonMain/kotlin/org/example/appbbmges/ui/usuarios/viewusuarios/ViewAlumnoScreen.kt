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
import androidx.compose.material.icons.filled.Person
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
import org.example.appbbmges.StudentEntity
import org.example.appbbmges.StudentAuthorizedAdultEntity

object AppColors {
    val Primary = Color(0xFF00B4D8)
    val Background = Color(0xFFF8F9FA)
    val OnPrimary = Color.White
    val TextColor = Color(0xFF333333)
    val BackgroundOverlay = Color(0xFF1C1C1C).copy(alpha = 0.7f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAlumnoScreen(
    studentId: Long,
    repository: Repository,
    navController: SimpleNavController,
    onDismiss: () -> Unit = { navController.navigateBack() }
) {
    val student by produceState<StudentEntity?>(null) {
        value = repository.getStudentById(studentId)
    }
    val adults by produceState<List<StudentAuthorizedAdultEntity>>(emptyList()) {
        value = repository.getStudentAuthorizedAdultsByStudentId(studentId)
    }

    var selectedSection by remember { mutableStateOf("personal") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra superior con título y botones
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = AppColors.Primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perfil de Alumno",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    OutlinedButton(
                        onClick = { /* Acción de exportar */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6366F1)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF6366F1))
                    ) {
                        Text("Exportar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { /* Acción de guardar */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        )
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }

        // Contenido principal
        student?.let { student ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Panel izquierdo - Información básica y avatar
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card del perfil
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
                                    text = "${student.first_name} ${student.last_name_paternal}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = student.email ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Última actualización: 15-11-2025",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            NavigationItem(
                                text = "Información Personal",
                                isSelected = selectedSection == "personal",
                                onClick = { selectedSection = "personal" }
                            )
                            NavigationItem(
                                text = "Información Financiera",
                                isSelected = selectedSection == "financiera",
                                onClick = { selectedSection = "financiera" }
                            )
                            NavigationItem(
                                text = "Calendario de clases",
                                isSelected = selectedSection == "calendario",
                                onClick = { selectedSection = "calendario" }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Información Personal
                    FormSection(
                        title = "INFORMACIÓN PERSONAL",
                        content = {
                            FormGrid {
                                FormField(
                                    label = "Nombre",
                                    value = student.first_name ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                                FormField(
                                    label = "Apellidos",
                                    value = "${student.last_name_paternal ?: ""} ${student.last_name_maternal ?: ""}".trim(),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            FormGrid {
                                FormField(
                                    label = "Estado",
                                    value = "Activo",
                                    modifier = Modifier.weight(1f)
                                )
                                FormField(
                                    label = "Fecha de Nacimiento",
                                    value = student.birth_date ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            FormGrid {
                                FormField(
                                    label = "Correo Electrónico",
                                    value = student.email ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                                FormField(
                                    label = "Teléfono",
                                    value = student.phone ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            FormField(
                                label = "CURP",
                                value = student.curp ?: "",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )

                    // Dirección Personal
                    FormSection(
                        title = "DIRECCIÓN PERSONAL",
                        content = {
                            FormGrid {
                                FormField(
                                    label = "Dirección",
                                    value = student.address_street ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                                FormField(
                                    label = "Ciudad",
                                    value = "", // Ajusta según tu modelo
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            FormGrid {
                                FormField(
                                    label = "País",
                                    value = student.nationality ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                                FormField(
                                    label = "Código Postal",
                                    value = student.address_zip ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    )

                    // Información de Responsables
                    FormSection(
                        title = "RESPONSABLES",
                        content = {
                            FormGrid {
                                FormField(
                                    label = "Padre",
                                    value = buildParentName(
                                        student.parent_father_first_name,
                                        student.parent_father_last_name_paternal,
                                        student.parent_father_last_name_maternal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FormField(
                                    label = "Madre",
                                    value = buildParentName(
                                        student.parent_mother_first_name,
                                        student.parent_mother_last_name_paternal,
                                        student.parent_mother_last_name_maternal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    )

                    // Información Médica
                    FormSection(
                        title = "INFORMACIÓN MÉDICA",
                        content = {
                            FormGrid {
                                FormField(
                                    label = "Tipo Sanguíneo",
                                    value = student.blood_type ?: "N/A",
                                    modifier = Modifier.weight(1f)
                                )
                                FormField(
                                    label = "Enfermedad Crónica",
                                    value = student.chronic_disease ?: "Ninguna",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    )

                    // Adultos Autorizados
                    if (adults.isNotEmpty()) {
                        FormSection(
                            title = "ADULTOS AUTORIZADOS (${adults.size})",
                            content = {
                                adults.chunked(2).forEach { pair ->
                                    FormGrid {
                                        pair.forEach { adult ->
                                            FormField(
                                                label = "Adulto Autorizado",
                                                value = "${adult.first_name} ${adult.last_name_paternal ?: ""}",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        )
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

@Composable
private fun NavigationItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF6366F1).copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        val icon = when {
            text.contains("Personal") -> Icons.Default.Person
            text.contains("Financiera") -> Icons.Default.Person
            else -> Icons.Default.Person
        }

        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF6366F1) else Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color(0xFF6366F1) else Color(0xFF374151),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
            content()
        }
    }
}

@Composable
private fun FormGrid(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { /* Solo lectura por ahora */ },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFFD1D5DB),
                disabledBorderColor = Color(0xFFD1D5DB),
                disabledTextColor = Color(0xFF374151)
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
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