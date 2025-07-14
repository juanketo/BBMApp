package org.example.appbbmges.ui.diciplinashorarios.formclass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.delay

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
    val globalError: String? = null
)

class ClassValidator {
    companion object {
        fun validate(
            className: String,
            teacherName: String,
            studentsCount: String,
            price: String,
            description: String
        ): ClassValidationResult {
            var isValid = true
            var nameError: String? = null
            var teacherError: String? = null
            var studentsError: String? = null
            var priceError: String? = null
            var descriptionError: String? = null

            if (className.isBlank()) {
                nameError = "El nombre de la clase es obligatorio."
                isValid = false
            } else if (className.length < 3) {
                nameError = "Mínimo 3 caracteres."
                isValid = false
            }

            if (teacherName.isBlank()) {
                teacherError = "El profesor es obligatorio."
                isValid = false
            }

            val studentsNum = studentsCount.toIntOrNull()
            if (studentsCount.isBlank()) {
                studentsError = "El número de alumnos es obligatorio."
                isValid = false
            } else if (studentsNum == null || studentsNum <= 0) {
                studentsError = "Debe ser un número válido mayor a 0."
                isValid = false
            }

            val priceNum = price.toDoubleOrNull()
            if (price.isBlank()) {
                priceError = "El precio es obligatorio."
                isValid = false
            } else if (priceNum == null || priceNum < 0) {
                priceError = "Debe ser un número válido y no negativo."
                isValid = false
            }

            if (description.isBlank()) {
                descriptionError = "La descripción es obligatoria."
                isValid = false
            } else if (description.length < 10) {
                descriptionError = "Mínimo 10 caracteres."
                isValid = false
            }

            return ClassValidationResult(
                isValid = isValid,
                nameError = nameError,
                teacherError = teacherError,
                studentsError = studentsError,
                priceError = priceError,
                descriptionError = descriptionError
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewClassMuestra(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    var currentStep by remember { mutableStateOf(ClassFormStep.INFO) }
    var classFormState by remember { mutableStateOf<ClassFormState>(ClassFormState.Idle) }

    var className by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var studentsCount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    val tipoOptions = listOf("PRIVADA", "PÚBLICA")
    var selectedTipoExpanded by remember { mutableStateOf(false) }
    var selectedTipo by remember { mutableStateOf(tipoOptions[0]) }

    val ubicacionOptions = listOf("CLASE EN LÍNEA", "PRESENCIAL")
    var selectedUbicacionExpanded by remember { mutableStateOf(false) }
    var selectedUbicacion by remember { mutableStateOf(ubicacionOptions[0]) }

    val estadoOptions = listOf("PLANIFICADO", "ACTIVO")
    var selectedEstadoExpanded by remember { mutableStateOf(false) }
    var selectedEstado by remember { mutableStateOf(estadoOptions[0]) }

    val tipoClaseOptions = listOf("Intensivo", "Regular", "Taller")
    var selectedTipoClaseExpanded by remember { mutableStateOf(false) }
    var selectedTipoClase by remember { mutableStateOf(tipoClaseOptions[0]) }

    val estiloOptions = listOf("Ballet", "Hip-Hop", "Jazz", "Contemporáneo", "Flamenco")
    var selectedEstiloExpanded by remember { mutableStateOf(false) }
    var selectedEstilo by remember { mutableStateOf(estiloOptions[0]) }

    val nivelOptions = listOf("Principiante", "Intermedio", "Avanzado", "Profesional")
    var selectedNivelExpanded by remember { mutableStateOf(false) }
    var selectedNivel by remember { mutableStateOf(nivelOptions[0]) }

    var validationResult by remember { mutableStateOf(ClassValidationResult(true)) }

    fun validateForm(): Boolean {
        val result = ClassValidator.validate(
            className,
            teacherName,
            studentsCount,
            price,
            description
        )
        validationResult = result
        return result.isValid
    }

    @Composable
    fun proceedToNext() {
        when (currentStep) {
            ClassFormStep.INFO -> {
                if (validateForm()) {
                    currentStep = ClassFormStep.CONFIRMATION
                }
            }
            ClassFormStep.CONFIRMATION -> {
                classFormState = ClassFormState.Loading
                LaunchedEffect(Unit) {
                    try {
                        delay(1000)
                        classFormState = ClassFormState.Success
                        onDismiss()
                    } catch (e: Exception) {
                        classFormState = ClassFormState.Error("Error al registrar la clase: ${e.message}")
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
                        text = "Registro de Nueva Clase",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    when (currentStep) {
                        ClassFormStep.INFO -> {
                            OutlinedTextField(
                                value = className,
                                onValueChange = {
                                    className = it
                                    if (validationResult.nameError != null) validationResult = validationResult.copy(nameError = null)
                                },
                                label = { Text("Nombre de la Clase") },
                                isError = validationResult.nameError != null,
                                supportingText = { validationResult.nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            OutlinedTextField(
                                value = teacherName,
                                onValueChange = {
                                    teacherName = it
                                    if (validationResult.teacherError != null) validationResult = validationResult.copy(teacherError = null)
                                },
                                label = { Text("Profesor") },
                                isError = validationResult.teacherError != null,
                                supportingText = { validationResult.teacherError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            OutlinedTextField(
                                value = studentsCount,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                        studentsCount = newValue
                                        if (validationResult.studentsError != null) validationResult = validationResult.copy(studentsError = null)
                                    }
                                },
                                label = { Text("Alumnos") },
                                isError = validationResult.studentsError != null,
                                supportingText = { validationResult.studentsError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = {
                                    description = it
                                    if (validationResult.descriptionError != null) validationResult = validationResult.copy(descriptionError = null)
                                },
                                label = { Text("Descripción") },
                                isError = validationResult.descriptionError != null,
                                supportingText = { validationResult.descriptionError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            ExposedDropdownMenuBox(
                                expanded = selectedTipoExpanded,
                                onExpandedChange = { selectedTipoExpanded = !selectedTipoExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedTipo,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tipo") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedTipoExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = selectedTipoExpanded,
                                    onDismissRequest = { selectedTipoExpanded = false }
                                ) {
                                    tipoOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                selectedTipo = item
                                                selectedTipoExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = selectedUbicacionExpanded,
                                onExpandedChange = { selectedUbicacionExpanded = !selectedUbicacionExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedUbicacion,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Ubicación") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedUbicacionExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = selectedUbicacionExpanded,
                                    onDismissRequest = { selectedUbicacionExpanded = false }
                                ) {
                                    ubicacionOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                selectedUbicacion = item
                                                selectedUbicacionExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = price,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() || it == '.' } || newValue.isEmpty()) {
                                        price = newValue
                                        if (validationResult.priceError != null) validationResult = validationResult.copy(priceError = null)
                                    }
                                },
                                label = { Text("Precio ($)") },
                                isError = validationResult.priceError != null,
                                supportingText = { validationResult.priceError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            ExposedDropdownMenuBox(
                                expanded = selectedEstadoExpanded,
                                onExpandedChange = { selectedEstadoExpanded = !selectedEstadoExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedEstado,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Estado") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedEstadoExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = selectedEstadoExpanded,
                                    onDismissRequest = { selectedEstadoExpanded = false }
                                ) {
                                    estadoOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                selectedEstado = item
                                                selectedEstadoExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = selectedTipoClaseExpanded,
                                onExpandedChange = { selectedTipoClaseExpanded = !selectedTipoClaseExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedTipoClase,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tipo de Clase") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedTipoClaseExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = selectedTipoClaseExpanded,
                                    onDismissRequest = { selectedTipoClaseExpanded = false }
                                ) {
                                    tipoClaseOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                selectedTipoClase = item
                                                selectedTipoClaseExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = selectedEstiloExpanded,
                                onExpandedChange = { selectedEstiloExpanded = !selectedEstiloExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedEstilo,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Estilo") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedEstiloExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = selectedEstiloExpanded,
                                    onDismissRequest = { selectedEstiloExpanded = false }
                                ) {
                                    estiloOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                selectedEstilo = item
                                                selectedEstiloExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = selectedNivelExpanded,
                                onExpandedChange = { selectedNivelExpanded = !selectedNivelExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedNivel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Nivel") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedNivelExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = selectedNivelExpanded,
                                    onDismissRequest = { selectedNivelExpanded = false }
                                ) {
                                    nivelOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                selectedNivel = item
                                                selectedNivelExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        ClassFormStep.CONFIRMATION -> {
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
                                        text = "Confirmar Registro de Clase",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Divider(color = AppColors.Primary.copy(alpha = 0.3f))

                                    ConfirmationItem(label = "Nombre:", value = className)
                                    ConfirmationItem(label = "Profesor:", value = teacherName)
                                    ConfirmationItem(label = "Alumnos:", value = studentsCount)
                                    ConfirmationItem(label = "Descripción:", value = description)
                                    ConfirmationItem(label = "Tipo:", value = selectedTipo)
                                    ConfirmationItem(label = "Ubicación:", value = selectedUbicacion)
                                    ConfirmationItem(label = "Precio:", value = "$$price")
                                    ConfirmationItem(label = "Estado:", value = selectedEstado)
                                    ConfirmationItem(label = "Tipo de Clase:", value = selectedTipoClase)
                                    ConfirmationItem(label = "Estilo:", value = selectedEstilo)
                                    ConfirmationItem(label = "Nivel:", value = selectedNivel)
                                }
                            }
                            Spacer(Modifier.height(16.dp))

                            if (classFormState is ClassFormState.Error) {
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
                                            text = (classFormState as ClassFormState.Error).message,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

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
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            enabled = classFormState !is ClassFormState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                disabledContainerColor = AppColors.Primary.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (classFormState is ClassFormState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    when (currentStep) {
                                        ClassFormStep.INFO -> "Siguiente"
                                        ClassFormStep.CONFIRMATION -> "Registrar"
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

@Composable
fun ConfirmationItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}