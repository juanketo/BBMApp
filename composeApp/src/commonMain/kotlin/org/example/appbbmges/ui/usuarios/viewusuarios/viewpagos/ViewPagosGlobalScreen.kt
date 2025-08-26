package org.example.appbbmges.ui.usuarios.viewusuarios.viewpagos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.viewusuarios.AppColors
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPagosGlobalScreen(
    studentId: Long,
    repository: Repository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paymentRepository = remember { DatabasePaymentRepository(repository) }
    val paymentCalculator = remember { PaymentCalculator(paymentRepository) }

    // Obtener el precio actual de inscripción
    val currentInscriptionPrice = remember {
        val inscriptions = repository.getAllInscriptions()
        inscriptions.lastOrNull()?.precio ?: 800.00 // Valor por defecto si no hay inscripciones
    }

    var selectedType by remember { mutableStateOf<PaymentSelection?>(null) }
    var numClasses by remember { mutableIntStateOf(1) }
    var selectedMembershipId by remember { mutableLongStateOf(0L) }
    var includeEnrollment by remember { mutableStateOf(false) }
    var paymentResult by remember { mutableStateOf<PaymentResult?>(null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Efectivo") }

    val availableMemberships by produceState<List<MembershipInfo>>(emptyList()) {
        value = try {
            paymentCalculator.getAvailableMemberships()
        } catch (_: Exception) {
            emptyList()
        }
    }

    LaunchedEffect(availableMemberships) {
        if (selectedMembershipId == 0L && availableMemberships.isNotEmpty()) {
            selectedMembershipId = availableMemberships.first().id
        }
    }

    LaunchedEffect(selectedType, numClasses, selectedMembershipId, includeEnrollment, selectedPaymentMethod) {
        paymentResult = null
        showError = false

        val selection = when (selectedType) {
            is PaymentSelection.Disciplines -> PaymentSelection.Disciplines(numClasses, 1)
            is PaymentSelection.Membership -> {
                if (availableMemberships.isNotEmpty() && selectedMembershipId > 0L) {
                    PaymentSelection.Membership(selectedMembershipId)
                } else null
            }
            else -> null
        }

        if (selection != null) {
            try {
                paymentResult = paymentCalculator.calculatePayment(
                    selection = selection,
                    includeEnrollment = includeEnrollment,
                    timing = PaymentTiming.NORMAL,
                    paymentMethod = selectedPaymentMethod
                )
            } catch (e: Exception) {
                showError = true
                errorMessage = "Error al calcular el pago: ${e.message}"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 500.dp, max = 700.dp)
                .heightIn(max = 800.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                BackgroundLogo()

                Column(modifier = Modifier.fillMaxSize()) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Primary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RESUMEN DEL PAGO",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ServiceInfoCard(
                            selectedType = selectedType,
                            numClasses = numClasses,
                            availableMemberships = availableMemberships,
                            selectedMembershipId = selectedMembershipId,
                            paymentResult = paymentResult
                        )

                        ServiceTypeSelector(
                            selectedType = selectedType,
                            availableMemberships = availableMemberships,
                            selectedMembershipId = selectedMembershipId,
                            onTypeSelected = { type ->
                                selectedType = type
                            }
                        )

                        when (selectedType) {
                            is PaymentSelection.Disciplines -> {
                                DisciplinesConfiguration(
                                    numClasses = numClasses,
                                    onNumClassesChanged = { numClasses = it }
                                )
                            }
                            is PaymentSelection.Membership -> {
                                MembershipConfiguration(
                                    availableMemberships = availableMemberships,
                                    selectedMembershipId = selectedMembershipId,
                                    onMembershipSelected = { selectedMembershipId = it }
                                )
                            }
                            null -> {
                                EmptyStateCard()
                            }
                            is PaymentSelection.SiblingsWithMixedDisciplines -> {
                                // Manejar este caso si es necesario
                            }
                        }

                        EnrollmentCheckbox(
                            includeEnrollment = includeEnrollment,
                            onEnrollmentChanged = { includeEnrollment = it },
                            enrollmentFee = currentInscriptionPrice
                        )

                        // Método de pago
                        PaymentMethodSelector(
                            selectedPaymentMethod = selectedPaymentMethod,
                            onMethodSelected = { selectedPaymentMethod = it }
                        )

                        // Desglose de precios
                        paymentResult?.let { result ->
                            PriceBreakdownCard(result)

                            // Información de ahorro
                            if (result.discount > 0) {
                                SavingsInfoCard(result)
                            }
                        }

                        // Mostrar errores
                        if (showError) {
                            ErrorCard(errorMessage)
                        }
                    }

                    // Botones de acción
                    ActionButtons(
                        paymentResult = paymentResult,
                        showError = showError,
                        onConfirmPayment = {
                            paymentResult?.let { result ->
                                if (!showError) {
                                    try {
                                        repository.insertPayment(
                                            studentId = studentId,
                                            amount = result.finalAmount,
                                            description = result.description,
                                            paymentDate = Clock.System.now().toEpochMilliseconds(),
                                            baseAmount = result.baseAmount,
                                            discount = result.discount,
                                            membershipInfo = if (selectedType is PaymentSelection.Membership) {
                                                availableMemberships.find { it.id == selectedMembershipId }?.name
                                            } else {
                                                "${numClasses} disciplina(s)"
                                            },
                                            inscriptionId = if (includeEnrollment) 1L else null
                                        )
                                        onDismiss()
                                    } catch (e: Exception) {
                                        showError = true
                                        errorMessage = "Error al guardar el pago: ${e.message}"
                                    }
                                }
                            }
                        },
                        onCancel = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundLogo() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.logoSystem),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                .alpha(0.08f),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ServiceInfoCard(
    selectedType: PaymentSelection?,
    numClasses: Int,
    availableMemberships: List<MembershipInfo>,
    selectedMembershipId: Long,
    paymentResult: PaymentResult?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(60.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when (selectedType) {
                        is PaymentSelection.Disciplines -> "Baby Ballet - ${if (numClasses == 1) "1 Disciplina" else "$numClasses Disciplinas"}"
                        is PaymentSelection.Membership -> "Baby Ballet - Membresía ${availableMemberships.find { it.id == selectedMembershipId }?.membershipType ?: ""}"
                        else -> "Seleccionar Servicio"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Text(
                    text = when (selectedType) {
                        is PaymentSelection.Disciplines -> "${numClasses * 8} clases mensuales"
                        is PaymentSelection.Membership -> "${availableMemberships.find { it.id == selectedMembershipId }?.monthsPaid ?: 0} meses de clases"
                        else -> "Configurar servicio"
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                paymentResult?.let { result ->
                    Text(
                        text = "$${result.finalAmount.roundToInt()}.00 MXN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AppColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceTypeSelector(
    selectedType: PaymentSelection?,
    availableMemberships: List<MembershipInfo>,
    selectedMembershipId: Long,
    onTypeSelected: (PaymentSelection) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tipo de Servicio",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Black
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentTypeChip(
                text = "Mensualidad",
                isSelected = selectedType is PaymentSelection.Disciplines,
                onClick = { onTypeSelected(PaymentSelection.Disciplines(1, 1)) }
            )
            PaymentTypeChip(
                text = "Membresía",
                isSelected = selectedType is PaymentSelection.Membership,
                onClick = {
                    onTypeSelected(
                        if (availableMemberships.isNotEmpty()) {
                            PaymentSelection.Membership(selectedMembershipId.takeIf { it > 0 } ?: availableMemberships.first().id)
                        } else PaymentSelection.Membership(1L)
                    )
                }
            )
        }
    }
}

@Composable
private fun DisciplinesConfiguration(
    numClasses: Int,
    onNumClassesChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Número de Disciplinas",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            NumberSelector(
                value = numClasses,
                onValueChange = onNumClassesChanged,
                range = 1..4
            )
        }
    }
}

@Composable
private fun MembershipConfiguration(
    availableMemberships: List<MembershipInfo>,
    selectedMembershipId: Long,
    onMembershipSelected: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Selecciona Membresía",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            if (availableMemberships.isEmpty()) {
                Text(
                    text = "No hay membresías disponibles",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            } else {
                availableMemberships.forEach { membership ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMembershipSelected(membership.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMembershipId == membership.id,
                            onClick = { onMembershipSelected(membership.id) },
                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = membership.name,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${membership.monthsPaid} meses (ahorro: ${membership.monthsSaved} meses)",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "$${membership.totalPrice.roundToInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AppColors.Primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selecciona un tipo de servicio",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EnrollmentCheckbox(
    includeEnrollment: Boolean,
    onEnrollmentChanged: (Boolean) -> Unit,
    enrollmentFee: Double
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onEnrollmentChanged(!includeEnrollment) }
    ) {
        Checkbox(
            checked = includeEnrollment,
            onCheckedChange = onEnrollmentChanged,
            colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary)
        )
        Text(
            text = "Incluir Inscripción ($${enrollmentFee.roundToInt()}.00 MXN)",
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun PaymentMethodSelector(
    selectedPaymentMethod: String,
    onMethodSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Método de Pago",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Efectivo", "Transferencia", "Tarjeta").forEach { method ->
                PaymentMethodChip(
                    text = method,
                    isSelected = selectedPaymentMethod == method,
                    onClick = { onMethodSelected(method) }
                )
            }
        }
    }
}

@Composable
private fun PriceBreakdownCard(result: PaymentResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Desglose del Pago",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PriceRow(
                label = "Precio base",
                value = "$${result.baseAmount.roundToInt()}.00 MXN"
            )

            if (result.discount > 0) {
                PriceRow(
                    label = "Descuento ${result.discountType ?: ""}",
                    value = "-$${result.discount.roundToInt()}.00 MXN",
                    isDiscount = true
                )
            }

            if (result.includesEnrollment) {
                PriceRow(
                    label = "Inscripción",
                    value = "$${result.enrollmentFee.roundToInt()}.00 MXN"
                )
            }

            PriceRow(
                label = "IVA incluido (${result.taxPercentage.roundToInt()}%)",
                value = "$${result.taxAmount.roundToInt()}.00 MXN",
                isSubtotal = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = "$${result.finalAmount.roundToInt()}.00 MXN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppColors.Primary
                )
            }
        }
    }
}

@Composable
private fun SavingsInfoCard(result: PaymentResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Ahorro de $${result.discount.roundToInt()}.00 MXN con ${result.discountType}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun ErrorCard(errorMessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color.Red),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = errorMessage,
            color = Color.Red,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ActionButtons(
    paymentResult: PaymentResult?,
    showError: Boolean,
    onConfirmPayment: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topStart = 0.dp, topEnd = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFff8abe)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancelar", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onConfirmPayment,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = paymentResult != null && !showError
            ) {
                Text(
                    "REALIZAR PAGO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    isSubtotal: Boolean = false,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isSubtotal) 13.sp else 14.sp,
            color = if (isSubtotal) Color.Gray else Color.Black,
            fontWeight = if (isSubtotal) FontWeight.Normal else FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = if (isSubtotal) 13.sp else 14.sp,
            color = if (isDiscount) Color(0xFF4CAF50) else if (isSubtotal) Color.Gray else Color.Black,
            fontWeight = if (isSubtotal) FontWeight.Normal else FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentTypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.Primary else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) AppColors.Primary else Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PaymentMethodChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.Primary else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) AppColors.Primary else Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Default.Payment,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color.White else Color.Gray
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}

@Composable
private fun NumberSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            enabled = value > range.first
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Disminuir",
                tint = if (value > range.first) AppColors.Primary else Color.Gray
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.Primary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            enabled = value < range.last
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Aumentar",
                tint = if (value < range.last) AppColors.Primary else Color.Gray
            )
        }
    }
}