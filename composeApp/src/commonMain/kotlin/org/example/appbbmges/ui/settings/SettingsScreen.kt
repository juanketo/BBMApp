package org.example.appbbmges.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.appbbmges.navigation.SimpleNavController
import org.example.appbbmges.data.Repository
import org.example.appbbmges.ui.settings.registationex.AddNewDisciplinaScreen
import org.example.appbbmges.ui.settings.registationex.AddNewPromoScreen
import org.example.appbbmges.ui.settings.registationex.AddNivelScreen
import org.example.appbbmges.ui.settings.registationex.AddProductoScreen
import org.example.appbbmges.ui.settings.registationex.AddSucursalScreen



@Composable
fun SettingsScreen(navController: SimpleNavController, repository: Repository) {

    var selectedFormType by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        if (selectedFormType != null) {
            when (selectedFormType) {
                "Sucursal" -> {
                    AddSucursalScreen(
                    onDismiss = { selectedFormType = null },
                    repository = repository
                    )
                }
                "Nivel" -> {
                    AddNivelScreen(
                    onDismiss = { selectedFormType = null },
                    repository = repository
                    )
                }
                "Producto" -> {
                    AddProductoScreen(
                    onDismiss = { selectedFormType = null },
                    repository = repository
                    )
                }
                "Disciplina" -> {
                    AddNewDisciplinaScreen(
                    onDismiss = { selectedFormType = null },
                    repository = repository
                    )
                }
                "Promocion" -> {
                    AddNewPromoScreen(
                    onDismiss = { selectedFormType = null },
                    repository = repository
                    )
                }

            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F8F8))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = "Configuración",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Fila 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConfigurationCard(
                                icon = Icons.Outlined.Business,
                                title = "Agregar Nueva Sucursal",
                                backgroundColor = Color(0xFFFFCDD2), // Rosa claro
                                iconColor = Color(0xFFD32F2F)
                            ) {
                                selectedFormType = "Sucursal"
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ConfigurationCard(
                                icon = Icons.Outlined.School,
                                title = "Agregar Nuevo Nivel",
                                backgroundColor = Color(0xFFFFE0B2),
                                iconColor = Color(0xFFFF9800)
                            ) {
                                selectedFormType = "Nivel"
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConfigurationCard(
                                icon = Icons.Outlined.Inventory,
                                title = "Agregar Nuevo Producto",
                                backgroundColor = Color(0xFFC8E6C9),
                                iconColor = Color(0xFF4CAF50)
                            ) {
                                selectedFormType = "Producto"
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ConfigurationCard(
                                icon = Icons.Outlined.SportsGymnastics,
                                title = "Agregar Nueva Disciplina",
                                backgroundColor = Color(0xFFDCEDC8),
                                iconColor = Color(0xFF8BC34A)
                            ) {
                                selectedFormType = "Disciplina"
                            }
                        }
                    }

                    // Fila 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConfigurationCard(
                                icon = Icons.Outlined.LocalOffer,
                                title = "Agregar Nueva Promoción",
                                backgroundColor = Color(0xFFBBDEFB), // Azul claro
                                iconColor = Color(0xFF2196F3)
                            ) {
                                selectedFormType = "Promocion"
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            Spacer(modifier = Modifier.height(140.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Configuraciones Generales",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        GeneralSettingItem(
                            icon = Icons.Outlined.Backup,
                            title = "Respaldo de Datos",
                            subtitle = "Exportar información del sistema"
                        ) {
                            selectedFormType = "Respaldo"
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = Color(0xFFF0F0F0)
                        )

                        GeneralSettingItem(
                            icon = Icons.Outlined.Settings,
                            title = "Configuración Avanzada",
                            subtitle = "Ajustes del sistema y preferencias"
                        ) {
                            selectedFormType = "ConfigAvanzada"
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigurationCard(
    icon: ImageVector,
    title: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.9f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFF3667EA).copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF3667EA),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Ir a $title",
                tint = Color(0xFF999999),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}