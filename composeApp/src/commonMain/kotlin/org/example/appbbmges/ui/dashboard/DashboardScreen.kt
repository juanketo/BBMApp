package org.example.appbbmges.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.appbbmges.navigation.SimpleNavController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.atan2
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import org.example.appbbmges.data.Repository
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.appbbmges.StudentEntity

@Composable
fun DashboardScreen(navController: SimpleNavController, repository: Repository) {

    var studentCount by remember { mutableStateOf(0L) }
    var teacherCount by remember { mutableStateOf(0L) }
    var activeBranchesCount by remember { mutableStateOf(0L) }
    var maleCount by remember { mutableStateOf(0L) }
    var femaleCount by remember { mutableStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()

    fun refreshCounts() {
        coroutineScope.launch {
            studentCount = repository.getStudentCount()
            teacherCount = repository.getTeacherCount()
            activeBranchesCount = repository.getActiveBranchesCount()

            val genders = repository.getStudentsByGender()
            maleCount = genders.first
            femaleCount = genders.second
        }
    }

    LaunchedEffect(Unit) {
        refreshCounts()
    }

    // Refrescar datos periódicamente
    LaunchedEffect(repository) {
        while (true) {
            kotlinx.coroutines.delay(5000) // Refrescar cada 5 segundos
            refreshCounts()
        }
    }

    // Calcular porcentajes reales
    val totalStudents = maleCount + femaleCount
    val malePercentage = if (totalStudents > 0) (maleCount.toFloat() / totalStudents) * 100 else 0f
    val femalePercentage = if (totalStudents > 0) (femaleCount.toFloat() / totalStudents) * 100 else 0f

    // Datos para la gráfica (mostrando "Niños/Niñas" pero con datos reales)
    val genderData = listOf(
        Pair("Niños", malePercentage),  // Datos de "masculino"
        Pair("Niñas", femalePercentage) // Datos de "femenino"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                icon = Icons.Outlined.People,
                title = studentCount.toString(),
                subtitle = "Total de Alumnos",
                backgroundColor = Color(0xFFFBACB9),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                icon = Icons.Outlined.School,
                title = teacherCount.toString(),
                subtitle = "Total de Profesores",
                backgroundColor = Color(0xFFFFE4B5),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                icon = Icons.Outlined.Store,
                title = activeBranchesCount.toString(),
                subtitle = "Sucursales Activas",
                backgroundColor = Color(0xFFB8E6B8),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmptyCard(
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                val colors = listOf(Color(0xFFADD8E6), Color(0xFFFBACB9))
                var selectedSlice by remember { mutableStateOf(-1) }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Distribución por Género",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .padding(4.dp)
                    ) {
                        AnimatedPieChart(
                            data = genderData,
                            colors = colors,
                            selectedSlice = selectedSlice,
                            onSliceSelected = { selectedSlice = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selectedSlice == 0) Color(0xFFF0F0F0) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .clickable { selectedSlice = if (selectedSlice == 0) -1 else 0 },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colors[0], RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Niños: ${malePercentage.toInt()}%",
                                fontSize = if (selectedSlice == 0) 15.sp else 13.sp,
                                color = if (selectedSlice == 0) Color.Black else Color.DarkGray,
                                fontWeight = if (selectedSlice == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selectedSlice == 1) Color(0xFFF0F0F0) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .clickable { selectedSlice = if (selectedSlice == 1) -1 else 1 },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colors[1], RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Niñas: ${femalePercentage.toInt()}%",
                                fontSize = if (selectedSlice == 1) 15.sp else 13.sp,
                                color = if (selectedSlice == 1) Color.Black else Color.DarkGray,
                                fontWeight = if (selectedSlice == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            EmptyCard(
                modifier = Modifier.weight(2f).fillMaxHeight()
            ) {
                BirthdayContent(repository = repository)
            }
        }
    }
}

@Composable
fun BirthdayContent(repository: Repository) {
    val coroutineScope = rememberCoroutineScope()
    var birthdayStudents by remember { mutableStateOf<List<StudentEntity>>(emptyList()) }

    fun loadBirthdayStudents() {
        coroutineScope.launch {
            // Obtener todos los estudiantes activos
            val allStudents = repository.getAllStudents().filter { it.active == 1L }

            val currentMonth = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .monthNumber

            // Filtrar estudiantes que cumplen años este mes
            birthdayStudents = allStudents.filter { student ->
                student.birth_date?.let { birthDate ->
                    when {
                        birthDate.contains("/") -> {
                            val parts = birthDate.split("/")
                            if (parts.size >= 2) {
                                try {
                                    parts[1].toInt() == currentMonth
                                } catch (_: Exception) { false }
                            } else false
                        }
                        // Formato YYYY-MM-DD (como 2017-09-01)
                        birthDate.contains("-") -> {
                            val parts = birthDate.split("-")
                            if (parts.size >= 2 && parts[0].length == 4) {
                                try {
                                    parts[1].toInt() == currentMonth
                                } catch (_: Exception) { false }
                            } else false
                        }
                        else -> false
                    }
                } ?: false
            }.sortedBy { student ->
                // Ordenar por día del mes
                student.birth_date?.let { birthDate ->
                    try {
                        when {
                            birthDate.contains("/") -> birthDate.split("/")[0].toInt()
                            birthDate.contains("-") -> {
                                val parts = birthDate.split("-")
                                if (parts[0].length == 4) parts[2].toInt() else parts[0].toInt()
                            }
                            else -> 0
                        }
                    } catch (_: Exception) { 0 }
                } ?: 0
            }
        }
    }

    LaunchedEffect(Unit) {
        loadBirthdayStudents()
    }

    LaunchedEffect(repository) {
        while (true) {
            kotlinx.coroutines.delay(30000)
            loadBirthdayStudents()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cumpleaños del Mes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFF800080), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            if (birthdayStudents.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cake,
                        contentDescription = "No hay cumpleaños",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay cumpleaños este mes",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(birthdayStudents.size) { index ->
                        BirthdayItem(birthdayStudents[index])
                    }
                }
            }
        }
    }
}

@Composable
fun BirthdayItem(student: StudentEntity) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFF800080), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .border(2.dp, Color(0xFF800080), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Face,
                contentDescription = "Avatar",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF800080)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildString {
                append(student.first_name)
                student.last_name_paternal?.let { append(" $it") }
                student.last_name_maternal?.let { append(" $it") }
            }.trim(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E2E2E),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = student.birth_date?.let { formatBirthDate(it) } ?: "Sin fecha",
            fontSize = 12.sp,
            color = Color(0xFF4A4A4A),
            textAlign = TextAlign.Center
        )
    }
}

fun formatBirthDate(dateStr: String): String {
    return try {
        when {
            // Formato DD/MM/YYYY (como 01/09/2017)
            dateStr.contains("/") -> {
                val parts = dateStr.split("/")
                if (parts.size == 3) {
                    val day = parts[0]
                    val month = parts[1]
                    val year = parts[2]
                    val monthName = getMonthName(month.toInt())
                    "$day de $monthName"
                } else dateStr
            }
            // Formato YYYY-MM-DD (como 2017-09-01)
            dateStr.contains("-") -> {
                val parts = dateStr.split("-")
                if (parts.size == 3 && parts[0].length == 4) {
                    val year = parts[0]
                    val month = parts[1]
                    val day = parts[2]
                    val monthName = getMonthName(month.toInt())
                    "$day de $monthName"
                } else dateStr
            }
            else -> dateStr
        }
    } catch (_: Exception) {
        dateStr
    }
}

fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Enero"
        2 -> "Febrero"
        3 -> "Marzo"
        4 -> "Abril"
        5 -> "Mayo"
        6 -> "Junio"
        7 -> "Julio"
        8 -> "Agosto"
        9 -> "Septiembre"
        10 -> "Octubre"
        11 -> "Noviembre"
        12 -> "Diciembre"
        else -> "Mes $month"
    }
}

@Composable
fun AnimatedPieChart(
    data: List<Pair<String, Float>>,
    colors: List<Color>,
    selectedSlice: Int,
    onSliceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val total = data.sumOf { it.second.toDouble() }.toFloat()
    val animatedValues = remember(data) {
        data.map { Animatable(0f) }
    }
    val sliceScales = remember(data) {
        data.map { Animatable(1f) }
    }

    LaunchedEffect(data) {
        data.forEachIndexed { index, (_, value) ->
            coroutineScope.launch {
                animatedValues[index].animateTo(
                    targetValue = if (total > 0) value / total else 0f,
                    animationSpec = tween(durationMillis = 1000, delayMillis = index * 100)
                )
            }
        }
    }

    LaunchedEffect(selectedSlice) {
        sliceScales.forEachIndexed { index, animatable ->
            coroutineScope.launch {
                animatable.animateTo(
                    targetValue = if (index == selectedSlice) 1.05f else 1f,
                    animationSpec = tween(150)
                )
            }
        }
    }

    Canvas(modifier = modifier
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = min(centerX, centerY)

                val dx = offset.x - centerX
                val dy = offset.y - centerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                if (distance <= radius) {
                    val angle = (atan2(dy, dx) * (180f / PI.toFloat()) + 90f + 360f) % 360f
                    var startAngle = 0f
                    var newSelectedSlice = -1

                    for (i in data.indices) {
                        val sweepAngle = animatedValues[i].value * 360f
                        if (angle >= startAngle && angle < startAngle + sweepAngle) {
                            newSelectedSlice = if (selectedSlice == i) -1 else i
                            break
                        }
                        startAngle += sweepAngle
                    }

                    onSliceSelected(newSelectedSlice)
                }
            }
        }
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = min(size.width, size.height) / 2 * 0.9f
        var startAngle = -90f

        data.forEachIndexed { index, (_, _) ->
            if (index != selectedSlice) {
                val sweepAngle = animatedValues[index].value * 360f
                val sectorRadius = maxRadius * sliceScales[index].value

                drawArc(
                    color = colors[index].copy(alpha = 0.8f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - sectorRadius, center.y - sectorRadius),
                    size = Size(sectorRadius * 2, sectorRadius * 2),
                    style = Fill
                )

                startAngle += sweepAngle
            } else {
                startAngle += animatedValues[index].value * 360f
            }
        }

        if (selectedSlice >= 0 && selectedSlice < data.size) {
            var selectedStartAngle = -90f
            for (i in 0 until selectedSlice) {
                selectedStartAngle += animatedValues[i].value * 360f
            }

            val sweepAngle = animatedValues[selectedSlice].value * 360f
            val sectorRadius = maxRadius * sliceScales[selectedSlice].value

            drawArc(
                color = colors[selectedSlice],
                startAngle = selectedStartAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - sectorRadius, center.y - sectorRadius),
                size = Size(sectorRadius * 2, sectorRadius * 2),
                style = Fill
            )

            drawArc(
                color = Color.White,
                startAngle = selectedStartAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - sectorRadius, center.y - sectorRadius),
                size = Size(sectorRadius * 2, sectorRadius * 2),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures {
            onClick()
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun EmptyCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            content()
        }
    }
}