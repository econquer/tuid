package im.toss.util.data.encoding.i62

import im.toss.test.equalsTo
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.system.measureNanoTime

class I62Test {
    @Test
    fun `BigInteger_of`() {
        I62.of(BigInteger.ZERO, len = 3) equalsTo "000"
        for(value in 60..1000L) {
            println("${I62.of(value.toBigInteger(), 5)} -> $value")
        }
        for(value in (Long.MAX_VALUE-1000)..Long.MAX_VALUE) {
            println("${I62.of(value.toBigInteger(), 8)} -> $value")
        }

        for(i in 0..1000L) {
            val value = System.currentTimeMillis()
            println("${I62.of(value.toBigInteger())} -> $value")
            Thread.sleep(1)
        }
    }

    @Test
    fun `Long_toI62`() {
        for(value in 60..1000L) {
            println("${value.toI62(5)} -> $value")
        }
        for(value in (Long.MAX_VALUE-1000)..Long.MAX_VALUE) {
            println("${value.toI62(8)} -> $value")
        }

        for(i in 0..1000L) {
            val value = System.currentTimeMillis()
            println("${value.toI62()} -> $value")
            Thread.sleep(1)
        }
    }

    @Test
    fun `Int_toI62`() {
        for(value in 0..1000) {
            println("${value.toI62()} -> $value")
        }
        for(value in (Int.MAX_VALUE-1000)..Int.MAX_VALUE) {
            println("${value.toI62()} -> $value")
        }
    }

    @Test
    fun `Long to I62`() {
        1564573699000L.toI62(7).equalsTo("RXnjnJI")
        1564572851823L.toI62(7).equalsTo("RXngEv9")
        1564573833651L.toI62().equalsTo("0000RXnkML5")
        1564573834269L.toI62().equalsTo("0000RXnkMV3")
        1564573834667L.toI62(3).equalsTo("MbT")
        1564573699000L.toI62(1).equalsTo("I")
    }

    @Test
    fun `I62 parse`() {
        for (i in -64L..64L) {
            I62.toBigInteger(I62.of(i.toBigInteger(), 1))
        }
        I62.toLong("zzzzzzzz").equalsTo(-1L)
        I62.toLong("zzzzzzz").equalsTo(-1L)
        I62.toLong("zzzzz").equalsTo(-1L)
        I62.toLong("RXnjnJI").equalsTo(1564573699000L)
        I62.toLong("RXngEv9").equalsTo(1564572851823L)
        I62.toLong("0000RXnkML5").equalsTo(1564573833651L)
        I62.toLong("0000RXnkMV3").equalsTo(1564573834269L)
        I62.toLong("0000RXnjnJI").equalsTo(1564573699000L)
        I62.toLong("0000000000RXnkMbT").equalsTo(1564573834667L)

        I62.toBigInteger("0000000000RXnkMbT") equalsTo (1564573834667L.toBigInteger())
        I62.toBigInteger("z") equalsTo (-1L.toBigInteger())
    }

    @Test
    fun `Invalid format case`() {
        assertThrows<NumberFormatException> {
            I62.toInt("hello!")
        }

        assertThrows<NumberFormatException> { I62.toBigInteger("안녕하세요") }
        assertThrows<NumberFormatException> { I62.toBigInteger("000000000000안녕하세요") }
        assertThrows<NumberFormatException> { I62.toBigInteger("000000000000_") }
    }

    @Test
    fun randomIntTest() {
        for(i in 1..1000) {
            val value = Random.nextInt()
            val encoded = I62.of(value)
            val decoded = I62.toInt(encoded)

            println("$value -> $encoded -> $decoded")
            decoded equalsTo value
        }
    }

    @Test
    fun randomLongTest() {
        for(i in 1..1000) {
            val value = Random.nextLong()
            val encoded = I62.of(value)
            val decoded = I62.toLong(encoded)

            println("$value -> $encoded -> $decoded")
            decoded equalsTo value
        }
    }

    @Test
    fun randomBigIntegerTest() {
        for(l in 1..10) {
            for (i in 1..100) {
                listOf((l + 1) * 12, (l + 1) * 10).forEach { len ->
                    var value = BigInteger.ONE
                    for (j in 1..l) {
                        value *= Random.nextLong().toBigInteger()
                    }
                    val encoded = I62.of(value, len)
                    val decoded = I62.toBigInteger(encoded)

                    println("$value -> $encoded -> $decoded")
                    decoded equalsTo value
                }
            }
        }
    }

    @Test
    fun encodePerformanceTest() {
        val positiveElapsed = run {
            val timer = AtomicLong()
            val seed = Random(0L)
            for (i in 1..1_000_000L) {
                val value = (seed.nextLong().absoluteValue)
                timer.addAndGet(
                    measureNanoTime {
                        I62.of(value, 11)
                    }
                )
            }
            TimeUnit.NANOSECONDS.toMillis(timer.get())
        }
        println("positive values are $positiveElapsed ms elapsed in 1,000,000 times")


        val negativeElapsed = run {
            val timer = AtomicLong()
            val seed = Random(0L)
            for (i in 1..1_000_000L) {
                val value = (-seed.nextLong().absoluteValue)
                timer.addAndGet(
                    measureNanoTime {
                        I62.of(value, 11)
                    }
                )
            }
            TimeUnit.NANOSECONDS.toMillis(timer.get())
        }
        println("negative values are $negativeElapsed ms elapsed in 1,000,000 times")
    }

    @Test
    fun intOverflowRangeTest() {
        (-916132830).toI62(5) equalsTo "00002"
        (-916132831).toI62(5) equalsTo "00001"
        (-916132832).toI62(5) equalsTo "00000"
        (-916132833).toI62(5) equalsTo "zzzzz"
        (-916132834).toI62(5) equalsTo "zzzzy"

        (-916132830).toI62(4) equalsTo "0002"
        (-916132831).toI62(4) equalsTo "0001"
        (-916132832).toI62(4) equalsTo "0000"
        (-916132833).toI62(4) equalsTo "zzzz"
        (-916132834).toI62(4) equalsTo "zzzy"

        (-916132830).toI62(3) equalsTo "002"
        (-916132831).toI62(3) equalsTo "001"
        (-916132832).toI62(3) equalsTo "000"
        (-916132833).toI62(3) equalsTo "zzz"
        (-916132834).toI62(3) equalsTo "zzy"
    }

    @Test
    fun longOverflowRangeTest() {
        (-839299365868340222L).toI62(15) equalsTo "zzzzz0000000002"
        I62.toLong("zzzzz0000000002") equalsTo -839299365868340222L

        (-839299365868340223L).toI62(15) equalsTo "zzzzz0000000001"
        I62.toLong("zzzzz0000000001") equalsTo -839299365868340223L

        (-839299365868340224L).toI62(15) equalsTo "zzzzz0000000000"
        I62.toLong("zzzzz0000000000") equalsTo -839299365868340224L

        (-839299365868340225L).toI62(15) equalsTo "zzzzyzzzzzzzzzz"
        I62.toLong("zzzzyzzzzzzzzzz") equalsTo -839299365868340225L

        (-839299365868340226L).toI62(15) equalsTo "zzzzyzzzzzzzzzy"
        I62.toLong("zzzzyzzzzzzzzzy") equalsTo -839299365868340226L

        (-839299365868340222L).toI62(10) equalsTo "0000000002"
        (-839299365868340223L).toI62(10) equalsTo "0000000001"
        (-839299365868340224L).toI62(10) equalsTo "0000000000"
        (-839299365868340225L).toI62(10) equalsTo "zzzzzzzzzz"
        (-839299365868340226L).toI62(10) equalsTo "zzzzzzzzzy"

        (-839299365868340222L).toI62(5) equalsTo "00002"
        (-839299365868340223L).toI62(5) equalsTo "00001"
        (-839299365868340224L).toI62(5) equalsTo "00000"
        (-839299365868340225L).toI62(5) equalsTo "zzzzz"
        (-839299365868340226L).toI62(5) equalsTo "zzzzy"

        (-839299365868340222L).toI62(4) equalsTo "0002"
        (-839299365868340223L).toI62(4) equalsTo "0001"
        (-839299365868340224L).toI62(4) equalsTo "0000"
        (-839299365868340225L).toI62(4) equalsTo "zzzz"
        (-839299365868340226L).toI62(4) equalsTo "zzzy"

        (-916132830L).toI62(5) equalsTo "00002"
        (-916132831L).toI62(5) equalsTo "00001"
        (-916132832L).toI62(5) equalsTo "00000"
        (-916132833L).toI62(5) equalsTo "zzzzz"
        (-916132834L).toI62(5) equalsTo "zzzzy"
    }

    @Test
    fun decodeIntPerformanceTest() {
       val elapsed = run {
            val timer = AtomicLong()
            val seed = Random(0L)
            for (i in 1..1_000_000L) {
                val value = seed.nextInt()
                val encoded = value.toI62()
                timer.addAndGet(
                    measureNanoTime {
                        I62.toInt(encoded)
                    }
                )
                I62.toBigInteger(encoded) equalsTo value.toBigInteger()
            }
            TimeUnit.NANOSECONDS.toMillis(timer.get())
        }
        println("decode i62 are $elapsed ms elapsed in 1,000,000 times")
    }

    @Test
    fun decodeLongPerformanceTest() {
        val elapsed = run {
            val timer = AtomicLong()
            val seed = Random(0L)
            for (i in 1..1_000_000L) {
                val value = seed.nextLong()
                val encoded = value.toI62()
                timer.addAndGet(
                    measureNanoTime {
                        I62.toLong(encoded)
                    }
                )
                I62.toBigInteger(encoded) equalsTo value.toBigInteger()
            }
            TimeUnit.NANOSECONDS.toMillis(timer.get())
        }
        println("decode i62 are $elapsed ms elapsed in 1,000,000 times")
    }


    @Test
    fun decodeBigIntPerformanceTest() {
        val elapsed = run {
            val timer = AtomicLong()
            val seed = Random(0L)
            for (i in 1..1_000_000L) {
                val value = seed.nextLong().toBigInteger()
                val encoded = value.toI62()
                timer.addAndGet(
                    measureNanoTime {
                        I62.toBigInteger(encoded)
                    }
                )
                I62.toBigInteger(encoded) equalsTo value
            }
            TimeUnit.NANOSECONDS.toMillis(timer.get())
        }
        println("decode i62 are $elapsed ms elapsed in 1,000,000 times")
    }

    @TestFactory
    fun intOverflow(): List<DynamicTest> {
        data class Given(
            val len: Int,
            val value: Int,
            val expectedDecode: Int,
            val expectedI62: String
        )
        return listOf(
            Given(1, 30, 30, "U"),
            Given(1, -31, -31, "V"),
            Given(1, 31, -31, "V"),
            Given(1, 32, -30, "W"),

            Given(2, 1921, 1921, "Uz"),
            Given(2, -1922, -1922, "V0"),
            Given(2, 1922, -1922, "V0"),
            Given(2, -1921, -1921, "V1"),
            Given(2, 1923, -1921, "V1"),

            Given(3, 119163, 119163, "Uzz"),
            Given(3, -119164, -119164, "V00"),
            Given(3, 119164, -119164, "V00"),
            Given(3, -119163, -119163, "V01"),
            Given(3, 119165, -119163, "V01"),

            Given(4, 7388167, 7388167, "Uzzz"),
            Given(4, -7388168, -7388168, "V000"),
            Given(4, 7388168, -7388168, "V000"),
            Given(4, -7388167, -7388167, "V001"),
            Given(4, 7388169, -7388167, "V001"),

            Given(5, 458066415, 458066415, "Uzzzz"),
            Given(5, -458066416, -458066416, "V0000"),
            Given(5, 458066416, -458066416, "V0000"),
            Given(5, -458066415, -458066415, "V0001"),
            Given(5, 458066417, -458066415, "V0001"),


            Given(6, Int.MAX_VALUE, Int.MAX_VALUE, "2LKcb1"),
            Given(6, Int.MAX_VALUE-1, Int.MAX_VALUE-1, "2LKcb0"),
            Given(6, Int.MIN_VALUE, Int.MIN_VALUE, "xefNOy"),
            Given(6, Int.MIN_VALUE+1, Int.MIN_VALUE+1, "xefNOz")
        ).map {
            it.run {
                dynamicTest("$value 를 $len 길이의 I62로 인코딩하면 $expectedI62 가 된다") {
                    val i62value = I62.of(value, len)
                    println(i62value)
                    i62value equalsTo expectedI62
                    I62.toInt(i62value) equalsTo expectedDecode
                }
            }
        }
    }

    @TestFactory
    fun longOverflow(): List<DynamicTest> {
        data class Given(
            val len: Int,
            val value: Long,
            val expectedDecode: Long,
            val expectedI62: String
        )
        return listOf(
            Given(11, Long.MIN_VALUE, Long.MIN_VALUE, "p0erCzRurDs"),
            Given(11, Long.MAX_VALUE, Long.MAX_VALUE, "AzL8n0Y58m7"),

            Given(1, 30, 30, "U"),
            Given(1, -31, -31, "V"),
            Given(1, 31, -31, "V"),
            Given(1, 32, -30, "W"),

            Given(2, 1921, 1921, "Uz"),
            Given(2, -1922, -1922, "V0"),
            Given(2, 1922, -1922, "V0"),
            Given(2, -1921, -1921, "V1"),
            Given(2, 1923, -1921, "V1"),

            Given(3, 119163, 119163, "Uzz"),
            Given(3, -119164, -119164, "V00"),
            Given(3, 119164, -119164, "V00"),
            Given(3, -119163, -119163, "V01"),
            Given(3, 119165, -119163, "V01"),

            Given(4, 7388167, 7388167, "Uzzz"),
            Given(4, -7388168, -7388168, "V000"),
            Given(4, 7388168, -7388168, "V000"),
            Given(4, -7388167, -7388167, "V001"),
            Given(4, 7388169, -7388167, "V001"),

            Given(5, 458066415, 458066415, "Uzzzz"),
            Given(5, -458066416, -458066416, "V0000"),
            Given(5, 458066416, -458066416, "V0000"),
            Given(5, -458066415, -458066415, "V0001"),
            Given(5, 458066417, -458066415, "V0001"),


            Given(6, Int.MAX_VALUE.toLong(), Int.MAX_VALUE.toLong(), "2LKcb1"),
            Given(6, Int.MAX_VALUE.toLong()-1, Int.MAX_VALUE.toLong()-1, "2LKcb0"),
            Given(6, Int.MIN_VALUE.toLong(), Int.MIN_VALUE.toLong(), "xefNOy"),
            Given(6, Int.MIN_VALUE.toLong()+1, Int.MIN_VALUE.toLong()+1, "xefNOz"),


            Given(11, Long.MAX_VALUE, Long.MAX_VALUE, "AzL8n0Y58m7"),
            Given(11, Long.MAX_VALUE-1, Long.MAX_VALUE-1, "AzL8n0Y58m6"),
            Given(11, Long.MIN_VALUE, Long.MIN_VALUE, "p0erCzRurDs"),
            Given(11, Long.MIN_VALUE+1, Long.MIN_VALUE+1, "p0erCzRurDt")

        ).map {
            it.run {
                dynamicTest("$value 를 $len 길이의 I62로 인코딩하면 $expectedI62 가 된다") {
                    val i62value = I62.of(value, len)
                    println(i62value)
                    i62value equalsTo expectedI62
                    I62.toLong(i62value) equalsTo expectedDecode
                }
            }
        }
    }

    @Test
    fun complementTest() {
        I62.ComplementLong.encode(-63, 1) equalsTo 3781L
        I62.ComplementLong.encode(-63, 2) equalsTo 3781L

        I62.ComplementLong.encode(2, 1) equalsTo 2L
        I62.ComplementLong.encode(1, 1) equalsTo 1L
        I62.ComplementLong.encode(0, 1) equalsTo 0L
        I62.ComplementLong.encode(-1, 1) equalsTo 61L
        I62.ComplementLong.encode(-2, 1) equalsTo 60L

        assertThrows<Throwable> { I62.ComplementInt.encode(Int.MIN_VALUE, 1) }
        assertThrows<Throwable> { I62.ComplementLong.encode(Long.MIN_VALUE, 1) }
        assertThrows<Throwable> {
            val b62 = 62L.toBigInteger()
            var value = b62
            for (i in 1..512) {
                value *= b62
            }
            value *= (-1L).toBigInteger()
            I62.ComplementBigInteger.encode(value, 1)
        }

        I62.ComplementLong.decode(2, 1) equalsTo 2L
        I62.ComplementLong.decode(1, 1) equalsTo 1L
        I62.ComplementLong.decode(0, 1) equalsTo 0L
        I62.ComplementLong.decode(62, 1) equalsTo 0L
        I62.ComplementLong.decode(61, 1) equalsTo -1L
        I62.ComplementLong.decode(60, 1) equalsTo -2L

        I62.ComplementLong.decode(I62.ComplementLong.encode(Int.MAX_VALUE.toLong(), 6), 6) equalsTo Int.MAX_VALUE.toLong()
        I62.ComplementLong.decode(I62.ComplementLong.encode(Int.MIN_VALUE.toLong(), 6), 6) equalsTo Int.MIN_VALUE.toLong()
    }
}