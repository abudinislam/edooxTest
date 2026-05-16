package abubakir.edooxtest.data.remote

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String
)

data class GenerationConfig(
    @SerializedName("response_mime_type") val responseMimeType: String = "application/json",
    val temperature: Float = 0.7f
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)
data class QuestionsResponse(
    val questions: List<QuestionRaw>
)

data class QuestionRaw(
    val id: String,
    val type: String,
    val text: String,
    val options: List<String>?,
    val correct_answer: String?,
    val sample_answer: String?,
    val pairs: List<MatchPairRaw>?,
    val explanation: String
)

data class MatchPairRaw(
    val left: String,
    val right: String
)

data class EvaluationResponse(
    val is_correct: Boolean,
    val feedback: String
)
