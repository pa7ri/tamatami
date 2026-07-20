package com.mobile.tamatami.ui.tamagotchi

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.mobile.tamatami.domain.model.TamagotchiMood
import kotlin.math.roundToInt

/**
 * Pixel-art cat renderer. The cat is drawn on a virtual [GRID]×[GRID] cell
 * grid mapped onto the canvas, each cell a crisp [DrawScope.drawRect] "pixel".
 *
 * The fur is a single warm-tabby palette regardless of mood — mood is conveyed
 * entirely through **expression** ([CatExpression]: eyes, mouth, ears, blush,
 * tail, sparkle), which is a set of continuous 0..1 params so it can be
 * interpolated smoothly as the mood changes (see [TamagotchiAvatar]).
 */

internal const val GRID = 32

// -- Tabby palette (mood-independent): grey cat with dark-brown markings -----
private val Outline = Color(0xFF2A211C)   // near-black brown outline
private val FurBase = Color(0xFF8D8A93)   // medium cool grey
private val FurDark = Color(0xFF4A3B33)   // dark brown stripes / shading
private val FurLight = Color(0xFFCFCDD4)  // light grey belly/highlight
private val Muzzle = Color(0xFFEDEBEF)    // near-white grey muzzle + paws
private val InnerEar = Color(0xFFE39FA8)  // dusty pink
private val NosePink = Color(0xFFD98A94)
private val EyeColor = Color(0xFF20303A)  // deep teal-black, high contrast on grey
private val Blush = Color(0xFFF3899E)
private val Sparkle = Color(0xFFFFF6C8)

/**
 * A cat facial/pose expression as continuous params so moods can be blended.
 * All fields are 0f..1f except [mouthCurve] which is -1f (frown) .. +1f (smile).
 */
internal data class CatExpression(
    val eyeOpen: Float,
    val mouthCurve: Float,
    val earPerk: Float,
    val cheekBlush: Float,
    val tailWag: Float,
    val sparkle: Float,
)

internal fun TamagotchiMood.expression(): CatExpression = when (this) {
    TamagotchiMood.SAD -> CatExpression(
        eyeOpen = 0.35f, mouthCurve = -1f, earPerk = 0.0f,
        cheekBlush = 0f, tailWag = 0.05f, sparkle = 0f,
    )
    TamagotchiMood.NEUTRAL -> CatExpression(
        eyeOpen = 0.8f, mouthCurve = 0.0f, earPerk = 0.4f,
        cheekBlush = 0f, tailWag = 0.25f, sparkle = 0f,
    )
    TamagotchiMood.CONTENT -> CatExpression(
        eyeOpen = 1f, mouthCurve = 0.55f, earPerk = 0.7f,
        cheekBlush = 0.5f, tailWag = 0.5f, sparkle = 0f,
    )
    TamagotchiMood.HAPPY -> CatExpression(
        eyeOpen = 1f, mouthCurve = 0.95f, earPerk = 0.95f,
        cheekBlush = 0.85f, tailWag = 0.85f, sparkle = 0.35f,
    )
    TamagotchiMood.GLOWING -> CatExpression(
        eyeOpen = 1f, mouthCurve = 1f, earPerk = 1f,
        cheekBlush = 1f, tailWag = 1f, sparkle = 1f,
    )
}

/**
 * Draw the whole cat. [tailPhase] (-1f..1f) drives the tail sway; [blink]
 * (1f open .. 0f closed) modulates eye height.
 */
internal fun DrawScope.drawPixelCat(
    canvasSize: Size,
    expr: CatExpression,
    blink: Float,
    tailPhase: Float,
) {
    // Fit a square grid centered in the canvas.
    val dim = minOf(canvasSize.width, canvasSize.height)
    val cell = dim / GRID
    val origin = Offset(
        x = (canvasSize.width - cell * GRID) / 2f,
        y = (canvasSize.height - cell * GRID) / 2f,
    )

    // Tail: sways horizontally with tailPhase; drawn behind the body.
    drawTail(cell, origin, tailPhase)

    // Ears perk up: earPerk 1 -> ears sit higher and more upright.
    val earLift = (expr.earPerk * 2f).roundToInt() // 0..2 rows up
    drawEars(cell, origin, earLift)

    drawHeadAndBody(cell, origin)

    // Muzzle + nose (kept low so the eyes have a clear band above it).
    fillRect(11, 18, 10, 5, Muzzle, cell, origin)
    px(15, 19, NosePink, cell, origin, w = 2, h = 2)

    drawWhiskers(cell, origin)
    drawEyes(cell, origin, expr.eyeOpen * blink, expr.mouthCurve)
    drawMouth(cell, origin, expr.mouthCurve)

    if (expr.cheekBlush > 0f) drawBlush(cell, origin, expr.cheekBlush)
    if (expr.sparkle > 0f) drawSparkles(cell, origin, expr.sparkle)
}

// -- Body parts --------------------------------------------------------------

private fun DrawScope.drawEars(cell: Float, origin: Offset, lift: Int) {
    // Ears sit on the top corners of the head (head top = row 6). `lift` (0..2)
    // raises them and makes them a touch taller/perkier for happy moods.
    val baseY = 6 - lift
    // Left ear: a stepped triangle, wide base (cols 8..11) tapering up.
    fillRect(8, baseY, 4, 1, FurBase, cell, origin)       // base row
    fillRect(8, baseY - 1, 3, 1, FurBase, cell, origin)
    fillRect(8, baseY - 2, 2, 1, FurBase, cell, origin)
    fillRect(8, baseY - 3, 1, 1, FurBase, cell, origin)   // tip
    px(9, baseY, InnerEar, cell, origin, w = 2, h = 1)    // pink inner
    px(9, baseY - 1, InnerEar, cell, origin)

    // Right ear: mirror (wide base cols 20..23).
    fillRect(20, baseY, 4, 1, FurBase, cell, origin)
    fillRect(21, baseY - 1, 3, 1, FurBase, cell, origin)
    fillRect(22, baseY - 2, 2, 1, FurBase, cell, origin)
    fillRect(23, baseY - 3, 1, 1, FurBase, cell, origin)  // tip
    px(21, baseY, InnerEar, cell, origin, w = 2, h = 1)
    px(21, baseY - 1, InnerEar, cell, origin)
}

private fun DrawScope.drawHeadAndBody(cell: Float, origin: Offset) {
    // Head: big rounded block. Corners are left unpainted (see corner rects
    // below) to fake rounding.
    fillRect(9, 6, 14, 16, FurBase, cell, origin)
    fillRect(8, 8, 16, 12, FurBase, cell, origin) // wider middle band -> rounded silhouette
    // Ear bases blend into head.
    fillRect(8, 5, 3, 2, FurBase, cell, origin)
    fillRect(21, 5, 3, 2, FurBase, cell, origin)

    // Tabby forehead stripes — bold dark-brown "M" marking.
    px(15, 6, FurDark, cell, origin, w = 2, h = 4)
    px(12, 6, FurDark, cell, origin, w = 1, h = 3)
    px(19, 6, FurDark, cell, origin, w = 1, h = 3)
    px(13, 6, FurDark, cell, origin, w = 1, h = 1)
    px(18, 6, FurDark, cell, origin, w = 1, h = 1)

    // Cheek / side stripes.
    fillRect(8, 15, 2, 5, FurDark, cell, origin)
    fillRect(22, 15, 2, 5, FurDark, cell, origin)

    // Body: seated, narrower than head, cream chest.
    fillRect(11, 21, 10, 8, FurBase, cell, origin)
    fillRect(10, 22, 12, 6, FurBase, cell, origin) // rounded sides
    fillRect(13, 22, 6, 6, FurLight, cell, origin) // chest

    // Front paws.
    fillRect(11, 27, 3, 2, Muzzle, cell, origin)
    fillRect(18, 27, 3, 2, Muzzle, cell, origin)
}

private fun DrawScope.drawTail(cell: Float, origin: Offset, phase: Float) {
    // Base at body's right; tip sways by up to 3 cells with phase.
    val sway = (phase * 3f).roundToInt()
    fillRect(21, 24, 3, 2, FurBase, cell, origin)
    fillRect(24, 23, 2, 2, FurBase, cell, origin)
    fillRect(25 + sway, 20, 2, 4, FurBase, cell, origin)
    fillRect(25 + sway, 19, 2, 1, FurDark, cell, origin) // striped tip
}

private fun DrawScope.drawEyes(cell: Float, origin: Offset, open: Float, curve: Float) {
    // Eyes occupy the band rows 13..16, cols 11..13 (left) and 18..20 (right),
    // clear of the muzzle. Their *shape* changes with mood for legibility:
    //   sad  -> low, downcast half-lidded slits
    //   neutral/up -> tall round eyes with catchlights (bigger the happier).
    val leftX = 11
    val rightX = 18

    if (curve < -0.33f) {
        // Sad: a heavy upper lid (dark bar) with just a sliver of eye beneath,
        // sitting low -> clearly downcast.
        fillRect(leftX, 15, 3, 1, EyeColor, cell, origin)
        fillRect(leftX, 16, 3, 1, EyeColor, cell, origin)
        fillRect(rightX, 15, 3, 1, EyeColor, cell, origin)
        fillRect(rightX, 16, 3, 1, EyeColor, cell, origin)
    } else {
        // Openness drives height (blink squashes it). Happier -> taller.
        val h = (open * 4f).coerceIn(1f, 4f).roundToInt()
        val top = 16 - h // bottom-aligned at row 16
        fillRect(leftX, top, 3, h, Muzzle, cell, origin)
        fillRect(rightX, top, 3, h, Muzzle, cell, origin)
        // Pupils fill most of the eye; a catchlight pixel top-left.
        fillRect(leftX, top, 2, h, EyeColor, cell, origin)
        fillRect(rightX + 1, top, 2, h, EyeColor, cell, origin)
        if (h >= 3) {
            px(leftX + 1, top, Muzzle, cell, origin)
            px(rightX + 2, top, Muzzle, cell, origin)
        }
    }

    drawBrows(cell, origin, curve)
}

/**
 * Eyebrows are the strongest mood cue. [curve] < 0 (sad) angles them into a
 * worried "\  /"; [curve] > 0 (happy) lifts flat brows clear of the eyes; near
 * 0 draws a subtle neutral brow. Sit at rows 11-12, above the eyes.
 */
private fun DrawScope.drawBrows(cell: Float, origin: Offset, curve: Float) {
    when {
        curve < -0.33f -> {
            // Worried: inner ends pulled up-and-in ( ⌐ ¬ ).
            px(11, 12, EyeColor, cell, origin)
            px(12, 11, EyeColor, cell, origin)
            px(13, 11, EyeColor, cell, origin)
            px(18, 11, EyeColor, cell, origin)
            px(19, 11, EyeColor, cell, origin)
            px(20, 12, EyeColor, cell, origin)
        }
        curve > 0.33f -> {
            // Cheerful: raised flat brows, higher for big smiles.
            val y = if (curve > 0.75f) 10 else 11
            px(11, y, EyeColor, cell, origin, w = 3, h = 1)
            px(18, y, EyeColor, cell, origin, w = 3, h = 1)
        }
        else -> {
            // Neutral: short brows just above the eyes.
            px(11, 11, EyeColor, cell, origin, w = 2, h = 1)
            px(19, 11, EyeColor, cell, origin, w = 2, h = 1)
        }
    }
}

private fun DrawScope.drawMouth(cell: Float, origin: Offset, curve: Float) {
    // On the muzzle, just under the nose (nose rows 19-20). Bold shape by mood.
    val cx = 16
    val topY = 22
    when {
        curve > 0.33f -> {
            // Big smile — wide upturned mouth, open for glee on strong smiles.
            px(cx - 3, topY - 1, EyeColor, cell, origin)
            px(cx - 2, topY, EyeColor, cell, origin)
            fillRect(cx - 1, topY, 3, 1, EyeColor, cell, origin)
            px(cx + 2, topY, EyeColor, cell, origin)
            px(cx + 3, topY - 1, EyeColor, cell, origin)
            if (curve > 0.75f) {
                px(cx - 1, topY + 1, NosePink, cell, origin, w = 3, h = 1) // tongue
            }
        }
        curve < -0.33f -> {
            // Deep frown — pronounced downturned mouth.
            px(cx - 3, topY, EyeColor, cell, origin)
            px(cx - 2, topY - 1, EyeColor, cell, origin)
            fillRect(cx - 1, topY - 1, 3, 1, EyeColor, cell, origin)
            px(cx + 2, topY - 1, EyeColor, cell, origin)
            px(cx + 3, topY, EyeColor, cell, origin)
        }
        else -> {
            // Neutral — short flat line.
            px(cx - 2, topY, EyeColor, cell, origin, w = 5, h = 1)
        }
    }
}

private fun DrawScope.drawWhiskers(cell: Float, origin: Offset) {
    px(6, 18, Outline, cell, origin, w = 4, h = 1)
    px(6, 20, Outline, cell, origin, w = 3, h = 1)
    px(22, 18, Outline, cell, origin, w = 4, h = 1)
    px(23, 20, Outline, cell, origin, w = 3, h = 1)
}

private fun DrawScope.drawBlush(cell: Float, origin: Offset, strength: Float) {
    val c = Blush.copy(alpha = 0.75f * strength)
    fillRect(9, 17, 3, 2, c, cell, origin)
    fillRect(20, 17, 3, 2, c, cell, origin)
}

private fun DrawScope.drawSparkles(cell: Float, origin: Offset, strength: Float) {
    val c = Sparkle.copy(alpha = strength)
    // Little plus-shaped twinkles around the head.
    sparkle(4, 8, c, cell, origin)
    sparkle(27, 10, c, cell, origin)
    sparkle(25, 5, c, cell, origin)
}

// -- Low-level pixel helpers -------------------------------------------------

/** Fill one or more grid cells starting at (gx, gy). */
private fun DrawScope.px(
    gx: Int, gy: Int, color: Color, cell: Float, origin: Offset, w: Int = 1, h: Int = 1,
) {
    drawRect(
        color = color,
        topLeft = Offset(origin.x + gx * cell, origin.y + gy * cell),
        size = Size(cell * w, cell * h),
    )
}

private fun DrawScope.fillRect(
    gx: Int, gy: Int, w: Int, h: Int, color: Color, cell: Float, origin: Offset,
) = px(gx, gy, color, cell, origin, w, h)

private fun DrawScope.sparkle(gx: Int, gy: Int, color: Color, cell: Float, origin: Offset) {
    px(gx, gy, color, cell, origin)
    px(gx - 1, gy, color, cell, origin)
    px(gx + 1, gy, color, cell, origin)
    px(gx, gy - 1, color, cell, origin)
    px(gx, gy + 1, color, cell, origin)
}

/** Soft mood-tinted aura behind the cat. [intensity] scales alpha. */
internal fun DrawScope.drawAura(canvasSize: Size, color: Color, intensity: Float) {
    if (intensity <= 0f) return
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val radius = minOf(canvasSize.width, canvasSize.height) * 0.58f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.45f * intensity),
                color.copy(alpha = 0f),
            ),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
