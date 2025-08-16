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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.appbbmges.PrecioBaseSelectAll
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.jetbrains.compose.resources.painterResource

// -----------------------------------------------
// Pantalla principal: Lista de Cuotas (PrecioBase)
// -----------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCuotasScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var cuotas by remember { mutableStateOf<List<PrecioBaseSelectAll>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showForm by remember { mutableStateOf(false) }
    var editingCuota by remember { mutableStateOf<PrecioBaseSelectAll?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PrecioBaseSelectAll?>(null) }

    // Cargar cuotas al inicio
    LaunchedEffect(Unit) {
        try {
            cuotas = repository.getAllPreciosBase()
        } catch (e: Exception) {
            // Log simple — considera mostrar Snackbar/Toast en producción
            println("Error cargando precios base: ${e.message}")
            cuotas = emptyList()
        } finally {
            isLoading = false
        }
    }

    // Recargar
    fun reloadCuotas() {
        isLoading = true
        try {
            cuotas = repository.getAllPreciosBase()
        } catch (e: Exception) {
            println("Error recargando precios base: ${e.message}")
            cuotas = emptyList()
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cuotas / Precios base",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextColor
                    )
                    Text(
                        text = "${cuotas.size} registros",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextColor.copy(alpha = 0.7f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff8abe)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Volver", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contenido: loading / vacío / lista
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else if (cuotas.isEmpty()) {
                // Estado vacío (mensaje agradable y guía)
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Sin cuotas",
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.Primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no hay cuotas registradas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextColor
                        )
                        Text(
                            text = "Presiona + para agregar la primera cuota",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                // Lista en forma de "tabla" simple
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        // Header fila
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppColors.Primary.copy(alpha = 0.08f))
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
                                    text = "Precio",
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Acciones",
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Primary,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        items(cuotas) { cuota ->
                            // --- Ajusta las propiedades según cómo SQLDelight haya generado la fila ---
                            // Probables propiedades generadas por SQLDelight:
                            // cuota.id, cuota.nombre, cuota.precio_costo, cuota.descripcion, cuota.activo, cuota.fecha_creacion
                            // Si tus propiedades difieren, cámbialas aquí (ej: cuota.precioCosto).
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cuota.nombre,
                                    modifier = Modifier.weight(3f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppColors.TextColor
                                )

                                // precio: si tu property es `precio_costo` o `precioCosto`, ajusta aquí
                                Text(
                                    text = "${cuota.precio_costo}", // <-- si falla, cambia a cuota.precioCosto
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(onClick = {
                                        editingCuota = cuota
                                        showForm = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Editar",
                                            tint = AppColors.Primary
                                        )
                                    }
                                    IconButton(onClick = { showDeleteDialog = cuota }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFFE57373)
                                        )
                                    }
                                }
                            }

                            // Divider entre filas (opcional)
                            if (cuota != cuotas.last()) {
                                Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // FloatingActionButton para agregar
        FloatingActionButton(
            onClick = {
                editingCuota = null
                showForm = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = AppColors.Primary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = "Agregar cuota")
        }

        // Diálogo de confirmación para eliminar
        showDeleteDialog?.let { cuotaToDelete ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Eliminar cuota") },
                text = { Text("¿Seguro que deseas eliminar '${cuotaToDelete.nombre}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        try {
                            repository.deletePrecioBase(cuotaToDelete.id)
                            reloadCuotas()
                        } catch (e: Exception) {
                            println("Error al eliminar cuota: ${e.message}")
                        } finally {
                            showDeleteDialog = null
                        }
                    }) {
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

        // Formulario / diálogo para crear / editar (reutiliza el mismo componente)
        if (showForm) {
            CuotaFormDialog(
                onDismiss = {
                    showForm = false
                    editingCuota = null
                    reloadCuotas()
                },
                repository = repository,
                existingCuotas = cuotas,
                editingCuota = editingCuota
            )
        }
    }
}

// -----------------------------------------------
// Dialog/Formulario para Crear/Editar Cuota
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
    val nameError: String? = null,
    val priceError: String? = null,
    val duplicateError: String? = null
)

object CuotaValidator {
    fun validate(
        name: String,
        priceString: String,
        existing: List<PrecioBaseSelectAll>,
        excludeId: Long? = null
    ): CuotaValidationResult {
        val nameErr = when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length < 3 -> "Mínimo 3 caracteres"
            name.length > 80 -> "Máximo 80 caracteres"
            !name.matches(Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s]+$")) -> "Solo letras, números y espacios"
            else -> null
        }

        val priceVal = priceString.toDoubleOrNull()
        val priceErr = when {
            priceString.isBlank() -> "El precio es obligatorio"
            priceVal == null -> "Formato de precio inválido"
            priceVal <= 0.0 -> "El precio debe ser mayor a 0"
            else -> null
        }

        val dup = if (nameErr == null && priceErr == null) {
            val exists = existing.any {
                it.nombre.equals(name, ignoreCase = true) && it.id != (excludeId ?: -1L)
            }
            if (exists) "Ya existe una cuota con este nombre" else null
        } else null

        return CuotaValidationResult(
            isValid = nameErr == null && priceErr == null && dup == null,
            nameError = nameErr,
            priceError = priceErr,
            duplicateError = dup
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuotaFormDialog(
    onDismiss: () -> Unit,
    repository: Repository,
    existingCuotas: List<PrecioBaseSelectAll>,
    editingCuota: PrecioBaseSelectAll? = null
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val isEditing = editingCuota != null
    val currentFranchiseId = 1L // REEMPLAZA según tu lógica si corresponde

    // Campos del formulario
    var currentStep by remember { mutableStateOf(CuotaFormStep.FORM) }
    var nombre by remember { mutableStateOf(editingCuota?.nombre ?: "") }
    // Ajuste: SQLDelight suele generar `precio_costo` (underscore) — si tu propiedad es distinta, cámbiala.
    var precio by remember {
        mutableStateOf(
            editingCuota?.let { it.precio_costo.toString() } ?: "" // <-- si falla, usa it.precioCosto
        )
    }
    var descripcion by remember { mutableStateOf(editingCuota?.descripcion ?: "") }
    var formState by remember { mutableStateOf<CuotaFormState>(CuotaFormState.Idle) }
    var validationResult by remember { mutableStateOf(CuotaValidationResult(true)) }

    fun validate(): Boolean {
        val r = CuotaValidator.validate(nombre.trim(), precio.trim(), existingCuotas, editingCuota?.id)
        validationResult = r
        return r.isValid
    }

    fun getCurrentDate(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
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
                            val id = editingCuota!!.id
                            val activo = editingCuota.activo // si tu property se llama distinto, ajústalo
                            repository.updatePrecioBase(
                                id = id,
                                franchiseId = currentFranchiseId,
                                nombre = nombre.trim(),
                                precioCosto = precio.toDouble(),
                                descripcion = if (descripcion.isBlank()) null else descripcion.trim(),
                                activo = activo,
                                fechaActualizacion = getCurrentDate()
                            )
                        } else {
                            repository.insertPrecioBase(
                                franchiseId = currentFranchiseId,
                                nombre = nombre.trim(),
                                precioCosto = precio.toDouble(),
                                descripcion = if (descripcion.isBlank()) null else descripcion.trim(),
                                activo = 1,
                                fechaCreacion = getCurrentDate()
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

    // Dialog tipo modal centrado (Card)
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
                    // Header con progreso
                    val progress by animateFloatAsState(
                        targetValue = when (currentStep) {
                            CuotaFormStep.FORM -> 0.5f
                            CuotaFormStep.CONFIRMATION -> 1f
                        },
                        animationSpec = tween(300),
                        label = "progress"
                    )

                    Text(
                        text = if (isEditing) "Editar Cuota" else "Registrar Cuota",
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
                            // Nombre
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = {
                                    nombre = it
                                    if (validationResult.nameError != null || validationResult.duplicateError != null) {
                                        validationResult = validationResult.copy(nameError = null, duplicateError = null)
                                    }
                                },
                                label = { Text("Nombre *") },
                                singleLine = true,
                                isError = validationResult.nameError != null || validationResult.duplicateError != null,
                                supportingText = {
                                    (validationResult.nameError ?: validationResult.duplicateError)?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            Spacer(Modifier.height(12.dp))

                            // Precio
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            Spacer(Modifier.height(12.dp))

                            // Descripción
                            OutlinedTextField(
                                value = descripcion,
                                onValueChange = { descripcion = it },
                                label = { Text("Descripción (opcional)") },
                                minLines = 2,
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }

                        CuotaFormStep.CONFIRMATION -> {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "Confirmar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Divider()
                                    Text("Nombre: $nombre", fontWeight = FontWeight.Medium)
                                    Text("Precio: $$precio", fontWeight = FontWeight.Medium)
                                    if (descripcion.isNotBlank()) Text("Descripción: $descripcion")
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

                    // Botones de navegación
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

// -----------------------------------------------
// Logo de fondo (reutilizable)
// -----------------------------------------------
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
