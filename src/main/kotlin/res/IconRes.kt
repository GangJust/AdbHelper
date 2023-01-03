package res

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.Density

object IconRes {
    val windowMinimized = readIconPainter("icon/ic_window_minimized.svg")
    val windowClosed = readIconPainter("icon/ic_window_closed.svg")
    val phone = readIconPainter("icon/ic_phone.svg")
    val browseActivity = readIconPainter("icon/ic_browse_activity.svg")
    val apkDocument = readIconPainter("icon/ic_apk_document.svg")
    val apkInstall = readIconPainter("icon/ic_apk_install.svg")
    val adb = readIconPainter("icon/ic_adb.svg")
    val exportNotes = readIconPainter("icon/ic_export_notes.svg")
    val delete = readIconPainter("icon/ic_delete.svg")
    val noDelete = readIconPainter("icon/ic_no_delete.svg")
    val notInterested = readIconPainter("icon/ic_not_interested.svg")
    val unknown = readIconPainter("icon/ic_unknown.svg")

    private fun readIconPainter(resourcePath: String): Painter {
        return useResource(resourcePath) {
            loadSvgPainter(it, Density(1f))
        }
    }
}