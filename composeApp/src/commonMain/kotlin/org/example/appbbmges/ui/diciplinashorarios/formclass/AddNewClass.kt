package org.example.appbbmges.ui.diciplinashorarios.formclass

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.appbbmges.data.Repository // Import Repository

@Composable
fun AddNewClass(onDismiss: () -> Unit,
                repository: Repository, // Added repository
                modifier: Modifier = Modifier) { // Added modifier
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Cyan.copy(alpha = 0.8f)), // Using a different background to distinguish
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Esta es la vista de clase nueva")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) { // Button to dismiss the view
                Text("Volver al Calendario")
            }
        }
    }
}