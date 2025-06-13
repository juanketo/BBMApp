package org.example.appbbmges.ui.clases

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import org.example.appbbmges.navigation.Screen
import org.example.appbbmges.navigation.SimpleNavController

// Modelo de datos para una clase
data class Clase(
    val tipo: String, // "Individual", "Grupo", "Privado"
    val fechaInicio: String, // Ejemplo: "2025-03-10 10:00"
    val fechaFin: String,
    val profesor: String,
    val aula: String,
    val disciplina: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClasesScreen(navController: SimpleNavController) {
    // Estado para la lista de clases
    var clases by remember { mutableStateOf(listOf<Clase>()) }
    // Estado para la vista seleccionada (Semana, Mes, Tarjetas)
    var vistaSeleccionada by remember { mutableStateOf("Semana") }
    // Estado para mostrar el formulario de agregar clase
    var mostrarFormulario by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Clases") },
                actions = {
                    // Botón para agregar clase
                    Button(
                        onClick = { mostrarFormulario = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("+ Agregar Clase")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Selector de vistas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { vistaSeleccionada = "Semana" },
                    enabled = vistaSeleccionada != "Semana"
                ) { Text("Semana") }
                Button(
                    onClick = { vistaSeleccionada = "Mes" },
                    enabled = vistaSeleccionada != "Mes"
                ) { Text("Mes") }
                Button(
                    onClick = { vistaSeleccionada = "Tarjetas" },
                    enabled = vistaSeleccionada != "Tarjetas"
                ) { Text("Tarjetas") }
            }

            // Contenido según la vista seleccionada
            when (vistaSeleccionada) {
                "Semana" -> VistaSemana(clases)
                "Mes" -> VistaMes(clases)
                "Tarjetas" -> VistaTarjetas(clases)
            }

            // Navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { }) {
                    Text("Ir a Eventos")
                }
                Button(onClick = { navController.navigateBack() }) {
                    Text("Regresar")
                }
            }
        }

        // Formulario para agregar clase (como un diálogo)
        if (mostrarFormulario) {
            AgregarClaseDialog(
                onDismiss = { mostrarFormulario = false },
                onClaseAgregada = { nuevaClase ->
                    clases = clases + nuevaClase
                    mostrarFormulario = false
                }
            )
        }
    }
}

// Vista de Semana (simulada)
@Composable
fun VistaSemana(clases: List<Clase>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Vista de Semana (Lunes a Domingo)", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (clases.isEmpty()) {
            Text("No hay clases programadas para esta semana",
                modifier = Modifier.padding(vertical = 16.dp))
        } else {
            clases.forEach { clase ->
                ClaseItem(clase)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Vista de Mes (simulada)
@Composable
fun VistaMes(clases: List<Clase>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Vista de Mes", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (clases.isEmpty()) {
            Text("No hay clases programadas para este mes",
                modifier = Modifier.padding(vertical = 16.dp))
        } else {
            clases.forEach { clase ->
                ClaseItem(clase)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Vista de Tarjetas
@Composable
fun VistaTarjetas(clases: List<Clase>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Vista de Tarjetas", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (clases.isEmpty()) {
            Text("No hay clases disponibles",
                modifier = Modifier.padding(vertical = 16.dp))
        } else {
            clases.forEach { clase ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = clase.color.copy(alpha = 0.2f))
                ) {
                    ClaseItem(clase)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Elemento individual de una clase
@Composable
fun ClaseItem(clase: Clase) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(clase.color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                "${clase.tipo} - ${clase.disciplina}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Profesor: ${clase.profesor}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Aula: ${clase.aula}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${clase.fechaInicio} - ${clase.fechaFin}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Diálogo para agregar clase
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarClaseDialog(onDismiss: () -> Unit, onClaseAgregada: (Clase) -> Unit) {
    var tipo by remember { mutableStateOf("Individual") }
    var profesor by remember { mutableStateOf("") }
    var aula by remember { mutableStateOf("") }
    var disciplina by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }

    // Asignar un color aleatorio para cada tipo de clase
    val color by remember(tipo) {
        mutableStateOf(
            when(tipo) {
                "Individual" -> Color(0xFF2196F3) // Azul
                "Grupo" -> Color(0xFF4CAF50)      // Verde
                "Privado" -> Color(0xFFE91E63)    // Rosa
                else -> Color(0xFF673AB7)         // Morado (default)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Clase") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Selector de tipo
                DropdownMenuTipo(
                    selectedTipo = tipo,
                    onTipoSelected = { tipo = it }
                )

                OutlinedTextField(
                    value = profesor,
                    onValueChange = { profesor = it },
                    label = { Text("Profesor") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = aula,
                    onValueChange = { aula = it },
                    label = { Text("Aula") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = disciplina,
                    onValueChange = { disciplina = it },
                    label = { Text("Disciplina") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fechaInicio,
                    onValueChange = { fechaInicio = it },
                    label = { Text("Fecha Inicio (ej: 2025-03-10 10:00)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = { fechaFin = it },
                    label = { Text("Fecha Fin (ej: 2025-03-10 11:00)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Muestra el color seleccionado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Color seleccionado: ")
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nuevaClase = Clase(
                        tipo = tipo,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        profesor = profesor,
                        aula = aula,
                        disciplina = disciplina,
                        color = color
                    )
                    onClaseAgregada(nuevaClase)
                },
                enabled = profesor.isNotBlank() &&
                        aula.isNotBlank() &&
                        disciplina.isNotBlank() &&
                        fechaInicio.isNotBlank() &&
                        fechaFin.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Dropdown para seleccionar tipo de clase
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuTipo(selectedTipo: String, onTipoSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val tipos = listOf("Individual", "Grupo", "Privado")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedTipo,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de Clase") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            tipos.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo) },
                    onClick = {
                        onTipoSelected(tipo)
                        expanded = false
                    }
                )
            }
        }
    }
}