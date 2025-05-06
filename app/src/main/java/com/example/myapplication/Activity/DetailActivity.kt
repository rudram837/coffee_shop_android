package com.example.myapplication.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.Adapter.SizeAdapter
import com.example.myapplication.Helper.ManagementCart
import com.example.myapplication.Model.ItemsModel
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private lateinit var managementCart: ManagementCart
    private lateinit var auth: FirebaseAuth // Declare auth variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        managementCart = ManagementCart(this)
        bundle()
        initSizeList()
        setupAddToCartListener()
        setupAddToFavoritesListener() // Set up the favourite button listener
    }

    private fun setupAddToCartListener() {
        binding.addToCart.setOnClickListener {
            addItemToCart()
        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""

        // Check if the user is authenticated and numberInCart is valid (greater than or equal to 1)
        if (userId.isNotEmpty()) {
            if (item.numberInCart < 1) {
                Toast.makeText(this, "Invalid quantity! Please select at least one quantity.", Toast.LENGTH_SHORT).show()
                return
            }

            val cartRef = database.child("users").child(userId).child("cartItems")
            val cartItem = hashMapOf(
                "title" to item.title,
                "price" to item.price,
                "numberInCart" to item.numberInCart,
                "picUrl" to item.picUrl,
                "totalEachItem" to item.totalEachItem
            )

            cartRef.push().setValue(cartItem).addOnSuccessListener {
                Toast.makeText(this, "Item added to cart!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to add item to cart!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAddToFavoritesListener() {
        binding.favBtn.setOnClickListener {
            if (isItemFavourited()) {
                // If the item is already favourited, remove it from the favourites
                removeItemFromFavorites()
                binding.favBtn.setImageResource(R.drawable.fav_fill_black) // Change to empty heart
            } else {
                // If the item is not favourited, add it to the favourites
                addItemToFavorites()
                binding.favBtn.setImageResource(R.drawable.favourite_svg) // Change to filled heart
            }
        }
    }

    private fun removeItemFromFavorites() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            val favRef = database.child("users").child(userId).child("favouriteItems")

            // Fetch all items in favourites to check
            favRef.get().addOnSuccessListener { dataSnapshot ->
                var itemFound = false
                // Loop through all the items in the favouriteItems list
                for (snapshot in dataSnapshot.children) {
                    val favouriteItem = snapshot.getValue(ItemsModel::class.java)

                    // Log the item being checked
                    Log.d("RemoveItem", "Checking item: ${favouriteItem?.title} with ${item.title}")

                    // Compare by title or other unique identifiers
                    if (favouriteItem?.title == item.title) {
                        itemFound = true
                        // Log and remove the item from favourites
                        Log.d("RemoveItem", "Item found, removing: ${favouriteItem?.title}")
                        snapshot.ref.removeValue().addOnSuccessListener {
                            // Successfully removed item
                            Toast.makeText(this, "Item removed from favourites!", Toast.LENGTH_SHORT).show()
                            binding.favBtn.setImageResource(R.drawable.fav_fill_black) // Change icon to empty heart
                        }.addOnFailureListener {
                            // Error removing the item
                            Toast.makeText(this, "Failed to remove item from favourites!", Toast.LENGTH_SHORT).show()
                        }
                        break // Exit loop after removing the item
                    }
                }

                if (!itemFound) {
                    // If item was not found in favourites
                    Toast.makeText(this, "Item not found in favourites!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Failure when retrieving favourites
                Toast.makeText(this, "Failed to check favourites!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // User not authenticated
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isItemFavourited(): Boolean {
        // Check if the item exists in the favourites in Firebase
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""
        var isFavourited = false

        if (userId.isNotEmpty()) {
            val favRef = database.child("users").child(userId).child("favouriteItems")
            favRef.get().addOnSuccessListener { dataSnapshot ->
                for (snapshot in dataSnapshot.children) {
                    val favouriteItem = snapshot.getValue(ItemsModel::class.java)
                    if (favouriteItem?.title == item.title) {
                        isFavourited = true
                        break
                    }
                }
            }
        }

        return isFavourited
    }

    private fun addItemToFavorites() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            val favRef = database.child("users").child(userId).child("favouriteItems")
            val favItem = hashMapOf(
                "title" to item.title,
                "price" to item.price,
                "picUrl" to item.picUrl,
                "description" to item.description
            )
            favRef.push().setValue(favItem).addOnSuccessListener {
                Toast.makeText(this, "Item added to favourites!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to add item to favourites!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bundle() {
        binding.apply {
            item = intent.getParcelableExtra("object")!!
            titleTxtdet.text = item.title
            descriptionTxtdet.text = item.description
            priceTxtdet.text = "₹" + item.price
            ratingBar2.rating = item.rating.toFloat()

            addToCart.setOnClickListener {
                item.numberInCart = numberItemTxt.text.toString().toInt()
                managementCart.insertItems(item)
            }

            bacBktn.setOnClickListener {
                startActivity(Intent(this@DetailActivity, MainActivity::class.java))
                finish()
            }

            plusCaart.setOnClickListener {
                numberItemTxt.text = (item.numberInCart + 1).toString()
                item.numberInCart++
            }

            minusCart.setOnClickListener {
                if (item.numberInCart > 0) {
                    numberItemTxt.text = (item.numberInCart - 1).toString()
                    item.numberInCart--
                } else {
                    Toast.makeText(
                        this@DetailActivity,
                        "Cannot decrease item count below zero!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun initSizeList() {
        val sizeList = arrayListOf("1", "2", "3", "4")

        binding.sizeList.adapter = SizeAdapter(sizeList)
        binding.sizeList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        Glide.with(this)
            .load(item.picUrl[0])
            .apply(RequestOptions.bitmapTransform(RoundedCorners(100)))
            .into(binding.picMain)
    }
}
