package com.plantaomaluco

import com.plantaomaluco.domain.Look
import com.plantaomaluco.domain.Patient
import com.plantaomaluco.domain.ProblemSpec
import com.plantaomaluco.domain.ProblemType
import com.plantaomaluco.game.GameIntent
import com.plantaomaluco.game.GameReducer
import com.plantaomaluco.game.GameState

fun testPatient(id: Int, problems: List<ProblemSpec>): Patient = Patient(
    id = id,
    name = "Paciente $id",
    story = "Historia $id",
    image = null,
    look = Look.Mascot,
    maxHealth = 100f,
    drainPerSecond = 10f,
    wrongToolDamage = 20f,
    problems = problems,
)

fun spec(type: ProblemType, x: Float, y: Float): ProblemSpec = ProblemSpec(type = type, x = x, y = y)

fun playing(patient: Patient): GameState =
    GameReducer.reduce(GameState.start(patient), GameIntent.Begin)

fun GameState.apply(vararg intents: GameIntent): GameState =
    intents.fold(this) { state, intent -> GameReducer.reduce(state, intent) }
