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
import org.example.appbbmges.LevelEntity
import org.jetbrains.compose.resources.painterResource

data class NivelData(
    val new_level: String,
    val selectedGrades: Set<Int>
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
    val gradesError: String? = null,
    val duplicateError: String? = null
)

class NivelValidator {
    companion object {
        fun validateNivel(
            newLevel: String,
            selectedGrades: Set<Int>,
            existingLevels: List<LevelEntity>
        ): NivelValidationResult {
            val newLevelError = when {
                newLevel.isEmpty() -> "El nombre del nivel es obligatorio"
                newLevel.length < 4 -> "El nombre del nivel debe tener al menos 4 caracteres"
                newLevel.length > 50 -> "El nombre del nivel no puede exceder 50 caracteres"
                !newLevel.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 ]+$")) -> "El nombre solo puede contener letras, números y espacios (sin caracteres especiales)"
                !newLevel.first().isUpperCase() -> "El nombre debe iniciar con mayúscula"
                else -> null
            }

            // Validación de duplicados para cada combinación
            val duplicateErrors = mutableListOf<String>()

            // Verificar si el nivel sin grado ya existe
            if (selectedGrades.isEmpty()) {
                if (existingLevels.any { it.name.equals(newLevel, ignoreCase = true) }) {
                    duplicateErrors.add("Ya existe el nivel '$newLevel'")
                }
            } else {
                // Verificar cada grado seleccionado
                selectedGrades.forEach { grade ->
                    val levelName = buildLevelName(newLevel, grade)
                    if (existingLevels.any { it.name.equals(levelName, ignoreCase = true) }) {
                        duplicateErrors.add("Ya existe '$levelName'")
                    }
                }
            }

            val duplicateError = if (duplicateErrors.isNotEmpty()) {
                duplicateErrors.joinToString(", ")
            } else null

            return NivelValidationResult(
                isValid = newLevelError == null && duplicateError == null,
                newLevelError = newLevelError,
                duplicateError = duplicateError
            )
        }
    }
}

// Función helper para construir el nombre del nivel con grado
private fun buildLevelName(name: String, grade: Int?): String {
    return if (grade != null) {
        "$name Grado $grade"
    } else {
        name
    }.trim()
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
    var selectedGrades by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var withoutGrade by remember { mutableStateOf(false) }
    var formState by remember { mutableStateOf<NivelFormState>(NivelFormState.Idle) }
    var validationResult by remember { mutableStateOf(NivelValidationResult(true)) }

    // Estado para almacenar los niveles existentes
    val existingLevels = remember { mutableStateOf<List<LevelEntity>>(emptyList()) }

    // Cargar niveles existentes al inicializar el componente
    LaunchedEffect(Unit) {
        existingLevels.value = repository.getAllLevels()
    }

    // Función de validación actualizada
    fun validateForm(): Boolean {
        val gradesToValidate = if (withoutGrade) emptySet() else selectedGrades
        val validation = NivelValidator.validateNivel(new_level, gradesToValidate, existingLevels.value)
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
                    if (withoutGrade) {
                        // Crear solo el nivel sin grado
                        repository.insertLevel(new_level)
                    } else {
                        // Crear un registro para cada grado seleccionado
                        selectedGrades.forEach { grade ->
                            val levelName = buildLevelName(new_level, grade)
                            repository.insertLevel(levelName)
                        }
                    }
                    formState = NivelFormState.Success
                    onDismiss()
                } catch (e: Exception) {
                    formState = NivelFormState.Error("Error al registrar el(los) nivel(es): ${e.message}")
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
                                selectedGrades = selectedGrades,
                                withoutGrade = withoutGrade,
                                validationResult = validationResult,
                                formState = formState,
                                onNewLevelChange = {
                                    new_level = it
                                    if (validationResult.newLevelError != null || validationResult.duplicateError != null) {
                                        validationResult = validationResult.copy(
                                            newLevelError = null,
                                            duplicateError = null
                                        )
                                    }
                                },
                                onGradeToggle = { grade ->
                                    selectedGrades = if (selectedGrades.contains(grade)) {
                                        selectedGrades - grade
                                    } else {
                                        selectedGrades + grade
                                    }
                                    if (validationResult.duplicateError != null) {
                                        validationResult = validationResult.copy(
                                            duplicateError = null
                                        )
                                    }
                                },
                                onWithoutGradeToggle = {
                                    withoutGrade = it
                                    if (it) {
                                        selectedGrades = emptySet()
                                    }
                                    if (validationResult.duplicateError != null) {
                                        validationResult = validationResult.copy(
                                            duplicateError = null
                                        )
                                    }
                                },
                                onProceedToNext = { proceedToNext() },
                                focusManager = focusManager
                            )
                        }
                        NivelFormStep.CONFIRMATION -> {
                            ConfirmationContent(
                                newLevel = new_level,
                                selectedGrades = selectedGrades,
                                withoutGrade = withoutGrade,
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
    selectedGrades: Set<Int>,
    withoutGrade: Boolean,
    validationResult: NivelValidationResult,
    formState: NivelFormState,
    onNewLevelChange: (String) -> Unit,
    onGradeToggle: (Int) -> Unit,
    onWithoutGradeToggle: (Boolean) -> Unit,
    onProceedToNext: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = newLevel,
            onValueChange = onNewLevelChange,
            label = { Text("Nombre del Nivel") },
            placeholder = { Text("Ej: Mini, Baby, Primary 1") },
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
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Seleccionar Grados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextColor
                )

                // Checkbox para sin grado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = withoutGrade,
                        onCheckedChange = onWithoutGradeToggle,
                        enabled = formState !is NivelFormState.Loading,
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppColors.Primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sin grado específico",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (withoutGrade) AppColors.Primary else AppColors.TextColor
                    )
                }

                if (!withoutGrade) {
                    HorizontalDivider(
                        color = AppColors.Primary.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Text(
                        text = "O selecciona uno o más grados específicos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextColor.copy(alpha = 0.7f)
                    )

                    // Checkboxes para los grados
                    (1..3).forEach { grade ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedGrades.contains(grade),
                                onCheckedChange = { onGradeToggle(grade) },
                                enabled = formState !is NivelFormState.Loading,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppColors.Primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Grado $grade",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedGrades.contains(grade)) AppColors.Primary else AppColors.TextColor
                            )
                        }
                    }
                }
            }
        }

        // Mostrar error de duplicado si existe
        if (validationResult.duplicateError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = validationResult.duplicateError,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Mostrar preview de lo que se va a crear
        if (newLevel.isNotEmpty() && (withoutGrade || selectedGrades.isNotEmpty())) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.Primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Se crearán los siguientes niveles:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Primary
                    )

                    if (withoutGrade) {
                        Text(
                            text = "• $newLevel",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Primary
                        )
                    } else {
                        selectedGrades.sorted().forEach { grade ->
                            Text(
                                text = "• ${buildLevelName(newLevel, grade)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationContent(
    newLevel: String,
    selectedGrades: Set<Int>,
    withoutGrade: Boolean,
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Confirmar Registro de Niveles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextColor
                )

                HorizontalDivider(
                    color = AppColors.Primary.copy(alpha = 0.3f),
                    thickness = 1.dp
                )

                val levelsToCreate = if (withoutGrade) {
                    listOf(newLevel)
                } else {
                    selectedGrades.sorted().map { grade -> buildLevelName(newLevel, grade) }
                }

                Text(
                    text = "Se ${if (levelsToCreate.size > 1) "registrarán" else "registrará"} ${levelsToCreate.size} ${if (levelsToCreate.size > 1) "niveles" else "nivel"}:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextColor
                )

                levelsToCreate.forEach { levelName ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = levelName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Primary,
                            fontWeight = FontWeight.Bold
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