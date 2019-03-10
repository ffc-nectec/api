package ffc.airsync.api.filter.cache

import com.nhaarman.mockitokotlin2.mock
import org.amshove.kluent.When
import org.amshove.kluent.`should equal`
import org.amshove.kluent.calling
import org.amshove.kluent.itReturns
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import javax.ws.rs.core.CacheControl

@RunWith(Parameterized::class)
class CacheCombineWithRequestTest(
    val setting: Cache,
    val request: CacheControl,
    val expectResponse: CacheControl
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun cacheResult() = listOf(
            arrayOf(annotation(), cacheControl(), cacheControl()),
            arrayOf(annotation(maxAge = 3600), cacheControl(), cacheControl(maxAge = 3600)),
            arrayOf(annotation(maxAge = 3600), cacheControl(maxAge = 7200), cacheControl(maxAge = 3600)),
            arrayOf(annotation(maxAge = 3600), cacheControl(maxAge = 100), cacheControl(maxAge = 100)),
            arrayOf(annotation(maxAge = 3600), cacheControl(maxAge = 0), cacheControl(maxAge = 0)),
            arrayOf(annotation(maxAge = 3600), cacheControl(maxAge = 0), cacheControl(maxAge = 0)),
            arrayOf(annotation(maxAge = 3600), cacheControl(private = true), cacheControl(private = true, maxAge = 3600)),
            arrayOf(annotation(private = true, maxAge = 3600), cacheControl(), cacheControl(private = true, maxAge = 3600)),
            arrayOf(annotation(noStore = true), cacheControl(maxAge = 300), cacheControl(noStore = true)),
            arrayOf(annotation(maxAge = 3600), cacheControl(noStore = true), cacheControl(noStore = true)),
            arrayOf(annotation(noCache = true), cacheControl(maxAge = 300), cacheControl(noCache = true)),
            arrayOf(
                annotation(noCache = true, maxAge = 3600),
                cacheControl(maxAge = 300),
                cacheControl(noCache = true, maxAge = 300)),
            arrayOf(
                annotation(maxAge = 300),
                cacheControl(noCache = true, maxAge = 300),
                cacheControl(noCache = true, maxAge = 300)),
            arrayOf(
                annotation(maxAge = 300),
                cacheControl(noCache = true, mustRevalidate = true),
                cacheControl(noCache = true, mustRevalidate = true, maxAge = 300)),
            arrayOf(
                annotation(noCache = true, mustRevalidate = true, maxAge = 300),
                cacheControl(maxAge = 150),
                cacheControl(noCache = true, mustRevalidate = true, maxAge = 150))
        )

        fun annotation(
            maxAge: Int = -1,
            private: Boolean = false,
            noStore: Boolean = false,
            noCache: Boolean = false,
            mustRevalidate: Boolean = false
        ): Cache {
            return mock {
                When calling it.maxAge itReturns maxAge
                When calling it.isPrivate itReturns private
                When calling it.noStore itReturns noStore
                When calling it.noCache itReturns noCache
                When calling it.mustRevalidate itReturns mustRevalidate
            }
        }

        fun cacheControl(
            maxAge: Int = -1,
            private: Boolean = false,
            noStore: Boolean = false,
            noCache: Boolean = false,
            mustRevalidate: Boolean = false,
            noTransform: Boolean = false
        ): CacheControl {
            return CacheControl().apply {
                this.maxAge = maxAge
                isPrivate = private
                isNoStore = noStore
                isNoCache = noCache
                isMustRevalidate = mustRevalidate
                isNoTransform = noTransform
            }
        }
    }

    @Test
    fun cache() {
        setting.combineWith(request) `should equal` expectResponse
    }
}
