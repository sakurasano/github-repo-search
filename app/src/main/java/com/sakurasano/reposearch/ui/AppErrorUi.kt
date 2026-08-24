package com.sakurasano.reposearch.ui

import androidx.annotation.StringRes
import com.sakurasano.reposearch.R
import com.sakurasano.reposearch.model.AppError

@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.Network -> R.string.error_network
    AppError.RateLimited -> R.string.error_rate_limited
    is AppError.Server -> R.string.error_server
    is AppError.Unknown -> R.string.error_unknown
}
