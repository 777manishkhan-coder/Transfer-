package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class IndicLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val script: String,
    val bcp47Tag: String,
    val packSizeMb: Int,
    val isDefaultInstalled: Boolean = false
) {
    val displayTitle: String
        get() = if (nativeName.isNotBlank() && nativeName != name) "$name ($nativeName)" else name
}

object IndicLanguages {
    val all: List<IndicLanguage> = listOf(
        IndicLanguage("EN", "English", "English", "Latin", "en-IN", 28, isDefaultInstalled = true),
        IndicLanguage("HI", "Hindi", "हिन्दी", "Devanagari", "hi-IN", 34, isDefaultInstalled = true),
        IndicLanguage("TA", "Tamil", "தமிழ்", "Tamil Script", "ta-IN", 36, isDefaultInstalled = true),
        IndicLanguage("TE", "Telugu", "తెలుగు", "Telugu Script", "te-IN", 40, isDefaultInstalled = true),
        IndicLanguage("BN", "Bengali", "বাংলা", "Eastern Nagari", "bn-IN", 35, isDefaultInstalled = false),
        IndicLanguage("MR", "Marathi", "मराठी", "Devanagari", "mr-IN", 32, isDefaultInstalled = false),
        IndicLanguage("GU", "Gujarati", "ગુજરાતી", "Gujarati Script", "gu-IN", 31, isDefaultInstalled = false),
        IndicLanguage("KN", "Kannada", "ಕನ್ನಡ", "Kannada Script", "kn-IN", 34, isDefaultInstalled = false),
        IndicLanguage("ML", "Malayalam", "മലയാളം", "Malayalam Script", "ml-IN", 36, isDefaultInstalled = false),
        IndicLanguage("PA", "Punjabi", "ਪੰਜਾਬੀ", "Gurmukhi", "pa-IN", 30, isDefaultInstalled = false),
        IndicLanguage("OR", "Odia", "ଓଡ଼ିଆ", "Odia Script", "or-IN", 29, isDefaultInstalled = false),
        IndicLanguage("UR", "Urdu", "اردو", "Perso-Arabic", "ur-IN", 33, isDefaultInstalled = false),
        IndicLanguage("AS", "Assamese", "অসমীয়া", "Assamese Script", "as-IN", 28, isDefaultInstalled = false),
        IndicLanguage("SA", "Sanskrit", "संस्कृतम्", "Devanagari", "sa-IN", 27, isDefaultInstalled = false),
        IndicLanguage("NE", "Nepali", "नेपाली", "Devanagari", "ne-NP", 29, isDefaultInstalled = false),
        IndicLanguage("KOK", "Konkani", "कोंकणी", "Devanagari", "kok-IN", 26, isDefaultInstalled = false),
        IndicLanguage("MAI", "Maithili", "मैथिली", "Devanagari", "mai-IN", 25, isDefaultInstalled = false),
        IndicLanguage("SD", "Sindhi", "سنڌي", "Arabic / Devanagari", "sd-IN", 26, isDefaultInstalled = false),
        IndicLanguage("KS", "Kashmiri", "कॉशुर", "Perso-Arabic", "ks-IN", 26, isDefaultInstalled = false),
        IndicLanguage("MNI", "Manipuri", "মৈতৈলোন্", "Meitei Mayek", "mni-IN", 24, isDefaultInstalled = false),
        IndicLanguage("BRX", "Bodo", "बड़ो", "Devanagari", "brx-IN", 24, isDefaultInstalled = false),
        IndicLanguage("SAT", "Santali", "ᱥᱟᱱᱛᱟᱲᱤ", "Ol Chiki", "sat-IN", 23, isDefaultInstalled = false)
    )

    val quickSwitchTargets: List<IndicLanguage> = all.filter {
        it.code in setOf("HI", "TA", "TE", "BN", "MR", "GU", "KN", "ML", "PA")
    }

    fun byCode(code: String): IndicLanguage =
        all.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: all[1]
}

enum class InfoCategory(val label: String) {
    GENERAL("General"),
    SIGN_OCR("Sign & OCR"),
    DOCUMENT("Document"),
    TRAVEL("Travel"),
    SHOPPING("Shopping"),
    HEALTH("Health"),
    LANGUAGE("Language");

    companion object {
        fun fromRaw(raw: String?): InfoCategory =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: GENERAL
    }
}

enum class InfoSourceType(val label: String) {
    TEXT("Text"),
    PHOTO("Photo"),
    CAMERA("Camera"),
    FILE("Document");

    companion object {
        fun fromRaw(raw: String?): InfoSourceType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: TEXT
    }
}

enum class ChatMessageRole {
    USER,
    ASSISTANT;

    companion object {
        fun fromRaw(raw: String?): ChatMessageRole =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ASSISTANT
    }
}

@Keep
@IgnoreExtraProperties
data class SavedTranslation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sourceLang: String = "",
    val targetLang: String = "",
    val targetCode: String = "",
    val targetScript: String = "",
    val sourceText: String = "",
    val translatedText: String = "",
    val phoneticGuide: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CollectedInfoItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = InfoCategory.GENERAL.name,
    val summary: String = "",
    val extractedFacts: List<String> = emptyList(),
    val sourceType: String = InfoSourceType.TEXT.name,
    val sourcePrompt: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = ChatMessageRole.USER.name,
    val text: String = "",
    val hasPhoto: Boolean = false,
    val photoCaption: String? = null,
    val extractedTitle: String? = null,
    val extractedCategory: String? = null,
    val extractedSummary: String? = null,
    val extractedFacts: List<String>? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class TranslationResultData(
    val translatedText: String,
    val phoneticGuide: String,
    val detectedSourceLang: String = "",
    val literalNote: String = ""
)

data class GrammarToneAnalysis(
    val formalVersion: String,
    val formalPhonetic: String,
    val casualVersion: String,
    val casualPhonetic: String,
    val grammarNotes: List<String>
)

data class WordBreakdownItem(
    val token: String,
    val transliteration: String,
    val rootWord: String,
    val partOfSpeech: String,
    val meaning: String
)

data class DictionaryEntry(
    val word: String,
    val transliteration: String,
    val partOfSpeech: String,
    val meanings: List<String>,
    val examples: List<String>,
    val idioms: List<String>
)

data class OcrTranslationResult(
    val extractedSourceText: String,
    val translatedText: String,
    val phoneticGuide: String,
    val contextSummary: String
)

data class ExtractedInfoBlock(
    val title: String,
    val category: InfoCategory,
    val summary: String,
    val facts: List<String>
)

data class ChatbotTurnResult(
    val reply: String,
    val extractedInfo: ExtractedInfoBlock?
)

data class EngineSettings(
    val preferFormalTone: Boolean = true,
    val autoShowPhonetics: Boolean = true,
    val offlineFirstMode: Boolean = true,
    val autoCollectFromChat: Boolean = false,
    val installedPackCodes: Set<String> = setOf("EN", "HI", "TA", "TE")
)
