package org.example.appbbmges.ui.usuarios.registation

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

data class FranchiseeData(
    val franchiseId: Long,
    val firstName: String,
    val lastNamePaternal: String?,
    val lastNameMaternal: String?,
    val gender: String?,
    val birthDate: String?,
    val nationality: String?,
    val taxId: String?,
    val phone: String?,
    val email: String?,
    val addressStreet: String?,
    val addressZip: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val startDate: String?,
    val active: Boolean = true
)

enum class FranchiseeFormStep {
    PERSONAL_INFO,
    PROFESSIONAL_INFO,
    ADDRESS_INFO,
    CONTACT_INFO,
    CONFIRMATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFranquiciatarioScreen(
    onDismiss: () -> Unit,
    repository: Repository
) {
    var currentStep by remember { mutableStateOf(FranchiseeFormStep.PERSONAL_INFO) }

    var franchiseId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastNamePaternal by remember { mutableStateOf("") }
    var lastNameMaternal by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var taxId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var addressStreet by remember { mutableStateOf("") }
    var addressZip by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }

    var franchiseIdError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var addressZipError by remember { mutableStateOf<String?>(null) }
    var taxIdError by remember { mutableStateOf<String?>(null) }
    var emergencyContactPhoneError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    fun validatePersonalInfo(): Boolean {
        franchiseIdError = null
        firstNameError = null

        var isValid = true

        if (franchiseId.isEmpty()) {
            franchiseIdError = "El ID de franquicia es obligatorio"
            isValid = false
        } else if (franchiseId.toLongOrNull() == null || franchiseId.toLong() <= 0) {
            franchiseIdError = "El ID de franquicia debe ser un número positivo válido"
            isValid = false
        }

        if (firstName.isEmpty()) {
            firstNameError = "El nombre es obligatorio"
            isValid = false
        }

        return isValid
    }

    fun validateProfessionalInfo(): Boolean {
        taxIdError = null

        var isValid = true

        if (taxId.isNotEmpty() && taxId.length !in 12..13) {
            taxIdError = "RFC debe tener 12-13 caracteres"
            isValid = false
        }

        return isValid
    }

    fun validateAddressInfo(): Boolean {
        addressZipError = null

        var isValid = true

        if (addressZip.isNotEmpty() && (addressZip.length != 5 || !addressZip.all { it.isDigit() })) {
            addressZipError = "El código postal debe ser de 5 dígitos"
            isValid = false
        }

        return isValid
    }

    fun validateContactInfo(): Boolean {
        phoneError = null
        emailError = null
        emergencyContactPhoneError = null

        var isValid = true

        if (phone.isNotEmpty() && !phone.all { it.isDigit() }) {
            phoneError = "El teléfono solo debe contener dígitos"
            isValid = false
        }

        if (email.isNotEmpty() && (!email.contains("@") || !email.contains("."))) {
            emailError = "Formato de email inválido"
            isValid = false
        }

        if (emergencyContactPhone.isNotEmpty() && !emergencyContactPhone.all { it.isDigit() }) {
            emergencyContactPhoneError = "El teléfono de emergencia solo debe contener dígitos"
            isValid = false
        }

        return isValid
    }

    fun validateForm(): Boolean {
        return validatePersonalInfo() &&
                validateProfessionalInfo() &&
                validateAddressInfo() &&
                validateContactInfo()
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
            Box(modifier = Modifier.fillMaxSize()) {
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
                        text = "Registro de Franquiciatario",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.TextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    LinearProgressIndicator(
                        progress = {
                            when (currentStep) {
                                FranchiseeFormStep.PERSONAL_INFO -> 0.2f
                                FranchiseeFormStep.PROFESSIONAL_INFO -> 0.4f
                                FranchiseeFormStep.ADDRESS_INFO -> 0.6f
                                FranchiseeFormStep.CONTACT_INFO -> 0.8f
                                FranchiseeFormStep.CONFIRMATION -> 1.0f
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = AppColors.Primary
                    )

                    Text(
                        text = when (currentStep) {
                            FranchiseeFormStep.PERSONAL_INFO -> "Datos Personales"
                            FranchiseeFormStep.PROFESSIONAL_INFO -> "Información Profesional"
                            FranchiseeFormStep.ADDRESS_INFO -> "Dirección"
                            FranchiseeFormStep.CONTACT_INFO -> "Contacto"
                            FranchiseeFormStep.CONFIRMATION -> "Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    when (currentStep) {
                        FranchiseeFormStep.PERSONAL_INFO -> {
                            OutlinedTextField(
                                value = franchiseId,
                                onValueChange = { franchiseId = it; franchiseIdError = null },
                                label = { Text("ID de Franquicia") },
                                placeholder = { Text("Ingrese ID") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = franchiseIdError != null,
                                supportingText = { franchiseIdError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it; firstNameError = null },
                                label = { Text("Nombre(s)") },
                                placeholder = { Text("Ingrese su(s) nombre(s)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = firstNameError != null,
                                supportingText = { firstNameError?.let { Text(it) } },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = lastNamePaternal,
                                    onValueChange = { lastNamePaternal = it },
                                    label = { Text("Apellido Paterno") },
                                    placeholder = { Text("Ingrese su apellido paterno") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                OutlinedTextField(
                                    value = lastNameMaternal,
                                    onValueChange = { lastNameMaternal = it },
                                    label = { Text("Apellido Materno") },
                                    placeholder = { Text("Ingrese su apellido materno") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = gender,
                                    onValueChange = { gender = it },
                                    label = { Text("Género") },
                                    placeholder = { Text("Seleccione su género") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                OutlinedTextField(
                                    value = birthDate,
                                    onValueChange = { birthDate = it },
                                    label = { Text("Fecha de Nacimiento") },
                                    placeholder = { Text("dd/mm/aaaa") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { /* Selector de fecha */ }) {
                                            Text("📅")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = nationality,
                                onValueChange = { nationality = it },
                                label = { Text("Nacionalidad") },
                                placeholder = { Text("Ingrese su nacionalidad") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )
                        }

                        FranchiseeFormStep.PROFESSIONAL_INFO -> {
                            OutlinedTextField(
                                value = taxId,
                                onValueChange = { taxId = it; taxIdError = null },
                                label = { Text("RFC") },
                                placeholder = { Text("Ingrese su RFC") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = taxIdError != null,
                                supportingText = { taxIdError?.let { Text(it) } },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("Fecha de Inicio") },
                                placeholder = { Text("dd/mm/aaaa") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = {
                                    IconButton(onClick = { /* Selector de fecha */ }) {
                                        Text("📅")
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Estado",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = active,
                                        onClick = { active = true },
                                        colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary)
                                    )
                                    Text("Activo", modifier = Modifier.padding(start = 4.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = !active,
                                        onClick = { active = false },
                                        colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary)
                                    )
                                    Text("Inactivo", modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }

                        FranchiseeFormStep.ADDRESS_INFO -> {
                            OutlinedTextField(
                                value = addressStreet,
                                onValueChange = { addressStreet = it },
                                label = { Text("Calle y número") },
                                placeholder = { Text("Ingrese su dirección") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = addressZip,
                                onValueChange = { addressZip = it; addressZipError = null },
                                label = { Text("Código Postal") },
                                placeholder = { Text("Ingrese CP") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = addressZipError != null,
                                supportingText = { addressZipError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }

                        FranchiseeFormStep.CONTACT_INFO -> {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it; phoneError = null },
                                label = { Text("Teléfono") },
                                placeholder = { Text("Ingrese su teléfono") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = phoneError != null,
                                supportingText = { phoneError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; emailError = null },
                                label = { Text("Email") },
                                placeholder = { Text("Ingrese su email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = emailError != null,
                                supportingText = { emailError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = emergencyContactName,
                                onValueChange = { emergencyContactName = it },
                                label = { Text("Nombre Contacto Emergencia") },
                                placeholder = { Text("Ingrese nombre completo") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = emergencyContactPhone,
                                onValueChange = { emergencyContactPhone = it; emergencyContactPhoneError = null },
                                label = { Text("Teléfono Emergencia") },
                                placeholder = { Text("Ingrese teléfono") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = emergencyContactPhoneError != null,
                                supportingText = { emergencyContactPhoneError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }

                        FranchiseeFormStep.CONFIRMATION -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Datos Personales",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("ID de Franquicia: $franchiseId")
                                    Text("Nombre(s): $firstName")
                                    if (lastNamePaternal.isNotEmpty()) Text("Apellido Paterno: $lastNamePaternal")
                                    if (lastNameMaternal.isNotEmpty()) Text("Apellido Materno: $lastNameMaternal")
                                    if (gender.isNotEmpty()) Text("Género: $gender")
                                    if (birthDate.isNotEmpty()) Text("Fecha de Nacimiento: $birthDate")
                                    if (nationality.isNotEmpty()) Text("Nacionalidad: $nationality")

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Información Profesional",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (taxId.isNotEmpty()) Text("RFC: $taxId")
                                    if (startDate.isNotEmpty()) Text("Fecha de Inicio: $startDate")
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
                                        text = "Contacto",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (phone.isNotEmpty()) Text("Teléfono: $phone")
                                    if (email.isNotEmpty()) Text("Email: $email")
                                    if (emergencyContactName.isNotEmpty()) Text("Nombre Contacto Emergencia: $emergencyContactName")
                                    if (emergencyContactPhone.isNotEmpty()) Text("Teléfono Emergencia: $emergencyContactPhone")
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

                        if (currentStep != FranchiseeFormStep.PERSONAL_INFO) {
                            OutlinedButton(
                                onClick = {
                                    currentStep = when (currentStep) {
                                        FranchiseeFormStep.PROFESSIONAL_INFO -> FranchiseeFormStep.PERSONAL_INFO
                                        FranchiseeFormStep.ADDRESS_INFO -> FranchiseeFormStep.PROFESSIONAL_INFO
                                        FranchiseeFormStep.CONTACT_INFO -> FranchiseeFormStep.ADDRESS_INFO
                                        FranchiseeFormStep.CONFIRMATION -> FranchiseeFormStep.CONTACT_INFO
                                        else -> FranchiseeFormStep.PERSONAL_INFO
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
                                    FranchiseeFormStep.PERSONAL_INFO -> {
                                        if (validatePersonalInfo()) currentStep = FranchiseeFormStep.PROFESSIONAL_INFO
                                    }
                                    FranchiseeFormStep.PROFESSIONAL_INFO -> {
                                        if (validateProfessionalInfo()) currentStep = FranchiseeFormStep.ADDRESS_INFO
                                    }
                                    FranchiseeFormStep.ADDRESS_INFO -> {
                                        if (validateAddressInfo()) currentStep = FranchiseeFormStep.CONTACT_INFO
                                    }
                                    FranchiseeFormStep.CONTACT_INFO -> {
                                        if (validateContactInfo()) currentStep = FranchiseeFormStep.CONFIRMATION
                                    }
                                    FranchiseeFormStep.CONFIRMATION -> {
                                        if (validateForm()) {
                                            repository.insertFranchisee(
                                                franchiseId = franchiseId.toLong(),
                                                firstName = firstName,
                                                lastNamePaternal = lastNamePaternal.takeIf { it.isNotEmpty() },
                                                lastNameMaternal = lastNameMaternal.takeIf { it.isNotEmpty() },
                                                gender = gender.takeIf { it.isNotEmpty() },
                                                birthDate = birthDate.takeIf { it.isNotEmpty() },
                                                nationality = nationality.takeIf { it.isNotEmpty() },
                                                taxId = taxId.takeIf { it.isNotEmpty() },
                                                phone = phone.takeIf { it.isNotEmpty() },
                                                email = email.takeIf { it.isNotEmpty() },
                                                addressStreet = addressStreet.takeIf { it.isNotEmpty() },
                                                addressZip = addressZip.takeIf { it.isNotEmpty() },
                                                emergencyContactName = emergencyContactName.takeIf { it.isNotEmpty() },
                                                emergencyContactPhone = emergencyContactPhone.takeIf { it.isNotEmpty() },
                                                startDate = startDate.takeIf { it.isNotEmpty() },
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
                                if (currentStep == FranchiseeFormStep.CONFIRMATION) 130.dp else 110.dp
                            ),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (currentStep == FranchiseeFormStep.CONFIRMATION) "Registrar" else "Siguiente"
                            )
                        }
                    }
                }
            }
        }
    }
}