package org.example.appbbmges.ui.usuarios.registation.studentsform

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun AddAlumnoScreen(
    onDismiss: () -> Unit,
    repository: Repository
) {
    val state = rememberStudentFormState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logoSystem),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(0.7f),
                        alpha = 0.1f,
                        contentScale = ContentScale.Fit
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Registro de Alumno",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.TextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    FormProgressIndicator(
                        currentStep = state.currentStep,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    when (state.currentStep) {
                        StudentFormStep.PERSONAL_INFO -> PersonalInfoStep(
                            data = state.data,
                            errors = state.errors,
                            onDataChange = { state.updateData(it) }
                        )
                        StudentFormStep.ADDRESS_INFO -> AddressInfoStep(
                            data = state.data,
                            errors = state.errors,
                            onDataChange = { state.updateData(it) }
                        )
                        StudentFormStep.ADDITIONAL_INFO -> AdditionalInfoStep(
                            data = state.data,
                            onDataChange = { state.updateData(it) }
                        )
                        StudentFormStep.CONFIRMATION -> ConfirmationStep(
                            data = state.data,
                            errors = state.errors
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    FormNavigationButtons(
                        currentStep = state.currentStep,
                        onCancel = onDismiss,
                        onPrevious = { state.previousStep() },
                        onNext = { state.nextStep() },
                        onSubmit = {
                            if (state.validateCurrentStep()) {
                                try {
                                    repository.insertStudent(
                                        franchiseId = 0L,
                                        firstName = state.data.firstName,
                                        lastNamePaternal = state.data.lastNamePaternal.takeIf { it.isNotEmpty() },
                                        lastNameMaternal = state.data.lastNameMaternal.takeIf { it.isNotEmpty() },
                                        gender = state.data.gender.takeIf { it.isNotEmpty() },
                                        birthDate = state.data.birthDate.takeIf { it.isNotEmpty() },
                                        nationality = state.data.nationality.takeIf { it.isNotEmpty() },
                                        curp = state.data.curp.takeIf { it.isNotEmpty() },
                                        phone = state.data.phone.takeIf { it.isNotEmpty() },
                                        email = state.data.email.takeIf { it.isNotEmpty() },
                                        addressStreet = state.data.addressStreet.takeIf { it.isNotEmpty() },
                                        addressZip = state.data.addressZip.takeIf { it.isNotEmpty() },
                                        parentFatherFirstName = state.data.parentFatherName.takeIf { it.isNotEmpty() },
                                        parentFatherLastNamePaternal = null,
                                        parentFatherLastNameMaternal = null,
                                        parentMotherFirstName = state.data.parentMotherName.takeIf { it.isNotEmpty() },
                                        parentMotherLastNamePaternal = null,
                                        parentMotherLastNameMaternal = null,
                                        bloodType = state.data.bloodType.takeIf { it.isNotEmpty() },
                                        chronicDisease = state.data.chronicDisease.takeIf { it.isNotEmpty() },
                                        active = if (state.data.active) 1L else 0L
                                    )
                                    onDismiss()
                                } catch (e: Exception) {
                                    state.setGeneralError("Error al guardar los datos: ${e.message}")
                                }
                            } else {
                                state.setGeneralError("Revise el formulario. Hay campos obligatorios sin completar.")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
            }
        }
    }
}