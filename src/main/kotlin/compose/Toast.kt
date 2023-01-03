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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import res.ColorRes
import res.TextStyleRes

//Toast控制器
class Toast {
    private val toastText = mutableStateOf("")
    private val toastController = mutableStateOf(false)
    private var job: Job? = null

    companion object {
        private val instance = Toast()

        fun show(text: String, duration: Long = 2000) {
            instance.toastText.value = text
            if (!instance.isShowing) {
                instance.job = GlobalScope.launch {
                    instance.toastController.value = true
                    delay(duration)
                    instance.toastController.value = false
                }
            }
        }

        fun cancel() {
            val job = instance.job ?: return
            if (job.isCancelled || job.isCompleted) return
            job.cancel()
        }

        fun defaultToastController(): Toast = instance
    }

    val isShowing
        get() = toastController.value

    val text
        get() = toastText.value
}

@OptIn(ExperimentalAnimationApi::class)
@Composable //Toast视图容器
fun ToastContainer(
    state: Toast = Toast.defaultToastController(),
    contentAlignment: Alignment = Alignment.BottomCenter,
    content: @Composable () -> Unit,
) {
    Box {
        content()

        BoxWithConstraints(
            contentAlignment = contentAlignment,
            modifier = Modifier.fillMaxSize(),
        ) {
            AnimatedVisibility(
                visible = state.isShowing,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                Card(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = ColorRes.icon,
                    modifier = Modifier.padding(vertical = 32.dp),
                ) {
                    Text(
                        text = state.text,
                        style = TextStyleRes.bodyMedium.copy(color = Color.White),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
            }
        }

    }
}
