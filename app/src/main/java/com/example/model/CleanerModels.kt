package com.example.model

import android.graphics.Bitmap
import android.net.Uri

enum class AppLanguage(val code: String) {
  EN("en"),
  BN("bn"),
}

enum class MetadataKind {
  LOCATION,
  CAMERA,
  DATE,
  SOFTWARE,
  EXIF,
  XMP,
  C2PA,
  ICC,
  TEXT,
}

data class MetadataItem(
  val id: String,
  val kind: MetadataKind,
  val sensitive: Boolean = false,
  val rawValue: String? = null,
  val lat: Double? = null,
  val lon: Double? = null,
)

data class PhotoSelection(
  val uri: Uri,
  val fileName: String,
  val originalSizeBytes: Long,
  val mimeType: String,
  val tags: List<MetadataItem>,
  val previewBitmap: Bitmap?,
)

data class CleanerResult(
  val cleanedFilePath: String,
  val cleanedFileName: String,
  val mimeType: String,
  val originalSizeBytes: Long,
  val cleanedSizeBytes: Long,
  val removedCount: Int,
)

enum class CleanerErrorReason {
  UNSUPPORTED_FORMAT,
  FILE_TOO_LARGE,
  DECODE_FAILED,
  SAVE_FAILED,
}

sealed interface CleanerUiState {
  data object Empty : CleanerUiState

  data class Selected(val selection: PhotoSelection) : CleanerUiState

  data class Cleaning(val selection: PhotoSelection) : CleanerUiState

  data class Done(
    val result: CleanerResult,
    val savedToGallery: Boolean = false,
    val statusMessage: String? = null,
  ) : CleanerUiState

  data class Error(val reason: CleanerErrorReason) : CleanerUiState
}
