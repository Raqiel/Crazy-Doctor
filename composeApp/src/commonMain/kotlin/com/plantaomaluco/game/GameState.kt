package com.plantaomaluco.game

import com.plantaomaluco.domain.Patient
import com.plantaomaluco.domain.ProblemType
import com.plantaomaluco.domain.Tool

data class Problem(
    val id: Int,
    val type: ProblemType,
    val x: Float,
    val y: Float,
    val progress: Float,
) {
    val healed: Boolean get() = progress >= 1f
}

enum class GameStatus { BRIEFING, PLAYING, WON, LOST }

data class GameState(
    val patient: Patient,
    val health: Float,
    val selectedTool: Tool,
    val problems: List<Problem>,
    val status: GameStatus,
    val hurtFlash: Float,
    val elapsedMillis: Long,
) {
    val healedCount: Int get() = problems.count { it.healed }

    val healthRatio: Float get() = (health / patient.maxHealth).coerceIn(0f, 1f)

    val stars: Int
        get() = when {
            status != GameStatus.WON -> 0
            healthRatio >= 0.7f -> 3
            healthRatio >= 0.4f -> 2
            else -> 1
        }

    companion object {
        fun start(patient: Patient): GameState = GameState(
            patient = patient,
            health = patient.maxHealth,
            selectedTool = Tool.TWEEZERS,
            problems = patient.problems.mapIndexed { index, spec ->
                Problem(id = index, type = spec.type, x = spec.x, y = spec.y, progress = 0f)
            },
            status = GameStatus.BRIEFING,
            hurtFlash = 0f,
            elapsedMillis = 0L,
        )
    }
}

sealed interface GameIntent {
    data object Begin : GameIntent
    data class SelectTool(val tool: Tool) : GameIntent
    data class Press(val x: Float, val y: Float) : GameIntent
    data class Drag(val x: Float, val y: Float, val distance: Float) : GameIntent
    data class Tick(val deltaMillis: Long) : GameIntent
}
