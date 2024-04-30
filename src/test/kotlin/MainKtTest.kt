import com.github.brucemelo.fetch
import com.github.brucemelo.saveJsonToFile
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MainKtTest {

    @Test
    fun testSuccessfulFetch() {
        mockStatic(HttpClient::class.java).use {
            val jsonText = """
                {
                    "key": "value"
                }
            """.trimIndent()
            val expectedJson = Json.parseToJsonElement(jsonText)
            val mockResponse = mock(HttpResponse::class.java) as HttpResponse<*>
            val mockClient = mock(HttpClient::class.java)

            `when`(mockResponse.statusCode()).thenReturn(200)
            `when`(mockResponse.body()).thenReturn(jsonText)
            `when`(mockClient.send(any(HttpRequest::class.java), any(HttpResponse.BodyHandler::class.java)))
                .thenReturn(mockResponse)
            `when`(HttpClient.newHttpClient()).thenReturn(mockClient)

            val result = fetch("http://example.com")

            assertEquals(expectedJson, result)
        }

    }

    @Test
    fun testNonOKStatusCode() {
        mockStatic(HttpClient::class.java).use {
            val mockResponse = mock(HttpResponse::class.java) as HttpResponse<*>
            val mockClient = mock(HttpClient::class.java)

            `when`(mockResponse.statusCode()).thenReturn(400)
            `when`(mockClient.send(any(HttpRequest::class.java), any(HttpResponse.BodyHandler::class.java)))
                .thenReturn(mockResponse)
            `when`(HttpClient.newHttpClient()).thenReturn(mockClient)

            assertThrows(IllegalStateException::class.java) {
                fetch("http://example.com")
            }
        }
    }

    @Test
    fun testSaveJsonObjectWithField() {
        mockStatic(Files::class.java).use { mockedFiles ->
            val jsonData = Json.parseToJsonElement("""{"name": "John", "age": 30}""")
            val path = Paths.get("result.json")
            mockedFiles.`when`<Any> { Files.write(any(Path::class.java), any(ByteArray::class.java)) }.thenReturn(path)
            saveJsonToFile(jsonData, "age")
            mockedFiles.verify { Files.write(any(Path::class.java), any(ByteArray::class.java)) }
        }
    }

    @Test
    fun testSaveJsonObjectWithoutField() {
        val jsonData = Json.parseToJsonElement("""{"name": "John", "age": 30}""")
        assertThrows(IllegalArgumentException::class.java) {
            saveJsonToFile(jsonData, "salary")
        }
    }

    @Test
    fun testSaveJsonArrayWithField() {
        mockStatic(Files::class.java).use { mockedFiles ->
            val jsonData = Json.parseToJsonElement(
                """
                [
                    {"name": "John", "age": 30},
                    {"name": "Jane", "age": 25}
                ]
            """
            )
            val path = Paths.get("result.json")
            mockedFiles.`when`<Any> { Files.write(any(Path::class.java), any(ByteArray::class.java)) }.thenReturn(path)
            saveJsonToFile(jsonData, "age")
            mockedFiles.verify { Files.write(any(Path::class.java), any(ByteArray::class.java)) }
        }
    }

    @Test
    fun testSaveJsonArrayWithoutField() {
        val jsonData = Json.parseToJsonElement(
            """
            [{"name": "John", "age": 30}, {"name": "Jane"}]
            """.trimIndent()
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            saveJsonToFile(jsonData, "salary")
        }

        assertEquals("Field not found in any object of json array.", exception.message)
    }

}