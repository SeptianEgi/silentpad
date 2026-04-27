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

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                SilentPadTheme {
                    LoginScreen()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LoginActivity", "Error in onCreate", e)
            finish()
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val authManager = remember { SimpleAuthManager(context) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Pure Black Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SilentPadColors.background)
        )
        
        // Back Button - Top Left Corner
        BackButton(
            onClick = {
                try {
                    val intent = Intent(context, WelcomeActivity::class.java)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("LoginActivity", "Back navigation error", e)
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 24.dp, start = 8.dp)
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(400.dp))
            
            // Sign In Title using global component
            SilentPadTitle(
                text = "Sign In",
                fontSize = 30.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // "Welcome back!" subtitle using theme colors
            Text(
                text = "Welcome back!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = SilentPadColors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Email Address Field
            SilentPadInputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                width = 330.dp,
                height = 60.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            SilentPadInputField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                width = 330.dp,
                height = 60.dp,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // LOGIN Button
            SilentPadButton(
                text = if (authManager.isLoading.value) "Logging in..." else "LOGIN",
                onClick = {
                    authManager.clearError()
                    if (email.isNotBlank() && password.isNotBlank()) {
                        authManager.signInWithEmailPassword(email, password) { success, error ->
                            if (success) {
                                try {
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.util.Log.e("LoginActivity", "Navigation error", e)
                                    authManager.authError.value = "Navigation failed"
                                }
                            }
                        }
                    } else {
                        authManager.authError.value = "Please fill in all fields"
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
            
            // Don't have Account? Sign Up Now
            Text(
                text = "Don't have Account? Sign Up Now",
                fontSize = 14.sp,
                color = SilentPadColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    try {
                        val intent = Intent(context, RegisterActivity::class.java)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("LoginActivity", "Register navigation error", e)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}