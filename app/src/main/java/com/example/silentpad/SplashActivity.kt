package com.example.silentpad

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.silentpad.ui.theme.SilentPadTheme
import com.example.silentpad.ui.theme.SilentPadColors
import com.example.silentpad.ui.theme.Black
import com.example.silentpad.ui.theme.White
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class SplashScreenState {
    Logo, FindingLocation, Ready
}

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SilentPadTheme {
                var location by remember { mutableStateOf("Unknown") }
                var screenState by remember { mutableStateOf(SplashScreenState.Logo) }
                val context = LocalContext.current

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                val authManager = remember { com.example.silentpad.auth.SimpleAuthManager(context) }

                fun navigateToNextScreen(loc: String) {
                    if (authManager.isLoggedIn.value) {
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(context, WelcomeActivity::class.java)
                        intent.putExtra("user_location", loc)
                        startActivity(intent)
                    }
                    finish()
                }

                fun updateLocationName(loc: Location?) {
                    if (loc != null) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                                    if (addresses.isNotEmpty()) {
                                        val address = addresses[0]
                                        val city = address.locality ?: address.subAdminArea ?: address.adminArea
                                        val country = address.countryName
                                        val fullAddress = listOfNotNull(city, country).joinToString(", ")
                                        location = if (fullAddress.isBlank()) "Lokasi Terdeteksi" else fullAddress
                                        screenState = SplashScreenState.Ready
                                    } else {
                                        location = "Lat: ${String.format("%.2f", loc.latitude)}, Lon: ${String.format("%.2f", loc.longitude)}"
                                        screenState = SplashScreenState.Ready
                                    }
                                }
                            } else {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                if (addresses != null && addresses.isNotEmpty()) {
                                    val address = addresses[0]
                                    val city = address.locality ?: address.subAdminArea ?: address.adminArea
                                    val country = address.countryName
                                    val fullAddress = listOfNotNull(city, country).joinToString(", ")
                                    location = if (fullAddress.isBlank()) "Lokasi Terdeteksi" else fullAddress
                                } else {
                                    location = "Lat: ${String.format("%.2f", loc.latitude)}, Lon: ${String.format("%.2f", loc.longitude)}"
                                }
                                screenState = SplashScreenState.Ready
                            }
                        } catch (e: Exception) {
                            location = "Lat: ${String.format("%.2f", loc.latitude)}, Lon: ${String.format("%.2f", loc.longitude)}"
                            screenState = SplashScreenState.Ready
                        }
                    } else {
                        location = "Lokasi Tidak Dikenal"
                        screenState = SplashScreenState.Ready
                    }
                }

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        updateLocationName(locationResult.lastLocation)
                    }
                }

                @SuppressLint("MissingPermission")
                fun startLocationProcess() {
                    // Check permissions before making location calls
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        location = "Lokasi Tidak Dikenal"
                        screenState = SplashScreenState.Ready
                        return
                    }
                    
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        if (loc != null) {
                            updateLocationName(loc)
                        } else {
                            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                                .setWaitForAccurateLocation(false)
                                .setMinUpdateIntervalMillis(500)
                                .setMaxUpdateDelayMillis(1000)
                                .build()
                            
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                        }
                    }.addOnFailureListener {
                        location = "Lokasi Tidak Dikenal"
                        screenState = SplashScreenState.Ready
                    }
                }

                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) startLocationProcess() else {
                        updateLocationName(null)
                    }
                }

                LaunchedEffect(screenState) {
                    when (screenState) {
                        SplashScreenState.Logo -> {
                            delay(2000)
                            screenState = SplashScreenState.FindingLocation
                        }
                        SplashScreenState.FindingLocation -> {
                            val job = launch {
                                delay(8000)
                                if (screenState == SplashScreenState.FindingLocation) {
                                    updateLocationName(null)
                                }
                            }

                            delay(1000)
                            val playServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                            if (playServicesAvailable == ConnectionResult.SUCCESS) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    startLocationProcess()
                                } else {
                                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            } else {
                                job.cancel()
                                updateLocationName(null)
                            }
                        }
                        SplashScreenState.Ready -> {
                            delay(1000)
                            navigateToNextScreen(location)
                        }
                    }
                }

                SplashScreenContent(state = screenState, location = location)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreenContent(state: SplashScreenState, location: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = SilentPadColors.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                },
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { targetState ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (targetState) {
                        SplashScreenState.Logo -> LogoStage()
                        SplashScreenState.FindingLocation -> FindingLocationStage()
                        SplashScreenState.Ready -> {
                            Text(
                                text = "Lokasi: $location",
                                color = White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoStage() {
    var displayedText by remember { mutableStateOf("") }
    var showCursor by remember { mutableStateOf(true) }
    val fullText = "SilentPad"
    
    // Logo image fade-in animation
    var imageVisible by remember { mutableStateOf(false) }
    
    val imageAlpha by animateFloatAsState(
        targetValue = if (imageVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "image_fade"
    )
    
    // Cursor blinking animation
    val cursorAlpha by animateFloatAsState(
        targetValue = if (showCursor) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_blink"
    )
    
    LaunchedEffect(Unit) {
        delay(300) // Initial delay
        imageVisible = true
        delay(500) // Show logo first
        
        // Typing animation
        fullText.forEachIndexed { index, _ ->
            delay(70) // Typing speed
            displayedText = fullText.substring(0, index + 1)
        }
        
        delay(800) // Show complete text
        showCursor = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Wolf Moon Logo positioned same as WelcomeActivity
        Image(
            painter = painterResource(id = R.drawable.wolfmoon),
            contentDescription = "SilentPad Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .offset(y = 80.dp)
                .alpha(imageAlpha),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
        
        // Text centered in screen
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Typing text effect with cursor
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayedText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SilentPadColors.textPrimary,
                    letterSpacing = 2.sp
                )
                
                // Blinking cursor - only show during typing
                if (displayedText.isNotEmpty() && displayedText.length < fullText.length) {
                    Text(
                        text = "|",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Normal,
                        color = SilentPadColors.textPrimary,
                        modifier = Modifier.alpha(cursorAlpha)
                    )
                }
            }
        }
    }
}


@Composable
private fun FindingLocationStage() {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .alpha(0.2f)
                    .background(White, shape = MaterialTheme.shapes.extraLarge)
            )
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Finding Location",
                tint = White,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TypingText(text = "Mencari lokasi Anda...")
    }
}

@Composable
fun TypingText(text: String) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        for (i in 1..text.length) {
            displayedText = text.substring(0, i)
            delay(50)
        }
    }

    Text(
        text = displayedText,
        color = SilentPadColors.textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun TypingSubtitle(text: String, delay: Long = 0) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        kotlinx.coroutines.delay(delay)
        displayedText = ""
        for (i in 1..text.length) {
            displayedText = text.substring(0, i)
            kotlinx.coroutines.delay(80) // Slightly faster for subtitle
        }
    }

    Text(
        text = displayedText,
        color = SilentPadColors.textSecondary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Light
    )
}


