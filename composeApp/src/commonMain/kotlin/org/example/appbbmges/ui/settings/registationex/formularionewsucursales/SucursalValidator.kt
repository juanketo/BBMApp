package org.example.appbbmges.ui.settings.registationex.formularionewsucursales
object TextUtils {
    fun capitalizeText(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word.substring(0, 1).uppercase() + word.substring(1).lowercase()
            } else {
                word
            }
        }
    }
}

object SucursalValidator {

    fun validatePersonalInfo(name: String, email: String, phone: String): SucursalValidationResult {
        val nameError = validateName(name, "nombre de la sucursal", true)
        val emailError = validateEmail(email)
        val phoneError = validatePhone(phone)

        return SucursalValidationResult(
            isValid = nameError == null && emailError == null && phoneError == null,
            nameError = nameError,
            emailError = emailError,
            phoneError = phoneError
        )
    }

    fun validateDetailInfo(basePrice: String, currency: String): SucursalValidationResult {
        val basePriceError = validateBasePrice(basePrice)
        val currencyError = validateCurrency(currency)

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
        val addressError = validateAddressFields(street, number, neighborhood, city, country)
        val zipError = validateZipCode(zip)

        return SucursalValidationResult(
            isValid = addressError == null && zipError == null,
            addressError = addressError,
            zipError = zipError
        )
    }

    fun validateTaxInfo(taxId: String): SucursalValidationResult {
        val taxIdError = validateRFC(taxId)

        return SucursalValidationResult(
            isValid = taxIdError == null,
            taxIdError = taxIdError
        )
    }
    private fun validateName(name: String, fieldName: String, required: Boolean): String? {
        if (required && name.isBlank()) return "El $fieldName es obligatorio."
        if (name.isNotBlank()) {
            when {
                name.length !in 3..100 -> return "El $fieldName debe tener entre 3 y 100 caracteres."
                name.any { it.isDigit() } -> return "El $fieldName no puede contener números."
                !name.matches("^[A-Za-zÀ-ÿ ]+\$".toRegex()) -> return "El $fieldName solo puede contener letras y espacios."
            }
        }
        return null
    }

    private fun validateEmail(email: String): String? {
        if (email.isEmpty()) return null

        return when {
            email.length > 100 -> "El email no puede tener más de 100 caracteres."
            !email.matches("^[a-z0-9+_.-]+@[a-z0-9.-]+\\.[a-z]{2,}\$".toRegex()) -> {
                when {
                    !email.contains("@") -> "El email debe contener el símbolo @."
                    email.count { it == '@' } > 1 -> "El email solo puede tener un símbolo @."
                    !email.contains(".") -> "El email debe contener un punto (.) en el dominio."
                    email.contains(Regex("[A-Z]")) -> "El email solo debe contener letras minúsculas."
                    email.contains(Regex("[^a-z0-9+_.@-]")) -> "El email contiene caracteres no válidos."
                    else -> "Formato de email no válido."
                }
            }
            else -> null
        }
    }

    private fun validatePhone(phone: String): String? {
        if (phone.isEmpty()) return null

        return when {
            phone.length < 10 -> "El teléfono debe tener exactamente 10 dígitos. Faltan ${10 - phone.length} dígitos."
            phone.length > 10 -> "El teléfono debe tener exactamente 10 dígitos. Sobran ${phone.length - 10} dígitos."
            !phone.all { it.isDigit() } -> "El teléfono solo puede contener números."
            else -> null
        }
    }

    private fun validateBasePrice(basePrice: String): String? {
        if (basePrice.isEmpty()) return null

        val price = basePrice.toDoubleOrNull()
        return when {
            price == null -> "El precio base debe ser un número válido (ejemplo: 1500.00)."
            price < 0 -> "El precio no puede ser negativo."
            price > 999999.99 -> "El precio es demasiado alto (máximo: $999,999.99)."
            basePrice.contains(",") -> "Use punto (.) para decimales, no coma (,)."
            else -> null
        }
    }

    private fun validateCurrency(currency: String): String? {
        if (currency.isEmpty()) return null

        return when {
            currency.length < 3 -> "El código de moneda debe tener 3 letras (ejemplo: MXN)."
            currency.length > 3 -> "El código de moneda debe tener exactamente 3 letras."
            !currency.all { it.isLetter() } -> "El código de moneda solo puede contener letras."
            !currency.all { it.isUpperCase() } -> "El código de moneda debe estar en mayúsculas."
            else -> null
        }
    }

    private fun validateAddressFields(
        street: String, number: String, neighborhood: String,
        city: String, country: String
    ): String? {
        return when {
            street.isNotEmpty() && street.length > 100 -> "La calle no puede tener más de 100 caracteres."
            street.isNotEmpty() && !street.matches("^[A-Za-z0-9À-ÿ\\s.,#-]+\$".toRegex()) ->
                "La calle contiene caracteres no válidos. Solo se permiten letras, números, espacios y los símbolos: ., #-"
            number.isNotEmpty() && number.length > 20 -> "El número no puede tener más de 20 caracteres."
            number.isNotEmpty() && !number.matches("^[A-Za-z0-9#-]+\$".toRegex()) ->
                "El número solo puede contener letras, números y los símbolos: #-"
            neighborhood.isNotEmpty() && neighborhood.length > 50 -> "La colonia no puede tener más de 50 caracteres."
            neighborhood.isNotEmpty() && !neighborhood.matches("^[A-Za-z0-9À-ÿ\\s.,#-]+\$".toRegex()) ->
                "La colonia contiene caracteres no válidos."
            city.isNotEmpty() && city.length > 50 -> "La ciudad no puede tener más de 50 caracteres."
            city.isNotEmpty() && !city.matches("^[A-Za-zÀ-ÿ ]+\$".toRegex()) ->
                "La ciudad solo puede contener letras y espacios."
            country.isNotEmpty() && country.length > 50 -> "El país no puede tener más de 50 caracteres."
            country.isNotEmpty() && !country.matches("^[A-Za-zÀ-ÿ ]+\$".toRegex()) ->
                "El país solo puede contener letras y espacios."
            else -> null
        }
    }

    private fun validateZipCode(zip: String): String? {
        if (zip.isEmpty()) return null

        return when {
            zip.length < 5 -> "El código postal debe tener exactamente 5 dígitos. Faltan ${5 - zip.length} dígitos."
            zip.length > 5 -> "El código postal debe tener exactamente 5 dígitos. Sobran ${zip.length - 5} dígitos."
            !zip.all { it.isDigit() } -> "El código postal solo puede contener números."
            else -> null
        }
    }

    private fun validateRFC(rfc: String): String? {
        if (rfc.isEmpty()) return null

        return when {
            rfc.length < 12 -> "El RFC es demasiado corto. Debe tener entre 12 y 13 caracteres."
            rfc.length > 13 -> "El RFC es demasiado largo. Debe tener entre 12 y 13 caracteres."
            rfc.length == 12 -> validateRFCPersonaFisica(rfc)
            rfc.length == 13 -> validateRFCPersonaMoral(rfc)
            else -> "Longitud de RFC inválida."
        }
    }

    private fun validateRFCPersonaFisica(rfc: String): String? {
        return when {
            !rfc.substring(0, 4).all { it.isLetter() && it.isUpperCase() } ->
                "Las primeras 4 posiciones del RFC deben ser letras mayúsculas."
            !rfc.substring(4, 10).all { it.isDigit() } ->
                "Las posiciones 5-10 del RFC deben ser números (fecha de nacimiento)."
            !rfc.substring(10, 12).all { it.isLetter() && it.isUpperCase() } ->
                "Las posiciones 11-12 del RFC deben ser letras mayúsculas."
            !rfc.substring(12, 13).all { it.isLetterOrDigit() && it.isUpperCase() } ->
                "La última posición del RFC debe ser una letra mayúscula o número."
            else -> null
        }
    }

    private fun validateRFCPersonaMoral(rfc: String): String? {
        return when {
            !rfc.substring(0, 3).all { it.isLetter() && it.isUpperCase() } ->
                "Las primeras 3 posiciones del RFC deben ser letras mayúsculas."
            !rfc.substring(3, 9).all { it.isDigit() } ->
                "Las posiciones 4-9 del RFC deben ser números (fecha)."
            !rfc.substring(9, 13).all { it.isLetterOrDigit() && it.isUpperCase() } ->
                "Las últimas 4 posiciones del RFC deben ser letras mayúsculas o números."
            else -> null
        }
    }
}