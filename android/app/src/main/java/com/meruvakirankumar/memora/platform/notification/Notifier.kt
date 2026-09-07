package com.meruvakirankumar.memora.platform.notification

/** Platform contract for posting reminder notifications. Implemented in Stage 6. */
interface Notifier {
    fun ensureChannel()
    fun show(id: Int, title: String, body: String)
    fun cancel(id: Int)
}
