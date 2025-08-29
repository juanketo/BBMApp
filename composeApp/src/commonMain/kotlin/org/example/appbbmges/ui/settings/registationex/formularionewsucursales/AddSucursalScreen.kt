package org.example.appbbmges.ui.settings.registationex.formularionewsucursales

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSucursalScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val formManager = rememberSucursalFormManager(repository, onDismiss)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 400.dp, max = 600.dp)
                .heightIn(max = 800.dp)
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
                    FormHeader(formManager)
                    Spacer(Modifier.height(16.dp))

                    FormContent(formManager, focusManager)

                    Spacer(Modifier.weight(1f))

                    FormActionButtons(formManager, onDismiss)
                }
            }
        }
    }
}

@Composable
private fun FormHeader(formManager: SucursalFormManager) {
    val progress by animateFloatAsState(
        targetValue = formManager.getProgress(),
        animationSpec = tween(300),
        label = "progress"
    )

    Text(
        text = "Registro de Sucursal",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = formManager.getCurrentStepTitle(),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun FormContent(
    formManager: SucursalFormManager,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    when (formManager.currentStep) {
        SucursalFormStep.PERSONAL_INFO -> {
            PersonalInfoStep(
                name = formManager.formData.name,
                email = formManager.formData.email,
                phone = formManager.formData.phone,
                validationResult = formManager.validationResult,
                onNameChange = formManager::updateName,
                onEmailChange = formManager::updateEmail,
                onPhoneChange = formManager::updatePhone,
                focusManager = focusManager
            )
        }

        SucursalFormStep.DETAIL_INFO -> {
            DetailInfoStep(
                basePrice = formManager.getEffectiveBasePrice(),
                currency = formManager.formData.currency,
                validationResult = formManager.validationResult,
                onBasePriceChange = formManager::updateBasePrice,
                onCurrencyChange = formManager::updateCurrency,
                focusManager = focusManager
            )
        }

        SucursalFormStep.ADDRESS_INFO -> {
            AddressInfoStep(
                street = formManager.formData.addressStreet,
                number = formManager.formData.addressNumber,
                neighborhood = formManager.formData.addressNeighborhood,
                zip = formManager.formData.addressZip,
                city = formManager.formData.addressCity,
                country = formManager.formData.addressCountry,
                validationResult = formManager.validationResult,
                onStreetChange = formManager::updateAddressStreet,
                onNumberChange = formManager::updateAddressNumber,
                onNeighborhoodChange = formManager::updateAddressNeighborhood,
                onZipChange = formManager::updateAddressZip,
                onCityChange = formManager::updateAddressCity,
                onCountryChange = formManager::updateAddressCountry,
                focusManager = focusManager
            )
        }

        SucursalFormStep.ADDITIONAL_INFO -> {
            AdditionalInfoStep(
                taxName = formManager.formData.taxName,
                taxId = formManager.formData.taxId,
                zone = formManager.formData.zone,
                zonas = SucursalConstants.ZONAS,
                expandedZonas = formManager.expandedZonas,
                isNew = formManager.formData.isNew,
                active = formManager.formData.active,
                onTaxNameChange = formManager::updateTaxName,
                onTaxIdChange = formManager::updateTaxId,
                onZoneChange = formManager::updateZone,
                onExpandedZonasChange = formManager::updateExpandedZonas,
                onIsNewChange = formManager::updateIsNew,
                onActiveChange = formManager::updateActive,
                validationResult = formManager.validationResult
            )
        }

        SucursalFormStep.CONFIRMATION -> {
            ConfirmationStep(
                formData = formManager.formData,
                formState = formManager.formState
            )
        }
    }
}

@Composable
private fun FormActionButtons(
    formManager: SucursalFormManager,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            text = "Cancelar",
            onClick = onDismiss,
            enabled = !formManager.isLoading(),
            color = Color(0xFFff8abe),
            modifier = Modifier.weight(1f)
        )

        if (formManager.canGoBack()) {
            ActionButton(
                text = "Atrás",
                onClick = formManager::proceedBack,
                enabled = !formManager.isLoading(),
                color = Color(0xFFff8abe),
                modifier = Modifier.weight(1f)
            )
        }

        // Botón Siguiente/Registrar
        ActionButton(
            text = formManager.getActionButtonText(),
            onClick = formManager::proceedToNext,
            enabled = !formManager.isLoading(),
            color = AppColors.Primary,
            isLoading = formManager.isLoading(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}

@Composable
private fun BackgroundLogo() {
    val painter = painterResource(Res.drawable.logoSystem)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                .alpha(0.08f),
            contentScale = ContentScale.Fit
        )
    }
}