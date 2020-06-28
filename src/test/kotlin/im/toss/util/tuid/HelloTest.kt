package im.toss.util.tuid

import im.toss.test.equalsTo
import org.junit.jupiter.api.Test

class HelloTest {
    @Test
    fun helloTest() {
        helloWorld() equalsTo "hello world"
    }
}