package org.example.appbbmges.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appbbmges.composeapp.generated.resources.Mascotas_3
import appbbmges.composeapp.generated.resources.Res
import appbbmges.composeapp.generated.resources.logoSystem
import kotlinx.coroutines.delay
import org.example.appbbmges.data.Repository
import org.example.appbbmges.navigation.Screen
import org.example.appbbmges.navigation.SimpleNavController
import org.jetbrains.compose.resources.painterResource

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Error(val message: String) : LoginState()
    object Success : LoginState()
}

data class ValidationResult(
    val isValid: Boolean,
    val usernameError: String? = null,
    val passwordError: String? = null
)

class LoginValidator {
    companion object {
        fun validateCredentials(username: String, password: String): ValidationResult {
            val usernameError = when {
                username.isEmpty() -> "El usuario es obligatorio"
                username.length !in 4..16 -> "El usuario debe tener entre 4 y 16 caracteres"
                !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "El usuario solo puede contener letras, números y guiones bajos"
                else -> null
            }

            val passwordError = when {
                password.isEmpty() -> "La contraseña es obligatoria"
                password.length !in 8..50 -> "La contraseña debe tener entre 8 y 50 caracteres"
                !password.any { it.isUpperCase() } -> "La contraseña debe contener al menos una mayúscula"
                !password.any { it.isLowerCase() } -> "La contraseña debe contener al menos una minúscula"
                !password.any { it.isDigit() } -> "La contraseña debe contener al menos un número"
                else -> null
            }

            return ValidationResult(
                isValid = usernameError == null && passwordError == null,
                usernameError = usernameError,
                passwordError = passwordError
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: SimpleNavController,
    userRepository: Repository,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginState by remember { mutableStateOf<LoginState>(LoginState.Idle) }
    var validationResult by remember { mutableStateOf(ValidationResult(true)) }

    suspend fun performLogin() {
        loginState = LoginState.Loading

        delay(1000)

        try {
            val user = userRepository.getUserByUsername(username)

            if (user != null && user.password == password) {
                loginState = LoginState.Success
                navController.navigateTo(Screen.Dashboard(username))
            } else {
                loginState = LoginState.Error("Usuario o contraseña incorrectos")
            }
        } catch (e: Exception) {
            loginState = LoginState.Error("Error de conexión. Intenta de nuevo.")
        }
    }

    fun validateAndLogin() {
        val validation = LoginValidator.validateCredentials(username, password)
        validationResult = validation

        if (validation.isValid) {

        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.75f)
                    .widthIn(max = 1000.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {

                    LeftPanel()

                    RightPanel(
                        username = username,
                        password = password,
                        passwordVisible = passwordVisible,
                        loginState = loginState,
                        validationResult = validationResult,
                        onUsernameChange = {
                            username = it
                            if (validationResult.usernameError != null) {
                                validationResult = validationResult.copy(usernameError = null)
                            }
                        },
                        onPasswordChange = {
                            password = it
                            if (validationResult.passwordError != null) {
                                validationResult = validationResult.copy(passwordError = null)
                            }
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onLoginClick = {
                            validateAndLogin()
                            if (validationResult.isValid) {

                                loginState = LoginState.Loading
                            }
                        },
                        onForgotPasswordClick = { /* Implementar */ },
                        focusManager = focusManager
                    )
                }
            }
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Loading) {
            performLogin()
        }
    }
}

@Composable
private fun RowScope.LeftPanel() {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(
                color = Color(0xFFff8abe),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Image(
                painter = painterResource(Res.drawable.logoSystem),
                contentDescription = "Logo de la aplicación",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(horizontal = 24.dp)
                    .semantics {
                        contentDescription = "Logo de la aplicación"
                    },
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(Res.drawable.Mascotas_3),
                contentDescription = "Niños jugando",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .semantics {
                        contentDescription = "Ilustración de niños jugando"
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun RowScope.RightPanel(
    username: String,
    password: String,
    passwordVisible: Boolean,
    loginState: LoginState,
    validationResult: ValidationResult,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo de usuario
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .semantics {
                        contentDescription = "Campo de nombre de usuario"
                    },
                label = { Text("Nombre de usuario") },
                placeholder = { Text("Ingresa tu usuario") },
                singleLine = true,
                enabled = loginState !is LoginState.Loading,
                isError = validationResult.usernameError != null,
                supportingText = {
                    validationResult.usernameError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Icono de usuario"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFbf0cb9),
                    focusedLabelColor = Color(0xFFbf0cb9)
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .semantics {
                        contentDescription = "Campo de contraseña"
                    },
                label = { Text("Contraseña") },
                placeholder = { Text("Ingresa tu contraseña") },
                singleLine = true,
                enabled = loginState !is LoginState.Loading,
                isError = validationResult.passwordError != null,
                supportingText = {
                    validationResult.passwordError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Icono de contraseña"
                    )
                },
                trailingIcon = {
                    val icon = if (passwordVisible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff

                    IconButton(
                        onClick = onPasswordVisibilityToggle,
                        enabled = loginState !is LoginState.Loading
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (passwordVisible)
                                "Ocultar contraseña"
                            else
                                "Mostrar contraseña"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLoginClick()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFbf0cb9),
                    focusedLabelColor = Color(0xFFbf0cb9)
                )
            )

            if (loginState is LoginState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = loginState.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text(
                text = "¿Olvidaste tu contraseña?",
                color = Color(0xFFbf0cb9), // Rosa pastel
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 24.dp)
                    .clickable(enabled = loginState !is LoginState.Loading) {
                        onForgotPasswordClick()
                    }
                    .semantics {
                        contentDescription = "Enlace para recuperar contraseña"
                    }
            )

            Button(
                onClick = onLoginClick,
                enabled = loginState !is LoginState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFff8abe), // Rosa pastel
                    disabledContainerColor = Color(0xFFff8abe).copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (loginState is LoginState.Loading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Iniciando sesión...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Text(
                    text = "  o  ",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Necesitas ayuda? Contacta al administrador",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}