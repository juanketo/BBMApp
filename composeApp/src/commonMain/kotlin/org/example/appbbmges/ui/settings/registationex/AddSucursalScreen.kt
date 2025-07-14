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

data class SucursalData(
    val name: String,
    val email: String?,
    val phone: String?,
    val base_price: String?,
    val currency: String?,
    val address_street: String?,
    val address_number: String?,
    val address_neighborhood: String?,
    val address_zip: String?,
    val address_city: String?,
    val address_country: String?,
    val tax_name: String?,
    val tax_id: String?,
    val zone: String?,
    val is_new: Boolean,
    val active: Boolean
)

enum class SucursalFormStep {
    PERSONAL_INFO,
    DETAIL_INFO,
    ADDRESS_INFO,
    ADDITIONAL_INFO,
    CONFIRMATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSucursalScreen(onDismiss: () -> Unit, repository: Repository) {
    var currentStep by remember { mutableStateOf(SucursalFormStep.PERSONAL_INFO) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var base_price by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var address_street by remember { mutableStateOf("") }
    var address_number by remember { mutableStateOf("") }
    var address_neighborhood by remember { mutableStateOf("") }
    var address_zip by remember { mutableStateOf("") }
    var address_city by remember { mutableStateOf("") }
    var address_country by remember { mutableStateOf("") }
    var tax_name by remember { mutableStateOf("") }
    var tax_id by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("") }
    var is_new by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(true) }

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
        return true // Validación simple por ahora
    }

    fun validateAddressInfo(): Boolean {
        return true // Validación simple por ahora
    }

    fun validateAdditionalInfo(): Boolean {
        return true // Validación simple por ahora
    }

    fun validateForm(): Boolean {
        return validatePersonalInfo() && validateDetailInfo() && validateAddressInfo() && validateAdditionalInfo()
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
                        "Registro de Sucursal",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.TextColor,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = {
                            when (currentStep) {
                                SucursalFormStep.PERSONAL_INFO -> 0.2f; SucursalFormStep.DETAIL_INFO -> 0.4f; SucursalFormStep.ADDRESS_INFO -> 0.6f; SucursalFormStep.ADDITIONAL_INFO -> 0.8f; SucursalFormStep.CONFIRMATION -> 1.0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        color = AppColors.Primary
                    )
                    Text(
                        when (currentStep) {
                            SucursalFormStep.PERSONAL_INFO -> "Datos Básicos"; SucursalFormStep.DETAIL_INFO -> "Detalles"; SucursalFormStep.ADDRESS_INFO -> "Dirección"; SucursalFormStep.ADDITIONAL_INFO -> "Impuestos"; SucursalFormStep.CONFIRMATION -> "Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    when (currentStep) {
                        SucursalFormStep.PERSONAL_INFO -> {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; nameError = null },
                                label = { Text("Nombre") },
                                isError = nameError != null,
                                supportingText = { nameError?.let { Text(it) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Teléfono") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        SucursalFormStep.DETAIL_INFO -> {
                            OutlinedTextField(
                                value = base_price,
                                onValueChange = { base_price = it },
                                label = { Text("Precio Base") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = currency,
                                onValueChange = { currency = it },
                                label = { Text("Moneda") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        SucursalFormStep.ADDRESS_INFO -> {
                            OutlinedTextField(
                                value = address_street,
                                onValueChange = { address_street = it },
                                label = { Text("Calle") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = address_number,
                                onValueChange = { address_number = it },
                                label = { Text("Número") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = address_neighborhood,
                                onValueChange = { address_neighborhood = it },
                                label = { Text("Colonia") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = address_zip,
                                onValueChange = { address_zip = it },
                                label = { Text("Código Postal") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = address_city,
                                onValueChange = { address_city = it },
                                label = { Text("Ciudad") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = address_country,
                                onValueChange = { address_country = it },
                                label = { Text("País") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        SucursalFormStep.ADDITIONAL_INFO -> {
                            OutlinedTextField(
                                value = tax_name,
                                onValueChange = { tax_name = it },
                                label = { Text("Nombre Impuesto") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = tax_id,
                                onValueChange = { tax_id = it },
                                label = { Text("ID Impuesto") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = zone,
                                onValueChange = { zone = it },
                                label = { Text("Zona") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = is_new,
                                    onCheckedChange = { is_new = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary)
                                )
                                Text("Nueva", modifier = Modifier.padding(start = 4.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = active,
                                    onCheckedChange = { active = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary)
                                )
                                Text("Activa", modifier = Modifier.padding(start = 4.dp))
                            }
                        }

                        SucursalFormStep.CONFIRMATION -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Datos Básicos",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Nombre: $name")
                                    if (email.isNotEmpty()) Text("Email: $email")
                                    if (phone.isNotEmpty()) Text("Teléfono: $phone")
                                    Text(
                                        "Detalles",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (base_price.isNotEmpty()) Text("Precio Base: $base_price")
                                    if (currency.isNotEmpty()) Text("Moneda: $currency")
                                    Text(
                                        "Dirección",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (address_street.isNotEmpty()) Text("Calle: $address_street")
                                    if (address_number.isNotEmpty()) Text("Número: $address_number")
                                    if (address_neighborhood.isNotEmpty()) Text("Colonia: $address_neighborhood")
                                    if (address_zip.isNotEmpty()) Text("Código Postal: $address_zip")
                                    if (address_city.isNotEmpty()) Text("Ciudad: $address_city")
                                    if (address_country.isNotEmpty()) Text("País: $address_country")
                                    Text(
                                        "Impuestos",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (tax_name.isNotEmpty()) Text("Nombre Impuesto: $tax_name")
                                    if (tax_id.isNotEmpty()) Text("ID Impuesto: $tax_id")
                                    if (zone.isNotEmpty()) Text("Zona: $zone")
                                    Text(
                                        "Estado",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Nueva: ${if (is_new) "Sí" else "No"}")
                                    Text("Activa: ${if (active) "Sí" else "No"}")
                                }
                            }
                            if (formError != null) Text(
                                formError!!,
                                color = MaterialTheme.colorScheme.error
                            )
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
                        if (currentStep != SucursalFormStep.PERSONAL_INFO) {
                            OutlinedButton(
                                onClick = {
                                    currentStep = SucursalFormStep.values()[currentStep.ordinal - 1]
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
                                    SucursalFormStep.PERSONAL_INFO -> if (validatePersonalInfo()) currentStep =
                                        SucursalFormStep.DETAIL_INFO

                                    SucursalFormStep.DETAIL_INFO -> if (validateDetailInfo()) currentStep =
                                        SucursalFormStep.ADDRESS_INFO

                                    SucursalFormStep.ADDRESS_INFO -> if (validateAddressInfo()) currentStep =
                                        SucursalFormStep.ADDITIONAL_INFO

                                    SucursalFormStep.ADDITIONAL_INFO -> if (validateAdditionalInfo()) currentStep =
                                        SucursalFormStep.CONFIRMATION

                                    SucursalFormStep.CONFIRMATION -> if (validateForm()) {
                                        repository.insertFranchise(
                                            name,
                                            email,
                                            phone,
                                            base_price.toDoubleOrNull(),
                                            currency,
                                            address_street,
                                            address_number,
                                            address_neighborhood,
                                            address_zip,
                                            address_city,
                                            address_country,
                                            tax_name,
                                            tax_id,
                                            zone,
                                            if (is_new) 1 else 0,
                                            if (active) 1 else 0
                                        )
                                        onDismiss()
                                    } else {
                                        formError =
                                            "Por favor complete todos los campos obligatorios"
                                    }
                                }
                            },
                            modifier = Modifier.width(if (currentStep == SucursalFormStep.CONFIRMATION) 130.dp else 110.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (currentStep == SucursalFormStep.CONFIRMATION) "Registrar" else "Siguiente")
                        }
                    }
                }
            }
        }
    }
}