package org.example.appbbmges.ui.usuarios.registation.studentsform

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

object DateUtils {
    fun calculateAge(birthDateString: String): Double? {
        if (birthDateString.length != 10 || !birthDateString.matches("\\d{2}/\\d{2}/\\d{4}".toRegex())) {
            return null
        }
        val parts = birthDateString.split("/")
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        if (day < 1 || day > 31 || month < 1 || month > 12) return null

        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        var age = currentDate.year - year
        if (currentDate.monthNumber < month || (currentDate.monthNumber == month && currentDate.dayOfMonth < day)) {
            age--
        }
        val monthDiff = if (currentDate.monthNumber >= month) {
            currentDate.monthNumber - month
        } else {
            12 + currentDate.monthNumber - month
        }
        return age + (monthDiff / 12.0)
    }

    fun formatDateFromMillis(millis: Long): String {
        // Convertir milisegundos a días desde epoch
        val daysSinceEpoch = millis / (24 * 60 * 60 * 1000)

        // Crear LocalDate directamente desde días, evitando problemas de zona horaria
        val epochDate = LocalDate(1970, 1, 1)
        val selectedDate = epochDate.plus(kotlinx.datetime.DatePeriod(days = daysSinceEpoch.toInt()))

        return "${selectedDate.dayOfMonth.toString().padStart(2, '0')}/${selectedDate.monthNumber.toString().padStart(2, '0')}/${selectedDate.year}"
    }
}

object CURPGenerator {
    fun generate(
        firstName: String,
        lastNamePaternal: String,
        lastNameMaternal: String,
        birthDate: String,
        gender: String
    ): String {
        println("Generando CURP para: $firstName $lastNamePaternal $lastNameMaternal")
        if (firstName.isBlank() || lastNamePaternal.isBlank() || birthDate.length != 10) return ""
        val parts = birthDate.split("/")
        if (parts.size != 3) return ""
        val day = parts[0].padStart(2, '0')
        val month = parts[1].padStart(2, '0')
        val year = parts[2].takeLast(2)
        val vowels = "AEIOU"
        val consonants = "BCDFGHJKLMNPQRSTVWXYZ"
        val firstLetter = lastNamePaternal.first().uppercaseChar()
        val firstVowel = lastNamePaternal.drop(1).firstOrNull { it.uppercaseChar() in vowels }?.uppercaseChar() ?: 'X'
        val secondLetter = if (lastNameMaternal.isNotBlank()) lastNameMaternal.first().uppercaseChar() else 'X'
        val cleanFirstName = firstName.trim().split(" ").firstOrNull { it.isNotBlank() } ?: return ""
        val thirdLetter = cleanFirstName.first().uppercaseChar()
        val sexLetter = when (gender.lowercase()) {
            "masculino", "hombre", "m" -> "H"
            "femenino", "mujer", "f" -> "M"
            else -> "H"
        }
        val state = "DF"
        val firstConsonant = lastNamePaternal.drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'
        val secondConsonant = if (lastNameMaternal.isNotBlank()) {
            lastNameMaternal.drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'
        } else 'X'
        val thirdConsonant = cleanFirstName.drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'
        val randomDigits = "01"
        return "$firstLetter$firstVowel$secondLetter$thirdLetter$year$month${day}$sexLetter$state$firstConsonant$secondConsonant$thirdConsonant$randomDigits"
    }
}

object TextFormatters {
    /**
     * Formatea un nombre o apellido para que cada palabra comience con mayúscula y no tenga espacios extra.
     */
    fun formatName(input: String): String {
        if (input.isBlank()) return ""
        return input.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.titlecase() }
            }
    }

    /**
     * Limpia y formatea la entrada del usuario en tiempo real para nombres.
     * Permite letras y un solo espacio entre palabras, y formatea cada palabra.
     * ESTA ES LA FUNCIÓN PRINCIPAL QUE SOLUCIONA EL PROBLEMA
     */
    fun cleanAndFormatNameInput(input: String): String {
        // Filtrar solo letras y espacios
        val filtered = input.filter { it.isLetter() || it.isWhitespace() }

        // Reemplazar múltiples espacios con uno solo
        val singleSpaced = filtered.replace("\\s{2,}".toRegex(), " ")

        // No permitir espacios al inicio
        val noLeadingSpace = if (singleSpaced.startsWith(" ")) {
            singleSpaced.substring(1)
        } else singleSpaced

        // Formatear en tiempo real
        return formatRealTime(noLeadingSpace)
    }

    /**
     * Formatea en tiempo real mientras el usuario escribe
     */
    private fun formatRealTime(input: String): String {
        if (input.isBlank()) return ""

        val words = input.split(" ")
        return words.mapIndexed { index, word ->
            if (word.isNotBlank()) {
                // Formatear todas las palabras
                word.lowercase().replaceFirstChar { it.titlecase() }
            } else word
        }.joinToString(" ")
    }

    /**
     * Versión simple para limpiar entrada (mantener para compatibilidad)
     */
    fun cleanNameInput(input: String): String {
        val filtered = input.filter { it.isLetter() || it.isWhitespace() }
        return filtered.replace("\\s{2,}".toRegex(), " ")
    }
}