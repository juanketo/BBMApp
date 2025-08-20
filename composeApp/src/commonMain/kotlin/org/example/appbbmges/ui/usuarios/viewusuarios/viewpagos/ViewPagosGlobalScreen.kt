package org.example.appbbmges.ui.usuarios.viewusuarios.viewpagos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.appbbmges.data.Repository
import org.example.appbbmges.navigation.SimpleNavController
import org.example.appbbmges.ui.usuarios.viewusuarios.AppColors
import kotlin.math.roundToInt

// Clases de datos necesarias para el cálculo de pagos
data class MembershipInfo(
    val id: Long,
    val name: String,
    val monthsPaid: Long,
    val monthsSaved: Double,
    val totalPrice: Double
)

data class PaymentResult(
    val baseAmount: Double,
    val discount: Double,
    val finalAmount: Double,
    val description: String,
    val breakdown: String
)

sealed class PaymentSelection {
    data class Disciplines(val numClasses: Int, val numStudents: Int) : PaymentSelection()
    data class Membership(val membershipId: Long) : PaymentSelection()
    data class SiblingsWithMixedDisciplines(val students: List<StudentPaymentInfo>) : PaymentSelection()
}

data class StudentPaymentInfo(
    val studentId: Long,
    val numClasses: Int
)

enum class PaymentTiming {
    NORMAL,
    EARLY,
    LATE
}

// Repositorio de pagos
class DatabasePaymentRepository(private val repository: Repository) {
    fun getBasePrice(): Double {
        // Obtener el precio base desde la base de datos
        val preciosBase = repository.getAllPreciosBase()
        return preciosBase.firstOrNull()?.precio ?: 400.0 // Precio por defecto
    }

    fun getMembership(id: Long): MembershipInfo? {
        val membership = repository.getMembershipById(id)
        return membership?.let {
            MembershipInfo(
                id = it.id,
                name = it.name,
                monthsPaid = it.months_paid,
                monthsSaved = it.months_saved,
                totalPrice = getBasePrice() * it.months_paid - (getBasePrice() * it.months_saved)
            )
        }
    }

    fun getAllMemberships(): List<MembershipInfo> {
        return repository.getAllMemberships().map { membership ->
            MembershipInfo(
                id = membership.id,
                name = membership.name,
                monthsPaid = membership.months_paid,
                monthsSaved = membership.months_saved,
                totalPrice = getBasePrice() * membership.months_paid - (getBasePrice() * membership.months_saved)
            )
        }
    }
}

// Calculadora de pagos
class PaymentCalculator(private val paymentRepository: DatabasePaymentRepository) {
    private val basePrice = paymentRepository.getBasePrice()
    private val enrollmentFee = 800.0

    fun getAvailableMemberships(): List<MembershipInfo> {
        return paymentRepository.getAllMemberships()
    }

    fun calculatePayment(
        selection: PaymentSelection,
        includeEnrollment: Boolean = false,
        timing: PaymentTiming = PaymentTiming.NORMAL
    ): PaymentResult {
        return when (selection) {
            is PaymentSelection.Disciplines -> calculateDisciplinesPayment(selection, includeEnrollment, timing)
            is PaymentSelection.Membership -> calculateMembershipPayment(selection, includeEnrollment, timing)
            is PaymentSelection.SiblingsWithMixedDisciplines -> calculateSiblingsPayment(selection, includeEnrollment, timing)
        }
    }

    private fun calculateDisciplinesPayment(
        selection: PaymentSelection.Disciplines,
        includeEnrollment: Boolean,
        timing: PaymentTiming
    ): PaymentResult {
        val baseAmount = basePrice * selection.numClasses
        val timingDiscount = when (timing) {
            PaymentTiming.EARLY -> baseAmount * 0.10
            PaymentTiming.LATE -> 0.0
            PaymentTiming.NORMAL -> 0.0
        }

        val enrollmentAmount = if (includeEnrollment) enrollmentFee else 0.0
        val finalAmount = baseAmount - timingDiscount + enrollmentAmount

        val breakdown = buildString {
            appendLine("Precio base por clase: \$${basePrice.toInt()}")
            appendLine("Número de clases: ${selection.numClasses}")
            appendLine("Subtotal: \$${baseAmount.toInt()}")
            if (timingDiscount > 0) {
                appendLine("Descuento por pago anticipado: -\$${timingDiscount.toInt()}")
            }
            if (includeEnrollment) {
                appendLine("Inscripción: +\$${enrollmentFee.toInt()}")
            }
        }

        return PaymentResult(
            baseAmount = baseAmount,
            discount = timingDiscount,
            finalAmount = finalAmount,
            description = "Pago por ${selection.numClasses} clase(s)",
            breakdown = breakdown
        )
    }

    private fun calculateMembershipPayment(
        selection: PaymentSelection.Membership,
        includeEnrollment: Boolean,
        timing: PaymentTiming
    ): PaymentResult {
        val membership = paymentRepository.getMembership(selection.membershipId)
            ?: return PaymentResult(0.0, 0.0, 0.0, "Membresía no encontrada", "")

        val baseAmount = membership.totalPrice
        val timingDiscount = when (timing) {
            PaymentTiming.EARLY -> baseAmount * 0.05
            PaymentTiming.LATE -> 0.0
            PaymentTiming.NORMAL -> 0.0
        }

        val enrollmentAmount = if (includeEnrollment) enrollmentFee else 0.0
        val finalAmount = baseAmount - timingDiscount + enrollmentAmount

        val breakdown = buildString {
            appendLine("Membresía: ${membership.name}")
            appendLine("Meses incluidos: ${membership.monthsPaid}")
            appendLine("Ahorro mensual: \$${(basePrice * membership.monthsSaved).toInt()}")
            appendLine("Precio total: \$${baseAmount.toInt()}")
            if (timingDiscount > 0) {
                appendLine("Descuento por pago anticipado: -\$${timingDiscount.toInt()}")
            }
            if (includeEnrollment) {
                appendLine("Inscripción: +\$${enrollmentFee.toInt()}")
            }
        }

        return PaymentResult(
            baseAmount = baseAmount,
            discount = timingDiscount,
            finalAmount = finalAmount,
            description = "Membresía ${membership.name}",
            breakdown = breakdown
        )
    }

    private fun calculateSiblingsPayment(
        selection: PaymentSelection.SiblingsWithMixedDisciplines,
        includeEnrollment: Boolean,
        timing: PaymentTiming
    ): PaymentResult {
        val totalClasses = selection.students.sumOf { it.numClasses }
        val baseAmount = basePrice * totalClasses
        val siblingDiscount = baseAmount * 0.15 // 15% descuento por hermanos
        val timingDiscount = when (timing) {
            PaymentTiming.EARLY -> baseAmount * 0.10
            PaymentTiming.LATE -> 0.0
            PaymentTiming.NORMAL -> 0.0
        }

        val totalDiscount = siblingDiscount + timingDiscount
        val enrollmentAmount = if (includeEnrollment) enrollmentFee else 0.0
        val finalAmount = baseAmount - totalDiscount + enrollmentAmount

        val breakdown = buildString {
            appendLine("Pago para hermanos:")
            selection.students.forEach { student ->
                appendLine("  Estudiante ${student.studentId}: ${student.numClasses} clase(s)")
            }
            appendLine("Total de clases: $totalClasses")
            appendLine("Subtotal: \${baseAmount.toInt()}")
            appendLine("Descuento por hermanos (15%): -\${siblingDiscount.toInt()}")
            if (timingDiscount > 0) {
                appendLine("Descuento por pago anticipado: -\${timingDiscount.toInt()}")
            }
            if (includeEnrollment) {
                appendLine("Inscripción: +\${enrollmentFee.toInt()}")
            }
        }

        return PaymentResult(
            baseAmount = baseAmount,
            discount = totalDiscount,
            finalAmount = finalAmount,
            description = "Pago para ${selection.students.size} hermanos",
            breakdown = breakdown
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPagosGlobalScreen(
    studentId: Long,
    repository: Repository,
    navController: SimpleNavController
) {
    val paymentRepository = remember { DatabasePaymentRepository(repository) }
    val paymentCalculator = remember { PaymentCalculator(paymentRepository) }

    var selectedType by remember { mutableStateOf<PaymentSelection?>(null) }
    var numClasses by remember { mutableStateOf(1) }
    var selectedMembershipId by remember { mutableStateOf<Long?>(null) }
    var includeEnrollment by remember { mutableStateOf(false) }
    var paymentResult by remember { mutableStateOf<PaymentResult?>(null) }

    val availableMemberships by produceState<List<MembershipInfo>>(emptyList()) {
        value = try {
            paymentCalculator.getAvailableMemberships()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Inicializar selectedMembershipId cuando las membresías se carguen
    LaunchedEffect(availableMemberships) {
        if (selectedMembershipId == null && availableMemberships.isNotEmpty()) {
            selectedMembershipId = availableMemberships.first().id
        }
    }

    LaunchedEffect(selectedType, numClasses, selectedMembershipId, includeEnrollment) {
        paymentResult = null
        val selection = when (selectedType) {
            is PaymentSelection.Disciplines -> PaymentSelection.Disciplines(numClasses, 1)
            is PaymentSelection.Membership -> {
                if (availableMemberships.isNotEmpty() && selectedMembershipId != null) {
                    PaymentSelection.Membership(selectedMembershipId!!)
                } else {
                    null
                }
            }
            else -> null
        }

        if (selection != null) {
            try {
                paymentResult = paymentCalculator.calculatePayment(
                    selection = selection,
                    includeEnrollment = includeEnrollment,
                    timing = PaymentTiming.NORMAL
                )
            } catch (e: Exception) {
                paymentResult = PaymentResult(
                    baseAmount = 0.0,
                    discount = 0.0,
                    finalAmount = 0.0,
                    description = "Error al calcular el pago",
                    breakdown = "Error: ${e.message}"
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
        TopAppBar(
            title = { Text("Nuevo Pago", color = AppColors.TextColor) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = AppColors.Primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Calculadora de Pagos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeEnrollment,
                            onCheckedChange = { includeEnrollment = it }
                        )
                        Text("Incluir Inscripción (\$800)", modifier = Modifier.clickable { includeEnrollment = !includeEnrollment })
                    }

                    Text(text = "Selecciona el tipo de pago:", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceChip(
                            text = "Mensualidad",
                            isSelected = selectedType is PaymentSelection.Disciplines,
                            onClick = { selectedType = PaymentSelection.Disciplines(numClasses, 1) }
                        )
                        ChoiceChip(
                            text = "Membresía",
                            isSelected = selectedType is PaymentSelection.Membership,
                            onClick = {
                                selectedType = if (availableMemberships.isNotEmpty()) {
                                    PaymentSelection.Membership(selectedMembershipId ?: availableMemberships.first().id)
                                } else {
                                    PaymentSelection.Membership(0L)
                                }
                            }
                        )
                    }

                    when (selectedType) {
                        is PaymentSelection.Disciplines -> {
                            Text(text = "Número de Clases", fontWeight = FontWeight.SemiBold)
                            NumberSelector(
                                value = numClasses,
                                onValueChange = { numClasses = it },
                                range = 1..4
                            )
                        }
                        is PaymentSelection.Membership -> {
                            Text(text = "Selecciona una Membresía", fontWeight = FontWeight.SemiBold)
                            if (availableMemberships.isEmpty()) {
                                Text("No hay membresías disponibles", color = Color.Red)
                            } else {
                                availableMemberships.forEach { membership ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = selectedMembershipId == membership.id,
                                            onClick = { selectedMembershipId = membership.id }
                                        )
                                        Text(
                                            text = "${membership.name} - \${membership.totalPrice.toInt()}",
                                            modifier = Modifier.clickable { selectedMembershipId = membership.id }
                                        )
                                    }
                                }
                            }
                        }
                        is PaymentSelection.SiblingsWithMixedDisciplines -> {
                            Text("Funcionalidad de hermanos no implementada aún")
                        }
                        null -> {
                            Text("Selecciona una opción para ver el cálculo.")
                        }
                    }

                    paymentResult?.let { result ->
                        if (result.description.contains("Error")) {
                            Text(
                                text = result.description,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                HorizontalDivider()
                                Text(
                                    text = "Detalles del Pago",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = result.breakdown,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Total Final: \${result.finalAmount.roundToInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = AppColors.Primary
                                )
                            }
                        }
                    } ?: Text("Selecciona una opción para ver el cálculo.")

                    Button(
                        onClick = {
                            paymentResult?.let { result ->
                                try {
                                    repository.insertPayment(
                                        studentId = studentId,
                                        amount = result.finalAmount,
                                        description = result.description,
                                        paymentDate = System.currentTimeMillis(),
                                        baseAmount = result.baseAmount,
                                        discount = result.discount,
                                        membershipInfo = if (selectedType is PaymentSelection.Membership) {
                                            availableMemberships.find { it.id == selectedMembershipId }?.name
                                        } else null
                                    )
                                    navController.navigateBack()
                                } catch (e: Exception) {
                                    // Manejo de errores - podrías mostrar un snackbar o diálogo
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        enabled = paymentResult != null && !paymentResult!!.description.contains("Error")
                    ) {
                        Text("Confirmar Pago", color = Color.White)
                    }
                }
            }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            enabled = value > range.first
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Decrease")
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            enabled = value < range.last
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Increase")
        }
    }
}

@Composable
fun ChoiceChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Add, contentDescription = null, Modifier.size(AssistChipDefaults.IconSize)) }
        } else null,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) AppColors.Primary else Color.Gray
        ),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isSelected) AppColors.Primary.copy(alpha = 0.1f) else Color.White,
            labelColor = if (isSelected) AppColors.Primary else Color.Gray,
            leadingIconContentColor = if (isSelected) AppColors.Primary else Color.Gray
        )
    )
}