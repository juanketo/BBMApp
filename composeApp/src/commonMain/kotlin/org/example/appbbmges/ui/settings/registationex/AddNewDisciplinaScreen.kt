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
import org.example.appbbmges.LevelEntity
import org.example.appbbmges.DisciplineSelectAll
import org.jetbrains.compose.resources.painterResource

data class DisciplinaData(
    val name: String,
    val levelId: Long
)

enum class DisciplinaFormStep {
    INFO,
    CONFIRMATION
}

sealed class DisciplinaFormState {
    object Idle : DisciplinaFormState()
    object Loading : DisciplinaFormState()
    data class Error(val message: String) : DisciplinaFormState()
    object Success : DisciplinaFormState()
}

data class DisciplinaValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val levelError: String? = null
)

class DisciplinaValidator {
    companion object {
        fun validateDisciplina(
            name: String,
            selectedLevel: LevelEntity?,
            existingDisciplines: List<DisciplineSelectAll>,
            allLevels: List<LevelEntity>
        ): DisciplinaValidationResult {
            val nameError = when {
                name.isEmpty() -> "El nombre de la disciplina es obligatorio"
                name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
                name.length > 50 -> "El nombre no puede exceder 50 caracteres"
                !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> "El nombre solo puede contener letras y espacios"
                name.trim() != name -> "El nombre no puede empezar o terminar con espacios"
                name.contains(Regex("\\s{2,}")) -> "El nombre no puede contener espacios consecutivos"
                else -> null
            }

            val levelError = when {
                selectedLevel == null -> "Debe seleccionar un nivel"
                allLevels.isEmpty() -> "No hay niveles disponibles"
                existingDisciplines.any {
                    it.name.trim().startsWith(name.trim(), ignoreCase = true) && it.level_id == selectedLevel.id
                } -> "La combinación de disciplina '${name.trim()}' y nivel '${selectedLevel.name}' ya existe"
                else -> null
            }

            return DisciplinaValidationResult(
                isValid = nameError == null && levelError == null,
                nameError = nameError,
                levelError = levelError
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewDisciplinaScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // Estados del formulario
    var currentStep by remember { mutableStateOf(DisciplinaFormStep.INFO) }
    var name by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf<LevelEntity?>(null) }
    var formState by remember { mutableStateOf<DisciplinaFormState>(DisciplinaFormState.Idle) }
    var validationResult by remember { mutableStateOf(DisciplinaValidationResult(true)) }
    var expanded by remember { mutableStateOf(false) }

    val allLevels = remember { mutableStateOf<List<LevelEntity>>(emptyList()) }
    val existingDisciplines = remember { mutableStateOf<List<DisciplineSelectAll>>(emptyList()) }

    LaunchedEffect(Unit) {
        allLevels.value = repository.getAllLevels()
        existingDisciplines.value = repository.getAllDisciplines()
    }

    // Función de validación
    fun validateForm(): Boolean {
        val validation = DisciplinaValidator.validateDisciplina(name, selectedLevel, existingDisciplines.value, allLevels.value)
        validationResult = validation
        return validation.isValid
    }

    // Función para proceder al siguiente paso
    fun proceedToNext() {
        when (currentStep) {
            DisciplinaFormStep.INFO -> {
                if (validateForm()) {
                    currentStep = DisciplinaFormStep.CONFIRMATION
                }
            }
            DisciplinaFormStep.CONFIRMATION -> {
                formState = DisciplinaFormState.Loading
                try {
                    selectedLevel?.id?.let { levelId ->
                        repository.insertDisciplineWithLevels(name.trim(), listOf(levelId))
                    }
                    formState = DisciplinaFormState.Success
                    onDismiss()
                } catch (e: Exception) {
                    formState = DisciplinaFormState.Error("Error al registrar la disciplina: ${e.message}")
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
                        DisciplinaFormStep.INFO -> {
                            InfoContent(
                                name = name,
                                selectedLevel = selectedLevel,
                                allLevels = allLevels.value,
                                validationResult = validationResult,
                                formState = formState,
                                expanded = expanded,
                                onNameChange = {
                                    name = it
                                    if (validationResult.nameError != null) {
                                        validationResult = validationResult.copy(nameError = null)
                                    }
                                },
                                onLevelChange = {
                                    selectedLevel = it
                                    if (validationResult.levelError != null) {
                                        validationResult = validationResult.copy(levelError = null)
                                    }
                                },
                                onExpandedChange = { expanded = it },
                                onProceedToNext = { proceedToNext() },
                                focusManager = focusManager
                            )
                        }
                        DisciplinaFormStep.CONFIRMATION -> {
                            ConfirmationContent(
                                name = name,
                                selectedLevel = selectedLevel,
                                formState = formState
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationButtons(
                        currentStep = currentStep,
                        formState = formState,
                        onDismiss = onDismiss,
                        onPrevious = { currentStep = DisciplinaFormStep.INFO },
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
private fun FormHeader(currentStep: DisciplinaFormStep) {
    val animatedProgress by animateFloatAsState(
        targetValue = when (currentStep) {
            DisciplinaFormStep.INFO -> 0.5f
            DisciplinaFormStep.CONFIRMATION -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "progress_animation"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Registro de Disciplina",
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
                DisciplinaFormStep.INFO -> "Paso 1: Información y Nivel"
                DisciplinaFormStep.CONFIRMATION -> "Paso 2: Confirmación"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoContent(
    name: String,
    selectedLevel: LevelEntity?,
    allLevels: List<LevelEntity>,
    validationResult: DisciplinaValidationResult,
    formState: DisciplinaFormState,
    expanded: Boolean,
    onNameChange: (String) -> Unit,
    onLevelChange: (LevelEntity?) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onProceedToNext: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre de la Disciplina") },
            placeholder = { Text("Ej: Danza Árabe, Música, Teatro") },
            isError = validationResult.nameError != null,
            supportingText = {
                validationResult.nameError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Campo para el nombre de la disciplina"
                },
            singleLine = true,
            enabled = formState !is DisciplinaFormState.Loading,
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

        if (allLevels.isEmpty()) {
            Text(
                text = "No hay niveles disponibles. Por favor crea niveles primero.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedLevel?.name ?: "Seleccionar nivel",
                        onValueChange = {},
                        label = { Text("Nivel") },
                        isError = validationResult.levelError != null,
                        supportingText = {
                            validationResult.levelError?.let {
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
                                contentDescription = "Campo para seleccionar nivel"
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
                        allLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text("Nivel ${level.name}", style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    onLevelChange(level)
                                    onExpandedChange(false)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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
    name: String,
    selectedLevel: LevelEntity?,
    formState: DisciplinaFormState
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
                    text = "Confirmar Datos de la Disciplina",
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
                        text = "Nombre:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = name.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nivel:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = selectedLevel?.name ?: "Ninguno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Primary
                    )
                }
            }
        }

        if (formState is DisciplinaFormState.Error) {
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
    currentStep: DisciplinaFormStep,
    formState: DisciplinaFormState,
    onDismiss: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .width(100.dp)
                .height(40.dp),
            enabled = formState !is DisciplinaFormState.Loading,
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

        if (currentStep == DisciplinaFormStep.CONFIRMATION) {
            Button(
                onClick = onPrevious,
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp),
                enabled = formState !is DisciplinaFormState.Loading,
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
            enabled = formState !is DisciplinaFormState.Loading,
            modifier = Modifier
                .width(110.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFff8abe),
                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (formState is DisciplinaFormState.Loading) {
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
                    text = if (currentStep == DisciplinaFormStep.CONFIRMATION) "Registrar" else "Siguiente",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}