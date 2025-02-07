package com.catchad.core.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.io.IOException

class ApiResponseAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): ApiResponse {
        val jsonObject = reader.readJsonValue() as Map<*, *>

        val message = jsonObject["message"] as? String
        if (message != null) {
            return ApiResponse.Success(message)
        }

        val error = jsonObject["error"] as? String
        val code = jsonObject["code"] as? Int
        if (error != null) {
            return ApiResponse.Error(error, code)
        }

        throw IOException("Invalid response format: missing 'message' or 'error'")
    }

    @ToJson
    fun toJson(response: ApiResponse): Map<String, Any?> {
        return when (response) {
            is ApiResponse.Success -> mapOf("message" to response.message)
            is ApiResponse.Error -> mapOf("error" to response.error, "code" to response.code)
        }
    }
}
