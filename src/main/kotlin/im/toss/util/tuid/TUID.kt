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

/**
 * Generate a TUID String
 */
fun tuid(type: Int = 0): String = TUIDGenerator.DEFAULT.next(type)

/**
 * Generate a TUID Object
 */
fun TUID(type: Int): TUID = TUID(tuid(type))


fun tuid_v1(
    type: Int,
    epochSecond: Long,
    nanos: Int,
    sequence: Int,
    fingerprint: Int,
    random: Int
): String = tuid_v1(
    type,
    epochSecond,
    nanos,
    sequence,
    I62.of(fingerprint, 6),
    random
)

private fun tuid_v1(
    type: Int,
    epochSecond: Long,
    nanos: Int,
    sequence: Int,
    fingerprint: String, // encoded
    random: Int
): String = tuid_v1(
    I62.of(epochSecond, 6),
    I62.of(nanos, 6),
    fingerprint,
    I62.of(random, 5),
    I62.of(sequence, 2),
    I62.of(type, 3)
)

private fun tuid_v1(
    epochSecond: String,
    nanos: String,
    fingerprint: String,
    random: String,
    sequence: String,
    type: String
): String = StringBuilder(28)
    .append(epochSecond)
    .append(nanos)
    .append(fingerprint)
    .append(random)
    .append(sequence)
    .append(type)
    .toString()

/*----------------------------------------------------------------------+
| name          | offset | length(bytes) | description                  |
|---------------|--------|---------------|------------------------------|
| epoch_seconds |      0 |             6 | seconds since epoch          |
| nanos+version |      6 |             6 | nanoseconds since timestamp  |
| fingerprint   |     12 |             6 | fingerprint of generator     |
| random        |     18 |             5 | random value                 |
| sequence      |     23 |             2 | sequential value             |
| type          |     25 |             3 | type of identifier           |
+----------------------------------------------------------------------*/
data class TUID(val value: String = tuid()): Comparable<TUID> {
    init {
        if (value.length != 28) {
            throw IllegalArgumentException("length of the tuid must be 28: $value -> ${value.length}")
        }
    }

    val type: Int get() = I62.toInt(value.substring(25, 28))
    val datetime: ZonedDateTime get() = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
    val instant: Instant get() = Instant.ofEpochSecond(epochSeconds, nanos)
    val fingerprint: Long get() = I62.toLong(value.substring(12, 18))
    val random: Int get() = I62.toInt(value.substring(18, 23))
    val sequence: Int get() = I62.toInt(value.substring(23, 25))

    val epochSeconds: Long get() = I62.toLong(value.substring(0, 6))
    val nanos: Long get() = I62.toLong(value.substring(6, 12)) % 1_000_000_000L
    val version: Int get() = (I62.toLong(value.substring(6, 12)) / 1_000_000_000L).toInt() + 1

    override fun toString(): String {
        return value
    }

    override fun compareTo(other: TUID): Int = value.compareTo(other.value)
}

class TUIDGenerator(
    private val clock: Clock = NanoClock.DEFAULT,
    private val random: Random = SecureRandom(),
    private val sequence: AtomicInteger = AtomicInteger(),
    fingerprint: Long = RuntimeInstance.fingerprint
) {
    companion object {
        val DEFAULT = TUIDGenerator()
    }

    private val encodedFingerprint = I62.of(fingerprint, 6)
    val fingerprint = I62.toLong(encodedFingerprint)

    fun next(type: Int): String = clock.instant().run {
        tuid_v1(
            type,
            epochSecond,
            nano,
            sequence.incrementAndGet(),
            encodedFingerprint,
            random.nextInt()
        )
    }
}
