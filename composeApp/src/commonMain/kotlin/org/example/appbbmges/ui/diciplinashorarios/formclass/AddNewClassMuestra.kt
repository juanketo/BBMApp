package org.example.appbbmges.ui.diciplinashorarios.formclass

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.example.appbbmges.DisciplineSelectAll
import org.example.appbbmges.LevelEntity
import org.jetbrains.compose.resources.painterResource

enum class ClassFormStep {
    INFO,
    CONFIRMATION
}

sealed class ClassFormState {
    object Idle : ClassFormState()
    object Loading : ClassFormState()
    data class Error(val message: String) : ClassFormState()
    object Success : ClassFormState()
}

data class ClassValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val teacherError: String? = null,
    val studentsError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val disciplineError: String? = null,
    val levelError: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewClassMuestra(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Estados del formulario
    var currentStep by remember { mutableStateOf(ClassFormStep.INFO) }
    var classFormState by remember { mutableStateOf<ClassFormState>(ClassFormState.Idle) }

    // Estados para los campos del formulario
    var className by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var studentsCount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // Estados para los dropdowns
    var expandedLevel by remember { mutableStateOf(false) }
    var expandedDiscipline by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf<LevelEntity?>(null) }
    var selectedDiscipline by remember { mutableStateOf<DisciplineSelectAll?>(null) }

    // Estados para los datos
    val levels = remember { mutableStateOf<List<LevelEntity>>(emptyList()) }
    val disciplines = remember { mutableStateOf<List<DisciplineSelectAll>>(emptyList()) }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        levels.value = repository.getAllLevels()
        disciplines.value = repository.getAllDisciplines()
    }

    // Filtrar disciplinas basadas en el nivel seleccionado
    val filteredDisciplines = remember(selectedLevel, disciplines.value) {
        if (selectedLevel == null) {
            disciplines.value
        } else {
            disciplines.value.filter { it.level_id == selectedLevel?.id }
        }
    }

    // Validación
    var validationResult by remember { mutableStateOf(ClassValidationResult(true)) }

    fun validateForm(): Boolean {
        val result = ClassValidationResult(
            isValid = true,
            nameError = if (className.isBlank()) "Nombre de clase requerido" else null,
            teacherError = if (teacherName.isBlank()) "Profesor requerido" else null,
            studentsError = when {
                studentsCount.isBlank() -> "Número de alumnos requerido"
                studentsCount.toIntOrNull() == null -> "Número inválido"
                studentsCount.toInt() <= 0 -> "Debe ser mayor a 0"
                else -> null
            },
            priceError = when {
                price.isBlank() -> "Precio requerido"
                price.toDoubleOrNull() == null -> "Precio inválido"
                price.toDouble() < 0 -> "Debe ser positivo"
                else -> null
            },
            descriptionError = if (description.isBlank()) "Descripción requerida" else null,
            disciplineError = if (selectedDiscipline == null) "Seleccione una disciplina" else null,
            levelError = if (selectedLevel == null) "Seleccione un nivel" else null
        )

        validationResult = result.copy(
            isValid = result.nameError == null &&
                    result.teacherError == null &&
                    result.studentsError == null &&
                    result.priceError == null &&
                    result.descriptionError == null &&
                    result.disciplineError == null &&
                    result.levelError == null
        )

        return validationResult.isValid
    }

    fun proceedToNext() {
        when (currentStep) {
            ClassFormStep.INFO -> {
                if (validateForm()) {
                    currentStep = ClassFormStep.CONFIRMATION
                }
            }
            ClassFormStep.CONFIRMATION -> {
                classFormState = ClassFormState.Loading
                coroutineScope.launch {
                    try {
                        // Simular guardado en base de datos
                        delay(1000)

                        // Aquí iría la lógica real para guardar la clase
                        // repository.insertSchedule(...)

                        classFormState = ClassFormState.Success
                        onDismiss()
                    } catch (e: Exception) {
                        classFormState = ClassFormState.Error("Error al guardar: ${e.message}")
                    }
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
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundLogo()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val progress by animateFloatAsState(
                        targetValue = when (currentStep) {
                            ClassFormStep.INFO -> 0.5f
                            ClassFormStep.CONFIRMATION -> 1f
                        },
                        animationSpec = tween(300),
                        label = "progress"
                    )

                    Text(
                        text = "Agendar Nueva Clase",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.Primary
                    )

                    Text(
                        text = when (currentStep) {
                            ClassFormStep.INFO -> "Paso 1: Información de la Clase"
                            ClassFormStep.CONFIRMATION -> "Paso 2: Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    when (currentStep) {
                        ClassFormStep.INFO -> {
                            // Campo Nombre de Clase
                            OutlinedTextField(
                                value = className,
                                onValueChange = {
                                    className = it
                                    if (validationResult.nameError != null) {
                                        validationResult = validationResult.copy(nameError = null)
                                    }
                                },
                                label = { Text("Nombre de la Clase") },
                                isError = validationResult.nameError != null,
                                supportingText = {
                                    validationResult.nameError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            // Campo Profesor
                            OutlinedTextField(
                                value = teacherName,
                                onValueChange = {
                                    teacherName = it
                                    if (validationResult.teacherError != null) {
                                        validationResult = validationResult.copy(teacherError = null)
                                    }
                                },
                                label = { Text("Profesor") },
                                isError = validationResult.teacherError != null,
                                supportingText = {
                                    validationResult.teacherError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            // Campo Número de Alumnos
                            OutlinedTextField(
                                value = studentsCount,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                        studentsCount = it
                                        if (validationResult.studentsError != null) {
                                            validationResult = validationResult.copy(studentsError = null)
                                        }
                                    }
                                },
                                label = { Text("Número de Alumnos") },
                                isError = validationResult.studentsError != null,
                                supportingText = {
                                    validationResult.studentsError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(Modifier.height(12.dp))

                            // Campo Descripción
                            OutlinedTextField(
                                value = description,
                                onValueChange = {
                                    description = it
                                    if (validationResult.descriptionError != null) {
                                        validationResult = validationResult.copy(descriptionError = null)
                                    }
                                },
                                label = { Text("Descripción") },
                                isError = validationResult.descriptionError != null,
                                supportingText = {
                                    validationResult.descriptionError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            // Campo Precio
                            OutlinedTextField(
                                value = price,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() || char == '.' } || it.isEmpty()) {
                                        price = it
                                        if (validationResult.priceError != null) {
                                            validationResult = validationResult.copy(priceError = null)
                                        }
                                    }
                                },
                                label = { Text("Precio ($)") },
                                isError = validationResult.priceError != null,
                                supportingText = {
                                    validationResult.priceError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(Modifier.height(16.dp))

                            // Dropdown para Niveles
                            ExposedDropdownMenuBox(
                                expanded = expandedLevel,
                                onExpandedChange = { expandedLevel = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedLevel?.name ?: "Seleccione nivel",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Nivel") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLevel) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    isError = validationResult.levelError != null,
                                    supportingText = {
                                        validationResult.levelError?.let {
                                            Text(it, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedLevel,
                                    onDismissRequest = { expandedLevel = false }
                                ) {
                                    levels.value.forEach { level ->
                                        DropdownMenuItem(
                                            text = { Text(level.name) },
                                            onClick = {
                                                selectedLevel = level
                                                selectedDiscipline = null // Resetear disciplina al cambiar nivel
                                                expandedLevel = false
                                                if (validationResult.levelError != null) {
                                                    validationResult = validationResult.copy(levelError = null)
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Dropdown para Disciplinas
                            ExposedDropdownMenuBox(
                                expanded = expandedDiscipline,
                                onExpandedChange = { expandedDiscipline = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedDiscipline?.let { "${it.name} (${it.level_name})" } ?: "Seleccione disciplina",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Disciplina") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDiscipline) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    enabled = selectedLevel != null,
                                    isError = validationResult.disciplineError != null,
                                    supportingText = {
                                        validationResult.disciplineError?.let {
                                            Text(it, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedDiscipline,
                                    onDismissRequest = { expandedDiscipline = false }
                                ) {
                                    filteredDisciplines.forEach { discipline ->
                                        DropdownMenuItem(
                                            text = { Text("${discipline.name} (${discipline.level_name})") },
                                            onClick = {
                                                selectedDiscipline = discipline
                                                expandedDiscipline = false
                                                if (validationResult.disciplineError != null) {
                                                    validationResult = validationResult.copy(disciplineError = null)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        ClassFormStep.CONFIRMATION -> {
                            // Vista de confirmación
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Confirmar Datos de la Clase",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Divider(color = AppColors.Primary.copy(alpha = 0.3f))

                                    // Mostrar todos los datos ingresados
                                    ConfirmationRow("Nombre:", className)
                                    ConfirmationRow("Profesor:", teacherName)
                                    ConfirmationRow("Alumnos:", studentsCount)
                                    ConfirmationRow("Descripción:", description)
                                    ConfirmationRow("Precio:", "$$price")
                                    selectedLevel?.let {
                                        ConfirmationRow("Nivel:", it.name)
                                    }
                                    selectedDiscipline?.let {
                                        ConfirmationRow("Disciplina:", "${it.name} (${it.level_name})")
                                    }
                                }
                            }

                            // Mostrar estado de carga o error
                            when (classFormState) {
                                is ClassFormState.Loading -> {
                                    Spacer(Modifier.height(16.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        color = AppColors.Primary
                                    )
                                }
                                is ClassFormState.Error -> {
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = (classFormState as ClassFormState.Error).message,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                else -> {}
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Botones de navegación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = classFormState !is ClassFormState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFff8abe),
                                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar")
                        }

                        if (currentStep == ClassFormStep.CONFIRMATION) {
                            Button(
                                onClick = { currentStep = ClassFormStep.INFO },
                                modifier = Modifier.weight(1f),
                                enabled = classFormState !is ClassFormState.Loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFff8abe),
                                    disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Atrás")
                            }
                        }

                        Button(
                            onClick = { proceedToNext() },
                            modifier = Modifier.weight(1f),
                            enabled = classFormState !is ClassFormState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                disabledContainerColor = AppColors.Primary.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                when (currentStep) {
                                    ClassFormStep.INFO -> "Siguiente"
                                    ClassFormStep.CONFIRMATION -> "Confirmar"
                                }
                            )
                        }
                    }
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
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                .alpha(0.08f),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}