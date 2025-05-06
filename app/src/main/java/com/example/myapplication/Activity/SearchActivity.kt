package com.example.myapplication.Activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Adapter.SearchAdapter
import com.example.myapplication.Model.CoffeeItem
import com.example.myapplication.databinding.ActivitySearchBinding
import com.google.firebase.database.*

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var databaseRefItems: DatabaseReference
    private lateinit var databaseRefBrewBased: DatabaseReference
    private lateinit var coffeeList: ArrayList<CoffeeItem>
    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFirebase()
        fetchCoffeeItems()
        setupSearch()
    }

    private fun setupRecyclerView() {
        coffeeList = ArrayList()
        adapter = SearchAdapter(coffeeList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFirebase() {
        databaseRefItems = FirebaseDatabase.getInstance().getReference("Items")
        databaseRefBrewBased = FirebaseDatabase.getInstance().getReference("BrewBased")
    }

    private fun fetchCoffeeItems() {
        coffeeList.clear()  // Clear existing data before fetching

        fetchDataFromFirebase(databaseRefItems)
        fetchDataFromFirebase(databaseRefBrewBased)
    }

    private fun fetchDataFromFirebase(databaseRef: DatabaseReference) {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    val title = itemSnapshot.child("title").getValue(String::class.java) ?: continue
                    val price = itemSnapshot.child("price").getValue(Double::class.java) ?: 0.0 // ✅ Fetching price as Double
                    val picUrls = itemSnapshot.child("picUrl").children.mapNotNull { it.getValue(String::class.java) }

                    coffeeList.add(CoffeeItem(title, price, ArrayList(picUrls))) // ✅ Pass price correctly
                }

                adapter.notifyDataSetChanged()
                showToast(if (coffeeList.isEmpty()) "No data found!" else "Loaded ${coffeeList.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error: ${error.message}")
            }
        })
    }

    private fun setupSearch() {
        binding.search1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterResults(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterResults(query: String) {
        val filteredList = coffeeList.filter { it.title.contains(query, ignoreCase = true) }
        adapter.updateList(ArrayList(filteredList))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
