package org.example.appbbmges.ui.usuarios.viewusuarios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Add
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
import org.example.appbbmges.StudentEntity
import org.example.appbbmges.StudentAuthorizedAdultEntity
import org.example.appbbmges.PaymentEntity
import kotlinx.datetime.*
import org.example.appbbmges.navigation.SimpleNavController

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

    var selectedSection by remember { mutableStateOf<String?>("main") }
    var showPaymentForm by remember { mutableStateOf(false) }

    // Estados de datos
    val student by produceState<StudentEntity?>(null) {
        value = repository.getStudentById(studentId)
    }
    val adults by produceState<List<StudentAuthorizedAdultEntity>>(emptyList()) {
        value = repository.getStudentAuthorizedAdultsByStudentId(studentId)
    }
    val payments by produceState<List<PaymentEntity>>(emptyList(), studentId) {
        value = repository.getPaymentsByStudentId(studentId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            showPaymentForm -> {
                NewPaymentScreen(
                    studentId = studentId,
                    onDismiss = {
                        showPaymentForm = false
                    },
                    repository = repository
                )
            }

            selectedSection == "main" -> {
                ViewAlumnoMainScreen(
                    student = student,
                    adults = adults,
                    payments = payments,
                    onDismiss = onDismiss,
                    onSectionClick = { section -> selectedSection = section },
                    onNewPaymentClick = { showPaymentForm = true }
                )
            }

            selectedSection != null -> {
                when (selectedSection) {
                    "personal" -> {
                        StudentPersonalDetailScreen(
                            student = student,
                            adults = adults,
                            onDismiss = { selectedSection = "main" }
                        )
                    }
                    "financiera" -> {
                        StudentFinancialDetailScreen(
                            payments = payments,
                            onDismiss = { selectedSection = "main" },
                            onNewPaymentClick = { showPaymentForm = true }
                        )
                    }
                    "calendario" -> {
                        StudentCalendarDetailScreen(
                            studentId = studentId,
                            onDismiss = { selectedSection = "main" },
                            repository = repository
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAlumnoMainScreen(
    student: StudentEntity?,
    adults: List<StudentAuthorizedAdultEntity>,
    payments: List<PaymentEntity>,
    onDismiss: () -> Unit,
    onSectionClick: (String) -> Unit,
    onNewPaymentClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
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

                    StudentProfileCard(student = student)

                    StudentNavigationCard(onSectionClick = onSectionClick)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    StudentPersonalPreview(
                        student = student,
                        adults = adults,
                        onViewMore = { onSectionClick("personal") }
                    )

                    StudentFinancialPreview(
                        payments = payments,
                        onViewMore = { onSectionClick("financiera") },
                        onNewPaymentClick = onNewPaymentClick
                    )

                    StudentCalendarPreview(
                        onViewMore = { onSectionClick("calendario") }
                    )
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
fun StudentPersonalDetailScreen(
    student: StudentEntity?,
    adults: List<StudentAuthorizedAdultEntity>,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        DetailScreenHeader(
            title = "Información Personal",
            onDismiss = onDismiss
        )

        student?.let { student ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StudentPersonalSection(student = student, adults = adults)
            }
        }
    }
}

@Composable
fun StudentFinancialDetailScreen(
    payments: List<PaymentEntity>,
    onDismiss: () -> Unit,
    onNewPaymentClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        DetailScreenHeader(
            title = "Información Financiera",
            onDismiss = onDismiss,
            action = {
                Button(
                    onClick = onNewPaymentClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Pago")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StudentFinancialSection(
                payments = payments,
                onNewPaymentClick = onNewPaymentClick
            )
        }
    }
}

@Composable
fun StudentCalendarDetailScreen(
    studentId: Long,
    onDismiss: () -> Unit,
    repository: Repository
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DetailScreenHeader(
            title = "Calendario de Clases",
            onDismiss = onDismiss
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Calendario de clases",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Funcionalidad pendiente de implementar")
                }
            }
        }
    }
}

@Composable
fun NewPaymentScreen(
    studentId: Long,
    onDismiss: () -> Unit,
    repository: Repository
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DetailScreenHeader(
            title = "Nuevo Pago",
            onDismiss = onDismiss
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Formulario de Nuevo Pago",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Student ID: $studentId")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Aquí iría el formulario de pago")
                }
            }
        }
    }
}

@Composable
private fun DetailScreenHeader(
    title: String,
    onDismiss: () -> Unit,
    action: @Composable (() -> Unit)? = null
) {
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
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            action?.invoke()
        }
    }
}

@Composable
private fun StudentProfileCard(student: StudentEntity) {
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
                    text = "Última actualización: ${getCurrentDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StudentNavigationCard(onSectionClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            NavigationItem(
                text = "Información Personal",
                onClick = { onSectionClick("personal") },
                icon = Icons.Default.Person
            )
            NavigationItem(
                text = "Información Financiera",
                onClick = { onSectionClick("financiera") },
                icon = Icons.Default.AttachMoney
            )
            NavigationItem(
                text = "Calendario de clases",
                onClick = { onSectionClick("calendario") },
                icon = Icons.Default.CalendarToday
            )
        }
    }
}

@Composable
private fun NavigationItem(
    text: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF6366F1),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StudentPersonalPreview(
    student: StudentEntity,
    adults: List<StudentAuthorizedAdultEntity>,
    onViewMore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "INFORMACIÓN PERSONAL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewMore) {
                    Text("Ver más")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preview de algunos campos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PreviewField("Estado", if (student.active == 1L) "Activo" else "Inactivo", Modifier.weight(1f))
                PreviewField("Teléfono", student.phone ?: "N/A", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            PreviewField("Email", student.email ?: "N/A")

            if (adults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PreviewField("Adultos autorizados", "${adults.size} registrados")
            }
        }
    }
}

@Composable
private fun StudentFinancialPreview(
    payments: List<PaymentEntity>,
    onViewMore: () -> Unit,
    onNewPaymentClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "INFORMACIÓN FINANCIERA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    TextButton(onClick = onNewPaymentClick) {
                        Text("Nuevo Pago")
                    }
                    TextButton(onClick = onViewMore) {
                        Text("Ver más")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (payments.isEmpty()) {
                Text("No hay pagos registrados", color = Color(0xFF6B7280))
            } else {
                val totalAmount = payments.sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PreviewField("Total pagos", "${payments.size}", Modifier.weight(1f))
                    PreviewField("Monto total", "$${totalAmount.toInt()}", Modifier.weight(1f))
                }

                if (payments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val lastPayment = payments.maxByOrNull { it.payment_date }
                    lastPayment?.let {
                        PreviewField("Último pago", "$${it.amount.toInt()} - ${formatTimestamp(it.payment_date)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCalendarPreview(onViewMore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CALENDARIO DE CLASES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewMore) {
                    Text("Ver más")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Próximas clases y horarios", color = Color(0xFF6B7280))
            Text("Funcionalidad pendiente", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PreviewField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF374151)
        )
    }
}

// Reutilizando las funciones y componentes del código original
@Composable
private fun StudentPersonalSection(
    student: StudentEntity,
    adults: List<StudentAuthorizedAdultEntity>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                        value = if (student.active == 1L) "Activo" else "Inactivo",
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

@Composable
private fun StudentFinancialSection(
    payments: List<PaymentEntity>,
    onNewPaymentClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HISTORIAL DE PAGOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    Button(onClick = onNewPaymentClick) {
                        Text("Nuevo Pago")
                    }
                }

                if (payments.isEmpty()) {
                    Text(
                        "No hay pagos registrados para este alumno.",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF6B7280)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                    ) {
                        items(payments) { payment ->
                            PaymentItem(payment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentItem(payment: PaymentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val formattedDate = formatTimestamp(payment.payment_date)
            Text("Fecha: $formattedDate", style = MaterialTheme.typography.bodyMedium)
            Text("Monto: \${payment.amount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppColors.Primary)
            Text("Descripción: ${payment.description}", style = MaterialTheme.typography.bodySmall)
            if (payment.membership_info != null) {
                Text("Membresía: ${payment.membership_info}", style = MaterialTheme.typography.bodySmall)
            }
        }
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

private fun getCurrentDate(): String {
    val now = Clock.System.now()
    val date = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.toString().padStart(2, '0')}-${date.monthNumber.toString().padStart(2, '0')}-${date.year}"
}

private fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
}