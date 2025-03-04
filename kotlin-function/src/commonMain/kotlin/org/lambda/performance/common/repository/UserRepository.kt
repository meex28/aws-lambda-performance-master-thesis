package org.lambda.performance.common.repository

import org.lambda.performance.common.model.User

class UserRepository {
    private val users = listOf(
        User("USER_123456", "John Doe", "john.doe@example.com"),
        User("USER_234567", "Jane Smith", "jane.smith@example.com"),
        User("USER_345678", "Alice Johnson", "alice.johnson@example.com")
    )

    fun findById(userId: String): User? {
        return users.find { it.id == userId }
    }

    fun existsById(userId: String): Boolean {
        return users.any { it.id == userId }
    }
}
