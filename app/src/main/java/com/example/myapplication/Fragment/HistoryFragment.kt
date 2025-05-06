package com.example.myapplication.Fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Adapter.HistoryAdapter
import com.example.myapplication.Model.OrderHistory
import com.example.myapplication.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryFragment : Fragment() {

    private lateinit var historyAdapter: HistoryAdapter
    private val orderList = mutableListOf<OrderHistory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView from ViewBinding
        val binding = FragmentHistoryBinding.bind(view)
        binding.historyView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(orderList)
        binding.historyView.adapter = historyAdapter

        // Fetch order history
        fetchOrderHistory()
    }

    private fun fetchOrderHistory() {
        // Get the current user's UID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Log.e("Firebase", "No authenticated user found")
            return
        }

        // Reference the user's orders in the database
        val database = FirebaseDatabase.getInstance().getReference("orderCompleted").child(currentUserUid)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear() // Clear the order list before adding new data

                // Iterate through all orders
                for (orderSnapshot in snapshot.children) {
                    // Get order details from snapshot
                    val orderId = orderSnapshot.key ?: ""
                    val amount = orderSnapshot.child("amount").getValue(String::class.java) ?: "0.0"
                    val itemsSnapshot = orderSnapshot.child("items")

                    // Initialize a list to store the items for this order
                    val items = mutableListOf<OrderHistory.Item>()

                    // Iterate through each item in the order
                    for (itemSnapshot in itemsSnapshot.children) {
                        val title = itemSnapshot.child("title").getValue(String::class.java) ?: ""
                        val price = itemSnapshot.child("price").getValue(Double::class.java) ?: 0.0
                        val totalEachItem = itemSnapshot.child("totalEachItem").getValue(Double::class.java) ?: 0.0
                        val numberInCart = itemSnapshot.child("numberInCart").getValue(Int::class.java) ?: 1

                        // Fetch the picUrl as ArrayList<String>
                        val picUrl = itemSnapshot.child("picUrl").getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: ArrayList()

                        // Add item to the list
                        items.add(
                            OrderHistory.Item(
                                title = title,
                                price = price,
                                picUrl = picUrl,
                                totalEachItem = totalEachItem,
                                numberInCart = numberInCart
                            )
                        )
                    }

                    // Add the order with its items to the order list, including totalAmount
                    orderList.add(
                        OrderHistory(
                            orderId = orderId,
                            totalAmount = amount,  // Add the amount to the order
                            items = items // Add items to the order
                        )
                    )
                }

                // Notify the adapter that the data has been updated
                historyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("Firebase", "Failed to fetch order history: ${error.message}")
            }
        })
    }

}
