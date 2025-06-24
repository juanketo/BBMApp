package org.example.appbbmges.ui.usuarios.registation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import org.example.appbbmges.ui.dashboard.clickable

data class StudentData(
    val franchiseId: Long,
    val firstName: String,
    val lastNamePaternal: String?,
    val lastNameMaternal: String?,
    val gender: String?,
    val birthDate: String?,
    val nationality: String?,
    val curp: String?,
    val phone: String?,
    val email: String?,
    val addressStreet: String?,
    val addressZip: String?,
    val parentFatherName: String?,
    val parentMotherName: String?,
    val bloodType: String?,
    val chronicDisease: String?,
    val active: Boolean = true
)

enum class StudentFormStep {
    PERSONAL_INFO,
    IDENTITY_INFO,
    ADDRESS_INFO,
    ADDITIONAL_INFO,
    CONFIRMATION
}

fun calculateAge(birthDateString: String): Double? {
    if (birthDateString.length != 10 || !birthDateString.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
        return null
    }

    val parts = birthDateString.split("/")
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null

    if (day < 1 || day > 31 || month < 1 || month > 12) return null

    val currentYear = 2024
    val currentMonth = 12
    val currentDay = 1

    var age = currentYear - year

    if (currentMonth < month || (currentMonth == month && currentDay < day)) {
        age--
    }

    val monthDiff = if (currentMonth >= month) {
        currentMonth - month
    } else {
        12 + currentMonth - month
    }

    return age + (monthDiff / 12.0)
}

fun generateCURP(
    firstName: String,
    lastNamePaternal: String,
    lastNameMaternal: String,
    birthDate: String,
    gender: String
): String {
    if (firstName.isEmpty() || lastNamePaternal.isEmpty() || birthDate.length != 10) {
        return ""
    }

    val parts = birthDate.split("/")
    if (parts.size != 3) return ""

    val day = parts[0].padStart(2, '0')
    val month = parts[1].padStart(2, '0')
    val year = parts[2].takeLast(2)

    val vowels = "AEIOU"
    val consonants = "BCDFGHJKLMNPQRSTVWXYZ"

    val firstLetter = lastNamePaternal.first().uppercaseChar()

    val firstVowel = lastNamePaternal.drop(1).firstOrNull { it.uppercaseChar() in vowels }?.uppercaseChar() ?: 'X'

    val secondLetter = if (lastNameMaternal.isNotEmpty()) {
        lastNameMaternal.first().uppercaseChar()
    } else {
        'X'
    }

    val thirdLetter = firstName.first().uppercaseChar()

    // Sexo
    val sexLetter = when (gender.lowercase()) {
        "masculino", "hombre", "m" -> "H"
        "femenino", "mujer", "f" -> "M"
        else -> "H"
    }

    val state = "DF"

    val firstConsonant = lastNamePaternal.drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'

    val secondConsonant = if (lastNameMaternal.isNotEmpty()) {
        lastNameMaternal.drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'
    } else {
        'X'
    }

    val thirdConsonant = firstName.drop(1).firstOrNull { it.uppercaseChar() in consonants }?.uppercaseChar() ?: 'X'

    val randomDigits = "01"

    return "$firstLetter$firstVowel$secondLetter$thirdLetter$year$month${day}$sexLetter$state$firstConsonant$secondConsonant$thirdConsonant$randomDigits"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

fun formatDateFromMillis(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlumnoScreen(
    onDismiss: () -> Unit,
    repository: Repository
) {
    var currentStep by remember { mutableStateOf(StudentFormStep.PERSONAL_INFO) }

    var franchiseId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastNamePaternal by remember { mutableStateOf("") }
    var lastNameMaternal by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("Mexicana") }
    var curp by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var addressStreet by remember { mutableStateOf("") }
    var addressZip by remember { mutableStateOf("") }
    var parentFatherName by remember { mutableStateOf("") }
    var parentMotherName by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var chronicDisease by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }

    var genderExpanded by remember { mutableStateOf(false) }
    var nationalityExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    val genderOptions = listOf("Masculino", "Femenino")
    val nationalityOptions = listOf(
        "Mexicana", "Estadounidense", "Canadiense", "Española", "Francesa",
        "Alemana", "Italiana", "Británica", "Argentina", "Brasileña",
        "Colombiana", "Peruana", "Chilena", "Venezolana", "Otra"
    )

    var franchiseIdError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNamePaternalError by remember { mutableStateOf<String?>(null) }
    var lastNameMaternalError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var addressStreetError by remember { mutableStateOf<String?>(null) }
    var addressZipError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(firstName, lastNamePaternal, lastNameMaternal, birthDate, gender) {
        if (firstName.isNotEmpty() && lastNamePaternal.isNotEmpty() && birthDate.isNotEmpty() && gender.isNotEmpty()) {
            curp = generateCURP(firstName, lastNamePaternal, lastNameMaternal, birthDate, gender)
        }
    }

    fun validatePersonalInfo(): Boolean {
        firstNameError = null
        lastNamePaternalError = null
        lastNameMaternalError = null
        birthDateError = null
        phoneError = null
        emailError = null

        var isValid = true

        // Validar nombre
        if (firstName.isEmpty()) {
            firstNameError = "El nombre es obligatorio"
            isValid = false
        } else if (firstName.length !in 2..50) {
            firstNameError = "El nombre debe tener entre 2 y 50 caracteres"
            isValid = false
        } else if (firstName.any { it.isDigit() }) {
            firstNameError = "El nombre no puede contener números"
            isValid = false
        }

        if (lastNamePaternal.isNotEmpty()) {
            if (lastNamePaternal.length !in 2..50) {
                lastNamePaternalError = "El apellido paterno debe tener entre 2 y 50 caracteres"
                isValid = false
            } else if (lastNamePaternal.any { it.isDigit() }) {
                lastNamePaternalError = "El apellido paterno no puede contener números"
                isValid = false
            }
        }

        if (lastNameMaternal.isNotEmpty()) {
            if (lastNameMaternal.length !in 2..50) {
                lastNameMaternalError = "El apellido materno debe tener entre 2 y 50 caracteres"
                isValid = false
            } else if (lastNameMaternal.any { it.isDigit() }) {
                lastNameMaternalError = "El apellido materno no puede contener números"
                isValid = false
            }
        }

        if (birthDate.isNotEmpty()) {
            val age = calculateAge(birthDate)
            if (age == null) {
                birthDateError = "Formato de fecha inválido (use dd/MM/yyyy)"
                isValid = false
            } else if (age < 1.5 || age > 18) {
                birthDateError = "La edad debe estar entre 1.5 y 18 años"
                isValid = false
            }
        }

        // Validar teléfono
        if (phone.isNotEmpty()) {
            if (phone.length !in 10..15) {
                phoneError = "El teléfono debe tener entre 10 y 15 dígitos"
                isValid = false
            } else if (!phone.all { it.isDigit() }) {
                phoneError = "El teléfono solo debe contener dígitos"
                isValid = false
            }
        }

        if (email.isNotEmpty() && (!email.contains("@") || !email.contains("."))) {
            emailError = "El email no tiene un formato válido"
            isValid = false
        }

        return isValid
    }

    fun validateIdentityInfo(): Boolean {
        franchiseIdError = null

        var isValid = true

        if (franchiseId.isEmpty()) {
            franchiseIdError = "El ID de franquicia es obligatorio"
            isValid = false
        } else if (franchiseId.toLongOrNull() == null || franchiseId.toLong() <= 0) {
            franchiseIdError = "El ID de franquicia debe ser un número positivo válido"
            isValid = false
        }

        return isValid
    }

    fun validateAddressInfo(): Boolean {
        addressStreetError = null
        addressZipError = null

        var isValid = true

        if (addressStreet.isNotEmpty() && addressStreet.length !in 5..100) {
            addressStreetError = "La calle debe tener entre 5 y 100 caracteres"
            isValid = false
        }

        if (addressZip.isNotEmpty() && (addressZip.length != 5 || !addressZip.all { it.isDigit() })) {
            addressZipError = "El código postal debe ser de 5 dígitos"
            isValid = false
        }

        return isValid
    }

    fun validateForm(): Boolean {
        return validatePersonalInfo() && validateIdentityInfo() && validateAddressInfo()
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
                        text = "Registro de Alumno",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.TextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    LinearProgressIndicator(
                        progress = {
                            when (currentStep) {
                                StudentFormStep.PERSONAL_INFO -> 0.2f
                                StudentFormStep.IDENTITY_INFO -> 0.4f
                                StudentFormStep.ADDRESS_INFO -> 0.6f
                                StudentFormStep.ADDITIONAL_INFO -> 0.8f
                                StudentFormStep.CONFIRMATION -> 1.0f
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = AppColors.Primary,
                    )

                    Text(
                        text = when (currentStep) {
                            StudentFormStep.PERSONAL_INFO -> "Datos Personales (Paso 1 de 5)"
                            StudentFormStep.IDENTITY_INFO -> "Información de Identidad (Paso 2 de 5)"
                            StudentFormStep.ADDRESS_INFO -> "Dirección (Paso 3 de 5)"
                            StudentFormStep.ADDITIONAL_INFO -> "Información Adicional (Paso 4 de 5)"
                            StudentFormStep.CONFIRMATION -> "Confirmación (Paso 5 de 5)"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    when (currentStep) {
                        StudentFormStep.PERSONAL_INFO -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it; firstNameError = null },
                                    label = { Text("Nombre(s)") },
                                    placeholder = { Text("Nombre(s)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = firstNameError != null,
                                    supportingText = { if (firstNameError != null) Text(firstNameError!!, style = MaterialTheme.typography.bodySmall) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )

                                OutlinedTextField(
                                    value = lastNamePaternal,
                                    onValueChange = { lastNamePaternal = it; lastNamePaternalError = null },
                                    label = { Text("Apellido Paterno") },
                                    placeholder = { Text("Apellido Paterno") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = lastNamePaternalError != null,
                                    supportingText = { if (lastNamePaternalError != null) Text(lastNamePaternalError!!, style = MaterialTheme.typography.bodySmall) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )

                                OutlinedTextField(
                                    value = lastNameMaternal,
                                    onValueChange = { lastNameMaternal = it; lastNameMaternalError = null },
                                    label = { Text("Apellido Materno") },
                                    placeholder = { Text("Apellido Materno") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = lastNameMaternalError != null,
                                    supportingText = { if (lastNameMaternalError != null) Text(lastNameMaternalError!!, style = MaterialTheme.typography.bodySmall) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                ExposedDropdownMenuBox(
                                    expanded = genderExpanded,
                                    onExpandedChange = { genderExpanded = !genderExpanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = gender,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Género") },
                                        placeholder = { Text("Seleccione su género") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.Primary,
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    )

                                    ExposedDropdownMenu(
                                        expanded = genderExpanded,
                                        onDismissRequest = { genderExpanded = false }
                                    ) {
                                        genderOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    gender = option
                                                    genderExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = birthDate,
                                    onValueChange = { /* No permitir edición manual */ },
                                    label = { Text("Fecha de Nacimiento") },
                                    placeholder = { Text("dd/mm/aaaa") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showDatePicker = true },
                                    singleLine = true,
                                    readOnly = true,
                                    isError = birthDateError != null,
                                    supportingText = {
                                        if (birthDateError != null)
                                            Text(birthDateError!!, style = MaterialTheme.typography.bodySmall)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Dropdown para Nacionalidad
                                ExposedDropdownMenuBox(
                                    expanded = nationalityExpanded,
                                    onExpandedChange = { nationalityExpanded = !nationalityExpanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = nationality,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Nacionalidad") },
                                        placeholder = { Text("Nacionalidad") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = nationalityExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.Primary,
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    )

                                    ExposedDropdownMenu(
                                        expanded = nationalityExpanded,
                                        onDismissRequest = { nationalityExpanded = false }
                                    ) {
                                        nationalityOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    nationality = option
                                                    nationalityExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it; emailError = null },
                                    label = { Text("Email") },
                                    placeholder = { Text("Email") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = emailError != null,
                                    supportingText = { if (emailError != null) Text(emailError!!, style = MaterialTheme.typography.bodySmall) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it; phoneError = null },
                                label = { Text("Teléfono") },
                                placeholder = { Text("Teléfono") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = phoneError != null,
                                supportingText = { if (phoneError != null) Text(phoneError!!, style = MaterialTheme.typography.bodySmall) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }

                        StudentFormStep.IDENTITY_INFO -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = franchiseId,
                                    onValueChange = { franchiseId = it; franchiseIdError = null },
                                    label = { Text("ID de Franquicia") },
                                    placeholder = { Text("ID de Franquicia") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = franchiseIdError != null,
                                    supportingText = { if (franchiseIdError != null) Text(franchiseIdError!!, style = MaterialTheme.typography.bodySmall) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )

                                OutlinedTextField(
                                    value = curp,
                                    onValueChange = { /* Solo lectura, se genera automáticamente */ },
                                    label = { Text("CURP (Auto-generado)") },
                                    placeholder = { Text("Se genera automáticamente") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    readOnly = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        disabledBorderColor = Color.Gray.copy(alpha = 0.3f),
                                        disabledTextColor = Color.Gray
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Estado",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = active,
                                        onClick = { active = true },
                                        colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary)
                                    )
                                    Text("Activo", modifier = Modifier.padding(start = 4.dp))
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = !active,
                                        onClick = { active = false },
                                        colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary)
                                    )
                                    Text("Inactivo", modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }

                        StudentFormStep.ADDRESS_INFO -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = addressStreet,
                                    onValueChange = { addressStreet = it; addressStreetError = null },
                                    label = { Text("Calle y número") },
                                    placeholder = { Text("Calle y número") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = addressStreetError != null,
                                    supportingText = { if (addressStreetError != null) Text(addressStreetError!!, style = MaterialTheme.typography.bodySmall) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )

                                OutlinedTextField(
                                    value = addressZip,
                                    onValueChange = { addressZip = it; addressZipError = null },
                                    label = { Text("Código Postal") },
                                    placeholder = { Text("Código Postal") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    isError = addressZipError != null,
                                    supportingText = { if (addressZipError != null) Text(addressZipError!!, style = MaterialTheme.typography.bodySmall) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }

                        StudentFormStep.ADDITIONAL_INFO -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = parentFatherName,
                                    onValueChange = { parentFatherName = it },
                                    label = { Text("Nombre del Padre") },
                                    placeholder = { Text("Opcional") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                OutlinedTextField(
                                    value = parentMotherName,
                                    onValueChange = { parentMotherName = it },
                                    label = { Text("Nombre de la Madre") },
                                    placeholder = { Text("Opcional") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = bloodType,
                                    onValueChange = { bloodType = it },
                                    label = { Text("Tipo de Sangre") },
                                    placeholder = { Text("Opcional") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                OutlinedTextField(
                                    value = chronicDisease,
                                    onValueChange = { chronicDisease = it },
                                    label = { Text("Enfermedad Crónica") },
                                    placeholder = { Text("Opcional") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }

                        StudentFormStep.CONFIRMATION -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Datos Personales",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text("Nombre(s): $firstName")
                                    if (lastNamePaternal.isNotEmpty()) Text("Apellido Paterno: $lastNamePaternal")
                                    if (lastNameMaternal.isNotEmpty()) Text("Apellido Materno: $lastNameMaternal")
                                    if (gender.isNotEmpty()) Text("Género: $gender")
                                    if (birthDate.isNotEmpty()) Text("Fecha de nacimiento: $birthDate")
                                    if (nationality.isNotEmpty()) Text("Nacionalidad: $nationality")
                                    if (email.isNotEmpty()) Text("Email: $email")
                                    if (phone.isNotEmpty()) Text("Teléfono: $phone")

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Información de Identidad",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text("ID de Franquicia: $franchiseId")
                                    if (curp.isNotEmpty()) Text("CURP: $curp")
                                    Text("Estado: ${if (active) "Activo" else "Inactivo"}")

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Dirección",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (addressStreet.isNotEmpty()) Text("Calle: $addressStreet")
                                    if (addressZip.isNotEmpty()) Text("Código Postal: $addressZip")

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Información Adicional",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (parentFatherName.isNotEmpty()) Text("Nombre del Padre: $parentFatherName")
                                    if (parentMotherName.isNotEmpty()) Text("Nombre de la Madre: $parentMotherName")
                                    if (bloodType.isNotEmpty()) Text("Tipo de Sangre: $bloodType")
                                    if (chronicDisease.isNotEmpty()) Text("Enfermedad Crónica: $chronicDisease")
                                }
                            }

                            if (formError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
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

                        Spacer(modifier = Modifier.weight(1f))

                        if (currentStep != StudentFormStep.PERSONAL_INFO) {
                            OutlinedButton(
                                onClick = {
                                    currentStep = when (currentStep) {
                                        StudentFormStep.IDENTITY_INFO -> StudentFormStep.PERSONAL_INFO
                                        StudentFormStep.ADDRESS_INFO -> StudentFormStep.IDENTITY_INFO
                                        StudentFormStep.ADDITIONAL_INFO -> StudentFormStep.ADDRESS_INFO
                                        StudentFormStep.CONFIRMATION -> StudentFormStep.ADDITIONAL_INFO
                                        else -> StudentFormStep.PERSONAL_INFO
                                    }
                                },
                                modifier = Modifier.width(110.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary)
                            ) {
                                Text("Anterior")
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Button(
                            onClick = {
                                when (currentStep) {
                                    StudentFormStep.PERSONAL_INFO -> {
                                        if (validatePersonalInfo()) {
                                            currentStep = StudentFormStep.IDENTITY_INFO
                                        }
                                    }
                                    StudentFormStep.IDENTITY_INFO -> {
                                        if (validateIdentityInfo()) {
                                            currentStep = StudentFormStep.ADDRESS_INFO
                                        }
                                    }
                                    StudentFormStep.ADDRESS_INFO -> {
                                        if (validateAddressInfo()) {
                                            currentStep = StudentFormStep.ADDITIONAL_INFO
                                        }
                                    }
                                    StudentFormStep.ADDITIONAL_INFO -> {
                                        currentStep = StudentFormStep.CONFIRMATION
                                    }
                                    StudentFormStep.CONFIRMATION -> {
                                        if (validateForm()) {
                                            repository.insertStudent(
                                                franchiseId = franchiseId.toLong(),
                                                firstName = firstName,
                                                lastNamePaternal = lastNamePaternal.takeIf { it.isNotEmpty() },
                                                lastNameMaternal = lastNameMaternal.takeIf { it.isNotEmpty() },
                                                gender = gender.takeIf { it.isNotEmpty() },
                                                birthDate = birthDate.takeIf { it.isNotEmpty() },
                                                nationality = nationality.takeIf { it.isNotEmpty() },
                                                curp = curp.takeIf { it.isNotEmpty() },
                                                phone = phone.takeIf { it.isNotEmpty() },
                                                email = email.takeIf { it.isNotEmpty() },
                                                addressStreet = addressStreet.takeIf { it.isNotEmpty() },
                                                addressZip = addressZip.takeIf { it.isNotEmpty() },
                                                parentFatherFirstName = parentFatherName.takeIf { it.isNotEmpty() },
                                                parentFatherLastNamePaternal = null,
                                                parentFatherLastNameMaternal = null,
                                                parentMotherFirstName = parentMotherName.takeIf { it.isNotEmpty() },
                                                parentMotherLastNamePaternal = null,
                                                parentMotherLastNameMaternal = null,
                                                bloodType = bloodType.takeIf { it.isNotEmpty() },
                                                chronicDisease = chronicDisease.takeIf { it.isNotEmpty() },
                                                active = if (active) 1L else 0L
                                            )
                                            onDismiss()
                                        } else {
                                            formError = "Por favor revise el formulario. Hay campos obligatorios sin completar."
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.width(
                                if (currentStep == StudentFormStep.CONFIRMATION) 130.dp else 110.dp
                            ),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (currentStep == StudentFormStep.CONFIRMATION) "Registrar" else "Siguiente"
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let {
                    birthDate = formatDateFromMillis(it)
                    birthDateError = null
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }
}