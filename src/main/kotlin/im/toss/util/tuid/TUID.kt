package im.toss.util.tuid

import im.toss.util.data.encoding.i62.I62
import im.toss.util.runtime.RuntimeInstance
import im.toss.util.time.NanoClock
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun tuid(
    type: Int,
    now: Instant,
    count: Int,
    runtime: Long,
    random: Int
): String {
    /* 6 */ val secs = I62.of(now.epochSecond, 6)
    /* 6 */ val nanos = I62.of(now.nano, 6)
    /* 6 */ val rtm = I62.of(runtime, 6)
    /* 6 */ val rnd = I62.of(random, 6)
    /* 2 */ val cnt = I62.of(count, 2)
    /* 2 */ val tp = I62.of(type, 2)
    return "$secs$nanos$rtm$rnd$cnt$tp"
}

fun tuid(type: Int = 0): String = TUIDGenerator.DEFAULT.next(type)
fun TUID(type: Int): TUID = TUID(tuid(type))

data class TUID(val value: String = tuid()): Comparable<TUID> {
    val type: Int get() = I62.toInt(value.substring(26, 28))
    val datetime: ZonedDateTime get() = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
    val instant: Instant get() = Instant.ofEpochSecond(epochSeconds, nanoseconds)
    val runtime: Long get() = I62.toLong(value.substring(12, 18))
    val random: Long get() = I62.toLong(value.substring(18, 24))
    val count: Int get() = I62.toInt(value.substring(24, 26))

    private val epochSeconds: Long get() = I62.toLong(value.substring(0, 6))
    private val nanoseconds: Long get() = I62.toLong(value.substring(6, 12))

    override fun toString(): String {
        return value
    }

    override fun compareTo(other: TUID): Int = value.compareTo(other.value)
}

class TUIDGenerator(
    private val clock: Clock = NanoClock.DEFAULT,
    private val random: Random = SecureRandom(),
    private val counter: AtomicInteger = AtomicInteger(),
    runtime: Long = RuntimeInstance.fingerprint
) {
    companion object {
        val DEFAULT = TUIDGenerator()
    }

    val runtimeFingerprint = I62.toLong(I62.of(runtime, 6))

    fun next(typeIdentifier: Int): String = tuid(
        typeIdentifier,
        clock.instant(),
        counter.incrementAndGet(),
        runtimeFingerprint,
        random.nextInt()
    )
}
