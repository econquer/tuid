package im.toss.util.runtime

import im.toss.util.random.nextBytes
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom

object RuntimeInstance {
    private val seed = SecureRandom()
    private fun getMacAddressOrRandom() = try {
        val localAddress = InetAddress.getLocalHost()
        val networkInterface = NetworkInterface.getByInetAddress(localAddress)
        networkInterface.hardwareAddress
    } catch (e: Throwable) {
        seed.nextBytes(20)
    }

    val fingerprint: Long =
        MessageDigest.getInstance("SHA-1")
            .run {
                update(getMacAddressOrRandom())
                update(seed.nextBytes(2))
                digest()
            }.run {
                copyOfRange(size - 8, size)
            }.run {
                ByteBuffer.wrap(this).getLong(0)
            }
}

