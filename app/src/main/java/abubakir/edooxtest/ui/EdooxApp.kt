package abubakir.edooxtest.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import abubakir.edooxtest.ui.result.ResultScreen
import abubakir.edooxtest.ui.subject.SubjectScreen
import abubakir.edooxtest.ui.test.TestScreen

@Composable
fun EdooxApp(appState: AppStateViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "subject") {

        composable("subject") {
            SubjectScreen(
                autoRetrySubject = appState.autoRetrySubject,
                onAutoRetryConsumed = { appState.autoRetrySubject = null },
                onNavigateToTest = { questions, subject ->
                    appState.pendingQuestions = questions
                    appState.currentSubject = subject
                    navController.navigate("test")
                }
            )
        }

        composable("test") {
            TestScreen(
                questions = appState.pendingQuestions,
                onFinished = { results ->
                    appState.pendingResults = results
                    navController.navigate("result") {
                        popUpTo("test") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("result") {
            ResultScreen(
                results = appState.pendingResults,
                subject = appState.currentSubject,
                onRetry = {
                    appState.autoRetrySubject = appState.currentSubject
                    navController.navigate("subject") {
                        popUpTo("result") { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate("subject") {
                        popUpTo("result") { inclusive = true }
                    }
                }
            )
        }
    }
}
