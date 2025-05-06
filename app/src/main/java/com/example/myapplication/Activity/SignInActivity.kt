package com.example.myapplication.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.Model.UserModel
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Google Sign-In
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignInBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Google Sign-In configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Use your Web Client ID from Firebase Console
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google Sign-In button click listener
        binding.googlebutn.setOnClickListener {
            signInWithGoogle()
        }

        // Create account logic
        binding.createbutn.setOnClickListener {
            name = binding.namebtn.text.toString().trim()
            email = binding.mailbutn.text.toString().trim()
            password = binding.pwdbutn.text.toString().trim()

            if (name.isBlank() || email.isBlank()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }

            if (password.isBlank()) {
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to login screen
        binding.alreadyhavebtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                saveUserData()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Account Creation Failed", Toast.LENGTH_SHORT).show()
                Log.d("Account", "createAccount: Failure", task.exception)
            }
        }
    }

    // Save user data into database
    private fun saveUserData() {
        name = binding.namebtn.text.toString().trim()
        email = binding.mailbutn.text.toString().trim()
        password = binding.pwdbutn.text.toString().trim()
        val user = UserModel(name, email, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("users").child(userId).child("userInfo").setValue(user)
    }

    // Google Sign-In method
    private fun signInWithGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.w("SignInActivity", "Google sign in failed", e)
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Authenticate with Firebase using Google credentials
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveGoogleUserData(it)
                    }
                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                    Log.w("SignInActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }

    // Save Google Sign-In user data into database
    private fun saveGoogleUserData(user: FirebaseUser) {
        val userId = user.uid
        val name = user.displayName ?: "Unknown"
        val email = user.email ?: "Unknown"
        val photoUrl = user.photoUrl?.toString() ?: "No Photo"

        val userData = mapOf(
            "name" to name,
            "email" to email,
            "photoUrl" to photoUrl
        )

        // Save user data in Realtime Database
        database.child("users").child(userId).child("userInfo").setValue(userData)
            .addOnSuccessListener {
                Log.d("SignInActivity", "User data saved successfully.")
            }
            .addOnFailureListener { exception ->
                Log.e("SignInActivity", "Failed to save user data.", exception)
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
