package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import kotlin.coroutines.cancellation.CancellationException

/**
 * DB書き込みを実行し、例外を[AppError]へ変換して[DataResult]で返す。
 */
suspend fun <T> dbCall(block: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: CancellationException) {
        throw e // キャンセルは飲み込まず伝播させる
    } catch (e: Exception) {
        DataResult.Failure(AppError.Unknown(e))
    }
