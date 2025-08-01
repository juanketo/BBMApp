package org.example.appbbmges.ui.usuarios.registation.studentsform
object FormValidation {
    fun validatePersonalInfo(data: StudentFormData): FormErrors {
        return FormErrors(
            firstName = validateName(data.firstName, "nombre", true),
            lastNamePaternal = validateName(data.lastNamePaternal, "apellido paterno", false),
            lastNameMaternal = validateName(data.lastNameMaternal, "apellido materno", false),
            birthDate = validateBirthDate(data.birthDate),
            curp = validateCURP(data.curp),
            phone = validatePhone(data.phone),
            email = validateEmail(data.email)
        )
    }

    fun validateAddressInfo(data: StudentFormData): FormErrors {
        return FormErrors(
            addressStreet = validateAddressStreet(data.addressStreet),
            addressZip = validateAddressZip(data.addressZip)
        )
    }

    private fun validateName(name: String, fieldName: String, required: Boolean): String? {
        println("Validando $fieldName: '$name'")
        if (required && name.isBlank()) return "El $fieldName es obligatorio."
        if (name.isNotBlank()) {
            if (name.length !in 2..50) return "El $fieldName debe tener entre 2 y 50 caracteres."
            if (name.any { it.isDigit() }) return "El $fieldName no puede contener números."
            if (!name.matches("^[A-Za-zÀ-ÿ ]+$".toRegex())) return "El $fieldName solo puede contener letras y espacios."
        }
        return null
    }

    private fun validateBirthDate(date: String): String? {
        if (date.isBlank()) return null
        val age = DateUtils.calculateAge(date)
        return when {
            age == null -> "Formato de fecha inválido (use dd/MM/yyyy)."
            age < 1.5 || age > 18 -> "La edad debe estar entre 1.5 y 18 años."
            else -> null
        }
    }

    private fun validateCURP(curp: String): String? {
        if (curp.isEmpty()) return null
        return when {
            curp.length != 18 -> "El CURP debe tener exactamente 18 caracteres."
            !curp.matches("^[A-Z]{4}\\d{6}[HM][A-Z]{5}\\d{2}$".toRegex()) -> "Formato de CURP inválido."
            else -> null
        }
    }

    private fun validatePhone(phone: String): String? {
        if (phone.isEmpty()) return null
        return if (!phone.matches("^\\d{10}$".toRegex())) "El teléfono debe tener 10 dígitos."
        else null
    }

    private fun validateEmail(email: String): String? {
        if (email.isEmpty()) return null
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return if (!emailPattern.matches(email)) "Formato de email no válido."
        else null
    }

    private fun validateAddressStreet(address: String): String? {
        if (address.isEmpty()) return null
        return if (address.length !in 5..100) "La calle debe tener entre 5 y 100 caracteres."
        else null
    }

    private fun validateAddressZip(zip: String): String? {
        if (zip.isEmpty()) return null
        return if (zip.length != 5 || !zip.all { it.isDigit() }) "El código postal debe tener 5 dígitos."
        else null
    }
}