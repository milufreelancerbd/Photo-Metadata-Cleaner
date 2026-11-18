package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppStrings
import com.example.data.PhotoCleanerEngine
import com.example.data.PhotoCleanerException
import com.example.model.AppLanguage
import com.example.model.CleanerErrorReason
import com.example.model.CleanerResult
import com.example.model.CleanerUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CleanerViewModel(application: Application) : AndroidViewModel(application) {
  private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  private val engine = PhotoCleanerEngine(application)

  private val _language = MutableStateFlow(loadSavedLanguage())
  val language: StateFlow<AppLanguage> = _language.asStateFlow()

  private val _uiState = MutableStateFlow<CleanerUiState>(CleanerUiState.Empty)
  val uiState: StateFlow<CleanerUiState> = _uiState.asStateFlow()

  private val _howItWorksExpanded = MutableStateFlow(false)
  val howItWorksExpanded: StateFlow<Boolean> = _howItWorksExpanded.asStateFlow()

  private fun loadSavedLanguage(): AppLanguage {
    val saved = prefs.getString(KEY_LANG, AppLanguage.EN.code)
    return if (saved == AppLanguage.BN.code) AppLanguage.BN else AppLanguage.EN
  }

  fun setLanguage(lang: AppLanguage) {
    _language.value = lang
    prefs.edit().putString(KEY_LANG, lang.code).apply()
  }

  fun toggleHowItWorks() {
    _howItWorksExpanded.value = !_howItWorksExpanded.value
  }

  fun onPhotoPicked(uri: Uri) {
    viewModelScope.launch {
      try {
        val selection = engine.inspectPhoto(uri)
        _uiState.value = CleanerUiState.Selected(selection)
      } catch (e: PhotoCleanerException) {
        _uiState.value = CleanerUiState.Error(e.reason)
      } catch (e: Exception) {
        _uiState.value = CleanerUiState.Error(CleanerErrorReason.DECODE_FAILED)
      }
    }
  }

  fun removeHiddenData() {
    val current = _uiState.value as? CleanerUiState.Selected ?: return
    val selection = current.selection
    _uiState.value = CleanerUiState.Cleaning(selection)

    viewModelScope.launch {
      val startedAt = System.currentTimeMillis()
      try {
        val result = engine.cleanPhoto(selection)
        val elapsed = System.currentTimeMillis() - startedAt
        if (elapsed < 420L) {
          delay(420L - elapsed)
        }
        _uiState.value = CleanerUiState.Done(result = result)
      } catch (e: PhotoCleanerException) {
        _uiState.value = CleanerUiState.Error(e.reason)
      } catch (e: Exception) {
        _uiState.value = CleanerUiState.Error(CleanerErrorReason.DECODE_FAILED)
      }
    }
  }

  fun saveToGallery(onShareFallback: (Intent) -> Unit) {
    val done = _uiState.value as? CleanerUiState.Done ?: return
    if (done.savedToGallery) return

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      val shareIntent = createShareIntent(done.result)
      if (shareIntent != null) {
        onShareFallback(shareIntent)
      } else {
        val copy = AppStrings.copyFor(_language.value)
        _uiState.value = done.copy(statusMessage = copy.errSave)
      }
      return
    }

    viewModelScope.launch {
      try {
        engine.saveToGallery(done.result)
        val copy = AppStrings.copyFor(_language.value)
        _uiState.value =
          done.copy(savedToGallery = true, statusMessage = copy.savedToGallery)
      } catch (e: Exception) {
        val copy = AppStrings.copyFor(_language.value)
        _uiState.value = done.copy(statusMessage = copy.errSave)
      }
    }
  }

  fun writeDownloadedCopy(destinationUri: Uri) {
    val done = _uiState.value as? CleanerUiState.Done ?: return
    viewModelScope.launch {
      try {
        engine.writeToDestinationUri(done.result, destinationUri)
        val copy = AppStrings.copyFor(_language.value)
        _uiState.value = done.copy(statusMessage = copy.savedCopyMessage)
      } catch (e: Exception) {
        val copy = AppStrings.copyFor(_language.value)
        _uiState.value = done.copy(statusMessage = copy.errSave)
      }
    }
  }

  fun createShareIntent(result: CleanerResult? = (_uiState.value as? CleanerUiState.Done)?.result): Intent? {
    val target = result ?: return null
    val copy = AppStrings.copyFor(_language.value)
    return engine.createShareIntent(target, copy.shareChooserTitle)
  }

  fun resetToEmpty() {
    _uiState.value = CleanerUiState.Empty
  }

  companion object {
    private const val PREFS_NAME = "pmc_prefs"
    private const val KEY_LANG = "pmc_lang"
  }
}
