import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (readln()) {
            "hide" -> hideMessage()
            "show" -> showMessage()
            "exit" -> {
                println("Bye!")
                return
            }

            else -> println("Wrong task")
        }
    }
}

fun hideMessage() {
    println("Input image file:")
    val inputFile = File(readln())
    println("Output image file:")
    val outputFile = File(readln())
    println("Message to hide:")
    val message = readln()
    println("Password:")
    val password = readln()

    val image = ImageIO.read(inputFile)
    val encryptedMessage =
        encryptMessage(message.encodeToByteArray(), password.encodeToByteArray()) + byteArrayOf(0, 0, 3)
    val messageBits = encryptedMessage.flatMap { byte ->
        (7 downTo 0).map { bit -> (byte.toInt() shr bit) and 1 == 1 }
    }

    if (!checkMessageLengthForImage(messageBits, image)) return

    hideMessageBitsForImage(image, messageBits)

    ImageIO.write(image, "png", outputFile)
    println("Message saved in ${outputFile.name} image.")
}

private fun encryptMessage(message: ByteArray, password: ByteArray): ByteArray {
    val encryptedMessage = ByteArray(message.size)
    for (i in message.indices) {
        encryptedMessage[i] = (message[i] xor password[i % password.size])
    }
    return encryptedMessage
}


private fun hideMessageBitsForImage(image: BufferedImage?, messageBits: List<Boolean>) {
    var bitIndex = 0
    if (image != null) {
        loop@ for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                if (bitIndex >= messageBits.size) break@loop
                val color = image.getRGB(x, y)
                val blue = color and 0xFF
                val newBlue = if (messageBits[bitIndex]) blue or 1 else blue and 0xFE
                val newColor = (color and 0xFFFF00) or newBlue
                image.setRGB(x, y, newColor)
                bitIndex++
            }
        }
    }
}

private fun checkMessageLengthForImage(
    messageBits: List<Boolean>,
    image: BufferedImage?,
): Boolean {
    if (image != null) {
        if (messageBits.size > image.width * image.height) {
            println("The input image is not large enough to hold this message.")
            return false
        }
    }
    return true
}

fun showMessage() {
    println("Input image file:")
    val inputFile = File(readln())
    println("Password:")
    val password = readln()

    val image = ImageIO.read(inputFile)

    val messageBytes = mutableListOf<Byte>()
    var currentByte = 0
    var bitCount = 0

    loop@ for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val color = image.getRGB(x, y)
            val blue = color and 0xFF
            currentByte = (currentByte shl 1) or (blue and 1)
            bitCount++
            if (bitCount == 8) {
                messageBytes.add(currentByte.toByte())
                if (messageBytes.size >= 3 &&
                    messageBytes.takeLast(3) == listOf<Byte>(0, 0, 3)
                ) {
                    break@loop
                }
                currentByte = 0
                bitCount = 0
            }
        }
    }

    if (messageBytes.size >= 3) {
        val encryptedMessage = messageBytes.dropLast(3).toByteArray()
        val decryptedMessage = decryptMessage(encryptedMessage, password.encodeToByteArray())
        val message = decryptedMessage.toString(Charsets.UTF_8)
        println("Message:")
        println(message)
    } else {
        println("No hidden message found.")
    }
}

private fun decryptMessage(encryptedMessage: ByteArray, password: ByteArray): ByteArray {
    val decryptedMessage = ByteArray(encryptedMessage.size)
    for (i in encryptedMessage.indices) {
        decryptedMessage[i] = (encryptedMessage[i] xor password[i % password.size])
    }
    return decryptedMessage
}
