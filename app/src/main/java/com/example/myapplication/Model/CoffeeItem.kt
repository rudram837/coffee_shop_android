package com.example.myapplication.Model

data class CoffeeItem(
    val title: String = "",
    val price: Double = 0.0, // ✅ Ensure this is a Double
    val picUrl: ArrayList<String> = arrayListOf() // ✅ Corrected initialization as an ArrayList
)
