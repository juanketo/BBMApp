package org.example.appbbmges

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import org.example.appbbmges.data.DatabaseDriverFactory
import org.example.appbbmges.data.Repository
import org.example.appbbmges.navigation.Screen
import org.example.appbbmges.navigation.SimpleNavController
import org.example.appbbmges.ui.login.LoginScreen
import org.example.appbbmges.ui.dashboard.DashboardScreen
import org.example.appbbmges.ui.diciplinashorarios.DisciplinasHorariosScreen
import org.example.appbbmges.ui.eventospromociones.EventosPromocionesScreen.EventosPromocionesScreen
import org.example.appbbmges.ui.franquicias.FranquiciasScreen
import org.example.appbbmges.ui.productos.ProductosScreen
import org.example.appbbmges.ui.settings.SettingsScreen
import org.example.appbbmges.ui.usuarios.UsuariosScreen
import org.jetbrains.compose.resources.painterResource

@Composable
fun App(databaseDriverFactory: DatabaseDriverFactory) {
    val appData = remember { AppData(databaseDriverFactory) }
    val repository = appData.userRepository

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = remember { SimpleNavController(Screen.Login) }
            val currentScreen by navController.currentScreen.collectAsState()

            when (currentScreen) {
                Screen.Login -> LoginScreen(navController = navController, userRepository = repository)
                else -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .fillMaxHeight()
                                .background(Color.White)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(0.dp)
                                )
                        ) {
                            SidebarWithLogo(
                                navController = navController,
                                currentRoute = when (currentScreen) {
                                    is Screen.Dashboard -> "dashboard"
                                    is Screen.Franquicias -> "franquicias"
                                    is Screen.Usuarios -> "usuarios"
                                    is Screen.DisciplinasHorarios -> "disciplinas_horarios"
                                    is Screen.Productos -> "productos"
                                    is Screen.EventosPromociones -> "eventos_promociones"
                                    is Screen.Settings -> "settings"
                                    else -> "dashboard"
                                }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {

                            CustomHeaderRight(
                                navController = navController,
                                userRepository = repository
                            )

                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = Color(0xFFE0E0E0)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF8F8F8))
                            ) {
                                when (currentScreen) {
                                    is Screen.Dashboard -> DashboardScreen(navController = navController, repository = repository)
                                    is Screen.Franquicias -> FranquiciasScreen(navController = navController, repository = repository)
                                    is Screen.Usuarios -> UsuariosScreen(navController = navController, repository = repository)
                                    is Screen.DisciplinasHorarios -> DisciplinasHorariosScreen(navController = navController, repository = repository)
                                    is Screen.Productos -> ProductosScreen(navController = navController)
                                    is Screen.EventosPromociones -> EventosPromocionesScreen(navController = navController)
                                    is Screen.Settings -> SettingsScreen(navController = navController, repository = repository)
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


class AppData(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabaseBaby(databaseDriverFactory.createDriver())
    val userRepository = Repository(database)

    init {
        userRepository.initializeData()
    }
}


fun Repository.clearUserSession() {
    // Aquí puedes implementar la lógica para limpiar la sesión
    // Por ejemplo: eliminar tokens, limpiar cache, etc.
    // clearAuthTokens()
    // clearUserData()
    // resetAppState()
}

@Composable
fun CustomHeaderRight(
    modifier: Modifier = Modifier,
    navController: SimpleNavController? = null,
    userRepository: Repository? = null
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Admin Baby Ballet Morbet®",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF333333)
            )


            Spacer(modifier = Modifier.weight(1f))

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showDropdownMenu = true }
                ) {
                    Text(
                        text = "Juan Abraham",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JA",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false },
                    modifier = Modifier.width(200.dp)
                ) {

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Configuración",
                                    tint = Color(0xFF555555),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Configuración",
                                    color = Color(0xFF555555),
                                    fontSize = 14.sp
                                )
                            }
                        },
                        onClick = {
                            showDropdownMenu = false
                            navController?.navigateTo(Screen.Settings())

                        }
                    )

                    // Divisor
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0)
                    )


                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = "Cerrar Sesión",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Cerrar Sesión",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp
                                )
                            }
                        },
                        onClick = {
                            showDropdownMenu = false

                            userRepository?.clearUserSession()

                            navController?.navigateTo(Screen.Login)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SidebarWithLogo(
    navController: SimpleNavController,
    currentRoute: String
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.logoSystem),
                    contentDescription = "Logo",
                    modifier = Modifier.size(150.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 2.dp)
        ) {
            SidebarMenuItem(
                icon = Icons.Outlined.Dashboard,
                title = "Dashboard",
                isSelected = currentRoute == "dashboard",
                onClick = { navController.navigateTo(Screen.Dashboard()) }
            )

            SidebarMenuItem(
                icon = Icons.Outlined.Business,
                title = "Franquicias",
                isSelected = currentRoute == "franquicias",
                onClick = { navController.navigateTo(Screen.Franquicias()) }
            )

            SidebarMenuItem(
                icon = Icons.Outlined.People,
                title = "Usuarios",
                isSelected = currentRoute == "usuarios",
                onClick = { navController.navigateTo(Screen.Usuarios()) }
            )

            SidebarMenuItem(
                icon = Icons.Outlined.DateRange,
                title = "Disciplinas y Horarios",
                isSelected = currentRoute == "disciplinas_horarios",
                onClick = { navController.navigateTo(Screen.DisciplinasHorarios()) }
            )

            SidebarMenuItem(
                icon = Icons.Outlined.ShoppingBag,
                title = "Productos",
                isSelected = currentRoute == "productos",
                onClick = { navController.navigateTo(Screen.Productos()) }
            )

            SidebarMenuItem(
                icon = Icons.Outlined.Event,
                title = "Eventos y Promociones",
                isSelected = currentRoute == "eventos_promociones",
                onClick = { navController.navigateTo(Screen.EventosPromociones()) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    val backgroundColor = if (isSelected) Color(0xFFF2F2F2) else Color.Transparent
    val textColor = if (isSelected) Color(0xFF3667EA) else Color(0xFF555555)
    val borderColor = if (isSelected) Color(0xFF3667EA) else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(
                width = if (isSelected) 0.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Icon(
                icon,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Título
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Expandir",
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}