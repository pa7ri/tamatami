package com.mobile.tamatami.ui.tamagotchi

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mobile.tamatami.domain.model.TamagotchiMood

/**
 * Internal drawing primitives for the Tamagotchi. Each function takes the
 * current canvas size and a mood and renders one anatomical feature.
 */

internal fun DrawScope.drawTamaBody(
    bodyColor: Color,
    shadeColor: Color,
    canvasSize: Size,
) {
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val radius = minOf(canvasSize.width, canvasSize.height) * 0.42f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(bodyColor, shadeColor),
            center = center,
            radius = radius * 1.1f,
        ),
        radius = radius,
        center = center,
    )
    drawCircle(
        color = shadeColor.copy(alpha = 0.55f),
        radius = radius,
        center = center,
        style = Stroke(width = 6f),
    )
}

internal fun DrawScope.drawCheeks(canvasSize: Size, alpha: Float) {
    if (alpha <= 0f) return
    val w = canvasSize.width
    val h = canvasSize.height
    val r = minOf(w, h) * 0.08f
    drawCircle(
        color = Color(0xFFFF8AB0).copy(alpha = alpha),
        radius = r,
        center = Offset(w * 0.32f, h * 0.58f),
    )
    drawCircle(
        color = Color(0xFFFF8AB0).copy(alpha = alpha),
        radius = r,
        center = Offset(w * 0.68f, h * 0.58f),
    )
}

internal fun DrawScope.drawEyes(
    mood: TamagotchiMood,
    blink: Float,
    canvasSize: Size,
) {
    val w = canvasSize.width
    val h = canvasSize.height
    val eyeY = h * 0.46f
    val eyeOffsetX = w * 0.16f
    val baseR = minOf(w, h) * 0.055f
    val eyeColor = Color(0xFF1B1024)

    val left = Offset(w / 2f - eyeOffsetX, eyeY)
    val right = Offset(w / 2f + eyeOffsetX, eyeY)

    when (mood) {
        TamagotchiMood.SAD -> {
            // Downward arc eyes ( ︶ flipped )
            drawSadEye(left, baseR, eyeColor, blink)
            drawSadEye(right, baseR, eyeColor, blink)
        }
        TamagotchiMood.NEUTRAL,
        TamagotchiMood.CONTENT -> {
            drawCircle(
                color = eyeColor,
                radius = baseR,
                center = left.copy(y = left.y - (baseR * (1f - blink)) / 2f),
            )
            drawCircle(
                color = eyeColor,
                radius = baseR,
                center = right.copy(y = right.y - (baseR * (1f - blink)) / 2f),
            )
        }
        TamagotchiMood.HAPPY,
        TamagotchiMood.GLOWING -> {
            // ^_^ arc eyes
            drawHappyEye(left, baseR, eyeColor, blink)
            drawHappyEye(right, baseR, eyeColor, blink)
        }
    }
}

private fun DrawScope.drawSadEye(center: Offset, r: Float, color: Color, blink: Float) {
    val path = Path().apply {
        moveTo(center.x - r, center.y + r * 0.2f)
        quadraticTo(center.x, center.y + r * 1.1f * blink, center.x + r, center.y + r * 0.2f)
    }
    drawPath(path, color = color, style = Stroke(width = 6f))
}

private fun DrawScope.drawHappyEye(center: Offset, r: Float, color: Color, blink: Float) {
    val path = Path().apply {
        moveTo(center.x - r, center.y + r * 0.2f)
        quadraticTo(center.x, center.y - r * 1.1f * blink, center.x + r, center.y + r * 0.2f)
    }
    drawPath(path, color = color, style = Stroke(width = 6f))
}

internal fun DrawScope.drawMouth(mood: TamagotchiMood, canvasSize: Size) {
    val w = canvasSize.width
    val h = canvasSize.height
    val cx = w / 2f
    val cy = h * 0.6f
    val r = minOf(w, h) * 0.08f
    val color = Color(0xFF1B1024)

    when (mood) {
        TamagotchiMood.SAD -> {
            val path = Path().apply {
                moveTo(cx - r, cy + r * 0.6f)
                quadraticTo(cx, cy - r * 0.4f, cx + r, cy + r * 0.6f)
            }
            drawPath(path, color = color, style = Stroke(width = 6f))
        }
        TamagotchiMood.NEUTRAL -> {
            drawLine(
                color = color,
                start = Offset(cx - r * 0.7f, cy),
                end = Offset(cx + r * 0.7f, cy),
                strokeWidth = 6f,
            )
        }
        TamagotchiMood.CONTENT,
        TamagotchiMood.HAPPY -> {
            val path = Path().apply {
                moveTo(cx - r, cy)
                quadraticTo(cx, cy + r, cx + r, cy)
            }
            drawPath(path, color = color, style = Stroke(width = 6f))
        }
        TamagotchiMood.GLOWING -> {
            // open "o" mouth
            drawCircle(color = color, radius = r * 0.55f, center = Offset(cx, cy + r * 0.15f), style = Stroke(width = 5f))
        }
    }
}

internal fun DrawScope.drawGlow(canvasSize: Size, glowColor: Color) {
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val radius = minOf(canvasSize.width, canvasSize.height) * 0.55f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = 0.55f), glowColor.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
