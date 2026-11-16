package com.plantaomaluco.clinic

import com.plantaomaluco.domain.Patient
import com.plantaomaluco.game.GameIntent
import com.plantaomaluco.game.GameState

enum class Screen { HOME, PATIENT_LIST, TREATMENT }

enum class LoadState { LOADING, READY, FAILED }

data class ClinicState(
    val screen: Screen,
    val loadState: LoadState,
    val patients: List<Patient>,
    val unlockedCount: Int,
    val bestStars: Map<Int, Int>,
    val game: GameState?,
) {
    fun isUnlocked(patient: Patient): Boolean = patients.indexOf(patient) in 0 until unlockedCount

    val hasNextPatient: Boolean
        get() {
            val current = game ?: return false
            val index = patients.indexOfFirst { it.id == current.patient.id }
            return index >= 0 && index + 1 < patients.size && index + 1 < unlockedCount
        }

    companion object {
        val Initial = ClinicState(
            screen = Screen.HOME,
            loadState = LoadState.LOADING,
            patients = emptyList(),
            unlockedCount = 1,
            bestStars = emptyMap(),
            game = null,
        )
    }
}

sealed interface ClinicIntent {
    data object OpenHome : ClinicIntent
    data object OpenPatientList : ClinicIntent
    data object ReloadPatients : ClinicIntent
    data class PatientsLoaded(val patients: List<Patient>) : ClinicIntent
    data object PatientsFailed : ClinicIntent
    data class StartTreatment(val patientId: Int) : ClinicIntent
    data class Play(val intent: GameIntent) : ClinicIntent
    data object Retry : ClinicIntent
    data object NextPatient : ClinicIntent
    data object LeaveTreatment : ClinicIntent
}
