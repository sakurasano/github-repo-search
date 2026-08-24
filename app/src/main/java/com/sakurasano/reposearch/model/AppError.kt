package com.sakurasano.reposearch.model

sealed interface AppError {
    /** 通信そのものの失敗（オフライン、タイムアウト等） */
    data object Network : AppError

    /** レート制限超過。[retryAtEpochSeconds]は再試行してよくなる時刻。応答から判断できなければnull */
    data class RateLimited(val retryAtEpochSeconds: Long?) : AppError

    /** その他のサーバーエラー */
    data class Server(val code: Int) : AppError

    /** 想定外のエラー。原因の例外を保持する */
    data class Unknown(val throwable: Throwable) : AppError
}
