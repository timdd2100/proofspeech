package com.blockchain.testspeech

import android.content.res.Resources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


fun String.printThreadInfo() = run { println("[${Thread.currentThread().name}] $this") }

val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun CoroutineScope.launchPeriodicAsync(
    delayMillis: Long = 0,
    repeatMillis: Long = 0,
    action: (scope: CoroutineScope) -> Unit
) = this.async {
    delay(delayMillis)
    if (repeatMillis > 0) {
        while (isActive) {
            action(this)
            delay(repeatMillis)
        }
    } else {
        action(this)
    }
}

suspend fun Call.await(): Response = suspendCoroutine { block ->
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            block.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            block.resume(response)
        }
    })

}