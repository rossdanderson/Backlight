@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight

fun UByteArray.cobsEncode(): UByteArray {
    var readIndex = 0
    var writeIndex = 1
    var codeIndex = 0
    var code: UByte = 1u

    val encodedBufferSize = size + size / 254 + 1
    val encodedBuffer = UByteArray(encodedBufferSize)

    while (readIndex < size) {
        if (get(readIndex) == 0.toUByte()) {
            encodedBuffer[codeIndex] = code
            code = 1u
            codeIndex = writeIndex++
            readIndex++
        } else {
            encodedBuffer[writeIndex++] = get(readIndex++)
            code++

            if (code == 0xFFu.toUByte()) {
                encodedBuffer[codeIndex] = code
                code = 1u
                codeIndex = writeIndex++
            }
        }
    }

    encodedBuffer[codeIndex] = code

    return encodedBuffer
}