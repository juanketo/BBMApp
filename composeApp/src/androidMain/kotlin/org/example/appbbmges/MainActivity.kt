package org.example.appbbmges

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.appbbmges.data.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val databaseDriverFactory = DatabaseDriverFactory(this)

        setContent {
            App(databaseDriverFactory)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {

}