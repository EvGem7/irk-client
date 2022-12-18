package me.evgem.irk.client

import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeoutOrNull
import me.evgem.irk.client.exception.LoginIrkException
import me.evgem.irk.client.internal.di.IrkClientComponent
import me.evgem.irk.client.internal.network.handler.message.MessageHandler
import me.evgem.irk.client.internal.network.handler.message.MessageHandlerFactory
import me.evgem.irk.client.internal.util.orElse
import me.evgem.irk.client.model.User
import me.evgem.irk.client.model.message.NickMessage
import me.evgem.irk.client.model.message.PasswordMessage
import me.evgem.irk.client.model.message.ReplyMessage
import me.evgem.irk.client.model.message.UserMessage
import me.evgem.irk.client.model.message.misc.KnownNumericReply
import me.evgem.irk.client.model.message.misc.isError

class IrkClient(
    workCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    companion object {
        private const val LOGIN_TIMEOUT_MS = 10_000L
    }

    private data class LoginResponse(
        val welcomeMessage: String,
        val motd: String,
        val loggedUsername: String,
    )

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
        val (welcomeMessage, motd, loggedUsername) = login(
            handler = handler,
            nickname = nickname,
            username = username,
            realName = realName,
            password = password,
        )
        return IrkServer(
            messageHandler = handler,
            welcomeMessage = welcomeMessage,
            motd = motd,
            hostname = hostname,
            port = port,
            me = User.fromNick(loggedUsername),
        )
    }

    suspend fun connectServer(
        address: InetSocketAddress,
        nickname: String,
        username: String = nickname,
        realName: String = nickname,
        password: String? = null,
    ): IrkServer {
        val handler = messageHandlerFactory.createMessageHandler(address)
        val (welcomeMessage, motd, loggedUsername) = login(
            handler = handler,
            nickname = nickname,
            username = username,
            realName = realName,
            password = password,
        )
        return IrkServer(
            messageHandler = handler,
            welcomeMessage = welcomeMessage,
            motd = motd,
            hostname = address.hostname,
            port = address.port,
            me = User.fromNick(loggedUsername),
        )
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
    ): LoginResponse {
        password?.let { handler.sendMessage(PasswordMessage(password)) }
        handler.sendMessage(NickMessage(nickname))
        handler.sendMessage(UserMessage(username = username, mode = 0, realName = realName))
        var welcomeMessage = ""
        val motdBuilder = StringBuilder()
        var loggedUsername = ""
        withTimeoutOrNull(LOGIN_TIMEOUT_MS) {
            handler
                .receiveMessages()
                .filterIsInstance<ReplyMessage>()
                .onEach { message ->
                    if (message.numericReply.isError) {
                        throw LoginIrkException(
                            message = message.toString(),
                            welcomeMessage = welcomeMessage,
                            motd = motdBuilder.toString(),
                        )
                    }
                    when (message.numericReply) {
                        KnownNumericReply.RPL_WELCOME -> {
                            welcomeMessage = message.replyStringParams.firstOrNull().orEmpty()
                            loggedUsername = message.target
                        }

                        KnownNumericReply.RPL_MOTDSTART, KnownNumericReply.RPL_MOTD, KnownNumericReply.RPL_ENDOFMOTD -> {
                            message.replyStringParams.firstOrNull()?.let {
                                motdBuilder.append(it)
                                motdBuilder.append('\n')
                            }
                        }

                        else -> Unit

                    }
                }
                .first { it.numericReply == KnownNumericReply.RPL_ENDOFMOTD }
        }.orElse { throw LoginIrkException("Login timeout exceeded.") }
        return LoginResponse(
            welcomeMessage = welcomeMessage,
            motd = motdBuilder.toString(),
            loggedUsername = loggedUsername,
        )
    }
}