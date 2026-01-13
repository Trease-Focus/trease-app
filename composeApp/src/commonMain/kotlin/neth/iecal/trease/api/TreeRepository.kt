package neth.iecal.trease.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import neth.iecal.trease.Constants
import neth.iecal.trease.models.TreeData
import neth.iecal.trease.models.TreeResponse

object TreeRepository {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchTrees(): List<TreeData> {
        val response: TreeResponse = client
            .get("${Constants.cdn}/entity_data.json")
            .body()
        return response.entities
    }
}