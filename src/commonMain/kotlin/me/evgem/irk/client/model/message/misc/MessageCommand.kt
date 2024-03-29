package me.evgem.irk.client.model.message.misc

enum class MessageCommand(val rfcName: String) {
    PASS("PASS"),
    NICK("NICK"),
    USER("USER"),
    OPER("OPER"),
    MODE("MODE"), // There are 2 MODE commands: user mode and channel mode
    SERVICE("SERVICE"),
    QUIT("QUIT"),
    SQUIT("SQUIT"),
    JOIN("JOIN"),
    PART("PART"),
    TOPIC("TOPIC"),
    NAMES("NAMES"),
    LIST("LIST"),
    INVITE("INVITE"),
    KICK("KICK"),
    PRIVMSG("PRIVMSG"),
    NOTICE("NOTICE"),
    MOTD("MOTD"),
    LUSERS("LUSERS"),
    VERSION("VERSION"),
    STATS("STATS"),
    LINKS("LINKS"),
    TIME("TIME"),
    CONNECT("CONNECT"),
    TRACE("TRACE"),
    ADMIN("ADMIN"),
    INFO("INFO"),
    SERVLIST("SERVLIST"),
    SQUERY("SQUERY"),
    WHO("WHO"),
    WHOIS("WHOIS"),
    WHOWAS("WHOWAS"),
    KILL("KILL"),
    PING("PING"),
    PONG("PONG"),
    ERROR("ERROR"),
    AWAY("AWAY"),
    REHASH("REHASH"),
    DIE("DIE"),
    RESTART("RESTART"),
    SUMMON("SUMMON"),
    USERS("USERS"),
    WALLOPS("WALLOPS"),
    USERHOST("USERHOST"),
    ISON("ISON"),
}