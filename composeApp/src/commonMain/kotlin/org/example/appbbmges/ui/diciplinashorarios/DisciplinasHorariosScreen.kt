package org.example.appbbmges.ui.diciplinashorarios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.example.appbbmges.ui.diciplinashorarios.formclass.AddNewClassMuestra
import org.example.appbbmges.ui.diciplinashorarios.formclass.AddNewClass
import org.example.appbbmges.ui.usuarios.AppColors

// --- Data Definitions (Unchanged) ---

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

// --- New Definitions for Internal Navigation of DisciplinasHorariosScreen ---

sealed class DisciplinasHorariosScreenState {
    object Calendar : DisciplinasHorariosScreenState()
    object AddClassMuestra : DisciplinasHorariosScreenState()
    object AddNewClass : DisciplinasHorariosScreenState()
}

// --- Modified Main Composable ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplinasHorariosScreen(navController: SimpleNavController, repository: Repository) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var currentWeek by remember { mutableStateOf(getCurrentWeekForDate(today)) }
    val currentMonth = remember(today) { getMonthYearString(today) }

    val scheduledClasses = remember { mutableStateListOf<ClassSchedule>() }

    var currentScreenState by remember { mutableStateOf<DisciplinasHorariosScreenState>(DisciplinasHorariosScreenState.Calendar) }

    // Callback for when AddNewClassMuestra needs to close
    val onDismissClassMuestra: () -> Unit = {
        currentScreenState = DisciplinasHorariosScreenState.Calendar
    }

    // Callback for when AddNewClass needs to close
    val onDismissNewClass: () -> Unit = {
        currentScreenState = DisciplinasHorariosScreenState.Calendar
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreenState) {
            is DisciplinasHorariosScreenState.Calendar -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(8.dp)
                ) {
                    ActionButtons(
                        onNewDisciplineClick = { currentScreenState = DisciplinasHorariosScreenState.AddNewClass },
                        onClassMuestraClick = { currentScreenState = DisciplinasHorariosScreenState.AddClassMuestra }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MonthHeader(currentMonth = currentMonth)

                    Spacer(modifier = Modifier.height(8.dp))

                    WeeklyCalendar(
                        selectedWeek = currentWeek,
                        scheduledClasses = scheduledClasses
                    )
                }
            }
            is DisciplinasHorariosScreenState.AddClassMuestra -> {
                // CORRECTED: Pass repository and modifier
                AddNewClassMuestra(
                    onDismiss = onDismissClassMuestra,
                    repository = repository, // Pass the repository
                    modifier = Modifier.fillMaxSize() // Pass a default modifier
                )
            }
            is DisciplinasHorariosScreenState.AddNewClass -> {
                // CORRECTED: Assuming AddNewClass will also accept repository and modifier
                AddNewClass(
                    onDismiss = onDismissNewClass,
                    repository = repository, // Pass the repository
                    modifier = Modifier.fillMaxSize() // Pass a default modifier
                )
            }
        }
    }
}

// --- Components (Unchanged) ---

@Composable
fun ActionButtons(onNewDisciplineClick: () -> Unit, onClassMuestraClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Button Add Student to Class
        Button(
            onClick = { /* Action to add student to class */ },
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

        // Sample Class Button
        Button(
            onClick = onClassMuestraClick,
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

        // New Discipline Button (now navigates to AddNewClass)
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

        // Day Cells
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

// --- Helper Functions (Unchanged) ---

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
        else -> ""
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
        else -> ""
    }
    return "$monthName ${date.year}"
}

fun generateTimeSlots(): List<TimeSlot> {
    val timeSlots = mutableListOf<TimeSlot>()
    for (hour in 7..22) {
        val formattedHour = hour.toString().padStart(2, '0')
        timeSlots.add(TimeSlot(hour, 0, "${formattedHour}:00"))
        if (hour < 22) {
            timeSlots.add(TimeSlot(hour, 30, "${formattedHour}:30"))
        }
    }
    return timeSlots
}