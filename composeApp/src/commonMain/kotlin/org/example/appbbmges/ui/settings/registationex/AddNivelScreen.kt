package org.example.appbbmges.ui.settings.registationex

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.jetbrains.compose.resources.painterResource

data class NivelData(
    val new_level: String,
    val level_number: String?
)

enum class NivelFormStep {
    FORM,
    CONFIRMATION
}

sealed class NivelFormState {
    object Idle : NivelFormState()
    object Loading : NivelFormState()
    data class Error(val message: String) : NivelFormState()
    object Success : NivelFormState()
}

data class NivelValidationResult(
    val isValid: Boolean,
    val newLevelError: String? = null,
    val levelNumberError: String? = null
)

class NivelValidator {
    companion object {
        fun validateNivel(newLevel: String, levelNumber: String?): NivelValidationResult {
            val newLevelError = when {
                newLevel.isEmpty() -> "El nombre del nivel es obligatorio"
                newLevel.length < 4 -> "El nombre del nivel debe tener al menos 4 caracteres"
                newLevel.length > 50 -> "El nombre del nivel no puede exceder 50 caracteres"
                newLevel.contains(" ") -> "El nombre del nivel no puede contener espacios"
                !newLevel.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+$")) -> "El nombre solo puede contener letras (sin números ni espacios)"
                !newLevel.first().isUpperCase() -> "El nombre debe iniciar con mayúscula"
                !newLevel.drop(1).all { it.isLowerCase() || it in "áéíóúñ" } -> "Después de la primera letra, solo se permiten minúsculas"
                else -> null
            }

            val levelNumberError = when {
                levelNumber != null && levelNumber.toIntOrNull() == null -> "El número de nivel debe ser válido"
                levelNumber != null && (levelNumber.toIntOrNull() ?: 0) !in 1..10 -> "El número debe estar entre 1 y 10"
                else -> null
            }

            return NivelValidationResult(
                isValid = newLevelError == null && levelNumberError == null,
                newLevelError = newLevelError,
                levelNumberError = levelNumberError
            )
        }
    }
}

fun toRomanNumeral(number: Int?): String {
    if (number == null || number <= 0) return ""
    val romanValues = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")
    return if (number <= romanValues.size) romanValues[number - 1] else number.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNivelScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // Estados del formulario
    var currentStep by remember { mutableStateOf(NivelFormStep.FORM) }
    var new_level by remember { mutableStateOf("") }
    var level_number by remember { mutableStateOf<String?>(null) }
    var formState by remember { mutableStateOf<NivelFormState>(NivelFormState.Idle) }
    var validationResult by remember { mutableStateOf(NivelValidationResult(true)) }
    var expanded by remember { mutableStateOf(false) }

    val levelOptions = (1..10).map { it.toString() }

    // Función de validación
    fun validateForm(): Boolean {
        val validation = NivelValidator.validateNivel(new_level, level_number)
        validationResult = validation
        return validation.isValid
    }

    // Función para proceder al siguiente paso
    fun proceedToNext() {
        when (currentStep) {
            NivelFormStep.FORM -> {
                if (validateForm()) {
                    currentStep = NivelFormStep.CONFIRMATION
                }
            }
            NivelFormStep.CONFIRMATION -> {
                formState = NivelFormState.Loading
                try {
                    val levelName = buildLevelName(new_level, level_number)
                    repository.insertLevel(levelName)
                    formState = NivelFormState.Success
                    onDismiss()
                } catch (e: Exception) {
                    formState = NivelFormState.Error("Error al registrar el nivel: ${e.message}")
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 400.dp, max = 500.dp)
                .heightIn(max = 700.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                BackgroundLogo()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    FormHeader(currentStep)

                    Spacer(modifier = Modifier.height(12.dp))

                    when (currentStep) {
                        NivelFormStep.FORM -> {
                            FormContent(
                                newLevel = new_level,
                                levelNumber = level_number,
                                validationResult = validationResult,
                                expanded = expanded,
                                levelOptions = levelOptions,
                                formState = formState,
                                onNewLevelChange = {
                                    new_level = it
                                    if (validationResult.newLevelError != null) {
                                        validationResult = validationResult.copy(newLevelError = null)
                                    }
                                },
                                onLevelNumberChange = {
                                    level_number = it
                                    if (validationResult.levelNumberError != null) {
                                        validationResult = validationResult.copy(levelNumberError = null)
                                    }
                                },
                                onExpandedChange = { expanded = it },
                                onProceedToNext = { proceedToNext() },
                                focusManager = focusManager
                            )
                        }
                        NivelFormStep.CONFIRMATION -> {
                            ConfirmationContent(
                                newLevel = new_level,
                                levelNumber = level_number,
                                formState = formState
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationButtons(
                        currentStep = currentStep,
                        formState = formState,
                        onDismiss = onDismiss,
                        onPrevious = { currentStep = NivelFormStep.FORM },
                        onNext = { proceedToNext() }
                    )
                }
            }
        }
    }
}

// Función helper para construir el nombre del nivel
private fun buildLevelName(name: String, number: String?): String {
    return if (number != null) {
        "$name ${toRomanNumeral(number.toInt())}"
    } else {
        name
    }.trim()
}

@Composable
private fun BackgroundLogo() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.logoSystem),
            contentDescription = "Logo de fondo",
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                .semantics {
                    contentDescription = "Logo de fondo de la aplicación"
                },
            alpha = 0.08f,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun FormHeader(currentStep: NivelFormStep) {
    // Animación suave para la barra de progreso
    val animatedProgress by animateFloatAsState(
        targetValue = when (currentStep) {
            NivelFormStep.FORM -> 0.5f
            NivelFormStep.CONFIRMATION -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "progress_animation"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Registro de Nivel",
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextColor,
            fontWeight = FontWeight.Bold
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.Primary
        )

        Text(
            text = when (currentStep) {
                NivelFormStep.FORM -> "Paso 1: Información del Nivel"
                NivelFormStep.CONFIRMATION -> "Paso 2: Confirmación"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    newLevel: String,
    levelNumber: String?,
    validationResult: NivelValidationResult,
    expanded: Boolean,
    levelOptions: List<String>,
    formState: NivelFormState,
    onNewLevelChange: (String) -> Unit,
    onLevelNumberChange: (String?) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onProceedToNext: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = newLevel,
            onValueChange = onNewLevelChange,
            label = { Text("Nombre del Nivel") },
            placeholder = { Text("Ej: Mini, Baby, Kids") },
            isError = validationResult.newLevelError != null,
            supportingText = {
                validationResult.newLevelError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Campo para el nombre del nivel"
                },
            singleLine = true,
            enabled = formState !is NivelFormState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary
            )
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = levelNumber?.let { "Nivel $it (${toRomanNumeral(it.toInt())})" } ?: "Seleccionar número (opcional)",
                    onValueChange = {},
                    label = { Text("Número de Nivel (Opcional)") },
                    isError = validationResult.levelNumberError != null,
                    supportingText = {
                        validationResult.levelNumberError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .semantics {
                            contentDescription = "Campo para seleccionar número de nivel"
                        },
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        focusedLabelColor = AppColors.Primary
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onProceedToNext()
                        }
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .exposedDropdownSize()
                ) {
                    DropdownMenuItem(
                        text = { Text("Ninguno", style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onLevelNumberChange(null)
                            onExpandedChange(false)
                            // Después de seleccionar, el usuario puede presionar Enter para continuar
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    levelOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text("Nivel $option (${toRomanNumeral(option.toInt())})", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onLevelNumberChange(option)
                                onExpandedChange(false)
                                // Después de seleccionar, el usuario puede presionar Enter para continuar
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationContent(
    newLevel: String,
    levelNumber: String?,
    formState: NivelFormState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Confirmar Datos del Nivel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextColor
                )

                HorizontalDivider(
                    color = AppColors.Primary.copy(alpha = 0.3f),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nombre completo:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = buildLevelName(newLevel, levelNumber),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (levelNumber != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Número de nivel:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$levelNumber (${toRomanNumeral(levelNumber.toInt())})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Primary
                        )
                    }
                }
            }
        }

        if (formState is NivelFormState.Error) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = formState.message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    currentStep: NivelFormStep,
    formState: NivelFormState,
    onDismiss: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Cancelar
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .width(100.dp)
                .height(40.dp),
            enabled = formState !is NivelFormState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFff8abe),
                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Cancelar",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        if (currentStep == NivelFormStep.CONFIRMATION) {
            Button(
                onClick = onPrevious,
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp),
                enabled = formState !is NivelFormState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFff8abe),
                    disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Anterior",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = formState !is NivelFormState.Loading,
            modifier = Modifier
                .width(110.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFff8abe),
                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (formState is NivelFormState.Loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = if (currentStep == NivelFormStep.CONFIRMATION) "Registrar" else "Siguiente",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}