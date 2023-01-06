package base.mvvm

import kotlinx.coroutines.*


interface IState {

}

abstract class AbstractState<M : ILogic> : IState {
    val logic = createLogic()

    //subclasses `createModel()` for data processing
    abstract fun createLogic(): M

    fun launch(block: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(block = block)
}