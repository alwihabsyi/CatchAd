package com.catchad.core.data.util

sealed class Response {
    data object Success : Response()
    data object Error : Response()
}