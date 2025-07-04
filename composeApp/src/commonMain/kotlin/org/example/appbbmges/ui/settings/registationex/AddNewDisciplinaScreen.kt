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
    var expanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var levelError by remember { mutableStateOf<String?>(null) }

    val allLevels = remember { mutableStateOf<List<LevelEntity>>(emptyList()) }
    val existingDisciplines = remember { mutableStateOf<List<DisciplineSelectAll>>(emptyList()) }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        try {
            allLevels.value = repository.getAllLevels()
            existingDisciplines.value = repository.getAllDisciplines()
            if (allLevels.value.isEmpty()) {
                formState = DisciplinaFormState.Error("No hay niveles disponibles. Crea niveles primero.")
            }
        } catch (e: Exception) {
            formState = DisciplinaFormState.Error("Error al cargar datos: ${e.message}")
        }
    }

    // Función de validación mejorada
    fun validateForm(): Boolean {
        nameError = when {
            name.isEmpty() -> "El nombre es obligatorio"
            name.length < 3 -> "Mínimo 3 caracteres"
            name.length > 50 -> "Máximo 50 caracteres"
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> "Solo letras y espacios"
            else -> null
        }

        levelError = when {
            selectedLevel == null -> "Selecciona un nivel"
            allLevels.value.isEmpty() -> "No hay niveles disponibles"
            existingDisciplines.value.any {
                it.name.equals(name.trim(), ignoreCase = true) && it.level_id == selectedLevel!!.id
            } -> "Ya existe esta disciplina en ${selectedLevel!!.name}"
            else -> null
        }

        return nameError == null && levelError == null
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
                        repository.insertDiscipline(name.trim(), levelId)
                    }
                    formState = DisciplinaFormState.Success
                    onDismiss()
                } catch (e: Exception) {
                    formState = DisciplinaFormState.Error("Error al registrar: ${e.message}")
                }
            }
        }
    }

    // UI Principal
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
                            DisciplinaFormStep.INFO -> "Paso 1: Información"
                            DisciplinaFormStep.CONFIRMATION -> "Paso 2: Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    // Contenido del formulario
                    when (currentStep) {
                        DisciplinaFormStep.INFO -> {
                            // Campo de nombre
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    nameError = null
                                },
                                label = { Text("Nombre de la disciplina") },
                                isError = nameError != null,
                                supportingText = {
                                    nameError?.let {
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

                            // Selector de nivel
                            if (allLevels.value.isEmpty()) {
                                Text(
                                    text = "⚠️ No hay niveles disponibles. Registra niveles primero.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        readOnly = true,
                                        value = selectedLevel?.name ?: "Seleccionar nivel",
                                        onValueChange = {},
                                        label = { Text("Nivel") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expanded
                                            )
                                        },
                                        isError = levelError != null,
                                        supportingText = {
                                            levelError?.let {
                                                Text(it, color = MaterialTheme.colorScheme.error)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        allLevels.value.forEach { level ->
                                            DropdownMenuItem(
                                                text = { Text(level.name) },
                                                onClick = {
                                                    selectedLevel = level
                                                    levelError = null
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        DisciplinaFormStep.CONFIRMATION -> {
                            // Confirmación
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
                                        text = "Confirmar Registro",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Divider(color = AppColors.Primary.copy(alpha = 0.3f))

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Disciplina:", fontWeight = FontWeight.Medium)
                                        Text(name.trim(), fontWeight = FontWeight.Bold)
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Nivel:", fontWeight = FontWeight.Medium)
                                        Text(selectedLevel?.name ?: "Ninguno", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Mensajes de error globales
                    if (formState is DisciplinaFormState.Error) {
                        Text(
                            text = (formState as DisciplinaFormState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Botones de navegación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFff8abe)
                            )
                        ) {
                            Text("Cancelar")
                        }

                        if (currentStep == DisciplinaFormStep.CONFIRMATION) {
                            Button(
                                onClick = { currentStep = DisciplinaFormStep.INFO },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFff8abe)
                                )
                            ) {
                                Text("Atrás")
                            }
                        }

                        Button(
                            onClick = { proceedToNext() },
                            modifier = Modifier.weight(1f),
                            enabled = formState !is DisciplinaFormState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            )
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