package com.example.myapplication.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Activity.ProceedActivity
import com.example.myapplication.Adapter.CartAdapter
import com.example.myapplication.Helper.ChangeNumberItemsListener
import com.example.myapplication.Model.ItemsModel
import com.example.myapplication.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val cartItems = ArrayList<ItemsModel>()
    private lateinit var cartAdapter: CartAdapter
    private val validCoupons = mapOf(
        "DISCOUNT10" to 0.10, // 10% discount
        "SAVE20" to 0.20,     // 20% discount
        "WELCOME" to 0.15,     // 15% discount
    )
    private var discount: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding?.root ?: throw IllegalStateException("Binding cannot be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupCartList()
        loadCartItems()

        binding?.checkoutBtn?.setOnClickListener {
            navigateToProceedActivity()
        }

        binding?.applyCouponBtn?.setOnClickListener {
            applyCoupon()
        }
    }

    private fun navigateToProceedActivity() {
        if (isCartEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(requireContext(), ProceedActivity::class.java)
            startActivity(intent)
        }
    }

    // Example method to check if the cart is empty
    private fun isCartEmpty(): Boolean {
        // Directly check if cartItems is empty
        return cartItems.isEmpty()
    }



    private fun applyCoupon() {
        val couponCode = binding?.couponTxt?.text.toString().trim()
        if (validCoupons.containsKey(couponCode)) {
            discount = validCoupons[couponCode] ?: 0.0
            Toast.makeText(requireContext(), "Coupon applied successfully!", Toast.LENGTH_SHORT).show()
            calculateCart()
        } else {
            Toast.makeText(requireContext(), "Invalid coupon code.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCartItems() {
        val userId = auth.currentUser?.uid

        if (!userId.isNullOrEmpty()) {
            val cartRef = database.child("users").child(userId).child("cartItems")

            cartRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedItems = ArrayList<ItemsModel>()
                    for (cartSnapshot in snapshot.children) {
                        val item = cartSnapshot.getValue(ItemsModel::class.java)
                        item?.let { fetchedItems.add(it) }
                    }
                    updateCartItems(fetchedItems)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load cart items", Toast.LENGTH_SHORT).show()
                    Log.e("CartFragment", "Error: ${error.message}")
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCartItems(fetchedItems: ArrayList<ItemsModel>) {
        cartItems.clear()
        cartItems.addAll(fetchedItems)
        cartAdapter.notifyDataSetChanged()
        calculateCart()
    }

    private fun setupCartList() {
        binding?.let {
            it.cartVew.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            cartAdapter = CartAdapter(cartItems, requireContext(), object : ChangeNumberItemsListener {
                override fun onChanged() {
                    calculateCart()
                }
            })
            it.cartVew.adapter = cartAdapter
        }
    }

    private fun calculateCart() {
        val percentTax = 0.5
        val deliveryFee = 15.0
        val itemTotal = cartItems.sumOf { it.price * it.numberInCart }
        val tax = (itemTotal * percentTax).roundTo(2)
        val discountAmount = (itemTotal * discount).roundTo(2)
        val totalAfterDiscount = (itemTotal + tax + deliveryFee - discountAmount).roundTo(2)

        binding?.let {
            it.totalFeeTxt.text = " %.2f ₹".format(itemTotal)
            it.taxTxt.text = "%.2f ₹".format(tax)
            it.deliveryTxt.text = "%.2f ₹".format(deliveryFee)
            it.discountTxt.text = "%.2f ₹".format(discountAmount)
            it.totalTxt.text = "%.2f ₹".format(totalAfterDiscount)
        }

        // Get the current user ID
        val userId = auth.currentUser?.uid

        if (!userId.isNullOrEmpty()) {
            // Reference to the 'price' node in Firebase
            val priceRef = database.child("users").child(userId).child("priceDetails")

            // Creating a map to update the prices
            val priceUpdates = mapOf(
                "totalFee" to itemTotal,
                "tax" to tax,
                "deliveryFee" to deliveryFee,
                "discount" to discountAmount,
                "totalPrice" to totalAfterDiscount
            )

            // Update the price information in Firebase
            priceRef.setValue(priceUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("CartFragment", "Price information updated successfully in Firebase")
                } else {
                    Log.e("CartFragment", "Error updating price information in Firebase", task.exception)
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Double.roundTo(decimals: Int): Double {
        return "%.${decimals}f".format(this).toDouble()
    }
}
