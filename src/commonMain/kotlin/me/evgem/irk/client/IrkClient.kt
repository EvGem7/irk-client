package me.evgem.irk.client

import io.ktor.network.sockets.SocketAddress
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeoutOrNull
import me.evgem.irk.client.exception.LoginIrkException
import me.evgem.irk.client.internal.di.IrkClientComponent
import me.evgem.irk.client.model.message.NickMessage
import me.evgem.irk.client.model.message.PasswordMessage
import me.evgem.irk.client.model.message.ReplyMessage
import me.evgem.irk.client.model.message.UserMessage
import me.evgem.irk.client.model.message.misc.NumericReply
import me.evgem.irk.client.internal.network.handler.message.MessageHandler
import me.evgem.irk.client.internal.network.handler.message.MessageHandlerFactory
import me.evgem.irk.client.internal.util.orElse

class IrkClient(
    workCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    companion object {
        private const val LOGIN_TIMEOUT_MS = 10_000L
    }

    private val component = IrkClientComponent(workCoroutineDispatcher)
    private val messageHandlerFactory: MessageHandlerFactory = component.messageHandlerFactory()

    suspend fun connectServer(
        hostname: String,
        port: Int,
        nickname: String,
        username: String = nickname,
        realName: String = nickname,
        password: String? = null,
    ): IrkServer {
        val handler = messageHandlerFactory.createMessageHandler(hostname, port)
        val (welcomeMessage, motd) = login(
            handler = handler,
            nickname = nickname,
            username = username,
            realName = realName,
            password = password,
        )
        return IrkServer(messageHandler = handler, welcomeMessage = welcomeMessage, motd = motd)
    }

    suspend fun connectServer(
        address: SocketAddress,
        nickname: String,
        username: String = nickname,
        realName: String = nickname,
        password: String? = null,
    ): IrkServer {
        val handler = messageHandlerFactory.createMessageHandler(address)
        val (welcomeMessage, motd) = login(
            handler = handler,
            nickname = nickname,
            username = username,
            realName = realName,
            password = password,
        )
        return IrkServer(messageHandler = handler, welcomeMessage = welcomeMessage, motd = motd)
    }

    /**
     * @return Pair of welcome and motd strings.
     */
    private suspend fun login(
        handler: MessageHandler,
        nickname: String,
        username: String,
        realName: String,
        password: String?,
    ): Pair<String, String> {
        password?.let { handler.sendMessage(PasswordMessage(password)) }
        handler.sendMessage(NickMessage(nickname))
        handler.sendMessage(UserMessage(user = username, mode = 0, realName = realName))
        var welcomeMessage = ""
        val motdBuilder = StringBuilder()
        withTimeoutOrNull(LOGIN_TIMEOUT_MS) {
            handler
                .receiveMessages()
                .filterIsInstance<ReplyMessage>()
                .onEach { message ->
                    if (message.numericReply.isError) {
                        throw LoginIrkException(
                            message = "code=${message.numericReply.code} ${message.stringReply.orEmpty()}",
                            welcomeMessage = welcomeMessage,
                            motd = motdBuilder.toString(),
                        )
                    }
                    when (message.numericReply) {
                        NumericReply.RPL_WELCOME -> {
                            welcomeMessage = message.stringReply.orEmpty()
                        }

                        NumericReply.RPL_MOTDSTART, NumericReply.RPL_MOTD, NumericReply.RPL_ENDOFMOTD -> {
                            message.stringReply?.let {
                                motdBuilder.append(it)
                                motdBuilder.append('\n')
                            }
                        }

                        else -> Unit

                    }
                }
                .first { it.numericReply == NumericReply.RPL_ENDOFMOTD }
        }.orElse { throw LoginIrkException("Login timeout exceeded.") }
        return welcomeMessage to motdBuilder.toString()
    }
}