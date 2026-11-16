package com.plantaomaluco

import com.plantaomaluco.data.PatientRepository
import com.plantaomaluco.game.GameReducer
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PatientsFileTest {

    private val repository = PatientRepository { path ->
        File("src/commonMain/composeResources/$path").readBytes()
    }

    @Test
    fun patientsFileIsValid() = runBlocking {
        val patients = repository.loadPatients()

        assertTrue(patients.isNotEmpty())
        assertEquals(patients.size, patients.map { it.id }.toSet().size)
        patients.forEach { patient ->
            assertTrue(patient.problems.isNotEmpty(), patient.name)
            assertTrue(patient.maxHealth > 0f, patient.name)
            listOf(patient.look.skin, patient.look.hair, patient.look.gown).forEach { hex ->
                assertTrue(Regex("#[0-9A-Fa-f]{6}").matches(hex), "${patient.name} $hex")
            }
            patient.problems.forEach { problem ->
                assertTrue(problem.x in 0f..1f && problem.y in 0f..1f, "${patient.name} ${problem.type}")
            }
        }
    }

    @Test
    fun problemsDoNotOverlap() = runBlocking {
        repository.loadPatients().forEach { patient ->
            patient.problems.forEachIndexed { index, first ->
                patient.problems.drop(index + 1).forEach { second ->
                    val dx = first.x - second.x
                    val dy = (first.y - second.y) / GameReducer.BOARD_ASPECT
                    val distance = sqrt(dx * dx + dy * dy)
                    assertTrue(distance > GameReducer.HIT_RADIUS * 2, "${patient.name}: ${first.type} x ${second.type}")
                }
            }
        }
    }
}
