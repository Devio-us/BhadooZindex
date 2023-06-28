package com.d0st.bhadoozindex

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import java.util.concurrent.CountDownLatch

class Utils {
//    class PauseInterceptor : Interceptor {
//        var isPaused = false
//        fun pause() {
//            isPaused = true
//        }
//        fun resume() {
//            isPaused = false
//        }
//        override fun intercept(chain: Interceptor.Chain): Response {
//            if (isPaused) {
//                // Return an empty response to pause the download
//                return Response.Builder()
//                    .code(503) // Service Unavailable
//                    .request(chain.request())
//                    .protocol(Protocol.get(chain.request().url.scheme)) // Obtain the Protocol object
//                    .build()
//            }
//            return chain.proceed(chain.request())
//        }
//    }

    class PauseInterceptor(private val latch: CountDownLatch) : Interceptor {
        var isPaused = false
        fun pause() {
            isPaused = true
        }
        fun resume() {
            isPaused = false
            latch.countDown()
        }
        override fun intercept(chain: Interceptor.Chain): Response {
            if (isPaused) {
                try {
                    latch.await() // Wait until resumed
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            return chain.proceed(chain.request())
        }
    }
}