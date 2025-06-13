package org.example.appbbmges.ui.reportes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import org.example.appbbmges.navigation.SimpleNavController

@Composable
fun ReportesScreen(navController: SimpleNavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "reportes Screen", fontSize = 24.sp)
        Button(onClick = { }) {
            Text("Ir a Mensajes")
        }

    }
}