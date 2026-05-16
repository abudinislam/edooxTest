package abubakir.edooxtest.ui.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import abubakir.edooxtest.data.model.MatchPair
import abubakir.edooxtest.data.model.Question
import abubakir.edooxtest.data.model.QuestionResult
import abubakir.edooxtest.data.model.UserAnswer
import abubakir.edooxtest.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TestViewModel : ViewModel() {

    sealed class UiState {
        data class ShowQuestion(
            val question: Question,
            val index: Int,
            val total: Int,
            val canProceed: Boolean,
            val currentAnswer: UserAnswer? = null
        ) : UiState()
        object Evaluating : UiState()
        data class Done(val results: List<QuestionResult>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState = _uiState.asStateFlow()

    private val questions = mutableListOf<Question>()
    private val answers = mutableMapOf<Int, UserAnswer>()
    private var currentIndex = 0

    private val repository = QuizRepository()

    fun init(questionList: List<Question>) {
        if (questions.isNotEmpty()) return // already initialized (e.g. after rotation)
        questions.addAll(questionList)
        answers.clear()
        currentIndex = 0
        emitCurrentQuestion()
    }

    fun setAnswer(answer: UserAnswer) {
        answers[currentIndex] = answer
        emitCurrentQuestion(canProceed = true)
    }

    fun clearAnswer() {
        answers.remove(currentIndex)
        emitCurrentQuestion(canProceed = false)
    }

    fun next() {
        if (currentIndex < questions.size - 1) {
            currentIndex++
            emitCurrentQuestion(canProceed = answers.containsKey(currentIndex))
        }
    }

    fun finish() {
        viewModelScope.launch {
            _uiState.value = UiState.Evaluating
            val results = questions.mapIndexed { i, question -> buildResult(question, answers[i]) }
            _uiState.value = UiState.Done(results)
        }
    }

    private fun emitCurrentQuestion(canProceed: Boolean = answers.containsKey(currentIndex)) {
        if (questions.isEmpty()) return
        _uiState.value = UiState.ShowQuestion(
            question = questions[currentIndex],
            index = currentIndex,
            total = questions.size,
            canProceed = canProceed,
            currentAnswer = answers[currentIndex]
        )
    }

    private suspend fun buildResult(question: Question, answer: UserAnswer?): QuestionResult {
        return when (question) {
            is Question.MultipleChoice -> {
                val mc = answer as? UserAnswer.MultipleChoice
                QuestionResult(
                    questionText = question.text,
                    questionType = "multiple_choice",
                    userAnswerText = mc?.selected ?: "(нет ответа)",
                    correctAnswerText = question.correctAnswer,
                    isCorrect = mc?.selected == question.correctAnswer,
                    feedback = question.explanation
                )
            }
            is Question.OpenEnded -> {
                val userText = (answer as? UserAnswer.OpenEnded)?.text ?: ""
                if (userText.isBlank()) {
                    QuestionResult(
                        questionText = question.text,
                        questionType = "open_ended",
                        userAnswerText = "(нет ответа)",
                        correctAnswerText = question.sampleAnswer,
                        isCorrect = false,
                        feedback = "Ответ не был дан."
                    )
                } else {
                    repository.evaluateOpenAnswer(question.text, question.sampleAnswer, userText)
                        .fold(
                            onSuccess = { eval ->
                                QuestionResult(
                                    questionText = question.text,
                                    questionType = "open_ended",
                                    userAnswerText = userText,
                                    correctAnswerText = question.sampleAnswer,
                                    isCorrect = eval.is_correct,
                                    feedback = eval.feedback
                                )
                            },
                            onFailure = { e ->
                                QuestionResult(
                                    questionText = question.text,
                                    questionType = "open_ended",
                                    userAnswerText = userText,
                                    correctAnswerText = question.sampleAnswer,
                                    isCorrect = false,
                                    feedback = "Ошибка проверки: ${e.message}"
                                )
                            }
                        )
                }
            }
            is Question.Matching -> {
                val ma = answer as? UserAnswer.Matching
                val correctMap = question.pairs.associate { it.left to it.right }
                val userMap = ma?.answers ?: emptyMap()
                QuestionResult(
                    questionText = question.text,
                    questionType = "matching",
                    userAnswerText = formatMatchingAnswer(userMap, question.pairs),
                    correctAnswerText = formatMatchingAnswer(correctMap, question.pairs),
                    isCorrect = correctMap == userMap,
                    feedback = question.explanation
                )
            }
        }
    }

    private fun formatMatchingAnswer(map: Map<String, String>, pairs: List<MatchPair>): String =
        pairs.joinToString("\n") { "${it.left}  →  ${map[it.left] ?: "?"}" }
}
