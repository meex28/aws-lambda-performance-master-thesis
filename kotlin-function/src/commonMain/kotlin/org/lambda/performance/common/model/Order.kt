package org.lambda.performance.common.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderSummary(
    val userId: String,
    val totalItems: Int,
    val totalPrice: Double,
    val items: List<CartItem>
)
