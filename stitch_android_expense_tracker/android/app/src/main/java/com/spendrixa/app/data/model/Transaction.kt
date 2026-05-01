package com.spendrixa.app.data.model

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val type: TransactionType = TransactionType.EXPENSE,
    val note: String = "",
    val merchant: String = "",
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "",
    val isVerified: Boolean = false,
    val points: Int = 0,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val sourceHash: String = ""
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class TransactionCategory {
    FOOD,
    TRANSPORT,
    SHOPPING,
    GAMING,
    GYM,
    ENTERTAINMENT,
    TECHNOLOGY,
    LIFESTYLE,
    HEALTH,
    TRAVEL,
    SUBSCRIPTION,
    HOUSING,
    INCOME,
    OTHER
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    VERIFIED
}

data class UserAccount(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val monthlyBudget: Double = 0.0,
    val cardLastDigits: String = ""
)

data class TransactionGroup(
    val date: String = "",
    val transactions: List<Transaction> = emptyList()
)
