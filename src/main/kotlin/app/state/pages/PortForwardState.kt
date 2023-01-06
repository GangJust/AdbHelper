package app.state.pages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import app.logic.pages.PortForwardLogic
import app.model.MessageModel
import app.state.HomeState
import app.view.HomeUI
import base.mvvm.AbstractState
import base.mvvm.StateManager
import utils.ShellUtils
import utils.formatAdbCommand
import java.util.*

class PortForwardState : AbstractState<PortForwardLogic>() {
    override fun createLogic() = PortForwardLogic()
    private val homeState = StateManager.findState<HomeState>(HomeUI::class.java)
    private val device = homeState?.currentDevice?.value ?: ""

    val messageHint = mutableStateOf(true)
    private val _messageValue = mutableStateOf("")

    val chatMessageLazyListState = LazyListState()
    val chatMessageList = mutableStateListOf<MessageModel>()

    //用户发送消息
    fun sendMessage(message: String) {
        if (message.isEmpty()) return

        chatMessageList.add(MessageModel(1, message, Date()))
        messageValue = ""

        if (message == "命令列表") { //命令列表
            replyMessage("命令列表\n端口转发: tcp:8080  tcp:8080\n取消转发: remove tcp:8080\n取消所有: remove all\n转发列表: forward list")
        } else if (message.contains(Regex("tcp:(\\d+)\\s+tcp:(\\d+)"))) { //转发端口
            val find = Regex("tcp:(\\d+)").find(message)!!
            val tcp1 = find.groupValues[1]
            val tcp2 = find.next()?.groupValues?.get(1) ?: ""
            if (tcp1.contains(Regex("\\D")) || tcp2.contains(Regex("\\D"))) {
                replyMessage("错误的端口号!")
                return
            }

            launch {
                val command = "adb forward tcp:$tcp1 tcp:$tcp2".formatAdbCommand(device)
                ShellUtils.shell(command) { success, error ->
                    if (error.isNotBlank()) {
                        replyMessage(error.trim())
                        return@shell
                    }
                    if (success.trim().isEmpty() || success.contains(tcp1) || success.contains(tcp2))
                        replyMessage("转发成功!")
                    else
                        replyMessage(success.trim())
                }
            }
        } else if (message.contains(Regex("remove tcp:(\\d+)"))) { //取消转发
            val find = Regex("tcp:(\\d+)").find(message)!!
            val tcp1 = find.groupValues[1]
            if (tcp1.contains(Regex("\\D"))) {
                replyMessage("错误的端口号!")
                return
            }
            launch {
                val command = "adb forward --remove tcp:$tcp1".formatAdbCommand(device)
                ShellUtils.shell(command) { success, error ->
                    if (error.isNotBlank()) {
                        if (error.contains("not found")) {
                            replyMessage("该端口号未被转发!")
                        } else {
                            replyMessage(error.trim())
                        }
                        return@shell
                    }
                    if (success.trim().isEmpty() || success.contains(tcp1))
                        replyMessage("取消成功!")
                    else
                        replyMessage(success.trim())
                }
            }
        } else if (message.contains("remove all")) { //取消所有
            launch {
                val command = "adb forward --remove-all".formatAdbCommand(device)
                ShellUtils.shell(command) { success, error ->
                    if (error.isNotBlank()) {
                        replyMessage(error.trim())
                        return@shell
                    }
                    if (success.trim().isEmpty())
                        replyMessage("所有转发规则已重置!")
                    else
                        replyMessage(success.trim())
                }
            }
        } else if (message.contains("forward list")) { //转发列表
            launch {
                val command = "adb forward --list".formatAdbCommand(device)
                ShellUtils.shell(command) { success, error ->
                    if (error.isNotBlank()) {
                        replyMessage(error.trim())
                        return@shell
                    }
                    if (success.trim().isEmpty())
                        replyMessage("没有转发规则!")
                    else
                        replyMessage(success.replace("$device ", "").trim())
                }
            }
        }
    }

    //系统回复消息
    fun replyMessage(message: String) {
        chatMessageList.add(MessageModel(0, message, Date()))
    }

    var messageValue
        set(value) {
            _messageValue.value = value
            messageHint.value = value.isEmpty()
        }
        get() = _messageValue.value
}