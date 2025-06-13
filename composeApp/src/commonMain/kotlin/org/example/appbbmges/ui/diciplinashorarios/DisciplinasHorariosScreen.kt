package org.example.appbbmges.ui.diciplinashorarios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.appbbmges.navigation.SimpleNavController
import kotlinx.datetime.*
import org.example.appbbmges.data.Repository


data class CalendarDay(
    val dayName: String,
    val dayNumber: Int,
    val month: Int,
    val year: Int,
    val date: LocalDate
)

data class TimeSlot(
    val hour: Int,
    val minute: Int,
    val displayTime: String
)

data class ClassSchedule(
    val className: String,
    val teacher: String,
    val students: Int,
    val description: String,
    val isPublic: Boolean,
    val isInPerson: Boolean,
    val includedFor: String,
    val status: String,
    val type: String,
    val style: String,
    val level: String,
    val day: CalendarDay,
    val timeSlot: TimeSlot
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplinasHorariosScreen(navController: SimpleNavController, repository: Repository) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var currentWeek by remember { mutableStateOf(getCurrentWeekForDate(today)) }
    val currentMonth = remember(today) { getMonthYearString(today) }

    // State for dialog visibility and form data
    var showDialog by remember { mutableStateOf(false) }
    var formData by remember { mutableStateOf(ClassSchedule(
        className = "",
        teacher = "",
        students = 0,
        description = "",
        isPublic = true,
        isInPerson = true,
        includedFor = "No Incluido",
        status = "PLANIFICADO",
        type = "Regular",
        style = "Selecciona Disciplina",
        level = "Selecciona Nivel",
        day = currentWeek.first(), // Default to first day
        timeSlot = generateTimeSlots().first() // Default to first time slot
    )) }

    // List to store scheduled classes (in memory, since there's no database)
    val scheduledClasses = remember { mutableStateListOf<ClassSchedule>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        // Botones de acción superiores
        ActionButtons(onNewDisciplineClick = { showDialog = true })

        Spacer(modifier = Modifier.height(8.dp))

        // Header con el mes y año actual
        MonthHeader(currentMonth = currentMonth)

        Spacer(modifier = Modifier.height(8.dp))

        // Calendario semanal
        WeeklyCalendar(
            selectedWeek = currentWeek,
            scheduledClasses = scheduledClasses
        )
    }

    // Show the dialog when triggered
    if (showDialog) {
        ClassFormDialog(
            formData = formData,
            onFormDataChange = { formData = it },
            onDismiss = { showDialog = false },
            onSave = {
                scheduledClasses.add(formData)
                showDialog = false
            },
            daysOfWeek = currentWeek,
            timeSlots = generateTimeSlots()
        )
    }
}

@Composable
fun ActionButtons(onNewDisciplineClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón Agregar Alumno a Clase
        Button(
            onClick = { /* Acción agregar alumno a clase */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Agregar Alumno",
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Botón Clase Muestra
        Button(
            onClick = { /* Acción clase muestra */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "Clase Muestra",
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Botón Nueva Disciplina
        Button(
            onClick = onNewDisciplineClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text(
                text = "Nueva Disciplina",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun MonthHeader(currentMonth: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentMonth,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun WeeklyCalendar(selectedWeek: List<CalendarDay>, scheduledClasses: List<ClassSchedule>) {
    val timeSlots = remember { generateTimeSlots() }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            WeekHeader(selectedWeek)

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                timeSlots.forEachIndexed { index, timeSlot ->
                    TimeSlotRow(
                        timeSlot = timeSlot,
                        daysOfWeek = selectedWeek,
                        isEvenRow = index % 2 == 0,
                        scheduledClasses = scheduledClasses
                    )
                }
            }
        }
    }
}

@Composable
fun WeekHeader(daysOfWeek: List<CalendarDay>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(45.dp)
        )

        daysOfWeek.forEach { day ->
            DayHeaderCell(
                day = day,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DayHeaderCell(day: CalendarDay, modifier: Modifier = Modifier) {
    val backgroundColor = when (day.dayName.uppercase()) {
        "LUNES" -> Color(0xFFFFE5E5)
        "MARTES" -> Color(0xFFE5F3FF)
        "MIÉRCOLES" -> Color(0xFFE5FFE5)
        "JUEVES" -> Color(0xFFFFF5E5)
        "VIERNES" -> Color(0xFFE5E5FF)
        "SÁBADO" -> Color(0xFFFFE5F5)
        "DOMINGO" -> Color(0xFFF5E5FF)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .height(45.dp)
            .padding(horizontal = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayName.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = day.dayNumber.toString(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TimeSlotRow(
    timeSlot: TimeSlot,
    daysOfWeek: List<CalendarDay>,
    isEvenRow: Boolean,
    scheduledClasses: List<ClassSchedule>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        
        TimeCell(
            timeSlot = timeSlot,
            modifier = Modifier.weight(1f),
            isEvenRow = isEvenRow
        )

        // Celdas de días
        daysOfWeek.forEach { day ->
            ScheduleCell(
                day = day,
                timeSlot = timeSlot,
                modifier = Modifier.weight(1f),
                isEvenRow = isEvenRow,
                scheduledClasses = scheduledClasses
            )
        }
    }
}

@Composable
fun TimeCell(timeSlot: TimeSlot, modifier: Modifier = Modifier, isEvenRow: Boolean) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = if (isEvenRow) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeSlot.displayTime,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScheduleCell(
    day: CalendarDay,
    timeSlot: TimeSlot,
    modifier: Modifier = Modifier,
    isEvenRow: Boolean,
    scheduledClasses: List<ClassSchedule>
) {
    // Find if there's a class scheduled for this day and time slot
    val scheduledClass = scheduledClasses.find {
        it.day.date == day.date && it.timeSlot.displayTime == timeSlot.displayTime
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 1.dp)
            .background(
                color = if (isEvenRow) Color.White else Color(0xFFFAFAFA),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (scheduledClass != null) {
            // Display the scheduled class in a card
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = scheduledClass.className,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${scheduledClass.students} alumnos",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassFormDialog(
    formData: ClassSchedule,
    onFormDataChange: (ClassSchedule) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    daysOfWeek: List<CalendarDay>,
    timeSlots: List<TimeSlot>
) {
    // Dropdown menu states
    var teacherExpanded by remember { mutableStateOf(false) }
    var styleExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    var timeSlotExpanded by remember { mutableStateOf(false) }
    var includedForExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    // Dropdown options
    val teachers = listOf("Ana García", "Luis Martínez", "Sofía Ramírez")
    val disciplines = listOf(
        "Ballet Clásico",
        "Danza Contemporánea",
        "Jazz Moderno",
        "Hip Hop",
        "Danza Lírica",
        "Tap",
        "Flamenco",
        "Salsa",
        "Bachata",
        "Tango",
        "Danza Africana",
        "Breakdance",
        "Danza Moderna",
        "Ballet Moderno",
        "Danza Urbana",
        "K-Pop",
        "Danza Folclórica",
        "Swing",
        "Danza del Vientre"
    )
    val levels = listOf("Baby", "Kids", "Primary 1", "Primary 2")
    val includedForOptions = listOf("No Incluido", "Incluido para Todos", "Incluido para Miembros")
    val typeOptions = listOf("Regular", "Especial", "Intensiva")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Class Name
                OutlinedTextField(
                    value = formData.className,
                    onValueChange = { onFormDataChange(formData.copy(className = it)) },
                    label = { Text("Nombre de la Clase") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Day (Fecha)
                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = !dayExpanded }
                ) {
                    OutlinedTextField(
                        value = "${formData.day.dayName} ${formData.day.dayNumber}",
                        onValueChange = {},
                        label = { Text("Fecha") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        daysOfWeek.forEach { day ->
                            DropdownMenuItem(
                                text = { Text("${day.dayName} ${day.dayNumber}") },
                                onClick = {
                                    onFormDataChange(formData.copy(day = day))
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }

                // Time Slot (Horario)
                ExposedDropdownMenuBox(
                    expanded = timeSlotExpanded,
                    onExpandedChange = { timeSlotExpanded = !timeSlotExpanded }
                ) {
                    OutlinedTextField(
                        value = formData.timeSlot.displayTime,
                        onValueChange = {},
                        label = { Text("Horario") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeSlotExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = timeSlotExpanded,
                        onDismissRequest = { timeSlotExpanded = false }
                    ) {
                        timeSlots.forEach { slot ->
                            DropdownMenuItem(
                                text = { Text(slot.displayTime) },
                                onClick = {
                                    onFormDataChange(formData.copy(timeSlot = slot))
                                    timeSlotExpanded = false
                                }
                            )
                        }
                    }
                }

                // Teacher
                ExposedDropdownMenuBox(
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = !teacherExpanded }
                ) {
                    OutlinedTextField(
                        value = formData.teacher,
                        onValueChange = {},
                        label = { Text("Profesor") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = teacherExpanded,
                        onDismissRequest = { teacherExpanded = false }
                    ) {
                        teachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = { Text(teacher) },
                                onClick = {
                                    onFormDataChange(formData.copy(teacher = teacher))
                                    teacherExpanded = false
                                }
                            )
                        }
                    }
                }

                // Number of Students
                OutlinedTextField(
                    value = formData.students.toString(),
                    onValueChange = {
                        val students = it.toIntOrNull() ?: 0
                        onFormDataChange(formData.copy(students = students))
                    },
                    label = { Text("Alumnos") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Valid",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                // Description
                OutlinedTextField(
                    value = formData.description,
                    onValueChange = { onFormDataChange(formData.copy(description = it)) },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )

                // Public Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pública")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = formData.isPublic,
                        onCheckedChange = { onFormDataChange(formData.copy(isPublic = it)) }
                    )
                }

                // In-Person Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Presencial")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = formData.isInPerson,
                        onCheckedChange = { onFormDataChange(formData.copy(isInPerson = it)) }
                    )
                }

                // Included For Dropdown
                ExposedDropdownMenuBox(
                    expanded = includedForExpanded,
                    onExpandedChange = { includedForExpanded = !includedForExpanded }
                ) {
                    OutlinedTextField(
                        value = formData.includedFor,
                        onValueChange = {},
                        label = { Text("Incluido para") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = includedForExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = includedForExpanded,
                        onDismissRequest = { includedForExpanded = false }
                    ) {
                        includedForOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onFormDataChange(formData.copy(includedFor = option))
                                    includedForExpanded = false
                                }
                            )
                        }
                    }
                }

                // Status Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onFormDataChange(formData.copy(status = "PLANIFICADO")) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formData.status == "PLANIFICADO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("PLANIFICADO")
                    }
                    Button(
                        onClick = { onFormDataChange(formData.copy(status = "ACTIVO")) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formData.status == "ACTIVO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("ACTIVO")
                    }
                }

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = formData.type,
                        onValueChange = {},
                        label = { Text("Tipo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onFormDataChange(formData.copy(type = type))
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = styleExpanded,
                    onExpandedChange = { styleExpanded = !styleExpanded }
                ) {
                    OutlinedTextField(
                        value = formData.style,
                        onValueChange = {},
                        label = { Text("Estilo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = styleExpanded,
                        onDismissRequest = { styleExpanded = false }
                    ) {
                        disciplines.forEach { discipline ->
                            DropdownMenuItem(
                                text = { Text(discipline) },
                                onClick = {
                                    onFormDataChange(formData.copy(style = discipline))
                                    styleExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = levelExpanded,
                    onExpandedChange = { levelExpanded = !levelExpanded }
                ) {
                    OutlinedTextField(
                        value = formData.level,
                        onValueChange = {},
                        label = { Text("Nivel") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = levelExpanded,
                        onDismissRequest = { levelExpanded = false }
                    ) {
                        levels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    onFormDataChange(formData.copy(level = level))
                                    levelExpanded = false
                                }
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("ARCHIVAR CLASE")
                    }
                    Button(
                        onClick = { /* Handle copy creation */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("CREAR COPIA")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("GUARDAR")
                    }
                }
            }
        }
    }
}

fun getCurrentWeekForDate(referenceDate: LocalDate): List<CalendarDay> {
    val currentDayOfWeek = referenceDate.dayOfWeek.ordinal
    val startOfWeek = referenceDate.minus(currentDayOfWeek, DateTimeUnit.DAY)

    return (0..6).map { dayOffset ->
        val date = startOfWeek.plus(dayOffset, DateTimeUnit.DAY)
        CalendarDay(
            dayName = getDayNameInSpanish(date.dayOfWeek),
            dayNumber = date.dayOfMonth,
            month = date.monthNumber,
            year = date.year,
            date = date
        )
    }
}

fun getDayNameInSpanish(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Lunes"
        DayOfWeek.TUESDAY -> "Martes"
        DayOfWeek.WEDNESDAY -> "Miércoles"
        DayOfWeek.THURSDAY -> "Jueves"
        DayOfWeek.FRIDAY -> "Viernes"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
        else -> TODO()
    }
}

fun getMonthYearString(date: LocalDate): String {
    val monthName = when (date.monthNumber) {
        1 -> "ENERO"
        2 -> "FEBRERO"
        3 -> "MARZO"
        4 -> "ABRIL"
        5 -> "MAYO"
        6 -> "JUNIO"
        7 -> "JULIO"
        8 -> "AGOSTO"
        9 -> "SEPTIEMBRE"
        10 -> "OCTUBRE"
        11 -> "NOVIEMBRE"
        12 -> "DICIEMBRE"
        else -> "ENERO"
    }
    return "$monthName ${date.year}"
}

fun generateTimeSlots(): List<TimeSlot> {
    val slots = mutableListOf<TimeSlot>()

    for (hour in 9..20) {
        val displayTime = when (hour) {
            9 -> "9:00 AM"
            10 -> "10:00 AM"
            11 -> "11:00 AM"
            12 -> "12:00 PM"
            13 -> "13:00 PM"
            14 -> "14:00 PM"
            15 -> "15:00 PM"
            16 -> "16:00 PM"
            17 -> "17:00 PM"
            18 -> "18:00 PM"
            19 -> "19:00 PM"
            20 -> "20:00 PM"
            else -> "${hour}:00"
        }

        slots.add(
            TimeSlot(
                hour = hour,
                minute = 0,
                displayTime = displayTime
            )
        )
    }

    return slots
}