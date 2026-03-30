package com.anddd.nevera.core.network

import com.anddd.nevera.core.common.ApiResponse
import com.anddd.nevera.core.common.ApiResult
import com.anddd.nevera.core.common.NetworkError
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend inline fun <T> apiCall(
    crossinline block: suspend () -> ApiResponse<T>?
): ApiResult<T> {
    return try {
        val body = block() ?: return ApiResult.Error(
            NetworkError.UnknownError(message = "Empty response body")
        )

        body.error?.let { error ->
            return ApiResult.Error(
                NetworkError.HttpError(
                    code = error.code ?: -1,
                    message = error.message ?: "Empty Message"
                )
            )
        }

        val result = body.result
        if (result != null) {
            ApiResult.Success(result)
        } else {
            ApiResult.Error(
                NetworkError.UnknownError(message = "Empty result")
            )
        }
    } catch (e: CancellationException) {
        // 코루틴 취소 재전파
        throw e
    } catch (e: SocketTimeoutException) {
        ApiResult.Error(
            NetworkError.TimeoutError()
        )
    } catch (e: HttpException) {
        ApiResult.Error(
            NetworkError.HttpError(
                code = e.code(),
                message = e.message()
            )
        )
    } catch (e: IOException) {
        ApiResult.Error(
            NetworkError.NetworkConnectionError()
        )
    } catch (t: Throwable) {
        ApiResult.Error(
            NetworkError.UnknownError(
                message = t.message,
                throwable = t
            )
        )
    }
}
