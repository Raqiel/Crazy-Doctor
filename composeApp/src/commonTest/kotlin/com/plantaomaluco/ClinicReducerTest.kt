package com.plantaomaluco

import com.plantaomaluco.clinic.ClinicIntent
import com.plantaomaluco.clinic.ClinicReducer
import com.plantaomaluco.clinic.ClinicState
import com.plantaomaluco.clinic.LoadState
import com.plantaomaluco.clinic.Screen
import com.plantaomaluco.domain.ProblemType
import com.plantaomaluco.domain.Tool
import com.plantaomaluco.game.GameIntent
import com.plantaomaluco.game.GameStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClinicReducerTest {

    private val patients = listOf(1, 2, 3).map { id ->
        testPatient(id = id, problems = listOf(spec(ProblemType.GERM, 0.5f, 0.5f)))
    }

    private val loaded = ClinicReducer.reduce(ClinicState.Initial, ClinicIntent.PatientsLoaded(patients))

    private fun ClinicState.send(vararg intents: ClinicIntent): ClinicState =
        intents.fold(this) { state, intent -> ClinicReducer.reduce(state, intent) }

    private val sprayGerm = arrayOf(
        ClinicIntent.Play(GameIntent.Begin),
        ClinicIntent.Play(GameIntent.SelectTool(Tool.SPRAY)),
        ClinicIntent.Play(GameIntent.Press(0.5f, 0.5f)),
        ClinicIntent.Play(GameIntent.Press(0.5f, 0.5f)),
        ClinicIntent.Play(GameIntent.Press(0.5f, 0.5f)),
        ClinicIntent.Play(GameIntent.Press(0.5f, 0.5f)),
    )

    @Test
    fun onlyFirstPatientStartsUnlocked() {
        assertEquals(LoadState.READY, loaded.loadState)
        assertTrue(loaded.isUnlocked(patients[0]))
        assertFalse(loaded.isUnlocked(patients[1]))

        val blocked = loaded.send(ClinicIntent.StartTreatment(2))
        assertNull(blocked.game)
        assertEquals(Screen.HOME, blocked.screen)
    }

    @Test
    fun winningUnlocksNextPatientAndKeepsBestStars() {
        val won = loaded.send(ClinicIntent.StartTreatment(1), *sprayGerm)

        assertEquals(GameStatus.WON, won.game?.status)
        assertEquals(2, won.unlockedCount)
        assertEquals(3, won.bestStars[1])
        assertTrue(won.hasNextPatient)

        val next = won.send(ClinicIntent.NextPatient)
        assertEquals(2, next.game?.patient?.id)
        assertEquals(GameStatus.BRIEFING, next.game?.status)
    }

    @Test
    fun losingDoesNotUnlockAndRetryRestarts() {
        val lost = loaded.send(
            ClinicIntent.StartTreatment(1),
            ClinicIntent.Play(GameIntent.Begin),
            ClinicIntent.Play(GameIntent.Tick(20_000)),
        )

        assertEquals(GameStatus.LOST, lost.game?.status)
        assertEquals(1, lost.unlockedCount)
        assertFalse(lost.hasNextPatient)

        val retried = lost.send(ClinicIntent.Retry)
        assertEquals(GameStatus.BRIEFING, retried.game?.status)
        assertEquals(100f, retried.game?.health)
    }

    @Test
    fun leavingGoesBackToPatientList() {
        val left = loaded.send(ClinicIntent.StartTreatment(1), ClinicIntent.LeaveTreatment)

        assertEquals(Screen.PATIENT_LIST, left.screen)
        assertNull(left.game)
    }
}
