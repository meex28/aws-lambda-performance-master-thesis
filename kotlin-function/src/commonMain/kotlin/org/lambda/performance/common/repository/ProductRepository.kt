package org.lambda.performance.common.repository

import org.lambda.performance.common.model.Product

class ProductRepository {
    private val products = listOf(
        Product("PROD001", "Wireless Headphones", 99.99),
        Product("PROD002", "Smart Watch", 199.50),
        Product("PROD003", "Bluetooth Speaker", 79.99),
        Product("PROD004", "Laptop Sleeve", 24.99),
        Product("PROD005", "Power Bank", 49.99)
    )

    fun findById(productId: String): Product? {
        return products.find { it.id == productId }
    }

    fun existsById(productId: String): Boolean {
        return products.any { it.id == productId }
    }
}