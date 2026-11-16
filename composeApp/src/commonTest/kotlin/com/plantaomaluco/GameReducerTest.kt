package com.plantaomaluco

import com.plantaomaluco.domain.ProblemType
import com.plantaomaluco.domain.Tool
import com.plantaomaluco.game.GameIntent
import com.plantaomaluco.game.GameState
import com.plantaomaluco.game.GameStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameReducerTest {

    private val bruiseAndCut = testPatient(
        id = 1,
        problems = listOf(
            spec(ProblemType.BRUISE, 0.2f, 0.2f),
            spec(ProblemType.CUT, 0.8f, 0.8f),
        ),
    )

    @Test
    fun ignoresActionsBeforeBriefingIsDismissed() {
        val briefing = GameState.start(bruiseAndCut)

        val after = briefing.apply(GameIntent.Tick(1000), GameIntent.Press(0.2f, 0.2f))

        assertEquals(GameStatus.BRIEFING, after.status)
        assertEquals(100f, after.health)
    }

    @Test
    fun wrongToolHurtsAndFlashes() {
        val state = playing(bruiseAndCut).apply(
            GameIntent.SelectTool(Tool.SPRAY),
            GameIntent.Press(0.2f, 0.2f),
        )

        assertEquals(80f, state.health)
        assertEquals(1f, state.hurtFlash)
        assertEquals(0f, state.problems[0].progress)
    }

    @Test
    fun pressingEmptySpotDoesNothing() {
        val state = playing(bruiseAndCut).apply(
            GameIntent.SelectTool(Tool.SPRAY),
            GameIntent.Press(0.5f, 0.5f),
        )

        assertEquals(100f, state.health)
        assertEquals(0f, state.hurtFlash)
    }

    @Test
    fun bandageHealsBruiseInTwoTapsAndGivesBonus() {
        val hurt = playing(bruiseAndCut).apply(GameIntent.Tick(1000))
        assertEquals(90f, hurt.health)

        val state = hurt.apply(
            GameIntent.SelectTool(Tool.BANDAGE),
            GameIntent.Press(0.2f, 0.2f),
            GameIntent.Press(0.2f, 0.2f),
        )

        assertTrue(state.problems[0].healed)
        assertEquals(95f, state.health)
        assertEquals(GameStatus.PLAYING, state.status)
    }

    @Test
    fun dragToolOnlyHealsWhileDragging() {
        val selected = playing(bruiseAndCut).apply(GameIntent.SelectTool(Tool.NEEDLE))

        val pressed = selected.apply(GameIntent.Press(0.8f, 0.8f))
        assertEquals(0f, pressed.problems[1].progress)

        val dragged = pressed.apply(GameIntent.Drag(0.8f, 0.8f, 0.25f))
        assertEquals(0.45f, dragged.problems[1].progress, 0.0001f)
    }

    @Test
    fun dragWithMatchingToolOnOtherProblemTypeDoesNotHurt() {
        val state = playing(bruiseAndCut).apply(
            GameIntent.SelectTool(Tool.NEEDLE),
            GameIntent.Drag(0.2f, 0.2f, 0.3f),
        )

        assertEquals(100f, state.health)
        assertEquals(0f, state.problems[0].progress)
    }

    @Test
    fun healingEverythingWinsWithStars() {
        val state = playing(bruiseAndCut).apply(
            GameIntent.SelectTool(Tool.BANDAGE),
            GameIntent.Press(0.2f, 0.2f),
            GameIntent.Press(0.2f, 0.2f),
            GameIntent.SelectTool(Tool.NEEDLE),
            GameIntent.Drag(0.8f, 0.8f, 0.6f),
        )

        assertEquals(GameStatus.WON, state.status)
        assertEquals(3, state.stars)
    }

    @Test
    fun healthDrainingToZeroLoses() {
        val state = playing(bruiseAndCut).apply(GameIntent.Tick(10_000))

        assertEquals(0f, state.health)
        assertEquals(GameStatus.LOST, state.status)
        assertEquals(0, state.stars)
    }

    @Test
    fun finishedGameIgnoresNewIntents() {
        val lost = playing(bruiseAndCut).apply(GameIntent.Tick(10_000))

        val after = lost.apply(GameIntent.SelectTool(Tool.BANDAGE), GameIntent.Press(0.2f, 0.2f))

        assertEquals(lost, after)
    }

    @Test
    fun hurtFlashFadesOverTime() {
        val state = playing(bruiseAndCut).apply(
            GameIntent.SelectTool(Tool.SPRAY),
            GameIntent.Press(0.2f, 0.2f),
            GameIntent.Tick(200),
        )

        assertEquals(0.5f, state.hurtFlash, 0.0001f)
    }
}
