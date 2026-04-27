package com.example.silentpad.auth

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth

class SimpleAuthManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    val isLoading = mutableStateOf(false)
    val authError = mutableStateOf<String?>(null)
    val currentUser = mutableStateOf<String?>(null)
    val isLoggedIn = mutableStateOf(false)
    
    init {
        // Check if user is already logged in
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            currentUser.value = firebaseUser.email
            isLoggedIn.value = true
        }
    }
    
    fun createUserWithEmailPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        try {
            isLoading.value = true
            authError.value = null
            
            // Basic validation
            if (email.isBlank() || !email.contains("@")) {
                authError.value = "Please enter a valid email address"
                isLoading.value = false
                onComplete(false, "Please enter a valid email address")
                return
            }
            
            if (password.length < 6) {
                authError.value = "Password must be at least 6 characters"
                isLoading.value = false
                onComplete(false, "Password must be at least 6 characters")
                return
            }
            
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading.value = false
                    if (task.isSuccessful) {
                        currentUser.value = auth.currentUser?.email
                        isLoggedIn.value = true
                        onComplete(true, null)
                    } else {
                        val errorMessage = task.exception?.message ?: "Registration failed"
                        authError.value = errorMessage
                        onComplete(false, errorMessage)
                    }
                }
            
        } catch (e: Exception) {
            authError.value = "Registration failed: ${e.message}"
            isLoading.value = false
            onComplete(false, "Registration failed: ${e.message}")
        }
    }
    
    fun signInWithEmailPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        try {
            isLoading.value = true
            authError.value = null
            
            // Basic validation
            if (email.isBlank() || !email.contains("@")) {
                authError.value = "Please enter a valid email address"
                isLoading.value = false
                onComplete(false, "Please enter a valid email address")
                return
            }
            
            if (password.isBlank()) {
                authError.value = "Please enter your password"
                isLoading.value = false
                onComplete(false, "Please enter your password")
                return
            }
            
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading.value = false
                    if (task.isSuccessful) {
                        currentUser.value = auth.currentUser?.email
                        isLoggedIn.value = true
                        onComplete(true, null)
                    } else {
                        val errorMessage = task.exception?.message ?: "Login failed"
                        authError.value = errorMessage
                        onComplete(false, errorMessage)
                    }
                }
            
        } catch (e: Exception) {
            authError.value = "Login failed: ${e.message}"
            isLoading.value = false
            onComplete(false, "Login failed: ${e.message}")
        }
    }
    
    fun signOut() {
        auth.signOut()
        currentUser.value = null
        isLoggedIn.value = false
        authError.value = null
    }
    
    fun clearError() {
        authError.value = null
    }
}