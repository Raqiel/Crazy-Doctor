package com.plantaomaluco.data

import com.plantaomaluco.domain.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class PatientRepository(private val readFile: suspend (String) -> ByteArray) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun loadPatients(): List<Patient> {
        val bytes = readFile(PATIENTS_FILE)
        return withContext(Dispatchers.Default) {
            json.decodeFromString<List<Patient>>(bytes.decodeToString())
        }
    }

    companion object {
        const val PATIENTS_FILE = "files/patients.json"
        const val PORTRAITS_DIR = "files/patients"
    }
}
