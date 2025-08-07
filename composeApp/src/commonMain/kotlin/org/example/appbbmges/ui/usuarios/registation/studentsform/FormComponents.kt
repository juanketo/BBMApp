package org.example.appbbmges.ui.usuarios.registation.studentsform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.example.appbbmges.ui.usuarios.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    maxLength: Int? = null,
    onClick: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filteredValue = if (maxLength != null) newValue.take(maxLength) else newValue
            onValueChange(filteredValue)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .let { m ->
                if (onClick != null) m.clickable { onClick() } else m
            },
        singleLine = true,
        readOnly = readOnly,
        isError = isError,
        supportingText = {
            if (errorMessage != null) {
                Text(errorMessage, style = MaterialTheme.typography.bodySmall)
            }
        },
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.Primary,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text(label) },
            placeholder = { Text("dd/MM/yyyy") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false,
            isError = isError,
            supportingText = {
                if (errorMessage != null) {
                    Text(errorMessage, style = MaterialTheme.typography.bodySmall)
                }
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Seleccionar fecha",
                    tint = if (isError) MaterialTheme.colorScheme.error else AppColors.Primary,
                    modifier = Modifier.padding(12.dp)
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black.copy(alpha = 0.87f),
                disabledBorderColor = if (isError) MaterialTheme.colorScheme.error
                else Color.Gray.copy(alpha = 0.5f),
                disabledLabelColor = if (isError) MaterialTheme.colorScheme.error
                else Color.Black.copy(alpha = 0.87f),
                disabledPlaceholderColor = Color.Black.copy(alpha = 0.6f)
            )
        )
    }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let { onDateSelected(DateUtils.formatDateFromMillis(it)) }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccione una opción"
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCountryCodeDropdown(
    selectedCode: String,
    onCodeSelected: (String) -> Unit,
    options: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = options.find { it.first == selectedCode }?.let { "${it.first} ${it.second}" } ?: selectedCode,
            onValueChange = { },
            readOnly = true,
            label = { Text("País") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (code, country) ->
                DropdownMenuItem(
                    text = { Text("$code $country") },
                    onClick = {
                        onCodeSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FormProgressIndicator(
    currentStep: StudentFormStep,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = {
                when (currentStep) {
                    StudentFormStep.PERSONAL_INFO -> 0.25f
                    StudentFormStep.ADDRESS_INFO -> 0.50f
                    StudentFormStep.ADDITIONAL_INFO -> 0.75f
                    StudentFormStep.CONFIRMATION -> 1.0f
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = AppColors.Primary
        )
        Text(
            text = when (currentStep) {
                StudentFormStep.PERSONAL_INFO -> "Datos Personales (Paso 1 de 4)"
                StudentFormStep.ADDRESS_INFO -> "Dirección (Paso 2 de 4)"
                StudentFormStep.ADDITIONAL_INFO -> "Información Adicional (Paso 3 de 4)"
                StudentFormStep.CONFIRMATION -> "Confirmación (Paso 4 de 4)"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun FormNavigationButtons(
    currentStep: StudentFormStep,
    onCancel: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onCancel,
            modifier = Modifier.width(100.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1E88E5))
        ) {
            Text("Cancelar")
        }
        Spacer(modifier = Modifier.weight(1f))
        if (currentStep != StudentFormStep.PERSONAL_INFO) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.width(110.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary)
            ) {
                Text("Anterior")
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Button(
            onClick = if (currentStep == StudentFormStep.CONFIRMATION) onSubmit else onNext,
            modifier = Modifier.width(if (currentStep == StudentFormStep.CONFIRMATION) 130.dp else 110.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (currentStep == StudentFormStep.CONFIRMATION) "Registrar" else "Siguiente")
        }
    }
}