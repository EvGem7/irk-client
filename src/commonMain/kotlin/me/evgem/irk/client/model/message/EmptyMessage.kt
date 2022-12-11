package me.evgem.irk.client.model.message

object EmptyMessage : AbstractMessage(command = "") {

    override fun toString(): String {
        return "EmptyMessage"
    }
}
