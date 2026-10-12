package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.RobotAccent
import com.example.data.model.RobotExpression
import kotlin.math.sin

@Composable
fun NovaRobotAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    expression: RobotExpression = RobotExpression.HAPPY,
    accent: RobotAccent = RobotAccent.CYAN,
    isSpeaking: Boolean = false,
    audioLevel: Float = 0f,
    lowPerformanceMode: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "robot_anim")

    // Subtle floating bobbing animation
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    // Eye blinking animation (blinks every ~3.5 seconds)
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                1f at 0
                1f at 3200
                0.05f at 3300
                1f at 3400
                1f at 3500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Glowing aura pulse
    val auraGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )

    // Speaking mouth motion
    val mouthAudioFactor = if (isSpeaking) {
        (0.4f + audioLevel * 0.6f).coerceIn(0.2f, 1f)
    } else 0f

    val accentColor = accent.color

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f + if (!lowPerformanceMode) bobbingOffset else 0f

            val headRadius = canvasW * 0.36f

            // 1. Glowing outer holographic halo (skipped if low performance)
            if (!lowPerformanceMode) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f * auraGlow),
                            accentColor.copy(alpha = 0.08f * auraGlow),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = headRadius * 1.45f
                    ),
                    radius = headRadius * 1.45f,
                    center = Offset(centerX, centerY)
                )
            }

            // 2. Headphone Earcups on Left & Right
            val earWidth = headRadius * 0.28f
            val earHeight = headRadius * 0.72f

            // Left Earcup
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2B3A55), Color(0xFF131E33)),
                    start = Offset(centerX - headRadius - earWidth, centerY),
                    end = Offset(centerX - headRadius, centerY)
                ),
                topLeft = Offset(centerX - headRadius - earWidth * 0.8f, centerY - earHeight / 2),
                size = Size(earWidth, earHeight),
                cornerRadius = CornerRadius(earWidth / 2, earWidth / 2)
            )
            // Left Earcup LED Light
            drawCircle(
                color = accentColor,
                radius = earWidth * 0.3f,
                center = Offset(centerX - headRadius - earWidth * 0.3f, centerY)
            )

            // Right Earcup
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF131E33), Color(0xFF2B3A55)),
                    start = Offset(centerX + headRadius, centerY),
                    end = Offset(centerX + headRadius + earWidth, centerY)
                ),
                topLeft = Offset(centerX + headRadius - earWidth * 0.2f, centerY - earHeight / 2),
                size = Size(earWidth, earHeight),
                cornerRadius = CornerRadius(earWidth / 2, earWidth / 2)
            )
            // Right Earcup LED Light
            drawCircle(
                color = accentColor,
                radius = earWidth * 0.3f,
                center = Offset(centerX + headRadius + earWidth * 0.3f, centerY)
            )

            // Top Headphone Bridge Band
            drawArc(
                color = Color(0xFF334155),
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(centerX - headRadius * 0.85f, centerY - headRadius * 1.15f),
                size = Size(headRadius * 1.7f, headRadius * 1.6f),
                style = Stroke(width = headRadius * 0.16f, cap = StrokeCap.Round)
            )

            // 3. Cute Robot Head Helmet (Sleek White/Metallic shell)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF1F5F9),
                        Color(0xFFCBD5E1),
                        Color(0xFF64748B)
                    ),
                    center = Offset(centerX - headRadius * 0.2f, centerY - headRadius * 0.3f),
                    radius = headRadius * 1.2f
                ),
                radius = headRadius,
                center = Offset(centerX, centerY)
            )

            // Antenna Orb on top
            val antennaRadius = headRadius * 0.14f
            drawCircle(
                color = accentColor,
                radius = antennaRadius,
                center = Offset(centerX, centerY - headRadius * 0.95f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = antennaRadius * 0.4f,
                center = Offset(centerX - antennaRadius * 0.2f, centerY - headRadius * 0.95f - antennaRadius * 0.2f)
            )

            // 4. Black Gloss Visor Faceplate
            val visorWidth = headRadius * 1.48f
            val visorHeight = headRadius * 1.18f
            val visorTopLeft = Offset(centerX - visorWidth / 2f, centerY - visorHeight * 0.42f)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                ),
                topLeft = visorTopLeft,
                size = Size(visorWidth, visorHeight),
                cornerRadius = CornerRadius(visorHeight * 0.44f, visorHeight * 0.44f)
            )
            // Visor Rim Highlight
            drawRoundRect(
                color = accentColor.copy(alpha = 0.4f),
                topLeft = visorTopLeft,
                size = Size(visorWidth, visorHeight),
                cornerRadius = CornerRadius(visorHeight * 0.44f, visorHeight * 0.44f),
                style = Stroke(width = 2.5f)
            )

            // 5. Digital Robot Eyes
            val eyeSpacing = visorWidth * 0.26f
            val eyeY = centerY - visorHeight * 0.05f
            val leftEyeX = centerX - eyeSpacing
            val rightEyeX = centerX + eyeSpacing
            val eyeRadius = visorWidth * 0.14f

            drawEyes(
                expression = expression,
                accentColor = accentColor,
                blinkProgress = if (expression == RobotExpression.SLEEPY) 0.35f else blinkProgress,
                leftX = leftEyeX,
                rightX = rightEyeX,
                eyeY = eyeY,
                eyeRadius = eyeRadius
            )

            // 6. Digital Robot Mouth
            val mouthY = centerY + visorHeight * 0.28f
            drawMouth(
                expression = expression,
                accentColor = accentColor,
                centerX = centerX,
                mouthY = mouthY,
                visorWidth = visorWidth,
                isSpeaking = isSpeaking,
                mouthAudioFactor = mouthAudioFactor
            )
        }
    }
}

private fun DrawScope.drawEyes(
    expression: RobotExpression,
    accentColor: Color,
    blinkProgress: Float,
    leftX: Float,
    rightX: Float,
    eyeY: Float,
    eyeRadius: Float
) {
    val effectiveEyeHeight = eyeRadius * blinkProgress.coerceAtLeast(0.1f)

    when (expression) {
        RobotExpression.HAPPY -> {
            // Cute inverted arc / smiling squint
            drawArc(
                color = accentColor,
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(leftX - eyeRadius, eyeY - eyeRadius * 0.6f),
                size = Size(eyeRadius * 2, eyeRadius * 1.2f),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(rightX - eyeRadius, eyeY - eyeRadius * 0.6f),
                size = Size(eyeRadius * 2, eyeRadius * 1.2f),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
        RobotExpression.SURPRISED -> {
            // Large circular open eyes
            drawCircle(color = accentColor, radius = eyeRadius * 1.15f, center = Offset(leftX, eyeY))
            drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(leftX - 4f, eyeY - 4f))
            drawCircle(color = accentColor, radius = eyeRadius * 1.15f, center = Offset(rightX, eyeY))
            drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(rightX - 4f, eyeY - 4f))
        }
        RobotExpression.CONFIDENT -> {
            // Sleek slanted confident eyes
            drawLine(
                color = accentColor,
                start = Offset(leftX - eyeRadius, eyeY + 2f),
                end = Offset(leftX + eyeRadius, eyeY - 6f),
                strokeWidth = 7f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accentColor,
                start = Offset(rightX - eyeRadius, eyeY - 6f),
                end = Offset(rightX + eyeRadius, eyeY + 2f),
                strokeWidth = 7f,
                cap = StrokeCap.Round
            )
        }
        else -> {
            // Standard expressive oval LED eyes with highlights
            drawOval(
                color = accentColor,
                topLeft = Offset(leftX - eyeRadius, eyeY - effectiveEyeHeight),
                size = Size(eyeRadius * 2, effectiveEyeHeight * 2)
            )
            drawOval(
                color = accentColor,
                topLeft = Offset(rightX - eyeRadius, eyeY - effectiveEyeHeight),
                size = Size(eyeRadius * 2, effectiveEyeHeight * 2)
            )
            // Iris highlights if open
            if (blinkProgress > 0.4f) {
                drawCircle(
                    color = Color.White,
                    radius = eyeRadius * 0.35f,
                    center = Offset(leftX - eyeRadius * 0.3f, eyeY - eyeRadius * 0.3f)
                )
                drawCircle(
                    color = Color.White,
                    radius = eyeRadius * 0.35f,
                    center = Offset(rightX - eyeRadius * 0.3f, eyeY - eyeRadius * 0.3f)
                )
            }
        }
    }
}

private fun DrawScope.drawMouth(
    expression: RobotExpression,
    accentColor: Color,
    centerX: Float,
    mouthY: Float,
    visorWidth: Float,
    isSpeaking: Boolean,
    mouthAudioFactor: Float
) {
    if (isSpeaking) {
        // Dynamic speaking waveform mouth
        val waveWidth = visorWidth * 0.38f
        val openHeight = (visorWidth * 0.16f) * mouthAudioFactor
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(centerX - waveWidth / 2f, mouthY - openHeight / 2f),
            size = Size(waveWidth, openHeight.coerceAtLeast(4f)),
            cornerRadius = CornerRadius(openHeight / 2f, openHeight / 2f)
        )
    } else {
        when (expression) {
            RobotExpression.HAPPY, RobotExpression.CONFIDENT -> {
                val mouthW = visorWidth * 0.32f
                drawArc(
                    color = accentColor,
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(centerX - mouthW / 2f, mouthY - mouthW * 0.35f),
                    size = Size(mouthW, mouthW * 0.7f),
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
            }
            RobotExpression.SURPRISED -> {
                drawCircle(
                    color = accentColor,
                    radius = visorWidth * 0.07f,
                    center = Offset(centerX, mouthY),
                    style = Stroke(width = 4f)
                )
            }
            RobotExpression.THINKING -> {
                // Subtle wavy line
                drawLine(
                    color = accentColor,
                    start = Offset(centerX - visorWidth * 0.12f, mouthY),
                    end = Offset(centerX + visorWidth * 0.12f, mouthY),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
            else -> {
                drawLine(
                    color = accentColor.copy(alpha = 0.85f),
                    start = Offset(centerX - visorWidth * 0.14f, mouthY),
                    end = Offset(centerX + visorWidth * 0.14f, mouthY),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
