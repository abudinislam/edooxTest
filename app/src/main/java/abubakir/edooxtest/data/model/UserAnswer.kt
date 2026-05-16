package abubakir.edooxtest.data.model

sealed class UserAnswer {
    data class MultipleChoice(val selected: String) : UserAnswer()
    data class OpenEnded(val text: String) : UserAnswer()
    data class Matching(val answers: Map<String, String>) : UserAnswer()
}
