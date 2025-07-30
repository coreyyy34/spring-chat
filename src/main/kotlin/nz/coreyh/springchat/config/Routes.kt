package nz.coreyh.springchat.config

object Routes {
    object Auth {
        private const val AUTH_PATH = "/auth"
        const val LOGIN = "$AUTH_PATH/login"
        const val REGISTER = "$AUTH_PATH/register"
        const val LOGOUT = "$AUTH_PATH/logout" }

    object Api {
        const val API_V1_PATH = "/api/v1"

        object History {
            const val CHANNEL = "$API_V1_PATH/history/{channelId}"
        }

        object Presence {
            const val ONLINE_USERS = "$API_V1_PATH/presence/online-users"
        }
    }

    const val WEBSOCKET = "/ws"
    const val CHAT = "/chat"
    const val HOME = "/"
}

object Templates {
    object Auth {
        const val LOGIN = "pages/auth/login"
        const val REGISTER = "pages/auth/register"
    }

    const val CHAT = "pages/chat"
    const val HOME = "pages/home"
}