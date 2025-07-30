package nz.coreyh.springchat.common.exception

class ValidationException(
    val errors: Map<String, String>
) : RuntimeException() {

    companion object {
        fun throwValidationException(vararg pairs: Pair<String, String>): Nothing {
            throwValidationException(mapOf(*pairs))
        }

        fun throwValidationException(errors: Map<String, String>): Nothing {
            throw ValidationException(errors)
        }
    }
}