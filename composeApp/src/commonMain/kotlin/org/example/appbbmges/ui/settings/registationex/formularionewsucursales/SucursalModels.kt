package org.example.appbbmges.ui.settings.registationex.formularionewsucursales

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

data class SucursalFormData(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val basePrice: String = "",
    val currency: String = "MXN",
    val addressStreet: String = "",
    val addressNumber: String = "",
    val addressNeighborhood: String = "",
    val addressZip: String = "",
    val addressCity: String = "",
    val addressCountry: String = "México",
    val taxName: String = "",
    val taxId: String = "",
    val zone: String = "",
    val isNew: Boolean = false,
    val active: Boolean = true
)