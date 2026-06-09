package com.example.commov.data.local

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import kotlin.math.max

object ProfilePhotoImageReader {
    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 85

    fun readCompressedJpeg(context: Context, uri: Uri): ByteArray? {
        val resolver = context.applicationContext.contentResolver
        val rawBytes = readUriBytes(resolver, uri) ?: return null
        if (rawBytes.isEmpty()) {
            return null
        }

        val bitmap = decodeBitmap(resolver, uri, rawBytes) ?: return null
        return try {
            compressToJpeg(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    fun readRawBytes(context: Context, uri: Uri): ByteArray? {
        return readUriBytes(context.applicationContext.contentResolver, uri)
    }

    private fun readUriBytes(resolver: ContentResolver, uri: Uri): ByteArray? {
        runCatching {
            resolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull()?.takeIf { it.isNotEmpty() }?.let { return it }

        return runCatching {
            resolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                FileInputStream(parcelFileDescriptor.fileDescriptor).use { it.readBytes() }
            }
        }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private fun decodeBitmap(resolver: ContentResolver, uri: Uri, rawBytes: ByteArray): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodeWithImageDecoder(resolver, uri)?.let { return it }
            decodeWithImageDecoder(ByteBuffer.wrap(rawBytes))?.let { return it }
        }

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOptions)
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
            return null
        }

        val sampleSize = calculateSampleSize(boundsOptions.outWidth, boundsOptions.outHeight)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions)
    }

    private fun decodeWithImageDecoder(resolver: ContentResolver, uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return null
        }

        return runCatching {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        }.getOrNull()
    }

    private fun decodeWithImageDecoder(buffer: ByteBuffer): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return null
        }

        return runCatching {
            val source = ImageDecoder.createSource(buffer)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        }.getOrNull()
    }

    private fun compressToJpeg(bitmap: Bitmap): ByteArray? {
        return ByteArrayOutputStream().use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                null
            } else {
                output.toByteArray()
            }
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        val largestSide = max(width, height)
        while (largestSide / sampleSize > MAX_DIMENSION) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
