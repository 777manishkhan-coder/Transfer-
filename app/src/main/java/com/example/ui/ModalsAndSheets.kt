package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.DictionaryEntry
import com.example.model.EngineSettings
import com.example.model.GrammarToneAnalysis
import com.example.model.IndicLanguage
import com.example.model.IndicLanguages
import com.example.model.InfoSourceType
import com.example.model.OcrTranslationResult
import com.example.model.WordBreakdownItem
import com.example.ui.theme.BorderMedium
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ErrorContainerDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardSub
import com.example.ui.theme.SurfaceDim
import com.example.ui.theme.SurfacePitch
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextSlate
import com.google.firebase.auth.FirebaseUser

@Composable
fun BolIndiaTopBar(
    offlineModeActive: Boolean,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfacePitch.copy(alpha = 0.96f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCardSub)
                            .border(1.dp, BorderMedium, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = stringResource(R.string.app_name),
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = PureWhite
                        )
                        Surface(
                            color = SurfaceCardSub,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, BorderMedium)
                        ) {
                            Text(
                                text = stringResource(R.string.badge_one_to_one),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        if (offlineModeActive) {
                            Surface(
                                color = SurfaceCardSub,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, BorderMedium)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudDone,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "Offline",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextSilver
                                    )
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = SurfaceCard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenSettings)
                            .testTag("engine_settings_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = stringResource(R.string.cd_engine_settings),
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        color = SurfaceCard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenProfile)
                            .testTag("profile_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = stringResource(R.string.cd_user_profile),
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = BorderSubtle)
        }
    }
}

@Composable
fun FullscreenTranslationModal(
    targetLang: IndicLanguage,
    translatedText: String,
    phoneticGuide: String,
    onListenSpeech: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(onBack = onDismiss)
        Surface(
            color = SurfacePitch,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                        )
                        Text(
                            text = "${targetLang.displayTitle} • ${targetLang.script}",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceCardSub)
                            .testTag("close_fullscreen_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close fullscreen display",
                            tint = PureWhite
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = translatedText,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 36.sp,
                            lineHeight = 48.sp
                        ),
                        color = PureWhite
                    )
                    if (phoneticGuide.isNotBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "\"$phoneticGuide\"",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = TextSilver
                        )
                    }
                }

                Button(
                    onClick = onListenSpeech,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Listen Speech", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerSheet(
    title: String,
    selectedCode: String,
    installedCodes: Set<String>,
    onSelect: (IndicLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) IndicLanguages.all
        else IndicLanguages.all.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.nativeName.contains(q, ignoreCase = true) ||
                it.script.contains(q, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        contentColor = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = PureWhite)

            Surface(
                color = SurfaceDim,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSlate, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text("Search Indic language or script...", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                            cursorBrush = SolidColor(PureWhite),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.code }) { lang ->
                    val isSelected = lang.code == selectedCode
                    val isOffline = lang.code in installedCodes
                    Surface(
                        color = if (isSelected) PureWhite else SurfaceCardSub,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isSelected) PureWhite else BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelect(lang)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lang.displayTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isSelected) PureBlack else PureWhite
                                )
                                Text(
                                    text = "${lang.script} • ${lang.packSizeMb} MB offline pack",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) PureBlack.copy(alpha = 0.7f) else TextSlate
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = "Selected", tint = PureBlack)
                            } else if (isOffline) {
                                Icon(Icons.Filled.DownloadDone, contentDescription = "Offline ready", tint = TextSilver, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflinePacksSheet(
    installedCodes: Set<String>,
    onTogglePack: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        contentColor = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Offline Language Packs", style = MaterialTheme.typography.titleLarge, color = PureWhite)
            val totalMb = IndicLanguages.all.filter { it.code in installedCodes }.sumOf { it.packSizeMb }
            Text(
                text = "${installedCodes.size} languages configured ($totalMb MB cached for low-connectivity travel)",
                style = MaterialTheme.typography.bodySmall,
                color = TextSlate
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(IndicLanguages.all, key = { it.code }) { lang ->
                    val installed = lang.code in installedCodes
                    Surface(
                        color = SurfaceCardSub,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (installed) BorderMedium else BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lang.displayTitle, style = MaterialTheme.typography.titleSmall, color = PureWhite)
                                Text("${lang.script} • ${lang.packSizeMb} MB", style = MaterialTheme.typography.bodySmall, color = TextSlate)
                            }
                            Button(
                                onClick = { onTogglePack(lang.code) },
                                enabled = lang.code != "EN",
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (installed) SurfaceDim else PureWhite,
                                    contentColor = if (installed) PureWhite else PureBlack
                                ),
                                border = if (installed) BorderStroke(1.dp, BorderMedium) else null,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (installed) Icons.Filled.DownloadDone else Icons.Filled.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (installed) "Installed" else "Install",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryModalSheet(
    query: String,
    targetLang: IndicLanguage,
    state: AsyncState<DictionaryEntry>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onListenSpeech: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        contentColor = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Indic Lexicon • ${targetLang.name}",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = SurfaceDim,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                        if (query.isEmpty()) {
                            Text("Enter word or idiom...", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                            cursorBrush = SolidColor(PureWhite),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Button(
                    onClick = onSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Text("Lookup", style = MaterialTheme.typography.labelLarge)
                }
            }

            when (state) {
                is AsyncState.Idle -> {
                    Text(
                        text = "Search any Indic or English word, phrase, or idiom to inspect meanings, usage examples, and cultural expressions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSlate
                    )
                }
                is AsyncState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Consulting lexicon...", color = TextSilver)
                    }
                }
                is AsyncState.Error -> {
                    Surface(
                        color = ErrorContainerDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f))
                    ) {
                        Text(state.message, color = PureWhite, modifier = Modifier.padding(14.dp))
                    }
                }
                is AsyncState.Success -> {
                    val entry = state.data
                    Surface(
                        color = SurfaceDim,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.word, style = MaterialTheme.typography.headlineSmall, color = PureWhite)
                                    Text(
                                        text = "${entry.transliteration} • ${entry.partOfSpeech}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSlate
                                    )
                                }
                                IconButton(onClick = { onListenSpeech(entry.word) }) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = "Listen", tint = PureWhite)
                                }
                            }
                            HorizontalDivider(color = BorderSubtle)
                            Text("MEANINGS", style = MaterialTheme.typography.labelSmall, color = TextSlate)
                            entry.meanings.forEach { m ->
                                Text("• $m", style = MaterialTheme.typography.bodyMedium, color = PureWhite)
                            }
                            if (entry.examples.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text("EXAMPLES", style = MaterialTheme.typography.labelSmall, color = TextSlate)
                                entry.examples.forEach { ex ->
                                    Text("“$ex”", style = MaterialTheme.typography.bodySmall, color = TextSilver)
                                }
                            }
                            if (entry.idioms.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text("IDIOMS & EXPRESSIONS", style = MaterialTheme.typography.labelSmall, color = TextSlate)
                                entry.idioms.forEach { id ->
                                    Text("• $id", style = MaterialTheme.typography.bodySmall, color = TextSlate)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarToneModalSheet(
    targetLang: IndicLanguage,
    state: AsyncState<GrammarToneAnalysis>,
    onListenSpeech: (String) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        contentColor = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Grammar & Tone • ${targetLang.name}",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite
            )
            when (state) {
                is AsyncState.Idle, is AsyncState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Analyzing formal vs casual registers...", color = TextSilver)
                    }
                }
                is AsyncState.Error -> {
                    Surface(
                        color = ErrorContainerDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f))
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.message, color = PureWhite)
                            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack)) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is AsyncState.Success -> {
                    val data = state.data
                    ToneVariantBox(
                        badge = "FORMAL / RESPECTFUL",
                        text = data.formalVersion,
                        phonetic = data.formalPhonetic,
                        onListen = { onListenSpeech(data.formalVersion) }
                    )
                    ToneVariantBox(
                        badge = "CASUAL / EVERYDAY",
                        text = data.casualVersion,
                        phonetic = data.casualPhonetic,
                        onListen = { onListenSpeech(data.casualVersion) }
                    )
                    if (data.grammarNotes.isNotEmpty()) {
                        Surface(
                            color = SurfaceDim,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("GRAMMAR & HONORIFIC NOTES", style = MaterialTheme.typography.labelSmall, color = TextSlate)
                                data.grammarNotes.forEach { note ->
                                    Text("• $note", style = MaterialTheme.typography.bodySmall, color = TextSilver)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToneVariantBox(
    badge: String,
    text: String,
    phonetic: String,
    onListen: () -> Unit
) {
    Surface(
        color = SurfaceDim,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderMedium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = SurfaceCardSub, shape = RoundedCornerShape(6.dp)) {
                    Text(badge, style = MaterialTheme.typography.labelSmall, color = PureWhite, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                IconButton(onClick = onListen, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "Listen", tint = PureWhite, modifier = Modifier.size(18.dp))
                }
            }
            Text(text, style = MaterialTheme.typography.titleLarge, color = PureWhite)
            Text("\"$phonetic\"", style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic), color = TextSilver)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordBreakdownModalSheet(
    targetLang: IndicLanguage,
    state: AsyncState<List<WordBreakdownItem>>,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        contentColor = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Word Breakdown • ${targetLang.name}",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite
            )
            when (state) {
                is AsyncState.Idle, is AsyncState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Analyzing lexicon tokens...", color = TextSilver)
                    }
                }
                is AsyncState.Error -> {
                    Surface(
                        color = ErrorContainerDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f))
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.message, color = PureWhite)
                            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack)) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is AsyncState.Success -> {
                    state.data.forEach { item ->
                        Surface(
                            color = SurfaceDim,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.token, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                                    Text(
                                        text = "${item.transliteration} • Root: ${item.rootWord}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSlate
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 10.dp)) {
                                    Text(item.meaning, style = MaterialTheme.typography.labelLarge, color = PureWhite)
                                    Text(item.partOfSpeech, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScannerModalSheet(
    targetLang: IndicLanguage,
    state: AsyncState<OcrTranslationResult>,
    sourceType: InfoSourceType,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onPickFile: () -> Unit,
    onApplyToTranslator: (OcrTranslationResult) -> Unit,
    onSaveToVault: (OcrTranslationResult) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        contentColor = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Scan Sign, Photo or Document → ${targetLang.name}",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPickCamera,
                    colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Camera", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onPickGallery,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardSub, contentColor = PureWhite),
                    border = BorderStroke(1.dp, BorderMedium),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Photo", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onPickFile,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardSub, contentColor = PureWhite),
                    border = BorderStroke(1.dp, BorderMedium),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PDF/Doc", style = MaterialTheme.typography.labelMedium)
                }
            }

            when (state) {
                is AsyncState.Idle -> {
                    Text(
                        text = "Capture a street sign, menu, handwritten note, or select a PDF/document to extract and translate into ${targetLang.displayTitle}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSlate
                    )
                }
                is AsyncState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Extracting & translating into ${targetLang.name}...", color = TextSilver)
                    }
                }
                is AsyncState.Error -> {
                    Surface(
                        color = ErrorContainerDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f))
                    ) {
                        Text(state.message, color = PureWhite, modifier = Modifier.padding(14.dp))
                    }
                }
                is AsyncState.Success -> {
                    val res = state.data
                    Surface(
                        color = SurfaceDim,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BorderMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("EXTRACTED SOURCE TEXT", style = MaterialTheme.typography.labelSmall, color = TextSlate)
                            Text(res.extractedSourceText, style = MaterialTheme.typography.bodyMedium, color = TextSilver)
                            HorizontalDivider(color = BorderSubtle)
                            Text("TRANSLATION (${targetLang.code})", style = MaterialTheme.typography.labelSmall, color = PureWhite)
                            Text(res.translatedText, style = MaterialTheme.typography.titleLarge, color = PureWhite)
                            if (res.phoneticGuide.isNotBlank()) {
                                Text("\"${res.phoneticGuide}\"", style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic), color = TextSilver)
                            }
                            if (res.contextSummary.isNotBlank()) {
                                Text(res.contextSummary, style = MaterialTheme.typography.bodySmall, color = TextSlate)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onApplyToTranslator(res)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardSub, contentColor = PureWhite),
                            border = BorderStroke(1.dp, BorderMedium),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Use in Translator", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = { onSaveToVault(res) },
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save to Vault", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineSettingsSheet(
    settings: EngineSettings,
    onToggleFormal: (Boolean) -> Unit,
    onTogglePhonetics: (Boolean) -> Unit,
    onToggleOfflineFirst: (Boolean) -> Unit,
    onToggleAutoCollect: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        contentColor = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Engine Settings", style = MaterialTheme.typography.titleLarge, color = PureWhite)
            SettingSwitchRow(
                title = "Prefer Polite / Formal Register",
                subtitle = "Default translations to respectful Indic honorifics",
                checked = settings.preferFormalTone,
                onCheckedChange = onToggleFormal
            )
            SettingSwitchRow(
                title = "Show Phonetic English Guide",
                subtitle = "Display Latin pronunciation guide below every result",
                checked = settings.autoShowPhonetics,
                onCheckedChange = onTogglePhonetics
            )
            SettingSwitchRow(
                title = "Offline-First Indicator",
                subtitle = "Prioritize installed language pack status",
                checked = settings.offlineFirstMode,
                onCheckedChange = onToggleOfflineFirst
            )
            SettingSwitchRow(
                title = "Auto-Collect AI Chat Insights",
                subtitle = "Automatically save structured chatbot extractions to Vault",
                checked = settings.autoCollectFromChat,
                onCheckedChange = onToggleAutoCollect
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = SurfaceDim,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = PureWhite)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSlate)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureBlack,
                    checkedTrackColor = PureWhite,
                    uncheckedThumbColor = TextSlate,
                    uncheckedTrackColor = SurfaceCardSub
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileModalSheet(
    user: FirebaseUser,
    savedCount: Int,
    collectedCount: Int,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard,
        contentColor = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PureWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user.displayName?.firstOrNull() ?: user.email?.firstOrNull() ?: 'B')
                            .uppercaseChar().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = PureBlack
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName?.ifBlank { "BolIndia User" } ?: "BolIndia User",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email ?: "Signed in with Google",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SurfaceDim,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(collectedCount.toString(), style = MaterialTheme.typography.headlineSmall, color = PureWhite)
                        Text("Collected Info Cards", style = MaterialTheme.typography.bodySmall, color = TextSlate)
                    }
                }
                Surface(
                    color = SurfaceDim,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(savedCount.toString(), style = MaterialTheme.typography.headlineSmall, color = PureWhite)
                        Text("Saved Bookmarks", style = MaterialTheme.typography.bodySmall, color = TextSlate)
                    }
                }
            }

            Button(
                onClick = {
                    onDismiss()
                    onSignOut()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceCardSub,
                    contentColor = PureWhite
                ),
                border = BorderStroke(1.dp, BorderMedium),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_btn")
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.sign_out), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
