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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.jetbrains.compose.resources.painterResource

data class ProductoData(
    val description: String,
    val code: String,
    val line: String?,
    val franchisePrice: String?,
    val suggestedPrice: String?,
    val country: String?
)

enum class ProductoFormStep {
    INFO,
    CONFIRMATION
}

sealed class ProductoFormState {
    object Idle : ProductoFormState()
    object Loading : ProductoFormState()
    data class Error(val message: String) : ProductoFormState()
    object Success : ProductoFormState()
}

data class ProductoValidationResult(
    val isValid: Boolean,
    val descriptionError: String? = null,
    val codeError: String? = null,
    val lineError: String? = null,
    val franchisePriceError: String? = null,
    val suggestedPriceError: String? = null,
    val countryError: String? = null
)

class ProductoValidator {
    companion object {
        fun validateProducto(
            data: ProductoData,
            existingCode: String?,
            countries: List<String>
        ): ProductoValidationResult {
            val descriptionError = when {
                data.description.isEmpty() -> "La descripción es obligatoria"
                data.description.length < 3 -> "La descripción debe tener al menos 3 caracteres"
                data.description.length > 100 -> "La descripción no puede exceder 100 caracteres"
                !data.description.matches(Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]+$")) -> "La descripción solo puede contener letras, números y espacios"
                data.description.trim() != data.description -> "La descripción no puede empezar o terminar con espacios"
                data.description.contains(Regex("\\s{2,}")) -> "La descripción no puede contener espacios consecutivos"
                else -> null
            }

            val codeError = when {
                data.code.isEmpty() -> "El código es obligatorio"
                data.code.length < 3 -> "El código debe tener al menos 3 caracteres"
                data.code.length > 20 -> "El código no puede exceder 20 caracteres"
                !data.code.matches(Regex("^[a-zA-Z0-9]+$")) -> "El código solo puede contener letras y números"
                existingCode != null -> "El código '${data.code}' ya existe"
                else -> null
            }

            val lineError = when {
                data.line?.isNotEmpty() == true && data.line.length > 50 -> "La línea no puede exceder 50 caracteres"
                data.line?.isNotEmpty() == true && !data.line.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> "La línea solo puede contener letras y espacios"
                data.line?.isNotEmpty() == true && data.line.trim() != data.line -> "La línea no puede empezar o terminar con espacios"
                data.line?.isNotEmpty() == true && data.line.contains(Regex("\\s{2,}")) -> "La línea no puede contener espacios consecutivos"
                else -> null
            }

            val franchisePriceError = when {
                data.franchisePrice?.isNotEmpty() == true && data.franchisePrice.toDoubleOrNull() == null -> "El precio de franquicia debe ser un número válido"
                data.franchisePrice?.isNotEmpty() == true && (data.franchisePrice.toDoubleOrNull() ?: 0.0) <= 0 -> "El precio de franquicia debe ser mayor a 0"
                else -> null
            }

            val suggestedPriceError = when {
                data.suggestedPrice?.isNotEmpty() == true && data.suggestedPrice.toDoubleOrNull() == null -> "El precio sugerido debe ser un número válido"
                data.suggestedPrice?.isNotEmpty() == true && (data.suggestedPrice.toDoubleOrNull() ?: 0.0) <= 0 -> "El precio sugerido debe ser mayor a 0"
                else -> null
            }

            val countryError = when {
                data.country?.isNotEmpty() == true && data.country !in countries -> "El país seleccionado no es válido"
                else -> null
            }

            return ProductoValidationResult(
                isValid = descriptionError == null && codeError == null && lineError == null &&
                        franchisePriceError == null && suggestedPriceError == null && countryError == null,
                descriptionError = descriptionError,
                codeError = codeError,
                lineError = lineError,
                franchisePriceError = franchisePriceError,
                suggestedPriceError = suggestedPriceError,
                countryError = countryError
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductoScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // Estados del formulario
    var currentStep by remember { mutableStateOf(ProductoFormStep.INFO) }
    var description by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var line by remember { mutableStateOf("") }
    var franchisePrice by remember { mutableStateOf("") }
    var suggestedPrice by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var formState by remember { mutableStateOf<ProductoFormState>(ProductoFormState.Idle) }
    var validationResult by remember { mutableStateOf(ProductoValidationResult(true)) }
    var expanded by remember { mutableStateOf(false) }

    // Lista de países predefinida
    val countries = listOf(
        "México", "Estados Unidos", "Canadá", "Argentina", "Brasil", "Chile", "Colombia", "Perú",
        "España", "Francia", "Alemania", "Italia", "Reino Unido"
    )

    // Verificar código existente
    var existingCode by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(code) {
        if (code.isNotEmpty()) {
            val item = repository.getBoutiqueItemByCode(code)
            existingCode = if (item != null) code else null
        } else {
            existingCode = null
        }
    }

    // Función de validación
    fun validateForm(): Boolean {
        val data = ProductoData(
            description = description,
            code = code,
            line = line.takeIf { it.isNotEmpty() },
            franchisePrice = franchisePrice.takeIf { it.isNotEmpty() },
            suggestedPrice = suggestedPrice.takeIf { it.isNotEmpty() },
            country = country.takeIf { it.isNotEmpty() }
        )
        val validation = ProductoValidator.validateProducto(data, existingCode, countries)
        validationResult = validation
        return validation.isValid
    }

    // Función para proceder al siguiente paso
    fun proceedToNext() {
        when (currentStep) {
            ProductoFormStep.INFO -> {
                if (validateForm()) {
                    currentStep = ProductoFormStep.CONFIRMATION
                }
            }
            ProductoFormStep.CONFIRMATION -> {
                formState = ProductoFormState.Loading
                try {
                    repository.insertBoutiqueItem(
                        description = description.trim(),
                        code = code.trim(),
                        line = line.takeIf { it.isNotEmpty() }?.trim(),
                        franchisePrice = franchisePrice.toDoubleOrNull(),
                        suggestedPrice = suggestedPrice.toDoubleOrNull(),
                        country = country.takeIf { it.isNotEmpty() }?.trim()
                    )
                    formState = ProductoFormState.Success
                    onDismiss()
                } catch (e: Exception) {
                    formState = ProductoFormState.Error("Error al registrar el producto: ${e.message}")
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
                    FormHeader(currentStep)

                    Spacer(modifier = Modifier.height(12.dp))

                    when (currentStep) {
                        ProductoFormStep.INFO -> {
                            InfoContent(
                                description = description,
                                code = code,
                                line = line,
                                franchisePrice = franchisePrice,
                                suggestedPrice = suggestedPrice,
                                country = country,
                                validationResult = validationResult,
                                formState = formState,
                                countries = countries,
                                expanded = expanded,
                                onDescriptionChange = {
                                    description = it
                                    if (validationResult.descriptionError != null) {
                                        validationResult = validationResult.copy(descriptionError = null)
                                    }
                                },
                                onCodeChange = {
                                    code = it
                                    if (validationResult.codeError != null) {
                                        validationResult = validationResult.copy(codeError = null)
                                    }
                                },
                                onLineChange = {
                                    line = it
                                    if (validationResult.lineError != null) {
                                        validationResult = validationResult.copy(lineError = null)
                                    }
                                },
                                onFranchisePriceChange = {
                                    franchisePrice = it
                                    if (validationResult.franchisePriceError != null) {
                                        validationResult = validationResult.copy(franchisePriceError = null)
                                    }
                                },
                                onSuggestedPriceChange = {
                                    suggestedPrice = it
                                    if (validationResult.suggestedPriceError != null) {
                                        validationResult = validationResult.copy(suggestedPriceError = null)
                                    }
                                },
                                onCountryChange = {
                                    country = it
                                    if (validationResult.countryError != null) {
                                        validationResult = validationResult.copy(countryError = null)
                                    }
                                },
                                onExpandedChange = { expanded = it },
                                onProceedToNext = { proceedToNext() },
                                focusManager = focusManager
                            )
                        }
                        ProductoFormStep.CONFIRMATION -> {
                            ConfirmationContent(
                                data = ProductoData(
                                    description = description,
                                    code = code,
                                    line = line.takeIf { it.isNotEmpty() },
                                    franchisePrice = franchisePrice.takeIf { it.isNotEmpty() },
                                    suggestedPrice = suggestedPrice.takeIf { it.isNotEmpty() },
                                    country = country.takeIf { it.isNotEmpty() }
                                ),
                                formState = formState
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationButtons(
                        currentStep = currentStep,
                        formState = formState,
                        onDismiss = onDismiss,
                        onPrevious = { currentStep = ProductoFormStep.INFO },
                        onNext = { proceedToNext() }
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
private fun FormHeader(currentStep: ProductoFormStep) {
    val animatedProgress by animateFloatAsState(
        targetValue = when (currentStep) {
            ProductoFormStep.INFO -> 0.5f
            ProductoFormStep.CONFIRMATION -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "progress_animation"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Registro de Producto",
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
                ProductoFormStep.INFO -> "Paso 1: Información"
                ProductoFormStep.CONFIRMATION -> "Paso 2: Confirmación"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoContent(
    description: String,
    code: String,
    line: String,
    franchisePrice: String,
    suggestedPrice: String,
    country: String,
    validationResult: ProductoValidationResult,
    formState: ProductoFormState,
    countries: List<String>,
    expanded: Boolean,
    onDescriptionChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onLineChange: (String) -> Unit,
    onFranchisePriceChange: (String) -> Unit,
    onSuggestedPriceChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onProceedToNext: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción") },
            placeholder = { Text("Ej: Camiseta de entrenamiento") },
            isError = validationResult.descriptionError != null,
            supportingText = {
                validationResult.descriptionError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Campo para la descripción" },
            singleLine = true,
            enabled = formState !is ProductoFormState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary
            )
        )

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text("Código") },
            placeholder = { Text("Ej: CAM001") },
            isError = validationResult.codeError != null,
            supportingText = {
                validationResult.codeError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Campo para el código" },
            singleLine = true,
            enabled = formState !is ProductoFormState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary
            )
        )

        OutlinedTextField(
            value = line,
            onValueChange = onLineChange,
            label = { Text("Línea (Opcional)") },
            placeholder = { Text("Ej: Ropa deportiva") },
            isError = validationResult.lineError != null,
            supportingText = {
                validationResult.lineError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Campo para la línea" },
            singleLine = true,
            enabled = formState !is ProductoFormState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary
            )
        )

        OutlinedTextField(
            value = franchisePrice,
            onValueChange = onFranchisePriceChange,
            label = { Text("Precio Franquicia (Opcional)") },
            placeholder = { Text("Ej: 100.00") },
            isError = validationResult.franchisePriceError != null,
            supportingText = {
                validationResult.franchisePriceError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Campo para el precio de franquicia" },
            singleLine = true,
            enabled = formState !is ProductoFormState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary
            )
        )

        OutlinedTextField(
            value = suggestedPrice,
            onValueChange = onSuggestedPriceChange,
            label = { Text("Precio Sugerido (Opcional)") },
            placeholder = { Text("Ej: 150.00") },
            isError = validationResult.suggestedPriceError != null,
            supportingText = {
                validationResult.suggestedPriceError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Campo para el precio sugerido" },
            singleLine = true,
            enabled = formState !is ProductoFormState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary
            )
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = country.takeIf { it.isNotEmpty() }?.let { "País $it" } ?: "Seleccionar país (opcional)",
                    onValueChange = {},
                    label = { Text("País (Opcional)") },
                    isError = validationResult.countryError != null,
                    supportingText = {
                        validationResult.countryError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .semantics { contentDescription = "Campo para seleccionar país" },
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        focusedLabelColor = AppColors.Primary
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onProceedToNext()
                        }
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .exposedDropdownSize()
                ) {
                    DropdownMenuItem(
                        text = { Text("Ninguno", style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onCountryChange("")
                            onExpandedChange(false)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    countries.forEach { countryOption ->
                        DropdownMenuItem(
                            text = { Text("País $countryOption", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onCountryChange(countryOption)
                                onExpandedChange(false)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationContent(
    data: ProductoData,
    formState: ProductoFormState
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Confirmar Datos del Producto",
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Descripción:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = data.description.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Código:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = data.code.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Primary
                    )
                }

                data.line?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Línea:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = it.trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Primary
                        )
                    }
                }

                data.franchisePrice?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Precio Franquicia:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Primary
                        )
                    }
                }

                data.suggestedPrice?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Precio Sugerido:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Primary
                        )
                    }
                }

                data.country?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "País:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Primary
                        )
                    }
                }
            }
        }

        if (formState is ProductoFormState.Error) {
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
    currentStep: ProductoFormStep,
    formState: ProductoFormState,
    onDismiss: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
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
            enabled = formState !is ProductoFormState.Loading,
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

        if (currentStep == ProductoFormStep.CONFIRMATION) {
            Button(
                onClick = onPrevious,
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp),
                enabled = formState !is ProductoFormState.Loading,
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
            enabled = formState !is ProductoFormState.Loading,
            modifier = Modifier
                .width(110.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFff8abe),
                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (formState is ProductoFormState.Loading) {
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
                    text = if (currentStep == ProductoFormStep.CONFIRMATION) "Registrar" else "Siguiente",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}