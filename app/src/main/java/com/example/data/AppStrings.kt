package com.example.data

import com.example.model.AppLanguage
import com.example.model.CleanerErrorReason
import com.example.model.MetadataItem
import com.example.model.MetadataKind
import java.util.Locale

data class AppCopy(
  val brandName: String,
  val appTitle: String,
  val appSubtitle: String,
  val dropzoneTitle: String,
  val dropzoneHint: String,
  val choosePhoto: String,
  val trustNote: String,
  val previewAlt: String,
  val changePhoto: String,
  val hiddenDataFound: String,
  val noHiddenDataTitle: String,
  val noHiddenDataSubtitle: String,
  val sensitiveTag: String,
  val removeHiddenData: String,
  val cleaningOnDevice: String,
  val doneTitle: String,
  val itemsRemovedZero: String,
  val itemsRemovedOne: String,
  val itemsRemovedMany: String,
  val saveToGallery: String,
  val savedToGallery: String,
  val download: String,
  val share: String,
  val shareChooserTitle: String,
  val savedCopyMessage: String,
  val cleanAnotherPhoto: String,
  val tryAgain: String,
  val footerLine: String,
  val howItWorks: String,
  val step1Title: String,
  val step1Desc: String,
  val step2Title: String,
  val step2Desc: String,
  val step3Title: String,
  val step3Desc: String,
  val metaLocation: String,
  val metaCamera: String,
  val metaTaken: String,
  val metaSoftware: String,
  val metaExif: String,
  val metaExifVal: String,
  val metaXmp: String,
  val metaXmpVal: String,
  val metaC2pa: String,
  val metaC2paVal: String,
  val metaIcc: String,
  val metaIccVal: String,
  val metaText: String,
  val metaTextVal: String,
  val errUnsupported: String,
  val errTooLarge: String,
  val errDecode: String,
  val errSave: String,
)

object AppStrings {
  private val english =
    AppCopy(
      brandName = "Photo Metadata Cleaner",
      appTitle = "Photo Metadata Cleaner",
      appSubtitle = "Remove hidden data from your photos. Private, on your device.",
      dropzoneTitle = "Pick a photo from your device",
      dropzoneHint = "JPEG, PNG, or WebP",
      choosePhoto = "Choose a photo",
      trustNote = "Your photo never leaves your phone.",
      previewAlt = "Selected photo preview",
      changePhoto = "Change photo",
      hiddenDataFound = "Hidden data found",
      noHiddenDataTitle = "No hidden data found",
      noHiddenDataSubtitle = "We can still save a fresh copy to be completely sure.",
      sensitiveTag = "Sensitive",
      removeHiddenData = "Remove hidden data",
      cleaningOnDevice = "Cleaning on your device...",
      doneTitle = "Your photo is clean",
      itemsRemovedZero = "Fresh copy saved — no hidden data",
      itemsRemovedOne = "1 item removed",
      itemsRemovedMany = "%s items removed",
      saveToGallery = "Save to gallery",
      savedToGallery = "Saved to gallery",
      download = "Download",
      share = "Share",
      shareChooserTitle = "Share cleaned photo",
      savedCopyMessage = "Cleaned copy saved.",
      cleanAnotherPhoto = "Clean another photo",
      tryAgain = "Try again",
      footerLine = "Works offline. No account. No uploads.",
      howItWorks = "How does it work?",
      step1Title = "Checks inside the photo",
      step1Desc =
        "Reads hidden file tags like location, camera model, and date directly on your phone.",
      step2Title = "Draws a fresh copy",
      step2Desc =
        "Copies only the visible picture onto a clean canvas, leaving all hidden tags behind.",
      step3Title = "Saves straight to you",
      step3Desc =
        "Nothing is ever sent anywhere. Save or share the clean photo right away.",
      metaLocation = "Location",
      metaCamera = "Camera",
      metaTaken = "Taken",
      metaSoftware = "Editing app",
      metaExif = "Camera & location data",
      metaExifVal = "EXIF metadata",
      metaXmp = "Editing & app info",
      metaXmpVal = "XMP metadata",
      metaC2pa = "Content credentials",
      metaC2paVal = "AI or edit label",
      metaIcc = "Color profile",
      metaIccVal = "ICC data",
      metaText = "Embedded note",
      metaTextVal = "Text tag",
      errUnsupported =
        "That file type is not supported. Please choose a JPEG, PNG, or WebP photo.",
      errTooLarge =
        "This photo is too large for your device to process. Please try a smaller file.",
      errDecode = "We could not open this photo. Please try a different file.",
      errSave = "Could not save the cleaned photo. Please try Download or Share instead.",
    )

  private val bengali =
    AppCopy(
      brandName = "ফটো মেটাডাটা ক্লিনার",
      appTitle = "ছবির গোপন তথ্য মুছুন",
      appSubtitle =
        "আপনার ছবি থেকে লুকানো তথ্য সরিয়ে ফেলুন। সম্পূর্ণ ব্যক্তিগত, আপনার ফোনেই।",
      dropzoneTitle = "ফোন থেকে একটি ছবি বেছে নিন",
      dropzoneHint = "JPEG, PNG বা WebP",
      choosePhoto = "ছবি বেছে নিন",
      trustNote = "আপনার ছবি কখনোই আপনার ফোন থেকে বাইরে যায় না।",
      previewAlt = "নির্বাচিত ছবির প্রিভিউ",
      changePhoto = "অন্য ছবি নিন",
      hiddenDataFound = "লুকানো তথ্য পাওয়া গেছে",
      noHiddenDataTitle = "কোনো লুকানো তথ্য পাওয়া যায়নি",
      noHiddenDataSubtitle =
        "তবুও পুরোপুরি নিশ্চিত থাকতে একটি নতুন পরিষ্কার কপি তৈরি করতে পারেন।",
      sensitiveTag = "সংবেদনশীল",
      removeHiddenData = "গোপন তথ্য মুছুন",
      cleaningOnDevice = "আপনার ডিভাইসে পরিষ্কার করা হচ্ছে...",
      doneTitle = "আপনার ছবি এখন পরিষ্কার",
      itemsRemovedZero = "নতুন পরিষ্কার কপি তৈরি হয়েছে",
      itemsRemovedOne = "১টি তথ্য মুছে ফেলা হয়েছে",
      itemsRemovedMany = "%sটি তথ্য মুছে ফেলা হয়েছে",
      saveToGallery = "গ্যালারিতে সেভ করুন",
      savedToGallery = "গ্যালারিতে সেভ হয়েছে",
      download = "ডাউনলোড করুন",
      share = "শেয়ার করুন",
      shareChooserTitle = "পরিষ্কার ছবি শেয়ার করুন",
      savedCopyMessage = "পরিষ্কার ছবিটি সেভ করা হয়েছে।",
      cleanAnotherPhoto = "আরেকটি ছবি পরিষ্কার করুন",
      tryAgain = "আবার চেষ্টা করুন",
      footerLine = "অফলাইনেও চলে। অ্যাকাউন্ট লাগে না। কোনো আপলোড নেই।",
      howItWorks = "এটি কীভাবে কাজ করে?",
      step1Title = "ছবির ভেতরের তথ্য খোঁজে",
      step1Desc =
        "ফোনের ভেতরেই ছবির লোকেশন, ক্যামেরা মডেল ও তোলার সময় সংক্রান্ত লুকানো তথ্য পড়ে দেখে।",
      step2Title = "নতুন পরিষ্কার কপি বানায়",
      step2Desc =
        "শুধু দৃশ্যমান ছবিটি নতুন ক্যানভাসে এঁকে নেয়, ফলে লুকানো সব তথ্য বাদ পড়ে যায়।",
      step3Title = "সরাসরি আপনার কাছে সেভ হয়",
      step3Desc =
        "কোথাও কিছু পাঠানো হয় না। সাথে সাথেই পরিষ্কার ছবিটি সেভ বা শেয়ার করতে পারবেন।",
      metaLocation = "অবস্থান",
      metaCamera = "ক্যামেরা",
      metaTaken = "তোলার সময়",
      metaSoftware = "এডিটিং অ্যাপ",
      metaExif = "ক্যামেরা ও লোকেশন তথ্য",
      metaExifVal = "EXIF মেটাডাটা",
      metaXmp = "এডিটিং ও অ্যাপ তথ্য",
      metaXmpVal = "XMP মেটাডাটা",
      metaC2pa = "কনটেন্ট ক্রেডেনশিয়াল",
      metaC2paVal = "AI বা এডিটের তথ্য",
      metaIcc = "রঙের প্রোফাইল",
      metaIccVal = "ICC ডেটা",
      metaText = "ফাইলের ভেতরের লেখা",
      metaTextVal = "টেক্সট ট্যাগ",
      errUnsupported =
        "এই ধরনের ফাইল কাজ করবে না। অনুগ্রহ করে JPEG, PNG বা WebP ছবি বেছে নিন।",
      errTooLarge =
        "ছবিটি অনেক বড়। অনুগ্রহ করে তুলনামূলক ছোট একটি ছবি বেছে নিন।",
      errDecode = "ছবিটি খোলা যায়নি। অনুগ্রহ করে অন্য একটি ছবি দিয়ে চেষ্টা করুন।",
      errSave = "ছবিটি সেভ করা যায়নি। নিচের ডাউনলোড বা শেয়ার ব্যবহার করে দেখুন।",
    )

  private val monthsEn =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
  private val monthsBn =
    listOf(
      "জানু",
      "ফেব্রু",
      "মার্চ",
      "এপ্রিল",
      "মে",
      "জুন",
      "জুলাই",
      "আগস্ট",
      "সেপ্টে",
      "অক্টো",
      "নভে",
      "ডিসে",
    )
  private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

  fun copyFor(lang: AppLanguage): AppCopy =
    when (lang) {
      AppLanguage.EN -> english
      AppLanguage.BN -> bengali
    }

  fun toLocaleDigits(input: String, lang: AppLanguage): String {
    if (lang != AppLanguage.BN) return input
    val sb = StringBuilder(input.length)
    for (ch in input) {
      if (ch in '0'..'9') {
        sb.append(bnDigits[ch - '0'])
      } else {
        sb.append(ch)
      }
    }
    return sb.toString()
  }

  fun formatBytes(bytes: Long, lang: AppLanguage): String {
    val raw =
      when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
      }
    return toLocaleDigits(raw, lang)
  }

  fun formatExifDate(rawDate: String, lang: AppLanguage): String {
    val match = Regex("""^(\d{4}):(\d{2}):(\d{2})""").find(rawDate.trim()) ?: return rawDate
    val year = match.groupValues[1]
    val monthIdx = match.groupValues[2].toIntOrNull()?.minus(1) ?: return rawDate
    val day = match.groupValues[3].toIntOrNull() ?: return rawDate
    if (monthIdx !in 0..11 || day <= 0) return rawDate
    val month = if (lang == AppLanguage.BN) monthsBn[monthIdx] else monthsEn[monthIdx]
    return "${toLocaleDigits(day.toString(), lang)} $month ${toLocaleDigits(year, lang)}"
  }

  fun describeTag(tag: MetadataItem, lang: AppLanguage): Pair<String, String> {
    val copy = copyFor(lang)
    return when (tag.kind) {
      MetadataKind.LOCATION -> {
        val lat = tag.lat ?: 0.0
        val lon = tag.lon ?: 0.0
        val coords = String.format(Locale.US, "%.2f, %.2f", lat, lon)
        copy.metaLocation to toLocaleDigits(coords, lang)
      }
      MetadataKind.CAMERA -> copy.metaCamera to (tag.rawValue ?: copy.metaExifVal)
      MetadataKind.DATE ->
        copy.metaTaken to formatExifDate(tag.rawValue.orEmpty(), lang)
      MetadataKind.SOFTWARE -> copy.metaSoftware to (tag.rawValue ?: copy.metaXmpVal)
      MetadataKind.EXIF -> copy.metaExif to copy.metaExifVal
      MetadataKind.XMP -> copy.metaXmp to copy.metaXmpVal
      MetadataKind.C2PA -> copy.metaC2pa to copy.metaC2paVal
      MetadataKind.ICC -> copy.metaIcc to copy.metaIccVal
      MetadataKind.TEXT ->
        copy.metaText to (tag.rawValue?.takeIf { it.isNotBlank() } ?: copy.metaTextVal)
    }
  }

  fun formatDoneSummary(count: Int, lang: AppLanguage): String {
    val copy = copyFor(lang)
    return when (count) {
      0 -> copy.itemsRemovedZero
      1 -> copy.itemsRemovedOne
      else -> copy.itemsRemovedMany.replace("%s", toLocaleDigits(count.toString(), lang))
    }
  }

  fun errorMessage(reason: CleanerErrorReason, lang: AppLanguage): String {
    val copy = copyFor(lang)
    return when (reason) {
      CleanerErrorReason.UNSUPPORTED_FORMAT -> copy.errUnsupported
      CleanerErrorReason.FILE_TOO_LARGE -> copy.errTooLarge
      CleanerErrorReason.DECODE_FAILED -> copy.errDecode
      CleanerErrorReason.SAVE_FAILED -> copy.errSave
    }
  }
}
