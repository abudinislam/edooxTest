package abubakir.edooxtest.ui.subject

import abubakir.edooxtest.R
import abubakir.edooxtest.data.model.Question
import abubakir.edooxtest.ui.theme.Blue700
import abubakir.edooxtest.ui.theme.Ink
import abubakir.edooxtest.ui.theme.MutedInk
import abubakir.edooxtest.ui.theme.SubjectBiology
import abubakir.edooxtest.ui.theme.SubjectHistory
import abubakir.edooxtest.ui.theme.SubjectKazakh
import abubakir.edooxtest.ui.theme.SubjectMath
import abubakir.edooxtest.ui.theme.WarmSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private data class Subject(val nameRes: Int, val emoji: String, val descRes: Int, val color: Color)

private val subjects = listOf(
    Subject(R.string.subject_math, "∑", R.string.subject_math_desc, SubjectMath),
    Subject(R.string.subject_history, "◎", R.string.subject_history_desc, SubjectHistory),
    Subject(R.string.subject_biology, "◌", R.string.subject_biology_desc, SubjectBiology),
    Subject(R.string.subject_kazakh, "Ә", R.string.subject_kazakh_desc, SubjectKazakh)
)

@Composable
fun SubjectScreen(
    viewModel: SubjectViewModel = viewModel(),
    autoRetrySubject: String?,
    onAutoRetryConsumed: () -> Unit,
    onNavigateToTest: (List<Question>, String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(autoRetrySubject) {
        autoRetrySubject?.let {
            viewModel.generateQuestions(it)
            onAutoRetryConsumed()
        }
    }

    LaunchedEffect(state) {
        if (state is SubjectViewModel.UiState.Success) {
            val s = state as SubjectViewModel.UiState.Success
            onNavigateToTest(s.questions, s.subject)
            viewModel.resetState()
        }
        if (state is SubjectViewModel.UiState.Error) {
            snackbarHostState.showSnackbar((state as SubjectViewModel.UiState.Error).message)
        }
    }

    if (state is SubjectViewModel.UiState.Loading) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color.White,
            shape = MaterialTheme.shapes.large,
            title = {
                Text(stringResource(R.string.dialog_generating_title), style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(bottom = 14.dp),
                        color = Blue700
                    )
                    Text(stringResource(R.string.dialog_generating_body), style = MaterialTheme.typography.bodyMedium, color = MutedInk)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::cancelGeneration) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                HeroCard()
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricPill(stringResource(R.string.metric_subjects_title), stringResource(R.string.metric_subjects_caption), Modifier.weight(1f))
                    MetricPill(stringResource(R.string.metric_questions_title), stringResource(R.string.metric_questions_caption), Modifier.weight(1f))
                    MetricPill(stringResource(R.string.metric_ai_title), stringResource(R.string.metric_ai_caption), Modifier.weight(1f))
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)) {
                    Text(
                        text = stringResource(R.string.subject_select_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink
                    )
                    Text(
                        text = stringResource(R.string.subject_select_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            items(subjects) { subject ->
                val subjectName = stringResource(subject.nameRes)
                SubjectCard(
                    subject = subject,
                    onClick = { viewModel.generateQuestions(subjectName) }
                )
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WarmSurface,
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.subject_next_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink
                        )
                        Text(
                            text = stringResource(R.string.subject_next_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFEAF2FF), Color(0xFFD9EDFF), Color(0xFFFFF2E6))
                    )
                )
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = Color.White.copy(alpha = 0.82f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(R.string.hero_badge),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Blue700,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Text(
                    text = stringResource(R.string.hero_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = stringResource(R.string.hero_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedInk,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricPill(title: String, caption: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(text = caption, style = MaterialTheme.typography.bodySmall, color = MutedInk)
        }
    }
}

@Composable
private fun SubjectCard(subject: Subject, onClick: () -> Unit) {
    val subjectName = stringResource(subject.nameRes)
    val subjectDesc = stringResource(subject.descRes)
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(subject.color, subject.color.copy(alpha = 0.76f))
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(subject.emoji, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = subjectName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Text(
                    text = subjectDesc,
                    color = Color.White.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(R.string.start_test),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }
        }
    }
}
