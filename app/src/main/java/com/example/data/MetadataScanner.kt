package com.example.data

import android.media.ExifInterface
import com.example.model.MetadataItem
import com.example.model.MetadataKind
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ExifDetails(
  val lat: Double? = null,
  val lon: Double? = null,
  val camera: String? = null,
  val dateTaken: String? = null,
  val software: String? = null,
  val hasXmp: Boolean = false,
)

object MetadataScanner {
  private val PNG_SIGNATURE =
    byteArrayOf(
      0x89.toByte(),
      0x50,
      0x4E,
      0x47,
      0x0D,
      0x0A,
      0x1A,
      0x0A,
    )

  fun scanImageBytes(bytes: ByteArray): List<MetadataItem> =
    scanBytes(bytes) { rawBytes -> extractViaAndroidExif(rawBytes) }

  internal fun scanBytes(
    bytes: ByteArray,
    exifExtractor: (ByteArray) -> ExifDetails,
  ): List<MetadataItem> {
    if (bytes.size < 4) return emptyList()

    val items = mutableListOf<MetadataItem>()
    val seenIds = mutableSetOf<String>()

    fun addUnique(item: MetadataItem) {
      if (seenIds.add(item.id)) items.add(item)
    }

    val exifDetails =
      runCatching { exifExtractor(bytes) }.getOrElse { ExifDetails() }

    if (exifDetails.lat != null && exifDetails.lon != null) {
      addUnique(
        MetadataItem(
          id = "location",
          kind = MetadataKind.LOCATION,
          sensitive = true,
          lat = exifDetails.lat,
          lon = exifDetails.lon,
        )
      )
    }

    if (!exifDetails.camera.isNullOrBlank()) {
      addUnique(
        MetadataItem(
          id = "camera",
          kind = MetadataKind.CAMERA,
          rawValue = exifDetails.camera,
        )
      )
    }

    if (!exifDetails.dateTaken.isNullOrBlank()) {
      addUnique(
        MetadataItem(
          id = "date",
          kind = MetadataKind.DATE,
          rawValue = exifDetails.dateTaken,
        )
      )
    }

    if (!exifDetails.software.isNullOrBlank()) {
      addUnique(
        MetadataItem(
          id = "software",
          kind = MetadataKind.SOFTWARE,
          rawValue = exifDetails.software,
        )
      )
    }

    val containerTags = detectContainerTags(bytes)
    val hasSpecificExif =
      seenIds.any { it == "location" || it == "camera" || it == "date" || it == "software" }

    for (tag in containerTags) {
      if (tag.kind == MetadataKind.EXIF && hasSpecificExif) continue
      addUnique(tag)
    }

    if (exifDetails.hasXmp) {
      addUnique(MetadataItem(id = "xmp", kind = MetadataKind.XMP))
    }

    return items
  }

  private fun extractViaAndroidExif(bytes: ByteArray): ExifDetails {
    val exif = ExifInterface(ByteArrayInputStream(bytes))
    val latLong = FloatArray(2)
    @Suppress("DEPRECATION")
    val hasLatLong = exif.getLatLong(latLong)

    val make = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim().orEmpty()
    val model = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim().orEmpty()
    val camera =
      when {
        make.isNotEmpty() && model.isNotEmpty() ->
          if (model.lowercase().startsWith(make.lowercase())) model else "$make $model"
        model.isNotEmpty() -> model
        make.isNotEmpty() -> make
        else -> null
      }

    val dateTaken =
      exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)?.trim()?.takeIf { it.isNotEmpty() }
        ?: exif.getAttribute(ExifInterface.TAG_DATETIME)?.trim()?.takeIf { it.isNotEmpty() }

    val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)?.trim()?.takeIf { it.isNotEmpty() }
    val hasXmp = exif.getAttribute(ExifInterface.TAG_XMP) != null

    return ExifDetails(
      lat = if (hasLatLong) latLong[0].toDouble() else null,
      lon = if (hasLatLong) latLong[1].toDouble() else null,
      camera = camera,
      dateTaken = dateTaken,
      software = software,
      hasXmp = hasXmp,
    )
  }

  internal fun detectContainerTags(bytes: ByteArray): List<MetadataItem> {
    if (isJpeg(bytes)) return detectJpegTags(bytes)
    if (isPng(bytes)) return detectPngTags(bytes)
    if (isWebp(bytes)) return detectWebpTags(bytes)
    return emptyList()
  }

  fun detectMimeFromMagic(bytes: ByteArray): String? =
    when {
      isJpeg(bytes) -> "image/jpeg"
      isPng(bytes) -> "image/png"
      isWebp(bytes) -> "image/webp"
      else -> null
    }

  private fun isJpeg(bytes: ByteArray): Boolean =
    bytes.size >= 2 && (bytes[0].toInt() and 0xFF) == 0xFF && (bytes[1].toInt() and 0xFF) == 0xD8

  private fun isPng(bytes: ByteArray): Boolean {
    if (bytes.size < PNG_SIGNATURE.size) return false
    for (i in PNG_SIGNATURE.indices) {
      if (bytes[i] != PNG_SIGNATURE[i]) return false
    }
    return true
  }

  private fun isWebp(bytes: ByteArray): Boolean =
    bytes.size >= 12 &&
      readAscii(bytes, 0, 4) == "RIFF" &&
      readAscii(bytes, 8, 4) == "WEBP"

  private fun detectJpegTags(bytes: ByteArray): List<MetadataItem> {
    val tags = mutableListOf<MetadataItem>()
    var offset = 2
    while (offset + 4 <= bytes.size) {
      if ((bytes[offset].toInt() and 0xFF) != 0xFF) {
        offset++
        continue
      }
      val marker = bytes[offset + 1].toInt() and 0xFF
      if (marker == 0xD8 || marker == 0xD9 || marker == 0x01 || marker in 0xD0..0xD7) {
        offset += 2
        continue
      }
      if (marker == 0xDA) break
      val length = ((bytes[offset + 2].toInt() and 0xFF) shl 8) or (bytes[offset + 3].toInt() and 0xFF)
      if (length < 2) break
      val dataStart = offset + 4

      when (marker) {
        0xE1 -> {
          val exifSig = readAscii(bytes, dataStart, 6)
          if (exifSig.startsWith("Exif")) {
            tags.add(MetadataItem(id = "exif", kind = MetadataKind.EXIF, sensitive = true))
          } else {
            val xmpSig = readAscii(bytes, dataStart, 29)
            if (xmpSig.startsWith("http://ns.adobe.com/xap/")) {
              tags.add(MetadataItem(id = "xmp", kind = MetadataKind.XMP))
            }
          }
        }
        0xE2 -> {
          val iccSig = readAscii(bytes, dataStart, 11)
          if (iccSig.startsWith("ICC_PROFILE")) {
            tags.add(MetadataItem(id = "icc", kind = MetadataKind.ICC))
          }
        }
        0xEB -> tags.add(MetadataItem(id = "c2pa", kind = MetadataKind.C2PA))
        0xFE -> tags.add(MetadataItem(id = "com", kind = MetadataKind.TEXT))
      }

      val next = dataStart + length - 2
      if (next <= offset) break
      offset = next
    }
    return tags
  }

  private fun detectPngTags(bytes: ByteArray): List<MetadataItem> {
    val tags = mutableListOf<MetadataItem>()
    var offset = 8
    while (offset + 8 <= bytes.size) {
      val length =
        ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.BIG_ENDIAN).int
      if (length < 0) break
      val type = readAscii(bytes, offset + 4, 4)
      val dataStart = offset + 8
      if (type == "IEND") break

      when (type) {
        "tEXt", "iTXt", "zTXt" -> {
          val keyword = readNullTerminatedAscii(bytes, dataStart, 80)
          if (keyword == "XML:com.adobe.xmp") {
            tags.add(MetadataItem(id = "xmp", kind = MetadataKind.XMP))
          } else {
            tags.add(
              MetadataItem(
                id = "text_${keyword.ifEmpty { offset.toString() }}",
                kind = MetadataKind.TEXT,
                rawValue = keyword.takeIf { it.isNotEmpty() },
              )
            )
          }
        }
        "eXIf" -> tags.add(MetadataItem(id = "exif", kind = MetadataKind.EXIF, sensitive = true))
        "caBX" -> tags.add(MetadataItem(id = "c2pa", kind = MetadataKind.C2PA))
        "iCCP" -> tags.add(MetadataItem(id = "icc", kind = MetadataKind.ICC))
      }

      val next = dataStart.toLong() + length.toLong() + 4L
      if (next > bytes.size || next <= offset) break
      offset = next.toInt()
    }
    return tags
  }

  private fun detectWebpTags(bytes: ByteArray): List<MetadataItem> {
    val tags = mutableListOf<MetadataItem>()
    var offset = 12
    while (offset + 8 <= bytes.size) {
      val fourcc = readAscii(bytes, offset, 4)
      val size = ByteBuffer.wrap(bytes, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
      if (size < 0) break
      when (fourcc) {
        "EXIF" -> tags.add(MetadataItem(id = "exif", kind = MetadataKind.EXIF, sensitive = true))
        "XMP " -> tags.add(MetadataItem(id = "xmp", kind = MetadataKind.XMP))
      }
      val paddedSize = size.toLong() + (size % 2)
      val next = offset.toLong() + 8L + paddedSize
      if (next > bytes.size || next <= offset) break
      offset = next.toInt()
    }
    return tags
  }

  private fun readAscii(bytes: ByteArray, offset: Int, length: Int): String {
    if (offset < 0 || offset >= bytes.size) return ""
    val count = minOf(length, bytes.size - offset)
    val sb = StringBuilder(count)
    for (i in 0 until count) {
      sb.append((bytes[offset + i].toInt() and 0xFF).toChar())
    }
    return sb.toString()
  }

  private fun readNullTerminatedAscii(bytes: ByteArray, offset: Int, maxLen: Int): String {
    if (offset < 0 || offset >= bytes.size) return ""
    val limit = minOf(maxLen, bytes.size - offset)
    val sb = StringBuilder()
    for (i in 0 until limit) {
      val b = bytes[offset + i].toInt() and 0xFF
      if (b == 0) break
      sb.append(b.toChar())
    }
    return sb.toString()
  }
}
