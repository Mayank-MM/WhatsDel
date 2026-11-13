package com.example.whatsdel.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whatsdel.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // --- Animation values ---
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0f) }

    // Pulsing glow behind the logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotating particle ring
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Launch animation sequence
    LaunchedEffect(Unit) {
        // 1. Scale in logo with bounce
        logoScale.animateTo(1f, animationSpec = tween(600, easing = EaseOutBack))
        logoAlpha.animateTo(1f, animationSpec = tween(400))
        
        // 2. Expand ring
        ringScale.animateTo(1f, animationSpec = tween(500, easing = EaseInOutCubic))
        
        // 3. Fade in title
        titleAlpha.animateTo(1f, animationSpec = tween(400))
        
        // 4. Fade in subtitle
        subtitleAlpha.animateTo(1f, animationSpec = tween(300))
        
        // Hold for a moment
        delay(1200)
        
        // Navigate away
        onSplashFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating ambient particles (background decoration)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val particleCount = 8
            val radius = size.minDimension * 0.35f
            val centerX = size.width / 2
            val centerY = size.height / 2

            for (i in 0 until particleCount) {
                val angle = Math.toRadians((rotationAngle + (i * 360f / particleCount)).toDouble())
                val x = centerX + (radius * cos(angle)).toFloat()
                val y = centerY + (radius * sin(angle)).toFloat()
                val particleAlpha = (ringScale.value * 0.25f).coerceIn(0f, 0.3f)
                
                drawCircle(
                    color = primaryColor.copy(alpha = particleAlpha),
                    radius = 4.dp.toPx() + (i % 3) * 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing glow behind logo
            Box(contentAlignment = Alignment.Center) {
                // Glow circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale * ringScale.value)
                        .alpha(0.15f * ringScale.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Main logo container
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.whatsdel_logo),
                        contentDescription = "WhatsDel Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name
            Text(
                text = "WhatsDel",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Recover. Preserve. Protect.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading dots
            LoadingDots(
                modifier = Modifier.alpha(subtitleAlpha.value),
                color = primaryColor
            )
        }
    }
}

@Composable
private fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ), label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ), label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ), label = "dot3"
    )

    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(dot1, dot2, dot3).forEach { offsetY ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.7f))
            )
        }
    }
}
