// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.state.pages.FileSystemState
import app.view.HomeUI
import app.view.pages.CustomScaffold
import app.view.pages.ErrorPage
import base.mvvm.*
import compose.*
import res.ColorRes
import res.TextStyleRes
import utils.ShellUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


class AppLogic : AbstractLogic() {
    override fun dispose() {
    }
}

class AppState : AbstractState<AppLogic>() {
    override fun createLogic() = AppLogic()
    private val ahConfig = File("ah_config.properties")

    val hasConfig = ahConfig.exists()

    fun adbExists(): Boolean {
        val adbDir = readConfig.getProperty("adb_dir", "")
        val adbDirFile = File(adbDir)
        val hasAdb = adbDirFile.list { _, name -> name.contains("^adb(\\.exe)?\$".toRegex()) }?.isNotEmpty() ?: false

        //如果Adb文件存在, 则设置Shell工作目录
        return if (hasAdb) {
            ShellUtils.workDirectory = adbDirFile
            true
        } else {
            false
        }
    }

    // 保存配置文件
    fun storeConfig(properties: Properties) {
        properties.store(FileOutputStream(ahConfig), "This is the AdbHelper configuration file.")
    }

    //读取配置文件
    val readConfig = Properties().apply {
        try {
            load(FileInputStream(ahConfig))
        } catch (_: Exception) {

        }
    }
}

class App(private var application: ApplicationScope) : AbstractView<AppState>() {
    override fun createState() = AppState()

    @Composable
    override fun viewCompose() {
        if (!state.hasConfig || !state.adbExists()) {
            ConfigWindow()
        } else {
            HomeWindow()
        }
    }

    @Composable
    private fun ConfigWindow() {
        val windowState = rememberWindowState(
            size = DpSize(380.dp, 280.dp),
            position = WindowPosition.Aligned(Alignment.Center),
        )
        val adbDir = mutableStateOf("tools")

        Window(
            onCloseRequest = { application.exitApplication() },
            title = "AdbHelper",
            resizable = false,
            undecorated = true,
            transparent = true,
            state = windowState,
            content = {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp,
                    border = BorderStroke(1.dp, ColorRes.icon.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(12.dp),
                    content = {
                        ComposeToastContainer {
                            CustomScaffold(
                                application = application,
                                windowScope = this,
                                windowState = windowState,
                                title = {
                                    Text(
                                        text = "AdbHelper",
                                        style = TextStyleRes.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                },
                                content = {
                                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                        Text(
                                            text = "欢迎使用!",
                                            style = TextStyleRes.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                        Text(
                                            text = "您需要进行以下基本配置才能正常运行!",
                                            style = TextStyleRes.bodySmall,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                        BoxWithConstraints(
                                            modifier = Modifier
                                                .padding(vertical = 8.dp)
                                                .border(width = 1.dp, color = ColorRes.divider, shape = RoundedCornerShape(8.dp)),
                                            content = {
                                                CustomTextField(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                                    value = adbDir,
                                                    hintText = "请输入可执行adb所在路径 (默认tools即可)",
                                                    textStyle = TextStyleRes.bodyMedium,
                                                    fillMaxWidth = true,
                                                    onValueChange = { adbDir.value = it },
                                                )
                                            }
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.End,
                                            content = {
                                                TextButton(
                                                    onClick = {
                                                        if (adbDir.value.isBlank()) return@TextButton

                                                        val adbDirFile = File(adbDir.value)
                                                        if (!adbDirFile.exists() || adbDirFile.isFile) {
                                                            ComposeToast.show("请输入正确的目录!")
                                                            return@TextButton
                                                        }

                                                        val adbFile =
                                                            adbDirFile.listFiles { _, name -> name.contains("^adb(\\.exe)?\$".toRegex()) }
                                                        if (adbFile == null || adbFile.isEmpty()) {
                                                            ComposeToast.show("可执行adb不存在!")
                                                            return@TextButton
                                                        }

                                                        //保存配置
                                                        val properties = state.readConfig
                                                        properties["adb_dir"] = adbDirFile.absolutePath
                                                        state.storeConfig(properties)
                                                        ComposeToast.show("保存成功, 请重新运行!", duration = 2000L) {
                                                            application.exitApplication()
                                                        }
                                                    },
                                                    content = {
                                                        Text(
                                                            text = "完成",
                                                            style = TextStyleRes.bodyMediumSurface,
                                                        )
                                                    }
                                                )
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    },
                )
            },
        )
    }

    @Composable
    private fun HomeWindow() {
        val windowState = rememberWindowState(
            size = DpSize(860.dp, 600.dp),
            position = WindowPosition.Aligned(Alignment.Center),
        )

        Window(
            onCloseRequest = {
                StateManager.clearStateMaps()
                application.exitApplication()
            },
            title = "AdbHelper",
            resizable = false,
            undecorated = true,
            transparent = true,
            state = windowState,
            content = {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(
                        primary = ColorRes.primary,
                        onSurface = ColorRes.onSurface,
                    ),
                    content = {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = 4.dp,
                            border = BorderStroke(1.dp, ColorRes.icon.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(12.dp),
                            content = {
                                ComposeToastContainer {
                                    ComposeLoadingContainer(windowScope = this) {
                                        ComposeDialogContainer {
                                            ViewCompose {
                                                try {
                                                    HomeUI(
                                                        application = application,
                                                        windowScope = this,
                                                        windowState = windowState,
                                                    )
                                                } catch (e: Exception) {
                                                    ErrorPage(
                                                        application = application,
                                                        windowScope = this,
                                                        windowState = windowState,
                                                        e = e,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                )
            }
        )
    }
}

fun main() {
    application {
        ViewCompose {
            App(this)
        }
    }
}