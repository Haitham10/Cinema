package com.example.cinema.data.local.image

import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InternalFavouriteImageStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val posterUrl = "https://example.com/poster.jpg"
    private val backdropUrl = "https://example.com/backdrop.jpg"

    @Test
    fun saveImages_success_writesBothFiles() = runTest {
        val posterBytes = byteArrayOf(1, 2, 3)
        val backdropBytes = byteArrayOf(4, 5, 6)

        val store = createStore(
            dispatcher = StandardTestDispatcher(testScheduler)
        ) { request ->
            val bytes = if (request.url.encodedPath == "/poster.jpg") {
                posterBytes
            } else {
                backdropBytes
            }

            imageResponse(request, bytes)
        }

        val result = store.saveImages(
            movieId = 42,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl
        )

        val posterFile = File(checkNotNull(result.localPosterPath))
        val backdropFile = File(checkNotNull(result.localBackdropPath))

        assertTrue(posterFile.isFile)
        assertTrue(backdropFile.isFile)

        assertArrayEquals(posterBytes, posterFile.readBytes())
        assertArrayEquals(backdropBytes, backdropFile.readBytes())

        assertNoTemporaryFiles()
    }

    @Test
    fun saveImages_posterFails_backdropStillSaved() = runTest {
        val backdropBytes = byteArrayOf(4, 5, 6)

        val store = createStore(
            dispatcher = StandardTestDispatcher(testScheduler)
        ) { request ->
            if (request.url.encodedPath == "/poster.jpg") {
                imageResponse(
                    request = request,
                    bytes = byteArrayOf(),
                    code = 404
                )
            } else {
                imageResponse(request, backdropBytes)
            }
        }

        val result = store.saveImages(
            movieId = 42,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl
        )

        assertNull(result.localPosterPath)

        val backdropFile = File(checkNotNull(result.localBackdropPath))
        assertArrayEquals(backdropBytes, backdropFile.readBytes())

        assertNoTemporaryFiles()
    }

    @Test
    fun saveImages_missingOrInvalidUrls_doesNotSendRequests() = runTest {
        var requestCount = 0

        val store = createStore(
            dispatcher = StandardTestDispatcher(testScheduler)
        ) { request ->
            requestCount++
            imageResponse(request, byteArrayOf(1))
        }

        val missingResult = store.saveImages(
            movieId = 42,
            posterUrl = null,
            backdropUrl = ""
        )

        val invalidResult = store.saveImages(
            movieId = 43,
            posterUrl = "not-a-url",
            backdropUrl = "http://example.com/backdrop.jpg"
        )

        assertEquals(FavouriteImagePaths(), missingResult)
        assertEquals(FavouriteImagePaths(), invalidResult)
        assertEquals(0, requestCount)
    }

    @Test
    fun saveImages_requestFails_removesTemporaryFiles() = runTest {
        val store = createStore(
            dispatcher = StandardTestDispatcher(testScheduler)
        ) {
            throw IOException("Connection failed")
        }

        val result = store.saveImages(
            movieId = 42,
            posterUrl = posterUrl,
            backdropUrl = null
        )

        assertEquals(FavouriteImagePaths(), result)

        val remainingFiles = temporaryFolder.root
            .walkTopDown()
            .filter { it.isFile }
            .toList()

        assertTrue(remainingFiles.isEmpty())
    }

    @Test
    fun deleteImages_removesOnlySelectedMovie() = runTest {
        val store = createStore(
            dispatcher = StandardTestDispatcher(testScheduler)
        ) { request ->
            imageResponse(request, byteArrayOf(1, 2, 3))
        }

        val firstMovie = store.saveImages(
            movieId = 42,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl
        )

        val secondMovie = store.saveImages(
            movieId = 99,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl
        )

        store.deleteImages(movieId = 42)

        assertFalse(File(checkNotNull(firstMovie.localPosterPath)).exists())
        assertFalse(File(checkNotNull(firstMovie.localBackdropPath)).exists())

        assertTrue(File(checkNotNull(secondMovie.localPosterPath)).isFile)
        assertTrue(File(checkNotNull(secondMovie.localBackdropPath)).isFile)

        // Deleting an already deleted movie is harmless.
        store.deleteImages(movieId = 42)
    }

    @Test
    fun saveImages_cancellationIsPropagated_andTemporaryFileRemoved() = runTest {
        val cancellation = CancellationException("Download cancelled")

        val store = createStore(
            dispatcher = StandardTestDispatcher(testScheduler)
        ) {
            throw cancellation
        }

        try {
            store.saveImages(
                movieId = 42,
                posterUrl = posterUrl,
                backdropUrl = null
            )

            fail("Expected CancellationException")
        } catch (exception: CancellationException) {
            assertEquals(cancellation.message, exception.message)
        }

        val remainingFiles = temporaryFolder.root
            .walkTopDown()
            .filter { it.isFile }
            .toList()

        assertTrue(remainingFiles.isEmpty())
    }

    private fun createStore(
        dispatcher: CoroutineDispatcher,
        respond: (Request) -> Response
    ): InternalFavouriteImageStore {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                respond(chain.request())
            }
            .build()

        return InternalFavouriteImageStore(
            directory = temporaryFolder.root,
            client = client,
            ioDispatcher = dispatcher
        )
    }

    private fun imageResponse(
        request: Request,
        bytes: ByteArray,
        code: Int = 200
    ): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 200) "OK" else "Failed")
            .body(
                bytes.toResponseBody("image/jpeg".toMediaType())
            )
            .build()
    }

    private fun assertNoTemporaryFiles() {
        val temporaryFiles = temporaryFolder.root
            .walkTopDown()
            .filter { it.isFile && it.extension == "tmp" }
            .toList()

        assertTrue(temporaryFiles.isEmpty())
    }
}