package com.plantaomaluco.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.plantaomaluco.data.PatientRepository
import com.plantaomaluco.domain.Look
import com.plantaomaluco.resources.Res
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun rememberPortrait(image: String?): ImageBitmap? {
    val portrait by produceState<ImageBitmap?>(initialValue = null, image) {
        value = image?.let { loadPortrait(it) }
    }
    return portrait
}

@OptIn(ExperimentalResourceApi::class)
private suspend fun loadPortrait(file: String): ImageBitmap? = try {
    val bytes = Res.readBytes("${PatientRepository.PORTRAITS_DIR}/$file")
    withContext(Dispatchers.Default) { bytes.decodeToImageBitmap() }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}

@Composable
fun PatientPicture(image: String?, look: Look, mood: Float, modifier: Modifier) {
    val portrait = rememberPortrait(image)
    Canvas(modifier) {
        if (portrait != null) drawPortrait(portrait) else drawCartoonPatient(mood, look)
    }
}
