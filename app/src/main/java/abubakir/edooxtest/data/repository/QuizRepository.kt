package abubakir.edooxtest.data.repository

import abubakir.edooxtest.BuildConfig
import abubakir.edooxtest.data.model.MatchPair
import abubakir.edooxtest.data.model.Question
import abubakir.edooxtest.data.remote.EvaluationResponse
import abubakir.edooxtest.data.remote.GenerationConfig
import abubakir.edooxtest.data.remote.GeminiApi
import abubakir.edooxtest.data.remote.GeminiContent
import abubakir.edooxtest.data.remote.GeminiPart
import abubakir.edooxtest.data.remote.GeminiRequest
import abubakir.edooxtest.data.remote.QuestionRaw
import abubakir.edooxtest.data.remote.QuestionsResponse
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class QuizRepository {

    private val gson = Gson()

    private val api: GeminiApi = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeminiApi::class.java)

    suspend fun generateQuestions(subject: String): Result<List<Question>> {
        return try {
            ensureApiKeyConfigured()
            val prompt = buildGenerationPrompt(subject)
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))),
                generationConfig = GenerationConfig()
            )
            val response = api.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("Пустой ответ от Gemini"))

            val cleanText = text.trimMarkdownFences()
            val questionsResponse = gson.fromJson(cleanText, QuestionsResponse::class.java)
            validateGeneratedQuestions(questionsResponse.questions)
            val questions = questionsResponse.questions.map { it.toQuestion() }
            Result.success(questions)
        } catch (e: HttpException) {
            Result.failure(Exception(httpErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun evaluateOpenAnswer(
        questionText: String,
        sampleAnswer: String,
        userAnswer: String
    ): Result<EvaluationResponse> {
        return try {
            ensureApiKeyConfigured()
            val prompt = buildEvaluationPrompt(questionText, sampleAnswer, userAnswer)
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))),
                generationConfig = GenerationConfig()
            )
            val response = api.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("Пустой ответ"))
            val cleanText = text.trimMarkdownFences()
            val eval = gson.fromJson(cleanText, EvaluationResponse::class.java)
            Result.success(eval)
        } catch (e: HttpException) {
            Result.failure(Exception(httpErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ensureApiKeyConfigured() {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            throw IllegalStateException("GEMINI_API_KEY не задан в local.properties")
        }
    }

    private fun httpErrorMessage(error: HttpException): String {
        val code = error.code()
        val apiMessage = error.response()
            ?.errorBody()
            ?.string()
            ?.let(::extractApiMessage)

        if (code == 429 && apiMessage?.contains("limit: 0", ignoreCase = true) == true) {
            return "Gemini API отклоняет запрос: у текущего проекта нулевая квота для этой модели. Проверьте billing/план в Google AI Studio или используйте другой ключ."
        }

        return when (code) {
            400 -> apiMessage ?: "Некорректный запрос к Gemini API. Проверьте ключ и формат запроса."
            403 -> apiMessage ?: "Доступ к Gemini API запрещён. Проверьте ограничения и права API-ключа."
            429 -> apiMessage ?: "Превышен лимит запросов Gemini. Подождите и попробуйте снова."
            500, 503 -> "Сервер Gemini недоступен. Попробуйте позже."
            else -> apiMessage ?: "Ошибка сервера: $code"
        }
    }

    private fun String.trimMarkdownFences(): String =
        trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

    private fun extractApiMessage(rawBody: String): String? {
        return runCatching {
            JsonParser.parseString(rawBody)
                .asJsonObject
                .getAsJsonObject("error")
                ?.get("message")
                ?.asString
        }.getOrNull()
    }

    private fun buildGenerationPrompt(subject: String): String = """
Создай ровно 5 экзаменационных заданий по предмету "$subject" для казахстанских школьников (уровень ЕНТ).
Используй только типы multiple_choice, open_ended и matching. В итоговом наборе обязательно должны присутствовать все три типа вопросов, но конкретное распределение между ними выбери самостоятельно. Не копируй буквальную раскладку из тестового задания, если в этом нет необходимости. Все тексты на русском языке.
Верни ТОЛЬКО валидный JSON (без markdown, без пояснений):
{
  "questions": [
    {
      "id": "1",
      "type": "multiple_choice",
      "text": "текст вопроса",
      "options": ["вариант А", "вариант Б", "вариант В", "вариант Г"],
      "correct_answer": "вариант А",
      "explanation": "почему этот ответ верный"
    },
    {
      "id": "2",
      "type": "open_ended",
      "text": "текст вопроса",
      "sample_answer": "развёрнутый эталонный ответ",
      "explanation": "ключевые элементы хорошего ответа"
    },
    {
      "id": "3",
      "type": "matching",
      "text": "Сопоставьте понятия с определениями",
      "pairs": [
        {"left": "понятие 1", "right": "определение 1"},
        {"left": "понятие 2", "right": "определение 2"},
        {"left": "понятие 3", "right": "определение 3"},
        {"left": "понятие 4", "right": "определение 4"}
      ],
      "explanation": "пояснение всех пар"
    }
  ]
}
    """.trimIndent()

    private fun validateGeneratedQuestions(questions: List<QuestionRaw>) {
        require(questions.size == 5) {
            "Gemini вернул ${questions.size} вопросов вместо 5."
        }

        val allowedTypes = setOf("multiple_choice", "open_ended", "matching")
        val actualTypes = questions.map { it.type }.toSet()

        require(actualTypes.all { it in allowedTypes }) {
            "Gemini вернул неподдерживаемый тип вопроса."
        }

        require(allowedTypes.all { it in actualTypes }) {
            "Тест должен содержать все три типа вопросов: multiple_choice, open_ended и matching."
        }
    }

    private fun buildEvaluationPrompt(
        question: String,
        sampleAnswer: String,
        userAnswer: String
    ): String = """
Оцени ответ ученика на экзаменационный вопрос. Будь объективным и конструктивным.
Вопрос: $question
Эталонный ответ: $sampleAnswer
Ответ ученика: $userAnswer
Верни ТОЛЬКО валидный JSON (без markdown):
{"is_correct": true, "feedback": "разбор ответа: что верно, что неверно или чего не хватает"}
    """.trimIndent()
}

private fun QuestionRaw.toQuestion(): Question = when (type) {
    "multiple_choice" -> Question.MultipleChoice(
        id = id,
        text = text,
        options = options ?: emptyList(),
        correctAnswer = correct_answer ?: "",
        explanation = explanation
    )
    "open_ended" -> Question.OpenEnded(
        id = id,
        text = text,
        sampleAnswer = sample_answer ?: "",
        explanation = explanation
    )
    "matching" -> Question.Matching(
        id = id,
        text = text,
        pairs = pairs?.map { MatchPair(it.left, it.right) } ?: emptyList(),
        explanation = explanation
    )
    else -> throw IllegalArgumentException("Неизвестный тип вопроса: $type")
}
