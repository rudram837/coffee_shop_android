package com.example.myapplication.Model

data class OrderDetails(
    val name: String,
    val address: String,
    val phone: String,
    val totalAmount: Double,
    val paymentMethod: String, // Include the payment method
    val items: List<CartItem>, // This will store the ordered items
    val currentTime: Long // New property for the current time
)
