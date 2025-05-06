package com.example.myapplication.Model

data class OrderHistory(
    val orderId: String = "",
    val totalAmount: String = "", // Added totalAmount field to store the total amount of the order
    val items: List<Item> = listOf() // List of items in the order
) {
    // Nested class to represent individual items
    data class Item(
        val title: String = "",
        val price: Double = 0.0,
        val picUrl: ArrayList<String> = arrayListOf(),
        val totalEachItem: Double = 0.0,  // Total price for each item
        val numberInCart: Int = 1  // Default number of items in cart (can be updated)
    )
}
