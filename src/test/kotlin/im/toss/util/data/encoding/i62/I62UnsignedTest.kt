package im.toss.util.data.encoding.i62

import im.toss.test.equalsTo
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.math.BigInteger

class I62UnsignedTest {
    @TestFactory
    fun toIntUnsigned(): List<DynamicTest> {
        data class Given(
            val i62: String,
            val signed: Boolean,
            val expected: Int = -1,
            val overflowExpected: Boolean = false
        )
        return listOf(
            Given("1", true, 1),
            Given("1", false, 1),
            Given("zzz", true, -1),
            Given("zzz", false, 238327),
            Given("zzzz", true, -1),
            Given("zzzz", false, 14776335),
            Given("zzzzz", true, -1),
            Given("zzzzz", false, 916132831),
            Given("zzzzzz", true, -1) //,
//            Given("zzzzzz", false, -1) // FIXME overflow, test!
       ).map {
            it.run {
                dynamicTest("${i62}를 ${if (signed) "signed" else "unsigned"}로 읽으면 ${expected}가 된다") {
                    I62.toInt(i62, signed) equalsTo expected
                    I62.of(expected, i62.length) equalsTo i62
                }
            }
        }
    }

    @TestFactory
    fun toLongUnsigned(): List<DynamicTest> {
        data class Given(
            val i62: String,
            val signed: Boolean,
            val expected: Long
        )
        return listOf(
            Given("1", true, 1L),
            Given("1", false, 1L),
            Given("zzz", true, -1L),
            Given("zzz", false, 238327L),
            Given("zzzz", true, -1L),
            Given("zzzz", false, 14776335L),
            Given("zzzzz", true, -1L),
            Given("zzzzz", false, 916132831L),
            Given("zzzzzz", true, -1L),
            Given("zzzzzz", false, 56800235583L),
            Given("zzzzzzz", true, -1L),
            Given("zzzzzzz", false, 3521614606207L),
            Given("zzzzzzzz", true, -1L),
            Given("zzzzzzzz", false, 218340105584895L),
            Given("zzzzzzzzz", true, -1L),
            Given("zzzzzzzzz", false, 13537086546263551L),
            Given("zzzzzzzzzz", true, -1L),
            Given("zzzzzzzzzz", false, 839299365868340223L),
            Given("zzzzzzzzzzz", true, -1L) //,
            // Given("zzzzzzzzzzz", false, -1L) // FIXME overflow! ArithmeticException is thrown
        ).map {
            it.run {
                dynamicTest("${i62}를 ${if (signed) "signed" else "unsigned"}로 읽으면 ${expected}가 된다") {
                    I62.toLong(i62, signed) equalsTo expected
                    I62.of(expected, i62.length) equalsTo i62
                }
            }
        }
    }

    @TestFactory
    fun toBigIntegerUnsigned(): List<DynamicTest> {
        data class Given(
            val i62: String,
            val signed: Boolean,
            val expected: BigInteger
        )
        return listOf(
            Given("1", true, BigInteger.valueOf(1L)),
            Given("1", false, BigInteger.valueOf(1L)),
            Given("zzz", true, BigInteger.valueOf(-1L)),
            Given("zzz", false, BigInteger.valueOf(238327L)),
            Given("zzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzz", false, BigInteger.valueOf(14776335L)),
            Given("zzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzz", false, BigInteger.valueOf(916132831L)),
            Given("zzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzz", false, BigInteger.valueOf(56800235583L)),
            Given("zzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzz", false, BigInteger.valueOf(3521614606207L)),
            Given("zzzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzzz", false, BigInteger.valueOf(218340105584895L)),
            Given("zzzzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzzzz", false, BigInteger.valueOf(13537086546263551L)),
            Given("zzzzzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzzzzz", false, BigInteger.valueOf(839299365868340223L)),
            Given("zzzzzzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzzzzzz", false, BigInteger("52036560683837093887")),
            Given("zzzzzzzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzzzzzzz", false, BigInteger("3226266762397899821055")),
            Given("zzzzzzzzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzzzzzzzz", false, BigInteger("200028539268669788905471")),
            Given("zzzzzzzzzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzzzzzzzzz", false, BigInteger("12401769434657526912139263")),
            Given("zzzzzzzzzzzzzzz", true, BigInteger.valueOf(-1L)),
            Given("zzzzzzzzzzzzzzz", false, BigInteger("768909704948766668552634367"))
        ).map {
            it.run {
                dynamicTest("${i62}를 ${if (signed) "signed" else "unsigned"}로 읽으면 ${expected}가 된다") {
                    I62.toBigInteger(i62, signed) equalsTo expected
                    I62.of(expected, i62.length) equalsTo i62
                }
            }
        }
    }
}