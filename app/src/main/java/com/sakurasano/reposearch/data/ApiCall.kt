package com.sakurasano.reposearch.data

import com.sakurasano.reposearch.model.AppError
import com.sakurasano.reposearch.model.DataResult
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * API呼び出しを実行し、通信ライブラリ/HTTPの例外を[AppError]へ変換して[DataResult]で返す。
 */
suspend fun <T> apiCall(block: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: CancellationException) {
        throw e // キャンセルは飲み込まず伝播させる
    } catch (_: IOException) {
        DataResult.Failure(AppError.Network)
    } catch (e: HttpException) {
        val error = if (e.code() == 403) AppError.RateLimited else AppError.Server(e.code())
        DataResult.Failure(error)
    } catch (e: Exception) {
        DataResult.Failure(AppError.Unknown(e))
    }
