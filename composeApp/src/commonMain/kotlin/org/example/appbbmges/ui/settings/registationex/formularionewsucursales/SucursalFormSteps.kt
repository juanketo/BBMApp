package org.example.appbbmges.ui.settings.registationex.formularionewsucursales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.example.appbbmges.ui.usuarios.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoStep(
    name: String,
    email: String,
    phone: String,
    validationResult: SucursalValidationResult,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    focusManager: FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre de la sucursal *") },
            isError = validationResult.nameError != null,
            supportingText = {
                validationResult.nameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            placeholder = { Text("Zona Esmeralda") }
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = validationResult.emailError != null,
            supportingText = {
                validationResult.emailError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            placeholder = { Text("ejemplo@correo.com") }
        )

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Teléfono *") },
            isError = validationResult.phoneError != null,
            supportingText = {
                validationResult.phoneError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                } ?: if (phone.isNotEmpty()) {
                    Text("${phone.length}/10 dígitos", color = Color.Gray)
                } else {
                    null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            placeholder = { Text("5512345678") }
        )
    }
}

/**
 * Componente para el paso de información de precios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailInfoStep(
    basePrice: String,
    currency: String,
    validationResult: SucursalValidationResult,
    onBasePriceChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    focusManager: FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = basePrice,
            onValueChange = { value ->
                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    onBasePriceChange(value)
                }
            },
            label = { Text("Precio base *") },
            isError = validationResult.basePriceError != null,
            supportingText = {
                validationResult.basePriceError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            leadingIcon = { Text("$") },
            placeholder = { Text("1500.00") }
        )

        OutlinedTextField(
            value = currency,
            onValueChange = { value ->
                onCurrencyChange(value.uppercase().take(3))
            },
            label = { Text("Moneda") },
            isError = validationResult.currencyError != null,
            supportingText = {
                validationResult.currencyError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            placeholder = { Text("MXN") }
        )
    }
}

/**
 * Componente para el paso de información de dirección
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressInfoStep(
    street: String,
    number: String,
    neighborhood: String,
    zip: String,
    city: String,
    country: String,
    validationResult: SucursalValidationResult,
    onStreetChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onNeighborhoodChange: (String) -> Unit,
    onZipChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    focusManager: FocusManager
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = street,
                onValueChange = onStreetChange,
                label = { Text("Calle") },
                modifier = Modifier.weight(0.7f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                placeholder = { Text("Av. Principal") }
            )

            OutlinedTextField(
                value = number,
                onValueChange = onNumberChange,
                label = { Text("Número") },
                modifier = Modifier.weight(0.3f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                placeholder = { Text("123") }
            )
        }

        OutlinedTextField(
            value = neighborhood,
            onValueChange = onNeighborhoodChange,
            label = { Text("Colonia/Barrio") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            placeholder = { Text("Centro") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = zip,
                onValueChange = onZipChange,
                label = { Text("Código postal") },
                isError = validationResult.zipError != null,
                supportingText = {
                    validationResult.zipError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    } ?: if (zip.isNotEmpty()) {
                        Text("${zip.length}/5 dígitos", color = Color.Gray)
                    } else {
                        null
                    }
                },
                modifier = Modifier.weight(0.4f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                placeholder = { Text("12345") }
            )

            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = { Text("Ciudad") },
                modifier = Modifier.weight(0.6f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                placeholder = { Text("Ciudad de México") }
            )
        }

        OutlinedTextField(
            value = country,
            onValueChange = onCountryChange,
            label = { Text("País") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            placeholder = { Text("México") }
        )

        validationResult.addressError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Componente para el paso de información adicional
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalInfoStep(
    taxName: String,
    taxId: String,
    zone: String,
    zonas: List<String>,
    expandedZonas: Boolean,
    isNew: Boolean,
    active: Boolean,
    onTaxNameChange: (String) -> Unit,
    onTaxIdChange: (String) -> Unit,
    onZoneChange: (String) -> Unit,
    onExpandedZonasChange: (Boolean) -> Unit,
    onIsNewChange: (Boolean) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    validationResult: SucursalValidationResult
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ConfigurationCard(
            isNew = isNew,
            active = active,
            onIsNewChange = onIsNewChange,
            onActiveChange = onActiveChange
        )

        OutlinedTextField(
            value = taxName,
            onValueChange = onTaxNameChange,
            label = { Text("Razón social") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            placeholder = { Text("Empresa S.A. de C.V.") }
        )

        OutlinedTextField(
            value = taxId,
            onValueChange = onTaxIdChange,
            label = { Text("RFC") },
            isError = validationResult.taxIdError != null,
            supportingText = {
                validationResult.taxIdError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            placeholder = { Text("XAXX010101000") }
        )

        ZoneDropdown(
            zone = zone,
            zonas = zonas,
            expandedZonas = expandedZonas,
            onZoneChange = onZoneChange,
            onExpandedZonasChange = onExpandedZonasChange
        )
    }
}

/**
 * Componente de configuración (switches)
 */
@Composable
private fun ConfigurationCard(
    isNew: Boolean,
    active: Boolean,
    onIsNewChange: (Boolean) -> Unit,
    onActiveChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConfigurationSwitch(
                title = "Sucursal nueva",
                description = "Marcar si es una sucursal nueva",
                checked = isNew,
                onCheckedChange = onIsNewChange
            )

            ConfigurationSwitch(
                title = "Activa",
                description = "Si la sucursal está activa",
                checked = active,
                onCheckedChange = onActiveChange
            )
        }
    }
}

/**
 * Componente de switch de configuración
 */
@Composable
private fun ConfigurationSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Primary)
        )
    }
}

/**
 * Componente dropdown para selección de zona
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZoneDropdown(
    zone: String,
    zonas: List<String>,
    expandedZonas: Boolean,
    onZoneChange: (String) -> Unit,
    onExpandedZonasChange: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = zone,
            onValueChange = {},
            label = { Text("Zona") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar zonas")
            },
            placeholder = { Text("Selecciona una zona") }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .alpha(0f)
                .clickable { onExpandedZonasChange(true) }
        )
    }

    DropdownMenu(
        expanded = expandedZonas,
        onDismissRequest = { onExpandedZonasChange(false) },
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        zonas.forEach { zonaItem ->
            DropdownMenuItem(
                text = { Text(if (zonaItem.isEmpty()) "Sin zona" else zonaItem) },
                onClick = {
                    onZoneChange(zonaItem)
                    onExpandedZonasChange(false)
                }
            )
        }
    }
}

/**
 * Paso de confirmación con toda la información
 */
@Composable
fun ConfirmationStep(
    formData: SucursalFormData,
    formState: SucursalFormState
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 400.dp)
    ) {
        item {
            Text(
                text = "Confirmar datos de la sucursal:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            ConfirmationSection(
                title = "Información Básica",
                fields = buildList {
                    add("Nombre" to formData.name)
                    if (formData.email.isNotEmpty()) add("Email" to formData.email)
                    if (formData.phone.isNotEmpty()) add("Teléfono" to formData.phone)
                }
            )
        }

        if (formData.basePrice.isNotEmpty() || formData.currency.isNotEmpty()) {
            item {
                ConfirmationSection(
                    title = "Información de Precios",
                    fields = buildList {
                        if (formData.basePrice.isNotEmpty()) add("Precio base" to "$${formData.basePrice}")
                        if (formData.currency.isNotEmpty()) add("Moneda" to formData.currency)
                    }
                )
            }
        }

        if (hasAddressInfo(formData)) {
            item {
                ConfirmationSection(
                    title = "Dirección",
                    fields = listOf("Dirección completa" to buildFullAddress(formData))
                )
            }
        }

        if (formData.taxName.isNotEmpty() || formData.taxId.isNotEmpty() || formData.zone.isNotEmpty()) {
            item {
                ConfirmationSection(
                    title = "Información Adicional",
                    fields = buildList {
                        if (formData.taxName.isNotEmpty()) add("Razón social" to formData.taxName)
                        if (formData.taxId.isNotEmpty()) add("RFC/Tax ID" to formData.taxId)
                        if (formData.zone.isNotEmpty()) add("Zona" to formData.zone)
                    }
                )
            }
        }

        item {
            ConfirmationSection(
                title = "Configuración",
                fields = listOf(
                    "Sucursal nueva" to if (formData.isNew) "Sí" else "No",
                    "Activa" to if (formData.active) "Sí" else "No"
                )
            )
        }

        if (formState is SucursalFormState.Error) {
            item {
                ErrorCard(formState)
            }
        }
    }
}

/**
 * Sección de confirmación reutilizable
 */
@Composable
private fun ConfirmationSection(
    title: String,
    fields: List<Pair<String, String>>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )

            fields.forEach { (label, value) ->
                ConfirmationField(label, value)
            }
        }
    }
}

/**
 * Campo de confirmación individual
 */
@Composable
private fun ConfirmationField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

/**
 * Card de error reutilizable
 */
@Composable
private fun ErrorCard(formState: SucursalFormState.Error) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = formState.message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            if (formState.retryable) {
                Text(
                    text = "Puede intentar nuevamente",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Funciones auxiliares para la confirmación

private fun hasAddressInfo(formData: SucursalFormData): Boolean {
    return formData.addressStreet.isNotEmpty() ||
            formData.addressNumber.isNotEmpty() ||
            formData.addressNeighborhood.isNotEmpty() ||
            formData.addressZip.isNotEmpty() ||
            formData.addressCity.isNotEmpty() ||
            formData.addressCountry.isNotEmpty()
}

private fun buildFullAddress(formData: SucursalFormData): String {
    return buildString {
        if (formData.addressStreet.isNotEmpty()) append(formData.addressStreet)
        if (formData.addressNumber.isNotEmpty()) append(" ${formData.addressNumber}")
        if (formData.addressNeighborhood.isNotEmpty()) append(", ${formData.addressNeighborhood}")
        if (formData.addressZip.isNotEmpty()) append(", CP ${formData.addressZip}")
        if (formData.addressCity.isNotEmpty()) append(", ${formData.addressCity}")
        if (formData.addressCountry.isNotEmpty()) append(", ${formData.addressCountry}")
    }
}