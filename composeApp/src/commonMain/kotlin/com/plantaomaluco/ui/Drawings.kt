package com.plantaomaluco.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.plantaomaluco.domain.Accessory
import com.plantaomaluco.domain.HairStyle
import com.plantaomaluco.domain.Look
import com.plantaomaluco.domain.ProblemType
import com.plantaomaluco.domain.Tool
import com.plantaomaluco.game.Problem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val HelmetRed = Color(0xFFD32F2F)
private val HelmetBadge = Color(0xFFFFC107)
private val Ink = Color(0xFF3A2E2A)
private val Cheek = Color(0xFFFF8A80)
private val Blood = Color(0xFFD7263D)
private val Scar = Color(0xFFE8A0A0)
private val Wood = Color(0xFF8B5A2B)
private val Mud = Color(0xFF7A5230)
private val GermGreen = Color(0xFF6CC24A)
private val BruisePurple = Color(0xFF8E5BB5)
private val BandageBeige = Color(0xFFF3D9A4)
private val BandagePad = Color(0xFFE6BF7A)
private val Thread = Color(0xFF243B55)
private val Steel = Color(0xFF9AA5B1)
private val SpongeYellow = Color(0xFFFFD54F)
private val SpongeHole = Color(0xFFE0A800)
private val SprayBlue = Color(0xFF4FC3F7)
private val SprayCap = Color(0xFF607D8B)

fun DrawScope.drawPortrait(image: ImageBitmap) {
    val scale = min(size.width / image.width, size.height / image.height)
    val width = (image.width * scale).roundToInt()
    val height = (image.height * scale).roundToInt()
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(((size.width - width) / 2f).roundToInt(), ((size.height - height) / 2f).roundToInt()),
        dstSize = IntSize(width, height),
    )
}

fun String.toColor(): Color = Color(("FF" + removePrefix("#")).toLong(16))

private fun Color.shade(factor: Float): Color = Color(red * factor, green * factor, blue * factor, alpha)

fun DrawScope.drawCartoonPatient(mood: Float, look: Look) {
    val w = size.width
    val h = size.height
    val head = Offset(w * 0.5f, h * 0.3f)
    val r = w * 0.22f
    val skin = look.skin.toColor()
    val hair = look.hair.toColor()
    val gown = look.gown.toColor()

    if (look.hairStyle == HairStyle.LONG) {
        drawRoundRect(hair, topLeft = Offset(head.x - r * 1.1f, head.y - r * 0.6f), size = Size(r * 2.2f, r * 1.9f), cornerRadius = CornerRadius(r * 0.6f))
    }
    drawRoundRect(skin, topLeft = Offset(w * 0.06f, h * 0.55f), size = Size(w * 0.14f, h * 0.36f), cornerRadius = CornerRadius(w * 0.07f))
    drawRoundRect(skin, topLeft = Offset(w * 0.8f, h * 0.55f), size = Size(w * 0.14f, h * 0.36f), cornerRadius = CornerRadius(w * 0.07f))
    drawRoundRect(gown, topLeft = Offset(w * 0.18f, h * 0.5f), size = Size(w * 0.64f, h * 0.56f), cornerRadius = CornerRadius(w * 0.18f))
    drawRoundRect(gown.shade(0.8f), topLeft = Offset(w * 0.47f, h * 0.55f), size = Size(w * 0.06f, h * 0.45f), cornerRadius = CornerRadius(w * 0.03f))
    drawRect(skin, topLeft = Offset(w * 0.43f, h * 0.42f), size = Size(w * 0.14f, h * 0.1f))

    drawCircle(skin, radius = w * 0.05f, center = Offset(w * 0.28f, h * 0.31f))
    drawCircle(skin, radius = w * 0.05f, center = Offset(w * 0.72f, h * 0.31f))
    drawCircle(skin, radius = r, center = head)
    drawHair(look.hairStyle, hair, head, r)

    listOf(-1f, 1f).forEach { side ->
        val eye = Offset(head.x + side * r * 0.38f, head.y + r * 0.05f)
        drawCircle(Ink, radius = r * 0.09f, center = eye)
        drawCircle(Color.White, radius = r * 0.03f, center = eye + Offset(r * 0.03f, -r * 0.03f))
        drawCircle(Cheek.copy(alpha = 0.4f), radius = r * 0.12f, center = Offset(head.x + side * r * 0.55f, head.y + r * 0.3f))
    }

    val mouthStroke = Stroke(width = r * 0.07f, cap = StrokeCap.Round)
    if (mood > 0.5f) {
        drawArc(Ink, 20f, 140f, false, Offset(head.x - r * 0.3f, head.y + r * 0.15f), Size(r * 0.6f, r * 0.4f), style = mouthStroke)
    } else {
        drawArc(Ink, 200f, 140f, false, Offset(head.x - r * 0.3f, head.y + r * 0.42f), Size(r * 0.6f, r * 0.4f), style = mouthStroke)
    }
    drawAccessory(look.accessory, hair, head, r)
}

private fun DrawScope.drawHairCap(hair: Color, head: Offset, r: Float, height: Float) {
    drawArc(
        color = hair,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(head.x - r * 1.02f, head.y - r * 1.08f),
        size = Size(r * 2.04f, r * height),
    )
}

private fun DrawScope.drawHair(style: HairStyle, hair: Color, head: Offset, r: Float) {
    when (style) {
        HairStyle.SHORT, HairStyle.LONG -> drawHairCap(hair, head, r, 1.3f)
        HairStyle.BUN -> {
            drawCircle(hair, radius = r * 0.36f, center = Offset(head.x, head.y - r * 1.12f))
            drawHairCap(hair, head, r, 1.3f)
        }
        HairStyle.SPIKY -> {
            drawHairCap(hair, head, r, 1.1f)
            val spikes = Path()
            repeat(5) { index ->
                val left = head.x - r * 0.9f + index * r * 0.36f
                spikes.moveTo(left, head.y - r * 0.6f)
                spikes.lineTo(left + r * 0.36f, head.y - r * 0.6f)
                spikes.lineTo(left + r * 0.18f, head.y - r * 1.3f)
                spikes.close()
            }
            drawPath(spikes, hair)
        }
        HairStyle.BALD -> {
            drawCircle(hair, radius = r * 0.24f, center = Offset(head.x - r * 0.93f, head.y - r * 0.15f))
            drawCircle(hair, radius = r * 0.24f, center = Offset(head.x + r * 0.93f, head.y - r * 0.15f))
            drawCircle(Color.White.copy(alpha = 0.35f), radius = r * 0.16f, center = Offset(head.x - r * 0.35f, head.y - r * 0.6f))
        }
    }
}

private fun DrawScope.drawAccessory(accessory: Accessory, hair: Color, head: Offset, r: Float) {
    when (accessory) {
        Accessory.NONE -> Unit
        Accessory.GLASSES -> {
            val y = head.y + r * 0.05f
            listOf(-1f, 1f).forEach { side ->
                drawCircle(Ink, radius = r * 0.2f, center = Offset(head.x + side * r * 0.38f, y), style = Stroke(width = r * 0.05f))
            }
            drawLine(Ink, Offset(head.x - r * 0.18f, y), Offset(head.x + r * 0.18f, y), strokeWidth = r * 0.05f)
        }
        Accessory.MUSTACHE -> {
            drawOval(hair, topLeft = Offset(head.x - r * 0.38f, head.y + r * 0.12f), size = Size(r * 0.38f, r * 0.17f))
            drawOval(hair, topLeft = Offset(head.x, head.y + r * 0.12f), size = Size(r * 0.38f, r * 0.17f))
        }
        Accessory.HELMET -> {
            drawArc(
                color = HelmetRed,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(head.x - r * 1.12f, head.y - r * 1.2f),
                size = Size(r * 2.24f, r * 1.5f),
            )
            drawRoundRect(HelmetRed.shade(0.75f), topLeft = Offset(head.x - r * 1.3f, head.y - r * 0.52f), size = Size(r * 2.6f, r * 0.2f), cornerRadius = CornerRadius(r * 0.1f))
            drawCircle(HelmetBadge, radius = r * 0.15f, center = Offset(head.x, head.y - r * 0.85f))
        }
    }
}

fun DrawScope.drawProblem(problem: Problem, highlighted: Boolean) {
    val center = Offset(problem.x * size.width, problem.y * size.height)
    val unit = size.width * 0.06f
    if (highlighted && !problem.healed) {
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = unit * 1.7f,
            center = center,
            style = Stroke(width = unit * 0.18f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(unit * 0.4f, unit * 0.3f))),
        )
    }
    when (problem.type) {
        ProblemType.SPLINTER -> drawSplinter(center, unit, problem)
        ProblemType.CUT -> drawCut(center, unit, problem)
        ProblemType.DIRT -> drawDirt(center, unit, problem)
        ProblemType.GERM -> drawGerm(center, unit, problem)
        ProblemType.BRUISE -> drawBruise(center, unit, problem)
    }
}

private fun DrawScope.drawSplinter(center: Offset, unit: Float, problem: Problem) {
    if (problem.healed) return
    val pull = Offset(-0.6f, -0.8f) * (problem.progress * unit * 1.6f)
    drawCircle(Blood.copy(alpha = 0.5f * (1f - problem.progress)), radius = unit * 0.35f, center = center)
    drawLine(
        color = Wood,
        start = center + Offset(unit * 0.2f, unit * 0.27f) + pull,
        end = center + Offset(-unit * 0.7f, -unit * 0.95f) + pull,
        strokeWidth = unit * 0.28f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawCut(center: Offset, unit: Float, problem: Problem) {
    val half = unit * 1.3f
    val color = if (problem.healed) Scar else Blood
    val width = if (problem.healed) unit * 0.15f else unit * 0.32f
    drawLine(color, center - Offset(half, 0f), center + Offset(half, 0f), strokeWidth = width, cap = StrokeCap.Round)
    val stitches = (problem.progress * STITCHES).toInt().coerceAtMost(STITCHES)
    repeat(stitches) { index ->
        val x = center.x - half + half * 2f * (index + 0.5f) / STITCHES
        drawLine(
            color = Thread,
            start = Offset(x - unit * 0.12f, center.y - unit * 0.35f),
            end = Offset(x + unit * 0.12f, center.y + unit * 0.35f),
            strokeWidth = unit * 0.1f,
            cap = StrokeCap.Round,
        )
    }
}

private const val STITCHES = 5

private val DirtBlobs = listOf(
    Offset(0f, 0f) to 0.8f,
    Offset(0.9f, 0.4f) to 0.55f,
    Offset(-0.7f, 0.5f) to 0.5f,
    Offset(0.3f, -0.7f) to 0.4f,
)

private fun DrawScope.drawDirt(center: Offset, unit: Float, problem: Problem) {
    if (problem.healed) return
    val alpha = 0.85f * (1f - problem.progress)
    DirtBlobs.forEach { (offset, radius) ->
        drawCircle(Mud.copy(alpha = alpha), radius = unit * radius, center = center + offset * unit)
    }
}

private fun DrawScope.drawGerm(center: Offset, unit: Float, problem: Problem) {
    if (problem.healed) return
    val remaining = 1f - problem.progress
    val r = unit * (0.45f + 0.55f * remaining)
    val alpha = 0.4f + 0.6f * remaining
    val body = GermGreen.copy(alpha = alpha)
    repeat(8) { index ->
        val angle = index * PI.toFloat() / 4f
        val direction = Offset(cos(angle), sin(angle))
        drawLine(body, center + direction * (r * 0.8f), center + direction * (r * 1.35f), strokeWidth = r * 0.18f, cap = StrokeCap.Round)
        drawCircle(body, radius = r * 0.16f, center = center + direction * (r * 1.4f))
    }
    drawCircle(body, radius = r, center = center)
    listOf(-1f, 1f).forEach { side ->
        val eye = center + Offset(side * r * 0.35f, -r * 0.15f)
        drawCircle(Color.White.copy(alpha = alpha), radius = r * 0.25f, center = eye)
        drawCircle(Ink.copy(alpha = alpha), radius = r * 0.12f, center = eye)
    }
}

private fun DrawScope.drawBruise(center: Offset, unit: Float, problem: Problem) {
    drawCircle(BruisePurple.copy(alpha = if (problem.healed) 0.25f else 0.7f), radius = unit, center = center)
    if (!problem.healed) drawCircle(BruisePurple.copy(alpha = 0.5f), radius = unit * 0.55f, center = center)
    if (problem.progress <= 0f) return
    val alpha = 0.4f + 0.6f * problem.progress
    rotate(degrees = -20f, pivot = center) {
        drawRoundRect(
            color = BandageBeige.copy(alpha = alpha),
            topLeft = center - Offset(unit * 1.5f, unit * 0.5f),
            size = Size(unit * 3f, unit),
            cornerRadius = CornerRadius(unit * 0.4f),
        )
        drawRoundRect(
            color = BandagePad.copy(alpha = alpha),
            topLeft = center - Offset(unit * 0.45f, unit * 0.4f),
            size = Size(unit * 0.9f, unit * 0.8f),
            cornerRadius = CornerRadius(unit * 0.15f),
        )
    }
}

fun DrawScope.drawToolInHand(tool: Tool, position: Offset, scale: Float) {
    val iconSize = size.width * 0.18f
    val anchor = Offset(iconSize * 0.2f, iconSize * 1.05f)
    val middle = Offset(iconSize / 2f, iconSize / 2f)
    drawCircle(tool.accent.copy(alpha = 0.35f * scale), radius = size.width * 0.03f, center = position)
    withTransform({
        translate(position.x - anchor.x, position.y - anchor.y)
        scale(scale, scale, pivot = anchor)
    }) {
        drawCircle(Color.White.copy(alpha = 0.9f), radius = iconSize * 0.62f, center = middle)
        drawCircle(tool.accent, radius = iconSize * 0.62f, center = middle, style = Stroke(width = iconSize * 0.06f))
        drawToolIcon(tool, iconSize)
    }
}

fun DrawScope.drawToolIcon(tool: Tool, iconSize: Float) {
    val w = iconSize
    when (tool) {
        Tool.TWEEZERS -> {
            drawLine(Steel, Offset(w * 0.3f, w * 0.15f), Offset(w * 0.46f, w * 0.85f), strokeWidth = w * 0.09f, cap = StrokeCap.Round)
            drawLine(Steel, Offset(w * 0.7f, w * 0.15f), Offset(w * 0.54f, w * 0.85f), strokeWidth = w * 0.09f, cap = StrokeCap.Round)
        }
        Tool.NEEDLE -> {
            drawLine(Steel, Offset(w * 0.2f, w * 0.8f), Offset(w * 0.8f, w * 0.2f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
            drawLine(Thread, Offset(w * 0.74f, w * 0.26f), Offset(w * 0.86f, w * 0.62f), strokeWidth = w * 0.04f, cap = StrokeCap.Round)
        }
        Tool.SPONGE -> {
            drawRoundRect(SpongeYellow, topLeft = Offset(w * 0.15f, w * 0.28f), size = Size(w * 0.7f, w * 0.44f), cornerRadius = CornerRadius(w * 0.12f))
            drawCircle(SpongeHole, radius = w * 0.05f, center = Offset(w * 0.32f, w * 0.42f))
            drawCircle(SpongeHole, radius = w * 0.04f, center = Offset(w * 0.55f, w * 0.56f))
            drawCircle(SpongeHole, radius = w * 0.05f, center = Offset(w * 0.7f, w * 0.4f))
        }
        Tool.SPRAY -> {
            drawRoundRect(SprayBlue, topLeft = Offset(w * 0.32f, w * 0.35f), size = Size(w * 0.36f, w * 0.55f), cornerRadius = CornerRadius(w * 0.08f))
            drawRect(SprayCap, topLeft = Offset(w * 0.42f, w * 0.18f), size = Size(w * 0.16f, w * 0.17f))
            drawCircle(SprayBlue.copy(alpha = 0.6f), radius = w * 0.04f, center = Offset(w * 0.72f, w * 0.18f))
            drawCircle(SprayBlue.copy(alpha = 0.4f), radius = w * 0.035f, center = Offset(w * 0.84f, w * 0.27f))
        }
        Tool.BANDAGE -> rotate(degrees = -30f, pivot = Offset(w / 2f, w / 2f)) {
            drawRoundRect(BandageBeige, topLeft = Offset(w * 0.08f, w * 0.36f), size = Size(w * 0.84f, w * 0.28f), cornerRadius = CornerRadius(w * 0.12f))
            drawRoundRect(BandagePad, topLeft = Offset(w * 0.38f, w * 0.39f), size = Size(w * 0.24f, w * 0.22f), cornerRadius = CornerRadius(w * 0.05f))
        }
    }
}
