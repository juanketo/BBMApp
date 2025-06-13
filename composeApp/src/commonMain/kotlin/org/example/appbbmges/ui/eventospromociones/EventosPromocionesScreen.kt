package org.example.appbbmges.ui.eventospromociones.EventosPromocionesScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.navigation.SimpleNavController
import org.jetbrains.compose.resources.DrawableResource

// Data class para representar un evento/promoción
data class EventoPromocion(
    val id: Int,
    val titulo: String,
    val año: Int,
    val fechaInicio: String,
    val fechaCierre: String,
    val ubicacion: String,
    val costo: String?,
    val imagenRes: DrawableResource,
    val esGratis: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosPromocionesScreen(navController: SimpleNavController) {
    // Lista de ejemplo de eventos/promociones
    val eventosPromociones = remember {
        listOf(
            EventoPromocion(
                id = 1,
                titulo = "EXÁMENES ANUALES",
                año = 2025,
                fechaInicio = "30 Abril 18:00",
                fechaCierre = "19:00",
                ubicacion = "Baby Ballet Calz. del Hueso",
                costo = null,
                imagenRes = Res.drawable.logoSystem,
                esGratis = true
            ),
            EventoPromocion(
                id = 2,
                titulo = "CLASE DE BALLET CLÁSICO",
                año = 2025,
                fechaInicio = "15 Mayo 16:00",
                fechaCierre = "18:00",
                ubicacion = "Studio Principal",
                costo = "$350",
                imagenRes = Res.drawable.logoSystem,
                esGratis = false
            ),
            EventoPromocion(
                id = 3,
                titulo = "FESTIVAL DE DANZA",
                año = 2025,
                fechaInicio = "20 Junio 19:00",
                fechaCierre = "22:00",
                ubicacion = "Teatro Municipal",
                costo = "$500",
                imagenRes = Res.drawable.logoSystem,
                esGratis = false
            ),
            EventoPromocion(
                id = 4,
                titulo = "EXÁMENES ANUALES",
                año = 2025,
                fechaInicio = "30 Abril 18:00",
                fechaCierre = "19:00",
                ubicacion = "Baby Ballet Calz. del Hueso",
                costo = null,
                imagenRes = Res.drawable.logoSystem,
                esGratis = true
            ),
            EventoPromocion(
                id = 5,
                titulo = "CLASE DE BALLET CLÁSICO",
                año = 2025,
                fechaInicio = "15 Mayo 16:00",
                fechaCierre = "18:00",
                ubicacion = "Studio Principal",
                costo = "$350",
                imagenRes = Res.drawable.logoSystem,
                esGratis = false
            ),
            EventoPromocion(
                id = 6,
                titulo = "FESTIVAL DE DANZA",
                año = 2025,
                fechaInicio = "20 Junio 19:00",
                fechaCierre = "22:00",
                ubicacion = "Teatro Municipal",
                costo = "$500",
                imagenRes = Res.drawable.logoSystem,
                esGratis = false
            ),
            EventoPromocion(
                id = 7,
                titulo = "EXÁMENES ANUALES",
                año = 2025,
                fechaInicio = "30 Abril 18:00",
                fechaCierre = "19:00",
                ubicacion = "Baby Ballet Calz. del Hueso",
                costo = null,
                imagenRes = Res.drawable.logoSystem,
                esGratis = true
            ),
            EventoPromocion(
                id = 8,
                titulo = "CLASE DE BALLET CLÁSICO",
                año = 2025,
                fechaInicio = "15 Mayo 16:00",
                fechaCierre = "18:00",
                ubicacion = "Studio Principal",
                costo = "$350",
                imagenRes = Res.drawable.logoSystem,
                esGratis = false
            ),
            EventoPromocion(
                id = 9,
                titulo = "FESTIVAL DE DANZA",
                año = 2025,
                fechaInicio = "20 Junio 19:00",
                fechaCierre = "22:00",
                ubicacion = "Teatro Municipal",
                costo = "$500",
                imagenRes = Res.drawable.logoSystem,
                esGratis = false
            ),
            EventoPromocion(
                id = 10,
                titulo = "EXÁMENES ANUALES",
                año = 2025,
                fechaInicio = "30 Abril 18:00",
                fechaCierre = "19:00",
                ubicacion = "Baby Ballet Calz. del Hueso",
                costo = null,
                imagenRes = Res.drawable.logoSystem,
                esGratis = true
            ),
            EventoPromocion(
                id = 11,
                titulo = "CLASE DE BALLET CLÁSICO",
                año = 2025,
                fechaInicio = "15 Mayo 16:00",
                fechaCierre = "18:00",
                ubicacion = "Studio Principal",
                costo = "$350",
                imagenRes = Res.drawable.logoSystem,
                esGratis = false
            ),
            EventoPromocion(
                id = 12,
                titulo = "FESTIVAL DE DANZA",
                año = 2025,
                fechaInicio = "20 Junio 19:00",
                fechaCierre = "22:00",
                ubicacion = "Teatro Municipal",
                costo = "$500",
                imagenRes = Res.drawable.logoSystem,
                esGratis = false
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Eventos y Promociones",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(eventosPromociones) { evento ->
                EventoPromocionCard(
                    evento = evento,
                    onClick = {
                        // Aquí puedes manejar el click de la card
                        // Por ejemplo, navegar a los detalles del evento
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoPromocionCard(
    evento: EventoPromocion,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen de fondo
            Image(
                painter = painterResource(evento.imagenRes),
                contentDescription = evento.titulo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            // Overlay gradiente sobre la imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        ),
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
            )

            // Badge de costo o "GRATIS"
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                if (evento.esGratis) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "GRATIS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    evento.costo?.let { costo ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = costo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Contenido de texto en la parte inferior
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
                    .padding(12.dp)
            ) {
                // Título con año
                Text(
                    text = "${evento.titulo} ${evento.año}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Fechas
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${evento.fechaInicio} - ${evento.fechaCierre}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Ubicación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = evento.ubicacion,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}