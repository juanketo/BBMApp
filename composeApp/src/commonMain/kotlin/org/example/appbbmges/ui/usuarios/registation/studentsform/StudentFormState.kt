package org.example.appbbmges.ui.usuarios.registation.studentsform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

@Composable
fun rememberStudentFormState(): StudentFormState {
    return remember { StudentFormState() }
}

class StudentFormState {
    var data by mutableStateOf(StudentFormData())
        private set
    var errors by mutableStateOf(FormErrors())
        private set
    var currentStep by mutableStateOf(StudentFormStep.PERSONAL_INFO)
        private set

    fun updateData(newData: StudentFormData) {
        data = newData
        errors = errors.copy(general = null)
    }

    fun validateCurrentStep(): Boolean {
        errors = when (currentStep) {
            StudentFormStep.PERSONAL_INFO -> FormValidation.validatePersonalInfo(data)
            StudentFormStep.ADDRESS_INFO -> FormValidation.validateAddressInfo(data)
            else -> FormErrors()
        }
        return errors.isValid
    }

    fun setGeneralError(error: String?) {
        errors = errors.copy(general = error)
    }

    fun nextStep(): Boolean {
        if (validateCurrentStep()) {
            currentStep = when (currentStep) {
                StudentFormStep.PERSONAL_INFO -> StudentFormStep.ADDRESS_INFO
                StudentFormStep.ADDRESS_INFO -> StudentFormStep.ADDITIONAL_INFO
                StudentFormStep.ADDITIONAL_INFO -> StudentFormStep.CONFIRMATION
                StudentFormStep.CONFIRMATION -> currentStep
            }
            return true
        }
        return false
    }

    fun previousStep() {
        currentStep = when (currentStep) {
            StudentFormStep.ADDRESS_INFO -> StudentFormStep.PERSONAL_INFO
            StudentFormStep.ADDITIONAL_INFO -> StudentFormStep.ADDRESS_INFO
            StudentFormStep.CONFIRMATION -> StudentFormStep.ADDITIONAL_INFO
            StudentFormStep.PERSONAL_INFO -> currentStep
        }
    }
}