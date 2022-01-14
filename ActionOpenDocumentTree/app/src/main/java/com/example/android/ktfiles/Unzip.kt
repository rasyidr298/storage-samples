package com.example.android.ktfiles

import android.util.Log
import java.io.*
import java.util.zip.ZipFile
import androidx.documentfile.provider.DocumentFile

import android.R.string
import android.content.Context
import android.net.Uri
import android.os.Build
import java.nio.charset.Charset


object Unzip {

    @Throws(IOException::class)
    fun unzip(zipFilePath: File, destDirectory: String): String {
        val destDir = File(destDirectory)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        try {
            ZipFile(zipFilePath).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        val filePath = destDirectory + File.separator + entry.name
                        if (!entry.isDirectory) {
                            extractFile(input, filePath)
                        } else {
                            val dir = File(filePath)
                            dir.mkdir()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            return e.message!!
        }
        return "success"
    }

    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    private const val BUFFER_SIZE = 4096
}