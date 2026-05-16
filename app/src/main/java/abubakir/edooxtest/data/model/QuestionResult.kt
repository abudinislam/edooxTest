package abubakir.edooxtest.data.model

import com.google.gson.annotations.SerializedName

data class QuestionResult(
    @SerializedName("questionText") val questionText: String,
    @SerializedName("questionType") val questionType: String,
    @SerializedName("userAnswerText") val userAnswerText: String,
    @SerializedName("correctAnswerText") val correctAnswerText: String,
    @SerializedName("isCorrect") val isCorrect: Boolean,
    @SerializedName("feedback") val feedback: String
)
