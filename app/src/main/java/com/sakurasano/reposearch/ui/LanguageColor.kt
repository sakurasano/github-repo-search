package com.sakurasano.reposearch.ui

import androidx.compose.ui.graphics.Color

// GitHub Linguistの言語カラー（colors.yml）から主要言語を抜粋したもの。
private val languageColors = mapOf(
    "kotlin" to Color(0xFFA97BFF),
    "java" to Color(0xFFB07219),
    "javascript" to Color(0xFFF1E05A),
    "typescript" to Color(0xFF3178C6),
    "python" to Color(0xFF3572A5),
    "go" to Color(0xFF00ADD8),
    "rust" to Color(0xFFDEA584),
    "ruby" to Color(0xFF701516),
    "php" to Color(0xFF4F5D95),
    "c" to Color(0xFF555555),
    "c++" to Color(0xFFF34B7D),
    "c#" to Color(0xFF178600),
    "swift" to Color(0xFFF05138),
    "objective-c" to Color(0xFF438EFF),
    "dart" to Color(0xFF00B4AB),
    "shell" to Color(0xFF89E051),
    "html" to Color(0xFFE34C26),
    "css" to Color(0xFF663399),
    "vue" to Color(0xFF41B883),
    "scala" to Color(0xFFC22D40),
    "elixir" to Color(0xFF6E4A7E),
    "haskell" to Color(0xFF5E5086),
    "lua" to Color(0xFF000080),
    "r" to Color(0xFF198CE7),
    "perl" to Color(0xFF0298C3),
    "groovy" to Color(0xFF4298B8),
    "clojure" to Color(0xFFDB5855),
    "julia" to Color(0xFFA270BA),
    "dockerfile" to Color(0xFF384D54),
    "jupyter notebook" to Color(0xFFDA5B0B),
)

/**
 * 言語名に対応するGitHub Linguistの色を返す。マップに無ければnull。
 */
fun languageColor(language: String): Color? = languageColors[language.lowercase()]
