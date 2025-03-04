package org.lambda.performance.common

import kotlinx.serialization.json.Json
import org.lambda.performance.common.model.Cart
import org.lambda.performance.common.repository.ProductRepository
import org.lambda.performance.common.repository.UserRepository
import org.lambda.performance.common.service.OrderService

val productRepository = ProductRepository()
val userRepository = UserRepository()
val productService = OrderService(
    productRepository = productRepository,
    userRepository = userRepository
)

fun handle(input: String): String {
    val cart = Json.decodeFromString<Cart>(input)

    val result = productService.processCart(cart)

    return Json.encodeToString(result.getOrThrow())
}
