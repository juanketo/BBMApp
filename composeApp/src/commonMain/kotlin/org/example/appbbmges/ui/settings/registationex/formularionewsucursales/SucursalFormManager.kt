package org.example.appbbmges.ui.settings.registationex.formularionewsucursales

import androidx.compose.runtime.*
import org.example.appbbmges.data.Repository

object SucursalConstants {
    val ZONAS = listOf(
        "", "Norte", "Sur", "Este", "Oeste", "Centro",
        "Noreste", "Noroeste", "Sureste", "Suroeste"
    )

    const val DEFAULT_CURRENCY = "MXN"
    const val DEFAULT_COUNTRY = "México"
    const val DEFAULT_BASE_PRICE = "1500.00"
}

@Composable
fun rememberSucursalFormManager(
    repository: Repository,
    onDismiss: () -> Unit
): SucursalFormManager {
    return remember {
        SucursalFormManager(repository, onDismiss)
    }
}

class SucursalFormManager(
    private val repository: Repository,
    private val onDismiss: () -> Unit
) {
    // Estados del formulario
    var currentStep by mutableStateOf(SucursalFormStep.PERSONAL_INFO)
        private set

    var formData by mutableStateOf(SucursalFormData())
        private set

    var formState by mutableStateOf<SucursalFormState>(SucursalFormState.Idle)
        private set

    var validationResult by mutableStateOf(SucursalValidationResult(true))
        private set

    var expandedZonas by mutableStateOf(false)
        private set

    private val preciosBase by lazy { repository.getAllPreciosBase() }
    private val precioBaseDefault by lazy {
        preciosBase.firstOrNull()?.precio?.toString() ?: SucursalConstants.DEFAULT_BASE_PRICE
    }

    fun updateFormData(update: SucursalFormData.() -> SucursalFormData) {
        formData = formData.update()
    }

    fun updateName(name: String) {
        updateFormData { copy(name = TextUtils.capitalizeText(name)) }
    }

    fun updateEmail(email: String) {
        val formattedEmail = email.lowercase().filter { char ->
            char.isLetterOrDigit() || char in listOf('@', '.', '_', '-', '+')
        }
        updateFormData { copy(email = formattedEmail) }
    }

    fun updatePhone(phone: String) {
        val formattedPhone = phone.filter { char -> char.isDigit() }.take(10)
        updateFormData { copy(phone = formattedPhone) }
    }

    fun updateAddressStreet(street: String) {
        updateFormData { copy(addressStreet = TextUtils.capitalizeText(street)) }
    }

    fun updateAddressNumber(number: String) {
        val formattedNumber = number.filter { char ->
            char.isLetterOrDigit() || char in listOf('#', '-')
        }
        updateFormData { copy(addressNumber = formattedNumber) }
    }

    fun updateAddressNeighborhood(neighborhood: String) {
        updateFormData { copy(addressNeighborhood = TextUtils.capitalizeText(neighborhood)) }
    }

    fun updateAddressZip(zip: String) {
        val formattedZip = zip.filter { char -> char.isDigit() }.take(5)
        updateFormData { copy(addressZip = formattedZip) }
    }

    fun updateAddressCity(city: String) {
        updateFormData { copy(addressCity = TextUtils.capitalizeText(city)) }
    }

    fun updateAddressCountry(country: String) {
        updateFormData { copy(addressCountry = TextUtils.capitalizeText(country)) }
    }

    fun updateTaxName(taxName: String) {
        updateFormData { copy(taxName = TextUtils.capitalizeText(taxName)) }
    }

    fun updateTaxId(taxId: String) {
        val formattedTaxId = taxId.uppercase().filter { char ->
            char.isLetterOrDigit() || char == '&'
        }.take(13) // RFC máximo 13 caracteres
        updateFormData { copy(taxId = formattedTaxId) }
    }

    fun updateCurrency(currency: String) {
        val formattedCurrency = currency.uppercase().take(3)
        updateFormData { copy(currency = formattedCurrency) }
    }

    fun updateZone(zone: String) {
        updateFormData { copy(zone = zone) }
    }

    fun updateIsNew(isNew: Boolean) {
        updateFormData { copy(isNew = isNew) }
    }

    fun updateActive(active: Boolean) {
        updateFormData { copy(active = active) }
    }

    fun updateBasePrice(basePrice: String) {
        updateFormData { copy(basePrice = basePrice) }
    }

    fun updateExpandedZonas(expanded: Boolean) {
        expandedZonas = expanded
    }

    fun getEffectiveBasePrice(): String {
        return if (formData.basePrice.isEmpty()) precioBaseDefault else formData.basePrice
    }

    private fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            SucursalFormStep.PERSONAL_INFO -> {
                val result = SucursalValidator.validatePersonalInfo(
                    formData.name,
                    formData.email,
                    formData.phone
                )
                validationResult = result
                result.isValid
            }
            SucursalFormStep.DETAIL_INFO -> {
                val result = SucursalValidator.validateDetailInfo(
                    getEffectiveBasePrice(),
                    formData.currency
                )
                validationResult = result
                result.isValid
            }
            SucursalFormStep.ADDRESS_INFO -> {
                val result = SucursalValidator.validateAddressInfo(
                    formData.addressStreet,
                    formData.addressNumber,
                    formData.addressNeighborhood,
                    formData.addressZip,
                    formData.addressCity,
                    formData.addressCountry
                )
                validationResult = result
                result.isValid
            }
            SucursalFormStep.ADDITIONAL_INFO -> {
                val result = SucursalValidator.validateTaxInfo(formData.taxId)
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
                submitForm()
            }
        }
    }

    fun proceedBack() {
        if (currentStep.ordinal > 0) {
            currentStep = SucursalFormStep.entries[currentStep.ordinal - 1]
            formState = SucursalFormState.Idle
        }
    }

    private fun submitForm() {
        formState = SucursalFormState.Loading

        try {
            val effectiveBasePrice = getEffectiveBasePrice()

            repository.insertFranchise(
                formData.name,
                formData.email.ifEmpty { null },
                formData.phone.ifEmpty { null },
                effectiveBasePrice.toDoubleOrNull(),
                formData.currency.ifEmpty { null },
                formData.addressStreet.ifEmpty { null },
                formData.addressNumber.ifEmpty { null },
                formData.addressNeighborhood.ifEmpty { null },
                formData.addressZip.ifEmpty { null },
                formData.addressCity.ifEmpty { null },
                formData.addressCountry.ifEmpty { null },
                formData.taxName.ifEmpty { null },
                formData.taxId.ifEmpty { null },
                formData.zone.ifEmpty { null },
                if (formData.isNew) 1 else 0,
                if (formData.active) 1 else 0
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

    fun getProgress(): Float {
        return when (currentStep) {
            SucursalFormStep.PERSONAL_INFO -> 0.2f
            SucursalFormStep.DETAIL_INFO -> 0.4f
            SucursalFormStep.ADDRESS_INFO -> 0.6f
            SucursalFormStep.ADDITIONAL_INFO -> 0.8f
            SucursalFormStep.CONFIRMATION -> 1.0f
        }
    }

    fun getCurrentStepTitle(): String {
        return when (currentStep) {
            SucursalFormStep.PERSONAL_INFO -> "Paso 1: Información Básica"
            SucursalFormStep.DETAIL_INFO -> "Paso 2: Detalles de Precios"
            SucursalFormStep.ADDRESS_INFO -> "Paso 3: Dirección"
            SucursalFormStep.ADDITIONAL_INFO -> "Paso 4: Información Adicional"
            SucursalFormStep.CONFIRMATION -> "Paso 5: Confirmación"
        }
    }

    fun getActionButtonText(): String {
        return when (currentStep) {
            SucursalFormStep.CONFIRMATION -> "Registrar"
            else -> "Siguiente"
        }
    }

    fun canGoBack(): Boolean {
        return currentStep != SucursalFormStep.PERSONAL_INFO
    }

    fun isLoading(): Boolean {
        return formState is SucursalFormState.Loading
    }
}