package org.example.appbbmges.ui.usuarios.registation.studentsform

data class StudentFormData(
    val firstName: String = "",
    val lastNamePaternal: String = "",
    val lastNameMaternal: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val nationality: String = "Mexicana",
    val curp: String = "",
    val countryCode: String = "+52",
    val phone: String = "",
    val email: String = "",
    val addressStreet: String = "",
    val addressZip: String = "",
    val parentFatherName: String = "",
    val parentMotherName: String = "",
    val bloodType: String = "",
    val chronicDisease: String = "",
    val active: Boolean = true
)

data class FormErrors(
    val firstName: String? = null,
    val lastNamePaternal: String? = null,
    val lastNameMaternal: String? = null,
    val birthDate: String? = null,
    val curp: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val addressStreet: String? = null,
    val addressZip: String? = null,
    val general: String? = null
) {
    val isValid: Boolean
        get() = firstName == null && lastNamePaternal == null &&
                lastNameMaternal == null && birthDate == null &&
                curp == null && phone == null && email == null &&
                addressStreet == null && addressZip == null &&
                general == null
}

enum class StudentFormStep {
    PERSONAL_INFO,
    ADDRESS_INFO,
    ADDITIONAL_INFO,
    CONFIRMATION
}