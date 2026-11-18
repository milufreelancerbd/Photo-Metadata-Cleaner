package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppCopy
import com.example.data.AppStrings
import com.example.model.AppLanguage
import com.example.model.CleanerUiState
import com.example.model.MetadataItem
import com.example.model.MetadataKind
import com.example.ui.theme.LocalCleanerPalette

@Composable
fun CleanerScreen(viewModel: CleanerViewModel = viewModel()) {
  val context = LocalContext.current
  val palette = LocalCleanerPalette.current
  val language by viewModel.language.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val howItWorksExpanded by viewModel.howItWorksExpanded.collectAsStateWithLifecycle()
  val copy = remember(language) { AppStrings.copyFor(language) }

  val photoPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia(),
      onResult = { uri -> if (uri != null) viewModel.onPhotoPicked(uri) },
    )

  val createDocumentLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("*/*"),
      onResult = { uri -> if (uri != null) viewModel.writeDownloadedCopy(uri) },
    )

  fun launchPhotoPicker() {
    photoPickerLauncher.launch(
      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    )
  }

  BackHandler(enabled = uiState !is CleanerUiState.Empty) {
    viewModel.resetToEmpty()
  }

  Scaffold(
    containerColor = palette.paper,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
      ) {
        TopBrandBar(
          brandName = copy.brandName,
          language = language,
          onSelectLanguage = viewModel::setLanguage,
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = copy.appTitle,
          style = MaterialTheme.typography.headlineMedium,
          color = palette.ink,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = copy.appSubtitle,
          style = MaterialTheme.typography.bodyLarge,
          color = palette.inkSoft,
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
          targetState = uiState,
          transitionSpec = {
            (fadeIn(animationSpec = tween(200)) +
              slideInVertically(animationSpec = tween(200)) { it / 18 })
              .togetherWith(fadeOut(animationSpec = tween(160)))
          },
          contentKey = { it::class },
          label = "CleanerStageTransition",
          modifier = Modifier.weight(1f, fill = false),
        ) { state ->
          when (state) {
            is CleanerUiState.Empty ->
              EmptyStage(
                copy = copy,
                onPickPhoto = ::launchPhotoPicker,
              )

            is CleanerUiState.Selected ->
              SelectedStage(
                state = state,
                language = language,
                copy = copy,
                onChangePhoto = ::launchPhotoPicker,
                onRemoveMetadata = viewModel::removeHiddenData,
              )

            is CleanerUiState.Cleaning ->
              CleaningStage(copy = copy)

            is CleanerUiState.Done ->
              DoneStage(
                state = state,
                language = language,
                copy = copy,
                onSaveToGallery = {
                  viewModel.saveToGallery { fallbackIntent ->
                    context.startActivity(fallbackIntent)
                  }
                },
                onDownload = {
                  createDocumentLauncher.launch(state.result.cleanedFileName)
                },
                onShare = {
                  viewModel.createShareIntent()?.let { intent ->
                    context.startActivity(intent)
                  }
                },
                onCleanAnother = viewModel::resetToEmpty,
              )

            is CleanerUiState.Error ->
              ErrorStage(
                message = AppStrings.errorMessage(state.reason, language),
                copy = copy,
                onTryAgain = viewModel::resetToEmpty,
              )
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        CleanerFooter(
          copy = copy,
          language = language,
          expanded = howItWorksExpanded,
          onToggle = viewModel::toggleHowItWorks,
        )
      }
    }
  }
}

@Composable
private fun TopBrandBar(
  brandName: String,
  language: AppLanguage,
  onSelectLanguage: (AppLanguage) -> Unit,
) {
  val palette = LocalCleanerPalette.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f),
    ) {
      Icon(
        imageVector = Icons.Outlined.VerifiedUser,
        contentDescription = null,
        tint = palette.successText,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = brandName,
        style = MaterialTheme.typography.labelMedium,
        color = palette.inkSoft,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Surface(
      shape = CircleShape,
      color = palette.panel,
      border = BorderStroke(1.dp, palette.rule),
      shadowElevation = 1.dp,
    ) {
      Row(
        modifier = Modifier.padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LanguageChip(
          label = "English",
          selected = language == AppLanguage.EN,
          onClick = { onSelectLanguage(AppLanguage.EN) },
          modifier = Modifier.testTag("lang_en_button"),
        )
        LanguageChip(
          label = "বাংলা",
          selected = language == AppLanguage.BN,
          onClick = { onSelectLanguage(AppLanguage.BN) },
          modifier = Modifier.testTag("lang_bn_button"),
        )
      }
    }
  }
}

@Composable
private fun LanguageChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalCleanerPalette.current
  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .clip(CircleShape)
        .background(if (selected) palette.ink else Color.Transparent)
        .clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = if (selected) palette.panel else palette.inkSoft,
    )
  }
}

@Composable
private fun EmptyStage(
  copy: AppCopy,
  onPickPhoto: () -> Unit,
) {
  val palette = LocalCleanerPalette.current
  val dashColor = palette.ruleStrong
  val cardShape = RoundedCornerShape(22.dp)

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(min = 270.dp)
          .clip(cardShape)
          .background(palette.panel)
          .drawBehind {
            val strokeWidth = 1.5.dp.toPx()
            val radius = 22.dp.toPx()
            drawRoundRect(
              color = dashColor,
              style =
                Stroke(
                  width = strokeWidth,
                  pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f),
                ),
              cornerRadius = CornerRadius(radius, radius),
            )
          }
          .clickable(
            role = Role.Button,
            onClickLabel = copy.choosePhoto,
            onClick = onPickPhoto,
          )
          .padding(horizontal = 24.dp, vertical = 32.dp)
          .testTag("dropzone_card"),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(62.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(palette.panelSubtle)
              .border(1.dp, palette.rule, RoundedCornerShape(18.dp)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = palette.inkSoft,
            modifier = Modifier.size(30.dp),
          )
        }

        Text(
          text = copy.dropzoneTitle,
          style = MaterialTheme.typography.titleMedium,
          color = palette.ink,
          textAlign = TextAlign.Center,
        )

        Text(
          text = copy.dropzoneHint,
          style = MaterialTheme.typography.bodySmall,
          color = palette.inkSoft,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
          onClick = onPickPhoto,
          shape = RoundedCornerShape(16.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = palette.accent,
              contentColor = Color.White,
            ),
          contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
          modifier =
            Modifier
              .fillMaxWidth()
              .heightIn(min = 54.dp)
              .testTag("choose_photo_button"),
        ) {
          Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = copy.choosePhoto, style = MaterialTheme.typography.labelLarge)
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Outlined.Shield,
        contentDescription = null,
        tint = palette.successText,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = copy.trustNote,
        style = MaterialTheme.typography.bodySmall,
        color = palette.inkSoft,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun SelectedStage(
  state: CleanerUiState.Selected,
  language: AppLanguage,
  copy: AppCopy,
  onChangePhoto: () -> Unit,
  onRemoveMetadata: () -> Unit,
) {
  val palette = LocalCleanerPalette.current
  val selection = state.selection

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = palette.panel,
      border = BorderStroke(1.dp, palette.rule),
      shadowElevation = 1.dp,
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .heightIn(min = 140.dp, max = 270.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(palette.panelSubtle),
          contentAlignment = Alignment.Center,
        ) {
          val bmp = selection.previewBitmap
          if (bmp != null) {
            Image(
              bitmap = bmp.asImageBitmap(),
              contentDescription = copy.previewAlt,
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxWidth().heightIn(max = 270.dp),
            )
          }
        }

        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(start = 6.dp, end = 2.dp, top = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text =
              "${selection.fileName} · ${AppStrings.formatBytes(selection.originalSizeBytes, language)}",
            style = MaterialTheme.typography.bodySmall,
            color = palette.inkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          TextButton(
            onClick = onChangePhoto,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("change_photo_button"),
          ) {
            Text(
              text = copy.changePhoto,
              style = MaterialTheme.typography.labelMedium,
              color = palette.inkSoft,
            )
          }
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(22.dp),
      color = palette.panel,
      border = BorderStroke(1.dp, palette.rule),
      shadowElevation = 1.dp,
    ) {
      Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(
          text = copy.hiddenDataFound,
          style = MaterialTheme.typography.titleMedium,
          color = palette.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (selection.tags.isEmpty()) {
          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(11.dp))
                  .background(palette.successSoft),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = palette.successText,
                modifier = Modifier.size(18.dp),
              )
            }
            Column {
              Text(
                text = copy.noHiddenDataTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.successText,
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = copy.noHiddenDataSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.inkSoft,
              )
            }
          }
        } else {
          selection.tags.forEachIndexed { index, item ->
            if (index > 0) {
              HorizontalDivider(
                color = palette.rule,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 10.dp),
              )
            }
            MetadataTagRow(
              item = item,
              language = language,
              sensitiveLabel = copy.sensitiveTag,
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Button(
      onClick = onRemoveMetadata,
      shape = RoundedCornerShape(16.dp),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = palette.accent,
          contentColor = Color.White,
        ),
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(min = 54.dp)
          .testTag("remove_metadata_button"),
    ) {
      Icon(
        imageVector = Icons.Outlined.Shield,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(text = copy.removeHiddenData, style = MaterialTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun MetadataTagRow(
  item: MetadataItem,
  language: AppLanguage,
  sensitiveLabel: String,
) {
  val palette = LocalCleanerPalette.current
  val (label, value) = remember(item, language) { AppStrings.describeTag(item, language) }
  val icon = remember(item.kind) { iconForMetadataKind(item.kind) }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
      modifier =
        Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(11.dp))
          .background(if (item.sensitive) palette.accentSoft else palette.panelSubtle),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (item.sensitive) palette.accentText else palette.inkSoft,
        modifier = Modifier.size(18.dp),
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        color = palette.ink,
      )
    }

    if (item.sensitive) {
      Surface(
        shape = CircleShape,
        color = palette.accentSoft,
      ) {
        Text(
          text = sensitiveLabel,
          style = MaterialTheme.typography.labelMedium,
          color = palette.accentText,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun CleaningStage(copy: AppCopy) {
  val palette = LocalCleanerPalette.current

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = palette.panel,
      border = BorderStroke(1.dp, palette.rule),
      shadowElevation = 1.dp,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier =
            Modifier
              .size(56.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(palette.accentSoft),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = palette.accentText,
            modifier = Modifier.size(26.dp),
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LinearProgressIndicator(
          color = palette.accent,
          trackColor = palette.rule,
          modifier =
            Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(CircleShape),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = copy.cleaningOnDevice,
          style = MaterialTheme.typography.titleMedium,
          color = palette.ink,
          textAlign = TextAlign.Center,
        )
      }
    }

    Button(
      onClick = {},
      enabled = false,
      shape = RoundedCornerShape(16.dp),
      colors =
        ButtonDefaults.buttonColors(
          disabledContainerColor = palette.accent.copy(alpha = 0.72f),
          disabledContentColor = Color.White,
        ),
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(min = 54.dp)
          .testTag("cleaning_button"),
    ) {
      Text(text = copy.cleaningOnDevice, style = MaterialTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun DoneStage(
  state: CleanerUiState.Done,
  language: AppLanguage,
  copy: AppCopy,
  onSaveToGallery: () -> Unit,
  onDownload: () -> Unit,
  onShare: () -> Unit,
  onCleanAnother: () -> Unit,
) {
  val palette = LocalCleanerPalette.current
  val result = state.result
  var revealed by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) { revealed = true }

  val checkScale by
    animateFloatAsState(
      targetValue = if (revealed) 1f else 0.72f,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.68f),
      label = "DoneCheckScale",
    )

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = palette.panel,
      border = BorderStroke(1.dp, palette.rule),
      shadowElevation = 1.dp,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier =
            Modifier
              .size(68.dp)
              .scale(checkScale)
              .clip(CircleShape)
              .background(palette.successSoft),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = palette.successText,
            modifier = Modifier.size(34.dp),
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = copy.doneTitle,
          style = MaterialTheme.typography.titleLarge,
          color = palette.ink,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = AppStrings.formatDoneSummary(result.removedCount, language),
          style = MaterialTheme.typography.bodyLarge,
          color = palette.inkSoft,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          shape = CircleShape,
          color = palette.panelSubtle,
          border = BorderStroke(1.dp, palette.rule),
        ) {
          Text(
            text =
              "${AppStrings.formatBytes(result.originalSizeBytes, language)} → ${AppStrings.formatBytes(result.cleanedSizeBytes, language)}",
            style = MaterialTheme.typography.bodySmall,
            color = palette.inkSoft,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Button(
      onClick = onSaveToGallery,
      shape = RoundedCornerShape(16.dp),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = palette.success,
          contentColor = Color.White,
        ),
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(min = 54.dp)
          .testTag("save_gallery_button"),
    ) {
      Icon(
        imageVector = Icons.Outlined.Check,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = if (state.savedToGallery) copy.savedToGallery else copy.saveToGallery,
        style = MaterialTheme.typography.labelLarge,
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      OutlinedButton(
        onClick = onDownload,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, palette.ruleStrong),
        colors =
          ButtonDefaults.outlinedButtonColors(
            containerColor = palette.panel,
            contentColor = palette.ink,
          ),
        modifier =
          Modifier
            .weight(1f)
            .heightIn(min = 54.dp)
            .testTag("download_button"),
      ) {
        Icon(
          imageVector = Icons.Outlined.Download,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = copy.download, style = MaterialTheme.typography.labelLarge)
      }

      OutlinedButton(
        onClick = onShare,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, palette.ruleStrong),
        colors =
          ButtonDefaults.outlinedButtonColors(
            containerColor = palette.panel,
            contentColor = palette.ink,
          ),
        modifier =
          Modifier
            .weight(1f)
            .heightIn(min = 54.dp)
            .testTag("share_button"),
      ) {
        Icon(
          imageVector = Icons.Outlined.Share,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = copy.share, style = MaterialTheme.typography.labelLarge)
      }
    }

    if (!state.statusMessage.isNullOrBlank()) {
      Text(
        text = state.statusMessage,
        style = MaterialTheme.typography.bodySmall,
        color = palette.successText,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
      )
    }

    TextButton(
      onClick = onCleanAnother,
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(min = 48.dp)
          .testTag("clean_another_button"),
    ) {
      Text(
        text = copy.cleanAnotherPhoto,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = palette.inkSoft,
      )
    }
  }
}

@Composable
private fun ErrorStage(
  message: String,
  copy: AppCopy,
  onTryAgain: () -> Unit,
) {
  val palette = LocalCleanerPalette.current

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = palette.panel,
      border = BorderStroke(1.dp, palette.rule),
      shadowElevation = 1.dp,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier =
            Modifier
              .size(56.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(palette.accentSoft),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = palette.accentText,
            modifier = Modifier.size(28.dp),
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = message,
          style = MaterialTheme.typography.bodyLarge,
          color = palette.ink,
          textAlign = TextAlign.Center,
        )
      }
    }

    Button(
      onClick = onTryAgain,
      shape = RoundedCornerShape(16.dp),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = palette.accent,
          contentColor = Color.White,
        ),
      modifier =
        Modifier
          .fillMaxWidth()
          .heightIn(min = 54.dp)
          .testTag("try_again_button"),
    ) {
      Text(text = copy.tryAgain, style = MaterialTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun CleanerFooter(
  copy: AppCopy,
  language: AppLanguage,
  expanded: Boolean,
  onToggle: () -> Unit,
) {
  val palette = LocalCleanerPalette.current
  val chevronRotation by
    animateFloatAsState(
      targetValue = if (expanded) 180f else 0f,
      animationSpec = tween(200),
      label = "HowItWorksChevron",
    )

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    HorizontalDivider(color = palette.rule, thickness = 1.dp)

    Text(
      text = copy.footerLine,
      style = MaterialTheme.typography.bodySmall,
      color = palette.inkSoft,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    )

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = palette.panel,
      border = BorderStroke(1.dp, palette.rule),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .heightIn(min = 48.dp)
              .clickable(role = Role.Button, onClick = onToggle)
              .semantics { contentDescription = copy.howItWorks }
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .testTag("how_it_works_toggle"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = copy.howItWorks,
            style = MaterialTheme.typography.labelMedium,
            color = palette.inkSoft,
          )
          Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = palette.inkSoft,
            modifier = Modifier.size(18.dp).rotate(chevronRotation),
          )
        }

        AnimatedVisibility(visible = expanded) {
          Column(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            HorizontalDivider(color = palette.rule, thickness = 1.dp)
            HowItWorksStep(
              stepNum = AppStrings.toLocaleDigits("1", language),
              title = copy.step1Title,
              desc = copy.step1Desc,
            )
            HowItWorksStep(
              stepNum = AppStrings.toLocaleDigits("2", language),
              title = copy.step2Title,
              desc = copy.step2Desc,
            )
            HowItWorksStep(
              stepNum = AppStrings.toLocaleDigits("3", language),
              title = copy.step3Title,
              desc = copy.step3Desc,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HowItWorksStep(
  stepNum: String,
  title: String,
  desc: String,
) {
  val palette = LocalCleanerPalette.current
  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Box(
      modifier =
        Modifier
          .padding(top = 2.dp)
          .size(22.dp)
          .clip(CircleShape)
          .background(palette.panelSubtle)
          .border(1.dp, palette.rule, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = stepNum,
        style = MaterialTheme.typography.labelMedium,
        color = palette.ink,
      )
    }
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = palette.ink,
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall,
        color = palette.inkSoft,
      )
    }
  }
}

private fun iconForMetadataKind(kind: MetadataKind): ImageVector =
  when (kind) {
    MetadataKind.LOCATION -> Icons.Outlined.LocationOn
    MetadataKind.CAMERA, MetadataKind.EXIF -> Icons.Outlined.PhotoCamera
    MetadataKind.DATE -> Icons.Outlined.CalendarToday
    MetadataKind.SOFTWARE, MetadataKind.XMP -> Icons.Outlined.Tune
    MetadataKind.C2PA -> Icons.Outlined.Shield
    MetadataKind.ICC -> Icons.Outlined.Image
    MetadataKind.TEXT -> Icons.Outlined.Description
  }
