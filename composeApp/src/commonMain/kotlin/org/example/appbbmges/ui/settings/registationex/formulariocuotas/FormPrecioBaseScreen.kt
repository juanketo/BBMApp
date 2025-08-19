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
import org.example.appbbmges.PrecioBaseEntity
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.jetbrains.compose.resources.painterResource

// -----------------------------------------------
// Formulario para Crear/Editar Cuota (Precio Base)
// -----------------------------------------------
enum class CuotaFormStep { FORM, CONFIRMATION }

sealed class CuotaFormState {
    object Idle : CuotaFormState()
    object Loading : CuotaFormState()
    object Success : CuotaFormState()
    data class Error(val message: String) : CuotaFormState()
}

data class CuotaValidationResult(
    val isValid: Boolean,
    val priceError: String? = null
)

object CuotaValidator {
    fun validate(priceString: String): CuotaValidationResult {
        val priceVal = priceString.toDoubleOrNull()
        val priceErr = when {
            priceString.isBlank() -> "El precio es obligatorio"
            priceVal == null -> "Formato de precio inválido"
            priceVal <= 0.0 -> "El precio debe ser mayor a 0"
            else -> null
        }
        return CuotaValidationResult(
            isValid = priceErr == null,
            priceError = priceErr
        )
    }
}

@Composable
fun FormPrecioBaseScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    editingPrecio: PrecioBaseEntity? = null
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val isEditing = editingPrecio != null

    var currentStep by remember { mutableStateOf(CuotaFormStep.FORM) }
    var precio by remember { mutableStateOf(editingPrecio?.precio?.toString() ?: "") }
    var formState by remember { mutableStateOf<CuotaFormState>(CuotaFormState.Idle) }
    var validationResult by remember { mutableStateOf(CuotaValidationResult(true)) }

    fun validate(): Boolean {
        val r = CuotaValidator.validate(precio.trim())
        validationResult = r
        return r.isValid
    }

    fun proceedToNext() {
        when (currentStep) {
            CuotaFormStep.FORM -> {
                if (validate()) currentStep = CuotaFormStep.CONFIRMATION
            }
            CuotaFormStep.CONFIRMATION -> {
                formState = CuotaFormState.Loading
                coroutineScope.launch {
                    try {
                        if (isEditing) {
                            val id = editingPrecio!!.id
                            repository.updatePrecioBase(
                                id = id,
                                precio = precio.toDouble()
                            )
                        } else {
                            repository.insertPrecioBase(
                                precio = precio.toDouble()
                            )
                        }
                        formState = CuotaFormState.Success
                        onDismiss()
                    } catch (e: Exception) {
                        formState = CuotaFormState.Error("Error al ${if (isEditing) "actualizar" else "registrar"}: ${e.message}")
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
                            CuotaFormStep.FORM -> 0.5f
                            CuotaFormStep.CONFIRMATION -> 1f
                        },
                        animationSpec = tween(300),
                        label = "progress"
                    )

                    Text(
                        text = if (isEditing) "Editar Precio Base" else "Registrar Precio Base",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = AppColors.Primary)

                    Text(
                        text = when (currentStep) {
                            CuotaFormStep.FORM -> "Paso 1: Información"
                            CuotaFormStep.CONFIRMATION -> "Paso 2: Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when (currentStep) {
                        CuotaFormStep.FORM -> {
                            OutlinedTextField(
                                value = precio,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                        precio = newValue
                                        if (validationResult.priceError != null) {
                                            validationResult = validationResult.copy(priceError = null)
                                        }
                                    }
                                },
                                label = { Text("Precio *") },
                                prefix = { Text("$", color = AppColors.Primary) },
                                singleLine = true,
                                isError = validationResult.priceError != null,
                                supportingText = {
                                    validationResult.priceError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }

                        CuotaFormStep.CONFIRMATION -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "Confirmar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Divider()
                                    Text("Precio: $$precio", fontWeight = FontWeight.Medium)
                                }
                            }
                            if (formState is CuotaFormState.Error) {
                                Spacer(Modifier.height(12.dp))
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                    Text((formState as CuotaFormState.Error).message, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onDismiss() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff8abe))) {
                            Text("Cancelar")
                        }
                        if (currentStep == CuotaFormStep.CONFIRMATION) {
                            Button(onClick = { currentStep = CuotaFormStep.FORM }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff8abe))) {
                                Text("Atrás")
                            }
                        }
                        Button(onClick = { proceedToNext() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)) {
                            if (formState is CuotaFormState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(if (currentStep == CuotaFormStep.FORM) "Siguiente" else if (isEditing) "Actualizar" else "Registrar")
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