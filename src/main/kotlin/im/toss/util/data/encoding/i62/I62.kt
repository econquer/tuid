package im.toss.util.data.encoding.i62

import java.math.BigInteger
import kotlin.math.min

/**
 * I62 FORMAT
 * 62진법 문자열로 표현되는 정수 인코딩
 */


fun BigInteger.toI62(len:Int = 22): String = I62.of(this, len)
fun Long.toI62(len:Int = 11): String = I62.of(this, len)
fun Int.toI62(len:Int = 6): String = I62.of(this, len)

object I62 {
    private val I62TABLE = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z'
    )
    private val I62REVERSE = run {
        val arr = IntArray(255) { -1 }
        for (i in I62TABLE.indices) {
            arr[I62TABLE[i].toInt()] = i
        }
        arr
    }

    private val I62REVERSE_BIG = run {
        val arr = Array<BigInteger?>(255) { null }
        for (i in I62TABLE.indices) {
            arr[I62TABLE[i].toInt()] = i.toBigInteger()
        }
        arr
    }

    private val ZERO = I62TABLE[0]

    fun of(value: Int, len: Int = 6): String = encodeInt(value, len - 1, CharArray(len) { ZERO })
    fun of(value: Long, len: Int = 11): String = encodeLong(value, len - 1, CharArray(len) { ZERO })
    fun of(value: BigInteger, len: Int = 22): String = encodeBigInt(value, len - 1, CharArray(len) { ZERO })

    private fun encodeInt(value: Int, index: Int, buffer: CharArray): String =
        if (ComplementInt.available(value, buffer.size)) {
            var left = ComplementInt.encode(value, buffer.size)
            var i = index
            while (left > 0 && i > -1) {
                buffer[i--] = I62TABLE[left % 62]
                left /= 62
            }
            String(buffer)
        } else {
            encodeLong(value.toLong(), index, buffer)
        }

    private fun encodeLong(value: Long, index: Int, buffer: CharArray): String =
        if (ComplementLong.available(value, buffer.size)) {
            var left = ComplementLong.encode(value, buffer.size)
            var i = index
            while (left > 0 && i > -1) {
                buffer[i--] = I62TABLE[(left % 62).toInt()]
                left /= 62
            }
            String(buffer)
        } else {
            encodeBigInt(value.toBigInteger(), index, buffer)
        }

    private val BIGINT_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE)
    private val BIGINT_2 = BigInteger.valueOf(2L)
    private val BIGINT_62 = BigInteger.valueOf(62L)

    private fun encodeBigInt(value: BigInteger, index: Int, buffer: CharArray): String {
        var left = ComplementBigInteger.encode(value, buffer.size)
        var i = index
        while (left > BigInteger.ZERO && i > -1) {
            if (left <= BIGINT_LONG_MAX) {
                return encodeLong(left.toLong(), i, buffer)
            }

            buffer[i--] = I62TABLE[(left % BIGINT_62).toInt()]
            left /= BIGINT_62
        }
        return String(buffer)
    }

    fun toBigInteger(i62value: CharSequence, signed: Boolean = true): BigInteger {
        val longPart = i62value.subSequence(0, min(i62value.length, 10))
        var value = toLong(longPart, signed).toBigInteger()
        if (i62value.length > 10) {
            for (i in 10 until i62value.length) {
                value *= BIGINT_62
                val ch = i62value[i].toInt()
                if (ch > 255)
                    throw NumberFormatException("Invalid I62 format: $this")

                val v = I62REVERSE_BIG[ch] ?: throw NumberFormatException("Invalid I62 format: $this")

                value += v
            }
        }
        return value
    }

    fun toLong(i62value: CharSequence, signed: Boolean = true): Long {
        if (i62value.length >= 11) {
            return toBigInteger(i62value, signed).longValueExact()
        }
        var value = 0L
        for (i in 0 until i62value.length) {
            value *= 62
            val ch = i62value[i].toInt()
            if (ch > 255)
                throw NumberFormatException("Invalid I62 format: $this")

            val v = I62REVERSE[ch]
            if (v == -1)
                throw NumberFormatException("Invalid I62 format: $this")

            value += v
        }

        return if (signed) ComplementLong.decode(value, i62value.length) else value
    }

    fun toInt(i62value: CharSequence, signed: Boolean = true): Int = toLong(i62value, signed).toInt()

    object ComplementInt {
        private val base = run {
            val base = IntArray(6)
            base[0] = 0
            base[1] = 62
            for (i in 2..5) {
                base[i] = base[i - 1] * 62
            }
            base
        }

        private val max = run {
            val max = IntArray(7)
            for (i in 0..5) {
                max[i] = base[i] / 2
            }
            max
        }

        fun available(value:Int, len: Int): Boolean {
            if (value < -916132832) {
                return false
            }
            if (len > 5 && value < 0) {
                return false
            }
            return true
        }

        fun encode(value: Int, len: Int): Int {
            if (value < 0) {
                for (l in len .. 5) {
                    val r = base[l] + value
                    if (r >= 0) return r
                }
                throw IllegalArgumentException("value is too small")
            } else return value
        }
    }

    object ComplementLong {
        private val base = run {
            val base = LongArray(12)
            base[0] = 0
            base[1] = 62L
            for (i in 2..11) {
                base[i] = base[i - 1] * 62L
            }
            base
        }

        private val max = run {
            val max = LongArray(32)
            for (i in 0..10) {
                max[i] = base[i] / 2L
            }
            max
        }

        fun encode(value: Long, len: Int): Long {
            if (value < 0L) {
                for (l in len .. 10) {
                    val r = base[l] + value
                    if (r >= 0) return r
                }
                throw IllegalArgumentException("value is too small")
            } else return value
        }

        fun decode(value: Long, len: Int): Long {
            return if (value < max[len]) {
                value
            } else {
                value - base[len]
            }
        }

        fun available(value: Long, len: Int): Boolean {
            if (value < -839299365868340224L) {
                return false
            }
            if (len > 5 && value < 0) {
                return false
            }
            return true
        }
    }


    object ComplementBigInteger {
        private val base = run {
            val base = Array(512) { BigInteger.ZERO }
            base[0] = BigInteger.ZERO
            base[1] = BIGINT_62
            for (i in 2..511) {
                base[i] = base[i - 1] * BIGINT_62
            }
            base
        }

        private val max = run {
            val max = Array(512) { BigInteger.ZERO }
            for (i in 0..511) {
                max[i] = base[i] / BIGINT_2
            }
            max
        }

        fun encode(value: BigInteger, len: Int): BigInteger {
            return if (value.signum() < 0) {
                for (l in len .. 511) {
                    val r = base[l] + value
                    if (r.signum() >= 0) return r
                }
                throw IllegalArgumentException("value is too small")
            } else value
        }
    }
}
