package com.backend.data.api.domain
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class BinaryFile (private val binaryData: BinaryData){

    constructor(path: String) : this(BinaryData(path))

    fun getFileInBytes():ByteArray {
        val file = File(binaryData.path)
        val inputStream = FileInputStream(file)
        val bufferedImg = ImageIO.read(inputStream)
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImg, binaryData.fileType, outputStream)
        return outputStream.toByteArray()
    }
}