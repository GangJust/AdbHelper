package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import state.SettingState
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @Author: Gang
 * @Date: 2022/10/7 22:00
 * @Description:
 */

@Composable
fun SettingUI(state: SettingState) {
    Box {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            SettingConfigPort(state)
        }
        if (state.showOnSaveTips.value) {
            Snackbar(modifier = Modifier.align(Alignment.TopCenter)) { Text(state.onSaveTips.value) }

            CoroutineScope(EmptyCoroutineContext).launch {
                delay(2000L)
                state.showOnSaveTips.value = false
            }
        }
    }
}

@Composable  //设置项部分
fun SettingConfigPort(state: SettingState) {
    val androidSdkPath = mutableStateOf(state.config.settingConfig.androidSdkPath)
    val androidAdbPath = mutableStateOf(state.config.settingConfig.androidAdbPath)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "常规设置",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        ChoicePathItem(androidSdkPath, "SDK路径", "请填写 Android SDK 路径...")
        ChoicePathItem(androidAdbPath, "ADB路径", "请填写 ADB 路径...")
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) {
            Button(
                onClick = {
                    if (androidSdkPath.value.isEmpty() || androidAdbPath.value.isEmpty()) {
                        state.onSaveCallback?.invoke(false)
                        state.showOnSaveTips.value = true
                        state.onSaveTips.value = "请填写正确路径."
                        return@Button
                    }

                    //sdk
                    val tools = File(androidSdkPath.value, "tools")
                    val platformTools = File(androidSdkPath.value, "platform-tools")

                    //adb
                    val adb = File(androidAdbPath.value).listFiles { _, name -> name.contains("adb") } ?: arrayOf()

                    if (tools.exists() && platformTools.exists() && adb.isNotEmpty()) {
                        if (tools.isDirectory && platformTools.isDirectory && adb.first().isFile) {
                            state.config.settingConfig.androidSdkPath = androidSdkPath.value
                            state.config.settingConfig.androidAdbPath = androidAdbPath.value
                            state.saveConfig(state.config)
                            state.onSaveCallback?.invoke(true)
                            return@Button
                        }
                    }

                    state.onSaveCallback?.invoke(false)
                    state.showOnSaveTips.value = true
                    state.onSaveTips.value = "请检查SDK、ADB路径是否正确."
                },
            ) {
                Text("保存")
            }
        }
    }
}

@Composable //路径设置
fun ChoicePathItem(
    value: MutableState<String>,
    title: String,
    hint: String,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value.value,
            onValueChange = { value.value = it },
            textStyle = TextStyle.Default,
            placeholder = { Text(hint, style = TextStyle.Default) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}