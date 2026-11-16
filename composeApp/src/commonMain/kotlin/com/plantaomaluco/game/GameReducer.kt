package com.plantaomaluco.game

import com.plantaomaluco.domain.Gesture
import kotlin.math.sqrt

object GameReducer {
    const val BOARD_ASPECT = 0.75f
    const val HIT_RADIUS = 0.1f
    const val HEAL_BONUS_RATIO = 0.05f
    private const val HURT_FADE_MILLIS = 400f

    fun reduce(state: GameState, intent: GameIntent): GameState = when (state.status) {
        GameStatus.BRIEFING -> if (intent is GameIntent.Begin) state.copy(status = GameStatus.PLAYING) else state
        GameStatus.PLAYING -> play(state, intent).settle()
        GameStatus.WON, GameStatus.LOST -> state
    }

    private fun play(state: GameState, intent: GameIntent): GameState = when (intent) {
        GameIntent.Begin -> state
        is GameIntent.SelectTool -> state.copy(selectedTool = intent.tool)
        is GameIntent.Press -> press(state, intent.x, intent.y)
        is GameIntent.Drag -> drag(state, intent.x, intent.y, intent.distance)
        is GameIntent.Tick -> tick(state, intent.deltaMillis)
    }

    private fun press(state: GameState, x: Float, y: Float): GameState {
        val target = state.problemAt(x, y) ?: return state
        return when {
            target.type.tool != state.selectedTool -> state.hurt()
            state.selectedTool.gesture == Gesture.TAP -> state.heal(target, target.type.healPerUse)
            else -> state
        }
    }

    private fun drag(state: GameState, x: Float, y: Float, distance: Float): GameState {
        if (state.selectedTool.gesture != Gesture.DRAG || distance <= 0f) return state
        val target = state.problemAt(x, y) ?: return state
        if (target.type.tool != state.selectedTool) return state
        return state.heal(target, distance * target.type.healPerUse)
    }

    private fun tick(state: GameState, deltaMillis: Long): GameState {
        if (deltaMillis <= 0L) return state
        val seconds = deltaMillis / 1000f
        return state.copy(
            health = (state.health - state.patient.drainPerSecond * seconds).coerceAtLeast(0f),
            hurtFlash = (state.hurtFlash - deltaMillis / HURT_FADE_MILLIS).coerceAtLeast(0f),
            elapsedMillis = state.elapsedMillis + deltaMillis,
        )
    }

    private fun GameState.problemAt(x: Float, y: Float): Problem? = problems
        .filterNot { it.healed }
        .map { it to distanceTo(it, x, y) }
        .filter { (_, distance) -> distance <= HIT_RADIUS }
        .minByOrNull { (_, distance) -> distance }
        ?.first

    private fun distanceTo(problem: Problem, x: Float, y: Float): Float {
        val dx = problem.x - x
        val dy = (problem.y - y) / BOARD_ASPECT
        return sqrt(dx * dx + dy * dy)
    }

    private fun GameState.hurt(): GameState = copy(
        health = (health - patient.wrongToolDamage).coerceAtLeast(0f),
        hurtFlash = 1f,
    )

    private fun GameState.heal(target: Problem, amount: Float): GameState {
        val progress = (target.progress + amount).coerceAtMost(1f)
        val bonus = if (progress >= 1f) patient.maxHealth * HEAL_BONUS_RATIO else 0f
        return copy(
            problems = problems.map { if (it.id == target.id) it.copy(progress = progress) else it },
            health = (health + bonus).coerceAtMost(patient.maxHealth),
        )
    }

    private fun GameState.settle(): GameState = when {
        problems.all { it.healed } -> copy(status = GameStatus.WON)
        health <= 0f -> copy(status = GameStatus.LOST)
        else -> this
    }
}
