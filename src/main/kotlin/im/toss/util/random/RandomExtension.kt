package im.toss.util.random

import java.util.*

fun Random.nextBytes(n: Int): ByteArray = ByteArray(n).run { this@nextBytes.nextBytes(this); this }
