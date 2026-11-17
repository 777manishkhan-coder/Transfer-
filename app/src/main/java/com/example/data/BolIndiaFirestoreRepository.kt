package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.ChatMessage
import com.example.model.ChatMessageRole
import com.example.model.CollectedInfoItem
import com.example.model.ExtractedInfoBlock
import com.example.model.InfoCategory
import com.example.model.InfoSourceType
import com.example.model.SavedTranslation
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BolIndiaFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun checkUserMatches(userId: String) {
        val currentUid = requireUserId()
        check(currentUid == userId) { "Signed-in user changed; refusing cross-user access." }
    }

    fun observeSavedTranslations(userId: String): Flow<List<SavedTranslation>> = flow {
        checkUserMatches(userId)
        val path = "users/$userId/saved_translations"
        emitAll(
            db.collection("users").document(userId).collection("saved_translations")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SavedTranslation::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCollectedItems(userId: String): Flow<List<CollectedInfoItem>> = flow {
        checkUserMatches(userId)
        val path = "users/$userId/collected_items"
        emitAll(
            db.collection("users").document(userId).collection("collected_items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CollectedInfoItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeChatMessages(userId: String): Flow<List<ChatMessage>> = flow {
        checkUserMatches(userId)
        val path = "users/$userId/chat_messages"
        emitAll(
            db.collection("users").document(userId).collection("chat_messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ChatMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveTranslation(
        sourceLang: String,
        targetLang: String,
        targetCode: String,
        targetScript: String,
        sourceText: String,
        translatedText: String,
        phoneticGuide: String
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/saved_translations"
        return try {
            val uid = requireUserId()
            val cleanSource = sourceText.trim().take(2000)
            val cleanTranslated = translatedText.trim().take(4000)
            require(cleanSource.isNotEmpty() && cleanTranslated.isNotEmpty()) {
                "Source and translated text cannot be empty."
            }
            val docRef = db.collection("users").document(uid)
                .collection("saved_translations").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "sourceLang" to sourceLang.trim().ifEmpty { "Auto" }.take(60),
                "targetLang" to targetLang.trim().ifEmpty { "Hindi" }.take(60),
                "targetCode" to targetCode.trim().ifEmpty { "HI" }.take(10),
                "targetScript" to targetScript.trim().ifEmpty { "Devanagari" }.take(60),
                "sourceText" to cleanSource,
                "translatedText" to cleanTranslated,
                "phoneticGuide" to phoneticGuide.trim().take(2000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteSavedTranslation(translationId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/saved_translations/$translationId"
        return try {
            val uid = requireUserId()
            require(translationId.isNotBlank()) { "Translation ID cannot be blank." }
            val docRef = db.collection("users").document(uid)
                .collection("saved_translations").document(translationId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveCollectedItem(
        title: String,
        category: InfoCategory,
        summary: String,
        facts: List<String>,
        sourceType: InfoSourceType,
        sourcePrompt: String
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/collected_items"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().ifEmpty { "Collected Note" }.take(160)
            val cleanSummary = summary.trim().take(4000)
            require(cleanSummary.isNotEmpty()) { "Summary cannot be empty." }
            val cleanFacts = facts.map { it.trim().take(300) }.filter { it.isNotEmpty() }.take(12)
            val docRef = db.collection("users").document(uid)
                .collection("collected_items").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "category" to category.name,
                "summary" to cleanSummary,
                "extractedFacts" to cleanFacts,
                "sourceType" to sourceType.name,
                "sourcePrompt" to sourcePrompt.trim().take(2000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateCollectedItem(
        itemId: String,
        title: String,
        category: InfoCategory,
        summary: String,
        facts: List<String>
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/collected_items/$itemId"
        return try {
            val uid = requireUserId()
            require(itemId.isNotBlank()) { "Collected item ID cannot be blank." }
            val cleanTitle = title.trim().take(160)
            val cleanSummary = summary.trim().take(4000)
            require(cleanTitle.isNotEmpty() && cleanSummary.isNotEmpty()) {
                "Title and summary cannot be empty."
            }
            val cleanFacts = facts.map { it.trim().take(300) }.filter { it.isNotEmpty() }.take(12)
            val docRef = db.collection("users").document(uid)
                .collection("collected_items").document(itemId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "title" to cleanTitle,
                    "category" to category.name,
                    "summary" to cleanSummary,
                    "extractedFacts" to cleanFacts,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCollectedItem(itemId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/collected_items/$itemId"
        return try {
            val uid = requireUserId()
            require(itemId.isNotBlank()) { "Collected item ID cannot be blank." }
            val docRef = db.collection("users").document(uid)
                .collection("collected_items").document(itemId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addChatMessage(
        role: ChatMessageRole,
        text: String,
        hasPhoto: Boolean,
        photoCaption: String? = null,
        extractedInfo: ExtractedInfoBlock? = null
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/chat_messages"
        return try {
            val uid = requireUserId()
            val cleanText = text.trim().take(8000)
            require(cleanText.isNotEmpty()) { "Chat message text cannot be empty." }
            val docRef = db.collection("users").document(uid)
                .collection("chat_messages").document()
            docPath = docRef.path

            val rawMap = mutableMapOf<String, Any?>(
                "userId" to uid,
                "role" to role.name,
                "text" to cleanText,
                "hasPhoto" to hasPhoto,
                "photoCaption" to photoCaption?.trim()?.take(300)?.ifEmpty { null },
                "extractedTitle" to extractedInfo?.title?.trim()?.take(160)?.ifEmpty { null },
                "extractedCategory" to extractedInfo?.category?.name,
                "extractedSummary" to extractedInfo?.summary?.trim()?.take(4000)?.ifEmpty { null },
                "extractedFacts" to extractedInfo?.facts
                    ?.map { it.trim().take(300) }
                    ?.filter { it.isNotEmpty() }
                    ?.take(12),
                "createdAt" to FieldValue.serverTimestamp()
            )
            @Suppress("UNCHECKED_CAST")
            val payload = rawMap.filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun clearChatHistory(messageIds: List<String>): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return try {
            val batch = db.batch()
            val colRef = db.collection("users").document(uid).collection("chat_messages")
            messageIds.forEach { id ->
                if (id.isNotBlank()) {
                    batch.delete(colRef.document(id))
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/chat_messages")
            Result.failure(e)
        }
    }
}
