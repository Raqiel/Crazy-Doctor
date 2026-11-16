package com.plantaomaluco.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plantaomaluco.clinic.ClinicIntent
import com.plantaomaluco.domain.Patient
import com.plantaomaluco.domain.Tool
import com.plantaomaluco.game.GameIntent
import com.plantaomaluco.game.GameReducer
import com.plantaomaluco.game.GameState
import com.plantaomaluco.game.GameStatus
import com.plantaomaluco.resources.Res
import com.plantaomaluco.resources.begin
import com.plantaomaluco.resources.briefing_hint
import com.plantaomaluco.resources.healed_count
import com.plantaomaluco.resources.health
import com.plantaomaluco.resources.leave
import com.plantaomaluco.resources.lost_body
import com.plantaomaluco.resources.lost_title
import com.plantaomaluco.resources.next_patient
import com.plantaomaluco.resources.other_patient
import com.plantaomaluco.resources.try_again
import com.plantaomaluco.resources.won_title
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource

private const val MAX_TICK_MILLIS = 100L
private const val NANOS_PER_MILLI = 1_000_000L

@Composable
fun TreatmentScreen(game: GameState, hasNextPatient: Boolean, onIntent: (ClinicIntent) -> Unit) {
    val play: (GameIntent) -> Unit = remember(onIntent) { { onIntent(ClinicIntent.Play(it)) } }

    GameLoop(running = game.status == GameStatus.PLAYING, onTick = { play(GameIntent.Tick(it)) })

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            TreatmentHeader(game = game, onLeave = { onIntent(ClinicIntent.LeaveTreatment) })
            Spacer(Modifier.height(8.dp))
            HealthBar(ratio = game.healthRatio)
            Spacer(Modifier.height(12.dp))
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                PatientBoard(
                    game = game,
                    onPress = { x, y -> play(GameIntent.Press(x, y)) },
                    onDrag = { x, y, distance -> play(GameIntent.Drag(x, y, distance)) },
                    modifier = Modifier.aspectRatio(GameReducer.BOARD_ASPECT, matchHeightConstraintsFirst = true),
                )
            }
            Spacer(Modifier.height(12.dp))
            ToolBar(selected = game.selectedTool, onSelect = { play(GameIntent.SelectTool(it)) })
        }

        if (game.hurtFlash > 0f) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.error.copy(alpha = game.hurtFlash * 0.25f)))
        }

        when (game.status) {
            GameStatus.BRIEFING -> BriefingDialog(patient = game.patient, onBegin = { play(GameIntent.Begin) })
            GameStatus.WON, GameStatus.LOST -> ResultDialog(
                game = game,
                hasNextPatient = hasNextPatient,
                onRetry = { onIntent(ClinicIntent.Retry) },
                onNext = { onIntent(ClinicIntent.NextPatient) },
                onLeave = { onIntent(ClinicIntent.LeaveTreatment) },
            )
            GameStatus.PLAYING -> Unit
        }
    }
}

@Composable
private fun GameLoop(running: Boolean, onTick: (Long) -> Unit) {
    val currentOnTick by rememberUpdatedState(onTick)
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                val deltaMillis = (now - last) / NANOS_PER_MILLI
                if (deltaMillis > 0L) {
                    currentOnTick(deltaMillis.coerceAtMost(MAX_TICK_MILLIS))
                    last += deltaMillis * NANOS_PER_MILLI
                }
            }
        }
    }
}

@Composable
private fun TreatmentHeader(game: GameState, onLeave: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onLeave) {
            Text(stringResource(Res.string.leave))
        }
        Text(
            text = game.patient.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(Res.string.healed_count, game.healedCount, game.problems.size),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun HealthBar(ratio: Float) {
    val color = if (ratio > 0.35f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.health),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 8.dp),
        )
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.weight(1f).height(14.dp).clip(RoundedCornerShape(7.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun PatientBoard(
    game: GameState,
    onPress: (Float, Float) -> Unit,
    onDrag: (Float, Float, Float) -> Unit,
    modifier: Modifier,
) {
    val portrait = rememberPortrait(game.patient.image)
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnDrag by rememberUpdatedState(onDrag)
    var pointer by remember { mutableStateOf<Offset?>(null) }
    val toolColor = game.selectedTool.accent

    Canvas(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()
                    pointer = down.position
                    currentOnPress(down.position.x / width, down.position.y / height)
                    var last = down.position
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val moved = (change.position - last).getDistance() / width
                        if (moved > 0f) currentOnDrag(change.position.x / width, change.position.y / height, moved)
                        last = change.position
                        pointer = change.position
                        change.consume()
                    }
                    pointer = null
                }
            },
    ) {
        if (portrait != null) drawPortrait(portrait) else drawCartoonPatient(game.healthRatio, game.patient.look)
        game.problems.forEach { drawProblem(it, highlighted = it.type.tool == game.selectedTool) }
        pointer?.let { position ->
            val radius = size.width * 0.07f
            drawCircle(toolColor.copy(alpha = 0.3f), radius = radius, center = position)
            drawCircle(toolColor, radius = radius, center = position, style = Stroke(width = size.width * 0.012f))
        }
    }
}

@Composable
private fun ToolBar(selected: Tool, onSelect: (Tool) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Tool.entries.forEach { tool ->
            ToolButton(tool = tool, selected = tool == selected, onClick = { onSelect(tool) })
        }
    }
}

@Composable
private fun ToolButton(tool: Tool, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(4.dp),
    ) {
        Canvas(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (selected) tool.accent.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface)
                .border(3.dp, if (selected) tool.accent else Color.Transparent, CircleShape)
                .padding(8.dp),
        ) {
            drawToolIcon(tool)
        }
        Text(
            text = stringResource(tool.label),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun DialogCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun BriefingDialog(patient: Patient, onBegin: () -> Unit) {
    DialogCard {
        Text(patient.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(patient.story, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Text(
            text = stringResource(Res.string.briefing_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onBegin, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.begin))
        }
    }
}

@Composable
private fun ResultDialog(
    game: GameState,
    hasNextPatient: Boolean,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onLeave: () -> Unit,
) {
    val won = game.status == GameStatus.WON
    DialogCard {
        Text(
            text = stringResource(if (won) Res.string.won_title else Res.string.lost_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (won) {
            Text(starsText(game.stars), style = MaterialTheme.typography.displaySmall, color = Gold)
        } else {
            Text(stringResource(Res.string.lost_body), textAlign = TextAlign.Center)
        }
        if (won && hasNextPatient) {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.next_patient))
            }
        }
        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.try_again))
        }
        TextButton(onClick = onLeave) {
            Text(stringResource(Res.string.other_patient))
        }
    }
}
