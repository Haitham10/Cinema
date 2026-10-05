package com.example.cinema.data.local.image

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class InternalFavouriteImageStore(
    private val directory: File,
    private val client: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FavouriteImageStore {

    private val mutex = Mutex()

    override suspend fun saveImages(
        movieId: Int,
        posterUrl: String?,
        backdropUrl: String?
    ): FavouriteImagePaths = withContext(ioDispatcher) {
        mutex.withLock {
            val movieDirectory = File(directory, movieId.toString())

            val posterPath = downloadImage(
                url = posterUrl,
                movieDirectory = movieDirectory,
                fileName = "poster.image"
            )

            val backdropPath = downloadImage(
                url = backdropUrl,
                movieDirectory = movieDirectory,
                fileName = "backdrop.image"
            )

            FavouriteImagePaths(
                localPosterPath = posterPath,
                localBackdropPath = backdropPath
            )
        }
    }

    override suspend fun deleteImages(movieId: Int) {
        withContext(ioDispatcher) {
            mutex.withLock {
                coroutineContext.ensureActive()

                val movieDirectory = File(directory, movieId.toString())

                if (
                    movieDirectory.exists() &&
                    !movieDirectory.deleteRecursively()
                ) {
                    throw IOException("Unable to delete movie images")
                }
            }
        }
    }

    private suspend fun downloadImage(
        url: String?,
        movieDirectory: File,
        fileName: String
    ): String? {
        coroutineContext.ensureActive()

        val imageUrl = url?.toHttpUrlOrNull() ?: return null
        if (!imageUrl.isHttps) return null

        var temporaryFile: File? = null

        return try {
            if (
                !movieDirectory.isDirectory &&
                !movieDirectory.mkdirs()
            ) {
                throw IOException("Unable to create image directory")
            }

            val targetFile = File(movieDirectory, fileName)

            val pendingFile = File.createTempFile(
                "download_",
                ".tmp",
                movieDirectory
            )
            temporaryFile = pendingFile

            val request = Request.Builder()
                .url(imageUrl)
                .build()

            val call = client.newCall(request)
            call.timeout().timeout(30, TimeUnit.SECONDS)

            call.execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Image request failed: ${response.code}")
                }

                val body = response.body
                    ?: throw IOException("Image response has no body")

                if (body.contentType()?.type != "image") {
                    throw IOException("Response is not an image")
                }

                body.byteStream().use { input ->
                    pendingFile.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                        while (true) {
                            coroutineContext.ensureActive()

                            val count = input.read(buffer)
                            if (count == -1) break

                            output.write(buffer, 0, count)
                        }
                    }
                }

                coroutineContext.ensureActive()

                if (pendingFile.length() == 0L) {
                    throw IOException("Downloaded image is empty")
                }

                if (!pendingFile.renameTo(targetFile)) {
                    throw IOException("Unable to move downloaded image")
                }

                targetFile.absolutePath
            }
        } catch (exception: IOException) {
            coroutineContext.ensureActive()
            null
        } finally {
            temporaryFile?.delete()
        }
    }
}