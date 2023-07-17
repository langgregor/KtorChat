package ktorchat.common

import io.ktor.server.plugins.requestvalidation.*


enum class AllowedUserNameCharacters(val description: String, val test: (String) -> Boolean) {
    ALL("all", { true }),
    ALPHANUMERIC("alphanumeric", { s -> s.all { it.isLetterOrDigit() } }),
    ALPHA("alphabetical", { s -> s.all { it.isLetter() } }),
}

fun RequestValidationConfig.validateUserData(
    maxUserNameLength: Int = 10,
    minUserNameLength: Int = 3,
    allowedUserNameCharacters: AllowedUserNameCharacters = AllowedUserNameCharacters.ALPHANUMERIC
) {
    this.validate<UserData> {
        val username = it.username

        if (username.length < minUserNameLength || username.length > maxUserNameLength) {
            return@validate ValidationResult.Invalid("Username has to be between $minUserNameLength and $maxUserNameLength characters!")
        }

        if (allowedUserNameCharacters.test(username)) {
            return@validate ValidationResult.Invalid("Username can only contain ${allowedUserNameCharacters.description} characters!")
        }

        return@validate ValidationResult.Valid
    }
}

fun RequestValidationConfig.validateMessageData(maxMessageLength: Int = 256) {
    this.validate<MessageData> {
        val message = it.message
        if (message.length > 256) {
            ValidationResult.Invalid("Message cannot be longer than $maxMessageLength characters!")
        }

        ValidationResult.Valid
    }
}

