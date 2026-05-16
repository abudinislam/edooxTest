package abubakir.edooxtest.ui.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import abubakir.edooxtest.data.model.Question
import abubakir.edooxtest.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectViewModel : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val questions: List<Question>, val subject: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val repository = QuizRepository()
    private var generationJob: Job? = null
    private var isGenerating = false

    fun generateQuestions(subject: String) {
        if (isGenerating || _uiState.value is UiState.Loading) return
        generationJob?.cancel()
        isGenerating = true
        _uiState.value = UiState.Loading
        generationJob = viewModelScope.launch {
            try {
                repository.generateQuestions(subject).fold(
                    onSuccess = { questions ->
                        _uiState.value = UiState.Success(questions, subject)
                    },
                    onFailure = { e ->
                        _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
                    }
                )
            } finally {
                isGenerating = false
            }
        }
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        isGenerating = false
        _uiState.value = UiState.Idle
    }

    fun resetState() {
        isGenerating = false
        _uiState.value = UiState.Idle
    }
}
