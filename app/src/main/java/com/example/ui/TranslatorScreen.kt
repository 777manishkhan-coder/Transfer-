package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EngineSettings
import com.example.model.IndicLanguage
import com.example.model.IndicLanguages
import com.example.model.TranslationResultData
import com.example.ui.theme.BorderMedium
import com.example.ui.theme.BorderStrong
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorContainerDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardSub
import com.example.ui.theme.SurfaceDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSlate

@Composable
fun TranslatorScreen(
    sourceLang: IndicLanguage,
    targetLang: IndicLanguage,
    sourceText: String,
    quickSearchQuery: String,
    translationState: AsyncState<TranslationResultData>,
    engineSettings: EngineSettings,
    isBookmarked: Boolean,
    onQuickSearchChange: (String) -> Unit,
    onLookupClick: () -> Unit,
    onOpenCameraScan: () -> Unit,
    onOpenFileUpload: () -> Unit,
    onOpenDictionary: () -> Unit,
    onOpenSourcePicker: () -> Unit,
    onOpenTargetPicker: () -> Unit,
    onSwapLanguages: () -> Unit,
    onQuickSwitchTarget: (IndicLanguage) -> Unit,
    onOpenOfflineManager: () -> Unit,
    onSourceTextChange: (String) -> Unit,
    onClearSourceText: () -> Unit,
    onSpeakInput: () -> Unit,
    onPasteInput: () -> Unit,
    onTranslateClick: () -> Unit,
    onListenSpeech: (String, IndicLanguage) -> Unit,
    onCopyResult: (String) -> Unit,
    onShareResult: (String) -> Unit,
    onSaveBookmark: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onOpenGrammarTone: () -> Unit,
    onOpenWordBreakdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val installedPacks = IndicLanguages.all.filter { it.code in engineSettings.installedPackCodes }
    val totalInstalledMb = installedPacks.sumOf { it.packSizeMb }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Search & Multi-modal Input Options Bar
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Dictionary / Word Lookup
                    Surface(
                        color = SurfaceDim,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = TextSlate,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (quickSearchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.search_placeholder),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = quickSearchQuery,
                                    onValueChange = onQuickSearchChange,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                                    cursorBrush = SolidColor(PureWhite),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { onLookupClick() }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quick_search_input")
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onLookupClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = PureBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .heightIn(min = 36.dp)
                                    .testTag("lookup_action_btn")
                            ) {
                                Text(
                                    text = "Lookup",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    // 3 Distinct Multi-Modal Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MultimodalActionTile(
                            icon = Icons.Filled.DocumentScanner,
                            title = "Camera / Scan",
                            subtitle = "Sign & OCR",
                            onClick = onOpenCameraScan,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_camera")
                        )
                        MultimodalActionTile(
                            icon = Icons.Filled.UploadFile,
                            title = "PDF / Doc",
                            subtitle = "Translate Files",
                            onClick = onOpenFileUpload,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_doc")
                        )
                        MultimodalActionTile(
                            icon = Icons.Filled.ManageSearch,
                            title = "Dictionary",
                            subtitle = "Lexicon View",
                            onClick = onOpenDictionary,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_glossary")
                        )
                    }
                }
            }

            // 2. Focused Source & Target Language Selectors (1-to-1)
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // FROM (Source)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FROM (SOURCE)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSlate,
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                            )
                            Surface(
                                color = SurfaceDim,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(onClick = onOpenSourcePicker)
                                    .testTag("src_lang_picker_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sourceLang.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = PureWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = sourceLang.script,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.ExpandMore,
                                        contentDescription = "Select source language",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Swap Button
                        Box(modifier = Modifier.padding(top = 20.dp)) {
                            Surface(
                                color = PureWhite,
                                shape = CircleShape,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onSwapLanguages)
                                    .testTag("swap_lang_action")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.SyncAlt,
                                        contentDescription = stringResource(R.string.cd_swap_languages),
                                        tint = PureBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // TO (Target)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TO (TARGET)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSlate,
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                            )
                            Surface(
                                color = SurfaceCardSub,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BorderMedium),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(onClick = onOpenTargetPicker)
                                    .testTag("tgt_lang_picker_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = targetLang.displayTitle,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = PureWhite,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (targetLang.code in engineSettings.installedPackCodes) {
                                                Surface(
                                                    color = BorderSubtle,
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.5.dp, BorderStrong)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.OfflinePin,
                                                            contentDescription = null,
                                                            tint = PureWhite,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Text(
                                                            text = "${targetLang.packSizeMb}MB",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                            color = PureWhite
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Text(
                                            text = targetLang.script,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextSlate,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.ExpandMore,
                                        contentDescription = "Select target language",
                                        tint = PureWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Target Switcher Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Quick switch:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        IndicLanguages.quickSwitchTargets.forEach { lang ->
                            val isSelected = lang.code == targetLang.code
                            Surface(
                                color = if (isSelected) PureWhite else SurfaceCardSub,
                                shape = RoundedCornerShape(50),
                                border = if (isSelected) null else BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onQuickSwitchTarget(lang) }
                                    .testTag("quick_switch_${lang.code.lowercase()}")
                            ) {
                                Text(
                                    text = lang.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) PureBlack else TextSilver,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Offline Mode Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (engineSettings.offlineFirstMode) PureWhite else TextMuted)
                            )
                            Text(
                                text = if (engineSettings.offlineFirstMode) "Offline Mode Active" else "Cloud Neural Mode",
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhite
                            )
                            Text(
                                text = "• ${installedPacks.size} Languages (${totalInstalledMb} MB)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = SurfaceCardSub,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderMedium),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenOfflineManager)
                                .testTag("manage_offline_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DownloadDone,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Manage",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }

            // 3. Source Text Input Card
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite)
                            )
                            Text(
                                text = "SOURCE TEXT",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSilver
                            )
                        }
                        IconButton(
                            onClick = onClearSourceText,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceDim)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                .testTag("clear_input_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_clear_input),
                                tint = TextSlate,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 78.dp)
                    ) {
                        if (sourceText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.source_placeholder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted
                            )
                        }
                        BasicTextField(
                            value = sourceText,
                            onValueChange = onSourceTextChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = PureWhite),
                            cursorBrush = SolidColor(PureWhite),
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("source_text_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = SurfaceCardSub,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderMedium),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = onSpeakInput)
                                    .testTag("mic_action_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "Speak input",
                                        tint = PureWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Speak",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = PureWhite
                                    )
                                }
                            }

                            Surface(
                                color = SurfaceDim,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = onPasteInput)
                                    .testTag("paste_action_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentPaste,
                                        contentDescription = "Paste text",
                                        tint = TextSilver,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Paste",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSilver
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onTranslateClick,
                            enabled = translationState !is AsyncState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 42.dp)
                                .testTag("trigger_translate_btn")
                        ) {
                            if (translationState is AsyncState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = PureBlack,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Translating", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Text("TRANSLATE", style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. The SINGLE Dedicated Result Card (Target Language ONLY)
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderMedium),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("single_result_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Target Header & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCardSub)
                                    .border(1.dp, BorderMedium, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = targetLang.code,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PureWhite
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = targetLang.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PureWhite
                                    )
                                    if (targetLang.nativeName != targetLang.name) {
                                        Text(
                                            text = "(${targetLang.nativeName})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSilver
                                        )
                                    }
                                }
                                Text(
                                    text = "${targetLang.script} • Direct Indic Neural Translation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSlate,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            color = PureWhite,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PureBlack)
                                )
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureBlack
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))

                    when (translationState) {
                        is AsyncState.Idle -> {
                            Text(
                                text = "Tap Translate to generate ${targetLang.name} output.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSlate
                            )
                        }

                        is AsyncState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = PureWhite,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Translating into ${targetLang.name}...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSilver
                                )
                            }
                        }

                        is AsyncState.Error -> {
                            Surface(
                                color = ErrorContainerDark,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = translationState.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PureWhite
                                    )
                                    Button(
                                        onClick = onTranslateClick,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PureWhite,
                                            contentColor = PureBlack
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Retry Translation", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }

                        is AsyncState.Success -> {
                            val result = translationState.data
                            // Big Beautiful Translated Text
                            Text(
                                text = result.translatedText,
                                style = MaterialTheme.typography.headlineMedium,
                                color = PureWhite,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("translated_main_text")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Phonetic English Guide
                            if (engineSettings.autoShowPhonetics && result.phoneticGuide.isNotBlank()) {
                                Surface(
                                    color = SurfaceDim,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceCardSub)
                                                .border(1.dp, BorderMedium, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.RecordVoiceOver,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "PHONETIC ENGLISH GUIDE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PureWhite
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "\"${result.phoneticGuide}\"",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontStyle = FontStyle.Italic
                                                ),
                                                color = TextSilver
                                            )
                                        }
                                    }
                                }
                            }

                            if (result.literalNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = result.literalNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSlate
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Primary Output Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { onListenSpeech(result.translatedText, targetLang) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PureWhite,
                                        contentColor = PureBlack
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                    modifier = Modifier
                                        .heightIn(min = 44.dp)
                                        .testTag("tts_listen_action")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = PureBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Listen Speech",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ResultUtilityIconButton(
                                        icon = Icons.Filled.ContentCopy,
                                        contentDescription = stringResource(R.string.cd_copy_result),
                                        onClick = { onCopyResult(result.translatedText) },
                                        tag = "copy_result_btn"
                                    )
                                    ResultUtilityIconButton(
                                        icon = Icons.Filled.Share,
                                        contentDescription = stringResource(R.string.cd_share_result),
                                        onClick = {
                                            onShareResult(
                                                "${result.translatedText}\n(${result.phoneticGuide})"
                                            )
                                        },
                                        tag = "share_result_btn"
                                    )
                                    ResultUtilityIconButton(
                                        icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = stringResource(R.string.cd_save_result),
                                        onClick = onSaveBookmark,
                                        tag = "save_result_btn",
                                        highlighted = isBookmarked
                                    )
                                    ResultUtilityIconButton(
                                        icon = Icons.Filled.Fullscreen,
                                        contentDescription = stringResource(R.string.cd_fullscreen_result),
                                        onClick = onOpenFullscreen,
                                        tag = "fullscreen_result_btn"
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Single-Language Deep Dive Tools
                    Text(
                        text = "DEEP TOOLS FOR SELECTED LANGUAGE",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeepToolTile(
                            icon = Icons.Filled.Spellcheck,
                            title = "Grammar & Tone",
                            subtitle = "Formal vs Casual",
                            onClick = onOpenGrammarTone,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_grammar_modal")
                        )
                        DeepToolTile(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Word Breakdown",
                            subtitle = "Lexicon analysis",
                            onClick = onOpenWordBreakdown,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_breakdown_modal")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MultimodalActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceCardSub,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .heightIn(min = 92.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard)
                    .border(1.dp, BorderMedium, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = PureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = TextSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ResultUtilityIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tag: String,
    highlighted: Boolean = false
) {
    Surface(
        color = if (highlighted) PureWhite else SurfaceCardSub,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (highlighted) PureWhite else BorderSubtle),
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (highlighted) PureBlack else TextSilver,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DeepToolTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDim,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
            .heightIn(min = 58.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCardSub)
                    .border(1.dp, BorderMedium, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
