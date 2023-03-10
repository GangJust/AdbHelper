package res

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.Density

object IconRes {
    val windowMinimized = readIconPainter("icon/ic_window_minimized.svg")
    val windowClosed = readIconPainter("icon/ic_window_closed.svg")
    val phone = readIconPainter("icon/ic_phone.svg")
    val check = readIconPainter("icon/ic_check.svg")
    val screenshot = readIconPainter("icon/ic_screenshot.svg")
    val wifi = readIconPainter("icon/ic_wifi.svg")
    val browseActivity = readIconPainter("icon/ic_browse_activity.svg")
    val apkDocument = readIconPainter("icon/ic_apk_document.svg")
    val apkInstall = readIconPainter("icon/ic_apk_install.svg")
    val fileSystem = readIconPainter("icon/ic_file_system.svg")
    val storage = readIconPainter("icon/ic_storage.svg")
    val file = readIconPainter("icon/ic_file.svg")
    val folder = readIconPainter("icon/ic_folder.svg")
    val linkFolder = readIconPainter("icon/ic_link_folder.svg")
    val linkFile = readIconPainter("icon/ic_link_file.svg")
    val unknownFile = readIconPainter("icon/ic_unknown_file.svg")
    val contentCopy = readIconPainter("icon/ic_content_copy.svg")
    val push = readIconPainter("icon/ic_push.svg")
    val newFolder = readIconPainter("icon/ic_new_folder.svg")
    val portForward = readIconPainter("icon/ic_port_forward.svg")
    val viewLayout = readIconPainter("icon/ic_view_layout.svg")
    val adb = readIconPainter("icon/ic_adb.svg")
    val exportNotes = readIconPainter("icon/ic_export_notes.svg")
    val delete = readIconPainter("icon/ic_delete.svg")
    val open = readIconPainter("icon/ic_open.svg")
    val fold = readIconPainter("icon/ic_fold.svg")
    val noDelete = readIconPainter("icon/ic_no_delete.svg")
    val notInterested = readIconPainter("icon/ic_not_interested.svg")
    val unknown = readIconPainter("icon/ic_unknown.svg")

    private fun readIconPainter(resourcePath: String): Painter {
        return useResource(resourcePath) {
            loadSvgPainter(it, Density(1f))
        }
    }
}