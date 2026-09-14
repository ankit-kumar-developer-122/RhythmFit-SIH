package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// CALM LIGHT GREEN & CALM LIGHT BLUE SPECTRUM (Primary Brand Identity)
// ============================================================================
val CalmGreen = Color(0xFF10B981)          // Calming emerald mint
val CalmGreenLight = Color(0xFF34D399)     // Vibrant light mint
val CalmGreenSubtle = Color(0xFFD1FAE5)    // Soft frosted green tint
val CalmGreenDark = Color(0xFF065F46)      // Deep forest emerald
val CalmGreenGlow = Color(0x4034D399)      // Ambient radial glow

val CalmBlue = Color(0xFF0284C7)           // Professional cerulean blue
val CalmBlueLight = Color(0xFF38BDF8)      // Crisp light sky blue
val CalmBlueSubtle = Color(0xFFE0F2FE)     // Soft frosted sky tint
val CalmBlueDark = Color(0xFF0C4A6E)       // Deep twilight navy
val CalmBlueGlow = Color(0x4038BDF8)       // Ambient radial glow

// Legacy color aliases to maintain zero regressions
val RhythmCyan = CalmBlueLight
val RhythmCyanLight = CalmBlueSubtle
val RhythmCyanDark = CalmBlue
val CalmMint = CalmGreenLight
val CalmMintSoft = CalmGreenSubtle
val GentleSky = CalmBlueLight
val EmeraldVitality = CalmGreen
val EmeraldLight = CalmGreenLight

// Supporting Accent Tones (iOS-like subtle vibrancy)
val CoralPulse = Color(0xFFF97316)
val CoralLight = Color(0xFFFDBA74)
val AmberFocus = Color(0xFFF59E0B)
val RoseDampener = Color(0xFFF43F5E)
val SoftLilac = Color(0xFFA78BFA)
val WarmPeach = Color(0xFFFB923C)
val MutedRose = Color(0xFFF43F5E)
val SereneIndigo = Color(0xFF818CF8)
val TranquilSlate = Color(0xFF64748B)

// ============================================================================
// PROFESSIONAL BACKGROUND & FOREGROUND (Light & Dark iPhone Themes)
// ============================================================================
// Light Mode (Clean, airy, professional gallery off-white)
val LightBg = Color(0xFFF8FAFC)
val LightBgSecondary = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightGlassSurface = Color(0xCCFFFFFF)       // 80% white frosted glass
val LightGlassCard = Color(0xB3FFFFFF)          // 70% translucent glass card
val LightGlassBorder = Color(0x260F172A)        // 15% hairline border
val LightGlassHighlight = Color(0x66FFFFFF)     // Specular top highlight
val LightTextPrimary = Color(0xFF0F172A)        // High contrast slate black
val LightTextSecondary = Color(0xFF475569)      // Balanced graphite slate
val LightTextMuted = Color(0xFF94A3B8)          // Muted subtext
val LightCard = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)

// Dark Mode (Deep OLED slate midnight, calm and comfortable)
val DarkNavyBg = Color(0xFF0B0F19)
val DarkNavySurface = Color(0xFF111827)
val DarkNavyCard = Color(0xFF1E293B)
val DarkGlassSurface = Color(0x8C1E293B)        // 55% translucent dark slate
val DarkGlassCard = Color(0x73111827)           // 45% dark frosted glass
val DarkGlassBorder = Color(0x26FFFFFF)         // 15% hairline light border
val DarkGlassHighlight = Color(0x40FFFFFF)      // Specular glass rim
val DarkNavyBorder = Color(0xFF243666)
val DarkTextPrimary = Color(0xFFF8FAFC)         // Crisp off-white
val DarkTextSecondary = Color(0xFF94A3B8)       // Muted slate gray
val DarkTextMuted = Color(0xFF64748B)           // Deep muted slate

// Calming Glass Frosted Accents
val FrostedTealGlass = Color(0x2434D399)
val FrostedBlueGlass = Color(0x2438BDF8)
val FrostedLilacGlass = Color(0x24A78BFA)

val TextPrimaryDark = DarkTextPrimary
val TextSecondaryDark = DarkTextSecondary
val TextPrimaryLight = LightTextPrimary
val TextSecondaryLight = LightTextSecondary
