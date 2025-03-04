package org.lambda.performance.common.service

import io.konform.validation.Validation
import io.konform.validation.constraints.exclusiveMinimum
import io.konform.validation.constraints.minItems
import io.konform.validation.constraints.notBlank
import io.konform.validation.constraints.pattern
import org.lambda.performance.common.model.Cart
import org.lambda.performance.common.model.CartItem
import org.lambda.performance.common.model.OrderSummary
import org.lambda.performance.common.repository.ProductRepository
import org.lambda.performance.common.repository.UserRepository

val cartValidation = Validation {
    Cart::userId {
        notBlank() hint "User ID must not be blank"
        pattern(Regex("^USER_\\d{6}\$")) hint "User ID must be in format USER_######"
    }

    Cart::items {
        minItems(1) hint "Cart must have at least one item"
    }

    Cart::items onEach {
        CartItem::quantity {
            exclusiveMinimum(0) hint "Quantity must be greater than 0"
        }

        CartItem::productId {
            notBlank() hint "Product ID must not be blank"
        }
    }
}

class OrderService(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {
    fun processCart(cart: Cart): Result<OrderSummary> {
        val user = userRepository.findById(cart.userId)
            ?: return Result.failure(IllegalArgumentException("User not found"))

        val validationResult = cartValidation.validate(cart)
        if (validationResult.errors.isNotEmpty()) {
            return Result.failure(IllegalArgumentException(validationResult.errors.toString()))
        }

        val orderItems = cart.items.map { cartItem ->
            val product = productRepository.findById(cartItem.productId)
                ?: return Result.failure(IllegalArgumentException("Product ${cartItem.productId} not found"))

            product to cartItem.quantity
        }

        val orderSummary = OrderSummary(
            userId = cart.userId,
            totalItems = orderItems.sumOf { it.second },
            totalPrice = orderItems.sumOf { it.first.price * it.second },
            items = cart.items
        )

        return Result.success(orderSummary)
    }
}
