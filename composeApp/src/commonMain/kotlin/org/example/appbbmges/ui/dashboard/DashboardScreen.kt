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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import org.example.appbbmges.data.Repository
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: SimpleNavController, repository: Repository) {
    // Obtener los datos del Repository
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
                BirthdayContent()
            }
        }
    }
}

@Composable
fun BirthdayContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cumpleaños",
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
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFC1CC),
                            Color(0xFFFFB6C1),
                            Color(0xFFFFA0B4)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            DecorativeFlowers()

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Feliz Cumpleaños",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE91E63),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        shadow = Shadow(
                            color = Color(0x40000000),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color(0xFFE91E63), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Face,
                        contentDescription = "Bebé",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFE91E63)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ana Luisa Galvan Ruiz",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E2E2E),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "25 de Agosto de 2008",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A4A4A),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DecorativeFlowers() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val flowerSize = 20.dp.toPx()
        val petalColor = Color(0xFFFFFFFF).copy(alpha = 0.6f)
        val centerColor = Color(0xFFE91E63).copy(alpha = 0.8f)

        drawFlower(
            center = Offset(30f, 30f),
            petalSize = flowerSize * 0.6f,
            petalColor = petalColor,
            centerColor = centerColor
        )

        drawFlower(
            center = Offset(size.width - 30f, 40f),
            petalSize = flowerSize * 0.8f,
            petalColor = petalColor,
            centerColor = centerColor
        )

        drawFlower(
            center = Offset(40f, size.height - 40f),
            petalSize = flowerSize * 0.7f,
            petalColor = petalColor,
            centerColor = centerColor
        )

        drawFlower(
            center = Offset(size.width - 40f, size.height - 30f),
            petalSize = flowerSize * 0.5f,
            petalColor = petalColor,
            centerColor = centerColor
        )

        drawFlower(
            center = Offset(size.width * 0.8f, size.height * 0.3f),
            petalSize = flowerSize * 0.4f,
            petalColor = petalColor,
            centerColor = centerColor
        )

        drawFlower(
            center = Offset(size.width * 0.2f, size.height * 0.7f),
            petalSize = flowerSize * 0.4f,
            petalColor = petalColor,
            centerColor = centerColor
        )
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlower(
    center: Offset,
    petalSize: Float,
    petalColor: Color,
    centerColor: Color
) {
    for (i in 0..4) {
        val angle = (i * 72f) * (PI / 180f).toFloat()
        val petalCenter = Offset(
            center.x + cos(angle) * petalSize * 0.6f,
            center.y + sin(angle) * petalSize * 0.6f
        )

        drawCircle(
            color = petalColor,
            radius = petalSize * 0.4f,
            center = petalCenter
        )
    }

    drawCircle(
        color = centerColor,
        radius = petalSize * 0.3f,
        center = center
    )
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

fun Color.lighten(fraction: Float): Color {
    val a = alpha
    val r = red + (1 - red) * fraction
    val g = green + (1 - green) * fraction
    val b = blue + (1 - blue) * fraction
    return Color(red = r, green = g, blue = b, alpha = a)
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