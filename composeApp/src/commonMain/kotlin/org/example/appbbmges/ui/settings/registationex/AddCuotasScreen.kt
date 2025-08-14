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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
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
import org.example.appbbmges.FranchiseEntity
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.example.appbbmges.BasePriceWithFranchise

// --- Modelos de datos y utilidades ---

enum class BasePriceFormStep {
    FORM,
    CONFIRMATION
}

sealed class BasePriceFormState {
    object Idle : BasePriceFormState()
    object Loading : BasePriceFormState()
    data class Error(val message: String) : BasePriceFormState()
    object Success : BasePriceFormState()
}

data class BasePriceValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val priceError: String? = null,
    val franchiseError: String? = null,
    val duplicateError: String? = null
)

class BasePriceValidator {
    companion object {
        fun validate(
            name: String,
            price: String,
            selectedFranchiseId: Long?,
            existingPrices: List<BasePriceWithFranchise>,
            excludeId: Long? = null
        ): BasePriceValidationResult {
            val priceDouble = price.toDoubleOrNull()
            val nameError = when {
                name.isEmpty() -> "El nombre es obligatorio"
                name.length < 3 -> "Mínimo 3 caracteres"
                name.length > 50 -> "Máximo 50 caracteres"
                else -> null
            }

            val priceError = when {
                price.isEmpty() -> "El precio es obligatorio"
                priceDouble == null || priceDouble <= 0 -> "Debe ser un número mayor que 0"
                else -> null
            }

            val franchiseError = if (selectedFranchiseId == null) "Debes seleccionar una sucursal" else null

            val duplicateError = if (existingPrices.any {
                    it.name.equals(name, ignoreCase = true) && it.franchiseId == selectedFranchiseId && it.id != excludeId
                }) {
                "Ya existe un precio base con este nombre en la sucursal seleccionada"
            } else null

            return BasePriceValidationResult(
                isValid = nameError == null && priceError == null && franchiseError == null && duplicateError == null,
                nameError = nameError,
                priceError = priceError,
                franchiseError = franchiseError,
                duplicateError = duplicateError
            )
        }
    }
}

// --- Composable Principal ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCuotasScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    var showForm by remember { mutableStateOf(false) }
    var editingPrice by remember { mutableStateOf<BasePriceWithFranchise?>(null) }
    var showDeleteDialog by remember { mutableStateOf<BasePriceWithFranchise?>(null) }
    var existingPrices by remember { mutableStateOf<List<BasePriceWithFranchise>>(emptyList()) }
    var existingFranchises by remember { mutableStateOf<List<FranchiseEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    fun reloadPricesAndFranchises() {
        isLoading = true
        try {
            existingPrices = repository.getAllBasePricesWithFranchise()
            existingFranchises = repository.getAllFranchises()
        } catch (e: Exception) {
            println("Error recargando precios y sucursales: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        reloadPricesAndFranchises()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Formulario para agregar/editar
        if (showForm || editingPrice != null) {
            BasePriceFormDialog(
                onDismiss = {
                    showForm = false
                    editingPrice = null
                    reloadPricesAndFranchises()
                },
                repository = repository,
                existingPrices = existingPrices,
                editingPrice = editingPrice,
                existingFranchises = existingFranchises,
                snackbarHostState = snackbarHostState
            )
        }

        // Diálogo de confirmación para eliminar
        showDeleteDialog?.let { price ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Eliminar Precio Base") },
                text = { Text("¿Estás seguro de que deseas eliminar el precio base '${price.name}' de la sucursal '${price.franchiseName}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            try {
                                repository.deleteBasePrice(price.id)
                                reloadPricesAndFranchises()
                                showDeleteDialog = null
                            } catch (e: Exception) {
                                println("Error al eliminar precio base: ${e.message}")
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

        // Vista principal de la lista de precios
        if (!showForm && editingPrice == null && showDeleteDialog == null) {
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
                            text = "Precios Base de Sucursales",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextColor
                        )
                        Text(
                            text = "${existingPrices.size} precios registrados",
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

                // Contenido principal (Tabla de precios o mensaje de vacío)
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                } else if (existingPrices.isEmpty()) {
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
                                imageVector = Icons.Outlined.Payments,
                                contentDescription = "Sin precios",
                                modifier = Modifier.size(64.dp),
                                tint = AppColors.Primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay cuotas disponibles de las unidades",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextColor,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Añade una nueva",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AppColors.Primary.copy(alpha = 0.1f))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Nombre Sucursal",
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Precio Base",
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary,
                                        modifier = Modifier.weight(1f)
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

                            items(existingPrices) { price ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = price.franchiseName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$ ${"%.2f".format(price.price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = { editingPrice = price },
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
                                            onClick = { showDeleteDialog = price },
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
                                if (price != existingPrices.last()) {
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
                onClick = {
                    if (existingFranchises.isEmpty()) {
                        // Muestra un mensaje si no hay sucursales para asociar
                        // En un futuro, podrías usar un Snackbar para mostrar esto
                        println("No hay sucursales para asociar un precio base. Por favor, crea una sucursal primero.")
                    } else {
                        showForm = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = AppColors.Primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Agregar precio base"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasePriceFormDialog(
    onDismiss: () -> Unit,
    repository: Repository,
    existingPrices: List<BasePriceWithFranchise>,
    editingPrice: BasePriceWithFranchise? = null,
    existingFranchises: List<FranchiseEntity>,
    snackbarHostState: SnackbarHostState
) {
    val focusManager = LocalFocusManager.current
    val isEditing = editingPrice != null

    // Estados del formulario
    var currentStep by remember { mutableStateOf(BasePriceFormStep.FORM) }
    var name by remember { mutableStateOf(editingPrice?.name ?: "") }
    var priceText by remember { mutableStateOf(editingPrice?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(editingPrice?.description ?: "") }
    var formState by remember { mutableStateOf<BasePriceFormState>(BasePriceFormState.Idle) }
    var validationResult by remember { mutableStateOf(BasePriceValidationResult(true)) }

    // Estados para el selector de franquicias
    var expanded by remember { mutableStateOf(false) }
    var selectedFranchiseId by remember { mutableStateOf(editingPrice?.franchiseId) }
    val selectedFranchiseName = existingFranchises.find { it.id == selectedFranchiseId }?.name ?: "Selecciona una sucursal"


    // Función de validación
    fun validateForm(): Boolean {
        val validation = BasePriceValidator.validate(
            name = name,
            price = priceText,
            selectedFranchiseId = selectedFranchiseId,
            existingPrices = existingPrices,
            excludeId = editingPrice?.id
        )
        validationResult = validation
        return validation.isValid
    }

    // Función para proceder al siguiente paso o guardar
    fun proceedToNext() {
        when (currentStep) {
            BasePriceFormStep.FORM -> {
                if (validateForm()) {
                    currentStep = BasePriceFormStep.CONFIRMATION
                }
            }
            BasePriceFormStep.CONFIRMATION -> {
                formState = BasePriceFormState.Loading
                try {
                    val franchiseIdToUse = selectedFranchiseId!! // Asumimos que la validación ya lo verificó
                    if (isEditing) {
                        repository.updateBasePrice(
                            id = editingPrice!!.id,
                            name = name,
                            price = priceText.toDouble(),
                            description = description.ifEmpty { null }
                        )
                    } else {
                        repository.insertBasePrice(
                            franchiseId = franchiseIdToUse,
                            name = name,
                            price = priceText.toDouble(),
                            description = description.ifEmpty { null }
                        )
                    }
                    formState = BasePriceFormState.Success
                    onDismiss()
                } catch (e: Exception) {
                    formState = BasePriceFormState.Error("Error al ${if (isEditing) "actualizar" else "registrar"} el precio: ${e.message}")
                    println("Error al ${if (isEditing) "actualizar" else "registrar"} el precio: ${e.message}")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 400.dp, max = 500.dp)
                .heightIn(max = 700.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.Center),
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
                        BasePriceFormStep.FORM -> {
                            BasePriceFormContent(
                                name = name,
                                priceText = priceText,
                                description = description,
                                validationResult = validationResult,
                                formState = formState,
                                existingFranchises = existingFranchises,
                                selectedFranchiseId = selectedFranchiseId,
                                onNameChange = {
                                    name = it
                                    validationResult = validationResult.copy(nameError = null, duplicateError = null)
                                },
                                onPriceChange = {
                                    priceText = it
                                    validationResult = validationResult.copy(priceError = null)
                                },
                                onDescriptionChange = { description = it },
                                onFranchiseSelected = { id ->
                                    selectedFranchiseId = id
                                    validationResult = validationResult.copy(franchiseError = null, duplicateError = null)
                                },
                                focusManager = focusManager
                            )
                        }
                        BasePriceFormStep.CONFIRMATION -> {
                            BasePriceConfirmationContent(
                                name = name,
                                price = priceText,
                                description = description,
                                franchiseName = selectedFranchiseName,
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
                        onPrevious = { currentStep = BasePriceFormStep.FORM },
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
private fun FormHeader(currentStep: BasePriceFormStep, isEditing: Boolean) {
    val animatedProgress by animateFloatAsState(
        targetValue = when (currentStep) {
            BasePriceFormStep.FORM -> 0.5f
            BasePriceFormStep.CONFIRMATION -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "progress_animation"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isEditing) "Editar Precio Base" else "Registro de Precio Base",
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
                BasePriceFormStep.FORM -> "Paso 1: Información del Precio"
                BasePriceFormStep.CONFIRMATION -> "Paso 2: Confirmación"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasePriceFormContent(
    name: String,
    priceText: String,
    description: String,
    validationResult: BasePriceValidationResult,
    formState: BasePriceFormState,
    existingFranchises: List<FranchiseEntity>,
    selectedFranchiseId: Long?,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onFranchiseSelected: (Long) -> Unit,
    focusManager: FocusManager
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selector de franquicias
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = existingFranchises.find { it.id == selectedFranchiseId }?.name ?: "Selecciona una sucursal",
                onValueChange = { },
                label = { Text("Sucursal") },
                isError = validationResult.franchiseError != null,
                supportingText = {
                    validationResult.franchiseError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                existingFranchises.forEach { franchise ->
                    DropdownMenuItem(
                        text = { Text(franchise.name) },
                        onClick = {
                            onFranchiseSelected(franchise.id)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        // Campos de texto para nombre, precio y descripción
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre del Precio") },
            placeholder = { Text("Ej: Cuota General, Descuento Hermanos") },
            isError = validationResult.nameError != null || validationResult.duplicateError != null,
            supportingText = {
                (validationResult.nameError ?: validationResult.duplicateError)?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = formState !is BasePriceFormState.Loading,
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
            value = priceText,
            onValueChange = onPriceChange,
            label = { Text("Monto") },
            placeholder = { Text("Ej: 500.00") },
            isError = validationResult.priceError != null,
            supportingText = {
                validationResult.priceError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = formState !is BasePriceFormState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
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
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción (opcional)") },
            placeholder = { Text("Ej: Precio regular por un estudiante") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            enabled = formState !is BasePriceFormState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary
            )
        )
    }
}

@Composable
private fun BasePriceConfirmationContent(
    name: String,
    price: String,
    description: String,
    franchiseName: String,
    formState: BasePriceFormState,
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
                    text = if (isEditing) "Confirmar Edición de Precio" else "Confirmar Registro de Precio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextColor
                )
                HorizontalDivider(
                    color = AppColors.Primary.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                InfoRow(label = "Sucursal:", value = franchiseName)
                InfoRow(label = "Nombre:", value = name)
                InfoRow(label = "Precio:", value = "$ ${"%.2f".format(price.toDoubleOrNull() ?: 0.0)}")
                if (description.isNotEmpty()) {
                    InfoRow(label = "Descripción:", value = description)
                }
            }
        }
        if (formState is BasePriceFormState.Error) {
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
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextColor,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NavigationButtons(
    currentStep: BasePriceFormStep,
    formState: BasePriceFormState,
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
            modifier = Modifier.weight(1f),
            enabled = formState !is BasePriceFormState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFff8abe),
                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cancelar", fontSize = 12.sp)
        }

        if (currentStep == BasePriceFormStep.CONFIRMATION) {
            Button(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                enabled = formState !is BasePriceFormState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFff8abe),
                    disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Atrás", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = formState !is BasePriceFormState.Loading,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFff8abe),
                disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (formState is BasePriceFormState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = when {
                        currentStep == BasePriceFormStep.CONFIRMATION && isEditing -> "Actualizar"
                        currentStep == BasePriceFormStep.CONFIRMATION -> "Registrar"
                        else -> "Siguiente"
                    },
                    fontSize = 12.sp,
                )
            }
        }
    }
}