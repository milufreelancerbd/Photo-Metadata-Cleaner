package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.model.CleanerErrorReason
import com.example.model.CleanerResult
import com.example.model.PhotoSelection
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoCleanerException(val reason: CleanerErrorReason) : Exception(reason.name)

class PhotoCleanerEngine(private val context: Context) {

  companion object {
    const val MAX_FILE_BYTES = 50L * 1024L * 1024L
    private const val PREVIEW_MAX_DIM = 1400
    private const val CLEAN_MAX_DIM = 8192
  }

  suspend fun inspectPhoto(uri: Uri): PhotoSelection =
    withContext(Dispatchers.IO) {
      val resolver = context.contentResolver
      val rawMime = resolver.getType(uri)
      val (displayName, queriedSize) = queryDisplayMeta(uri)

      if (queriedSize > MAX_FILE_BYTES) {
        throw PhotoCleanerException(CleanerErrorReason.FILE_TOO_LARGE)
      }

      val bytes = readLimitedBytes(uri) ?: throw PhotoCleanerException(CleanerErrorReason.DECODE_FAILED)
      val detectedMime = MetadataScanner.detectMimeFromMagic(bytes) ?: rawMime
      if (detectedMime !in setOf("image/jpeg", "image/jpg", "image/png", "image/webp")) {
        throw PhotoCleanerException(CleanerErrorReason.UNSUPPORTED_FORMAT)
      }
      val normalizedMime = if (detectedMime == "image/jpg") "image/jpeg" else detectedMime!!

      val preview = decodeOrientedBitmap(bytes, PREVIEW_MAX_DIM)
        ?: throw PhotoCleanerException(CleanerErrorReason.DECODE_FAILED)

      val tags = MetadataScanner.scanImageBytes(bytes)
      val fileName = displayName.ifBlank { "photo${extensionFor(normalizedMime)}" }

      PhotoSelection(
        uri = uri,
        fileName = fileName,
        originalSizeBytes = bytes.size.toLong(),
        mimeType = normalizedMime,
        tags = tags,
        previewBitmap = preview,
      )
    }

  suspend fun cleanPhoto(selection: PhotoSelection): CleanerResult =
    withContext(Dispatchers.IO) {
      val bytes =
        readLimitedBytes(selection.uri)
          ?: throw PhotoCleanerException(CleanerErrorReason.DECODE_FAILED)

      val decoded =
        decodeOrientedBitmap(bytes, CLEAN_MAX_DIM)
          ?: throw PhotoCleanerException(CleanerErrorReason.DECODE_FAILED)

      // Redraw pixels onto a fresh Bitmap/Canvas so zero original metadata segments survive
      val freshBitmap =
        Bitmap.createBitmap(
          decoded.width,
          decoded.height,
          Bitmap.Config.ARGB_8888,
        )
      Canvas(freshBitmap).drawBitmap(decoded, 0f, 0f, null)
      if (freshBitmap !== decoded) {
        decoded.recycle()
      }

      val cleanedDir = File(context.cacheDir, "cleaned").apply { mkdirs() }
      // Clear older cached outputs to keep app cache clean
      cleanedDir.listFiles()?.forEach { old -> runCatching { old.delete() } }

      val baseName = selection.fileName.substringBeforeLast('.').ifBlank { "photo" }
      val ext = extensionFor(selection.mimeType)
      val cleanName = "$baseName-clean$ext"
      val outFile = File(cleanedDir, cleanName)

      val compressSuccess =
        FileOutputStream(outFile).use { fos ->
          when (selection.mimeType) {
            "image/png" -> freshBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            "image/webp" -> {
              val format =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                  Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                  @Suppress("DEPRECATION")
                  Bitmap.CompressFormat.WEBP
                }
              freshBitmap.compress(format, 95, fos)
            }
            else -> freshBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
          }
        }

      freshBitmap.recycle()

      if (!compressSuccess || !outFile.exists() || outFile.length() == 0L) {
        throw PhotoCleanerException(CleanerErrorReason.DECODE_FAILED)
      }

      CleanerResult(
        cleanedFilePath = outFile.absolutePath,
        cleanedFileName = cleanName,
        mimeType = selection.mimeType,
        originalSizeBytes = selection.originalSizeBytes,
        cleanedSizeBytes = outFile.length(),
        removedCount = selection.tags.size,
      )
    }

  suspend fun saveToGallery(result: CleanerResult): Uri? =
    withContext(Dispatchers.IO) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return@withContext null
      }
      val srcFile = File(result.cleanedFilePath)
      if (!srcFile.exists()) throw PhotoCleanerException(CleanerErrorReason.SAVE_FAILED)

      val resolver = context.contentResolver
      val values =
        ContentValues().apply {
          put(MediaStore.Images.Media.DISPLAY_NAME, result.cleanedFileName)
          put(MediaStore.Images.Media.MIME_TYPE, result.mimeType)
          put(
            MediaStore.Images.Media.RELATIVE_PATH,
            "${Environment.DIRECTORY_PICTURES}/PhotoCleaner",
          )
          put(MediaStore.Images.Media.IS_PENDING, 1)
        }

      val itemUri =
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
          ?: throw PhotoCleanerException(CleanerErrorReason.SAVE_FAILED)

      try {
        resolver.openOutputStream(itemUri)?.use { out ->
          srcFile.inputStream().use { input -> input.copyTo(out) }
        } ?: throw PhotoCleanerException(CleanerErrorReason.SAVE_FAILED)

        val doneValues = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
        resolver.update(itemUri, doneValues, null, null)
        itemUri
      } catch (e: Exception) {
        runCatching { resolver.delete(itemUri, null, null) }
        throw PhotoCleanerException(CleanerErrorReason.SAVE_FAILED)
      }
    }

  suspend fun writeToDestinationUri(result: CleanerResult, destinationUri: Uri) =
    withContext(Dispatchers.IO) {
      val srcFile = File(result.cleanedFilePath)
      if (!srcFile.exists()) throw PhotoCleanerException(CleanerErrorReason.SAVE_FAILED)
      context.contentResolver.openOutputStream(destinationUri)?.use { out ->
        srcFile.inputStream().use { input -> input.copyTo(out) }
      } ?: throw PhotoCleanerException(CleanerErrorReason.SAVE_FAILED)
    }

  fun createShareIntent(result: CleanerResult, chooserTitle: String): Intent? {
    val file = File(result.cleanedFilePath)
    if (!file.exists()) return null
    val contentUri =
      FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent =
      Intent(Intent.ACTION_SEND).apply {
        type = result.mimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
    return Intent.createChooser(shareIntent, chooserTitle)
  }

  private fun queryDisplayMeta(uri: Uri): Pair<String, Long> {
    var name = ""
    var size = 0L
    runCatching {
      context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (nameIdx >= 0) name = cursor.getString(nameIdx).orEmpty()
          if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
        }
      }
    }
    return name to size
  }

  private fun readLimitedBytes(uri: Uri): ByteArray? =
    runCatching {
      context.contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArrayOutputStream()
        val chunk = ByteArray(16 * 1024)
        var total = 0L
        while (true) {
          val read = input.read(chunk)
          if (read <= 0) break
          total += read
          if (total > MAX_FILE_BYTES) {
            throw PhotoCleanerException(CleanerErrorReason.FILE_TOO_LARGE)
          }
          buffer.write(chunk, 0, read)
        }
        buffer.toByteArray()
      }
    }.getOrElse { err ->
      if (err is PhotoCleanerException) throw err
      null
    }

  private fun decodeOrientedBitmap(bytes: ByteArray, maxDimension: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    var halfW = bounds.outWidth
    var halfH = bounds.outHeight
    while (halfW > maxDimension || halfH > maxDimension) {
      sampleSize *= 2
      halfW /= 2
      halfH /= 2
    }

    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts) ?: return null

    val orientation =
      runCatching {
        ExifInterface(ByteArrayInputStream(bytes))
          .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
      }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    val matrix = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
      ExifInterface.ORIENTATION_TRANSPOSE -> {
        matrix.postRotate(90f)
        matrix.postScale(-1f, 1f)
      }
      ExifInterface.ORIENTATION_TRANSVERSE -> {
        matrix.postRotate(270f)
        matrix.postScale(-1f, 1f)
      }
      else -> return rawBitmap
    }

    return runCatching {
      val rotated =
        Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
      if (rotated !== rawBitmap) rawBitmap.recycle()
      rotated
    }.getOrDefault(rawBitmap)
  }

  private fun extensionFor(mime: String): String =
    when (mime) {
      "image/png" -> ".png"
      "image/webp" -> ".webp"
      else -> ".jpg"
    }
}
