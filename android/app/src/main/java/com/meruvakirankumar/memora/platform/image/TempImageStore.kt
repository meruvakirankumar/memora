package com.meruvakirankumar.memora.platform.image

/**
 * Owns the lifecycle of captured images. Enforces the V1 privacy rule: images are
 * temporary inputs to extraction and never become permanent application data.
 */
interface TempImageStore {
    /** A content Uri (as string) the camera can write a new full-resolution capture into. */
    suspend fun newCaptureUri(): String

    /** Copies an external image (e.g. from the photo picker) into private temp storage. */
    suspend fun importImage(sourceUri: String): String

    /** Deletes a single temporary image once it is confirmed or discarded. */
    suspend fun delete(uri: String)

    /** Removes any temporary images left behind. */
    suspend fun clear()
}
