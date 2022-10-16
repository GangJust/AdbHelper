package state

import androidx.compose.runtime.mutableStateOf
import config.AppConfig

/**
 * @Author: Gang
 * @Date: 2022/10/7 22:02
 * @Description:
 */
class SettingState(private val state: AppState) {
    val config = state.config

    var showOnSaveTips = mutableStateOf(false)

    var onSaveTips = mutableStateOf("")

    var onSaveCallback: ((Boolean) -> Unit)? = null

    fun saveConfig(appConfig: AppConfig) {
        config.saveSettingConfig(appConfig.settingConfig)
    }
}