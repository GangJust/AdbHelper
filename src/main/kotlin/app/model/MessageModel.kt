package app.model

import java.util.Date

data class MessageModel(
    val msgType: Int, //0:系统消息, 1:用户消息
    val message: String,
    val senderTime: Date,
)