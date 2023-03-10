package app.model

data class FileModel(
    var name: String,
    var path: String,
    var typePerm: String = "",
    var ownerName: String = "",
    var groupName: String = "",
    var lastModifyTime: String = "",
    var type: FileType = FileType.UNKNOWN,
)

enum class FileType {
    FILE,
    DIR,
    LINK_DIR,
    LINK_FILE,
    UNKNOWN,
}