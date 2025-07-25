package org.example.appbbmges.ui.usuarios.registation.studentsform

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
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
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
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
        if (firstName.isEmpty() || lastNamePaternal.isEmpty() || birthDate.length != 10) return ""
        val parts = birthDate.split("/")
        if (parts.size != 3) return ""
        val day = parts[0].padStart(2, '0')
        val month = parts[1].padStart(2, '0')
        val year = parts[2].takeLast(2)
        val vowels = "AEIOU"
        val consonants = "BCDFGHJKLMNPQRSTVWXYZ"
        val firstLetter = lastNamePaternal.first().uppercaseChar()
        val firstVowel = lastNamePaternal.drop(1).firstOrNull { it.uppercaseChar() in vowels }?.uppercaseChar() ?: 'X'
        val secondLetter = if (lastNameMaternal.isNotEmpty()) lastNameMaternal.first().uppercaseChar() else 'X'
        val thirdLetter = firstName.split(" ").first().first().uppercaseChar()
        val sexLetter = when (gender.lowercase()) {
            "masculino", "hombre", "m" -> "H"
            "femenino", "mujer", "f" -> "M"
            else -> "H"
        }
        val state = "DF"
        val firstConsonant = lastNamePaternal.drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'
        val secondConsonant = if (lastNameMaternal.isNotEmpty()) {
            lastNameMaternal.drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'
        } else 'X'
        val thirdConsonant = firstName.split(" ").first().drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'
        val randomDigits = "01"
        return "$firstLetter$firstVowel$secondLetter$thirdLetter$year$month${day}$sexLetter$state$firstConsonant$secondConsonant$thirdConsonant$randomDigits"
    }
}

object TextFormatters {
    fun formatName(input: String): String {
        return input.trim().split("\\s+".toRegex()).joinToString(" ") {
            it.lowercase().replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() }
        }
    }
}