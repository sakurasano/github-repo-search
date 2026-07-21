package com.sakurasano.reposearch.ui

import com.sakurasano.reposearch.model.DataResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * お気に入りの書き込み結果を受け取り、失敗したときだけ通知イベントを流す。
 */
class FavoriteWriteNotifier {
    private val _writeFailed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val writeFailed: SharedFlow<Unit> = _writeFailed.asSharedFlow()

    fun notifyIfFailure(result: DataResult<Unit>) {
        if (result is DataResult.Failure) _writeFailed.tryEmit(Unit)
    }
}
