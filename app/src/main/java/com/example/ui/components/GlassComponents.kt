package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalmBlue
import com.example.ui.theme.CalmBlueGlow
import com.example.ui.theme.CalmBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenGlow
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.DarkGlassSurface
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.LightBg
import com.example.ui.theme.LightGlassBorder
import com.example.ui.theme.LightGlassCard
import com.example.ui.theme.LightGlassSurface
import com.example.ui.theme.LightTextPrimary

/**
 * Ambient background mesh canvas that creates authentic frosted glass depth
 * by subtly dispersing calm light blue and calm light green radial gradients.
 */
@Composable
fun GlassMeshBackground(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor = if (isDarkMode) DarkNavyBg else LightBg
    val greenOrbColor = if (isDarkMode) CalmGreen.copy(alpha = 0.12f) else CalmGreenLight.copy(alpha = 0.18f)
    val blueOrbColor = if (isDarkMode) CalmBlue.copy(alpha = 0.14f) else CalmBlueLight.copy(alpha = 0.20f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .drawBehind {
                val width = size.width
                val height = size.height

                // Calm Light Green ambient glow in top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(greenOrbColor, Color.Transparent),
                        center = Offset(width * 0.85f, height * 0.12f),
                        radius = width * 0.65f
                    )
                )

                // Calm Light Blue ambient glow in middle-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(blueOrbColor, Color.Transparent),
                        center = Offset(width * 0.15f, height * 0.45f),
                        radius = width * 0.70f
                    )
                )

                // Bottom calm green/blue subtle pool
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            (if (isDarkMode) CalmBlueGlow else CalmGreenGlow).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.70f, height * 0.88f),
                        radius = width * 0.55f
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * iPhone-like Frosted Glass Card with translucent background, specular hairline border,
 * and smooth squircle corner radii.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val containerColor = if (isDarkMode) DarkGlassCard else LightGlassCard
    val defaultBorder = BorderStroke(
        width = 1.dp,
        color = if (isDarkMode) DarkGlassBorder else LightGlassBorder
    )
    val effectiveBorder = border ?: defaultBorder

    val cardModifier = modifier
        .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
        .clip(shape)
        .background(containerColor)
        .border(effectiveBorder, shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = if (isDarkMode) CalmBlueLight else CalmGreen),
                    role = Role.Button,
                    onClick = onClick
                )
            } else Modifier
        )

    Box(
        modifier = cardModifier
    ) {
        content()
    }
}

/**
 * Minimalist iPhone-style Glass Pill / Badge.
 */
@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true,
    accentColor: Color = if (isDarkMode) CalmGreenLight else CalmBlue,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val containerColor = accentColor.copy(alpha = if (isDarkMode) 0.15f else 0.12f)
    val borderColor = accentColor.copy(alpha = if (isDarkMode) 0.35f else 0.28f)
    val shape = CircleShape

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = accentColor),
                        role = Role.Button,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Box(modifier = Modifier.size(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                ),
                color = accentColor
            )
        }
    }
}

/**
 * iPhone-like Minimalist Dark/Light Mode Switch Toggle.
 * Smooth tactile pill with sliding Sun/Moon indicator.
 */
@Composable
fun IosThemeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isDarkMode) 360f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "theme_icon_rotation"
    )

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "theme_icon_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isDarkMode) CalmBlueLight else CalmBlue,
        label = "theme_icon_color"
    )

    val pillContainer = if (isDarkMode) DarkGlassSurface else LightGlassSurface
    val pillBorder = if (isDarkMode) DarkGlassBorder else LightGlassBorder

    Surface(
        modifier = modifier
            .testTag("theme_mode_toggle")
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = iconColor),
                onClick = onToggle
            ),
        shape = CircleShape,
        color = pillContainer,
        border = BorderStroke(1.dp, pillBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = iconColor,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation)
                    .scale(scale)
            )
            Box(modifier = Modifier.size(6.dp))
            Text(
                text = if (isDarkMode) "Dark" else "Light",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = if (isDarkMode) DarkTextPrimary else LightTextPrimary
            )
        }
    }
}
