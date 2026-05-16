package abubakir.edooxtest.data.model

sealed class Question {
    abstract val id: String
    abstract val text: String
    abstract val explanation: String

    data class MultipleChoice(
        override val id: String,
        override val text: String,
        val options: List<String>,
        val correctAnswer: String,
        override val explanation: String
    ) : Question()

    data class OpenEnded(
        override val id: String,
        override val text: String,
        val sampleAnswer: String,
        override val explanation: String
    ) : Question()

    data class Matching(
        override val id: String,
        override val text: String,
        val pairs: List<MatchPair>,
        override val explanation: String
    ) : Question()
}

data class MatchPair(val left: String, val right: String)
