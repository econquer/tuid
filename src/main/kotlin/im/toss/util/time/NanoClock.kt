package im.toss.util.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * NanoClock
 *
 * nanoseconds 해상도를 가지는 시계
 */
class NanoClock(private val zone: ZoneId = ZoneId.systemDefault()): Clock() {
    companion object {
        // 동기화된 시간 샘플 10개 중, 오차가 가장 적을것으로 예상되는 1개를 선택한다
        private val baseTime = (1..10).map { BaseTime.sync() }.minBy { it.elapsed }!!
        private const val NANOS_PER_MILLI = 1_000_000L
        private const val MILLIS_PER_SEC = 1_000L

        val DEFAULT = NanoClock()
    }

    override fun withZone(zone: ZoneId): Clock {
        if (zone == this.zone) {
            return this
        }
        return NanoClock(zone)
    }

    override fun getZone(): ZoneId = zone

    override fun instant(): Instant {
        val nanosSinceInit = baseTime.nanoTime()
        val epochMillis = nanosSinceInit / NANOS_PER_MILLI + baseTime.millis
        val epochSeconds = epochMillis / MILLIS_PER_SEC
        val nanoAdjustment = epochMillis % MILLIS_PER_SEC * NANOS_PER_MILLI + nanosSinceInit % NANOS_PER_MILLI
        return Instant.ofEpochSecond(epochSeconds, nanoAdjustment)
    }

    private data class BaseTime(
        val millis: Long,
        private val nanos: Long,
        val elapsed: Long
    ) {
        fun nanoTime() = System.nanoTime() - nanos

        companion object {
            fun sync(): BaseTime {
                while (true) {
                    /* 시간 오차를 줄이기 위해 milliseconds 단위의 시간이 변경되는 시점을 측정한다 */
                    val startNanos = System.nanoTime()
                    val prevMillis = System.currentTimeMillis()
                    val millis = System.currentTimeMillis()
                    val nanos = System.nanoTime()
                    val elapsedNanos = nanos - startNanos
                    if (millis - prevMillis == 1L) {
                        return BaseTime(millis, nanos - elapsedNanos, elapsedNanos)
                    }
                }
            }
        }
    }
}


