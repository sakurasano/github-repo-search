// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.spotless)
}

// コードのフォーマッタ。スタイルは.editorconfigで一元管理する
spotless {
    // 意図的なレイアウトを展開・再構成するルールは無効化
    val ktlintRules = mapOf(
        "ktlint_standard_function-signature" to "disabled",
        "ktlint_standard_class-signature" to "disabled",
        "ktlint_standard_multiline-expression-wrapping" to "disabled",
    )
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(ktlintRules)
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint().editorConfigOverride(ktlintRules)
    }
}
