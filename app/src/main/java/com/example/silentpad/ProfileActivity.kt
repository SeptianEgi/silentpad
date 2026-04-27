package com.example.silentpad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silentpad.ui.theme.SilentPadTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

val ProfileBackgroundTop = Color(0xFFA6C6FF)
val ProfileBackgroundBottom = Color(0xFF050505)
val ProfileCardColor = Color(0xFFA6C6FF)
val ProfileTextColor = Color(0xFF1A1A1A)

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SilentPadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ProfileBackgroundBottom
                ) {
                    ProfileScreen()
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? ComponentActivity
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    // Get email from Firebase Auth first, then fallback to SharedPreferences
    val firebaseEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val registeredEmail = prefs.getString("registered_email", "") 
        ?: prefs.getString("email", "") 
        ?: prefs.getString("user_email", "") 
        ?: ""
    
    // Use Firebase email if available, otherwise use stored email
    val emailToShow = if (firebaseEmail.isNotEmpty()) firebaseEmail else registeredEmail
    
    var name by remember { mutableStateOf(prefs.getString("name", "") ?: "") }
    var phoneNumber by remember { mutableStateOf(prefs.getString("phone", "") ?: "") }
    var email by remember { mutableStateOf(emailToShow) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Profile picture state
    var profileImagePath by remember { mutableStateOf(prefs.getString("profile_image", "") ?: "") }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Copy selected image to internal storage so we have permanent access
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val outputFile = File(context.filesDir, "profile_picture.jpg")
                    val outputStream = FileOutputStream(outputFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    
                    // Save path to SharedPreferences and update state
                    val newPath = outputFile.absolutePath
                    prefs.edit().putString("profile_image", newPath).apply()
                    profileImagePath = newPath
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Save email to SharedPreferences if we got it from Firebase
    LaunchedEffect(firebaseEmail) {
        if (firebaseEmail.isNotEmpty() && registeredEmail != firebaseEmail) {
            prefs.edit().putString("registered_email", firebaseEmail).apply()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackgroundBottom)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .background(ProfileBackgroundTop),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = ProfileTextColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable {
                        // Launch image picker when clicked
                        imagePickerLauncher.launch("image/*")
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(4.dp, ProfileTextColor, CircleShape)
                            .padding(4.dp)
                            .background(Color.White, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Check if we have a saved profile image
                        val bitmap = remember(profileImagePath) {
                            if (profileImagePath.isNotEmpty() && File(profileImagePath).exists()) {
                                BitmapFactory.decodeFile(profileImagePath)
                            } else null
                        }
                        
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.size(150.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.menu22_text),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.size(150.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "+ Add Photo Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileTextColor,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .background(ProfileBackgroundBottom)
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileDisplayCard(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = email.ifEmpty { "Not set" }
                    )
                    
                    ProfileInputCard(
                        icon = Icons.Default.Person,
                        placeholder = "Name",
                        value = name,
                        onClick = {
                            editField = "name"
                            editValue = name
                            showEditDialog = true
                        }
                    )
                    
                    ProfileInputCard(
                        icon = Icons.Default.Phone,
                        placeholder = "Phone Number",
                        value = phoneNumber,
                        onClick = {
                            editField = "phone"
                            editValue = phoneNumber
                            showEditDialog = true
                        }
                    )
                    
                    ProfileInputCard(
                        icon = Icons.Default.Lock,
                        placeholder = "Change Password",
                        value = "••••••••",
                        onClick = {
                            showPasswordDialog = true
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            prefs.edit().apply {
                                putString("name", name)
                                putString("phone", phoneNumber)
                                apply()
                            }
                            Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileCardColor,
                            contentColor = ProfileTextColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Edit Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = { showLogoutDialog = true }
                    ) {
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Red,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
        
        IconButton(
            onClick = { activity?.finish() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfileTextColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
    
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { 
                Text(
                    when(editField) {
                        "name" -> "Edit Name"
                        "phone" -> "Edit Phone Number"
                        else -> "Edit"
                    },
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    placeholder = { 
                        Text(
                            when(editField) {
                                "name" -> "Enter your name"
                                "phone" -> "Enter phone number"
                                else -> ""
                            }
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when(editField) {
                            "name" -> name = editValue
                            "phone" -> phoneNumber = editValue
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showPasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        placeholder = { Text("Enter current password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        placeholder = { Text("Enter new password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Re-enter new password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        
                        when {
                            currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            }
                            newPassword != confirmPassword -> {
                                Toast.makeText(context, "New passwords don't match", Toast.LENGTH_SHORT).show()
                            }
                            newPassword.length < 6 -> {
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            }
                            firebaseUser != null -> {
                                firebaseUser.updatePassword(newPassword).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                        showPasswordDialog = false
                                    } else {
                                        Toast.makeText(context, "Failed to change password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            else -> {
                                Toast.makeText(context, "You must be logged in to change password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout? You will be redirected to login screen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Sign out from Firebase
                        FirebaseAuth.getInstance().signOut()
                        
                        // Clear only auth session (keep notes data)
                        prefs.edit().clear().apply()
                        
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to LoginActivity
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        activity?.finish()
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileInputCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = placeholder,
                tint = ProfileTextColor,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = value.ifEmpty { placeholder },
                fontSize = 16.sp,
                fontWeight = if (value.isEmpty()) FontWeight.Normal else FontWeight.SemiBold,
                color = if (value.isEmpty()) ProfileTextColor.copy(alpha = 0.6f) else ProfileTextColor,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProfileDisplayCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCardColor.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ProfileTextColor,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProfileTextColor,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
