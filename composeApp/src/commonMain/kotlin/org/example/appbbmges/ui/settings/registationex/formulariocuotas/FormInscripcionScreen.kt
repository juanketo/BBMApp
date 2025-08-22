package org.example.appbbmges.ui.settings.registationex.formulariocuotas

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.appbbmges.InscriptionEntity
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.jetbrains.compose.resources.painterResource

enum class InscripcionFormStep { FORM, CONFIRMATION }

sealed class InscripcionFormState {
    object Idle : InscripcionFormState()
    object Loading : InscripcionFormState()
    object Success : InscripcionFormState()
    data class Error(val message: String) : InscripcionFormState()
}

data class InscripcionValidationResult(
    val isValid: Boolean,
    val precioError: String? = null
)

object InscripcionValidator {
    fun validate(precioString: String): InscripcionValidationResult {
        val precioVal = precioString.toDoubleOrNull()
        val precioErr = when {
            precioString.isBlank() -> "El precio es obligatorio"
            precioVal == null -> "Formato de precio inválido"
            precioVal <= 0.0 -> "El precio debe ser mayor a 0"
            else -> null
        }
        return InscripcionValidationResult(
            isValid = precioErr == null,
            precioError = precioErr
        )
    }
}

@Composable
fun FormInscripcionScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    editingInscripcion: InscriptionEntity? = null
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val isEditing = editingInscripcion != null

    var currentStep by remember { mutableStateOf(InscripcionFormStep.FORM) }
    var precio by remember { mutableStateOf(editingInscripcion?.precio?.toString() ?: "") }
    var formState by remember { mutableStateOf<InscripcionFormState>(InscripcionFormState.Idle) }
    var validationResult by remember { mutableStateOf(InscripcionValidationResult(true)) }

    fun validate(): Boolean {
        val r = InscripcionValidator.validate(precio.trim())
        validationResult = r
        return r.isValid
    }

    fun proceedToNext() {
        when (currentStep) {
            InscripcionFormStep.FORM -> {
                if (validate()) currentStep = InscripcionFormStep.CONFIRMATION
            }
            InscripcionFormStep.CONFIRMATION -> {
                formState = InscripcionFormState.Loading
                coroutineScope.launch {
                    try {
                        if (isEditing) {
                            val id = editingInscripcion!!.id
                            repository.updateInscription(
                                id = id,
                                precio = precio.toDouble()
                            )
                        } else {
                            repository.insertInscription(
                                precio = precio.toDouble()
                            )
                        }
                        formState = InscripcionFormState.Success
                        onDismiss()
                    } catch (e: Exception) {
                        formState = InscripcionFormState.Error("Error al ${if (isEditing) "actualizar" else "registrar"}: ${e.message}")
                        println("Repo error: ${e.message}")
                    }
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
                .widthIn(min = 360.dp, max = 560.dp)
                .heightIn(max = 760.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundLogo()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val progress by animateFloatAsState(
                        targetValue = when (currentStep) {
                            InscripcionFormStep.FORM -> 0.5f
                            InscripcionFormStep.CONFIRMATION -> 1f
                        },
                        animationSpec = tween(300),
                        label = "progress"
                    )

                    Text(
                        text = if (isEditing) "Editar Inscripción" else "Registrar Inscripción",
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
                            InscripcionFormStep.FORM -> "Paso 1: Información"
                            InscripcionFormStep.CONFIRMATION -> "Paso 2: Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when (currentStep) {
                        InscripcionFormStep.FORM -> {
                            OutlinedTextField(
                                value = precio,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                        precio = newValue
                                        if (validationResult.precioError != null) {
                                            validationResult = validationResult.copy(precioError = null)
                                        }
                                    }
                                },
                                label = { Text("Precio de Inscripción *") },
                                prefix = { Text("$", color = AppColors.Primary) },
                                singleLine = true,
                                isError = validationResult.precioError != null,
                                supportingText = {
                                    validationResult.precioError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        InscripcionFormStep.CONFIRMATION -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Confirmar Inscripción",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Divider()
                                    Text("Precio: $$precio", fontWeight = FontWeight.Medium)
                                }
                            }
                            if (formState is InscripcionFormState.Error) {
                                Spacer(Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Text(
                                        (formState as InscripcionFormState.Error).message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onDismiss() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff8abe))
                        ) {
                            Text("Cancelar")
                        }
                        if (currentStep == InscripcionFormStep.CONFIRMATION) {
                            Button(
                                onClick = { currentStep = InscripcionFormStep.FORM },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff8abe))
                            ) {
                                Text("Atrás")
                            }
                        }
                        Button(
                            onClick = { proceedToNext() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                        ) {
                            if (formState is InscripcionFormState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    if (currentStep == InscripcionFormStep.FORM) "Siguiente"
                                    else if (isEditing) "Actualizar"
                                    else "Registrar"
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
            contentDescription = "Logo de fondo",
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                .semantics { contentDescription = "Logo de fondo de la aplicación" }
                .alpha(0.08f),
            contentScale = ContentScale.Fit
        )
    }
}