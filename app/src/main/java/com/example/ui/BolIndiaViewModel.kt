package com.example.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BolIndiaFirestoreRepository
import com.example.data.GeminiAiRepository
import com.example.data.PreferencesRepository
import com.example.model.ChatMessage
import com.example.model.ChatMessageRole
import com.example.model.CollectedInfoItem
import com.example.model.DictionaryEntry
import com.example.model.EngineSettings
import com.example.model.ExtractedInfoBlock
import com.example.model.GrammarToneAnalysis
import com.example.model.IndicLanguage
import com.example.model.IndicLanguages
import com.example.model.InfoCategory
import com.example.model.InfoSourceType
import com.example.model.OcrTranslationResult
import com.example.model.SavedTranslation
import com.example.model.TranslationResultData
import com.example.model.WordBreakdownItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AsyncState<out T> {
    data object Idle : AsyncState<Nothing>
    data object Loading : AsyncState<Nothing>
    data class Success<T>(val data: T) : AsyncState<T>
    data class Error(val message: String) : AsyncState<Nothing>
}

enum class AppTab {
    TRANSLATE,
    AI_COLLECT,
    VAULT
}

class BolIndiaViewModel(
    private val firestoreRepo: BolIndiaFirestoreRepository,
    private val aiRepo: GeminiAiRepository,
    private val prefsRepo: PreferencesRepository,
    private val currentUserId: String
) : ViewModel() {

    val engineSettings: StateFlow<EngineSettings> = prefsRepo.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = EngineSettings()
        )

    val savedTranslationsState: StateFlow<AsyncState<List<SavedTranslation>>> =
        firestoreRepo.observeSavedTranslations(currentUserId)
            .map<List<SavedTranslation>, AsyncState<List<SavedTranslation>>> { AsyncState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing saved translations", rootError)
                emit(AsyncState.Error(rootError.localizedMessage ?: "Failed to load bookmarks"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = AsyncState.Loading
            )

    val collectedItemsState: StateFlow<AsyncState<List<CollectedInfoItem>>> =
        firestoreRepo.observeCollectedItems(currentUserId)
            .map<List<CollectedInfoItem>, AsyncState<List<CollectedInfoItem>>> { AsyncState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing collected items", rootError)
                emit(AsyncState.Error(rootError.localizedMessage ?: "Failed to load collected info"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = AsyncState.Loading
            )

    val chatMessagesState: StateFlow<AsyncState<List<ChatMessage>>> =
        firestoreRepo.observeChatMessages(currentUserId)
            .map<List<ChatMessage>, AsyncState<List<ChatMessage>>> { AsyncState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing chat messages", rootError)
                emit(AsyncState.Error(rootError.localizedMessage ?: "Failed to load chat history"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = AsyncState.Loading
            )

    private val _selectedTab = MutableStateFlow(AppTab.TRANSLATE)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _sourceLang = MutableStateFlow(IndicLanguages.byCode("EN"))
    val sourceLang: StateFlow<IndicLanguage> = _sourceLang.asStateFlow()

    private val _targetLang = MutableStateFlow(IndicLanguages.byCode("HI"))
    val targetLang: StateFlow<IndicLanguage> = _targetLang.asStateFlow()

    private val _sourceText = MutableStateFlow("Where can I find safe drinking water and food here?")
    val sourceText: StateFlow<String> = _sourceText.asStateFlow()

    private val _translationState = MutableStateFlow<AsyncState<TranslationResultData>>(
        AsyncState.Success(
            TranslationResultData(
                translatedText = "यहाँ मुझे पीने का साफ़ पानी और भोजन कहाँ मिलेगा?",
                phoneticGuide = "Yahan mujhe peene ka saaf paani aur bhojan kahan milega?",
                detectedSourceLang = "English",
                literalNote = "Polite & clear travel inquiry"
            )
        )
    )
    val translationState: StateFlow<AsyncState<TranslationResultData>> = _translationState.asStateFlow()

    private val _quickSearchQuery = MutableStateFlow("")
    val quickSearchQuery: StateFlow<String> = _quickSearchQuery.asStateFlow()

    private val _dictionaryState = MutableStateFlow<AsyncState<DictionaryEntry>>(AsyncState.Idle)
    val dictionaryState: StateFlow<AsyncState<DictionaryEntry>> = _dictionaryState.asStateFlow()

    private val _grammarToneState = MutableStateFlow<AsyncState<GrammarToneAnalysis>>(AsyncState.Idle)
    val grammarToneState: StateFlow<AsyncState<GrammarToneAnalysis>> = _grammarToneState.asStateFlow()

    private val _wordBreakdownState = MutableStateFlow<AsyncState<List<WordBreakdownItem>>>(AsyncState.Idle)
    val wordBreakdownState: StateFlow<AsyncState<List<WordBreakdownItem>>> = _wordBreakdownState.asStateFlow()

    private val _ocrState = MutableStateFlow<AsyncState<OcrTranslationResult>>(AsyncState.Idle)
    val ocrState: StateFlow<AsyncState<OcrTranslationResult>> = _ocrState.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun updateSourceText(text: String) {
        _sourceText.value = text
    }

    fun updateQuickSearchQuery(query: String) {
        _quickSearchQuery.value = query
    }

    fun setSourceLanguage(language: IndicLanguage) {
        _sourceLang.value = language
    }

    fun setTargetLanguage(language: IndicLanguage, autoTranslate: Boolean = true) {
        _targetLang.value = language
        if (autoTranslate && _sourceText.value.isNotBlank()) {
            translateCurrentText()
        }
    }

    fun swapLanguages() {
        val prevSource = _sourceLang.value
        val prevTarget = _targetLang.value
        _sourceLang.value = prevTarget
        _targetLang.value = prevSource
        val currentResult = (_translationState.value as? AsyncState.Success)?.data
        if (currentResult != null && currentResult.translatedText.isNotBlank()) {
            _sourceText.value = currentResult.translatedText
        }
        if (_sourceText.value.isNotBlank()) {
            translateCurrentText()
        }
    }

    fun translateCurrentText() {
        val textToTranslate = _sourceText.value.trim()
        if (textToTranslate.isEmpty()) {
            _snackbarMessage.value = "Enter or speak some text to translate."
            return
        }
        viewModelScope.launch {
            _translationState.value = AsyncState.Loading
            try {
                val result = aiRepo.translateText(
                    text = textToTranslate,
                    sourceLang = _sourceLang.value,
                    targetLang = _targetLang.value,
                    preferFormalTone = engineSettings.value.preferFormalTone
                )
                _translationState.value = AsyncState.Success(result)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Translation timed out", e)
                _translationState.value = AsyncState.Error("Translation timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Translation failed", e)
                _translationState.value = AsyncState.Error(
                    e.localizedMessage ?: "Unable to complete translation."
                )
            }
        }
    }

    fun lookupWord(queryOverride: String? = null) {
        val query = (queryOverride ?: _quickSearchQuery.value).trim()
        if (query.isEmpty()) {
            _snackbarMessage.value = "Enter a word, phrase, or idiom to look up."
            return
        }
        _quickSearchQuery.value = query
        viewModelScope.launch {
            _dictionaryState.value = AsyncState.Loading
            try {
                val entry = aiRepo.lookupDictionary(
                    query = query,
                    sourceLang = _sourceLang.value,
                    targetLang = _targetLang.value
                )
                _dictionaryState.value = AsyncState.Success(entry)
            } catch (e: TimeoutCancellationException) {
                _dictionaryState.value = AsyncState.Error("Dictionary lookup timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Dictionary lookup failed", e)
                _dictionaryState.value = AsyncState.Error(
                    e.localizedMessage ?: "Dictionary lookup failed."
                )
            }
        }
    }

    fun loadGrammarAndTone() {
        val currentResult = (_translationState.value as? AsyncState.Success)?.data
        val translated = currentResult?.translatedText.orEmpty()
        val src = _sourceText.value.trim()
        if (src.isEmpty() || translated.isEmpty()) {
            _snackbarMessage.value = "Translate a phrase first to analyze grammar & tone."
            return
        }
        viewModelScope.launch {
            _grammarToneState.value = AsyncState.Loading
            try {
                val analysis = aiRepo.analyzeGrammarAndTone(
                    sourceText = src,
                    translatedText = translated,
                    targetLang = _targetLang.value
                )
                _grammarToneState.value = AsyncState.Success(analysis)
            } catch (e: TimeoutCancellationException) {
                _grammarToneState.value = AsyncState.Error("Grammar analysis timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Grammar analysis failed", e)
                _grammarToneState.value = AsyncState.Error(
                    e.localizedMessage ?: "Grammar analysis failed."
                )
            }
        }
    }

    fun loadWordBreakdown() {
        val currentResult = (_translationState.value as? AsyncState.Success)?.data
        val translated = currentResult?.translatedText.orEmpty()
        if (translated.isEmpty()) {
            _snackbarMessage.value = "Translate a phrase first for word-by-word breakdown."
            return
        }
        viewModelScope.launch {
            _wordBreakdownState.value = AsyncState.Loading
            try {
                val tokens = aiRepo.analyzeWordBreakdown(
                    translatedText = translated,
                    targetLang = _targetLang.value
                )
                _wordBreakdownState.value = AsyncState.Success(tokens)
            } catch (e: TimeoutCancellationException) {
                _wordBreakdownState.value = AsyncState.Error("Word breakdown timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Word breakdown failed", e)
                _wordBreakdownState.value = AsyncState.Error(
                    e.localizedMessage ?: "Word breakdown failed."
                )
            }
        }
    }

    fun translateImageOcr(bitmap: Bitmap) {
        viewModelScope.launch {
            _ocrState.value = AsyncState.Loading
            try {
                val res = aiRepo.translateImageOcr(bitmap, _targetLang.value)
                _ocrState.value = AsyncState.Success(res)
            } catch (e: TimeoutCancellationException) {
                _ocrState.value = AsyncState.Error("Image scan timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "OCR scan failed", e)
                _ocrState.value = AsyncState.Error(e.localizedMessage ?: "Failed to scan image.")
            }
        }
    }

    fun translateDocumentBytes(bytes: ByteArray, mimeType: String, fileName: String) {
        viewModelScope.launch {
            _ocrState.value = AsyncState.Loading
            try {
                val res = aiRepo.translateDocumentBytes(
                    bytes = bytes,
                    mimeType = mimeType,
                    fileName = fileName,
                    targetLang = _targetLang.value
                )
                _ocrState.value = AsyncState.Success(res)
            } catch (e: TimeoutCancellationException) {
                _ocrState.value = AsyncState.Error("Document translation timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Document translation failed", e)
                _ocrState.value = AsyncState.Error(e.localizedMessage ?: "Failed to translate file.")
            }
        }
    }

    fun applyOcrToMainTranslator(result: OcrTranslationResult) {
        _sourceText.value = result.extractedSourceText
        _translationState.value = AsyncState.Success(
            TranslationResultData(
                translatedText = result.translatedText,
                phoneticGuide = result.phoneticGuide,
                detectedSourceLang = "Scanned Content",
                literalNote = result.contextSummary
            )
        )
    }

    fun saveOcrToVault(result: OcrTranslationResult, sourceType: InfoSourceType) {
        viewModelScope.launch {
            val saveRes = firestoreRepo.saveCollectedItem(
                title = result.contextSummary.take(80).ifBlank { "Scanned ${_targetLang.value.name} Text" },
                category = if (sourceType == InfoSourceType.FILE) InfoCategory.DOCUMENT else InfoCategory.SIGN_OCR,
                summary = "${result.translatedText}\n\nPhonetic: ${result.phoneticGuide}",
                facts = listOfNotNull(
                    result.extractedSourceText.takeIf { it.isNotBlank() }?.let { "Original: $it" },
                    result.translatedText.takeIf { it.isNotBlank() }?.let { "Translated (${_targetLang.value.code}): $it" }
                ),
                sourceType = sourceType,
                sourcePrompt = result.contextSummary
            )
            saveRes.onSuccess {
                _snackbarMessage.value = "Saved scan to Vault."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to save to Vault."
            }
        }
    }

    fun saveCurrentTranslationBookmark() {
        val currentResult = (_translationState.value as? AsyncState.Success)?.data ?: return
        val src = _sourceText.value.trim()
        if (src.isEmpty() || currentResult.translatedText.isBlank()) return

        viewModelScope.launch {
            val res = firestoreRepo.saveTranslation(
                sourceLang = _sourceLang.value.name,
                targetLang = _targetLang.value.displayTitle,
                targetCode = _targetLang.value.code,
                targetScript = _targetLang.value.script,
                sourceText = src,
                translatedText = currentResult.translatedText,
                phoneticGuide = currentResult.phoneticGuide
            )
            res.onSuccess {
                _snackbarMessage.value = "Saved translation to Vault."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to save bookmark."
            }
        }
    }

    fun deleteSavedTranslation(translationId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteSavedTranslation(translationId)
                .onSuccess { _snackbarMessage.value = "Bookmark removed." }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Delete failed." }
        }
    }

    fun sendChatMessage(
        promptText: String,
        attachedBitmap: Bitmap?,
        photoSourceType: InfoSourceType = InfoSourceType.PHOTO
    ) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty() && attachedBitmap == null) {
            _snackbarMessage.value = "Type a question or attach a photo to collect information."
            return
        }
        val displayUserText = cleanPrompt.ifEmpty { "Analyze this photo and extract all key information." }

        viewModelScope.launch {
            _isChatSending.value = true
            _chatError.value = null
            try {
                // Snapshot recent messages for multi-turn context
                val existingHistory = (chatMessagesState.value as? AsyncState.Success)?.data
                    ?.map { it.role to it.text }
                    ?: emptyList()

                // Persist user turn
                firestoreRepo.addChatMessage(
                    role = ChatMessageRole.USER,
                    text = displayUserText,
                    hasPhoto = attachedBitmap != null,
                    photoCaption = if (attachedBitmap != null) "${photoSourceType.label} attached" else null
                )

                val turnResult = aiRepo.sendChatbotTurn(
                    userPrompt = displayUserText,
                    attachedBitmap = attachedBitmap,
                    recentHistory = existingHistory,
                    preferredLanguage = _targetLang.value
                )

                // Persist assistant turn
                firestoreRepo.addChatMessage(
                    role = ChatMessageRole.ASSISTANT,
                    text = turnResult.reply,
                    hasPhoto = false,
                    extractedInfo = turnResult.extractedInfo
                )

                // Optionally auto-collect to Vault if enabled in Engine Settings
                if (engineSettings.value.autoCollectFromChat && turnResult.extractedInfo != null) {
                    val info = turnResult.extractedInfo
                    firestoreRepo.saveCollectedItem(
                        title = info.title,
                        category = info.category,
                        summary = info.summary,
                        facts = info.facts,
                        sourceType = if (attachedBitmap != null) photoSourceType else InfoSourceType.TEXT,
                        sourcePrompt = displayUserText
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chatbot turn timed out", e)
                _chatError.value = "AI response timed out after 60 seconds. Please try again."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chatbot turn failed", e)
                _chatError.value = e.localizedMessage ?: "AI chatbot request failed."
            } finally {
                _isChatSending.value = false
            }
        }
    }

    fun collectInfoFromChatMessage(message: ChatMessage) {
        val title = message.extractedTitle?.ifBlank { "Collected Note" } ?: "Collected Note"
        val summary = message.extractedSummary?.ifBlank { message.text } ?: message.text
        val category = InfoCategory.fromRaw(message.extractedCategory)
        val facts = message.extractedFacts.orEmpty()

        viewModelScope.launch {
            firestoreRepo.saveCollectedItem(
                title = title,
                category = category,
                summary = summary,
                facts = facts,
                sourceType = if (message.hasPhoto) InfoSourceType.PHOTO else InfoSourceType.TEXT,
                sourcePrompt = "Collected from AI Assistant"
            ).onSuccess {
                _snackbarMessage.value = "Collected \"$title\" into your Vault."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to save to Vault."
            }
        }
    }

    fun saveManualCollectedItem(
        title: String,
        category: InfoCategory,
        summary: String,
        factsText: String
    ) {
        val facts = factsText.lines().map { it.trim() }.filter { it.isNotEmpty() }.take(12)
        viewModelScope.launch {
            firestoreRepo.saveCollectedItem(
                title = title,
                category = category,
                summary = summary,
                facts = facts,
                sourceType = InfoSourceType.TEXT,
                sourcePrompt = "Manual Vault Entry"
            ).onSuccess {
                _snackbarMessage.value = "Saved note to Vault."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to save note."
            }
        }
    }

    fun updateCollectedItem(
        itemId: String,
        title: String,
        category: InfoCategory,
        summary: String,
        factsText: String
    ) {
        val facts = factsText.lines().map { it.trim() }.filter { it.isNotEmpty() }.take(12)
        viewModelScope.launch {
            firestoreRepo.updateCollectedItem(
                itemId = itemId,
                title = title,
                category = category,
                summary = summary,
                facts = facts
            ).onSuccess {
                _snackbarMessage.value = "Updated Vault item."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Update failed."
            }
        }
    }

    fun deleteCollectedItem(itemId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteCollectedItem(itemId)
                .onSuccess { _snackbarMessage.value = "Deleted from Vault." }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Delete failed." }
        }
    }

    fun clearChatHistory() {
        val msgs = (chatMessagesState.value as? AsyncState.Success)?.data ?: return
        if (msgs.isEmpty()) return
        viewModelScope.launch {
            firestoreRepo.clearChatHistory(msgs.map { it.id })
                .onSuccess { _snackbarMessage.value = "Chat history cleared." }
                .onFailure { _snackbarMessage.value = it.localizedMessage ?: "Failed to clear chat." }
        }
    }

    fun updatePreferFormalTone(value: Boolean) {
        viewModelScope.launch { prefsRepo.updatePreferFormalTone(value) }
    }

    fun updateAutoShowPhonetics(value: Boolean) {
        viewModelScope.launch { prefsRepo.updateAutoShowPhonetics(value) }
    }

    fun updateOfflineFirstMode(value: Boolean) {
        viewModelScope.launch { prefsRepo.updateOfflineFirstMode(value) }
    }

    fun updateAutoCollectFromChat(value: Boolean) {
        viewModelScope.launch { prefsRepo.updateAutoCollectFromChat(value) }
    }

    fun toggleOfflinePack(code: String) {
        viewModelScope.launch { prefsRepo.toggleOfflinePack(code) }
    }

    private companion object {
        const val TAG = "BolIndiaVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
