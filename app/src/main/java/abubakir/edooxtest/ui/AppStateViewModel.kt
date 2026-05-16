package abubakir.edooxtest.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import abubakir.edooxtest.data.model.Question
import abubakir.edooxtest.data.model.QuestionResult

class AppStateViewModel : ViewModel() {
    var pendingQuestions: List<Question> by mutableStateOf(emptyList())
    var currentSubject: String by mutableStateOf("")
    var pendingResults: List<QuestionResult> by mutableStateOf(emptyList())
    var autoRetrySubject: String? by mutableStateOf(null)
}
