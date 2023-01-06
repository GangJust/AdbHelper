package base.mvvm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


interface ILogic {
    fun dispose()
}


abstract class AbstractLogic : ILogic {

    fun launch(block: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(block = block)
}