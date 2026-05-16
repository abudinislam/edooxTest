package abubakir.edooxtest.ui.test

import abubakir.edooxtest.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import abubakir.edooxtest.data.model.MatchPair
import abubakir.edooxtest.data.model.Question
import abubakir.edooxtest.data.model.QuestionResult
import abubakir.edooxtest.data.model.UserAnswer
import abubakir.edooxtest.ui.theme.Blue700
import abubakir.edooxtest.ui.theme.Ink
import abubakir.edooxtest.ui.theme.MutedInk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    questions: List<Question>,
    viewModel: TestViewModel = viewModel(),
    onFinished: (List<QuestionResult>) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(questions)
    }

    LaunchedEffect(state) {
        if (state is TestViewModel.UiState.Done) {
            onFinished((state as TestViewModel.UiState.Done).results)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                val progress = (state as? TestViewModel.UiState.ShowQuestion)
                Surface(shadowElevation = 2.dp) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(stringResource(R.string.test_appbar_title), style = MaterialTheme.typography.labelLarge)
                                if (progress != null) {
                                    Text(
                                        stringResource(R.string.question_progress, progress.index + 1, progress.total),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.82f)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                if (state is TestViewModel.UiState.ShowQuestion) {
                    val s = state as TestViewModel.UiState.ShowQuestion
                    Surface(shadowElevation = 10.dp, color = Color.White) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            LinearProgressIndicator(
                                progress = { (s.index + 1).toFloat() / s.total },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Blue700,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Button(
                                onClick = { if (s.index == s.total - 1) viewModel.finish() else viewModel.next() },
                                enabled = s.canProceed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = stringResource(if (s.index == s.total - 1) R.string.btn_finish else R.string.btn_next),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            when (val s = state) {
                is TestViewModel.UiState.ShowQuestion -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        QuestionHeader(state = s)
                        QuestionCard(
                            state = s,
                            onAnswerChanged = { viewModel.setAnswer(it) },
                            onAnswerCleared = { viewModel.clearAnswer() }
                        )
                    }
                }
                else -> Unit
            }
        }

         if (state is TestViewModel.UiState.Evaluating) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier.size(width = 240.dp, height = 140.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.evaluating_answers), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    state: TestViewModel.UiState.ShowQuestion,
    onAnswerChanged: (UserAnswer) -> Unit,
    onAnswerCleared: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = when (state.question) {
                        is Question.MultipleChoice -> stringResource(R.string.question_type_multiple_choice)
                        is Question.OpenEnded -> stringResource(R.string.question_type_open_ended)
                        is Question.Matching -> stringResource(R.string.question_type_matching)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink
                )
            }
            Text(
                text = state.question.text,
                style = MaterialTheme.typography.titleLarge,
                lineHeight = MaterialTheme.typography.titleLarge.lineHeight,
                color = Ink,
                modifier = Modifier.padding(top = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            when (val q = state.question) {
                is Question.MultipleChoice -> MultipleChoiceView(
                    question = q,
                    savedAnswer = state.currentAnswer as? UserAnswer.MultipleChoice,
                    onAnswerChanged = onAnswerChanged,
                    onAnswerCleared = onAnswerCleared
                )
                is Question.OpenEnded -> OpenEndedView(
                    question = q,
                    savedAnswer = state.currentAnswer as? UserAnswer.OpenEnded,
                    onAnswerChanged = onAnswerChanged,
                    onAnswerCleared = onAnswerCleared
                )
                is Question.Matching -> MatchingView(
                    question = q,
                    savedAnswer = state.currentAnswer as? UserAnswer.Matching,
                    onAnswerChanged = onAnswerChanged
                )
            }
        }
    }
}

@Composable
private fun QuestionHeader(state: TestViewModel.UiState.ShowQuestion) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        color = Color.Transparent
    ) {
        Column {
            Text(
                text = stringResource(R.string.mini_test_title),
                style = MaterialTheme.typography.labelLarge,
                color = Blue700
            )
            Text(
                text = stringResource(R.string.mini_test_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MultipleChoiceView(
    question: Question.MultipleChoice,
    savedAnswer: UserAnswer.MultipleChoice?,
    onAnswerChanged: (UserAnswer) -> Unit,
    onAnswerCleared: () -> Unit
) {
    var selected by remember(question.id) { mutableStateOf(savedAnswer?.selected) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        question.options.forEach { option ->
            Card(
                onClick = {
                    selected = option
                    onAnswerChanged(UserAnswer.MultipleChoice(option))
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == option)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    RadioButton(selected = selected == option, onClick = {
                        selected = option
                        onAnswerChanged(UserAnswer.MultipleChoice(option))
                    })
                    Text(text = option, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun OpenEndedView(
    question: Question.OpenEnded,
    savedAnswer: UserAnswer.OpenEnded?,
    onAnswerChanged: (UserAnswer) -> Unit,
    onAnswerCleared: () -> Unit
) {
    var text by remember(question.id) { mutableStateOf(savedAnswer?.text ?: "") }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            if (newText.isNotBlank()) onAnswerChanged(UserAnswer.OpenEnded(newText))
            else onAnswerCleared()
        },
        placeholder = { Text(stringResource(R.string.open_answer_hint)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4,
        maxLines = 8,
        shape = MaterialTheme.shapes.medium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchingView(
    question: Question.Matching,
    savedAnswer: UserAnswer.Matching?,
    onAnswerChanged: (UserAnswer) -> Unit
) {
    val shuffledRights = remember(question.id) { question.pairs.map { it.right }.shuffled() }

    var answers by remember(question.id) {
        val initial = savedAnswer?.answers
            ?: question.pairs.associate { it.left to "" }
        mutableStateOf(initial)
    }

    Text(
        text = stringResource(R.string.matching_instruction),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        question.pairs.forEach { pair ->
            MatchingRow(
                pair = pair,
                options = shuffledRights,
                selected = answers[pair.left].orEmpty(),
                onSelect = { choice ->
                    val updated = answers.toMutableMap().also { it[pair.left] = choice }
                    answers = updated
                    if (updated.values.all { it.isNotBlank() }) {
                        onAnswerChanged(UserAnswer.Matching(updated))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchingRow(
    pair: MatchPair,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = pair.left,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.choose_option), style = MaterialTheme.typography.bodySmall) },
                textStyle = MaterialTheme.typography.bodySmall,
                shape = MaterialTheme.shapes.small
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodySmall) },
                        onClick = { onSelect(option); expanded = false },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
