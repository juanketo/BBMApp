package org.example.appbbmges.ui.settings.registationex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.appbbmges.InscriptionEntity
import org.example.appbbmges.MembershipEntity
import org.example.appbbmges.PrecioBaseEntity
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.usuarios.AppColors
import org.example.appbbmges.ui.settings.registationex.formulariocuotas.FormInscripcionScreen
import org.example.appbbmges.ui.settings.registationex.formulariocuotas.FormMembresiasScreen
import org.example.appbbmges.ui.settings.registationex.formulariocuotas.FormPrecioBaseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCuotasScreen(
    onDismiss: () -> Unit,
    repository: Repository,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Precios Base") }
    var showAddMenu by remember { mutableStateOf(false) }
    var showFormScreen by remember { mutableStateOf<String?>(null) }
    var editingEntity by remember { mutableStateOf<Any?>(null) }

    var preciosBase by remember { mutableStateOf<List<PrecioBaseEntity>>(emptyList()) }
    var memberships by remember { mutableStateOf<List<MembershipEntity>>(emptyList()) }
    var inscripciones by remember { mutableStateOf<List<InscriptionEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    fun reloadData() {
        coroutineScope.launch {
            try {
                preciosBase = repository.getAllPreciosBase()
                memberships = repository.getAllMemberships()
                inscripciones = repository.getAllInscriptions()
            } catch (e: Exception) {
                println("Error recargando datos: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadData()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        when {
            showFormScreen != null -> {
                when (showFormScreen) {
                    "precio" -> {
                        FormPrecioBaseScreen(
                            onDismiss = {
                                showFormScreen = null
                                editingEntity = null
                                reloadData()
                            },
                            repository = repository,
                            editingPrecio = editingEntity as? PrecioBaseEntity
                        )
                    }
                    "membresia" -> {
                        FormMembresiasScreen(
                            onDismiss = {
                                showFormScreen = null
                                editingEntity = null
                                reloadData()
                            },
                            repository = repository,
                            editingMembership = editingEntity as? MembershipEntity
                        )
                    }
                    "inscripcion" -> {
                        FormInscripcionScreen(
                            onDismiss = {
                                showFormScreen = null
                                editingEntity = null
                                reloadData()
                            },
                            repository = repository,
                            editingInscripcion = editingEntity as? InscriptionEntity
                        )
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Gestión de Cuotas",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextColor
                            )
                            Text(
                                text = "Precios base, inscripciones y planes de membresía",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextColor.copy(alpha = 0.7f)
                            )
                        }
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFff8abe)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Volver", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TabRow(
                        selectedTabIndex = when (selectedTab) {
                            "Precios Base" -> 0
                            "Inscripciones" -> 1
                            "Membresías" -> 2
                            else -> 0
                        },
                        containerColor = Color.White,
                        contentColor = AppColors.Primary
                    ) {
                        Tab(
                            selected = selectedTab == "Precios Base",
                            onClick = { selectedTab = "Precios Base" },
                            text = { Text("Precios Base") }
                        )
                        Tab(
                            selected = selectedTab == "Inscripciones",
                            onClick = { selectedTab = "Inscripciones" },
                            text = { Text("Inscripciones") }
                        )
                        Tab(
                            selected = selectedTab == "Membresías",
                            onClick = { selectedTab = "Membresías" },
                            text = { Text("Membresías") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AppColors.Primary)
                        }
                    } else {
                        when (selectedTab) {
                            "Precios Base" -> {
                                PreciosBaseList(
                                    precios = preciosBase,
                                    onEdit = {
                                        editingEntity = it
                                        showFormScreen = "precio"
                                    },
                                    onDelete = {
                                        repository.deletePrecioBase(it.id)
                                        reloadData()
                                    }
                                )
                            }
                            "Inscripciones" -> {
                                InscripcionesList(
                                    inscripciones = inscripciones,
                                    onEdit = {
                                        editingEntity = it
                                        showFormScreen = "inscripcion"
                                    },
                                    onDelete = {
                                        repository.deleteInscription(it.id)
                                        reloadData()
                                    }
                                )
                            }
                            "Membresías" -> {
                                MembresiasList(
                                    memberships = memberships,
                                    onEdit = {
                                        editingEntity = it
                                        showFormScreen = "membresia"
                                    },
                                    onDelete = {
                                        repository.deleteMembership(it.id)
                                        reloadData()
                                    }
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showAddMenu = true },
                        containerColor = AppColors.Primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Agregar precio base") },
                            onClick = {
                                showAddMenu = false
                                showFormScreen = "precio"
                                editingEntity = null
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Agregar inscripción") },
                            onClick = {
                                showAddMenu = false
                                showFormScreen = "inscripcion"
                                editingEntity = null
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Agregar membresía") },
                            onClick = {
                                showAddMenu = false
                                showFormScreen = "membresia"
                                editingEntity = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreciosBaseList(
    precios: List<PrecioBaseEntity>,
    onEdit: (PrecioBaseEntity) -> Unit,
    onDelete: (PrecioBaseEntity) -> Unit
) {
    if (precios.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay precios base registrados", fontWeight = FontWeight.Bold)
                Text("Presiona el botón '+' para agregar uno.")
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(AppColors.Primary.copy(alpha = 0.08f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ID", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                        Text("Precio", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                        Text("Acciones", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                }
                items(precios) { cuota ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cuota.id.toString(), modifier = Modifier.weight(1f))
                        Text("$${cuota.precio}", modifier = Modifier.weight(1f))
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onEdit(cuota) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { onDelete(cuota) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                    if (cuota != precios.last()) {
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

@Composable
private fun InscripcionesList(
    inscripciones: List<InscriptionEntity>,
    onEdit: (InscriptionEntity) -> Unit,
    onDelete: (InscriptionEntity) -> Unit
) {
    if (inscripciones.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay inscripciones registradas", fontWeight = FontWeight.Bold)
                Text("Presiona el botón '+' para agregar una.")
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(AppColors.Primary.copy(alpha = 0.08f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ID", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                        Text("Precio", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                        Text("Acciones", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                }
                items(inscripciones) { inscripcion ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(inscripcion.id.toString(), modifier = Modifier.weight(1f))
                        Text("$${inscripcion.precio}", modifier = Modifier.weight(1f))
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onEdit(inscripcion) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { onDelete(inscripcion) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                    if (inscripcion != inscripciones.last()) {
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

@Composable
private fun MembresiasList(
    memberships: List<MembershipEntity>,
    onEdit: (MembershipEntity) -> Unit,
    onDelete: (MembershipEntity) -> Unit
) {
    if (memberships.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay membresías registradas", fontWeight = FontWeight.Bold)
                Text("Presiona el botón '+' para agregar una.")
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(AppColors.Primary.copy(alpha = 0.08f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nombre", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                        Text("Meses", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                        Text("Ahorro", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                        Text("Acciones", fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                }
                items(memberships) { membership ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(membership.name, modifier = Modifier.weight(1f))
                        Text(membership.months_paid.toString(), modifier = Modifier.weight(1f))
                        Text(membership.months_saved.toString(), modifier = Modifier.weight(1f))
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onEdit(membership) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { onDelete(membership) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                    if (membership != memberships.last()) {
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}