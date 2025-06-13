package org.example.appbbmges.ui.gastosnomina

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import org.example.appbbmges.navigation.SimpleNavController

@Composable
fun GastosNominaScreen(navController: SimpleNavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "gastos nominaScreen", fontSize = 24.sp)
        Button(onClick = { }) {
            Text("Ir a Mensajes")
        }

    }
}