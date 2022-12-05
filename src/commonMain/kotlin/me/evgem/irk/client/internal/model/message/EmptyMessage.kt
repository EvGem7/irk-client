package me.evgem.irk.client.internal.model.message

internal object EmptyMessage : AbstractMessage(command = "") {

    override fun toString(): String {
        return "EmptyMessage"
    }
}
