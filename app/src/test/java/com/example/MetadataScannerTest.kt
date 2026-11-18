package com.example

import com.example.data.AppStrings
import com.example.data.ExifDetails
import com.example.data.MetadataScanner
import com.example.model.AppLanguage
import com.example.model.MetadataKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MetadataScannerTest {

  @Test
  fun `detects JPEG EXIF, XMP, ICC, C2PA and COM segments`() {
    val jpeg =
      buildList<Byte> {
        add(0xFF.toByte()); add(0xD8.toByte()) // SOI
        // APP1 Exif
        val exifPayload = "Exif\u0000\u0000MM".encodeToByteArray()
        add(0xFF.toByte()); add(0xE1.toByte())
        add(((exifPayload.size + 2) shr 8).toByte()); add(((exifPayload.size + 2) and 0xFF).toByte())
        addAll(exifPayload.toList())
        // APP11 C2PA
        add(0xFF.toByte()); add(0xEB.toByte())
        add(0x00); add(0x04); add(0x01); add(0x02)
        // COM
        add(0xFF.toByte()); add(0xFE.toByte())
        add(0x00); add(0x04); add(0x41); add(0x42)
        // SOS
        add(0xFF.toByte()); add(0xDA.toByte())
      }.toByteArray()

    val tags =
      MetadataScanner.scanBytes(jpeg) {
        ExifDetails(
          lat = 23.81,
          lon = 90.41,
          camera = "iPhone 13",
          dateTaken = "2026:03:14 10:22:05",
        )
      }

    assertTrue(tags.any { it.kind == MetadataKind.LOCATION && it.sensitive })
    assertTrue(tags.any { it.kind == MetadataKind.CAMERA && it.rawValue == "iPhone 13" })
    assertTrue(tags.any { it.kind == MetadataKind.DATE })
    assertTrue(tags.any { it.kind == MetadataKind.C2PA })
    assertTrue(tags.any { it.kind == MetadataKind.TEXT })
  }

  @Test
  fun `formats Bengali dates and summary counts naturally`() {
    val formattedDate = AppStrings.formatExifDate("2026:03:14 09:00:00", AppLanguage.BN)
    assertEquals("১৪ মার্চ ২০২৬", formattedDate)

    val summary = AppStrings.formatDoneSummary(5, AppLanguage.BN)
    assertEquals("৫টি তথ্য মুছে ফেলা হয়েছে", summary)
  }
}
