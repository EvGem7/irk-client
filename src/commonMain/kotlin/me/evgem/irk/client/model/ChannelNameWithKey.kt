package me.evgem.irk.client.model

data class ChannelNameWithKey(
    val channelName: ChannelName,
    val key: String?,
) {
    constructor(channelName: String, key: String?) : this(ChannelName(channelName), key)
}
