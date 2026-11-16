package com.plantaomaluco

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantaomaluco.clinic.ClinicIntent
import com.plantaomaluco.clinic.ClinicState
import com.plantaomaluco.clinic.ClinicViewModel
import com.plantaomaluco.clinic.Screen
import com.plantaomaluco.data.PatientRepository
import com.plantaomaluco.resources.Res
import com.plantaomaluco.ui.ClinicTheme
import com.plantaomaluco.ui.HomeScreen
import com.plantaomaluco.ui.PatientListScreen
import com.plantaomaluco.ui.TreatmentScreen

@Composable
fun App() {
    val viewModel = viewModel { ClinicViewModel(PatientRepository { path -> Res.readBytes(path) }) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    ClinicTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                ClinicContent(state = state, onIntent = viewModel::onIntent)
            }
        }
    }
}

@Composable
private fun ClinicContent(state: ClinicState, onIntent: (ClinicIntent) -> Unit) {
    val game = state.game
    when (state.screen) {
        Screen.HOME -> HomeScreen(onPlay = { onIntent(ClinicIntent.OpenPatientList) })
        Screen.PATIENT_LIST -> PatientListScreen(state = state, onIntent = onIntent)
        Screen.TREATMENT -> if (game != null) {
            TreatmentScreen(game = game, hasNextPatient = state.hasNextPatient, onIntent = onIntent)
        }
    }
}
