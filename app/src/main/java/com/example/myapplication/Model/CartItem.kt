package com.example.myapplication.Model

data class CartItem(
    var title: String = "",  // Default value for title
    var price: Double = 0.0,  // Default value for price
    var picUrl: ArrayList<String> = arrayListOf(),
    var totalEachItem: Double = 0.0,  // Total price for each item
    var numberInCart: Int = 1  // Default number of items in cart (can be updated)
)
