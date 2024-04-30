package com.github.brucemelo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.net.HttpURLConnection.HTTP_OK
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    ApiData().main(args)
}

class ApiData : CliktCommand(printHelpOnEmptyArgs = true) {
    val apiUrl: String by argument(help = "Api URL")
    val field: String? by option(help = "Field value")

    override fun run() {
        if (apiUrl.isNotBlank()) {
            val resultFetch = fetch(apiUrl)
            saveJsonToFile(resultFetch, field)
            echo("Success!")
        }
    }
}

fun fetch(value: String): JsonElement {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder().uri(URI.create(value)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return when (response.statusCode()) {
        HTTP_OK -> Json.parseToJsonElement(response.body())
        else -> throw IllegalStateException("Unexpected response status code - ${response.statusCode()}.")
    }
}

fun saveJsonToFile(jsonData: JsonElement, field: String?) {
    val jsonToSave: JsonElement = when {
        field.isNullOrBlank() -> jsonData
        jsonData is JsonArray -> jsonData.let { jsonArray ->
            val elementsList = jsonArray.filter { it.jsonObject[field] != null }.map { it.jsonObject[field]!! }
            if (elementsList.isEmpty())
                throw IllegalArgumentException("Field not found in any object of json array.")
            else
                JsonArray(elementsList)
        }
        jsonData is JsonObject -> jsonData[field] ?: throw IllegalArgumentException("Field not found in json object.")
        else -> throw IllegalArgumentException("Invalid JsonElement type.")
    }
    val prettyJsonString = Json { prettyPrint = true }.encodeToString(JsonElement.serializer(), jsonToSave)
    Files.write(Paths.get("result.json"), prettyJsonString.toByteArray())
}