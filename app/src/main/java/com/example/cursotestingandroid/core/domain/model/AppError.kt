package com.example.cursotestingandroid.core.domain.model

sealed class AppError(
    override val message: String? = null,
) : Exception(message) {
    data object NetworkError : AppError("Network Error") {
        private fun readResolve(): Any = NetworkError
    }

    data object NotFoundError : AppError("Not Found Error") {
        private fun readResolve(): Any = NotFoundError
    }

    data object DatabaseError : AppError("Database Error") {
        private fun readResolve(): Any = DatabaseError
    }

    sealed class Validation(
        message: String?,
    ) : AppError(message) {
        data object QuantityMustBePositive : Validation("Quantity must be positive") {
            private fun readResolve(): Any = QuantityMustBePositive
        }

        data class InsufficientStock(
            val available: Int,
        ) : Validation("Insufficient stock: $available available")
    }

    data class UnknownError(
        override val message: String?,
    ) : AppError(message)
}
