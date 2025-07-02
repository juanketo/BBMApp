package org.example.appbbmges.ui.franquicias

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.example.appbbmges.data.Repository
import org.example.appbbmges.navigation.SimpleNavController

@Composable
fun FranquiciasScreen(navController: SimpleNavController, repository: Repository) {
    var selectedZona by remember { mutableStateOf<Zona?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showActiveOnly by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedFranquicia by remember { mutableStateOf<Franquicia?>(null) }

    val franquicias = remember { getFranquiciasEjemplo() }

    val filteredFranquicias = franquicias.filter { franquicia ->
        (selectedZona == null || franquicia.zona == selectedZona) &&
                (searchQuery.isEmpty() || franquicia.nombre.contains(searchQuery, ignoreCase = true)) &&
                (!showActiveOnly || franquicia.activa)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FranquiciasHeader(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = { showFilters = true }
            )

            Row(modifier = Modifier.fillMaxSize()) {
                ZonasSidebar(
                    selectedZona = selectedZona,
                    onZonaSelected = { selectedZona = it }
                )

                Box(modifier = Modifier.weight(1f)) {
                    val listState = rememberLazyListState()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        state = listState
                    ) {
                        item {
                            Text(
                                text = "Franquicias ${selectedZona?.nombre ?: "Todas las Zonas"}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        items(filteredFranquicias.chunked(2)) { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { franquicia ->
                                    FranquiciaCard(
                                        franquicia = franquicia,
                                        onClick = { selectedFranquicia = franquicia },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    CustomVerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .width(8.dp)
                            .fillMaxHeight(),
                        listState = listState
                    )
                }
            }
        }

        if (showFilters) {
            FiltrosDialog(
                showActiveOnly = showActiveOnly,
                onShowActiveOnlyChange = { showActiveOnly = it },
                onDismiss = { showFilters = false }
            )
        }

        selectedFranquicia?.let { franquicia ->
            FranquiciaDetailDialog(
                franquicia = franquicia,
                onDismiss = { selectedFranquicia = null }
            )
        }
    }
}

@Composable
fun CustomVerticalScrollbar(
    modifier: Modifier = Modifier,
    listState: LazyListState
) {
    val totalItems = listState.layoutInfo.totalItemsCount
    val visibleItems = listState.layoutInfo.visibleItemsInfo.size
    val firstVisibleItem = listState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset

    if (totalItems > visibleItems) {
        val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
        val itemHeight = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 180
        val totalContentHeight = (totalItems * itemHeight + 32).coerceAtLeast(1)

        val scrollProgress = if (totalItems > 0 && totalContentHeight > 0) {
            (firstVisibleItem * itemHeight + firstVisibleItemScrollOffset)
                .toFloat() / (totalContentHeight - viewportHeight).coerceAtLeast(1)
        } else {
            0f
        }

        val thumbHeightFraction = (viewportHeight.toFloat() / totalContentHeight.toFloat()).coerceIn(0.1f, 0.9f)

        BoxWithConstraints(
            modifier = modifier
                .background(
                    Color(0xFF3667EA).copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp)
                )
        ) {
            val maxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
            val thumbHeightPx = maxHeightPx * thumbHeightFraction
            val thumbPositionPx = scrollProgress * (maxHeightPx - thumbHeightPx)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) { thumbHeightPx.toDp() })
                    .offset(y = with(LocalDensity.current) { thumbPositionPx.toDp() })
                    .background(
                        Color(0xFF3667EA),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranquiciasHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        tonalElevation = 4.dp, // Updated to tonalElevation for Material 3 compatibility
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Business,
                contentDescription = "Franquicias",
                tint = Color(0xFF3667EA),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Administración de Franquicias",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF3667EA)
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar franquicia...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier.width(300.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF3667EA),
                    unfocusedIndicatorColor = Color(0xFFE0E0E0)
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            FilledTonalButton(
                onClick = onFilterClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFE6EDFF)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterAlt,
                    contentDescription = "Filtros",
                    tint = Color(0xFF3667EA)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filtros", color = Color(0xFF3667EA))
            }
        }
    }
}

@Composable
fun ZonasSidebar(
    selectedZona: Zona?,
    onZonaSelected: (Zona?) -> Unit
) {
    val zonas = listOf(
        Zona("Zona Norte", Icons.Default.NorthEast),
        Zona("Zona Sur", Icons.Default.SouthEast),
        Zona("Zona Oriente", Icons.Default.East),
        Zona("Zona Poniente", Icons.Default.West),
        Zona("Foráneas", Icons.Default.LocationOn),
        Zona("Internacionales", Icons.Default.Public)
    )

    Surface(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight(),
        color = Color(0xFFF8F8F8)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Zonas Geográficas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            item {
                ZonaItem(
                    zona = null,
                    icon = Icons.Default.Apps,
                    isSelected = selectedZona == null,
                    onClick = { onZonaSelected(null) }
                )
            }
            items(zonas) { zona ->
                ZonaItem(
                    zona = zona,
                    icon = zona.icon,
                    isSelected = selectedZona == zona,
                    onClick = { onZonaSelected(zona) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZonaItem(
    zona: Zona?,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFE6EDFF) else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF3667EA) else Color(0xFF555555)

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = zona?.nombre ?: "Todas las zonas",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = zona?.nombre ?: "Todas las zonas",
                color = contentColor,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun FranquiciaCard(
    franquicia: Franquicia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color(0xFF3667EA),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = franquicia.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (franquicia.activa) Color(0xFF4CAF50) else Color(0xFFFF5252))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color(0xFF555555),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = franquicia.telefono,
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF555555),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = franquicia.direccion,
                    fontSize = 14.sp,
                    color = Color(0xFF555555),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { /* TODO: Descargar contrato */ },
                    enabled = franquicia.contratoDisponible
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Descargar contrato",
                        tint = if (franquicia.contratoDisponible) Color(0xFF3667EA) else Color.Gray
                    )
                }
                IconButton(
                    onClick = { /* TODO: Descargar accesos */ },
                    enabled = franquicia.registroAccesosDisponible
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = "Descargar accesos",
                        tint = if (franquicia.registroAccesosDisponible) Color(0xFF3667EA) else Color.Gray
                    )
                }
                IconButton(onClick = { /* TODO: Enviar correo */ }) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Enviar correo",
                        tint = Color(0xFF3667EA)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO: Editar */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar franquicia",
                        tint = Color(0xFF3667EA)
                    )
                }
            }
        }
    }
}

@Composable
fun FiltrosDialog(
    showActiveOnly: Boolean,
    onShowActiveOnlyChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Filtros",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Estado de la franquicia",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = showActiveOnly,
                        onCheckedChange = onShowActiveOnlyChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF3667EA),
                            checkedTrackColor = Color(0xFFB8C7FA)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showActiveOnly) "Mostrar solo activas" else "Mostrar todas",
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3667EA)
                        )
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
}

@Composable
fun FranquiciaDetailDialog(
    franquicia: Franquicia,
    onDismiss: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = Color(0xFF3667EA),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = franquicia.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (franquicia.activa) Color(0xFF4CAF50) else Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (franquicia.activa) "Activa" else "Inactiva",
                            fontSize = 14.sp,
                            color = if (franquicia.activa) Color(0xFF4CAF50) else Color(0xFFFF5252)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { editing = !editing }) {
                        Icon(
                            imageVector = if (editing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (editing) "Cancelar edición" else "Editar",
                            tint = Color(0xFF3667EA)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (editing) {
                        FranquiciaEditContent(franquicia = franquicia)
                    } else {
                        FranquiciaViewContent(franquicia = franquicia)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    if (editing) {
                        Button(
                            onClick = { /* TODO: Guardar cambios */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3667EA)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar cambios")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { /* TODO: Descargar PDF */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFF3667EA)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generar reporte PDF", color = Color(0xFF3667EA))
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3667EA)
                            )
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranquiciaEditContent(franquicia: Franquicia) {
    var nombre by remember { mutableStateOf(franquicia.nombre) }
    var telefono by remember { mutableStateOf(franquicia.telefono) }
    var email by remember { mutableStateOf(franquicia.email) }
    var direccion by remember { mutableStateOf(franquicia.direccion) }
    var activa by remember { mutableStateOf(franquicia.activa) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Información General",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF3667EA)
            )
        }
        item {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la franquicia") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Estado de la franquicia:",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = activa,
                    onCheckedChange = { activa = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF3667EA),
                        checkedTrackColor = Color(0xFFB8C7FA)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (activa) "Activa" else "Inactiva",
                    fontSize = 16.sp,
                    color = if (activa) Color(0xFF4CAF50) else Color(0xFFFF5252)
                )
            }
        }
        item {
            Text(
                text = "Información Fiscal",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF3667EA),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        item {
            OutlinedTextField(
                value = franquicia.rfcOcurp,
                onValueChange = { },
                label = { Text("RFC o CURP") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = franquicia.razonSocial,
                onValueChange = { },
                label = { Text("Razón Social") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FranquiciaViewContent(franquicia: Franquicia) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Información General",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF3667EA)
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F8F8)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoRow(Icons.Default.Phone, "Teléfono", franquicia.telefono)
                    InfoRow(Icons.Default.Email, "Correo", franquicia.email)
                    InfoRow(Icons.Default.LocationOn, "Dirección", franquicia.direccion)
                    InfoRow(Icons.Default.Public, "Zona", franquicia.zona?.nombre ?: "")
                }
            }
        }
        item {
            Text(
                text = "Información Fiscal",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF3667EA)
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F8F8)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoRow(Icons.Default.Badge, "RFC o CURP", franquicia.rfcOcurp)
                    InfoRow(Icons.Default.Business, "Razón Social", franquicia.razonSocial)
                    InfoRow(Icons.Default.Home, "Dirección Fiscal", franquicia.direccionFiscal)
                }
            }
        }
        item {
            Text(
                text = "Documentos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF3667EA)
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DocumentButton(
                    icon = Icons.Default.Description,
                    text = "Contrato",
                    enabled = franquicia.contratoDisponible,
                    onClick = { /* TODO: Descargar contrato */ }
                )
                DocumentButton(
                    icon = Icons.Default.AccountBox,
                    text = "Registro de Accesos",
                    enabled = franquicia.registroAccesosDisponible,
                    onClick = { /* TODO: Descargar accesos */ }
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3667EA),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF888888)
            )
            Text(
                text = value,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun DocumentButton(
    icon: ImageVector,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (enabled) Color(0xFF3667EA) else Color.Gray
        ),
        modifier = Modifier.height(48.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

data class Zona(
    val nombre: String,
    val icon: ImageVector
)

data class Franquicia(
    val nombre: String,
    val telefono: String,
    val email: String,
    val direccion: String,
    val activa: Boolean,
    val zona: Zona?,
    val rfcOcurp: String,
    val razonSocial: String,
    val direccionFiscal: String,
    val contratoDisponible: Boolean,
    val registroAccesosDisponible: Boolean
)

fun getFranquiciasEjemplo(): List<Franquicia> {
    val zonaNorte = Zona("Zona Norte", Icons.Default.NorthEast)
    val zonaSur = Zona("Zona Sur", Icons.Default.SouthEast)
    val zonaOriente = Zona("Zona Oriente", Icons.Default.East)
    val zonaPoniente = Zona("Zona Poniente", Icons.Default.West)
    val zonaForanea = Zona("Foráneas", Icons.Default.LocationOn)
    val zonaInternacional = Zona("Internacionales", Icons.Default.Public)

    return listOf(
        Franquicia(
            nombre = "Franquicia Polanco",
            telefono = "55 1234 5678",
            email = "polanco@franquicias.com",
            direccion = "Av. Presidente Masaryk 123, Polanco, CDMX",
            activa = true,
            zona = zonaNorte,
            rfcOcurp = "FRA120230ABC",
            razonSocial = "Franquicias del Norte S.A. de C.V.",
            direccionFiscal = "Av. Presidente Masaryk 123, Polanco, CDMX",
            contratoDisponible = true,
            registroAccesosDisponible = true
        ),
        Franquicia(
            nombre = "Franquicia Condesa",
            telefono = "55 2345 6789",
            email = "condesa@franquicias.com",
            direccion = "Av. Tamaulipas 456, Condesa, CDMX",
            activa = true,
            zona = zonaSur,
            rfcOcurp = "FRA120231DEF",
            razonSocial = "Franquicias del Sur S.A. de C.V.",
            direccionFiscal = "Av. Tamaulipas 456, Condesa, CDMX",
            contratoDisponible = true,
            registroAccesosDisponible = false
        ),
        Franquicia(
            nombre = "Franquicia Santa Fe",
            telefono = "55 3456 7890",
            email = "santafe@franquicias.com",
            direccion = "Av. Vasco de Quiroga 789, Santa Fe, CDMX",
            activa = true,
            zona = zonaPoniente,
            rfcOcurp = "FRA120232GHI",
            razonSocial = "Franquicias del Poniente S.A. de C.V.",
            direccionFiscal = "Av. Vasco de Quiroga 789, Santa Fe, CDMX",
            contratoDisponible = false,
            registroAccesosDisponible = true
        ),
        Franquicia(
            nombre = "Franquicia Coyoacán",
            telefono = "55 4567 8901",
            email = "coyoacan@franquicias.com",
            direccion = "Av. Miguel Ángel de Quevedo 890, Coyoacán, CDMX",
            activa = false,
            zona = zonaSur,
            rfcOcurp = "FRA120233JKL",
            razonSocial = "Franquicias del Sur S.A. de C.V.",
            direccionFiscal = "Av. Miguel Ángel de Quevedo 890, Coyoacán, CDMX",
            contratoDisponible = true,
            registroAccesosDisponible = true
        ),
        Franquicia(
            nombre = "Franquicia Paseo Interlomas",
            telefono = "55 5678 9012",
            email = "interlomas@franquicias.com",
            direccion = "Av. Jesús del Monte 123, Interlomas, Edo. de México",
            activa = true,
            zona = zonaPoniente,
            rfcOcurp = "FRA120234MNO",
            razonSocial = "Franquicias del Poniente S.A. de C.V.",
            direccionFiscal = "Av. Jesús del Monte 123, Interlomas, Edo. de México",
            contratoDisponible = true,
            registroAccesosDisponible = true
        ),
        Franquicia(
            nombre = "Franquicia Reforma",
            telefono = "55 6789 0123",
            email = "reforma@franquicias.com",
            direccion = "Paseo de la Reforma 234, Cuauhtémoc, CDMX",
            activa = true,
            zona = zonaNorte,
            rfcOcurp = "FRA120235PQR",
            razonSocial = "Franquicias del Norte S.A. de C.V.",
            direccionFiscal = "Paseo de la Reforma 234, Cuauhtémoc, CDMX",
            contratoDisponible = false,
            registroAccesosDisponible = false
        ),
        Franquicia(
            nombre = "Franquicia Perisur",
            telefono = "55 7890 1234",
            email = "perisur@franquicias.com",
            direccion = "Anillo Periférico Sur 345, Coyoacán, CDMX",
            activa = true,
            zona = zonaSur,
            rfcOcurp = "FRA120236STU",
            razonSocial = "Franquicias del Sur S.A. de C.V.",
            direccionFiscal = "Anillo Periférico Sur 345, Coyoacán, CDMX",
            contratoDisponible = true,
            registroAccesosDisponible = true
        ),
        Franquicia(
            nombre = "Franquicia Satélite",
            telefono = "55 8901 2345",
            email = "satelite@franquicias.com",
            direccion = "Circuito Centro Comercial 456, Ciudad Satélite, Edo. de México",
            activa = false,
            zona = zonaNorte,
            rfcOcurp = "FRA120237VWX",
            razonSocial = "Franquicias del Norte S.A. de C.V.",
            direccionFiscal = "Circuito Centro Comercial 456, Ciudad Satélite, Edo. de México",
            contratoDisponible = true,
            registroAccesosDisponible = true
        ),
        Franquicia(
            nombre = "Franquicia Monterrey",
            telefono = "81 2345 6789",
            email = "monterrey@franquicias.com",
            direccion = "Av. Constitución 567, Monterrey, Nuevo León",
            activa = true,
            zona = zonaForanea,
            rfcOcurp = "FRA120238YZA",
            razonSocial = "Franquicias Foráneas S.A. de C.V.",
            direccionFiscal = "Av. Constitución 567, Monterrey, Nuevo León",
            contratoDisponible = true,
            registroAccesosDisponible = false
        ),
        Franquicia(
            nombre = "Franquicia Guadalajara",
            telefono = "33 3456 7890",
            email = "guadalajara@franquicias.com",
            direccion = "Av. Vallarta 678, Guadalajara, Jalisco",
            activa = true,
            zona = zonaForanea,
            rfcOcurp = "FRA120239BCD",
            razonSocial = "Franquicias Foráneas S.A. de C.V.",
            direccionFiscal = "Av. Vallarta 678, Guadalajara, Jalisco",
            contratoDisponible = false,
            registroAccesosDisponible = true
        ),
        Franquicia(
            nombre = "Franquicia Miami",
            telefono = "+1 305 123 4567",
            email = "miami@franquicias.com",
            direccion = "Brickell Avenue 789, Miami, FL, USA",
            activa = true,
            zona = zonaInternacional,
            rfcOcurp = "FRA120240EFG",
            razonSocial = "International Franchises LLC",
            direccionFiscal = "Brickell Avenue 789, Miami, FL, USA",
            contratoDisponible = true,
            registroAccesosDisponible = true
        ),
        Franquicia(
            nombre = "Franquicia Madrid",
            telefono = "+34 91 234 5678",
            email = "madrid@franquicias.com",
            direccion = "Gran Vía 890, Madrid, España",
            activa = false,
            zona = zonaInternacional,
            rfcOcurp = "FRA120241HIJ",
            razonSocial = "Franquicias Internacionales S.L.",
            direccionFiscal = "Gran Vía 890, Madrid, España",
            contratoDisponible = true,
            registroAccesosDisponible = false
        )
    )
}