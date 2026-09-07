package com.meruvakirankumar.memora.platform.image

/**
 * Stores captured images temporarily for processing, then deletes them.
 * Enforces the V1 privacy rule: Memora never permanently stores images.
 */
interface TempImageStore {
    /** Persist bytes to a private temporary file and return its uri. */
    suspend fun save(bytes: ByteArray): String

    /** Delete a temporary image once extraction is complete. */
    suspend fun delete(uri: String)

    /** Remove any temporary images left behind. */
    suspend fun clear()
}
