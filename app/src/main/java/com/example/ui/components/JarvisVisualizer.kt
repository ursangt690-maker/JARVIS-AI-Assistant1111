package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JarvisVisualizer(status: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_visualizer")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = android.graphics.PointF(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f * 0.85f * pulse

            // Outer ring segments
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )

            // Middle rotating ring
            drawArc(
                brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor, Color.Transparent)),
                startAngle = rotation,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx())
            )

            // Inner pulsing core
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = radius * 0.5f * pulse
            )

            drawCircle(
                color = primaryColor,
                radius = radius * 0.25f,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Text(
            text = status.uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}
