package com.example.myapplication.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myapplication.Activity.SignInActivity
import com.example.myapplication.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!  // To access views

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the binding object
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Fetch data from Firebase
        fetchUserData()

        // Fetch and bind photo URL to ImageView
        fetchPhotoUrl()

        // Sign-out button click listener
        binding.signoutlay.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            activity?.finish()
        }

        // Edit button click listener to toggle fields' enable/disable state
        var isEnable = false
        binding.edit.setOnClickListener {
            isEnable = !isEnable
            binding.name.isEnabled = isEnable
            binding.mail.isEnabled = isEnable

            // Show focus on the name field if it's enabled
            if (isEnable) {
                binding.name.requestFocus()
            }

            binding.save.isEnabled = isEnable

            // Add Toast message to show whether fields are enabled or disabled
            if (isEnable) {
                Toast.makeText(requireContext(), "Now, you can edit your Information", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Edit disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Save button click listener to update user data
        binding.save.setOnClickListener {
            updateUserData()
            Toast.makeText(requireContext(), "User data updated", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val userInfoRef = database.child("users").child(userId).child("userInfo")

            userInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)

                        // Bind data to EditTexts using ViewBinding
                        binding.name.setText(name ?: "No Name")
                        binding.mail.setText(email ?: "No Email")
                    } else {
                        Log.d("ProfileFragment", "User info not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Database error: ${error.message}")
                }
            })
        } else {
            Log.e("ProfileFragment", "No logged-in user")
        }
    }

    private fun fetchPhotoUrl() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val photoUrlRef = database.child("users")
                .child(userId)
                .child("userInfo")
                .child("photoUrl")

            photoUrlRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val photoUrl = snapshot.getValue(String::class.java)
                    if (!photoUrl.isNullOrEmpty()) {
                        // Use Glide to load the photo into the ImageView
                        Glide.with(this@ProfileFragment)
                            .load(photoUrl)
                            .circleCrop()
                            .into(binding.picicon)
                    } else {
                        Log.d("ProfileFragment", "Photo URL not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Error fetching photo URL: ${error.message}")
                }
            })
        } else {
            Log.e("ProfileFragment", "No logged-in user")
        }
    }

    private fun updateUserData() {
        val name = binding.name.text.toString()
        val email = binding.mail.text.toString()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userInfoRef = database.child("users").child(userId).child("userInfo")

            val updates = mapOf(
                "name" to name,
                "email" to email
            )

            userInfoRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileFragment", "User data updated successfully")
                } else {
                    Log.e("ProfileFragment", "Error updating user data", task.exception)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
