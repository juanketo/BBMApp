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
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.example.appbbmges.LevelEntity
import org.example.appbbmges.DisciplineSelectAll
import org.jetbrains.compose.resources.painterResource

// --- Modelos de datos y utilidades ---

enum class DisciplinaFormStep {
    INFO,
    CONFIRMATION
}

sealed class DisciplinaFormState {
    object Idle : DisciplinaFormState()
    object Loading : DisciplinaFormState()
    // El estado Error puede contener una lista de las disciplinas que fallaron la inserción (por si la validación UI falla o hay un race condition)
    data class Error(val message: String, val failedDisciplines: List<Pair<String, Long>> = emptyList()) : DisciplinaFormState()
    object Success : DisciplinaFormState()
}

// Clase para los resultados de validación, similar a la de niveles
data class DisciplinaValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val levelsError: String? = null, // Para indicar si no se seleccionó ningún nivel
    val duplicateError: String? = null // Para indicar los duplicados
)

// Función helper para construir el nombre completo de la disciplina (ej. "Danza Mini Grado 1")
private fun buildDisciplineFullName(baseName: String, levelName: String): String {
    return "$baseName $levelName".trim()
}

// Clase para la lógica de validación, similar a NivelValidator
class DisciplinaValidator {
    companion object {
        fun validate(
            baseName: String,
            selectedLevelEntities: List<LevelEntity>, // Recibe los objetos LevelEntity para obtener sus nombres
            existingDisciplines: List<DisciplineSelectAll>
        ): DisciplinaValidationResult {
            val nameError = when {
                baseName.isEmpty() -> "El nombre de la disciplina es obligatorio"
                baseName.length < 3 -> "Mínimo 3 caracteres"
                baseName.length > 50 -> "Máximo 50 caracteres"
                !baseName.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> "Solo letras y espacios (sin números ni caracteres especiales)"
                else -> null
            }

            val levelsError = if (selectedLevelEntities.isEmpty()) "Debes seleccionar al menos un nivel" else null

            val duplicateErrors = mutableListOf<String>()
            if (nameError == null && levelsError == null) { // Solo si el nombre y la selección de niveles son válidos, busca duplicados
                selectedLevelEntities.forEach { selectedLevel ->
                    val potentialDisciplineName = buildDisciplineFullName(baseName, selectedLevel.name)
                    // Comprueba si esta combinación (nombre completo + ID de nivel) ya existe
                    if (existingDisciplines.any {
                            it.name.equals(potentialDisciplineName, ignoreCase = true) &&
                                    it.level_id == selectedLevel.id
                        }) {
                        duplicateErrors.add("Ya existe '$potentialDisciplineName'")
                    }
                }
            }

            val duplicateError = if (duplicateErrors.isNotEmpty()) {
                duplicateErrors.joinToString("\n• ", prefix = "Las siguientes ya existen y no pueden ser registradas:\n• ")
            } else null

            return DisciplinaValidationResult(
                isValid = nameError == null && levelsError == null && duplicateError == null,
                nameError = nameError,
                levelsError = levelsError,
                duplicateError = duplicateError
            )
        }
    }
}

// --- Composable Principal ---

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
    var baseName by remember { mutableStateOf("") } // Nombre base de la disciplina
    var selectedLevelIds by remember { mutableStateOf<Set<Long>>(emptySet()) } // Set de IDs de los niveles seleccionados
    var formState by remember { mutableStateOf<DisciplinaFormState>(DisciplinaFormState.Idle) }

    // Estados de validación
    var validationResult by remember { mutableStateOf(DisciplinaValidationResult(true)) }

    // Datos maestros: todos los niveles disponibles y todas las disciplinas existentes
    val allLevels = remember { mutableStateOf<List<LevelEntity>>(emptyList()) }
    val existingDisciplines = remember { mutableStateOf<List<DisciplineSelectAll>>(emptyList()) }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        try {
            allLevels.value = repository.getAllLevels()
            existingDisciplines.value = repository.getAllDisciplines()
            if (allLevels.value.isEmpty()) {
                formState = DisciplinaFormState.Error("No hay niveles disponibles. Crea niveles primero para poder asociar disciplinas.")
            }
        } catch (e: Exception) {
            formState = DisciplinaFormState.Error("Error al cargar datos iniciales: ${e.message}")
        }
    }

    // Función de validación que usa la nueva clase DisciplinaValidator
    fun validateForm(): Boolean {
        // Mapea los IDs seleccionados a los objetos LevelEntity completos para la validación
        val selectedLevelEntities = allLevels.value.filter { it.id in selectedLevelIds }
        val result = DisciplinaValidator.validate(
            baseName = baseName,
            selectedLevelEntities = selectedLevelEntities,
            existingDisciplines = existingDisciplines.value
        )
        validationResult = result
        return result.isValid
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
                val levelsToInsertIds = selectedLevelIds.toList()

                if (levelsToInsertIds.isEmpty()) {
                    // Esto debería ser atrapado por la validación del paso INFO, pero como fallback
                    formState = DisciplinaFormState.Error("Debes seleccionar al menos un nivel para la disciplina.")
                    return
                }

                try {
                    // Usa el método de inserción por lotes
                    val failedInsertions = repository.insertDisciplinesWithLevelsBatch(baseName.trim(), levelsToInsertIds)

                    if (failedInsertions.isNotEmpty()) {
                        // Algunas disciplinas ya existían o hubo un problema específico
                        formState = DisciplinaFormState.Error(
                            message = "Se registraron algunas disciplinas, pero otras ya existían.",
                            failedDisciplines = failedInsertions
                        )
                        // Si quieres que el formulario se cierre solo si todo es exitoso,
                        // elimina el onDismiss() de aquí. Si quieres que se cierre y muestre el error de las fallidas, déjalo.
                        onDismiss() // Puedes decidir si cerrar o no si hay fallos parciales
                    } else {
                        // Todas las inserciones fueron exitosas
                        formState = DisciplinaFormState.Success
                        onDismiss()
                    }
                } catch (e: Exception) {
                    // Errores inesperados durante la transacción
                    formState = DisciplinaFormState.Error("Error inesperado al registrar disciplinas: ${e.message}")
                }
            }
        }
    }

    // --- UI Principal ---
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
                    // Header
                    val progress by animateFloatAsState(
                        targetValue = when (currentStep) {
                            DisciplinaFormStep.INFO -> 0.5f
                            DisciplinaFormStep.CONFIRMATION -> 1f
                        },
                        animationSpec = tween(300),
                        label = "progress"
                    )

                    Text(
                        text = "Registro de Disciplina",
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
                            DisciplinaFormStep.INFO -> "Paso 1: Información de la Disciplina"
                            DisciplinaFormStep.CONFIRMATION -> "Paso 2: Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    // Contenido del formulario basado en el paso actual
                    when (currentStep) {
                        DisciplinaFormStep.INFO -> {
                            // Campo de nombre base de la disciplina
                            OutlinedTextField(
                                value = baseName,
                                onValueChange = {
                                    baseName = it
                                    // Limpiar errores cuando el usuario escribe
                                    if (validationResult.nameError != null || validationResult.duplicateError != null) {
                                        validationResult = validationResult.copy(nameError = null, duplicateError = null)
                                    }
                                },
                                label = { Text("Nombre de la disciplina (base)") },
                                placeholder = { Text("Ej: Danza, Jazz, Hip Hop") },
                                isError = validationResult.nameError != null,
                                supportingText = {
                                    validationResult.nameError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )

                            Spacer(Modifier.height(16.dp))

                            // Selector de niveles (Múltiple con Checkboxes)
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
                                        text = "Seleccionar Niveles a Asociar",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextColor
                                    )

                                    if (allLevels.value.isEmpty()) {
                                        Text(
                                            text = "⚠️ No hay niveles disponibles. Crea niveles primero.",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    } else {
                                        // Mostrar error si no se selecciona ningún nivel
                                        if (validationResult.levelsError != null) {
                                            Text(
                                                text = validationResult.levelsError!!,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        // Checkboxes para los niveles existentes
                                        allLevels.value.forEach { level ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = selectedLevelIds.contains(level.id),
                                                    onCheckedChange = { isChecked ->
                                                        selectedLevelIds = if (isChecked) {
                                                            selectedLevelIds + level.id
                                                        } else {
                                                            selectedLevelIds - level.id
                                                        }
                                                        // Limpiar errores cuando se cambia la selección de niveles
                                                        if (validationResult.levelsError != null || validationResult.duplicateError != null) {
                                                            validationResult = validationResult.copy(levelsError = null, duplicateError = null)
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = level.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (selectedLevelIds.contains(level.id)) AppColors.Primary else AppColors.TextColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Mostrar error de duplicados de validación
                            if (validationResult.duplicateError != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = validationResult.duplicateError!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Vista previa de las disciplinas a crear
                            if (baseName.isNotEmpty() && selectedLevelIds.isNotEmpty()) {
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
                                            text = "Se crearán las siguientes disciplinas:",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = AppColors.Primary
                                        )
                                        val selectedLevelsData = allLevels.value.filter { it.id in selectedLevelIds }
                                        selectedLevelsData.sortedBy { it.name }.forEach { level ->
                                            Text(
                                                text = "• ${buildDisciplineFullName(baseName, level.name)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppColors.Primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        DisciplinaFormStep.CONFIRMATION -> {
                            // Contenido del paso de confirmación
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
                                        text = "Confirmar Registro de Disciplinas",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        color = AppColors.Primary.copy(alpha = 0.3f)
                                    )

                                    val disciplinesToConfirm = allLevels.value
                                        .filter { it.id in selectedLevelIds }
                                        .map { level -> buildDisciplineFullName(baseName, level.name) }
                                        .sorted()

                                    Text(
                                        text = "Se ${if (disciplinesToConfirm.size > 1) "registrarán" else "registrará"} ${disciplinesToConfirm.size} ${if (disciplinesToConfirm.size > 1) "disciplinas" else "disciplina"}:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.TextColor
                                    )
                                    disciplinesToConfirm.forEach { disciplineName ->
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
                                                text = disciplineName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = AppColors.Primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))

                            // Mensajes de error globales o de inserción
                            if (formState is DisciplinaFormState.Error) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Error de registro:",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = (formState as DisciplinaFormState.Error).message,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if ((formState as DisciplinaFormState.Error).failedDisciplines.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Las siguientes ya existían y no se registraron:",
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold // Resaltar los nombres de las que ya existen
                                            )
                                            (formState as DisciplinaFormState.Error).failedDisciplines.forEach { (name, _) ->
                                                Text(
                                                    text = "• $name",
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
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
                            enabled = formState !is DisciplinaFormState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFff8abe),
                                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar")
                        }

                        if (currentStep == DisciplinaFormStep.CONFIRMATION) {
                            Button(
                                onClick = { currentStep = DisciplinaFormStep.INFO },
                                modifier = Modifier.weight(1f),
                                enabled = formState !is DisciplinaFormState.Loading,
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
                            enabled = formState !is DisciplinaFormState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                disabledContainerColor = AppColors.Primary.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (formState is DisciplinaFormState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    when (currentStep) {
                                        DisciplinaFormStep.INFO -> "Siguiente"
                                        DisciplinaFormStep.CONFIRMATION -> "Registrar"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Composable para el Logo de Fondo (tal como lo tenías) ---
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