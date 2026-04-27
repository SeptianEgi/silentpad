package com.example.silentpad

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.silentpad.auth.AuthManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silentpad.ui.theme.SilentPadTheme
import com.example.silentpad.ui.theme.SilentPadColors
import com.example.silentpad.ui.theme.SilentPadButton
import com.example.silentpad.ui.theme.SilentPadTitle
import com.example.silentpad.ui.theme.BackButton
import com.example.silentpad.ui.theme.SocialButton

class WelcomeActivity : ComponentActivity() {
    
    // Create AuthManager at activity level to access it in onActivityResult
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager(this)
        val location = intent.getStringExtra("user_location") ?: "Unknown"
        
        setContent {
            SilentPadTheme {
                WelcomeScreen(location = location, authManager = authManager)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (::authManager.isInitialized) {
            authManager.onActivityResult(requestCode, resultCode, data)
        }
    }
}

@Composable
fun WelcomeScreen(location: String, authManager: AuthManager) {
    val context = LocalContext.current
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authManager.handleGoogleSignInResult(result.data)
    }

    LaunchedEffect(authManager.currentUser.value) {
        if (authManager.currentUser.value != null) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Pure Black Background
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
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 24.dp, start = 8.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            Spacer(modifier = Modifier.height(280.dp))
            
            // Location-based greeting
            if (location != "Unknown" && location.isNotBlank()) {
                Text(
                    text = getLocationGreeting(location),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = SilentPadColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Text(
                    text = getWelcomeMessage(location),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = SilentPadColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Text(
                    text = getWelcomeMessage(""),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = SilentPadColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            // SilentPad Title - Using global theme
            SilentPadTitle(
                text = "SilentPad",
                modifier = Modifier.padding(bottom = 40.dp)
            )
            
            // LOGIN Button - Using global component
            SilentPadButton(
                text = "LOGIN",
                onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(45.dp))
            
            // Sign Up Button - Using global component
            SilentPadButton(
                text = "Sign Up",
                onClick = {
                    val intent = Intent(context, RegisterActivity::class.java)
                    context.startActivity(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Social Logins (Google & Facebook)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SocialButton(
                    onClick = { authManager.signInWithGoogle(googleSignInLauncher) },
                    size = 56.dp,
                    backgroundColor = Color.White,
                    textColor = Color.Black,
                    iconRes = R.drawable.ic_google,
                    text = "Google Login"
                )
                
                Spacer(modifier = Modifier.width(24.dp))
                
                SocialButton(
                    onClick = { authManager.signInWithFacebook() },
                    size = 56.dp,
                    backgroundColor = Color.White,
                    textColor = Color.Black,
                    iconRes = R.drawable.ic_facebook,
                    text = "Facebook Login"
                )
            }
            
            // Show error if any
            authManager.authError.value?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// Function to generate location-based greeting with dynamic language
fun getLocationGreeting(location: String): String {
    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    
    return when {
        // Indonesian locations
        location.contains("Indonesia", ignoreCase = true) || 
        location.contains("Jakarta", ignoreCase = true) ||
        location.contains("Bandung", ignoreCase = true) ||
        location.contains("Surabaya", ignoreCase = true) ||
        location.contains("Medan", ignoreCase = true) ||
        location.contains("Semarang", ignoreCase = true) ||
        location.contains("Yogyakarta", ignoreCase = true) ||
        location.contains("Bali", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Selamat pagi"
                in 12..15 -> "Selamat siang" 
                in 16..18 -> "Selamat sore"
                else -> "Selamat malam"
            }
            "$timeGreeting dari $location!"
        }
        
        // Malaysian locations
        location.contains("Malaysia", ignoreCase = true) ||
        location.contains("Kuala Lumpur", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Selamat pagi"
                in 12..15 -> "Selamat tengah hari" 
                in 16..18 -> "Selamat petang"
                else -> "Selamat malam"
            }
            "$timeGreeting dari $location!"
        }
        
        // Singapore 
        location.contains("Singapore", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Good morning"
                in 12..15 -> "Good afternoon" 
                in 16..18 -> "Good evening"
                else -> "Good night"
            }
            "$timeGreeting from $location!"
        }
        
        // English-speaking countries
        location.contains("United States", ignoreCase = true) ||
        location.contains("United Kingdom", ignoreCase = true) ||
        location.contains("Australia", ignoreCase = true) ||
        location.contains("Canada", ignoreCase = true) ||
        location.contains("New Zealand", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Good morning"
                in 12..15 -> "Good afternoon" 
                in 16..18 -> "Good evening"
                else -> "Good night"
            }
            "$timeGreeting from $location!"
        }
        
        // Japanese locations
        location.contains("Japan", ignoreCase = true) ||
        location.contains("Tokyo", ignoreCase = true) ||
        location.contains("Osaka", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Ohayou gozaimasu"
                in 12..15 -> "Konnichiwa" 
                in 16..18 -> "Konbanwa"
                else -> "Konbanwa"
            }
            "$timeGreeting, $location kara!"
        }
        
        // Korean locations
        location.contains("Korea", ignoreCase = true) ||
        location.contains("Seoul", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Annyeonghaseyo"
                in 12..15 -> "Annyeonghaseyo" 
                in 16..18 -> "Annyeonghaseyo"
                else -> "Annyeonghaseyo"
            }
            "$timeGreeting from $location!"
        }
        
        // Chinese locations
        location.contains("China", ignoreCase = true) ||
        location.contains("Beijing", ignoreCase = true) ||
        location.contains("Shanghai", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Zao shang hao"
                in 12..15 -> "Xia wu hao" 
                in 16..18 -> "Wan shang hao"
                else -> "Wan an"
            }
            "$timeGreeting from $location!"
        }
        
        // French-speaking countries
        location.contains("France", ignoreCase = true) ||
        location.contains("Paris", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Bonjour"
                in 12..15 -> "Bon après-midi" 
                in 16..18 -> "Bonsoir"
                else -> "Bonsoir"
            }
            "$timeGreeting de $location!"
        }
        
        // Spanish-speaking countries
        location.contains("Spain", ignoreCase = true) ||
        location.contains("Mexico", ignoreCase = true) ||
        location.contains("Argentina", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Buenos días"
                in 12..15 -> "Buenas tardes" 
                in 16..18 -> "Buenas tardes"
                else -> "Buenas noches"
            }
            "¡$timeGreeting desde $location!"
        }
        
        // German-speaking countries
        location.contains("Germany", ignoreCase = true) ||
        location.contains("Austria", ignoreCase = true) -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Guten Morgen"
                in 12..15 -> "Guten Tag" 
                in 16..18 -> "Guten Abend"
                else -> "Gute Nacht"
            }
            "$timeGreeting aus $location!"
        }
        
        // Default to English for other locations
        else -> {
            val timeGreeting = when (currentHour) {
                in 5..11 -> "Good morning"
                in 12..15 -> "Good afternoon" 
                in 16..18 -> "Good evening"
                else -> "Good night"
            }
            "$timeGreeting from $location!"
        }
    }
}

// Function to get welcome message based on location language
fun getWelcomeMessage(location: String): String {
    return when {
        // Indonesian locations
        location.contains("Indonesia", ignoreCase = true) || 
        location.contains("Jakarta", ignoreCase = true) ||
        location.contains("Bandung", ignoreCase = true) ||
        location.contains("Surabaya", ignoreCase = true) ||
        location.contains("Medan", ignoreCase = true) ||
        location.contains("Semarang", ignoreCase = true) ||
        location.contains("Yogyakarta", ignoreCase = true) ||
        location.contains("Bali", ignoreCase = true) -> 
            "Selamat datang di SilentPad!"
        
        // Malaysian locations
        location.contains("Malaysia", ignoreCase = true) ||
        location.contains("Kuala Lumpur", ignoreCase = true) ->
            buildString {
        append("Selamat datang di")
    }
        
        // Japanese locations
        location.contains("Japan", ignoreCase = true) ||
        location.contains("Tokyo", ignoreCase = true) ||
        location.contains("Osaka", ignoreCase = true) -> 
            "Welcome to"
        
        // Korean locations
        location.contains("Korea", ignoreCase = true) ||
        location.contains("Seoul", ignoreCase = true) -> 
            "Welcome to"
        
        // Chinese locations
        location.contains("China", ignoreCase = true) ||
        location.contains("Beijing", ignoreCase = true) ||
        location.contains("Shanghai", ignoreCase = true) -> 
            "Welcome to"
        
        // French-speaking countries
        location.contains("France", ignoreCase = true) ||
        location.contains("Paris", ignoreCase = true) -> 
            "Bienvenue sur"
        
        // Spanish-speaking countries
        location.contains("Spain", ignoreCase = true) ||
        location.contains("Mexico", ignoreCase = true) ||
        location.contains("Argentina", ignoreCase = true) -> 
            "¡Bienvenido a"
        
        // German-speaking countries
        location.contains("Germany", ignoreCase = true) ||
        location.contains("Austria", ignoreCase = true) -> 
            "Willkommen bei"
        
        // Default to English for other locations or empty location
        else -> "Welcome to"
    }
}


