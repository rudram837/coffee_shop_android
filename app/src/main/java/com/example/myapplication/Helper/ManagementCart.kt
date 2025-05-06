package com.example.myapplication.Helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.myapplication.Model.ItemsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManagementCart(private val context: Context) {

    private val tinyDB = TinyDB(context)

    fun insertItems(item: ItemsModel) {
        val listItem = getListCart()
        val index = listItem.indexOfFirst { it.title == item.title }

        if (index != -1) {
            // Item already exists; update its quantity
            listItem[index].numberInCart = item.numberInCart
        } else {
            // Item does not exist; add it
            listItem.add(item)
        }

        // Save the updated list to TinyDB
        tinyDB.putListObject("CartList", listItem)
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<ItemsModel> {
        // Retrieve the list from TinyDB or return an empty list if null
        return tinyDB.getListObject("CartList", ItemsModel::class.java) // Pass the class type
    }

    fun plusItem(list: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (list.isNotEmpty() && position >= 0 && position < list.size) {
            val item = list[position]
            item.numberInCart++
            listener.onChanged()
        } else {
            Log.e("ManagementCart", "Invalid position or empty list")
        }
    }

    fun minusItem(list: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (list.isNotEmpty() && position >= 0 && position < list.size) {
            val item = list[position]
            if (item.numberInCart > 0) {
                item.numberInCart--
                listener.onChanged()
            } else {
                Log.e("ManagementCart", "Cannot reduce items below 0")
            }
        } else {
            Log.e("ManagementCart", "Invalid position or empty list")
        }
    }

    fun getTotalFee(): Double {
        val listItem = getListCart()
        return listItem.sumOf { it.price * it.numberInCart }
    }

    fun removeItem(item: ItemsModel, listener: ChangeNumberItemsListener) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val cartRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
                .child("cartItems")

            cartRef.orderByChild("title").equalTo(item.title).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                listener.onChanged() // Notify adapter about the change
                            }
                            .addOnFailureListener {
                                Log.e("ManagementCart", "Failed to remove item: ${it.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ManagementCart", "Error removing item: ${error.message}")
                }
            })
        }
    }

}