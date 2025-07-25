package org.example.appbbmges.ui.usuarios.registation.studentsform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PersonalInfoStep(
    data: StudentFormData,
    errors: FormErrors,
    onDataChange: (StudentFormData) -> Unit
) {
    var curpEdited by remember { mutableStateOf(false) }

    LaunchedEffect(data.firstName, data.lastNamePaternal, data.lastNameMaternal, data.birthDate, data.gender) {
        if (!curpEdited && data.firstName.isNotEmpty() && data.lastNamePaternal.isNotEmpty() &&
            data.birthDate.isNotEmpty() && data.gender.isNotEmpty() && data.curp.isEmpty()) {
            val generatedCurp = CURPGenerator.generate(
                data.firstName, data.lastNamePaternal, data.lastNameMaternal, data.birthDate, data.gender
            )
            onDataChange(data.copy(curp = generatedCurp))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomOutlinedTextField(
                value = data.firstName,
                onValueChange = { onDataChange(data.copy(firstName = TextFormatters.formatName(it))) },
                label = "Nombre(s)",
                placeholder = "Nombre(s)",
                modifier = Modifier.weight(1f),
                isError = errors.firstName != null,
                errorMessage = errors.firstName
            )
            CustomOutlinedTextField(
                value = data.lastNamePaternal,
                onValueChange = { onDataChange(data.copy(lastNamePaternal = TextFormatters.formatName(it))) },
                label = "Apellido Paterno",
                placeholder = "Apellido Paterno",
                modifier = Modifier.weight(1f),
                isError = errors.lastNamePaternal != null,
                errorMessage = errors.lastNamePaternal
            )
            CustomOutlinedTextField(
                value = data.lastNameMaternal,
                onValueChange = { onDataChange(data.copy(lastNameMaternal = TextFormatters.formatName(it))) },
                label = "Apellido Materno",
                placeholder = "Apellido Materno",
                modifier = Modifier.weight(1f),
                isError = errors.lastNameMaternal != null,
                errorMessage = errors.lastNameMaternal
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomDropdownField(
                value = data.gender,
                onValueChange = { onDataChange(data.copy(gender = it)) },
                label = "Género",
                options = FormConstants.genderOptions,
                placeholder = "Seleccione su género",
                modifier = Modifier.weight(1f)
            )
            DatePickerField(
                value = data.birthDate,
                onDateSelected = { onDataChange(data.copy(birthDate = it)) },
                label = "Fecha de Nacimiento",
                modifier = Modifier.weight(1f),
                isError = errors.birthDate != null,
                errorMessage = errors.birthDate
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomDropdownField(
                value = data.nationality,
                onValueChange = { onDataChange(data.copy(nationality = it)) },
                label = "Nacionalidad",
                options = FormConstants.nationalityOptions,
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = data.email,
                onValueChange = { onDataChange(data.copy(email = it.trim())) },
                label = "Email",
                placeholder = "ejemplo@correo.com",
                modifier = Modifier.weight(1f),
                isError = errors.email != null,
                errorMessage = errors.email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomCountryCodeDropdown(
                selectedCode = data.countryCode,
                onCodeSelected = { onDataChange(data.copy(countryCode = it)) },
                options = FormConstants.countryCodeOptions,
                modifier = Modifier.width(140.dp)
            )
            CustomOutlinedTextField(
                value = data.phone,
                onValueChange = {
                    val filtered = it.filter { char -> char.isDigit() }.take(10)
                    onDataChange(data.copy(phone = filtered))
                },
                label = "Teléfono",
                placeholder = "1234567890",
                modifier = Modifier.weight(1f),
                isError = errors.phone != null,
                errorMessage = errors.phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                maxLength = 10
            )
        }
        CustomOutlinedTextField(
            value = data.curp,
            onValueChange = {
                curpEdited = true
                onDataChange(data.copy(curp = it.uppercase().take(18)))
            },
            label = "CURP",
            placeholder = "Ej. ABCD123456HDFX01",
            modifier = Modifier.fillMaxWidth(),
            isError = errors.curp != null,
            errorMessage = errors.curp,
            maxLength = 18,
            trailingIcon = {
                IconButton(onClick = {
                    curpEdited = false
                    val generatedCurp = CURPGenerator.generate(
                        data.firstName, data.lastNamePaternal, data.lastNameMaternal,
                        data.birthDate, data.gender
                    )
                    onDataChange(data.copy(curp = generatedCurp))
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regenerar CURP")
                }
            }
        )
    }
}

@Composable
fun AddressInfoStep(
    data: StudentFormData,
    errors: FormErrors,
    onDataChange: (StudentFormData) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomOutlinedTextField(
            value = data.addressStreet,
            onValueChange = { onDataChange(data.copy(addressStreet = it)) },
            label = "Calle y número",
            placeholder = "Ej. Av. Siempre Viva 123",
            modifier = Modifier.weight(1f),
            isError = errors.addressStreet != null,
            errorMessage = errors.addressStreet
        )
        CustomOutlinedTextField(
            value = data.addressZip,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() }.take(5)
                onDataChange(data.copy(addressZip = filtered))
            },
            label = "Código Postal",
            placeholder = "Ej. 12345",
            modifier = Modifier.weight(1f),
            isError = errors.addressZip != null,
            errorMessage = errors.addressZip,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            maxLength = 5
        )
    }
}

@Composable
fun AdditionalInfoStep(
    data: StudentFormData,
    onDataChange: (StudentFormData) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomOutlinedTextField(
                value = data.parentFatherName,
                onValueChange = { onDataChange(data.copy(parentFatherName = TextFormatters.formatName(it))) },
                label = "Nombre del Padre",
                placeholder = "Opcional",
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = data.parentMotherName,
                onValueChange = { onDataChange(data.copy(parentMotherName = TextFormatters.formatName(it))) },
                label = "Nombre de la Madre",
                placeholder = "Opcional",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomOutlinedTextField(
                value = data.bloodType,
                onValueChange = { onDataChange(data.copy(bloodType = it)) },
                label = "Tipo de Sangre",
                placeholder = "Ej. O+",
                modifier = Modifier.weight(1f)
            )
            CustomOutlinedTextField(
                value = data.chronicDisease,
                onValueChange = { onDataChange(data.copy(chronicDisease = it)) },
                label = "Enfermedad Crónica",
                placeholder = "Opcional",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ConfirmationStep(
    data: StudentFormData,
    errors: FormErrors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Confirmación de Datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nombre(s): ${data.firstName}")
            if (data.lastNamePaternal.isNotEmpty()) Text("Apellido Paterno: ${data.lastNamePaternal}")
            if (data.lastNameMaternal.isNotEmpty()) Text("Apellido Materno: ${data.lastNameMaternal}")
            if (data.gender.isNotEmpty()) Text("Género: ${data.gender}")
            if (data.birthDate.isNotEmpty()) Text("Fecha de Nacimiento: ${data.birthDate}")
            if (data.nationality.isNotEmpty()) Text("Nacionalidad: ${data.nationality}")
            if (data.curp.isNotEmpty()) Text("CURP: ${data.curp}")
            if (data.phone.isNotEmpty()) Text("Teléfono: ${data.countryCode} ${data.phone}")
            if (data.email.isNotEmpty()) Text("Email: ${data.email}")
            if (data.addressStreet.isNotEmpty()) Text("Calle: ${data.addressStreet}")
            if (data.addressZip.isNotEmpty()) Text("Código Postal: ${data.addressZip}")
            if (data.parentFatherName.isNotEmpty()) Text("Nombre del Padre: ${data.parentFatherName}")
            if (data.parentMotherName.isNotEmpty()) Text("Nombre de la Madre: ${data.parentMotherName}")
            if (data.bloodType.isNotEmpty()) Text("Tipo de Sangre: ${data.bloodType}")
            if (data.chronicDisease.isNotEmpty()) Text("Enfermedad Crónica: ${data.chronicDisease}")
            Text("Estado: ${if (data.active) "Activo" else "Inactivo"}")
            if (errors.general != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errors.general,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
