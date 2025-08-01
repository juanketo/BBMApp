package org.example.appbbmges.ui.settings.registationex

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.example.appbbmges.ClassroomEntity
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.ui.focus.FocusManager

enum class SalonFormStep {
    FORM,
    CONFIRMATION
}

sealed class SalonFormState {
    object Idle : SalonFormState()
    object Loading : SalonFormState()
    data class Error(val message: String) : SalonFormState()
    object Success : SalonFormState()
}

data class SalonValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val duplicateError: String? = null
)

class SalonValidator {
    companion object {
        fun validateSalon(
            name: String,
            existingSalones: List<ClassroomEntity>,
            excludeId: Long? = null
        ): SalonValidationResult {
            val nameError = when {
                name.isEmpty() -> "El nombre del salón es obligatorio"
                name.length < 3 -> "El nombre del salón debe tener al menos 3 caracteres"
                name.length > 50 -> "El nombre del salón no puede exceder 50 caracteres"
                !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 ]+$")) -> "El nombre solo puede contener letras, números y espacios"
                !name.first().isUpperCase() -> "El nombre debe iniciar con mayúscula"
                else -> null
            }

            val duplicateError = if (existingSalones.any {
                    it.name.equals(name, ignoreCase = true) && it.id != excludeId
                }) {
                "Ya existe un salón con el nombre '$name'"
            } else null

            return SalonValidationResult(
                isValid = nameError == null && duplicateError == null,
                nameError = nameError,
                duplicateError = duplicateError
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSalonScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    var showForm by remember { mutableStateOf(false) }
    var editingSalon by remember { mutableStateOf<ClassroomEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ClassroomEntity?>(null) }
    var existingSalones by remember { mutableStateOf<List<ClassroomEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar salones existentes
    LaunchedEffect(Unit) {
        try {
            existingSalones = repository.getAllClassrooms()
        } catch (e: Exception) {
            println("Error cargando salones: ${e.message}")
            // Considera mostrar un Snackbar o Toast al usuario aquí
        } finally {
            isLoading = false
        }
    }

    // Recargar salones
    fun reloadSalones() {
        isLoading = true
        try {
            existingSalones = repository.getAllClassrooms()
        } catch (e: Exception) {
            println("Error recargando salones: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Formulario para agregar/editar
        if (showForm || editingSalon != null) {
            SalonFormDialog(
                onDismiss = {
                    showForm = false
                    editingSalon = null
                    reloadSalones()
                },
                repository = repository,
                existingSalones = existingSalones,
                editingSalon = editingSalon
            )
        }

        // Diálogo de confirmación para eliminar
        showDeleteDialog?.let { salon ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Eliminar Salón") },
                text = { Text("¿Estás seguro de que deseas eliminar el salón '${salon.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            try {
                                repository.deleteClassroom(salon.id)
                                reloadSalones()
                                showDeleteDialog = null
                            } catch (e: Exception) {
                                println("Error al eliminar salón: ${e.message}")
                                // Considera mostrar un Snackbar o Toast al usuario aquí
                                showDeleteDialog = null
                            }
                        }
                    ) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Vista principal de la lista de salones
        if (!showForm && editingSalon == null && showDeleteDialog == null) { // Solo mostrar si no hay formularios o diálogos activos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Salones",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextColor
                        )
                        Text(
                            text = "${existingSalones.size} salones registrados",
                            fontSize = 14.sp,
                            color = AppColors.TextColor.copy(alpha = 0.7f)
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFff8abe)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Volver",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contenido principal (Tabla de salones o mensaje de vacío)
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                } else if (existingSalones.isEmpty()) {
                    // Estado vacío
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MeetingRoom,
                                contentDescription = "Sin salones",
                                modifier = Modifier.size(64.dp),
                                tint = AppColors.Primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay salones registrados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextColor,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Presiona el botón + para agregar el primer salón",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    // Lista de salones
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Header de la tabla
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AppColors.Primary.copy(alpha = 0.1f))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Nombre",
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary,
                                        modifier = Modifier.weight(3f)
                                    )
                                    Text(
                                        text = "Acciones",
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Filas de datos
                            items(existingSalones) { salon ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = salon.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextColor,
                                        modifier = Modifier.weight(3f)
                                    )
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = { editingSalon = salon },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = "Editar",
                                                tint = AppColors.Primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { showDeleteDialog = salon },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color(0xFFE57373),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                if (salon != existingSalones.last()) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 1.dp,
                                        color = Color(0xFFF0F0F0)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botón flotante para agregar
            FloatingActionButton(
                onClick = { showForm = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = AppColors.Primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Agregar salón"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalonFormDialog(
    onDismiss: () -> Unit,
    repository: Repository,
    existingSalones: List<ClassroomEntity>,
    editingSalon: ClassroomEntity? = null
) {
    val focusManager = LocalFocusManager.current
    val isEditing = editingSalon != null

    // Estados del formulario
    var currentStep by remember { mutableStateOf(SalonFormStep.FORM) }
    var name by remember { mutableStateOf(editingSalon?.name ?: "") }
    var formState by remember { mutableStateOf<SalonFormState>(SalonFormState.Idle) }
    var validationResult by remember { mutableStateOf(SalonValidationResult(true)) }

    // NOTA IMPORTANTE: Necesitas una forma de obtener el ID de la franquicia actual.
    // Por simplicidad en este ejemplo, se usa un valor fijo.
    // DEBES REEMPLAZAR ESTO con el ID de franquicia real de tu aplicación.
    val currentFranchiseId = 1L

    // Función de validación
    fun validateForm(): Boolean {
        val validation = SalonValidator.validateSalon(
            name = name,
            existingSalones = existingSalones,
            excludeId = editingSalon?.id // Excluye el salón actual de la validación de duplicados al editar
        )
        validationResult = validation
        return validation.isValid
    }

    // Función para proceder al siguiente paso o guardar
    fun proceedToNext() {
        when (currentStep) {
            SalonFormStep.FORM -> {
                if (validateForm()) {
                    currentStep = SalonFormStep.CONFIRMATION
                }
            }
            SalonFormStep.CONFIRMATION -> {
                formState = SalonFormState.Loading
                try {
                    if (isEditing) {
                        editingSalon?.let { salon ->
                            repository.updateClassroom(
                                id = salon.id,
                                franchiseId = currentFranchiseId, // Usar el ID actual de la franquicia
                                name = name
                            )
                        }
                    } else {
                        repository.insertClassroom(
                            franchiseId = currentFranchiseId,
                            name = name
                        )
                    }
                    formState = SalonFormState.Success
                    onDismiss() // Cierra el diálogo al éxito
                } catch (e: Exception) {
                    formState = SalonFormState.Error("Error al ${if (isEditing) "actualizar" else "registrar"} el salón: ${e.message}")
                    println("Error al ${if (isEditing) "actualizar" else "registrar"} el salón: ${e.message}") // Log de error para depuración
                }
            }
        }
    }

    Box(
        modifier = Modifier
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
                    FormHeader(currentStep, isEditing)

                    Spacer(modifier = Modifier.height(12.dp))

                    when (currentStep) {
                        SalonFormStep.FORM -> {
                            SalonFormContent(
                                name = name,
                                validationResult = validationResult,
                                formState = formState,
                                onNameChange = {
                                    name = it
                                    // Limpiar errores de validación al cambiar el texto
                                    if (validationResult.nameError != null || validationResult.duplicateError != null) {
                                        validationResult = validationResult.copy(
                                            nameError = null,
                                            duplicateError = null
                                        )
                                    }
                                },
                                focusManager = focusManager
                            )
                        }
                        SalonFormStep.CONFIRMATION -> {
                            SalonConfirmationContent(
                                name = name,
                                formState = formState,
                                isEditing = isEditing
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationButtons(
                        currentStep = currentStep,
                        formState = formState,
                        onDismiss = onDismiss,
                        onPrevious = { currentStep = SalonFormStep.FORM },
                        onNext = { proceedToNext() },
                        isEditing = isEditing
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
private fun FormHeader(currentStep: SalonFormStep, isEditing: Boolean) {
    val animatedProgress by animateFloatAsState(
        targetValue = when (currentStep) {
            SalonFormStep.FORM -> 0.5f
            SalonFormStep.CONFIRMATION -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "progress_animation"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isEditing) "Editar Salón" else "Registro de Salón",
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
                SalonFormStep.FORM -> "Paso 1: Información del Salón"
                SalonFormStep.CONFIRMATION -> "Paso 2: Confirmación"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalonFormContent(
    name: String,
    validationResult: SalonValidationResult,
    formState: SalonFormState,
    onNameChange: (String) -> Unit,
    focusManager: FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre del Salón") },
            placeholder = { Text("Ej: Salón Principal, Aula 1, Gimnasio") },
            isError = validationResult.nameError != null || validationResult.duplicateError != null,
            supportingText = {
                (validationResult.nameError ?: validationResult.duplicateError)?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Campo para el nombre del salón"
                },
            singleLine = true,
            enabled = formState !is SalonFormState.Loading,
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

        // Preview
        if (name.isNotEmpty()) {
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
                        text = "Vista previa del salón:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Primary
                    )
                    Text(
                        text = "• Nombre: $name",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SalonConfirmationContent(
    name: String,
    formState: SalonFormState,
    isEditing: Boolean
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
                    text = if (isEditing) "Confirmar Edición de Salón" else "Confirmar Registro de Salón",
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nombre:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextColor,
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (formState is SalonFormState.Error) {
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
    currentStep: SalonFormStep,
    formState: SalonFormState,
    onDismiss: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isEditing: Boolean
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
            enabled = formState !is SalonFormState.Loading,
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

        if (currentStep == SalonFormStep.CONFIRMATION) {
            Button(
                onClick = onPrevious,
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp),
                enabled = formState !is SalonFormState.Loading,
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
            enabled = formState !is SalonFormState.Loading,
            modifier = Modifier
                .width(110.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFff8abe),
                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (formState is SalonFormState.Loading) {
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
                    text = when {
                        currentStep == SalonFormStep.CONFIRMATION && isEditing -> "Actualizar"
                        currentStep == SalonFormStep.CONFIRMATION -> "Registrar"
                        else -> "Siguiente"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}