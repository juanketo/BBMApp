package org.example.appbbmges.ui.diciplinashorarios.formclass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.appbbmges.data.Repository

@Composable
fun AddNewClassMuestra(onDismiss: () -> Unit,
                       repository: Repository,
                       modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(), // Use the passed modifier
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Esta es la vista de clases muestra")
            // Added a button to dismiss for testing purposes
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text("Volver al Calendario")
            }
        }
    }
}