package com.plantaomaluco.clinic

import com.plantaomaluco.game.GameIntent
import com.plantaomaluco.game.GameReducer
import com.plantaomaluco.game.GameState
import com.plantaomaluco.game.GameStatus

object ClinicReducer {

    fun reduce(state: ClinicState, intent: ClinicIntent): ClinicState = when (intent) {
        ClinicIntent.OpenHome -> state.copy(screen = Screen.HOME, game = null)
        ClinicIntent.OpenPatientList -> state.copy(screen = Screen.PATIENT_LIST, game = null)
        ClinicIntent.ReloadPatients -> state.copy(loadState = LoadState.LOADING)
        is ClinicIntent.PatientsLoaded -> state.copy(loadState = LoadState.READY, patients = intent.patients)
        ClinicIntent.PatientsFailed -> state.copy(loadState = LoadState.FAILED)
        is ClinicIntent.StartTreatment -> start(state, intent.patientId)
        is ClinicIntent.Play -> play(state, intent.intent)
        ClinicIntent.Retry -> state.game?.let { start(state, it.patient.id) } ?: state
        ClinicIntent.NextPatient -> next(state)
        ClinicIntent.LeaveTreatment -> state.copy(screen = Screen.PATIENT_LIST, game = null)
    }

    private fun start(state: ClinicState, patientId: Int): ClinicState {
        val index = state.patients.indexOfFirst { it.id == patientId }
        if (index !in 0 until state.unlockedCount) return state
        return state.copy(screen = Screen.TREATMENT, game = GameState.start(state.patients[index]))
    }

    private fun play(state: ClinicState, intent: GameIntent): ClinicState {
        val current = state.game ?: return state
        val updated = GameReducer.reduce(current, intent)
        val justWon = current.status != GameStatus.WON && updated.status == GameStatus.WON
        if (!justWon) return state.copy(game = updated)
        val patientId = updated.patient.id
        val index = state.patients.indexOfFirst { it.id == patientId }
        val best = maxOf(state.bestStars[patientId] ?: 0, updated.stars)
        return state.copy(
            game = updated,
            bestStars = state.bestStars + (patientId to best),
            unlockedCount = maxOf(state.unlockedCount, (index + 2).coerceAtMost(state.patients.size)),
        )
    }

    private fun next(state: ClinicState): ClinicState {
        val current = state.game ?: return state
        val index = state.patients.indexOfFirst { it.id == current.patient.id }
        val nextPatient = state.patients.getOrNull(index + 1)
            ?: return state.copy(screen = Screen.PATIENT_LIST, game = null)
        return start(state, nextPatient.id)
    }
}
