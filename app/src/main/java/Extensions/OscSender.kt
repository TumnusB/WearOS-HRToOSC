package Extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class OscSender {

    fun sendOsc(ip: String, port: Int, path: String, value: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            sendOscMessage(ip, port, path, value)
        }
    }

    fun sendOsc(ip: String, port: Int, path: String, value: Float) {
        CoroutineScope(Dispatchers.IO).launch {
            sendOscMessage(ip, port, path, value)
        }
    }

    private fun padString(s: String): ByteArray {
        val bytes = s.toByteArray(Charset.forName("US-ASCII"))
        val padding = (4 - bytes.size % 4) % 4
        return bytes + ByteArray(padding)
    }

    fun sendOscMessage(ip: String, port: Int, path: String, value: Int) {
        buildAndSend(ip, port, path, ",i") {
            putInt(value)
        }
    }

    fun sendOscMessage(ip: String, port: Int, path: String, value: Float) {
        buildAndSend(ip, port, path, ",f") {
            putFloat(value)
        }
    }

    private fun buildAndSend(
        ip: String,
        port: Int,
        path: String,
        typeTag: String,
        putValue: ByteBuffer.() -> Unit
    ) {
        require(path.startsWith("/")) { "OSC path must start with /" }

        val address = InetAddress.getByName(ip)

        val pathBytes = padString(path)
        val typeTagBytes = padString(typeTag)
        val valueBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).apply(putValue).array()

        val messageBytes = pathBytes + typeTagBytes + valueBytes

        println("=== OSC MESSAGE DEBUG ===")
        println("Path bytes: ${pathBytes.joinToString(" ") { String.format("%02X", it) }}")
        println("Type tag bytes: ${typeTagBytes.joinToString(" ") { String.format("%02X", it) }}")
        println("Value bytes: ${valueBytes.joinToString(" ") { String.format("%02X", it) }}")
        println("Full message bytes: ${messageBytes.joinToString(" ") { String.format("%02X", it) }}")
        println("=========================")

        DatagramSocket().use { socket ->
            val packet = DatagramPacket(messageBytes, messageBytes.size, address, port)
            println("Sending UDP packet to ${address.hostAddress}:$port")
            socket.send(packet)
            println("Packet sent successfully!")
        }
    }
}
