package org.example.appbbmges.ui.usuarios.viewusuarios.viewpagos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import org.example.appbbmges.data.Repository
import org.example.appbbmges.PaymentEntity
import org.example.appbbmges.navigation.SimpleNavController
import org.example.appbbmges.ui.usuarios.viewusuarios.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPagosListScreen(
    studentId: Long,
    repository: Repository,
    navController: SimpleNavController,
    modifier: Modifier = Modifier
) {
    var showPaymentForm by remember { mutableStateOf(false) }

    when {
        showPaymentForm -> {
            ViewPagosGlobalScreen(
                studentId = studentId,
                repository = repository,
                onDismiss = { showPaymentForm = false },
                modifier = modifier
            )
        }
        else -> {
            ViewPagosListContent(
                studentId = studentId,
                repository = repository,
                navController = navController,
                modifier = modifier,
                onNewPaymentClick = { showPaymentForm = true }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewPagosListContent(
    studentId: Long,
    repository: Repository,
    navController: SimpleNavController,
    modifier: Modifier = Modifier,
    onNewPaymentClick: () -> Unit
) {
    var payments by remember { mutableStateOf<List<PaymentEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<PaymentEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Cargar pagos del estudiante con DEBUG
    LaunchedEffect(studentId) {
        scope.launch {
            try {
                println("DEBUG: Cargando pagos para studentId: $studentId")
                val result = repository.getPaymentsByStudentId(studentId)
                println("DEBUG: Pagos obtenidos: ${result.size}")
                result.forEachIndexed { index, payment ->
                    println("DEBUG: Pago $index - ID: ${payment.id}, Monto: ${payment.amount}, Fecha: ${payment.payment_date}")
                }
                payments = result
                errorMessage = null
            } catch (e: Exception) {
                println("ERROR cargando pagos: ${e.message}")
                errorMessage = "Error al cargar pagos: ${e.message}"
                payments = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun reloadPayments() {
        scope.launch {
            isLoading = true
            try {
                payments = repository.getPaymentsByStudentId(studentId)
                errorMessage = null
            } catch (e: Exception) {
                println("Error recargando pagos: ${e.message}")
                errorMessage = "Error al recargar pagos: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HISTORIAL DE PAGOS",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = AppColors.Primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNewPaymentClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar pago",
                            tint = AppColors.Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = AppColors.Primary,
                    actionIconContentColor = AppColors.Primary
                )
            )
        },
        containerColor = AppColors.Background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Mostrar mensaje de error si existe
                errorMessage?.let { message ->
                    ErrorMessageCard(message = message, onRetry = { reloadPayments() })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Resumen estadístico
                PaymentSummary(payments)

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de pagos o estado vacío
                if (isLoading) {
                    LoadingState()
                } else if (payments.isEmpty()) {
                    EmptyState(onNewPaymentClick = onNewPaymentClick)
                } else {
                    PaymentsList(
                        payments = payments,
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }
            }

            showDeleteDialog?.let { payment ->
                DeleteConfirmationDialog(
                    payment = payment,
                    onDismiss = { showDeleteDialog = null },
                    onConfirm = {
                        scope.launch {
                            try {
                                repository.deletePayment(payment.id)
                                reloadPayments()
                                showDeleteDialog = null
                            } catch (e: Exception) {
                                println("Error al eliminar pago: ${e.message}")
                                errorMessage = "Error al eliminar pago: ${e.message}"
                                showDeleteDialog = null
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorMessageCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color.Red)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Reintentar", color = Color.White)
            }
        }
    }
}

@Composable
private fun PaymentSummary(payments: List<PaymentEntity>) {
    val totalAmount = payments.sumOf { it.amount }
    val lastPayment = payments.maxByOrNull { it.payment_date }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem(
                title = "Total Pagado",
                value = "$${totalAmount.toInt()} MXN",
                icon = Icons.Default.AttachMoney
            )
            SummaryItem(
                title = "Pagos Realizados",
                value = payments.size.toString(),
                icon = Icons.Default.Receipt
            )
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = Color.Gray.copy(alpha = 0.2f),
            thickness = 1.dp
        )

        if (lastPayment != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Último pago:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = formatTimestamp(lastPayment.payment_date),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Primary
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(16.dp),
                tint = AppColors.Primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Primary
        )
    }
}

@Composable
private fun PaymentsList(
    payments: List<PaymentEntity>,
    onDeleteClick: (PaymentEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header de la tabla
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Primary.copy(alpha = 0.1f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Fecha",
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary,
                modifier = Modifier.weight(2f)
            )
            Text(
                text = "Monto",
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                text = "Acciones",
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(payments) { payment ->
                PaymentRow(
                    payment = payment,
                    onDeleteClick = { onDeleteClick(payment) }
                )
                if (payment != payments.last()) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(
    payment: PaymentEntity,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = formatTimestamp(payment.payment_date),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = payment.description ?: "Regular",
                fontSize = 12.sp,
                color = Color.Gray
            )
            payment.membership_info?.let { reference ->
                Text(
                    text = reference,
                    fontSize = 11.sp,
                    color = AppColors.Primary.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }

        // Monto
        Text(
            text = "$${payment.amount.toInt()} MXN",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Primary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        // Acciones
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar pago",
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.Primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Cargando pagos...", color = AppColors.Primary)
        }
    }
}

@Composable
private fun EmptyState(onNewPaymentClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Payment,
            contentDescription = "Sin pagos",
            modifier = Modifier.size(64.dp),
            tint = AppColors.Primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aún no hay pagos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Este estudiante no tiene pagos registrados",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNewPaymentClick,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generar nuevo pago")
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    payment: PaymentEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar Pago",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("¿Estás seguro de que deseas eliminar este pago?")
                Spacer(modifier = Modifier.height(8.dp))

                Divider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fecha: ${formatTimestamp(payment.payment_date)}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Monto: $${payment.amount.toInt()} MXN",
                    fontSize = 14.sp
                )
                payment.description?.let {
                    Text(
                        text = "Concepto: $it",
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
    val date = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
}