package com.example.ui.avatar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BunyWardrobeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun BunyAvatarView(
    isSpeaking: Boolean,
    emotion: String,
    gesture: String,
    wardrobe: BunyWardrobeState,
    onAvatarTapped: (touchType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // 1. Natural blinking loop
    var isBlinkClosed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2500L..4200L).random())
            isBlinkClosed = true
            delay(160)
            isBlinkClosed = false
        }
    }

    // 2. Idle breathing & floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "BunyIdleLoop")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

    // 3. Ear subtle idle twitch
    val earTwitch by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EarTwitch"
    )

    // 4. Lip-sync mouth oscillation when speaking
    val mouthOscillation by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(130, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MouthOscillation"
    )

    // 5. Hand wave motion
    val handWaveRotation by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HandWave"
    )

    // 6. Interactive Tap Bounce & Blush Animation
    val tapScale = remember { Animatable(1f) }
    var blushIntensity by remember { mutableFloatStateOf(0.4f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
            .testTag("buny_avatar_container")
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    coroutineScope.launch {
                        tapScale.animateTo(1.08f, tween(100))
                        tapScale.animateTo(0.96f, tween(100))
                        tapScale.animateTo(1f, tween(120))
                    }
                    if (offset.y < size.height * 0.45f) {
                        blushIntensity = 1.0f
                        onAvatarTapped("HEAD")
                    } else {
                        onAvatarTapped("BELLY")
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("buny_avatar_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = (canvasH / 2f) + floatY + 15f

            val effectiveScale = tapScale.value

            // 1. Draw Aura / Glow
            drawAura(centerX, centerY, wardrobe.auraId)

            // 2. Draw Ears (Behind head)
            drawBunyEars(centerX, centerY, earTwitch, wardrobe)

            // 3. Draw Body & Outfits
            drawBunyBody(centerX, centerY, wardrobe)

            // 4. Draw Neckwear / Accessories
            drawNeckAccessories(centerX, centerY, wardrobe)

            // 5. Draw Hands & Gestures
            drawBunyHands(centerX, centerY, gesture, handWaveRotation)

            // 6. Draw Head
            drawBunyHead(centerX, centerY, effectiveScale)

            // 7. Draw Cheeks (Blushing)
            drawCheeks(centerX, centerY, blushIntensity, emotion)

            // 8. Draw Eyes (Expressive & Blinking)
            drawBunyEyes(centerX, centerY, emotion, isBlinkClosed)

            // 9. Draw Nose & Mouth (Lip-sync animated!)
            drawBunyNoseAndMouth(centerX, centerY, isSpeaking, mouthOscillation, emotion)

            // 10. Draw Glasses / Visor
            drawBunyGlasses(centerX, centerY, wardrobe.glassesId)

            // 11. Draw Headwear / Hats
            drawBunyHeadwear(centerX, centerY, wardrobe.headwearId)
        }
    }
}

private fun DrawScope.drawAura(cx: Float, cy: Float, auraId: String) {
    val auraColor = when (auraId) {
        "aura_cyan" -> Color(0xFF00E5FF)
        "aura_gold" -> Color(0xFFFFB703)
        "aura_pink" -> Color(0xFFFF2A85)
        else -> Color(0xFFA855F7) // Violet
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                auraColor.copy(alpha = 0.28f),
                auraColor.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = 160.dp.toPx()
        ),
        radius = 160.dp.toPx(),
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawBunyEars(cx: Float, cy: Float, earTwitch: Float, wardrobe: BunyWardrobeState) {
    val earOuterColor = Color(0xFFF1F5F9)
    val earInnerColor = when (wardrobe.auraId) {
        "aura_cyan" -> Color(0xFF38BDF8)
        "aura_gold" -> Color(0xFFFBBF24)
        "aura_pink" -> Color(0xFFFF4081)
        else -> Color(0xFFFF6584)
    }

    // Left Ear
    rotate(degrees = -14f + earTwitch, pivot = Offset(cx - 45.dp.toPx(), cy - 40.dp.toPx())) {
        val leftEarPath = Path().apply {
            moveTo(cx - 55.dp.toPx(), cy - 35.dp.toPx())
            cubicTo(
                cx - 85.dp.toPx(), cy - 90.dp.toPx(),
                cx - 65.dp.toPx(), cy - 145.dp.toPx(),
                cx - 35.dp.toPx(), cy - 140.dp.toPx()
            )
            cubicTo(
                cx - 15.dp.toPx(), cy - 135.dp.toPx(),
                cx - 25.dp.toPx(), cy - 80.dp.toPx(),
                cx - 35.dp.toPx(), cy - 35.dp.toPx()
            )
            close()
        }
        drawPath(leftEarPath, earOuterColor)

        // Inner Left Ear
        val innerLeftEar = Path().apply {
            moveTo(cx - 50.dp.toPx(), cy - 42.dp.toPx())
            cubicTo(
                cx - 72.dp.toPx(), cy - 88.dp.toPx(),
                cx - 58.dp.toPx(), cy - 130.dp.toPx(),
                cx - 36.dp.toPx(), cy - 128.dp.toPx()
            )
            cubicTo(
                cx - 22.dp.toPx(), cy - 125.dp.toPx(),
                cx - 30.dp.toPx(), cy - 80.dp.toPx(),
                cx - 38.dp.toPx(), cy - 42.dp.toPx()
            )
            close()
        }
        drawPath(innerLeftEar, earInnerColor)
    }

    // Right Ear
    rotate(degrees = 14f - earTwitch, pivot = Offset(cx + 45.dp.toPx(), cy - 40.dp.toPx())) {
        val rightEarPath = Path().apply {
            moveTo(cx + 35.dp.toPx(), cy - 35.dp.toPx())
            cubicTo(
                cx + 25.dp.toPx(), cy - 80.dp.toPx(),
                cx + 15.dp.toPx(), cy - 135.dp.toPx(),
                cx + 35.dp.toPx(), cy - 140.dp.toPx()
            )
            cubicTo(
                cx + 65.dp.toPx(), cy - 145.dp.toPx(),
                cx + 85.dp.toPx(), cy - 90.dp.toPx(),
                cx + 55.dp.toPx(), cy - 35.dp.toPx()
            )
            close()
        }
        drawPath(rightEarPath, earOuterColor)

        // Inner Right Ear
        val innerRightEar = Path().apply {
            moveTo(cx + 38.dp.toPx(), cy - 42.dp.toPx())
            cubicTo(
                cx + 30.dp.toPx(), cy - 80.dp.toPx(),
                cx + 22.dp.toPx(), cy - 125.dp.toPx(),
                cx + 36.dp.toPx(), cy - 128.dp.toPx()
            )
            cubicTo(
                cx + 58.dp.toPx(), cy - 130.dp.toPx(),
                cx + 72.dp.toPx(), cy - 88.dp.toPx(),
                cx + 50.dp.toPx(), cy - 42.dp.toPx()
            )
            close()
        }
        drawPath(innerRightEar, earInnerColor)
    }
}

private fun DrawScope.drawBunyBody(cx: Float, cy: Float, wardrobe: BunyWardrobeState) {
    val bodyW = 100.dp.toPx()
    val bodyH = 80.dp.toPx()
    val bodyTop = cy + 30.dp.toPx()

    // Base body (white fur)
    drawRoundRect(
        color = Color(0xFFF8FAFC),
        topLeft = Offset(cx - bodyW / 2f, bodyTop),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(45.dp.toPx(), 45.dp.toPx())
    )

    // Wardrobe Outfits
    when (wardrobe.outfitId) {
        "outfit_cyber" -> {
            // Cyber jacket
            drawRoundRect(
                color = Color(0xFF1E1035),
                topLeft = Offset(cx - (bodyW + 6.dp.toPx()) / 2f, bodyTop + 2.dp.toPx()),
                size = Size(bodyW + 6.dp.toPx(), bodyH - 2.dp.toPx()),
                cornerRadius = CornerRadius(40.dp.toPx(), 40.dp.toPx())
            )
            // Glowing neon cyan jacket zipper & lines
            drawLine(
                color = Color(0xFF00E5FF),
                start = Offset(cx, bodyTop + 2.dp.toPx()),
                end = Offset(cx, bodyTop + bodyH - 2.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFF007F),
                start = Offset(cx - 30.dp.toPx(), bodyTop + 20.dp.toPx()),
                end = Offset(cx - 15.dp.toPx(), bodyTop + 60.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFF007F),
                start = Offset(cx + 30.dp.toPx(), bodyTop + 20.dp.toPx()),
                end = Offset(cx + 15.dp.toPx(), bodyTop + 60.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        "outfit_hoodie" -> {
            // Red street hoodie
            drawRoundRect(
                color = Color(0xFFDC2626),
                topLeft = Offset(cx - (bodyW + 8.dp.toPx()) / 2f, bodyTop + 2.dp.toPx()),
                size = Size(bodyW + 8.dp.toPx(), bodyH),
                cornerRadius = CornerRadius(40.dp.toPx(), 40.dp.toPx())
            )
            // Hoodie pocket
            drawRoundRect(
                color = Color(0xFFB91C1C),
                topLeft = Offset(cx - 32.dp.toPx(), bodyTop + 36.dp.toPx()),
                size = Size(64.dp.toPx(), 32.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            // White hoodie strings
            drawLine(Color.White, Offset(cx - 10.dp.toPx(), bodyTop + 8.dp.toPx()), Offset(cx - 10.dp.toPx(), bodyTop + 28.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)
            drawLine(Color.White, Offset(cx + 10.dp.toPx(), bodyTop + 8.dp.toPx()), Offset(cx + 10.dp.toPx(), bodyTop + 28.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)
        }
        "outfit_sherwani" -> {
            // Royal Kurta
            drawRoundRect(
                color = Color(0xFF7C2D12),
                topLeft = Offset(cx - bodyW / 2f, bodyTop + 2.dp.toPx()),
                size = Size(bodyW, bodyH + 8.dp.toPx()),
                cornerRadius = CornerRadius(35.dp.toPx(), 35.dp.toPx())
            )
            // Gold band in middle
            drawLine(
                color = Color(0xFFFFD700),
                start = Offset(cx, bodyTop + 4.dp.toPx()),
                end = Offset(cx, bodyTop + bodyH + 6.dp.toPx()),
                strokeWidth = 4.dp.toPx()
            )
            // Gold buttons
            for (i in 0..3) {
                drawCircle(Color(0xFFFFD700), 2.5.dp.toPx(), Offset(cx, bodyTop + 14.dp.toPx() + (i * 12.dp.toPx())))
            }
        }
        "outfit_suit" -> {
            // Black suit jacket
            drawRoundRect(
                color = Color(0xFF09090B),
                topLeft = Offset(cx - bodyW / 2f, bodyTop + 2.dp.toPx()),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(35.dp.toPx(), 35.dp.toPx())
            )
            // White shirt V-shape
            val shirtPath = Path().apply {
                moveTo(cx - 18.dp.toPx(), bodyTop + 2.dp.toPx())
                lineTo(cx, bodyTop + 38.dp.toPx())
                lineTo(cx + 18.dp.toPx(), bodyTop + 2.dp.toPx())
                close()
            }
            drawPath(shirtPath, Color.White)
            // Red tie
            val tiePath = Path().apply {
                moveTo(cx - 4.dp.toPx(), bodyTop + 8.dp.toPx())
                lineTo(cx + 4.dp.toPx(), bodyTop + 8.dp.toPx())
                lineTo(cx + 6.dp.toPx(), bodyTop + 48.dp.toPx())
                lineTo(cx, bodyTop + 54.dp.toPx())
                lineTo(cx - 6.dp.toPx(), bodyTop + 48.dp.toPx())
                close()
            }
            drawPath(tiePath, Color(0xFFDC2626))
        }
        "outfit_gamer" -> {
            // Gamer Jersey
            drawRoundRect(
                color = Color(0xFF059669),
                topLeft = Offset(cx - bodyW / 2f, bodyTop + 2.dp.toPx()),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(40.dp.toPx(), 40.dp.toPx())
            )
            // Black & Neon stripes
            drawLine(Color.Black, Offset(cx - 40.dp.toPx(), bodyTop + 30.dp.toPx()), Offset(cx + 40.dp.toPx(), bodyTop + 30.dp.toPx()), 4.dp.toPx())
            drawCircle(Color(0xFF00E5FF), 8.dp.toPx(), Offset(cx, bodyTop + 45.dp.toPx()))
        }
    }
}

private fun DrawScope.drawNeckAccessories(cx: Float, cy: Float, wardrobe: BunyWardrobeState) {
    val neckY = cy + 32.dp.toPx()
    when (wardrobe.accessoryId) {
        "acc_gold_chain" -> {
            // Heavy gold chain with dollar pendant
            drawArc(
                color = Color(0xFFFFD700),
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx - 24.dp.toPx(), neckY - 8.dp.toPx()),
                size = Size(48.dp.toPx(), 26.dp.toPx()),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
            // Dollar pendant
            drawCircle(Color(0xFFFFD700), 7.dp.toPx(), Offset(cx, neckY + 20.dp.toPx()))
            drawCircle(Color(0xFF0F081D), 5.dp.toPx(), Offset(cx, neckY + 20.dp.toPx()))
        }
        "acc_bowtie" -> {
            val bowLeft = Path().apply {
                moveTo(cx, neckY)
                lineTo(cx - 14.dp.toPx(), neckY - 7.dp.toPx())
                lineTo(cx - 14.dp.toPx(), neckY + 7.dp.toPx())
                close()
            }
            val bowRight = Path().apply {
                moveTo(cx, neckY)
                lineTo(cx + 14.dp.toPx(), neckY - 7.dp.toPx())
                lineTo(cx + 14.dp.toPx(), neckY + 7.dp.toPx())
                close()
            }
            drawPath(bowLeft, Color(0xFFDC2626))
            drawPath(bowRight, Color(0xFFDC2626))
            drawCircle(Color(0xFF991B1B), 4.dp.toPx(), Offset(cx, neckY))
        }
    }
}

private fun DrawScope.drawBunyHands(cx: Float, cy: Float, gesture: String, waveRot: Float) {
    val pawColor = Color(0xFFF1F5F9)
    val pawPink = Color(0xFFFFB2D6)
    val bodyCenterY = cy + 45.dp.toPx()

    // Left Hand (Normal resting or cheer)
    if (gesture == "CHEER_ARMS") {
        rotate(-45f, Offset(cx - 50.dp.toPx(), bodyCenterY)) {
            drawCircle(pawColor, 14.dp.toPx(), Offset(cx - 65.dp.toPx(), bodyCenterY - 25.dp.toPx()))
            drawCircle(pawPink, 6.dp.toPx(), Offset(cx - 65.dp.toPx(), bodyCenterY - 25.dp.toPx()))
        }
    } else {
        drawCircle(pawColor, 13.dp.toPx(), Offset(cx - 52.dp.toPx(), bodyCenterY + 12.dp.toPx()))
        drawCircle(pawPink, 5.5.dp.toPx(), Offset(cx - 52.dp.toPx(), bodyCenterY + 12.dp.toPx()))
    }

    // Right Hand (Interactive gestures: Wave, Thumbs Up, Thinking, Cheer)
    when (gesture) {
        "WAVE" -> {
            rotate(waveRot, pivot = Offset(cx + 50.dp.toPx(), bodyCenterY - 10.dp.toPx())) {
                drawRoundRect(
                    color = pawColor,
                    topLeft = Offset(cx + 52.dp.toPx(), bodyCenterY - 45.dp.toPx()),
                    size = Size(18.dp.toPx(), 26.dp.toPx()),
                    cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
                )
                // Cute paw pad
                drawCircle(pawPink, 6.dp.toPx(), Offset(cx + 61.dp.toPx(), bodyCenterY - 33.dp.toPx()))
            }
        }
        "THUMBS_UP" -> {
            // Paw with raised thumb
            drawCircle(pawColor, 14.dp.toPx(), Offset(cx + 56.dp.toPx(), bodyCenterY - 10.dp.toPx()))
            drawRoundRect(
                color = pawColor,
                topLeft = Offset(cx + 52.dp.toPx(), bodyCenterY - 28.dp.toPx()),
                size = Size(8.dp.toPx(), 16.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawCircle(pawPink, 6.dp.toPx(), Offset(cx + 56.dp.toPx(), bodyCenterY - 10.dp.toPx()))
        }
        "THINKING_CHIN" -> {
            // Hand tapping chin
            drawCircle(pawColor, 13.dp.toPx(), Offset(cx + 25.dp.toPx(), cy + 22.dp.toPx()))
            drawCircle(pawPink, 5.dp.toPx(), Offset(cx + 25.dp.toPx(), cy + 22.dp.toPx()))
        }
        "CHEER_ARMS" -> {
            rotate(45f, Offset(cx + 50.dp.toPx(), bodyCenterY)) {
                drawCircle(pawColor, 14.dp.toPx(), Offset(cx + 65.dp.toPx(), bodyCenterY - 25.dp.toPx()))
                drawCircle(pawPink, 6.dp.toPx(), Offset(cx + 65.dp.toPx(), bodyCenterY - 25.dp.toPx()))
            }
        }
        else -> {
            // Idle right hand
            drawCircle(pawColor, 13.dp.toPx(), Offset(cx + 52.dp.toPx(), bodyCenterY + 12.dp.toPx()))
            drawCircle(pawPink, 5.5.dp.toPx(), Offset(cx + 52.dp.toPx(), bodyCenterY + 12.dp.toPx()))
        }
    }
}

private fun DrawScope.drawBunyHead(cx: Float, cy: Float, scale: Float) {
    val headRadiusX = 64.dp.toPx() * scale
    val headRadiusY = 56.dp.toPx() * scale

    // Head base shadow/layer
    drawOval(
        color = Color(0xFFF1F5F9),
        topLeft = Offset(cx - headRadiusX, cy - headRadiusY),
        size = Size(headRadiusX * 2, headRadiusY * 2)
    )

    // Subtle 3D gradient highlight on head
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFF8FAFC), Color(0xFFE2E8F0)),
            center = Offset(cx - 15.dp.toPx(), cy - 18.dp.toPx()),
            radius = headRadiusX * 1.3f
        ),
        topLeft = Offset(cx - headRadiusX, cy - headRadiusY),
        size = Size(headRadiusX * 2, headRadiusY * 2)
    )

    // Cute lower cheek roundness
    drawOval(
        color = Color.White,
        topLeft = Offset(cx - 40.dp.toPx(), cy + 2.dp.toPx()),
        size = Size(80.dp.toPx(), 36.dp.toPx())
    )
}

private fun DrawScope.drawCheeks(cx: Float, cy: Float, blush: Float, emotion: String) {
    val blushAlpha = if (emotion == "HEART_EYES" || emotion == "HAPPY") 0.85f else blush.coerceIn(0.2f, 0.8f)
    val blushColor = Color(0xFFFF4081).copy(alpha = blushAlpha)

    // Left cheek
    drawOval(
        color = blushColor,
        topLeft = Offset(cx - 52.dp.toPx(), cy + 8.dp.toPx()),
        size = Size(18.dp.toPx(), 11.dp.toPx())
    )
    // Right cheek
    drawOval(
        color = blushColor,
        topLeft = Offset(cx + 34.dp.toPx(), cy + 8.dp.toPx()),
        size = Size(18.dp.toPx(), 11.dp.toPx())
    )
}

private fun DrawScope.drawBunyEyes(cx: Float, cy: Float, emotion: String, isBlinkClosed: Boolean) {
    val eyeSpacing = 28.dp.toPx()
    val eyeY = cy - 8.dp.toPx()

    if (isBlinkClosed) {
        // Natural blink: closed curved lines
        drawArc(
            color = Color(0xFF0F172A),
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(cx - eyeSpacing - 12.dp.toPx(), eyeY),
            size = Size(24.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF0F172A),
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(cx + eyeSpacing - 12.dp.toPx(), eyeY),
            size = Size(24.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        return
    }

    when (emotion) {
        "HAPPY" -> {
            // Happy crescent eyes
            drawArc(
                color = Color(0xFF0F172A),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - eyeSpacing - 13.dp.toPx(), eyeY - 8.dp.toPx()),
                size = Size(26.dp.toPx(), 20.dp.toPx()),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF0F172A),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx + eyeSpacing - 13.dp.toPx(), eyeY - 8.dp.toPx()),
                size = Size(26.dp.toPx(), 20.dp.toPx()),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        "WINK" -> {
            // Left eye normal open
            drawSingleEye(cx - eyeSpacing, eyeY)
            // Right eye wink closed line
            drawArc(
                color = Color(0xFF0F172A),
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx + eyeSpacing - 13.dp.toPx(), eyeY),
                size = Size(26.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        "HEART_EYES" -> {
            // Pulsing hearts
            drawHeart(cx - eyeSpacing, eyeY, 14.dp.toPx(), Color(0xFFFF2A85))
            drawHeart(cx + eyeSpacing, eyeY, 14.dp.toPx(), Color(0xFFFF2A85))
        }
        "SURPRISED" -> {
            // Big round wide eyes
            drawCircle(Color(0xFF0F172A), 15.dp.toPx(), Offset(cx - eyeSpacing, eyeY))
            drawCircle(Color.White, 6.dp.toPx(), Offset(cx - eyeSpacing - 4.dp.toPx(), eyeY - 5.dp.toPx()))
            drawCircle(Color(0xFF0F172A), 15.dp.toPx(), Offset(cx + eyeSpacing, eyeY))
            drawCircle(Color.White, 6.dp.toPx(), Offset(cx + eyeSpacing - 4.dp.toPx(), eyeY - 5.dp.toPx()))
        }
        "ROAST_SMIRK" -> {
            // Left eye cool squint
            drawOval(
                color = Color(0xFF0F172A),
                topLeft = Offset(cx - eyeSpacing - 11.dp.toPx(), eyeY - 3.dp.toPx()),
                size = Size(22.dp.toPx(), 13.dp.toPx())
            )
            drawCircle(Color.White, 3.dp.toPx(), Offset(cx - eyeSpacing - 2.dp.toPx(), eyeY + 1.dp.toPx()))
            // Right eye wide
            drawSingleEye(cx + eyeSpacing, eyeY)
        }
        else -> {
            // Normal adorable big bunny eyes
            drawSingleEye(cx - eyeSpacing, eyeY)
            drawSingleEye(cx + eyeSpacing, eyeY)
        }
    }
}

private fun DrawScope.drawSingleEye(eyeCx: Float, eyeCy: Float) {
    // Dark pupil
    drawOval(
        color = Color(0xFF0F172A),
        topLeft = Offset(eyeCx - 11.dp.toPx(), eyeCy - 14.dp.toPx()),
        size = Size(22.dp.toPx(), 28.dp.toPx())
    )
    // Primary catchlight reflection
    drawCircle(
        color = Color.White,
        radius = 5.dp.toPx(),
        center = Offset(eyeCx - 3.dp.toPx(), eyeCy - 6.dp.toPx())
    )
    // Secondary cute shine
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = 2.5.dp.toPx(),
        center = Offset(eyeCx + 4.dp.toPx(), eyeCy + 4.dp.toPx())
    )
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy + size * 0.7f)
        cubicTo(cx - size * 1.1f, cy + size * 0.2f, cx - size * 1.1f, cy - size * 0.7f, cx, cy - size * 0.3f)
        cubicTo(cx + size * 1.1f, cy - size * 0.7f, cx + size * 1.1f, cy + size * 0.2f, cx, cy + size * 0.7f)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawBunyNoseAndMouth(
    cx: Float,
    cy: Float,
    isSpeaking: Boolean,
    mouthOscillation: Float,
    emotion: String
) {
    val noseY = cy + 12.dp.toPx()

    // Pink heart-triangle nose
    val nosePath = Path().apply {
        moveTo(cx, noseY + 4.dp.toPx())
        lineTo(cx - 5.5.dp.toPx(), noseY - 3.dp.toPx())
        lineTo(cx + 5.5.dp.toPx(), noseY - 3.dp.toPx())
        close()
    }
    drawPath(nosePath, Color(0xFFFF2A85))

    val mouthY = noseY + 8.dp.toPx()

    if (isSpeaking) {
        // DYNAMIC LIP-SYNC: Real oscillating visemes!
        val openH = (6.dp.toPx() + 16.dp.toPx() * mouthOscillation)
        val openW = (18.dp.toPx() + 6.dp.toPx() * sin(mouthOscillation * 3.14f))

        // Open mouth interior (dark red)
        drawOval(
            color = Color(0xFF450A0A),
            topLeft = Offset(cx - openW / 2f, mouthY),
            size = Size(openW, openH)
        )
        // Cute pink tongue inside
        drawOval(
            color = Color(0xFFFF4081),
            topLeft = Offset(cx - openW * 0.35f, mouthY + openH * 0.45f),
            size = Size(openW * 0.7f, openH * 0.55f)
        )
        // Cute upper buck tooth (iconic bunny feature!)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(cx - 4.dp.toPx(), mouthY),
            size = Size(8.dp.toPx(), 4.5.dp.toPx()),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )
        // Mouth outline lips
        drawOval(
            color = Color(0xFF0F172A),
            topLeft = Offset(cx - openW / 2f, mouthY),
            size = Size(openW, openH),
            style = Stroke(width = 2.dp.toPx())
        )
    } else {
        // IDLE / EMOTION MOUTHS
        when (emotion) {
            "HAPPY" -> {
                // Open smiling grin with teeth
                val smilePath = Path().apply {
                    moveTo(cx - 16.dp.toPx(), mouthY)
                    quadraticTo(cx, mouthY + 16.dp.toPx(), cx + 16.dp.toPx(), mouthY)
                    close()
                }
                drawPath(smilePath, Color(0xFF450A0A))
                // Tongue
                drawCircle(Color(0xFFFF4081), 6.dp.toPx(), Offset(cx, mouthY + 10.dp.toPx()))
                // Outline
                drawPath(smilePath, Color(0xFF0F172A), style = Stroke(width = 2.5.dp.toPx()))
            }
            "SURPRISED" -> {
                // 'O' shaped mouth
                drawOval(
                    color = Color(0xFF450A0A),
                    topLeft = Offset(cx - 7.dp.toPx(), mouthY),
                    size = Size(14.dp.toPx(), 16.dp.toPx())
                )
            }
            "ROAST_SMIRK" -> {
                // Asymmetrical smirk
                val smirkPath = Path().apply {
                    moveTo(cx - 8.dp.toPx(), mouthY + 3.dp.toPx())
                    quadraticTo(cx + 4.dp.toPx(), mouthY + 5.dp.toPx(), cx + 16.dp.toPx(), mouthY - 4.dp.toPx())
                }
                drawPath(smirkPath, Color(0xFF0F172A), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            else -> {
                // Classic Bunny W-Smile
                val leftCurve = Path().apply {
                    moveTo(cx, mouthY - 2.dp.toPx())
                    quadraticTo(cx - 6.dp.toPx(), mouthY + 7.dp.toPx(), cx - 13.dp.toPx(), mouthY + 2.dp.toPx())
                }
                val rightCurve = Path().apply {
                    moveTo(cx, mouthY - 2.dp.toPx())
                    quadraticTo(cx + 6.dp.toPx(), mouthY + 7.dp.toPx(), cx + 13.dp.toPx(), mouthY + 2.dp.toPx())
                }
                drawPath(leftCurve, Color(0xFF0F172A), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                drawPath(rightCurve, Color(0xFF0F172A), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

private fun DrawScope.drawBunyGlasses(cx: Float, cy: Float, glassesId: String) {
    val glassesY = cy - 14.dp.toPx()

    when (glassesId) {
        "glass_cyber" -> {
            // Futuristic glowing HUD visor
            val visorW = 96.dp.toPx()
            val visorH = 28.dp.toPx()
            drawRoundRect(
                color = Color(0xDD0F081D),
                topLeft = Offset(cx - visorW / 2f, glassesY),
                size = Size(visorW, visorH),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            // Cyber glow borders
            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(cx - visorW / 2f, glassesY),
                size = Size(visorW, visorH),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 2.5.dp.toPx())
            )
            // Scanning laser line
            drawLine(
                color = Color(0xFFFF007F),
                start = Offset(cx - visorW * 0.4f, glassesY + visorH / 2f),
                end = Offset(cx + visorW * 0.4f, glassesY + visorH / 2f),
                strokeWidth = 2.5.dp.toPx()
            )
        }
        "glass_thug" -> {
            // 8-bit Pixel thug life shades
            val shadeW = 90.dp.toPx()
            val shadeH = 22.dp.toPx()
            drawRect(
                color = Color.Black,
                topLeft = Offset(cx - shadeW / 2f, glassesY + 4.dp.toPx()),
                size = Size(shadeW, shadeH)
            )
            // Pixel white reflection blocks
            drawRect(Color.White, Offset(cx - 32.dp.toPx(), glassesY + 8.dp.toPx()), Size(8.dp.toPx(), 5.dp.toPx()))
            drawRect(Color.White, Offset(cx - 24.dp.toPx(), glassesY + 13.dp.toPx()), Size(8.dp.toPx(), 5.dp.toPx()))
            drawRect(Color.White, Offset(cx + 12.dp.toPx(), glassesY + 8.dp.toPx()), Size(8.dp.toPx(), 5.dp.toPx()))
            drawRect(Color.White, Offset(cx + 20.dp.toPx(), glassesY + 13.dp.toPx()), Size(8.dp.toPx(), 5.dp.toPx()))
        }
        "glass_nerd" -> {
            // Gold round wireframe spectacles
            val eyeSpacing = 28.dp.toPx()
            drawCircle(Color(0xFFFFD700), 16.dp.toPx(), Offset(cx - eyeSpacing, glassesY + 8.dp.toPx()), style = Stroke(2.5.dp.toPx()))
            drawCircle(Color(0xFFFFD700), 16.dp.toPx(), Offset(cx + eyeSpacing, glassesY + 8.dp.toPx()), style = Stroke(2.5.dp.toPx()))
            // Bridge
            drawLine(Color(0xFFFFD700), Offset(cx - 12.dp.toPx(), glassesY + 8.dp.toPx()), Offset(cx + 12.dp.toPx(), glassesY + 8.dp.toPx()), 2.5.dp.toPx())
        }
        "glass_aviator" -> {
            // Aviator shades
            val eyeSpacing = 28.dp.toPx()
            drawOval(Color(0xDD1E293B), Offset(cx - eyeSpacing - 18.dp.toPx(), glassesY), Size(36.dp.toPx(), 26.dp.toPx()))
            drawOval(Color(0xDD1E293B), Offset(cx + eyeSpacing - 18.dp.toPx(), glassesY), Size(36.dp.toPx(), 26.dp.toPx()))
            drawOval(Color(0xFF94A3B8), Offset(cx - eyeSpacing - 18.dp.toPx(), glassesY), Size(36.dp.toPx(), 26.dp.toPx()), style = Stroke(2.dp.toPx()))
            drawOval(Color(0xFF94A3B8), Offset(cx + eyeSpacing - 18.dp.toPx(), glassesY), Size(36.dp.toPx(), 26.dp.toPx()), style = Stroke(2.dp.toPx()))
            drawLine(Color(0xFF94A3B8), Offset(cx - 8.dp.toPx(), glassesY + 5.dp.toPx()), Offset(cx + 8.dp.toPx(), glassesY + 5.dp.toPx()), 2.dp.toPx())
        }
    }
}

private fun DrawScope.drawBunyHeadwear(cx: Float, cy: Float, headwearId: String) {
    val topY = cy - 54.dp.toPx()

    when (headwearId) {
        "head_cap" -> {
            // Backward red cap
            drawRoundRect(
                color = Color(0xFFDC2626),
                topLeft = Offset(cx - 48.dp.toPx(), topY - 10.dp.toPx()),
                size = Size(96.dp.toPx(), 34.dp.toPx()),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )
            // Visor turned back
            drawRoundRect(
                color = Color(0xFF991B1B),
                topLeft = Offset(cx - 56.dp.toPx(), topY + 12.dp.toPx()),
                size = Size(112.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )
            // White emblem
            drawCircle(Color.White, 6.dp.toPx(), Offset(cx, topY + 6.dp.toPx()))
        }
        "head_crown" -> {
            // Golden imperial crown
            val crownPath = Path().apply {
                moveTo(cx - 40.dp.toPx(), topY + 6.dp.toPx())
                lineTo(cx - 36.dp.toPx(), topY - 26.dp.toPx())
                lineTo(cx - 18.dp.toPx(), topY - 8.dp.toPx())
                lineTo(cx, topY - 32.dp.toPx())
                lineTo(cx + 18.dp.toPx(), topY - 8.dp.toPx())
                lineTo(cx + 36.dp.toPx(), topY - 26.dp.toPx())
                lineTo(cx + 40.dp.toPx(), topY + 6.dp.toPx())
                close()
            }
            drawPath(crownPath, Color(0xFFFFD700))
            drawPath(crownPath, Color(0xFFB45309), style = Stroke(2.dp.toPx()))
            // Crown jewels
            drawCircle(Color(0xFFEF4444), 3.5.dp.toPx(), Offset(cx, topY - 26.dp.toPx()))
            drawCircle(Color(0xFF00E5FF), 3.dp.toPx(), Offset(cx - 34.dp.toPx(), topY - 21.dp.toPx()))
            drawCircle(Color(0xFF00E5FF), 3.dp.toPx(), Offset(cx + 34.dp.toPx(), topY - 21.dp.toPx()))
        }
        "head_beanie" -> {
            // Knitted winter beanie
            drawRoundRect(
                color = Color(0xFF0284C7),
                topLeft = Offset(cx - 45.dp.toPx(), topY - 14.dp.toPx()),
                size = Size(90.dp.toPx(), 40.dp.toPx()),
                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
            )
            // Beanie fold
            drawRoundRect(
                color = Color(0xFF0369A1),
                topLeft = Offset(cx - 48.dp.toPx(), topY + 12.dp.toPx()),
                size = Size(96.dp.toPx(), 14.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Pompom
            drawCircle(Color.White, 10.dp.toPx(), Offset(cx, topY - 18.dp.toPx()))
        }
        "head_ninja" -> {
            // Black Ninja headband with red symbol
            drawRoundRect(
                color = Color(0xFF18181B),
                topLeft = Offset(cx - 52.dp.toPx(), topY + 4.dp.toPx()),
                size = Size(104.dp.toPx(), 16.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawCircle(Color(0xFFE11D48), 5.dp.toPx(), Offset(cx, topY + 12.dp.toPx()))
        }
    }
}
