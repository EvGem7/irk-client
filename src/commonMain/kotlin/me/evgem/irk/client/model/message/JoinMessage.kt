package me.evgem.irk.client.model.message

class JoinMessage internal constructor(
    val channels: List<String>,
    val keys: List<String>,
) : AbstractMessage(
    command = "JOIN",
    middleParams = listOfNotNull(
        pack(channels),
        pack(keys)
    ),
    trailingParam = null,
) {

    companion object {
        private fun pack(names: List<String>): String? {
            return names.takeIf { it.isNotEmpty() }?.joinToString(separator = ",")
        }
    }

    override fun toString(): String {
        return "JoinMessage(channels=$channels, keys=$keys)"
    }
}