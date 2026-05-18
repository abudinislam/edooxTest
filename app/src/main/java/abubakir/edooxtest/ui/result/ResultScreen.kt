package abubakir.edooxtest.ui.result

import abubakir.edooxtest.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import abubakir.edooxtest.ui.theme.EdooxTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import abubakir.edooxtest.data.model.QuestionResult
import abubakir.edooxtest.ui.theme.Blue700
import abubakir.edooxtest.ui.theme.Green800
import abubakir.edooxtest.ui.theme.Ink
import abubakir.edooxtest.ui.theme.MutedInk
import abubakir.edooxtest.ui.theme.Red800

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    results: List<QuestionResult>,
    subject: String,
    viewModel: ResultViewModel = viewModel(),
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(results) { viewModel.init(results) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.result_title))
                        Text(
                            text = subject,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) { Text(stringResource(R.string.retry)) }
                    Button(
                        onClick = onHome,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) { Text(stringResource(R.string.go_home)) }
                }
            }
        }
        ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                ScoreCard(score = viewModel.score, total = viewModel.total)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryPill(stringResource(R.string.topic), subject, Modifier.weight(1f))
                    SummaryPill(stringResource(R.string.analysis_short), stringResource(R.string.analysis_short_value), Modifier.weight(1f))
                }
            }

            item {
                Text(
                    text = stringResource(R.string.analysis_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            itemsIndexed(state.results) { index, result ->
                ResultItem(
                    result = result,
                    index = index,
                    expanded = index in state.expandedIndices,
                    onClick = { viewModel.toggleExpanded(index) }
                )
            }
        }
    }
}

@Composable
private fun ScoreCard(score: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF173ECA), Color(0xFF2D78FF), Color(0xFF6EC7FF))
                    )
                )
                .padding(vertical = 28.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.score_ai_badge), color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
            Text(
                "$score из $total",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(stringResource(R.string.correct_answers_label), color = Color.White.copy(alpha = 0.84f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Text(
                text = when {
                    score == total -> stringResource(R.string.score_all_correct)
                    score >= total * 0.8 -> stringResource(R.string.score_strong)
                    score >= total * 0.6 -> stringResource(R.string.score_good_base)
                    else -> stringResource(R.string.score_growth)
                },
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryPill(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MutedInk)
            Text(value, style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun ResultItem(result: QuestionResult, index: Int, expanded: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = if (result.isCorrect) Green800.copy(alpha = 0.15f) else Red800.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (result.isCorrect) "✓" else "✗",
                            color = if (result.isCorrect) Green800 else Red800,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.question_index, index + 1, result.questionText),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (expanded) R.string.collapse_up else R.string.collapse_down), color = MutedInk, fontSize = 11.sp)
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                    DetailRow(stringResource(R.string.your_answer), result.userAnswerText)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(stringResource(R.string.correct_answer), result.correctAnswerText, Green800)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(stringResource(R.string.explanation_label), result.feedback)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
    Text(value, style = MaterialTheme.typography.bodySmall, color = valueColor, modifier = Modifier.padding(top = 2.dp))
}

private val previewResults = listOf(
    QuestionResult(
        questionText = "Чему равна производная функции f(x) = x²?",
        questionType = "multiple_choice",
        userAnswerText = "2x",
        correctAnswerText = "2x",
        isCorrect = true,
        feedback = "Производная степенной функции по правилу (xⁿ)' = n·xⁿ⁻¹."
    ),
    QuestionResult(
        questionText = "Объясните теорему Пифагора своими словами.",
        questionType = "open_ended",
        userAnswerText = "a плюс b равно c",
        correctAnswerText = "В прямоугольном треугольнике квадрат гипотенузы равен сумме квадратов катетов.",
        isCorrect = false,
        feedback = "Ответ неполный — необходимо указать квадраты и упомянуть прямой угол."
    ),
    QuestionResult(
        questionText = "Сопоставьте понятия с определениями.",
        questionType = "matching",
        userAnswerText = "Интеграл → Обратная производная\nПредел → Значение при x → a",
        correctAnswerText = "Интеграл → Обратная производная\nПредел → Значение при x → a",
        isCorrect = true,
        feedback = "Все пары сопоставлены верно."
    )
)

@Preview(showBackground = true, name = "Result Screen")
@Composable
private fun ResultScreenPreview() {
    val vm = remember { ResultViewModel().also { it.init(previewResults) } }
    EdooxTheme {
        ResultScreen(
            results = previewResults,
            subject = "Математика",
            viewModel = vm,
            onRetry = {},
            onHome = {}
        )
    }
}
