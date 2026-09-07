package com.meruvakirankumar.memora.core.error

/** Application-wide error model. Feature layers map failures into these types. */
sealed class AppError(open val cause: Throwable? = null) {

    /** Local persistence (Room) failure. */
    data class Storage(override val cause: Throwable? = null) : AppError(cause)

    /** OCR / text recognition failure. */
    data class Recognition(override val cause: Throwable? = null) : AppError(cause)

    /** The captured input could not be understood confidently. */
    data object NotUnderstood : AppError()

    /** A required permission was denied. */
    data class PermissionDenied(val permission: String) : AppError()

    /** Anything not otherwise classified. */
    data class Unexpected(override val cause: Throwable? = null) : AppError(cause)
}
