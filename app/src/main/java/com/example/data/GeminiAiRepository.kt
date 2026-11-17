package com.example.data

import android.graphics.Bitmap
import android.util.Log
import com.example.model.ChatbotTurnResult
import com.example.model.DictionaryEntry
import com.example.model.ExtractedInfoBlock
import com.example.model.GrammarToneAnalysis
import com.example.model.IndicLanguage
import com.example.model.InfoCategory
import com.example.model.OcrTranslationResult
import com.example.model.TranslationResultData
import com.example.model.WordBreakdownItem
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class GeminiAiRepository {

    private val translationSchema = Schema.obj(
        properties = mapOf(
            "translatedText" to Schema.string(description = "Natural, accurate translation in the target script"),
            "phoneticGuide" to Schema.string(description = "Clear Latin/English phonetic pronunciation guide"),
            "detectedSourceLang" to Schema.string(description = "Detected source language name"),
            "literalNote" to Schema.string(description = "Brief optional nuance or register note")
        ),
        optionalProperties = listOf("literalNote")
    )

    private val translationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationSchema
            }
        )
    }

    private val dictionarySchema = Schema.obj(
        properties = mapOf(
            "word" to Schema.string(description = "Looked-up word or phrase in target script"),
            "transliteration" to Schema.string(description = "Latin transliteration"),
            "partOfSpeech" to Schema.string(description = "Part of speech or phrase type"),
            "meanings" to Schema.array(items = Schema.string(), description = "Primary definitions in English and target language"),
            "examples" to Schema.array(items = Schema.string(), description = "2-3 practical example sentences with transliteration"),
            "idioms" to Schema.array(items = Schema.string(), description = "Related idioms or cultural expressions")
        )
    )

    private val dictionaryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = dictionarySchema
            }
        )
    }

    private val grammarToneSchema = Schema.obj(
        properties = mapOf(
            "formalVersion" to Schema.string(description = "Polite/formal version in target script"),
            "formalPhonetic" to Schema.string(description = "Phonetic guide for formal version"),
            "casualVersion" to Schema.string(description = "Natural everyday casual version in target script"),
            "casualPhonetic" to Schema.string(description = "Phonetic guide for casual version"),
            "grammarNotes" to Schema.array(items = Schema.string(), description = "3-4 concise grammar, honorific, or usage notes")
        )
    )

    private val grammarToneModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = grammarToneSchema
            }
        )
    }

    private val breakdownSchema = Schema.obj(
        properties = mapOf(
            "tokens" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "token" to Schema.string(description = "Word or phrase segment in target script"),
                        "transliteration" to Schema.string(description = "Latin phonetic pronunciation"),
                        "rootWord" to Schema.string(description = "Dictionary root / stem"),
                        "partOfSpeech" to Schema.string(description = "Grammatical role"),
                        "meaning" to Schema.string(description = "Direct English meaning in context")
                    )
                ),
                description = "Word-by-word breakdown of the translated sentence"
            )
        )
    )

    private val breakdownModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = breakdownSchema
            }
        )
    }

    private val ocrSchema = Schema.obj(
        properties = mapOf(
            "extractedSourceText" to Schema.string(description = "Original text transcribed from the image or document"),
            "translatedText" to Schema.string(description = "Translated text in target language script"),
            "phoneticGuide" to Schema.string(description = "Phonetic English guide for the translation"),
            "contextSummary" to Schema.string(description = "Helpful explanation of what the sign, label, or document is")
        )
    )

    private val ocrModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = ocrSchema
            }
        )
    }

    private val chatbotSchema = Schema.obj(
        properties = mapOf(
            "reply" to Schema.string(description = "Helpful, clear conversational response answering the user's request about the text or photo"),
            "shouldCollect" to Schema.boolean(description = "True whenever useful factual or reference information can be collected into a structured card"),
            "collectedTitle" to Schema.string(description = "Concise 3-7 word title for the collected information card"),
            "collectedCategory" to Schema.enumeration(
                values = InfoCategory.entries.map { it.name },
                description = "Best matching category for this collected information"
            ),
            "collectedSummary" to Schema.string(description = "Clean 1-3 sentence summary of the collected information"),
            "collectedFacts" to Schema.array(
                items = Schema.string(),
                description = "Up to 8 crisp bullet facts, key-value details, prices, dates, translations, or insights extracted from the text or photo"
            )
        ),
        optionalProperties = listOf("collectedTitle", "collectedCategory", "collectedSummary", "collectedFacts")
    )

    private val chatbotModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = chatbotSchema
            }
        )
    }

    suspend fun translateText(
        text: String,
        sourceLang: IndicLanguage,
        targetLang: IndicLanguage,
        preferFormalTone: Boolean
    ): TranslationResultData {
        val toneHint = if (preferFormalTone) "polite/formal standard" else "natural everyday conversational"
        val prompt = """
            You are BolIndia, an expert neural translator specializing in Indian (Indic) languages.
            Translate the following text from ${sourceLang.name} (${sourceLang.script}) to ${targetLang.name} (${targetLang.nativeName}, script: ${targetLang.script}).
            Register preference: $toneHint.
            Provide the translation in authentic ${targetLang.script}, a readable English/Latin phonetic pronunciation guide, the detected source language, and a short nuance note if helpful.

            Text:
            $text
        """.trimIndent()

        return runWithTimeout("translateText") {
            val response = translationModel.generateContent(prompt)
            val jsonString = response.text ?: throw IllegalStateException("Translation model returned empty response.")
            val json = JSONObject(jsonString)
            TranslationResultData(
                translatedText = json.getString("translatedText"),
                phoneticGuide = json.getString("phoneticGuide"),
                detectedSourceLang = json.optString("detectedSourceLang", sourceLang.name),
                literalNote = json.optString("literalNote", "")
            )
        }
    }

    suspend fun lookupDictionary(
        query: String,
        sourceLang: IndicLanguage,
        targetLang: IndicLanguage
    ): DictionaryEntry {
        val prompt = """
            Act as an authoritative Indic bilingual lexicon.
            Look up the word, phrase, or idiom "$query" between ${sourceLang.name} and ${targetLang.name} (${targetLang.nativeName}, ${targetLang.script}).
            Return its form in ${targetLang.script}, Latin transliteration, part of speech, primary meanings, 2-3 usage examples, and related idioms or expressions.
        """.trimIndent()

        return runWithTimeout("lookupDictionary") {
            val response = dictionaryModel.generateContent(prompt)
            val json = JSONObject(response.text ?: throw IllegalStateException("Dictionary model returned empty response."))
            DictionaryEntry(
                word = json.getString("word"),
                transliteration = json.getString("transliteration"),
                partOfSpeech = json.getString("partOfSpeech"),
                meanings = jsonArrayToList(json.optJSONArray("meanings")),
                examples = jsonArrayToList(json.optJSONArray("examples")),
                idioms = jsonArrayToList(json.optJSONArray("idioms"))
            )
        }
    }

    suspend fun analyzeGrammarAndTone(
        sourceText: String,
        translatedText: String,
        targetLang: IndicLanguage
    ): GrammarToneAnalysis {
        val prompt = """
            Analyze this translation into ${targetLang.name} (${targetLang.nativeName}, ${targetLang.script}).
            Source: "$sourceText"
            Current translation: "$translatedText"
            Provide both a respectful/formal version and a warm casual/everyday version in ${targetLang.script} with phonetic guides, plus 3-4 concise grammar & honorific notes.
        """.trimIndent()

        return runWithTimeout("analyzeGrammarAndTone") {
            val response = grammarToneModel.generateContent(prompt)
            val json = JSONObject(response.text ?: throw IllegalStateException("Grammar analysis returned empty response."))
            GrammarToneAnalysis(
                formalVersion = json.getString("formalVersion"),
                formalPhonetic = json.getString("formalPhonetic"),
                casualVersion = json.getString("casualVersion"),
                casualPhonetic = json.getString("casualPhonetic"),
                grammarNotes = jsonArrayToList(json.optJSONArray("grammarNotes"))
            )
        }
    }

    suspend fun analyzeWordBreakdown(
        translatedText: String,
        targetLang: IndicLanguage
    ): List<WordBreakdownItem> {
        val prompt = """
            Perform a word-by-word lexicon breakdown of this ${targetLang.name} (${targetLang.nativeName}) sentence:
            "$translatedText"
            For every meaningful word/token, give the script token, Latin transliteration, root/stem word, part of speech, and contextual English meaning.
        """.trimIndent()

        return runWithTimeout("analyzeWordBreakdown") {
            val response = breakdownModel.generateContent(prompt)
            val json = JSONObject(response.text ?: throw IllegalStateException("Breakdown returned empty response."))
            val tokensArray = json.getJSONArray("tokens")
            (0 until tokensArray.length()).map { idx ->
                val obj = tokensArray.getJSONObject(idx)
                WordBreakdownItem(
                    token = obj.getString("token"),
                    transliteration = obj.getString("transliteration"),
                    rootWord = obj.getString("rootWord"),
                    partOfSpeech = obj.getString("partOfSpeech"),
                    meaning = obj.getString("meaning")
                )
            }
        }
    }

    suspend fun translateImageOcr(
        bitmap: Bitmap,
        targetLang: IndicLanguage
    ): OcrTranslationResult {
        val scaled = scaleBitmapIfNeeded(bitmap)
        return runWithTimeout("translateImageOcr") {
            val response = ocrModel.generateContent(
                content {
                    image(scaled)
                    text(
                        "Extract all visible text from this image (signs, menus, labels, handwritten or printed Indic/English text), " +
                            "translate it into ${targetLang.name} (${targetLang.nativeName}, ${targetLang.script}), " +
                            "provide a clear phonetic English pronunciation guide, and summarize what the image shows."
                    )
                }
            )
            val json = JSONObject(response.text ?: throw IllegalStateException("OCR model returned empty response."))
            OcrTranslationResult(
                extractedSourceText = json.getString("extractedSourceText"),
                translatedText = json.getString("translatedText"),
                phoneticGuide = json.getString("phoneticGuide"),
                contextSummary = json.getString("contextSummary")
            )
        }
    }

    suspend fun translateDocumentBytes(
        bytes: ByteArray,
        mimeType: String,
        fileName: String,
        targetLang: IndicLanguage
    ): OcrTranslationResult {
        require(bytes.size <= 18 * 1024 * 1024) {
            "File exceeds 18 MB inline limit. Please select a smaller document."
        }
        return runWithTimeout("translateDocumentBytes") {
            val response = ocrModel.generateContent(
                content {
                    inlineData(bytes, mimeType)
                    text(
                        "Read and extract the key text from this document ($fileName). " +
                            "Translate it accurately into ${targetLang.name} (${targetLang.nativeName}, ${targetLang.script}), " +
                            "provide a helpful phonetic guide for the main translated summary, and explain the document's purpose."
                    )
                }
            )
            val json = JSONObject(response.text ?: throw IllegalStateException("Document translation returned empty response."))
            OcrTranslationResult(
                extractedSourceText = json.getString("extractedSourceText"),
                translatedText = json.getString("translatedText"),
                phoneticGuide = json.getString("phoneticGuide"),
                contextSummary = json.getString("contextSummary")
            )
        }
    }

    suspend fun sendChatbotTurn(
        userPrompt: String,
        attachedBitmap: Bitmap?,
        recentHistory: List<Pair<String, String>>,
        preferredLanguage: IndicLanguage
    ): ChatbotTurnResult {
        val historyContext = if (recentHistory.isEmpty()) {
            ""
        } else {
            recentHistory.takeLast(6).joinToString("\n") { (role, msg) -> "$role: $msg" }
        }

        val systemInstruction = """
            You are BolIndia AI Collector, an intelligent multimodal assistant that helps users collect, extract, translate, and organize any information from text or photos.
            - Always inspect any attached photo thoroughly: read signs, bills, receipts, menus, medicine labels, documents, landmarks, or objects, and extract accurate facts (names, dates, amounts, addresses, Indic translations, key takeaways).
            - Respond clearly and concisely in 'reply'. Include relevant ${preferredLanguage.name} (${preferredLanguage.nativeName}) translations or pronunciation tips when helpful.
            - Set 'shouldCollect' to true whenever there is structured information worth saving to the user's Vault, and populate 'collectedTitle', 'collectedCategory', 'collectedSummary', and 'collectedFacts' (up to 8 crisp items).
            Recent conversation:
            $historyContext
        """.trimIndent()

        val scaledBitmap = attachedBitmap?.let { scaleBitmapIfNeeded(it) }

        return runWithTimeout("sendChatbotTurn") {
            val requestContent: Content = content {
                if (scaledBitmap != null) {
                    image(scaledBitmap)
                }
                text("$systemInstruction\n\nUser request: ${userPrompt.ifBlank { "Analyze this photo and collect all key information from it." }}")
            }
            val response = chatbotModel.generateContent(requestContent)
            val json = JSONObject(response.text ?: throw IllegalStateException("AI chatbot returned empty response."))
            val reply = json.getString("reply")
            val shouldCollect = json.optBoolean("shouldCollect", true)
            val title = json.optString("collectedTitle", "").trim()
            val summary = json.optString("collectedSummary", "").trim()
            val category = InfoCategory.fromRaw(json.optString("collectedCategory", "GENERAL"))
            val facts = jsonArrayToList(json.optJSONArray("collectedFacts")).take(12)

            val extracted = if (shouldCollect && (title.isNotEmpty() || summary.isNotEmpty())) {
                ExtractedInfoBlock(
                    title = title.ifEmpty { "Collected Insight" },
                    category = category,
                    summary = summary.ifEmpty { reply.take(500) },
                    facts = facts
                )
            } else {
                null
            }
            ChatbotTurnResult(reply = reply, extractedInfo = extracted)
        }
    }

    private suspend fun <T> runWithTimeout(opName: String, block: suspend () -> T): T {
        return try {
            withTimeout(60_000L) {
                block()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI call failed ($opName)", e)
            throw e
        }
    }

    private fun jsonArrayToList(array: org.json.JSONArray?): List<String> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { idx ->
            array.optString(idx)?.takeIf { it.isNotBlank() }
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int = 1440): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap
        val ratio = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val newW = (width * ratio).toInt().coerceAtLeast(1)
        val newH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    private companion object {
        const val TAG = "GeminiAiRepo"
    }
}
