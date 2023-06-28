package com.d0st.bhadoozindex

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response

class Utils {
    class PauseInterceptor : Interceptor {
        var isPaused = false
        fun pause() {
            isPaused = true
        }
        fun resume() {
            isPaused = false
        }
        override fun intercept(chain: Interceptor.Chain): Response {
            if (isPaused) {
                // Return an empty response to pause the download
                return Response.Builder()
                    .code(503) // Service Unavailable
                    .request(chain.request())
                    .protocol(Protocol.get(chain.request().url.scheme)) // Obtain the Protocol object
                    .build()
            }
            return chain.proceed(chain.request())
        }
    }

}