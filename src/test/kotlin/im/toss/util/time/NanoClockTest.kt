package im.toss.util.time

import im.toss.test.equalsTo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.time.Duration
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureNanoTime

internal class NanoClockTest {
    @Test
    fun zoneTest() {
        NanoClock().zone equalsTo ZoneId.systemDefault()
        NanoClock().withZone(ZoneId.systemDefault()).zone equalsTo ZoneId.systemDefault()
        NanoClock().withZone(ZoneId.of("Asia/Seoul")).zone equalsTo ZoneId.of("Asia/Seoul")
        NanoClock().withZone(ZoneId.of("UTC")).zone equalsTo ZoneId.of("UTC")
    }

    @Test
    fun performanceTest() {
        val clockSource = OffsetClockSource(SystemClockSource)
        val clock = NanoClock(clockSource)
        clock.instant()

        val totalElapsedNanos = AtomicLong()
        for(i in 1..1_000_000) {
            if (i % 100_000 == 0) {
                val prev = clock.instant()
                clockSource.offsetMillis += 1500
                val start = System.nanoTime()
                val after = clock.instant()
                val diff = after.nano - prev.nano

                val elapsed = (System.nanoTime() - start) / 1_000_000L
                println("calibration -> $elapsed ms, $prev -> $after -> ${clock.instant()}, $diff")
            }
            totalElapsedNanos.addAndGet(
                measureNanoTime {
                    clock.instant()
                }
            )
        }

        val elapsed = totalElapsedNanos.get() / 1_000_000
        println("$elapsed ms in 1,000,000 times")
    }

    @TestFactory
    fun calibrationTest(): List<DynamicTest> {
        NanoClock(SystemClockSource).instant()

        val allowErrorNanos = 1_000_000L // 1 ms
        return listOf(0L, 100L, 200L, 300L, 500L, 10000L, -100L, -200L, -500L, -10000L)
            .map { offset ->
                dynamicTest("시간이 $offset ms 변경 후 측정하면 오차범위 $allowErrorNanos nano 이하로 보정된다") {
                    val clockSource = OffsetClockSource(SystemClockSource)
                    val clock = NanoClock(clockSource)

                    val before = clock.instant()
                    clockSource.offsetMillis = offset
                    val after = clock.instant()

                    val duration = Duration.between(before, after).toNanos()
                    println("before=$before, fix $offset ms, after=$after, duration=$duration")
                    assertThat(duration).isCloseTo(offset * 1_000_000L, Offset.offset(allowErrorNanos)) // 1ms
                }
            }
    }
}

class OffsetClockSource(private val clockSource: ClockSource): ClockSource by clockSource {
    var offsetMillis: Long = 0L
    override fun realtimeMillis(): Long = clockSource.realtimeMillis() + offsetMillis
}
