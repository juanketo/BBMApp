package org.example.appbbmges.ui.settings.registationex

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.dashboard.clickable
import org.example.appbbmges.ui.usuarios.AppColors
import org.jetbrains.compose.resources.painterResource

enum class SucursalFormStep {
    PERSONAL_INFO,
    DETAIL_INFO,
    ADDRESS_INFO,
    ADDITIONAL_INFO,
    CONFIRMATION
}

sealed class SucursalFormState {
    object Idle : SucursalFormState()
    object Loading : SucursalFormState()
    data class Error(val message: String, val retryable: Boolean = true) : SucursalFormState()
    object Success : SucursalFormState()
}

data class SucursalValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val basePriceError: String? = null,
    val currencyError: String? = null,
    val addressError: String? = null,
    val zipError: String? = null,
    val taxIdError: String? = null
)

class SucursalValidator {
    companion object {
        // Función para capitalizar automáticamente
        private fun capitalizeText(text: String): String {
            return text.split(" ").joinToString(" ") { word ->
                if (word.isNotEmpty()) {
                    word.substring(0, 1).uppercase() + word.substring(1).lowercase()
                } else {
                    word
                }
            }.trim()
        }

        fun validatePersonalInfo(name: String, email: String, phone: String): SucursalValidationResult {
            val capitalizedName = capitalizeText(name)
            val nameError = when {
                capitalizedName.isEmpty() -> "El nombre de la sucursal es obligatorio"
                capitalizedName.length < 3 -> "Mínimo 3 caracteres para el nombre"
                capitalizedName.length > 100 -> "Máximo 100 caracteres para el nombre"
                else -> null
            }

            val emailError = if (email.isNotEmpty()) {
                when {
                    !email.matches(Regex("^[a-z0-9+_.-]+@[a-z0-9.-]+\\.[a-z]{2,}\$")) -> "Formato de email inválido (solo minúsculas)"
                    email.length > 100 -> "Máximo 100 caracteres para el email"
                    else -> null
                }
            } else null

            val phoneError = if (phone.isNotEmpty()) {
                when {
                    !phone.matches(Regex("^[0-9]{10}\$")) -> "Debe tener exactamente 10 dígitos"
                    else -> null
                }
            } else null

            return SucursalValidationResult(
                isValid = nameError == null && emailError == null && phoneError == null,
                nameError = nameError,
                emailError = emailError,
                phoneError = phoneError
            )
        }

        fun validateDetailInfo(basePrice: String, currency: String): SucursalValidationResult {
            val basePriceError = if (basePrice.isNotEmpty()) {
                when {
                    basePrice.toDoubleOrNull() == null -> "El precio base debe ser un número válido"
                    basePrice.toDouble() < 0 -> "El precio no puede ser negativo"
                    basePrice.toDouble() > 999999.99 -> "El precio es demasiado alto"
                    else -> null
                }
            } else null

            val currencyError = if (currency.isNotEmpty()) {
                when {
                    currency.length > 10 -> "Máximo 10 caracteres para la moneda"
                    !currency.matches(Regex("^[A-Z]{3}\$")) -> "Use código de 3 letras (MXN, USD, EUR)"
                    else -> null
                }
            } else null

            return SucursalValidationResult(
                isValid = basePriceError == null && currencyError == null,
                basePriceError = basePriceError,
                currencyError = currencyError
            )
        }

        fun validateAddressInfo(
            street: String, number: String, neighborhood: String,
            zip: String, city: String, country: String
        ): SucursalValidationResult {
            val addressError = when {
                street.isNotEmpty() && street.length > 100 -> "Máximo 100 caracteres para la calle"
                number.isNotEmpty() && number.length > 20 -> "Máximo 20 caracteres para el número"
                neighborhood.isNotEmpty() && neighborhood.length > 50 -> "Máximo 50 caracteres para la colonia"
                city.isNotEmpty() && city.length > 50 -> "Máximo 50 caracteres para la ciudad"
                country.isNotEmpty() && country.length > 50 -> "Máximo 50 caracteres para el país"
                else -> null
            }

            val zipError = if (zip.isNotEmpty()) {
                when {
                    !zip.matches(Regex("^[0-9]{5}\$")) -> "El código postal debe tener 5 dígitos"
                    else -> null
                }
            } else null

            return SucursalValidationResult(
                isValid = addressError == null && zipError == null,
                addressError = addressError,
                zipError = zipError
            )
        }

        fun validateTaxInfo(taxId: String): SucursalValidationResult {
            val taxIdError = if (taxId.isNotEmpty()) {
                when {
                    !taxId.matches(Regex("^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}\$")) -> "Formato de RFC inválido"
                    else -> null
                }
            } else null

            return SucursalValidationResult(
                isValid = taxIdError == null,
                taxIdError = taxIdError
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSucursalScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val zonas = listOf("", "Norte", "Sur", "Este", "Oeste", "Centro", "Noreste", "Noroeste", "Sureste", "Suroeste")

    // Estados del formulario
    var currentStep by remember { mutableStateOf(SucursalFormStep.PERSONAL_INFO) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("MXN") }
    var addressStreet by remember { mutableStateOf("") }
    var addressNumber by remember { mutableStateOf("") }
    var addressNeighborhood by remember { mutableStateOf("") }
    var addressZip by remember { mutableStateOf("") }
    var addressCity by remember { mutableStateOf("") }
    var addressCountry by remember { mutableStateOf("México") }
    var taxName by remember { mutableStateOf("") }
    var taxId by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("") }
    var isNew by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(true) }
    var expandedZonas by remember { mutableStateOf(false) }

    var formState by remember { mutableStateOf<SucursalFormState>(SucursalFormState.Idle) }
    var validationResult by remember { mutableStateOf(SucursalValidationResult(true)) }

    // Obtener precio base de la base de datos
    val preciosBase by remember { mutableStateOf(repository.getAllPreciosBase()) }
    val precioBaseDefault by remember {
        mutableStateOf(preciosBase.firstOrNull()?.precio?.toString() ?: "1500.00")
    }

    // Definir las funciones de cambio que faltaban
    val onIsNewChange: (Boolean) -> Unit = { isNew = it }
    val onActiveChange: (Boolean) -> Unit = { active = it }

    // Función para capitalizar automáticamente
    fun capitalizeText(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word.substring(0, 1).uppercase() + word.substring(1).lowercase()
            } else {
                word
            }
        }.trim()
    }

    // Función de validación por paso
    fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            SucursalFormStep.PERSONAL_INFO -> {
                val result = SucursalValidator.validatePersonalInfo(name, email, phone)
                validationResult = result
                result.isValid
            }
            SucursalFormStep.DETAIL_INFO -> {
                val result = SucursalValidator.validateDetailInfo(basePrice, currency)
                validationResult = result
                result.isValid
            }
            SucursalFormStep.ADDRESS_INFO -> {
                val result = SucursalValidator.validateAddressInfo(
                    addressStreet, addressNumber, addressNeighborhood,
                    addressZip, addressCity, addressCountry
                )
                validationResult = result
                result.isValid
            }
            SucursalFormStep.ADDITIONAL_INFO -> {
                val result = SucursalValidator.validateTaxInfo(taxId)
                validationResult = result
                result.isValid
            }
            SucursalFormStep.CONFIRMATION -> true
        }
    }

    fun proceedToNext() {
        when (currentStep) {
            SucursalFormStep.PERSONAL_INFO -> {
                if (validateCurrentStep()) {
                    currentStep = SucursalFormStep.DETAIL_INFO
                }
            }
            SucursalFormStep.DETAIL_INFO -> {
                if (validateCurrentStep()) {
                    currentStep = SucursalFormStep.ADDRESS_INFO
                }
            }
            SucursalFormStep.ADDRESS_INFO -> {
                if (validateCurrentStep()) {
                    currentStep = SucursalFormStep.ADDITIONAL_INFO
                }
            }
            SucursalFormStep.ADDITIONAL_INFO -> {
                if (validateCurrentStep()) {
                    currentStep = SucursalFormStep.CONFIRMATION
                }
            }
            SucursalFormStep.CONFIRMATION -> {
                formState = SucursalFormState.Loading
                try {
                    repository.insertFranchise(
                        name,
                        email.ifEmpty { null },
                        phone.ifEmpty { null },
                        basePrice.ifEmpty { precioBaseDefault }?.toDoubleOrNull(),
                        currency.ifEmpty { null },
                        addressStreet.ifEmpty { null },
                        addressNumber.ifEmpty { null },
                        addressNeighborhood.ifEmpty { null },
                        addressZip.ifEmpty { null },
                        addressCity.ifEmpty { null },
                        addressCountry.ifEmpty { null },
                        taxName.ifEmpty { null },
                        taxId.ifEmpty { null },
                        zone.ifEmpty { null },
                        if (isNew) 1 else 0,
                        if (active) 1 else 0
                    )
                    formState = SucursalFormState.Success
                    onDismiss()
                } catch (e: Exception) {
                    formState = SucursalFormState.Error(
                        "Error al registrar la sucursal: ${e.message ?: "Error desconocido"}",
                        retryable = true
                    )
                    e.printStackTrace()
                }
            }
        }
    }

    fun proceedBack() {
        if (currentStep.ordinal > 0) {
            currentStep = SucursalFormStep.entries[currentStep.ordinal - 1]
            formState = SucursalFormState.Idle
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
                .widthIn(min = 400.dp, max = 600.dp)
                .heightIn(max = 800.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundLogo()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header con progreso
                    val progress by animateFloatAsState(
                        targetValue = when (currentStep) {
                            SucursalFormStep.PERSONAL_INFO -> 0.2f
                            SucursalFormStep.DETAIL_INFO -> 0.4f
                            SucursalFormStep.ADDRESS_INFO -> 0.6f
                            SucursalFormStep.ADDITIONAL_INFO -> 0.8f
                            SucursalFormStep.CONFIRMATION -> 1.0f
                        },
                        animationSpec = tween(300),
                        label = "progress"
                    )

                    Text(
                        text = "Registro de Sucursal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.Primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (currentStep) {
                            SucursalFormStep.PERSONAL_INFO -> "Paso 1: Información Básica"
                            SucursalFormStep.DETAIL_INFO -> "Paso 2: Detalles de Precios"
                            SucursalFormStep.ADDRESS_INFO -> "Paso 3: Dirección"
                            SucursalFormStep.ADDITIONAL_INFO -> "Paso 4: Información Adicional"
                            SucursalFormStep.CONFIRMATION -> "Paso 5: Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    // Contenido del formulario
                    when (currentStep) {
                        SucursalFormStep.PERSONAL_INFO -> PersonalInfoStep(
                            name = name,
                            email = email,
                            phone = phone,
                            validationResult = validationResult,
                            onNameChange = {
                                name = capitalizeText(it)
                            },
                            onEmailChange = {
                                email = it.lowercase().filter { char ->
                                    char.isLetterOrDigit() || char in listOf('@', '.', '_', '-')
                                }
                            },
                            onPhoneChange = {
                                phone = it.filter { char -> char.isDigit() }.take(10)
                            },
                            focusManager = focusManager
                        )

                        SucursalFormStep.DETAIL_INFO -> DetailInfoStep(
                            basePrice = if (basePrice.isEmpty()) precioBaseDefault else basePrice,
                            currency = currency,
                            validationResult = validationResult,
                            onBasePriceChange = { basePrice = it },
                            onCurrencyChange = { currency = it },
                            focusManager = focusManager
                        )

                        SucursalFormStep.ADDRESS_INFO -> AddressInfoStep(
                            street = addressStreet,
                            number = addressNumber,
                            neighborhood = addressNeighborhood,
                            zip = addressZip,
                            city = addressCity,
                            country = addressCountry,
                            validationResult = validationResult,
                            onStreetChange = { addressStreet = capitalizeText(it) },
                            onNumberChange = { addressNumber = it.filter { char -> char.isLetterOrDigit() } },
                            onNeighborhoodChange = { addressNeighborhood = capitalizeText(it) },
                            onZipChange = { addressZip = it.filter { char -> char.isDigit() }.take(5) },
                            onCityChange = { addressCity = capitalizeText(it) },
                            onCountryChange = { addressCountry = capitalizeText(it) },
                            focusManager = focusManager
                        )

                        SucursalFormStep.ADDITIONAL_INFO -> AdditionalInfoStep(
                            taxName = taxName,
                            taxId = taxId,
                            zone = zone,
                            zonas = zonas,
                            expandedZonas = expandedZonas,
                            isNew = isNew,
                            active = active,
                            onTaxNameChange = { taxName = capitalizeText(it) },
                            onTaxIdChange = { taxId = it.uppercase().filter { char ->
                                char.isLetterOrDigit() || char == '&' || char == 'Ñ'
                            }},
                            onZoneChange = { zone = it },
                            onExpandedZonasChange = { expandedZonas = it },
                            onIsNewChange = onIsNewChange,
                            onActiveChange = onActiveChange,
                            validationResult = validationResult
                        )

                        SucursalFormStep.CONFIRMATION -> ConfirmationStep(
                            name = name,
                            email = email,
                            phone = phone,
                            basePrice = basePrice,
                            currency = currency,
                            addressStreet = addressStreet,
                            addressNumber = addressNumber,
                            addressNeighborhood = addressNeighborhood,
                            addressZip = addressZip,
                            addressCity = addressCity,
                            addressCountry = addressCountry,
                            taxName = taxName,
                            taxId = taxId,
                            zone = zone,
                            isNew = isNew,
                            active = active,
                            formState = formState
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Botones de navegación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = formState !is SucursalFormState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFff8abe),
                                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar")
                        }

                        if (currentStep != SucursalFormStep.PERSONAL_INFO) {
                            Button(
                                onClick = { proceedBack() },
                                modifier = Modifier.weight(1f),
                                enabled = formState !is SucursalFormState.Loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFff8abe),
                                    disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Atrás")
                            }
                        }

                        Button(
                            onClick = { proceedToNext() },
                            modifier = Modifier.weight(1f),
                            enabled = formState !is SucursalFormState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                disabledContainerColor = AppColors.Primary.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (formState is SucursalFormState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    when (currentStep) {
                                        SucursalFormStep.CONFIRMATION -> "Registrar"
                                        else -> "Siguiente"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationStep(
    name: String,
    email: String,
    phone: String,
    basePrice: String,
    currency: String,
    addressStreet: String,
    addressNumber: String,
    addressNeighborhood: String,
    addressZip: String,
    addressCity: String,
    addressCountry: String,
    taxName: String,
    taxId: String,
    zone: String,
    isNew: Boolean,
    active: Boolean,
    formState: SucursalFormState
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 400.dp)
    ) {
        item {
            Text(
                text = "Confirmar datos de la sucursal:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Información Básica",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )

                    ConfirmationField("Nombre", name)
                    if (email.isNotEmpty()) ConfirmationField("Email", email)
                    if (phone.isNotEmpty()) ConfirmationField("Teléfono", phone)
                }
            }
        }

        if (basePrice.isNotEmpty() || currency.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Información de Precios",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Primary
                        )

                        if (basePrice.isNotEmpty()) ConfirmationField("Precio base", "$$basePrice")
                        if (currency.isNotEmpty()) ConfirmationField("Moneda", currency)
                    }
                }
            }
        }

        if (addressStreet.isNotEmpty() || addressNumber.isNotEmpty() ||
            addressNeighborhood.isNotEmpty() || addressZip.isNotEmpty() ||
            addressCity.isNotEmpty() || addressCountry.isNotEmpty()
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Dirección",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Primary
                        )

                        val fullAddress = buildString {
                            if (addressStreet.isNotEmpty()) append(addressStreet)
                            if (addressNumber.isNotEmpty()) append(" $addressNumber")
                            if (addressNeighborhood.isNotEmpty()) append(", $addressNeighborhood")
                            if (addressZip.isNotEmpty()) append(", CP $addressZip")
                            if (addressCity.isNotEmpty()) append(", $addressCity")
                            if (addressCountry.isNotEmpty()) append(", $addressCountry")
                        }

                        if (fullAddress.isNotEmpty()) {
                            ConfirmationField("Dirección completa", fullAddress)
                        }
                    }
                }
            }
        }

        if (taxName.isNotEmpty() || taxId.isNotEmpty() || zone.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Información Adicional",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Primary
                        )

                        if (taxName.isNotEmpty()) ConfirmationField("Razón social", taxName)
                        if (taxId.isNotEmpty()) ConfirmationField("RFC/Tax ID", taxId)
                        if (zone.isNotEmpty()) ConfirmationField("Zona", zone)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )

                    ConfirmationField("Sucursal nueva", if (isNew) "Sí" else "No")
                    ConfirmationField("Activa", if (active) "Sí" else "No")
                }
            }
        }

        if (formState is SucursalFormState.Error) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = formState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (formState.retryable) {
                            Text(
                                text = "Puede intentar nuevamente",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun BackgroundLogo() {
    val painter = painterResource(Res.drawable.logoSystem)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                .alpha(0.08f),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInfoStep(
    name: String,
    email: String,
    phone: String,
    validationResult: SucursalValidationResult,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    focusManager: FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre de la sucursal *") },
            isError = validationResult.nameError != null,
            supportingText = {
                validationResult.nameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            placeholder = { Text("Zona Esmeralda") }
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = validationResult.emailError != null,
            supportingText = {
                validationResult.emailError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            placeholder = { Text("ejemplo@correo.com") }
        )

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Teléfono *") },
            isError = validationResult.phoneError != null,
            supportingText = {
                validationResult.phoneError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                } ?: if (phone.isNotEmpty()) {
                    Text("${phone.length}/10 dígitos", color = Color.Gray)
                } else {
                    null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            placeholder = { Text("5512345678") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailInfoStep(
    basePrice: String,
    currency: String,
    validationResult: SucursalValidationResult,
    onBasePriceChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    focusManager: FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = basePrice,
            onValueChange = { value ->
                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    onBasePriceChange(value)
                }
            },
            label = { Text("Precio base *") },
            isError = validationResult.basePriceError != null,
            supportingText = {
                validationResult.basePriceError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            leadingIcon = { Text("$") },
            placeholder = { Text("1500.00") }
        )

        OutlinedTextField(
            value = currency,
            onValueChange = { value ->
                onCurrencyChange(value.uppercase().take(3))
            },
            label = { Text("Moneda") },
            isError = validationResult.currencyError != null,
            supportingText = {
                validationResult.currencyError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            placeholder = { Text("MXN") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressInfoStep(
    street: String,
    number: String,
    neighborhood: String,
    zip: String,
    city: String,
    country: String,
    validationResult: SucursalValidationResult,
    onStreetChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onNeighborhoodChange: (String) -> Unit,
    onZipChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    focusManager: FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = street,
                onValueChange = onStreetChange,
                label = { Text("Calle") },
                modifier = Modifier.weight(0.7f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                placeholder = { Text("Av. Principal") }
            )

            OutlinedTextField(
                value = number,
                onValueChange = onNumberChange,
                label = { Text("Número") },
                modifier = Modifier.weight(0.3f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                placeholder = { Text("123") }
            )
        }

        OutlinedTextField(
            value = neighborhood,
            onValueChange = onNeighborhoodChange,
            label = { Text("Colonia/Barrio") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            placeholder = { Text("Centro") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = zip,
                onValueChange = onZipChange,
                label = { Text("Código postal") },
                isError = validationResult.zipError != null,
                supportingText = {
                    validationResult.zipError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    } ?: if (zip.isNotEmpty()) {
                        Text("${zip.length}/5 dígitos", color = Color.Gray)
                    } else {
                        null
                    }
                },
                modifier = Modifier.weight(0.4f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                placeholder = { Text("12345") }
            )

            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = { Text("Ciudad") },
                modifier = Modifier.weight(0.6f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                placeholder = { Text("Ciudad de México") }
            )
        }

        OutlinedTextField(
            value = country,
            onValueChange = onCountryChange,
            label = { Text("País") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            placeholder = { Text("México") }
        )

        validationResult.addressError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdditionalInfoStep(
    taxName: String,
    taxId: String,
    zone: String,
    zonas: List<String>,
    expandedZonas: Boolean,
    isNew: Boolean,
    active: Boolean,
    onTaxNameChange: (String) -> Unit,
    onTaxIdChange: (String) -> Unit,
    onZoneChange: (String) -> Unit,
    onExpandedZonasChange: (Boolean) -> Unit,
    onIsNewChange: (Boolean) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    validationResult: SucursalValidationResult
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sucursal nueva",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Marcar si es una sucursal nueva",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isNew,
                        onCheckedChange = onIsNewChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Primary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Activa",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Si la sucursal está activa",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = active,
                        onCheckedChange = onActiveChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Primary)
                    )
                }
            }
        }

        OutlinedTextField(
            value = taxName,
            onValueChange = onTaxNameChange,
            label = { Text("Razón social") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            placeholder = { Text("Empresa S.A. de C.V.") }
        )

        OutlinedTextField(
            value = taxId,
            onValueChange = onTaxIdChange,
            label = { Text("RFC") },
            isError = validationResult.taxIdError != null,
            supportingText = {
                validationResult.taxIdError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            placeholder = { Text("XAXX010101000") }
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = zone,
                onValueChange = {},
                label = { Text("Zona") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar zonas")
                },
                placeholder = { Text("Selecciona una zona") }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .alpha(0f)
                    .clickable { onExpandedZonasChange(true) }
            )
        }

        // Menú desplegable de zonas
        DropdownMenu(
            expanded = expandedZonas,
            onDismissRequest = { onExpandedZonasChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            zonas.forEach { zonaItem ->
                DropdownMenuItem(
                    text = { Text(if (zonaItem.isEmpty()) "Sin zona" else zonaItem) },
                    onClick = {
                        onZoneChange(zonaItem)
                        onExpandedZonasChange(false)
                    }
                )
            }
        }
    }
}