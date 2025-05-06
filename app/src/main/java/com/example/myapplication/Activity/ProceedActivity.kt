package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.example.myapplication.Model.CartItem
import com.example.myapplication.Model.OrderDetails
import com.example.myapplication.Model.PaymentOption
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityProceedBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class ProceedActivity : BaseActivity(), PaymentResultListener {

    private lateinit var binding: ActivityProceedBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProceedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Razorpay
        Checkout.preload(applicationContext)

        setupDropdown()
        fetchTotalPrice()

        // Place Order Button Click
        binding.placeOrderBtn.setOnClickListener {
            placeOrder()
        }

        // Back Button Click
        binding.backBtnPro.setOnClickListener {
            finish()
        }
    }

    private fun fetchTotalPrice() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUserId)
                .child("priceDetails")
                .child("totalPrice")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val totalPrice = dataSnapshot.getValue(Double::class.java)
                    if (totalPrice != null) {
                        binding.totalAmount.text = "₹$totalPrice"
                    } else {
                        Toast.makeText(this@ProceedActivity, "Total price not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@ProceedActivity, "Failed to load price: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDropdown() {
        val paymentOptions = listOf(
            PaymentOption("1", "UPI"),
            PaymentOption("2", "Cash on Delivery"),
            PaymentOption("3", "Card Payment"),
            PaymentOption("4", "Wallet")
        )

        val adapter = ArrayAdapter(this, R.layout.dropdown_item, paymentOptions.map { it.name })
        binding.autoCompleteTextView.setAdapter(adapter)
    }

    private fun placeOrder() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            val name = binding.name.text.toString()
            val address = binding.address.text.toString()
            val phone = binding.phone.text.toString()
            val totalAmount = binding.totalAmount.text.toString().trim().removePrefix("₹")
            val selectedPaymentMethod = binding.autoCompleteTextView.text.toString()

            if (name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty() && totalAmount.isNotEmpty() && selectedPaymentMethod.isNotEmpty()) {
                getCartItems(currentUserId) { orderedItems ->
                    if (orderedItems.isNotEmpty()) {
                        val order = OrderDetails(
                            name = name,
                            address = address,
                            phone = phone,
                            totalAmount = totalAmount.toDouble(),
                            paymentMethod = selectedPaymentMethod,
                            items = orderedItems,
                            currentTime = System.currentTimeMillis()
                        )

                        val orderRef = FirebaseDatabase.getInstance().getReference("orderDetails")
                            .child(currentUserId)
                            .push()

                        orderRef.setValue(order).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Order placed successfully", Toast.LENGTH_SHORT).show()
                                if (selectedPaymentMethod == "Cash on Delivery") {
                                    showCODConfirmationSheet()
                                } else {
                                    startRazorpayPayment(totalAmount.toDouble())
                                }
                            } else {
                                Toast.makeText(this, "Order placement failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "No items in the cart", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCODConfirmationSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.cod_ordeer_successfull, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<Button>(R.id.ok).setOnClickListener {
            bottomSheetDialog.dismiss()
            clearCartAndRedirect()
        }

        bottomSheetDialog.show()
    }

    private fun clearCartAndRedirect() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            FirebaseDatabase.getInstance().getReference("users")
                .child(currentUserId)
                .child("cartItems")
                .removeValue()
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        }
    }

    private fun getCartItems(userId: String, callback: (List<CartItem>) -> Unit) {
        FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("cartItems")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val cartItems = dataSnapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                    callback(cartItems)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    private fun startRazorpayPayment(amount: Double) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_U1DIfJyKCywMJS")  // Store securely in strings.xml for best practice

        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: "user@example.com"  // Fetch user email, default if null
        val phone = currentUser?.phoneNumber ?: "9999999999"  // Fetch user phone, default if null

        try {
            val paymentDetails = JSONObject().apply {
                put("name", "CafeD")  // App name
                put("description", "Order Payment")
                put("currency", "INR")
                put("amount", (amount * 100).toInt())  // Convert amount to paise
                put("prefill", JSONObject().apply {
                    put("email", email)
                    put("contact", phone)
                })
            }

            checkout.open(this, paymentDetails)

        } catch (e: Exception) {
            Toast.makeText(this, "Payment failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        if (razorpayPaymentID != null) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                // Fetch Cart Items Before Order Placement
                getCartItems(currentUserId) { orderedItems ->
                    if (orderedItems.isNotEmpty()) {
                        val orderId = FirebaseDatabase.getInstance().getReference("orderCompleted")
                            .child(currentUserId)
                            .push().key  // Unique Order ID

                        if (orderId != null) {
                            val paymentDetails = mapOf(
                                "orderId" to orderId,
                                "paymentId" to razorpayPaymentID,
                                "amount" to binding.totalAmount.text.toString().trim(),
                                "currentTime" to System.currentTimeMillis(),
                                "address" to binding.address.text.toString(),
                                "name" to binding.name.text.toString(),
                                "phone" to binding.phone.text.toString().trim(),
                                "status" to "Success",
                                "items" to orderedItems // Ordered Items
                            )

                            val orderReference = FirebaseDatabase.getInstance().getReference("orderCompleted")
                                .child(currentUserId)
                                .child(orderId)

                            orderReference.setValue(paymentDetails)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
                                    removeCartItems(currentUserId)  // Clear Cart After Order Completion
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to update order: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Failed to generate order ID", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Cart is Empty!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Redirect to Home
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        Toast.makeText(this, "Payment Successful. ID: $razorpayPaymentID", Toast.LENGTH_LONG).show()
    }

    private fun removeCartItems(userId: String) {
        FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("cartItems")
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Cart Cleared", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to clear cart: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onPaymentError(code: Int, response: String?) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            val paymentDetails = mapOf(
                "paymentId" to "Failure",
                "amount" to binding.totalAmount.text.toString().trim(),
                "status" to "Failed",
                "name" to binding.name.text.toString(),
                "phone" to binding.phone.text.toString().trim(),
                "errorCode" to code.toString(),
                "errorResponse" to response.orEmpty()
            )

            FirebaseDatabase.getInstance().getReference("paymentError")
                .child(currentUserId)
                .setValue(paymentDetails)
                .addOnSuccessListener {
                    Toast.makeText(this, "Payment Failure Updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to log error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        Toast.makeText(this, "Payment Failed. Code: $code, Response: $response", Toast.LENGTH_LONG).show()
    }

}
