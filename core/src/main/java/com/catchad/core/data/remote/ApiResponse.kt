package com.catchad.core.data.remote

sealed class ApiResponse {
    data class Success(val message: String) : ApiResponse()
    data class Error(val error: String, val code: Int? = null) : ApiResponse()
}