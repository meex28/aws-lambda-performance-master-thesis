package org.lambda.performance.common

import org.lambda.performance.common.model.Cart
import org.lambda.performance.common.model.CartItem
import org.lambda.performance.common.repository.ProductRepository
import org.lambda.performance.common.repository.UserRepository
import org.lambda.performance.common.service.OrderService

fun handle() = "Hello World"

fun main() {
    println(OrderService(
        productRepository = ProductRepository(),
        userRepository = UserRepository()
    ).processCart(
        cart = Cart(
            userId = "USER_123456",
            items = listOf(
                CartItem(
                    productId = "PROD001",
                    quantity = 10
                )
            )
        )
    ))
}
