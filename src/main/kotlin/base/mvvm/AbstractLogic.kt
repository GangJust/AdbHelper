package base.mvvm

import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext


interface ILogic {
    fun dispose()

    fun launch(block: suspend CoroutineScope.() -> Unit): Job
}


abstract class AbstractLogic : ILogic {

    override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        val handlerException = CoroutineExceptionHandler { coroutineContext, throwable ->
            if (coroutineContext.isActive) {
                coroutineContext.cancelChildren()
                coroutineContext.cancel()
                writeErrLog(throwable)
            }
        }
        val job = CoroutineScope(EmptyCoroutineContext + handlerException).launch {
            try {
                block.invoke(this)
            } catch (e: CancellationException) {
                e.printStackTrace() //该异常不做操作,
            } catch (e: Exception) {
                e.printStackTrace()
                writeErrLog(e)
                if (isActive) {
                    cancel()
                }
            }
        }
        return job
    }

    //记录错误日志
    private fun writeErrLog(e: Throwable) {
        val logDir = File("log")
        if (!logDir.exists()) logDir.mkdirs()

        val logFile = File(logDir, SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date()) + ".log")
        val stackTrace = e.stackTraceToString()
        logFile.appendText(
            "Exception occurred at ${
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    Locale.getDefault()
                ).format(Date())
            }:\n"
        )
        logFile.appendText("$stackTrace\n\n")
    }
}