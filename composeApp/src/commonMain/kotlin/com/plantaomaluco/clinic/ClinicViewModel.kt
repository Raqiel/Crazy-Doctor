package com.plantaomaluco.clinic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantaomaluco.data.PatientRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClinicViewModel(private val repository: PatientRepository) : ViewModel() {

    private val mutableState = MutableStateFlow(ClinicState.Initial)
    val state: StateFlow<ClinicState> = mutableState.asStateFlow()

    init {
        loadPatients()
    }

    fun onIntent(intent: ClinicIntent) {
        mutableState.update { ClinicReducer.reduce(it, intent) }
        if (intent == ClinicIntent.ReloadPatients) loadPatients()
    }

    private fun loadPatients() {
        viewModelScope.launch {
            val result = try {
                ClinicIntent.PatientsLoaded(repository.loadPatients())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ClinicIntent.PatientsFailed
            }
            mutableState.update { ClinicReducer.reduce(it, result) }
        }
    }
}
