package com.example.ui.editor

import androidx.compose.ui.graphics.Color

data class EditorTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val background: Color,
    val sidebarBg: Color,
    val activityBarBg: Color,
    val statusBarBg: Color,
    val tabActiveBg: Color,
    val tabInactiveBg: Color,
    val textPrimary: Color,
    val accent: Color,
    val selectionBg: Color,
    val gutterBg: Color,
    val lineNumberColor: Color
)

object ThemeEngine {
    val DarkPlus = EditorTheme(
        id = "dark_plus",
        name = "VS Code Dark+",
        isDark = true,
        background = Color(0xFF1E1E1E),
        sidebarBg = Color(0xFF252526),
        activityBarBg = Color(0xFF333333),
        statusBarBg = Color(0xFF007ACC),
        tabActiveBg = Color(0xFF1E1E1E),
        tabInactiveBg = Color(0xFF2D2D2D),
        textPrimary = Color(0xFFD4D4D4),
        accent = Color(0xFF007ACC),
        selectionBg = Color(0xFF264F78),
        gutterBg = Color(0xFF1E1E1E),
        lineNumberColor = Color(0xFF858585)
    )

    val Monokai = EditorTheme(
        id = "monokai",
        name = "Monokai Classic",
        isDark = true,
        background = Color(0xFF272822),
        sidebarBg = Color(0xFF1E1F1C),
        activityBarBg = Color(0xFF141414),
        statusBarBg = Color(0xFF75715E),
        tabActiveBg = Color(0xFF272822),
        tabInactiveBg = Color(0xFF1E1F1C),
        textPrimary = Color(0xFFF8F8F2),
        accent = Color(0xFFA6E22E),
        selectionBg = Color(0xFF49483E),
        gutterBg = Color(0xFF272822),
        lineNumberColor = Color(0xFF90908A)
    )

    val OneDarkPro = EditorTheme(
        id = "one_dark_pro",
        name = "One Dark Pro",
        isDark = true,
        background = Color(0xFF282C34),
        sidebarBg = Color(0xFF21252B),
        activityBarBg = Color(0xFF1B1D23),
        statusBarBg = Color(0xFF61AFEF),
        tabActiveBg = Color(0xFF282C34),
        tabInactiveBg = Color(0xFF21252B),
        textPrimary = Color(0xFFABB2BF),
        accent = Color(0xFF61AFEF),
        selectionBg = Color(0xFF3E4451),
        gutterBg = Color(0xFF282C34),
        lineNumberColor = Color(0xFF5C6370)
    )

    val GitHubDark = EditorTheme(
        id = "github_dark",
        name = "GitHub Dark High Contrast",
        isDark = true,
        background = Color(0xFF0D1117),
        sidebarBg = Color(0xFF161B22),
        activityBarBg = Color(0xFF010409),
        statusBarBg = Color(0xFF1F6FEB),
        tabActiveBg = Color(0xFF0D1117),
        tabInactiveBg = Color(0xFF161B22),
        textPrimary = Color(0xFFE6EDF3),
        accent = Color(0xFF58A6FF),
        selectionBg = Color(0xFF1F6FEB),
        gutterBg = Color(0xFF0D1117),
        lineNumberColor = Color(0xFF7D8590)
    )

    val SynthWave84 = EditorTheme(
        id = "synthwave",
        name = "SynthWave '84 (Cyberpunk)",
        isDark = true,
        background = Color(0xFF262335),
        sidebarBg = Color(0xFF241B2F),
        activityBarBg = Color(0xFF1A1325),
        statusBarBg = Color(0xFFFF7EDB),
        tabActiveBg = Color(0xFF262335),
        tabInactiveBg = Color(0xFF241B2F),
        textPrimary = Color(0xFFF92AAD),
        accent = Color(0xFF36F9F6),
        selectionBg = Color(0xFF614D85),
        gutterBg = Color(0xFF262335),
        lineNumberColor = Color(0xFF848BBD)
    )

    val AllThemes = listOf(DarkPlus, Monokai, OneDarkPro, GitHubDark, SynthWave84)
}
