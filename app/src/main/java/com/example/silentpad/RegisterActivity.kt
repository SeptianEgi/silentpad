package com.example.silentpad

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silentpad.ui.theme.SilentPadTheme
import com.example.silentpad.ui.theme.SilentPadColors
import com.example.silentpad.ui.theme.SilentPadTitle
import com.example.silentpad.ui.theme.SilentPadButton
import com.example.silentpad.ui.theme.SilentPadInputField
import com.example.silentpad.ui.theme.BackButton
import com.example.silentpad.auth.SimpleAuthManager

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                SilentPadTheme {
                    RegisterScreen()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RegisterActivity", "Error in onCreate", e)
            finish()
        }
    }
}

@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val authManager = remember { SimpleAuthManager(context) }
    
    var nameText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Pure Black Background using theme
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SilentPadColors.background)
        )
        
        // Wolf Moon Image - centered
        Image(
            painter = painterResource(id = R.drawable.wolfmoon),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .offset(y = 80.dp),
            contentScale = ContentScale.Fit
        )
        
        // Back Button - Top Left Corner
        BackButton(
            onClick = {
                try {
                    val intent = Intent(context, WelcomeActivity::class.java)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("RegisterActivity", "Back navigation error", e)
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 24.dp, start = 8.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(400.dp))
            
            // Create Account Title using global component
            SilentPadTitle(
                text = "Create Account",
                fontSize = 25.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // "To get started now!" subtitle using theme colors
            Text(
                text = "To get started now!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = SilentPadColors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Name Field
            SilentPadInputField(
                value = nameText,
                onValueChange = { nameText = it },
                placeholder = "Full Name",
                width = 330.dp,
                height = 60.dp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Phone Number Field
            SilentPadInputField(
                value = phoneText,
                onValueChange = { phoneText = it },
                placeholder = "Phone Number",
                width = 330.dp,
                height = 60.dp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Email Address Field - Input Field
            SilentPadInputField(
                value = emailText,
                onValueChange = { emailText = it },
                placeholder = "Email Address",
                width = 330.dp,
                height = 60.dp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Password Field - Input Field  
            SilentPadInputField(
                value = passwordText,
                onValueChange = { passwordText = it },
                placeholder = "Password",
                width = 330.dp,
                height = 60.dp,
                isPassword = true,
                passwordVisible = isPasswordVisible,
                onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Confirm Password Field - Input Field
            SilentPadInputField(
                value = confirmPasswordText,
                onValueChange = { confirmPasswordText = it },
                placeholder = "Confirm Password",
                width = 330.dp,
                height = 60.dp,
                isPassword = true,
                passwordVisible = isConfirmPasswordVisible,
                onPasswordVisibilityToggle = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // REGISTER Button - Large like Login button
            SilentPadButton(
                text = if (authManager.isLoading.value) "Creating Account..." else "REGISTER",
                onClick = {
                    authManager.clearError()
                    
                    // Validate all fields
                    when {
                        nameText.isBlank() -> authManager.authError.value = "Please enter your name"
                        phoneText.isBlank() -> authManager.authError.value = "Please enter your phone number"
                        emailText.isBlank() -> authManager.authError.value = "Please enter your email"
                        passwordText.isBlank() -> authManager.authError.value = "Please enter a password"
                        confirmPasswordText.isBlank() -> authManager.authError.value = "Please confirm your password"
                        passwordText != confirmPasswordText -> authManager.authError.value = "Passwords do not match"
                        passwordText.length < 6 -> authManager.authError.value = "Password must be at least 6 characters"
                        else -> {
                            authManager.createUserWithEmailPassword(emailText, passwordText) { success, error ->
                                if (success) {
                                    try {
                                        // Save user data to SharedPreferences (only name and phone, Firebase handles auth)
                                        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                        prefs.edit().apply {
                                            putString("name", nameText)
                                            putString("phone", phoneText)
                                            apply()
                                        }
                                        
                                        val intent = Intent(context, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("RegisterActivity", "Navigation error", e)
                                        authManager.authError.value = "Navigation failed"
                                    }
                                }
                            }
                        }
                    }
                },
                width = 330.dp,
                height = 60.dp,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Show error message if exists
            authManager.authError.value?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Already have an Account? Login Now
            Text(
                text = "Already have an Account? Login Now",
                fontSize = 14.sp,
                color = SilentPadColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    try {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("RegisterActivity", "Login navigation error", e)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}