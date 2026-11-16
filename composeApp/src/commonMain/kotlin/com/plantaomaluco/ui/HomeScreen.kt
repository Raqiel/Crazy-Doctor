package com.plantaomaluco.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.plantaomaluco.domain.Look
import com.plantaomaluco.game.GameReducer
import com.plantaomaluco.resources.Res
import com.plantaomaluco.resources.app_name
import com.plantaomaluco.resources.home_subtitle
import com.plantaomaluco.resources.play
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(onPlay: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 420.dp),
        )
        Spacer(Modifier.height(24.dp))
        PatientPicture(
            image = null,
            look = Look.Mascot,
            mood = 1f,
            modifier = Modifier
                .widthIn(max = 260.dp)
                .fillMaxWidth(0.6f)
                .aspectRatio(GameReducer.BOARD_ASPECT)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onPlay,
            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth(0.7f).height(56.dp),
        ) {
            Text(stringResource(Res.string.play), style = MaterialTheme.typography.titleMedium)
        }
    }
}
