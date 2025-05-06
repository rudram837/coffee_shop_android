package com.example.myapplication.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.Model.FavoriteItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FavouriteAdapter(private val favouriteList: MutableList<FavoriteItem>) :
    RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_favourite, parent, false)
        return FavouriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val item = favouriteList[position]
        holder.nameTextView.text = item.title
        holder.priceTextView.text = "₹${item.price}"  // Bind the price

        // Use Glide to load the first image from picUrl ArrayList into the ImageView with CenterCrop
        if (item.picUrl.isNotEmpty()) {  // Check if picUrl list is not empty
            Glide.with(holder.itemView.context)
                .load(item.picUrl[0])  // Load the first URL from the picUrl list
                .apply(RequestOptions().centerCrop())  // Apply CenterCrop transformation
                .into(holder.imageView)  // Target ImageView
        }

        // Set click listener on the remove button
        holder.removeFavBtn.setOnClickListener {
            // Get the current user's UID
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                // Get reference to the user's favorite items in Firebase
                val favRef = FirebaseDatabase.getInstance().getReference("users/$userId/favouriteItems")

                // Remove the item from Firebase based on its title
                favRef.orderByChild("title").equalTo(item.title).get().addOnSuccessListener { snapshot ->
                    // Iterate through the snapshot to ensure the correct item is found
                    for (childSnapshot in snapshot.children) {
                        // Remove item from Firebase
                        childSnapshot.ref.removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Safely remove the item from the local list and notify the adapter
                                if (position >= 0 && position < favouriteList.size) {
                                    favouriteList.removeAt(position)
                                    notifyItemRemoved(position)
                                } else {
                                    // Handle index out of bounds situation
                                    Log.e("Adapter", "Invalid position: $position")
                                }
                            } else {
                                // Handle error if removal fails
                                Log.e("Firebase", "Failed to remove item from Firebase")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return favouriteList.size
    }

    class FavouriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.titleFav)
        val priceTextView: TextView = itemView.findViewById(R.id.priceFav)  // Add price TextView
        val imageView: ImageView = itemView.findViewById(R.id.picFav)
        val removeFavBtn: ImageView = itemView.findViewById(R.id.removeFavBtn)  // Add remove button reference
    }
}
