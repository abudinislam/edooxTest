package abubakir.edooxtest.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import abubakir.edooxtest.ui.theme.EdooxTheme

class MainActivity : ComponentActivity() {
    private val appState: AppStateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EdooxTheme {
                EdooxApp(appState)
            }
        }
    }
}
