package app.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.Objects

data class LayoutTree(
    var index: String,
    var text: String,
    var resourceId: String,
    var className: String,
    var packageName: String,
    var contentDesc: String,
    var checkable: String,
    var checked: String,
    var clickable: String,
    var enabled: String,
    var focusable: String,
    var focused: String,
    var scrollable: String,
    var longClickable: String,
    var password: String,
    var selected: String,
    var bounds: String,
    var parent: LayoutTree?,
    var children: MutableList<LayoutTree>,

    var isOpen: MutableState<Boolean> = mutableStateOf(true) //是否展开
) {
    override fun toString(): String {
        return "LayoutTree(index=$index, resourceId=$resourceId, class=$className, package=$packageName, children=${children.size})"
    }

    override fun hashCode(): Int {

        return Objects.hash(
            index, text, resourceId, className, packageName,
            contentDesc, checkable, checked, clickable,
            enabled, focusable, focused,
            scrollable, longClickable,
            password, selected, bounds,
        )
    }
}