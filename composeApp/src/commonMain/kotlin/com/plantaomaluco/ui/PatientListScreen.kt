package com.plantaomaluco.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plantaomaluco.clinic.ClinicIntent
import com.plantaomaluco.clinic.ClinicState
import com.plantaomaluco.clinic.LoadState
import com.plantaomaluco.domain.Patient
import com.plantaomaluco.game.GameReducer
import com.plantaomaluco.resources.Res
import com.plantaomaluco.resources.back
import com.plantaomaluco.resources.choose_patient
import com.plantaomaluco.resources.load_failed
import com.plantaomaluco.resources.locked
import com.plantaomaluco.resources.try_again
import org.jetbrains.compose.resources.stringResource

@Composable
fun PatientListScreen(state: ClinicState, onIntent: (ClinicIntent) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onIntent(ClinicIntent.OpenHome) }) {
                Text(stringResource(Res.string.back))
            }
            Text(
                text = stringResource(Res.string.choose_patient),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(12.dp))
        when (state.loadState) {
            LoadState.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            LoadState.FAILED -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(Res.string.load_failed))
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onIntent(ClinicIntent.ReloadPatients) }) {
                    Text(stringResource(Res.string.try_again))
                }
            }
            LoadState.READY -> LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.patients, key = { it.id }) { patient ->
                    PatientCard(
                        patient = patient,
                        unlocked = state.isUnlocked(patient),
                        stars = state.bestStars[patient.id] ?: 0,
                        onClick = { onIntent(ClinicIntent.StartTreatment(patient.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientCard(patient: Patient, unlocked: Boolean, stars: Int, onClick: () -> Unit) {
    Card(onClick = onClick, enabled = unlocked, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PatientPicture(
                image = patient.image,
                look = patient.look,
                mood = 1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(GameReducer.BOARD_ASPECT)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .alpha(if (unlocked) 1f else 0.35f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = patient.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (unlocked) starsText(stars) else stringResource(Res.string.locked),
                color = if (unlocked) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
