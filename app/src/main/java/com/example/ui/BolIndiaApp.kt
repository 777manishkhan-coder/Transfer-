package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.signOut
import com.example.data.BolIndiaFirestoreRepository
import com.example.data.GeminiAiRepository
import com.example.data.PreferencesRepository
import com.example.model.IndicLanguage
import com.example.model.IndicLanguages
import com.example.model.InfoSourceType
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfacePitch
import com.example.ui.theme.TextSlate
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ImageTargetPurpose {
    OCR_SCAN,
    AI_CHAT_PHOTO
}

@Composable
fun BolIndiaApp(
    user: FirebaseUser
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: BolIndiaViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                BolIndiaViewModel(
                    firestoreRepo = BolIndiaFirestoreRepository(app),
                    aiRepo = GeminiAiRepository(),
                    prefsRepo = PreferencesRepository(app),
                    currentUserId = user.uid
                )
            }
        }
    )

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val sourceLang by viewModel.sourceLang.collectAsStateWithLifecycle()
    val targetLang by viewModel.targetLang.collectAsStateWithLifecycle()
    val sourceText by viewModel.sourceText.collectAsStateWithLifecycle()
    val quickSearchQuery by viewModel.quickSearchQuery.collectAsStateWithLifecycle()
    val translationState by viewModel.translationState.collectAsStateWithLifecycle()
    val dictionaryState by viewModel.dictionaryState.collectAsStateWithLifecycle()
    val grammarToneState by viewModel.grammarToneState.collectAsStateWithLifecycle()
    val wordBreakdownState by viewModel.wordBreakdownState.collectAsStateWithLifecycle()
    val ocrState by viewModel.ocrState.collectAsStateWithLifecycle()
    val engineSettings by viewModel.engineSettings.collectAsStateWithLifecycle()
    val savedTranslationsState by viewModel.savedTranslationsState.collectAsStateWithLifecycle()
    val collectedItemsState by viewModel.collectedItemsState.collectAsStateWithLifecycle()
    val chatMessagesState by viewModel.chatMessagesState.collectAsStateWithLifecycle()
    val isChatSending by viewModel.isChatSending.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Back handler on secondary tabs returns to main Translate tab
    BackHandler(enabled = selectedTab != AppTab.TRANSLATE) {
        viewModel.selectTab(AppTab.TRANSLATE)
    }

    // Modal & sheet visibility state
    var showSourcePicker by remember { mutableStateOf(false) }
    var showTargetPicker by remember { mutableStateOf(false) }
    var showOfflinePacks by remember { mutableStateOf(false) }
    var showDictionaryModal by remember { mutableStateOf(false) }
    var showGrammarToneModal by remember { mutableStateOf(false) }
    var showWordBreakdownModal by remember { mutableStateOf(false) }
    var showOcrModal by remember { mutableStateOf(false) }
    var showFullscreen by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }

    // Pending photo state for AI Chatbot
    var pendingChatPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingChatBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingChatPhotoSource by remember { mutableStateOf(InfoSourceType.PHOTO) }
    var ocrSourceType by remember { mutableStateOf(InfoSourceType.CAMERA) }

    // Camera capture helper state
    var imagePurpose by remember { mutableStateOf(ImageTargetPurpose.OCR_SCAN) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Text-To-Speech lifecycle
    var ttsReady by remember { mutableStateOf(false) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
        ttsInstance = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            ttsInstance = null
        }
    }

    val speakText: (String, IndicLanguage) -> Unit = { text, lang ->
        val tts = ttsInstance
        if (tts != null && ttsReady && text.isNotBlank()) {
            val locale = Locale.forLanguageTag(lang.bcp47Tag)
            val langResult = tts.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.forLanguageTag("en-IN"))
                viewModel.showMessage("Playing with fallback voice (${lang.name} voice pack unavailable).")
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bolindia_${System.currentTimeMillis()}")
        } else {
            viewModel.showMessage("Speech synthesis is unavailable on this device.")
        }
    }

    // Speech-to-Text Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.updateSourceText(spoken)
                viewModel.translateCurrentText()
            }
        }
    }

    val handleBitmapPicked: (Uri, InfoSourceType, ImageTargetPurpose) -> Unit = { uri, srcType, purpose ->
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) { decodeBitmapFromUri(context, uri) }
            if (bitmap == null) {
                viewModel.showMessage("Could not read selected image.")
            } else if (purpose == ImageTargetPurpose.AI_CHAT_PHOTO) {
                pendingChatPhotoUri = uri
                pendingChatBitmap = bitmap
                pendingChatPhotoSource = srcType
            } else {
                ocrSourceType = srcType
                showOcrModal = true
                viewModel.translateImageOcr(bitmap)
            }
        }
    }

    // Photo Picker (Zero-permission per Play Policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            handleBitmapPicked(uri, InfoSourceType.PHOTO, imagePurpose)
        }
    }

    // Camera Capture via FileProvider
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            handleBitmapPicked(uri, InfoSourceType.CAMERA, imagePurpose)
        }
    }

    val launchCameraFlow: (ImageTargetPurpose) -> Unit = { purpose ->
        imagePurpose = purpose
        val uri = createTempCaptureUri(context)
        pendingCameraUri = uri
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraFlow(imagePurpose)
        } else {
            viewModel.showMessage("Camera permission denied. You can still pick photos from your gallery.")
        }
    }

    val requestCameraWithPermission: (ImageTargetPurpose) -> Unit = { purpose ->
        imagePurpose = purpose
        val hasCameraPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPerm) {
            launchCameraFlow(purpose)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Document / PDF Picker via SAF
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val docData = withContext(Dispatchers.IO) { readDocumentBytes(context, uri) }
                if (docData == null) {
                    viewModel.showMessage("Unable to read selected document.")
                } else {
                    val (bytes, mimeType, fileName) = docData
                    ocrSourceType = InfoSourceType.FILE
                    showOcrModal = true
                    viewModel.translateDocumentBytes(bytes, mimeType, fileName)
                }
            }
        }
    }

    val currentSavedList = (savedTranslationsState as? AsyncState.Success)?.data ?: emptyList()
    val currentCollectedList = (collectedItemsState as? AsyncState.Success)?.data ?: emptyList()
    val currentTranslatedText = (translationState as? AsyncState.Success)?.data?.translatedText.orEmpty()
    val isCurrentBookmarked = currentTranslatedText.isNotBlank() &&
        currentSavedList.any { it.translatedText == currentTranslatedText && it.targetCode == targetLang.code }

    Scaffold(
        containerColor = SurfacePitch,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceCard,
                    contentColor = PureWhite
                )
            }
        },
        topBar = {
            BolIndiaTopBar(
                offlineModeActive = engineSettings.offlineFirstMode,
                onOpenSettings = { showSettingsSheet = true },
                onOpenProfile = { showProfileSheet = true }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfacePitch)
        ) {
            val useNavigationRail = maxWidth >= 600.dp

            if (useNavigationRail) {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = SurfacePitch,
                        contentColor = PureWhite,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        NavigationRailItem(
                            selected = selectedTab == AppTab.TRANSLATE,
                            onClick = { viewModel.selectTab(AppTab.TRANSLATE) },
                            icon = { Icon(Icons.Filled.Translate, contentDescription = stringResource(R.string.nav_translate)) },
                            label = { Text(stringResource(R.string.nav_translate)) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PureBlack,
                                selectedTextColor = PureWhite,
                                indicatorColor = PureWhite,
                                unselectedIconColor = TextSlate,
                                unselectedTextColor = TextSlate
                            ),
                            modifier = Modifier.testTag("nav_translate")
                        )
                        NavigationRailItem(
                            selected = selectedTab == AppTab.AI_COLLECT,
                            onClick = { viewModel.selectTab(AppTab.AI_COLLECT) },
                            icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = stringResource(R.string.nav_ai_collect)) },
                            label = { Text(stringResource(R.string.nav_ai_collect)) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PureBlack,
                                selectedTextColor = PureWhite,
                                indicatorColor = PureWhite,
                                unselectedIconColor = TextSlate,
                                unselectedTextColor = TextSlate
                            ),
                            modifier = Modifier.testTag("nav_ai_collect")
                        )
                        NavigationRailItem(
                            selected = selectedTab == AppTab.VAULT,
                            onClick = { viewModel.selectTab(AppTab.VAULT) },
                            icon = { Icon(Icons.Filled.FolderSpecial, contentDescription = stringResource(R.string.nav_vault)) },
                            label = { Text(stringResource(R.string.nav_vault)) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = PureBlack,
                                selectedTextColor = PureWhite,
                                indicatorColor = PureWhite,
                                unselectedIconColor = TextSlate,
                                unselectedTextColor = TextSlate
                            ),
                            modifier = Modifier.testTag("nav_vault")
                        )
                    }
                    VerticalDivider(color = BorderSubtle)
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        ActiveTabContent(
                            selectedTab = selectedTab,
                            viewModel = viewModel,
                            sourceLang = sourceLang,
                            targetLang = targetLang,
                            sourceText = sourceText,
                            quickSearchQuery = quickSearchQuery,
                            translationState = translationState,
                            engineSettings = engineSettings,
                            isCurrentBookmarked = isCurrentBookmarked,
                            chatMessagesState = chatMessagesState,
                            isChatSending = isChatSending,
                            chatError = chatError,
                            collectedItemsState = collectedItemsState,
                            savedTranslationsState = savedTranslationsState,
                            pendingChatPhotoUri = pendingChatPhotoUri,
                            pendingChatBitmap = pendingChatBitmap,
                            pendingChatPhotoSource = pendingChatPhotoSource,
                            onClearPendingChatPhoto = {
                                pendingChatPhotoUri = null
                                pendingChatBitmap = null
                            },
                            onShowSourcePicker = { showSourcePicker = true },
                            onShowTargetPicker = { showTargetPicker = true },
                            onShowOfflineManager = { showOfflinePacks = true },
                            onShowDictionary = { showDictionaryModal = true },
                            onShowGrammarTone = {
                                viewModel.loadGrammarAndTone()
                                showGrammarToneModal = true
                            },
                            onShowWordBreakdown = {
                                viewModel.loadWordBreakdown()
                                showWordBreakdownModal = true
                            },
                            onShowFullscreen = { showFullscreen = true },
                            onShowOcrScanner = {
                                ocrSourceType = InfoSourceType.CAMERA
                                showOcrModal = true
                            },
                            onOpenDocumentPicker = {
                                openDocumentLauncher.launch(
                                    arrayOf("application/pdf", "text/plain", "text/*")
                                )
                            },
                            onPickChatPhoto = {
                                imagePurpose = ImageTargetPurpose.AI_CHAT_PHOTO
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onCaptureChatCamera = { requestCameraWithPermission(ImageTargetPurpose.AI_CHAT_PHOTO) },
                            onSpeakInput = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLang.bcp47Tag)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in ${sourceLang.name}...")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    speechLauncher.launch(intent)
                                } else {
                                    viewModel.showMessage("Voice dictation is not available on this device.")
                                }
                            },
                            onPasteInput = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipText = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                                if (!clipText.isNullOrBlank()) {
                                    viewModel.updateSourceText(clipText)
                                } else {
                                    viewModel.showMessage("Clipboard is empty.")
                                }
                            },
                            onCopyText = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("BolIndia", text))
                                viewModel.showMessage("Copied to clipboard.")
                            },
                            onShareText = { text ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            },
                            onListenSpeech = speakText
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        ActiveTabContent(
                            selectedTab = selectedTab,
                            viewModel = viewModel,
                            sourceLang = sourceLang,
                            targetLang = targetLang,
                            sourceText = sourceText,
                            quickSearchQuery = quickSearchQuery,
                            translationState = translationState,
                            engineSettings = engineSettings,
                            isCurrentBookmarked = isCurrentBookmarked,
                            chatMessagesState = chatMessagesState,
                            isChatSending = isChatSending,
                            chatError = chatError,
                            collectedItemsState = collectedItemsState,
                            savedTranslationsState = savedTranslationsState,
                            pendingChatPhotoUri = pendingChatPhotoUri,
                            pendingChatBitmap = pendingChatBitmap,
                            pendingChatPhotoSource = pendingChatPhotoSource,
                            onClearPendingChatPhoto = {
                                pendingChatPhotoUri = null
                                pendingChatBitmap = null
                            },
                            onShowSourcePicker = { showSourcePicker = true },
                            onShowTargetPicker = { showTargetPicker = true },
                            onShowOfflineManager = { showOfflinePacks = true },
                            onShowDictionary = { showDictionaryModal = true },
                            onShowGrammarTone = {
                                viewModel.loadGrammarAndTone()
                                showGrammarToneModal = true
                            },
                            onShowWordBreakdown = {
                                viewModel.loadWordBreakdown()
                                showWordBreakdownModal = true
                            },
                            onShowFullscreen = { showFullscreen = true },
                            onShowOcrScanner = {
                                ocrSourceType = InfoSourceType.CAMERA
                                showOcrModal = true
                            },
                            onOpenDocumentPicker = {
                                openDocumentLauncher.launch(
                                    arrayOf("application/pdf", "text/plain", "text/*")
                                )
                            },
                            onPickChatPhoto = {
                                imagePurpose = ImageTargetPurpose.AI_CHAT_PHOTO
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onCaptureChatCamera = { requestCameraWithPermission(ImageTargetPurpose.AI_CHAT_PHOTO) },
                            onSpeakInput = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLang.bcp47Tag)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in ${sourceLang.name}...")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    speechLauncher.launch(intent)
                                } else {
                                    viewModel.showMessage("Voice dictation is not available on this device.")
                                }
                            },
                            onPasteInput = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipText = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                                if (!clipText.isNullOrBlank()) {
                                    viewModel.updateSourceText(clipText)
                                } else {
                                    viewModel.showMessage("Clipboard is empty.")
                                }
                            },
                            onCopyText = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("BolIndia", text))
                                viewModel.showMessage("Copied to clipboard.")
                            },
                            onShareText = { text ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            },
                            onListenSpeech = speakText
                        )
                    }
                    HorizontalDivider(color = BorderSubtle)
                    NavigationBar(
                        containerColor = SurfacePitch,
                        contentColor = PureWhite,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == AppTab.TRANSLATE,
                            onClick = { viewModel.selectTab(AppTab.TRANSLATE) },
                            icon = { Icon(Icons.Filled.Translate, contentDescription = stringResource(R.string.nav_translate)) },
                            label = { Text(stringResource(R.string.nav_translate)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PureBlack,
                                selectedTextColor = PureWhite,
                                indicatorColor = PureWhite,
                                unselectedIconColor = TextSlate,
                                unselectedTextColor = TextSlate
                            ),
                            modifier = Modifier.testTag("nav_translate")
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppTab.AI_COLLECT,
                            onClick = { viewModel.selectTab(AppTab.AI_COLLECT) },
                            icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = stringResource(R.string.nav_ai_collect)) },
                            label = { Text(stringResource(R.string.nav_ai_collect)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PureBlack,
                                selectedTextColor = PureWhite,
                                indicatorColor = PureWhite,
                                unselectedIconColor = TextSlate,
                                unselectedTextColor = TextSlate
                            ),
                            modifier = Modifier.testTag("nav_ai_collect")
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppTab.VAULT,
                            onClick = { viewModel.selectTab(AppTab.VAULT) },
                            icon = { Icon(Icons.Filled.FolderSpecial, contentDescription = stringResource(R.string.nav_vault)) },
                            label = { Text(stringResource(R.string.nav_vault)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PureBlack,
                                selectedTextColor = PureWhite,
                                indicatorColor = PureWhite,
                                unselectedIconColor = TextSlate,
                                unselectedTextColor = TextSlate
                            ),
                            modifier = Modifier.testTag("nav_vault")
                        )
                    }
                }
            }
        }
    }

    // Modals & Bottom Sheets
    if (showFullscreen) {
        val currentSuccess = (translationState as? AsyncState.Success)?.data
        if (currentSuccess != null) {
            FullscreenTranslationModal(
                targetLang = targetLang,
                translatedText = currentSuccess.translatedText,
                phoneticGuide = currentSuccess.phoneticGuide,
                onListenSpeech = { speakText(currentSuccess.translatedText, targetLang) },
                onDismiss = { showFullscreen = false }
            )
        } else {
            showFullscreen = false
        }
    }

    if (showSourcePicker) {
        LanguagePickerSheet(
            title = "Select Source Language",
            selectedCode = sourceLang.code,
            installedCodes = engineSettings.installedPackCodes,
            onSelect = { viewModel.setSourceLanguage(it) },
            onDismiss = { showSourcePicker = false }
        )
    }

    if (showTargetPicker) {
        LanguagePickerSheet(
            title = "Select Target Language",
            selectedCode = targetLang.code,
            installedCodes = engineSettings.installedPackCodes,
            onSelect = { viewModel.setTargetLanguage(it, autoTranslate = true) },
            onDismiss = { showTargetPicker = false }
        )
    }

    if (showOfflinePacks) {
        OfflinePacksSheet(
            installedCodes = engineSettings.installedPackCodes,
            onTogglePack = { viewModel.toggleOfflinePack(it) },
            onDismiss = { showOfflinePacks = false }
        )
    }

    if (showDictionaryModal) {
        DictionaryModalSheet(
            query = quickSearchQuery,
            targetLang = targetLang,
            state = dictionaryState,
            onQueryChange = { viewModel.updateQuickSearchQuery(it) },
            onSearch = { viewModel.lookupWord() },
            onListenSpeech = { word -> speakText(word, targetLang) },
            onDismiss = { showDictionaryModal = false }
        )
    }

    if (showGrammarToneModal) {
        GrammarToneModalSheet(
            targetLang = targetLang,
            state = grammarToneState,
            onListenSpeech = { text -> speakText(text, targetLang) },
            onRetry = { viewModel.loadGrammarAndTone() },
            onDismiss = { showGrammarToneModal = false }
        )
    }

    if (showWordBreakdownModal) {
        WordBreakdownModalSheet(
            targetLang = targetLang,
            state = wordBreakdownState,
            onRetry = { viewModel.loadWordBreakdown() },
            onDismiss = { showWordBreakdownModal = false }
        )
    }

    if (showOcrModal) {
        OcrScannerModalSheet(
            targetLang = targetLang,
            state = ocrState,
            sourceType = ocrSourceType,
            onPickCamera = { requestCameraWithPermission(ImageTargetPurpose.OCR_SCAN) },
            onPickGallery = {
                imagePurpose = ImageTargetPurpose.OCR_SCAN
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickFile = {
                openDocumentLauncher.launch(
                    arrayOf("application/pdf", "text/plain", "text/*")
                )
            },
            onApplyToTranslator = { res -> viewModel.applyOcrToMainTranslator(res) },
            onSaveToVault = { res -> viewModel.saveOcrToVault(res, ocrSourceType) },
            onDismiss = { showOcrModal = false }
        )
    }

    if (showSettingsSheet) {
        EngineSettingsSheet(
            settings = engineSettings,
            onToggleFormal = { viewModel.updatePreferFormalTone(it) },
            onTogglePhonetics = { viewModel.updateAutoShowPhonetics(it) },
            onToggleOfflineFirst = { viewModel.updateOfflineFirstMode(it) },
            onToggleAutoCollect = { viewModel.updateAutoCollectFromChat(it) },
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showProfileSheet) {
        ProfileModalSheet(
            user = user,
            savedCount = currentSavedList.size,
            collectedCount = currentCollectedList.size,
            onSignOut = {
                signOut(credentialManager = credentialManager, scope = scope)
            },
            onDismiss = { showProfileSheet = false }
        )
    }
}

@Composable
private fun ActiveTabContent(
    selectedTab: AppTab,
    viewModel: BolIndiaViewModel,
    sourceLang: IndicLanguage,
    targetLang: IndicLanguage,
    sourceText: String,
    quickSearchQuery: String,
    translationState: AsyncState<com.example.model.TranslationResultData>,
    engineSettings: com.example.model.EngineSettings,
    isCurrentBookmarked: Boolean,
    chatMessagesState: AsyncState<List<com.example.model.ChatMessage>>,
    isChatSending: Boolean,
    chatError: String?,
    collectedItemsState: AsyncState<List<com.example.model.CollectedInfoItem>>,
    savedTranslationsState: AsyncState<List<com.example.model.SavedTranslation>>,
    pendingChatPhotoUri: Uri?,
    pendingChatBitmap: Bitmap?,
    pendingChatPhotoSource: InfoSourceType,
    onClearPendingChatPhoto: () -> Unit,
    onShowSourcePicker: () -> Unit,
    onShowTargetPicker: () -> Unit,
    onShowOfflineManager: () -> Unit,
    onShowDictionary: () -> Unit,
    onShowGrammarTone: () -> Unit,
    onShowWordBreakdown: () -> Unit,
    onShowFullscreen: () -> Unit,
    onShowOcrScanner: () -> Unit,
    onOpenDocumentPicker: () -> Unit,
    onPickChatPhoto: () -> Unit,
    onCaptureChatCamera: () -> Unit,
    onSpeakInput: () -> Unit,
    onPasteInput: () -> Unit,
    onCopyText: (String) -> Unit,
    onShareText: (String) -> Unit,
    onListenSpeech: (String, IndicLanguage) -> Unit
) {
    when (selectedTab) {
        AppTab.TRANSLATE -> {
            TranslatorScreen(
                sourceLang = sourceLang,
                targetLang = targetLang,
                sourceText = sourceText,
                quickSearchQuery = quickSearchQuery,
                translationState = translationState,
                engineSettings = engineSettings,
                isBookmarked = isCurrentBookmarked,
                onQuickSearchChange = { viewModel.updateQuickSearchQuery(it) },
                onLookupClick = {
                    viewModel.lookupWord()
                    onShowDictionary()
                },
                onOpenCameraScan = onShowOcrScanner,
                onOpenFileUpload = onOpenDocumentPicker,
                onOpenDictionary = onShowDictionary,
                onOpenSourcePicker = onShowSourcePicker,
                onOpenTargetPicker = onShowTargetPicker,
                onSwapLanguages = { viewModel.swapLanguages() },
                onQuickSwitchTarget = { lang -> viewModel.setTargetLanguage(lang, autoTranslate = true) },
                onOpenOfflineManager = onShowOfflineManager,
                onSourceTextChange = { viewModel.updateSourceText(it) },
                onClearSourceText = { viewModel.updateSourceText("") },
                onSpeakInput = onSpeakInput,
                onPasteInput = onPasteInput,
                onTranslateClick = { viewModel.translateCurrentText() },
                onListenSpeech = onListenSpeech,
                onCopyResult = onCopyText,
                onShareResult = onShareText,
                onSaveBookmark = { viewModel.saveCurrentTranslationBookmark() },
                onOpenFullscreen = onShowFullscreen,
                onOpenGrammarTone = onShowGrammarTone,
                onOpenWordBreakdown = onShowWordBreakdown
            )
        }

        AppTab.AI_COLLECT -> {
            AiChatbotScreen(
                chatMessagesState = chatMessagesState,
                isSending = isChatSending,
                chatError = chatError,
                autoCollectEnabled = engineSettings.autoCollectFromChat,
                pendingPhotoUri = pendingChatPhotoUri,
                pendingBitmap = pendingChatBitmap,
                pendingPhotoSource = pendingChatPhotoSource,
                onToggleAutoCollect = { viewModel.updateAutoCollectFromChat(it) },
                onPickPhoto = onPickChatPhoto,
                onCaptureCamera = onCaptureChatCamera,
                onClearPendingPhoto = onClearPendingChatPhoto,
                onSendMessage = { prompt ->
                    val bitmap = pendingChatBitmap
                    val src = pendingChatPhotoSource
                    onClearPendingChatPhoto()
                    viewModel.sendChatMessage(prompt, bitmap, src)
                },
                onCollectMessageInfo = { msg -> viewModel.collectInfoFromChatMessage(msg) },
                onClearChatHistory = { viewModel.clearChatHistory() }
            )
        }

        AppTab.VAULT -> {
            VaultScreen(
                collectedItemsState = collectedItemsState,
                savedTranslationsState = savedTranslationsState,
                onSaveManualItem = { title, cat, summary, facts ->
                    viewModel.saveManualCollectedItem(title, cat, summary, facts)
                },
                onUpdateCollectedItem = { id, title, cat, summary, facts ->
                    viewModel.updateCollectedItem(id, title, cat, summary, facts)
                },
                onDeleteCollectedItem = { id -> viewModel.deleteCollectedItem(id) },
                onDeleteSavedTranslation = { id -> viewModel.deleteSavedTranslation(id) },
                onCopyText = onCopyText,
                onListenSpeech = onListenSpeech,
                onLoadSavedIntoTranslator = { saved ->
                    viewModel.setSourceLanguage(IndicLanguages.all.firstOrNull { it.name == saved.sourceLang } ?: IndicLanguages.byCode("EN"))
                    viewModel.setTargetLanguage(IndicLanguages.byCode(saved.targetCode), autoTranslate = false)
                    viewModel.updateSourceText(saved.sourceText)
                    viewModel.selectTab(AppTab.TRANSLATE)
                }
            )
        }
    }
}

private fun createTempCaptureUri(context: Context): Uri {
    val capturesDir = File(context.cacheDir, "captures").apply { mkdirs() }
    val imageFile = File(capturesDir, "bolindia_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val resolver = context.contentResolver
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOpts)
        }
        val maxDim = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
        var sampleSize = 1
        while (maxDim / sampleSize > 1600) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        }
    } catch (_: Exception) {
        null
    }
}

private fun readDocumentBytes(context: Context, uri: Uri): Triple<ByteArray, String, String>? {
    return try {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/pdf"
        var fileName = "document"
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIdx) ?: "document"
            }
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        Triple(bytes, mimeType, fileName)
    } catch (_: Exception) {
        null
    }
}
