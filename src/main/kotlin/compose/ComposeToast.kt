package compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import kotlinx.coroutines.*
import res.ColorRes
import res.TextStyleRes

//Toast控制器
class ComposeToast private constructor() {
    private val toastText = mutableStateOf("")
    private val toastStatus = mutableStateOf(false)
    private var job: Job? = null

    companion object {
        private val instance = ComposeToast()

        fun show(text: String, duration: Long = 2000L, callback: () -> Unit = {}) {
            instance.toastText.value = text
            if (!instance.isShowing) {
                cancel()
                instance.job = GlobalScope.launch {
                    instance.toastStatus.value = true
                    delay(duration)
                    instance.toastStatus.value = false
                    callback.invoke()
                }
            }
        }

        fun cancel() {
            val job = instance.job ?: return
            if (job.isCancelled || job.isCompleted) return
            job.cancel()
        }

        fun defaultToastController(): ComposeToast = instance
    }

    val isShowing
        get() = toastStatus.value && job?.isActive ?: false

    val text
        get() = toastText.value
}

@OptIn(ExperimentalAnimationApi::class)
@Composable //Toast视图容器
fun ComposeToastContainer(
    contentAlignment: Alignment = Alignment.BottomCenter,
    content: @Composable () -> Unit,
) {
    val controller = ComposeToast.defaultToastController()

    Box {
        content()

        BoxWithConstraints(
            contentAlignment = contentAlignment,
            modifier = Modifier.fillMaxSize(),
            content = {
                AnimatedVisibility(
                    visible = controller.isShowing,
                    enter = scaleIn(),
                    exit = scaleOut(),
                    content = {
                        Card(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = ColorRes.icon,
                            modifier = Modifier.padding(vertical = 32.dp),
                            content = {
                                Text(
                                    text = controller.text,
                                    style = TextStyleRes.bodyMedium.copy(color = Color.White),
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                )
                            }
                        )
                    }
                )
            }
        )

    }
}
