package org.lambda.performance.common.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val productId: String,
    val quantity: Int,
)

@Serializable
data class Cart(
    val userId: String,
    val items: List<CartItem>
)
