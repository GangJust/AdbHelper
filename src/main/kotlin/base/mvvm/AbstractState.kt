package base.mvvm

import kotlinx.coroutines.*
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext


interface IState {

}

abstract class AbstractState<L : ILogic> : IState {
    val logic = createLogic()

    //subclasses `createModel()` for data processing
    abstract fun createLogic(): L

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return logic.launch(block)
    }
}