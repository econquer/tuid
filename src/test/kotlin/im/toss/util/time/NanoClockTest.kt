package im.toss.util.time

import im.toss.test.equalsTo
import org.junit.jupiter.api.Test
import java.time.ZoneId

internal class NanoClockTest {
    @Test
    fun zoneTest() {
        NanoClock().zone equalsTo ZoneId.systemDefault()
        NanoClock().withZone(ZoneId.systemDefault()).zone equalsTo ZoneId.systemDefault()
        NanoClock().withZone(ZoneId.of("Asia/Seoul")).zone equalsTo ZoneId.of("Asia/Seoul")
        NanoClock().withZone(ZoneId.of("UTC")).zone equalsTo ZoneId.of("UTC")
    }
}