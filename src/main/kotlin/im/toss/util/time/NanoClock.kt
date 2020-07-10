package im.toss.util.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

/**
 * NanoClock
 *
 * nanoseconds 해상도를 가지는 시계
 */
class NanoClock(
    private val clockSource: ClockSource = SystemClockSource,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val synchronizedClockSnapshotReference: AtomicReference<SynchronizedClockSnapshot> = AtomicReference(clockSource.synchronizeWithRealtime(10)),
    private val calibrationCounter: AtomicInteger = AtomicInteger()
): Clock() {
    companion object {
        private const val NANOS_PER_MILLI = 1_000_000L
        private const val MILLIS_PER_SEC = 1_000L

        val DEFAULT = NanoClock(SystemClockSource)
    }

    override fun withZone(zone: ZoneId): Clock {
        if (zone == this.zone) {
            return this
        }
        return NanoClock(clockSource, zone, synchronizedClockSnapshotReference)
    }

    override fun getZone(): ZoneId = zone

    override fun instant(): Instant {
        val monotonicNanos = clockSource.monotonicNanos()
        val snapshot = snapshotWithCalibration(monotonicNanos)
        val nanosSinceBase = monotonicNanos - snapshot.monotonicNanos
        val epochMillis = nanosSinceBase / NANOS_PER_MILLI + snapshot.realtimeMillis
        val epochSeconds = epochMillis / MILLIS_PER_SEC
        val nanoAdjustment = epochMillis % MILLIS_PER_SEC * NANOS_PER_MILLI + nanosSinceBase % NANOS_PER_MILLI
        return Instant.ofEpochSecond(epochSeconds, nanoAdjustment)
    }

    private fun snapshotWithCalibration(monotonicNanos: Long): SynchronizedClockSnapshot {
        val snapshot = synchronizedClockSnapshotReference.get()
        return if (isNeedsCalibration(clockSource, monotonicNanos, snapshot)) {
            calibration()
       } else snapshot
    }

    private fun isNeedsCalibration(clockSource: ClockSource, monotonicNanos: Long, snapshot: SynchronizedClockSnapshot): Boolean {
        val realtimeMillis = clockSource.realtimeMillis()
        val realtimeMillisSinceBase = (realtimeMillis - snapshot.realtimeMillis) * 1_000_000L
        val nanosSinceBase = monotonicNanos - snapshot.monotonicNanos
        return abs(nanosSinceBase - realtimeMillisSinceBase - 500_000L) > 1_000_000L
    }

    private fun calibration(): SynchronizedClockSnapshot = synchronized(synchronizedClockSnapshotReference) {
        val snapshot = synchronizedClockSnapshotReference.get()
        if (isNeedsCalibration(clockSource, clockSource.monotonicNanos(), snapshot)) {
            val newSnapshot = clockSource.synchronizeWithRealtime(5)
            synchronizedClockSnapshotReference.set(newSnapshot)
            calibrationCounter.incrementAndGet()
            newSnapshot
        } else snapshot
    }
}

data class SynchronizedClockSnapshot(
    val realtimeMillis: Long,
    val monotonicNanos: Long,
    val elapsed: Long
)

interface ClockSource {
    fun monotonicNanos(): Long
    fun realtimeMillis(): Long
}

object SystemClockSource: ClockSource {
    override fun monotonicNanos(): Long = System.nanoTime()
    override fun realtimeMillis(): Long = System.currentTimeMillis()
}

private fun ClockSource.synchronizeWithRealtime(): SynchronizedClockSnapshot {
    while (true) {
        val startNanos = monotonicNanos()
        val millis1 = realtimeMillis()
        val millis2 = realtimeMillis()
        val nanos = monotonicNanos()
        val elapsedNanos = nanos - startNanos
        if (millis2 - millis1 == 1L) {
            /* 시간 오차를 줄이기 위해 milliseconds단위의 realtime이 변경되는 시점의 값을 측정한다 */
            return SynchronizedClockSnapshot(
                realtimeMillis = millis2,
                monotonicNanos = nanos - elapsedNanos,
                elapsed = elapsedNanos
            )
        }
    }
}

private fun ClockSource.synchronizeWithRealtime(candidates: Int): SynchronizedClockSnapshot =
    (1..candidates)
        .map { synchronizeWithRealtime() }
        .minBy { it.elapsed }!!


