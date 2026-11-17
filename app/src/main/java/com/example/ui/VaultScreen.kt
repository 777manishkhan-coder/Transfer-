package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CollectedInfoItem
import com.example.model.IndicLanguage
import com.example.model.IndicLanguages
import com.example.model.InfoCategory
import com.example.model.InfoSourceType
import com.example.model.SavedTranslation
import com.example.ui.theme.BorderMedium
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

enum class VaultSection {
    COLLECTED_INFO,
    BOOKMARKS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultScreen(
    collectedItemsState: AsyncState<List<CollectedInfoItem>>,
    savedTranslationsState: AsyncState<List<SavedTranslation>>,
    onSaveManualItem: (String, InfoCategory, String, String) -> Unit,
    onUpdateCollectedItem: (String, String, InfoCategory, String, String) -> Unit,
    onDeleteCollectedItem: (String) -> Unit,
    onDeleteSavedTranslation: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onListenSpeech: (String, IndicLanguage) -> Unit,
    onLoadSavedIntoTranslator: (SavedTranslation) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSection by remember { mutableStateOf(VaultSection.COLLECTED_INFO) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<InfoCategory?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CollectedInfoItem?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Segmented Toggle: Collected Info vs Bookmarks
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VaultSegmentButton(
                        label = "Collected Info",
                        selected = activeSection == VaultSection.COLLECTED_INFO,
                        onClick = { activeSection = VaultSection.COLLECTED_INFO },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vault_tab_collected")
                    )
                    VaultSegmentButton(
                        label = "Bookmarks",
                        selected = activeSection == VaultSection.BOOKMARKS,
                        onClick = { activeSection = VaultSection.BOOKMARKS },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vault_tab_bookmarks")
                    )
                }
            }

            // Search Bar
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = TextSlate,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Filter saved notes & translations...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                            cursorBrush = SolidColor(PureWhite),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vault_search_input")
                        )
                    }
                }
            }

            if (activeSection == VaultSection.COLLECTED_INFO) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryFilterPill(
                        label = "All",
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )
                    InfoCategory.entries.forEach { cat ->
                        CategoryFilterPill(
                            label = cat.label,
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat }
                        )
                    }
                }

                when (collectedItemsState) {
                    is AsyncState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PureWhite)
                        }
                    }

                    is AsyncState.Error -> {
                        Surface(
                            color = ErrorContainerDark,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = collectedItemsState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    is AsyncState.Idle, is AsyncState.Success -> {
                        val allItems = (collectedItemsState as? AsyncState.Success)?.data ?: emptyList()
                        val filtered = allItems.filter { item ->
                            val matchesCat = selectedCategory == null ||
                                InfoCategory.fromRaw(item.category) == selectedCategory
                            val q = searchQuery.trim()
                            val matchesSearch = q.isEmpty() ||
                                item.title.contains(q, ignoreCase = true) ||
                                item.summary.contains(q, ignoreCase = true) ||
                                item.extractedFacts.any { it.contains(q, ignoreCase = true) }
                            matchesCat && matchesSearch
                        }

                        if (filtered.isEmpty()) {
                            EmptyVaultBox(
                                icon = Icons.Filled.FolderSpecial,
                                title = "No Collected Info Yet",
                                subtitle = "Use the AI Collect chatbot to extract facts from photos or text, or tap + to add a note."
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filtered, key = { it.id }) { item ->
                                    CollectedItemCard(
                                        item = item,
                                        onCopy = {
                                            val full = buildString {
                                                appendLine(item.title)
                                                appendLine(item.summary)
                                                item.extractedFacts.forEach { appendLine("• $it") }
                                            }
                                            onCopyText(full.trim())
                                        },
                                        onEdit = { editingItem = item },
                                        onDelete = { onDeleteCollectedItem(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                when (savedTranslationsState) {
                    is AsyncState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PureWhite)
                        }
                    }

                    is AsyncState.Error -> {
                        Surface(
                            color = ErrorContainerDark,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = savedTranslationsState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    is AsyncState.Idle, is AsyncState.Success -> {
                        val allSaved = (savedTranslationsState as? AsyncState.Success)?.data ?: emptyList()
                        val filtered = allSaved.filter { tr ->
                            val q = searchQuery.trim()
                            q.isEmpty() ||
                                tr.sourceText.contains(q, ignoreCase = true) ||
                                tr.translatedText.contains(q, ignoreCase = true) ||
                                tr.targetLang.contains(q, ignoreCase = true)
                        }

                        if (filtered.isEmpty()) {
                            EmptyVaultBox(
                                icon = Icons.Filled.Bookmark,
                                title = "No Saved Bookmarks",
                                subtitle = "Tap the Bookmark button on any translated card to keep essential phrases synced here."
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 84.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filtered, key = { it.id }) { tr ->
                                    SavedTranslationCard(
                                        item = tr,
                                        onListen = {
                                            onListenSpeech(
                                                tr.translatedText,
                                                IndicLanguages.byCode(tr.targetCode)
                                            )
                                        },
                                        onCopy = { onCopyText(tr.translatedText) },
                                        onLoadInTranslator = { onLoadSavedIntoTranslator(tr) },
                                        onDelete = { onDeleteSavedTranslation(tr.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeSection == VaultSection.COLLECTED_INFO) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PureWhite,
                contentColor = PureBlack,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("vault_add_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add collected note")
            }
        }
    }

    if (showCreateDialog) {
        CollectedItemEditorDialog(
            initialItem = null,
            onDismiss = { showCreateDialog = false },
            onSave = { title, category, summary, factsText ->
                onSaveManualItem(title, category, summary, factsText)
                showCreateDialog = false
            }
        )
    }

    val itemToEdit = editingItem
    if (itemToEdit != null) {
        CollectedItemEditorDialog(
            initialItem = itemToEdit,
            onDismiss = { editingItem = null },
            onSave = { title, category, summary, factsText ->
                onUpdateCollectedItem(itemToEdit.id, title, category, summary, factsText)
                editingItem = null
            }
        )
    }
}

@Composable
private fun VaultSegmentButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) PureWhite else SurfaceCard,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) PureBlack else TextSlate
            )
        }
    }
}

@Composable
private fun CategoryFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) PureWhite else SurfaceCardSub,
        shape = RoundedCornerShape(50),
        border = if (selected) null else BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) PureBlack else TextSilver,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun CollectedItemCard(
    item: CollectedInfoItem,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SurfaceCardSub,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, BorderMedium)
                    ) {
                        Text(
                            text = InfoCategory.fromRaw(item.category).label,
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = SurfaceDim,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            text = InfoSourceType.fromRaw(item.sourceType).label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSlate,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy collected info",
                            tint = TextSilver,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit collected info",
                            tint = TextSilver,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete collected info",
                            tint = TextSlate,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite
            )
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSilver
            )

            if (item.extractedFacts.isNotEmpty()) {
                HorizontalDivider(color = BorderSubtle)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.extractedFacts.forEach { fact ->
                        Text(
                            text = "• $fact",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedTranslationCard(
    item: SavedTranslation,
    onListen: () -> Unit,
    onCopy: () -> Unit,
    onLoadInTranslator: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onLoadInTranslator)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardSub)
                            .border(1.dp, BorderMedium, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.targetCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite
                        )
                    }
                    Text(
                        text = "${item.sourceLang} → ${item.targetLang}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(onClick = onListen, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "Listen to saved translation",
                            tint = PureWhite,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy saved translation",
                            tint = TextSilver,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Remove bookmark",
                            tint = TextSlate,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = item.sourceText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSlate
            )
            Text(
                text = item.translatedText,
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite
            )
            if (item.phoneticGuide.isNotBlank()) {
                Text(
                    text = "\"${item.phoneticGuide}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSilver
                )
            }
        }
    }
}

@Composable
private fun EmptyVaultBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCardSub)
                    .border(1.dp, BorderMedium, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSlate
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollectedItemEditorDialog(
    initialItem: CollectedInfoItem?,
    onDismiss: () -> Unit,
    onSave: (String, InfoCategory, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var summary by remember { mutableStateOf(initialItem?.summary ?: "") }
    var factsText by remember { mutableStateOf(initialItem?.extractedFacts?.joinToString("\n") ?: "") }
    var category by remember {
        mutableStateOf(
            if (initialItem != null) InfoCategory.fromRaw(initialItem.category) else InfoCategory.GENERAL
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        titleContentColor = PureWhite,
        textContentColor = TextSilver,
        title = {
            Text(
                text = if (initialItem == null) "Add Collected Info" else "Edit Collected Info",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = BorderMedium,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoCategory.entries.forEach { cat ->
                        CategoryFilterPill(
                            label = cat.label,
                            selected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary") },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = BorderMedium,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = factsText,
                    onValueChange = { factsText = it },
                    label = { Text("Key facts (one per line)") },
                    minLines = 2,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = BorderMedium,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, category, summary, factsText) },
                enabled = title.isNotBlank() && summary.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text("Save", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSlate)
            }
        }
    )
}
