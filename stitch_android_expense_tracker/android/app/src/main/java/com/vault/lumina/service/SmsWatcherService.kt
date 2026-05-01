package com.vault.lumina.service

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import com.vault.lumina.data.model.Transaction
import com.vault.lumina.data.model.TransactionCategory
import com.vault.lumina.data.model.TransactionStatus
import com.vault.lumina.data.model.TransactionType
import com.vault.lumina.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.regex.Pattern

class SmsWatcherService(
    private val context: Context,
    private val repository: TransactionRepository
) {
    private var observer: ContentObserver? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isWatching = false
    private var watchStartedAt = 0L

    private val amountPatterns = listOf(
        Pattern.compile("rs\\.?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("amount\\s*rs?\\.?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("inr\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("debited\\s*(?:by\\s*)?rs?\\.?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("credited\\s*(?:with\\s*)?rs?\\.?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
    )

    private val categoryKeywords = mapOf(
        TransactionCategory.FOOD to listOf("swiggy", "zomato", "restaurant", "cafe", "food"),
        TransactionCategory.TRANSPORT to listOf("uber", "ola", "metro", "fuel", "petrol"),
        TransactionCategory.SHOPPING to listOf("amazon", "flipkart", "myntra", "shopping", "store"),
        TransactionCategory.ENTERTAINMENT to listOf("netflix", "spotify", "movie", "cinema"),
        TransactionCategory.SUBSCRIPTION to listOf("subscription", "renewal", "membership"),
        TransactionCategory.HEALTH to listOf("hospital", "clinic", "pharmacy", "health"),
        TransactionCategory.TRAVEL to listOf("flight", "airlines", "hotel", "trip", "travel"),
        TransactionCategory.HOUSING to listOf("rent", "maintenance", "electricity", "water bill"),
        TransactionCategory.TECHNOLOGY to listOf("google", "apple", "microsoft", "software", "cloud")
    )

    fun startWatching() {
        if (isWatching) return

        watchStartedAt = System.currentTimeMillis()
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                processSms(uri)
            }
        }

        observer?.let {
            context.contentResolver.registerContentObserver(
                Telephony.Sms.Inbox.CONTENT_URI,
                true,
                it
            )
            isWatching = true
        }
    }

    fun stopWatching() {
        observer?.let {
            context.contentResolver.unregisterContentObserver(it)
        }
        observer = null
        isWatching = false
    }

    private fun processSms(uri: Uri?) {
        scope.launch {
            try {
                val queryUri = uri ?: Telephony.Sms.Inbox.CONTENT_URI
                val cursor: Cursor? = context.contentResolver.query(
                    queryUri,
                    arrayOf(
                        Telephony.TextBasedSmsColumns.ADDRESS,
                        Telephony.TextBasedSmsColumns.BODY,
                        Telephony.TextBasedSmsColumns.DATE
                    ),
                    null,
                    null,
                    "${Telephony.TextBasedSmsColumns.DATE} DESC"
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val address = it.getString(0).orEmpty()
                        val body = it.getString(1).orEmpty()
                        val date = it.getLong(2)

                        if (date >= watchStartedAt) {
                            parseAndSaveTransaction(address, body, date)
                        }
                    }
                }
            } catch (_: Exception) {
                // SMS parsing is best-effort only.
            }
        }
    }

    private suspend fun parseAndSaveTransaction(address: String, body: String, date: Long) {
        val normalizedBody = body.trim()
        if (normalizedBody.isEmpty()) return

        val amount = extractAmount(normalizedBody)
        if (amount <= 0) return

        val lowerBody = normalizedBody.lowercase()
        val isCredit = lowerBody.contains("credited") ||
            lowerBody.contains("credit") ||
            lowerBody.contains("received")
        val isDebit = lowerBody.contains("debited") ||
            lowerBody.contains("spent") ||
            lowerBody.contains("paid") ||
            lowerBody.contains("purchase")

        val transactionType = when {
            isCredit -> TransactionType.INCOME
            isDebit -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }

        val merchant = extractMerchant(normalizedBody).ifEmpty { address.take(30) }
        val category = when (transactionType) {
            TransactionType.INCOME -> TransactionCategory.INCOME
            TransactionType.EXPENSE -> detectCategory(normalizedBody, merchant)
        }
        val sourceHash = buildSourceHash(address, normalizedBody, date, amount)

        val transaction = Transaction(
            amount = amount,
            category = category,
            type = transactionType,
            note = "Imported from SMS",
            merchant = merchant,
            date = date,
            paymentMethod = "SMS auto-import",
            isVerified = true,
            status = TransactionStatus.VERIFIED,
            sourceHash = sourceHash
        )

        repository.addTransaction(transaction)
    }

    private fun extractAmount(body: String): Double {
        amountPatterns.forEach { pattern ->
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "") ?: return 0.0
                return amountStr.toDoubleOrNull() ?: 0.0
            }
        }
        return 0.0
    }

    private fun detectCategory(body: String, merchant: String): TransactionCategory {
        val normalizedText = "$merchant $body".lowercase()
        categoryKeywords.forEach { (category, keywords) ->
            if (keywords.any { keyword -> normalizedText.contains(keyword) }) {
                return category
            }
        }
        return TransactionCategory.OTHER
    }

    private fun extractMerchant(body: String): String {
        val patterns = listOf(
            Pattern.compile("to\\s+([A-Za-z0-9\\s.&-]+?)\\s+on\\s", Pattern.CASE_INSENSITIVE),
            Pattern.compile("at\\s+([A-Za-z0-9\\s.&-]+?)\\s+(?:on|txn|ref|avl|,)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("paid to\\s+([A-Za-z0-9\\s.&-]+?)(?:\\.|,|\\s{2,})", Pattern.CASE_INSENSITIVE)
        )

        patterns.forEach { pattern ->
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                return matcher.group(1)?.trim()?.take(30).orEmpty()
            }
        }
        return ""
    }

    private fun buildSourceHash(address: String, body: String, date: Long, amount: Double): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$address|$body|$date|$amount"
        return digest.digest(input.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
    }
}
