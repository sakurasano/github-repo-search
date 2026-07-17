package com.sakurasano.reposearch.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * お気に入りの書き込み（add/remove）を実行し、失敗したときだけ通知イベントを流す。
 * コルーチンのキャンセルを壊さないよう CancellationException は握りつぶさず再throwする。
 */
internal suspend fun MutableSharedFlow<Unit>.runFavoriteWrite(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        tryEmit(Unit)
    }
}
