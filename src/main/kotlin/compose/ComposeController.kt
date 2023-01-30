package compose

interface ComposeController {
    fun show()

    fun hide()

    val isShowing: Boolean
}