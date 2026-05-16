package abubakir.edooxtest.ui.result

import androidx.lifecycle.ViewModel
import abubakir.edooxtest.data.model.QuestionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResultViewModel : ViewModel() {

    data class UiState(
        val results: List<QuestionResult> = emptyList(),
        val expandedIndices: Set<Int> = emptySet()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    val score: Int get() = _uiState.value.results.count { it.isCorrect }
    val total: Int get() = _uiState.value.results.size

    fun init(results: List<QuestionResult>) {
        _uiState.value = UiState(results = results)
    }

    fun toggleExpanded(index: Int) {
        val current = _uiState.value.expandedIndices
        _uiState.value = _uiState.value.copy(
            expandedIndices = if (index in current) current - index else current + index
        )
    }
}
