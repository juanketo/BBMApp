package org.example.appbbmges.ui.usuarios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.appbbmges.AdministrativeEntity
import org.example.appbbmges.FranchiseeEntity
import org.example.appbbmges.StudentEntity
import org.example.appbbmges.TeacherEntity
import org.example.appbbmges.data.Repository
import org.example.appbbmges.navigation.SimpleNavController
import org.example.appbbmges.ui.usuarios.registation.administrativeform.AddAdministrativoScreen
import org.example.appbbmges.ui.usuarios.registation.studentsform.AddAlumnoScreen
import org.example.appbbmges.ui.usuarios.registation.AddFranquiciatarioScreen
import org.example.appbbmges.ui.usuarios.registation.AddProfesorScreen
import org.example.appbbmges.ui.usuarios.viewusuarios.ViewAdministrativoScreen
import org.example.appbbmges.ui.usuarios.viewusuarios.ViewAlumnoScreen
import org.example.appbbmges.ui.usuarios.viewusuarios.ViewFranquiciatarioScreen
import org.example.appbbmges.ui.usuarios.viewusuarios.ViewProfesorScreen

object AppColors {
    val Primary = Color(0xFF00B4D8)
    val Background = Color(0xFFF8F9FA)
    val OnPrimary = Color.White
    val TextColor = Color(0xFF333333)
}

@Composable
fun UsuariosScreen(navController: SimpleNavController, repository: Repository) {
    var selectedTab by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    var rowsPerPage by remember { mutableStateOf(10) }
    var expandedRowsPerPage by remember { mutableStateOf(false) }
    var expandedAdd by remember { mutableStateOf(false) }
    var selectedUserType by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<Pair<Any, String>?>(null) }
    var userToEdit by remember { mutableStateOf<Pair<Any, String>?>(null) }
    var selectedProfile by remember { mutableStateOf<Pair<String, Long>?>(null) }

    val students = remember { mutableStateOf(repository.getAllStudents()) }
    val teachers = remember { mutableStateOf(repository.getAllTeachers()) }
    val franchisees = remember { mutableStateOf(repository.getAllFranchisees()) }
    val administratives = remember { mutableStateOf(repository.getAllAdministratives()) }

    LaunchedEffect(selectedTab, searchQuery) {
        students.value = repository.getAllStudents()
        teachers.value = repository.getAllTeachers()
        franchisees.value = repository.getAllFranchisees()
        administratives.value = repository.getAllAdministratives()
    }

    val filteredUsers: List<Pair<Any, String>> = when (selectedTab) {
        "Todos" -> mutableListOf<Pair<Any, String>>().apply {
            addAll(students.value.map { it to "Alumno" })
            addAll(teachers.value.map { it to "Profesor" })
            addAll(franchisees.value.map { it to "Franquiciatario" })
            addAll(administratives.value.map { it to "Administrativo" })
        }
        "Alumnos" -> students.value.map { it to "Alumno" }
        "Profesores" -> teachers.value.map { it to "Profesor" }
        "Franquiciatarios" -> franchisees.value.map { it to "Franquiciatario" }
        "Administrativos" -> administratives.value.map { it to "Administrativo" }
        else -> emptyList()
    }.filter { userPair ->
        val (user, _) = userPair
        val fullName = when (user) {
            is StudentEntity -> "${user.first_name} ${user.last_name_paternal ?: ""} ${user.last_name_maternal ?: ""}".trim()
            is TeacherEntity -> "${user.first_name} ${user.last_name_paternal ?: ""} ${user.last_name_maternal ?: ""}".trim()
            is FranchiseeEntity -> "${user.first_name} ${user.last_name_paternal ?: ""} ${user.last_name_maternal ?: ""}".trim()
            is AdministrativeEntity -> "${user.first_name} ${user.last_name_paternal ?: ""} ${user.last_name_maternal ?: ""}".trim()
            else -> ""
        }
        val phone = when (user) {
            is StudentEntity -> user.phone ?: ""
            is TeacherEntity -> user.phone ?: ""
            is FranchiseeEntity -> user.phone ?: ""
            is AdministrativeEntity -> user.phone ?: ""
            else -> ""
        }
        val email = when (user) {
            is StudentEntity -> user.email ?: ""
            is TeacherEntity -> user.email ?: ""
            is FranchiseeEntity -> user.email ?: ""
            is AdministrativeEntity -> user.email ?: ""
            else -> ""
        }

        searchQuery.isEmpty() ||
                fullName.contains(searchQuery, ignoreCase = true) ||
                phone.contains(searchQuery, ignoreCase = true) ||
                email.contains(searchQuery, ignoreCase = true)
    }

    val totalPages = (filteredUsers.size + rowsPerPage - 1) / rowsPerPage
    val paginatedUsers = filteredUsers
        .drop((currentPage - 1) * rowsPerPage)
        .take(rowsPerPage)

    Scaffold(
        containerColor = AppColors.Background,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { expandedAdd = true },
                    containerColor = AppColors.Primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar usuario",
                        tint = AppColors.OnPrimary
                    )
                }
                DropdownMenu(
                    expanded = expandedAdd,
                    onDismissRequest = { expandedAdd = false },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    listOf("Alumno", "Profesor", "Franquiciatario", "Administrativo").forEach { userType ->
                        DropdownMenuItem(
                            text = { Text("Agregar $userType") },
                            onClick = {
                                selectedUserType = userType
                                expandedAdd = false
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                selectedUserType != null && userToEdit == null -> {
                    when (selectedUserType) {
                        "Alumno" -> AddAlumnoScreen(
                            onDismiss = {
                                selectedUserType = null
                                students.value = repository.getAllStudents()
                            },
                            repository = repository
                        )
                        "Profesor" -> AddProfesorScreen(
                            onDismiss = {
                                selectedUserType = null
                                teachers.value = repository.getAllTeachers()
                            },
                            repository = repository
                        )
                        "Franquiciatario" -> AddFranquiciatarioScreen(
                            onDismiss = {
                                selectedUserType = null
                                franchisees.value = repository.getAllFranchisees()
                            },
                            repository = repository
                        )
                        "Administrativo" -> AddAdministrativoScreen(
                            onDismiss = {
                                selectedUserType = null
                                administratives.value = repository.getAllAdministratives()
                            },
                            repository = repository
                        )
                    }
                }
                userToEdit != null -> {
                    Text("Función de edición no implementada aún")
                }
                selectedProfile != null -> {
                    when (selectedProfile!!.first) {
                        "Alumno" -> ViewAlumnoScreen(
                            studentId = selectedProfile!!.second,
                            repository = repository,
                            navController = navController,
                            onDismiss = { selectedProfile = null }
                        )
                        "Profesor" -> ViewProfesorScreen(
                            teacherId = selectedProfile!!.second,
                            repository = repository,
                            navController = navController,
                            onDismiss = { selectedProfile = null }
                        )
                        "Franquiciatario" -> ViewFranquiciatarioScreen(
                            franchiseeId = selectedProfile!!.second,
                            repository = repository,
                            navController = navController,
                            onDismiss = { selectedProfile = null }
                        )
                        "Administrativo" -> ViewAdministrativoScreen(
                            administrativeId = selectedProfile!!.second,
                            repository = repository,
                            navController = navController,
                            onDismiss = { selectedProfile = null }
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lista de Usuarios",
                                style = MaterialTheme.typography.headlineSmall,
                                color = AppColors.Primary,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it; currentPage = 1 },
                                placeholder = { Text("Buscar...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = Color.Gray
                                    )
                                },
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(56.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = AppColors.Primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TabRow(
                            selectedTabIndex = when (selectedTab) {
                                "Todos" -> 0
                                "Alumnos" -> 1
                                "Profesores" -> 2
                                "Franquiciatarios" -> 3
                                "Administrativos" -> 4
                                else -> 0
                            },
                            containerColor = Color.White,
                            contentColor = AppColors.Primary
                        ) {
                            listOf("Todos", "Alumnos", "Profesores", "Franquiciatarios", "Administrativos").forEach { tab ->
                                Tab(
                                    text = { Text(tab) },
                                    selected = selectedTab == tab,
                                    onClick = {
                                        selectedTab = tab
                                        currentPage = 1
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 4.dp,
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White
                        ) {
                            LazyColumn {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = false,
                                            onCheckedChange = { /* TODO */ },
                                            modifier = Modifier.width(48.dp)
                                        )
                                        Text(
                                            "TIPO",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1.5f)
                                        )
                                        Text(
                                            "NOMBRE",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(2f)
                                        )
                                        Text(
                                            "TELÉFONO",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(2f)
                                        )
                                        Text(
                                            "EMAIL",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(2f)
                                        )
                                        Text(
                                            "ACTIVO",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.weight(2f))
                                    }
                                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                                }

                                items(paginatedUsers) { userPair ->
                                    var isHovered by remember { mutableStateOf(false) }
                                    val (user, type) = userPair
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isHovered) Color(0xFFF5F5F5)
                                                else Color.White
                                            )
                                            .pointerInput(Unit) {
                                                awaitPointerEventScope {
                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        when (event.type) {
                                                            PointerEventType.Enter -> isHovered = true
                                                            PointerEventType.Exit -> isHovered = false
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = false,
                                                onCheckedChange = { /* TODO */ },
                                                modifier = Modifier.width(48.dp)
                                            )
                                            when (type) {
                                                "Alumno" -> {
                                                    val student = user as StudentEntity
                                                    val studentFullName = "${student.first_name} ${student.last_name_paternal ?: ""} ${student.last_name_maternal ?: ""}".trim()
                                                    Text("Alumno", modifier = Modifier.weight(1.5f), color = AppColors.TextColor)
                                                    Text(studentFullName, modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(student.phone ?: "-", modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(student.email ?: "-", modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(if (student.active == 1L) "Sí" else "No", modifier = Modifier.weight(1f), color = AppColors.TextColor)
                                                    IconButton(
                                                        onClick = {
                                                            userToEdit = userPair
                                                            selectedUserType = "Alumno"
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Actualizar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            userToDelete = userPair
                                                            showDeleteDialog = true
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Eliminar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            selectedProfile = Pair("Alumno", student.id)
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = "Ver Perfil",
                                                            tint = AppColors.Primary
                                                        )
                                                    }
                                                }
                                                "Profesor" -> {
                                                    val teacher = user as TeacherEntity
                                                    val teacherFullName = "${teacher.first_name} ${teacher.last_name_paternal ?: ""} ${teacher.last_name_maternal ?: ""}".trim()
                                                    Text("Profesor", modifier = Modifier.weight(1.5f), color = AppColors.TextColor)
                                                    Text(teacherFullName, modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(teacher.phone ?: "-", modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(teacher.email ?: "-", modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(if (teacher.active == 1L) "Sí" else "No", modifier = Modifier.weight(1f), color = AppColors.TextColor)
                                                    IconButton(
                                                        onClick = {
                                                            userToEdit = userPair
                                                            selectedUserType = "Profesor"
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Actualizar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            userToDelete = userPair
                                                            showDeleteDialog = true
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Eliminar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            selectedProfile = Pair("Profesor", teacher.id)
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = "Ver Perfil",
                                                            tint = AppColors.Primary
                                                        )
                                                    }
                                                }
                                                "Franquiciatario" -> {
                                                    val franchisee = user as FranchiseeEntity
                                                    val franchiseeFullName = "${franchisee.first_name} ${franchisee.last_name_paternal ?: ""} ${franchisee.last_name_maternal ?: ""}".trim()
                                                    Text("Franquiciatario", modifier = Modifier.weight(1.5f), color = AppColors.TextColor)
                                                    Text(franchiseeFullName, modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(franchisee.phone ?: "-", modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(franchisee.email ?: "-", modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(if (franchisee.active == 1L) "Sí" else "No", modifier = Modifier.weight(1f), color = AppColors.TextColor)
                                                    IconButton(
                                                        onClick = {
                                                            userToEdit = userPair
                                                            selectedUserType = "Franquiciatario"
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Actualizar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            userToDelete = userPair
                                                            showDeleteDialog = true
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Eliminar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            selectedProfile = Pair("Franquiciatario", franchisee.id)
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = "Ver Perfil",
                                                            tint = AppColors.Primary
                                                        )
                                                    }
                                                }
                                                "Administrativo" -> {
                                                    val admin = user as AdministrativeEntity
                                                    val adminFullName = "${admin.first_name} ${admin.last_name_paternal ?: ""} ${admin.last_name_maternal ?: ""}".trim()
                                                    Text("Administrativo", modifier = Modifier.weight(1.5f), color = AppColors.TextColor)
                                                    Text(adminFullName, modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(admin.phone ?: "-", modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(admin.email ?: "-", modifier = Modifier.weight(2f), color = AppColors.TextColor)
                                                    Text(if (admin.active == 1L) "Sí" else "No", modifier = Modifier.weight(1f), color = AppColors.TextColor)
                                                    IconButton(
                                                        onClick = {
                                                            userToEdit = userPair
                                                            selectedUserType = "Administrativo"
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Actualizar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            userToDelete = userPair
                                                            showDeleteDialog = true
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Eliminar",
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            selectedProfile = Pair("Administrativo", admin.id)
                                                        },
                                                        modifier = Modifier.weight(0.5f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = "Ver Perfil",
                                                            tint = AppColors.Primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (paginatedUsers.last() != userPair) {
                                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { if (currentPage > 1) currentPage-- },
                                    enabled = currentPage > 1
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Página anterior",
                                        tint = if (currentPage > 1) AppColors.Primary else Color.Gray
                                    )
                                }
                                Text(
                                    text = "$currentPage de $totalPages",
                                    color = AppColors.TextColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(
                                    onClick = { if (currentPage < totalPages) currentPage++ },
                                    enabled = currentPage < totalPages
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Página siguiente",
                                        tint = if (currentPage < totalPages) AppColors.Primary else Color.Gray
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Filas por página:",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Box {
                                    OutlinedButton(
                                        onClick = { expandedRowsPerPage = true },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color.Gray
                                        ),
                                        border = BorderStroke(1.dp, Color.Gray),
                                        modifier = Modifier.width(100.dp)
                                    ) {
                                        Text("$rowsPerPage", color = Color.Gray)
                                    }
                                    DropdownMenu(
                                        expanded = expandedRowsPerPage,
                                        onDismissRequest = { expandedRowsPerPage = false }
                                    ) {
                                        listOf(5, 10, 20, 50).forEach { rows ->
                                            DropdownMenuItem(
                                                text = { Text("$rows") },
                                                onClick = {
                                                    rowsPerPage = rows
                                                    currentPage = 1
                                                    expandedRowsPerPage = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de eliminación
        if (showDeleteDialog && userToDelete != null) {
            val (user, type) = userToDelete!!
            val userName = when (user) {
                is StudentEntity -> "${user.first_name} ${user.last_name_paternal ?: ""} ${user.last_name_maternal ?: ""}".trim()
                is TeacherEntity -> "${user.first_name} ${user.last_name_paternal ?: ""} ${user.last_name_maternal ?: ""}".trim()
                is FranchiseeEntity -> "${user.first_name} ${user.last_name_paternal ?: ""} ${user.last_name_maternal ?: ""}".trim()
                is AdministrativeEntity -> "${user.first_name} ${user.last_name_paternal ?: ""} ${user.last_name_maternal ?: ""}".trim()
                else -> ""
            }
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    userToDelete = null
                },
                title = {
                    Text(
                        "Eliminar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "¿Estás seguro que quieres eliminar a $userName?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when (type) {
                                "Alumno" -> {
                                    val student = user as StudentEntity
                                    repository.deleteStudent(student.id)
                                    students.value = repository.getAllStudents()
                                }
                                "Profesor" -> {
                                    val teacher = user as TeacherEntity
                                    repository.deleteTeacher(teacher.id)
                                    teachers.value = repository.getAllTeachers()
                                }
                                "Franquiciatario" -> {
                                    val franchisee = user as FranchiseeEntity
                                    repository.deleteFranchisee(franchisee.id)
                                    franchisees.value = repository.getAllFranchisees()
                                }
                                "Administrativo" -> {
                                    val admin = user as AdministrativeEntity
                                    repository.deleteAdministrative(admin.id)
                                    administratives.value = repository.getAllAdministratives()
                                }
                            }
                            showDeleteDialog = false
                            userToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Primary)
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            userToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}