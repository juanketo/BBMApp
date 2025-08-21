package org.example.appbbmges.ui.settings.registationex.formulariocuotas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.appbbmges.MembershipEntity
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.jetbrains.compose.resources.painterResource

enum class MembresiaFormStep { FORM, CONFIRMATION }

sealed class MembresiaFormState {
    object Idle : MembresiaFormState()
    object Loading : MembresiaFormState()
    object Success : MembresiaFormState()
    data class Error(val message: String) : MembresiaFormState()
}

data class MembresiaValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val monthsPaidError: String? = null,
    val monthsSavedError: String? = null
)

object MembresiaValidator {
    fun validate(name: String, monthsPaidString: String, monthsSavedString: String): MembresiaValidationResult {
        val monthsPaid = monthsPaidString.toIntOrNull()
        val monthsSaved = monthsSavedString.toDoubleOrNull()

        val nameErr = if (name.isBlank()) "El nombre es obligatorio" else null
        val monthsPaidErr = when {
            monthsPaidString.isBlank() -> "Los meses a pagar son obligatorios"
            monthsPaid == null || monthsPaid <= 0 -> "Debe ser un número entero mayor a 0"
            else -> null
        }
        val monthsSavedErr = when {
            monthsSavedString.isBlank() -> "El ahorro es obligatorio"
            monthsSaved == null || monthsSaved < 0 -> "Debe ser un número positivo o 0"
            else -> null
        }

        return MembresiaValidationResult(
            isValid = nameErr == null && monthsPaidErr == null && monthsSavedErr == null,
            nameError = nameErr,
            monthsPaidError = monthsPaidErr,
            monthsSavedError = monthsSavedErr
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormMembresiasScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    editingMembership: MembershipEntity? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val isEditing = editingMembership != null

    var currentStep by remember { mutableStateOf(MembresiaFormStep.FORM) }
    var name by remember { mutableStateOf(editingMembership?.name ?: "") }
    var monthsPaid by remember { mutableStateOf(editingMembership?.months_paid?.toString() ?: "") }
    var monthsSaved by remember { mutableStateOf(editingMembership?.months_saved?.toString() ?: "") }
    var formState by remember { mutableStateOf<MembresiaFormState>(MembresiaFormState.Idle) }
    var validationResult by remember { mutableStateOf(MembresiaValidationResult(true)) }

    fun validate(): Boolean {
        val r = MembresiaValidator.validate(name.trim(), monthsPaid.trim(), monthsSaved.trim())
        validationResult = r
        return r.isValid
    }

    fun proceedToNext() {
        when (currentStep) {
            MembresiaFormStep.FORM -> {
                if (validate()) currentStep = MembresiaFormStep.CONFIRMATION
            }
            MembresiaFormStep.CONFIRMATION -> {
                formState = MembresiaFormState.Loading
                coroutineScope.launch {
                    try {
                        if (isEditing) {
                            val id = editingMembership!!.id
                            repository.updateMembership(
                                id = id,
                                name = name.trim(),
                                monthsPaid = monthsPaid.toLong(),
                                monthsSaved = monthsSaved.toDouble()
                            )
                        } else {
                            repository.insertMembership(
                                name = name.trim(),
                                monthsPaid = monthsPaid.toLong(),
                                monthsSaved = monthsSaved.toDouble()
                            )
                        }
                        formState = MembresiaFormState.Success
                        onDismiss()
                    } catch (e: Exception) {
                        formState = MembresiaFormState.Error("Error al ${if (isEditing) "actualizar" else "registrar"}: ${e.message}")
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
                            MembresiaFormStep.FORM -> 0.5f
                            MembresiaFormStep.CONFIRMATION -> 1f
                        },
                        animationSpec = tween(300),
                        label = "progress"
                    )

                    Text(
                        text = if (isEditing) "Editar Membresía" else "Registrar Membresía",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = AppColors.Primary)
                    Text(
                        text = when (currentStep) {
                            MembresiaFormStep.FORM -> "Paso 1: Información"
                            MembresiaFormStep.CONFIRMATION -> "Paso 2: Confirmación"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when (currentStep) {
                        MembresiaFormStep.FORM -> {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it.uppercase() },
                                label = { Text("Nombre") },
                                singleLine = true,
                                isError = validationResult.nameError != null,
                                supportingText = { validationResult.nameError?.let { Text(it) } }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = monthsPaid,
                                onValueChange = { monthsPaid = it },
                                label = { Text("Meses a Pagar") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                isError = validationResult.monthsPaidError != null,
                                supportingText = { validationResult.monthsPaidError?.let { Text(it) } }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = monthsSaved,
                                onValueChange = { monthsSaved = it },
                                label = { Text("Meses de Ahorro") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                isError = validationResult.monthsSavedError != null,
                                supportingText = { validationResult.monthsSavedError?.let { Text(it) } }
                            )
                        }

                        MembresiaFormStep.CONFIRMATION -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "Confirmar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Divider()
                                    Text("Nombre: $name", fontWeight = FontWeight.Medium)
                                    Text("Meses a Pagar: $monthsPaid", fontWeight = FontWeight.Medium)
                                    Text("Meses de Ahorro: $monthsSaved", fontWeight = FontWeight.Medium)
                                }
                            }
                            if (formState is MembresiaFormState.Error) {
                                Spacer(Modifier.height(12.dp))
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                    Text((formState as MembresiaFormState.Error).message, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onDismiss() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff8abe))) {
                            Text("Cancelar")
                        }
                        if (currentStep == MembresiaFormStep.CONFIRMATION) {
                            Button(onClick = { currentStep = MembresiaFormStep.FORM }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff8abe))) {
                                Text("Atrás")
                            }
                        }
                        Button(onClick = { proceedToNext() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)) {
                            if (formState is MembresiaFormState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(if (currentStep == MembresiaFormStep.FORM) "Siguiente" else if (isEditing) "Actualizar" else "Registrar")
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