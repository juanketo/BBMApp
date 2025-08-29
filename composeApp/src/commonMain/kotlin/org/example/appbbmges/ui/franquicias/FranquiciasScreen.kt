package org.example.appbbmges.ui.franquicias

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.appbbmges.FranchiseEntity
import org.example.appbbmges.navigation.SimpleNavController
import org.example.appbbmges.data.Repository
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranquiciasScreen(navController: SimpleNavController, repository: Repository) {
    var franquicias by remember { mutableStateOf<List<FranchiseEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            franquicias = repository.getAllFranchises()
        } catch (e: Exception) {
            println("Error cargando franquicias: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header mejorado
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Gestión de Franquicias",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Administra y supervisa todas tus franquicias",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${franquicias.size} ${if (franquicias.size == 1) "registro" else "registros"}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Contenido principal con Grid 2x2
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading) {
                LoadingState()
            } else if (franquicias.isEmpty()) {
                EmptyState()
            } else {
                // Grid 2x2 para mostrar las tarjetas
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(franquicias) { franquicia ->
                        CompactFranquiciaCard(
                            franquicia = franquicia,
                            onEditClick = {
                            },
                            onDeleteClick = {
                                // Eliminar franquicia
                                try {
                                    repository.deleteFranchise(franquicia.id)
                                    // Actualizar lista
                                    franquicias = repository.getAllFranchises()
                                } catch (e: Exception) {
                                    println("Error eliminando franquicia: ${e.message}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando franquicias...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BusinessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No hay franquicias registradas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Agrega tu primera franquicia para comenzar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactFranquiciaCard(
    franquicia: FranchiseEntity,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con título principal y línea decorativa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Sección del título con línea decorativa
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = franquicia.name.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 1.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Línea decorativa debajo del título
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(3.dp)
                                .background(
                                    color = Color(0xFFFF6B6B),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    // Icono del edificio mejorado
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(10.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Contenido principal mejorado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Contacto con borde lateral
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(35.dp)
                            .background(
                                color = Color(0xFFFF6B6B),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        CompactInfoSection(title = "Contacto:") {
                            franquicia.email?.let { email ->
                                CompactInfoItem(label = "Email", value = email)
                            }
                            franquicia.phone?.let { phone ->
                                CompactInfoItem(label = "Teléfono", value = phone)
                            }
                        }
                    }
                }

                // Dirección con borde lateral
                if (hasAddressInfo(franquicia)) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(35.dp)
                                .background(
                                    color = Color(0xFFFF6B6B),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            CompactInfoSection(title = "Dirección:") {
                                val address = buildAddressString(franquicia)
                                if (address.isNotBlank()) {
                                    Text(
                                        text = address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Información Comercial con borde lateral
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(35.dp)
                            .background(
                                color = Color(0xFFFF6B6B),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        CompactInfoSection(title = "Información Comercial:") {
                            franquicia.base_price?.let { price ->
                                CompactInfoItem(
                                    label = "Precio Base",
                                    value = "${franquicia.currency ?: "MXN"} ${formatDouble(price)}"
                                )
                            }
                            franquicia.zone?.let { zone ->
                                CompactInfoItem(label = "Zona", value = zone)
                            }
                        }
                    }
                }

                // Información Fiscal con borde lateral
                if (franquicia.tax_name != null || franquicia.tax_id != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(35.dp)
                                .background(
                                    color = Color(0xFFFF6B6B),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            CompactInfoSection(title = "Información Fiscal:") {
                                franquicia.tax_name?.let { taxName ->
                                    CompactInfoItem(label = "Razón Social", value = taxName)
                                }
                                franquicia.tax_id?.let { taxId ->
                                    CompactInfoItem(label = "RFC/Tax ID", value = taxId)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botones de acción - NUEVO DISEÑO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Editar
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFCC80),
                        disabledContainerColor = Color(0xFFFFCC80).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Editar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                // Botón Eliminar
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFAB91), // ROJO PASTEL
                        disabledContainerColor = Color(0xFFFFAB91).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eliminar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CompactInfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column(content = content)
    }
}

@Composable
fun CompactInfoItem(
    label: String,
    value: String
) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Black,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 11.sp
    )
}

// Función multiplataforma para formatear números con 2 decimales
private fun formatDouble(value: Double): String {
    val rounded = (value * 100).roundToInt()
    val integerPart = rounded / 100
    val decimalPart = rounded % 100
    return if (decimalPart == 0) {
        "$integerPart.00"
    } else if (decimalPart < 10) {
        "$integerPart.0$decimalPart"
    } else {
        "$integerPart.$decimalPart"
    }
}

// Funciones auxiliares
private fun hasAddressInfo(franquicia: FranchiseEntity): Boolean {
    return franquicia.address_street != null ||
            franquicia.address_number != null ||
            franquicia.address_neighborhood != null ||
            franquicia.address_city != null ||
            franquicia.address_country != null ||
            franquicia.address_zip != null
}

private fun buildAddressString(franquicia: FranchiseEntity): String {
    val addressParts = listOfNotNull(
        franquicia.address_street,
        franquicia.address_number,
        franquicia.address_neighborhood,
        franquicia.address_city,
        franquicia.address_country
    ).filter { it.isNotBlank() }

    val address = addressParts.joinToString(", ")
    return if (franquicia.address_zip != null && franquicia.address_zip.isNotBlank()) {
        "$address (CP: ${franquicia.address_zip})"
    } else {
        address
    }
}