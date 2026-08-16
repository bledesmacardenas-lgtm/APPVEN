package com.example.data.model

import com.example.data.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val quantity: Double = 1.0,
    val customUnitPrice: Double = product.salePrice
) {
    val total: Double
        get() = customUnitPrice * quantity

    val totalCost: Double
        get() = product.costPrice * quantity

    val profit: Double
        get() = total - totalCost
}
