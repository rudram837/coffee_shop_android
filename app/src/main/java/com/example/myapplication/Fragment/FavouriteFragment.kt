package com.example.myapplication.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.FavouriteAdapter
import com.example.myapplication.Model.FavoriteItem
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavouriteFragment : Fragment() {

    private lateinit var favRecyclerView: RecyclerView
    private lateinit var favouriteAdapter: FavouriteAdapter
    private lateinit var favouriteList: MutableList<FavoriteItem>
    private lateinit var favRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_favourite, container, false)

        favRecyclerView = rootView.findViewById(R.id.favView)
        favRecyclerView.layoutManager = LinearLayoutManager(context)

        favouriteList = mutableListOf()
        favouriteAdapter = FavouriteAdapter(favouriteList)
        favRecyclerView.adapter = favouriteAdapter

        favRef = FirebaseDatabase.getInstance().getReference("favorites")

        fetchFavorites()

        return rootView
    }

    private fun fetchFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val favRef = FirebaseDatabase.getInstance().getReference("users/$userId/favouriteItems")

            favRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favouriteList.clear()  // Clear previous list

                    // Iterate through the children of the snapshot (favourite items)
                    for (favSnapshot in snapshot.children) {
                        val favoriteItem = favSnapshot.getValue(FavoriteItem::class.java)
                        if (favoriteItem != null) {
                            favouriteList.add(favoriteItem)  // Add the favorite item to the list
                        }
                    }

                    // Notify the adapter that the data has been updated
                    favouriteAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error, for example, by logging it
                    Log.e("Firebase", "Error fetching favorites: ${error.message}")
                }
            })
        } else {
            // Handle case when userId is null (not logged in)
            Log.e("Firebase", "User not logged in")
        }
    }
}
