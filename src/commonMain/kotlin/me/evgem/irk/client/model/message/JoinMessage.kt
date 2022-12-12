package me.evgem.irk.client.model.message

class JoinMessage internal constructor(
    val channels: List<String>,
    val keys: List<String>,
    prefix: String? = null,
) : AbstractMessage(
    command = "JOIN",
    middleParams = listOfNotNull(
        pack(channels),
        pack(keys)
    ),
    trailingParam = null,
    prefix = prefix,
) {

    companion object {
        private fun pack(names: List<String>): String? {
            return names.takeIf { it.isNotEmpty() }?.joinToString(separator = ",")
        }
    }

    override fun toString(): String {
        return "JoinMessage(channels=$channels, keys=$keys, user=$user)"
    }
}