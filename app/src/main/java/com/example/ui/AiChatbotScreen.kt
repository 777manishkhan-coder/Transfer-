package com.example.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ChatMessage
import com.example.model.ChatMessageRole
import com.example.model.InfoCategory
import com.example.model.InfoSourceType
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

@Composable
fun AiChatbotScreen(
    chatMessagesState: AsyncState<List<ChatMessage>>,
    isSending: Boolean,
    chatError: String?,
    autoCollectEnabled: Boolean,
    pendingPhotoUri: Uri?,
    pendingBitmap: Bitmap?,
    pendingPhotoSource: InfoSourceType,
    onToggleAutoCollect: (Boolean) -> Unit,
    onPickPhoto: () -> Unit,
    onCaptureCamera: () -> Unit,
    onClearPendingPhoto: () -> Unit,
    onSendMessage: (String) -> Unit,
    onCollectMessageInfo: (ChatMessage) -> Unit,
    onClearChatHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val messages = (chatMessagesState as? AsyncState.Success)?.data ?: emptyList()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = remember {
        listOf(
            "Extract key facts & prices from this photo",
            "Read & translate this Indic sign",
            "Summarize important dates & numbers",
            "Explain local dish & ingredients"
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
        ) {
            // Top Collector Header Strip
            Surface(
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCardSub)
                                .border(1.dp, BorderMedium, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AI Info Collector",
                                style = MaterialTheme.typography.titleSmall,
                                color = PureWhite
                            )
                            Text(
                                text = "Collect facts by text or photo",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSlate
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Auto-Save",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSilver
                        )
                        Switch(
                            checked = autoCollectEnabled,
                            onCheckedChange = onToggleAutoCollect,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureBlack,
                                checkedTrackColor = PureWhite,
                                uncheckedThumbColor = TextSlate,
                                uncheckedTrackColor = SurfaceDim
                            ),
                            modifier = Modifier.testTag("auto_collect_switch")
                        )
                        if (messages.isNotEmpty()) {
                            IconButton(
                                onClick = onClearChatHistory,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("clear_chat_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Clear chat history",
                                    tint = TextSlate,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (chatMessagesState) {
                    is AsyncState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PureWhite)
                        }
                    }

                    is AsyncState.Error -> {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = ErrorContainerDark,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = chatMessagesState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PureWhite,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    is AsyncState.Idle, is AsyncState.Success -> {
                        if (messages.isEmpty()) {
                            EmptyCollectorState(
                                quickPrompts = quickPrompts,
                                onSelectPrompt = { prompt -> inputText = prompt },
                                onPickPhoto = onPickPhoto,
                                onCaptureCamera = onCaptureCamera
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(messages, key = { it.id }) { msg ->
                                    ChatBubbleItem(
                                        message = msg,
                                        onCollect = { onCollectMessageInfo(msg) }
                                    )
                                }
                                if (isSending) {
                                    item(key = "typing_indicator") {
                                        Surface(
                                            color = SurfaceCard,
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, BorderSubtle)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = PureWhite,
                                                    strokeWidth = 2.dp
                                                )
                                                Text(
                                                    text = "Analyzing & extracting information...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSilver
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!chatError.isNullOrBlank()) {
                Surface(
                    color = ErrorContainerDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chatError,
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Bottom Composer Card
            Surface(
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderMedium),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Prompt Pills when empty
                    if (messages.isNotEmpty() && inputText.isEmpty() && pendingBitmap == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickPrompts.forEach { prompt ->
                                Surface(
                                    color = SurfaceCardSub,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { inputText = prompt }
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSilver,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Attached Photo Preview
                    if (pendingPhotoUri != null || pendingBitmap != null) {
                        Surface(
                            color = SurfaceDim,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderMedium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (pendingBitmap != null) {
                                        Image(
                                            bitmap = pendingBitmap.asImageBitmap(),
                                            contentDescription = "Selected photo preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else if (pendingPhotoUri != null) {
                                        AsyncImage(
                                            model = pendingPhotoUri,
                                            contentDescription = "Selected photo preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "${pendingPhotoSource.label} attached",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = "Ask anything or tap Send to collect info",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSlate,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onClearPendingPhoto,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove attached photo",
                                        tint = TextSilver,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Input & Multimodal Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = SurfaceCardSub,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !isSending, onClick = onPickPhoto)
                                .testTag("chat_pick_photo_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = stringResource(R.string.cd_attach_photo),
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            color = SurfaceCardSub,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !isSending, onClick = onCaptureCamera)
                                .testTag("chat_camera_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = stringResource(R.string.cd_capture_camera),
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            color = SurfaceDim,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Ask or collect info from text / photo...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                                    cursorBrush = SolidColor(PureWhite),
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (!isSending && (inputText.isNotBlank() || pendingBitmap != null)) {
                                                val toSend = inputText
                                                inputText = ""
                                                onSendMessage(toSend)
                                            }
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chat_text_input")
                                )
                            }
                        }

                        Surface(
                            color = if (isSending) SurfaceCardSub else PureWhite,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    enabled = !isSending && (inputText.isNotBlank() || pendingBitmap != null)
                                ) {
                                    val toSend = inputText
                                    inputText = ""
                                    onSendMessage(toSend)
                                }
                                .testTag("chat_send_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = PureWhite,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = stringResource(R.string.cd_send_message),
                                        tint = PureBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCollectorState(
    quickPrompts: List<String>,
    onSelectPrompt: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onCaptureCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCardSub)
                .border(1.dp, BorderMedium, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Collect Any Information",
            style = MaterialTheme.typography.titleLarge,
            color = PureWhite
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Snap a photo of a menu, bill, street sign, medicine label, or ask by text. AI extracts key facts and saves them to your Vault.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSlate
        )
        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onCaptureCamera,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Take Photo", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onPickPhoto,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceCardSub,
                    contentColor = PureWhite
                ),
                border = BorderStroke(1.dp, BorderMedium),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Gallery", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { prompt ->
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectPrompt(prompt) }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSilver,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    onCollect: () -> Unit
) {
    val isUser = ChatMessageRole.fromRaw(message.role) == ChatMessageRole.USER
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) PureWhite else SurfaceCard,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            border = if (isUser) null else BorderStroke(1.dp, BorderMedium),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (message.hasPhoto) {
                    Surface(
                        color = if (isUser) SurfaceDim else SurfaceCardSub,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = message.photoCaption ?: "Photo attached",
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite
                            )
                        }
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) PureBlack else PureWhite
                )

                // Structured Extracted Info Card inside Assistant Turn
                if (!isUser && (!message.extractedTitle.isNullOrBlank() || !message.extractedSummary.isNullOrBlank())) {
                    HorizontalDivider(color = BorderSubtle)
                    Surface(
                        color = SurfaceDim,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = message.extractedTitle ?: "Collected Insight",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PureWhite,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = SurfaceCardSub,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, BorderMedium)
                                ) {
                                    Text(
                                        text = InfoCategory.fromRaw(message.extractedCategory).label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSilver,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!message.extractedSummary.isNullOrBlank()) {
                                Text(
                                    text = message.extractedSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSilver
                                )
                            }

                            val facts = message.extractedFacts.orEmpty()
                            if (facts.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    facts.forEach { fact ->
                                        Text(
                                            text = "• $fact",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSlate
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onCollect,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = PureBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BookmarkAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Collect to Vault",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
