package org.example.appbbmges.ui.settings.registationex

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.jetbrains.compose.resources.painterResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.Clock

fun convertDateToTimestamp(dateString: String): Long {
    return try {
        val parts = dateString.split("/")
        if (parts.size == 3) {
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            val localDate = LocalDate(year, month, day)
            localDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        } else {
            Clock.System.now().toEpochMilliseconds()
        }
    } catch (e: Exception) {
        Clock.System.now().toEpochMilliseconds()
    }
}

data class PromoData(
    val name: String,
    val start_date: String,
    val end_date: String,
    val discount_type: String,
    val discount_value: String,
    val applicable_to_new: Boolean,
    val applicable_to_active: Boolean
)

enum class PromoFormStep {
    PERSONAL_INFO,
    DETAIL_INFO,
    ADDITIONAL_INFO,
    CONFIRMATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPromoScreen(onDismiss: () -> Unit, repository: Repository) {
    var currentStep by remember { mutableStateOf(PromoFormStep.PERSONAL_INFO) }
    var name by remember { mutableStateOf("") }
    var start_date by remember { mutableStateOf("") }
    var end_date by remember { mutableStateOf("") }
    var discount_type by remember { mutableStateOf("PERCENT") }
    var discount_value by remember { mutableStateOf("") }
    var applicable_to_new by remember { mutableStateOf(false) }
    var applicable_to_active by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    fun validatePersonalInfo(): Boolean {
        nameError = null
        var isValid = true
        if (name.isEmpty()) {
            nameError = "El nombre es obligatorio"
            isValid = false
        }
        return isValid
    }

    fun validateDetailInfo(): Boolean {
        return start_date.isNotEmpty() && end_date.isNotEmpty() &&
                discount_type.isNotEmpty() && discount_value.isNotEmpty()
    }

    fun validateAdditionalInfo(): Boolean {
        return true // Validación simple por ahora
    }

    fun validateForm(): Boolean {
        return validatePersonalInfo() && validateDetailInfo() && validateAdditionalInfo()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logoSystem),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(0.7f),
                        alpha = 0.1f,
                        contentScale = ContentScale.Fit
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Registro de Promoción",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.TextColor,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = {
                            when (currentStep) {
                                PromoFormStep.PERSONAL_INFO -> 0.2f
                                PromoFormStep.DETAIL_INFO -> 0.4f
                                PromoFormStep.ADDITIONAL_INFO -> 0.8f
                                PromoFormStep.CONFIRMATION -> 1.0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        color = AppColors.Primary
                    )
                    Text(
                        when (currentStep) {
                            PromoFormStep.PERSONAL_INFO -> "Datos Básicos"
                            PromoFormStep.DETAIL_INFO -> "Detalles"
                            PromoFormStep.ADDITIONAL_INFO -> "Aplicabilidad"
                            PromoFormStep.CONFIRMATION -> "Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    when (currentStep) {
                        PromoFormStep.PERSONAL_INFO -> {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; nameError = null },
                                label = { Text("Nombre") },
                                isError = nameError != null,
                                supportingText = { nameError?.let { Text(it) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        PromoFormStep.DETAIL_INFO -> {
                            OutlinedTextField(
                                value = start_date,
                                onValueChange = { start_date = it },
                                label = { Text("Fecha Inicio (dd/mm/aaaa)") },
                                placeholder = { Text("01/01/2024") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = end_date,
                                onValueChange = { end_date = it },
                                label = { Text("Fecha Fin (dd/mm/aaaa)") },
                                placeholder = { Text("31/12/2024") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )

                            // Dropdown para tipo de descuento
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (discount_type == "PERCENT") "Porcentaje" else "Monto fijo",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tipo de Descuento") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Porcentaje") },
                                        onClick = {
                                            discount_type = "PERCENT"
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Monto fijo") },
                                        onClick = {
                                            discount_type = "AMOUNT"
                                            expanded = false
                                        }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = discount_value,
                                onValueChange = { discount_value = it },
                                label = { Text(if (discount_type == "PERCENT") "Porcentaje (%)" else "Monto en centavos") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        PromoFormStep.ADDITIONAL_INFO -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Checkbox(
                                    checked = applicable_to_new,
                                    onCheckedChange = { applicable_to_new = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary)
                                )
                                Text(
                                    "Aplicable a nuevos estudiantes",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Checkbox(
                                    checked = applicable_to_active,
                                    onCheckedChange = { applicable_to_active = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary)
                                )
                                Text(
                                    "Aplicable a estudiantes activos",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        PromoFormStep.CONFIRMATION -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Datos Básicos",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text("Nombre: $name", modifier = Modifier.padding(bottom = 8.dp))

                                    Text(
                                        "Detalles",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text("Fecha Inicio: $start_date", modifier = Modifier.padding(bottom = 2.dp))
                                    Text("Fecha Fin: $end_date", modifier = Modifier.padding(bottom = 2.dp))
                                    Text("Tipo Descuento: ${if (discount_type == "PERCENT") "Porcentaje" else "Monto fijo"}", modifier = Modifier.padding(bottom = 2.dp))
                                    Text("Valor Descuento: $discount_value${if (discount_type == "PERCENT") "%" else " centavos"}", modifier = Modifier.padding(bottom = 8.dp))

                                    Text(
                                        "Aplicabilidad",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text("Aplicable a nuevos: ${if (applicable_to_new) "Sí" else "No"}", modifier = Modifier.padding(bottom = 2.dp))
                                    Text("Aplicable a activos: ${if (applicable_to_active) "Sí" else "No"}")
                                }
                            }
                            if (formError != null) {
                                Text(
                                    formError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1E88E5))
                        ) {
                            Text("Cancelar")
                        }

                        if (currentStep != PromoFormStep.PERSONAL_INFO) {
                            OutlinedButton(
                                onClick = {
                                    currentStep = PromoFormStep.entries.toTypedArray()[currentStep.ordinal - 1]
                                },
                                modifier = Modifier.width(110.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary)
                            ) {
                                Text("Anterior")
                            }
                        }

                        Button(
                            onClick = {
                                when (currentStep) {
                                    PromoFormStep.PERSONAL_INFO -> {
                                        if (validatePersonalInfo()) {
                                            currentStep = PromoFormStep.DETAIL_INFO
                                        }
                                    }
                                    PromoFormStep.DETAIL_INFO -> {
                                        if (validateDetailInfo()) {
                                            currentStep = PromoFormStep.ADDITIONAL_INFO
                                        }
                                    }
                                    PromoFormStep.ADDITIONAL_INFO -> {
                                        if (validateAdditionalInfo()) {
                                            currentStep = PromoFormStep.CONFIRMATION
                                        }
                                    }
                                    PromoFormStep.CONFIRMATION -> {
                                        if (validateForm()) {
                                            val startTs = convertDateToTimestamp(start_date)
                                            val endTs = convertDateToTimestamp(end_date)

                                            repository.insertPromotion(
                                                name = name,
                                                startTs = startTs,
                                                endTs = endTs,
                                                discountType = discount_type,
                                                percentDiscount = if (discount_type == "PERCENT") discount_value.toLongOrNull() else null,
                                                amountCents = if (discount_type == "AMOUNT") discount_value.toLongOrNull() else null,
                                                franchiseId = null, // Puedes agregar lógica para obtener la franquicia actual
                                                applicableToNew = if (applicable_to_new) 1L else 0L,
                                                applicableToActive = if (applicable_to_active) 1L else 0L
                                            )
                                            onDismiss()
                                        } else {
                                            formError = "Por favor complete todos los campos obligatorios"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.width(if (currentStep == PromoFormStep.CONFIRMATION) 130.dp else 110.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (currentStep == PromoFormStep.CONFIRMATION) "Registrar" else "Siguiente")
                        }
                    }
                }
            }
        }
    }
}