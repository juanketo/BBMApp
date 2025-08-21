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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPagosGlobalScreen(
    studentId: Long,
    repository: Repository,
    navController: SimpleNavController
) {
    // Usar las clases corregidas del primer archivo
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
                                            text = "${membership.name} - \$${membership.totalPrice.toInt()}",
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
                                    text = "Total Final: \$${result.finalAmount.roundToInt()}",
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