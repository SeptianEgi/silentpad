package com.example.silentpad

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silentpad.ui.theme.SilentPadTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val BackgroundBlack = Color(0xFF050505)
val PeriwinkleBlue = Color(0xFFA6C6FF)
val CardTextBlack = Color(0xFF1A1A1A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SilentPadTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBlack) {
                    MainMenuScreen()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Trigger recomposition when activity resumes
        setContent {
            SilentPadTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBlack) {
                    MainMenuScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? MainActivity
    val noteManager = remember { NoteManager(context) }
    var notesList by remember { mutableStateOf(noteManager.getAllNotes()) }
    var showMenu by remember { mutableStateOf(false) }
    var placeholderCount by remember { mutableStateOf(maxOf(0, 4 - noteManager.getAllNotes().size)) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    
    // Auto-refresh notes ketika kembali ke screen ini
    LaunchedEffect(Unit) {
        while (true) {
            delay(300) // Check setiap 300ms
            val updatedNotes = noteManager.getAllNotes()
            if (updatedNotes != notesList) {
                notesList = updatedNotes
                // Update placeholder count jika kurang dari 4 total items
                placeholderCount = maxOf(0, 4 - notesList.size)
            }
        }
    }
    
    val moonAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500, easing = EaseInOut),
        label = "moonAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Bottom Moon Image - Layer paling bawah
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.menu11),
                contentDescription = "Moon Wolf",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(400.dp)
                    .offset(y = 50.dp)
                    .graphicsLayer(alpha = moonAlpha)
            )
        }
        
        // Content layer - di atas moon
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Menu dropdown di kanan atas
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = PeriwinkleBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Dropdown menu dengan alignment yang benar
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset((400).dp, 0.dp)
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "Profile",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        },
                        onClick = { 
                            showMenu = false
                            val intent = Intent(context, ProfileActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "Settings",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        },
                        onClick = { 
                            showMenu = false
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "About",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        },
                        onClick = { 
                            showMenu = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Logo Wolf + Quote Bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 40.dp)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            PeriwinkleBlue, 
                            RoundedCornerShape(
                                topEnd = 25.dp, 
                                bottomEnd = 25.dp, 
                                topStart = 10.dp, 
                                bottomStart = 10.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\"Silence writes memory\"",
                        color = CardTextBlack,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 30.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(80.dp)
                        .border(3.dp, PeriwinkleBlue, CircleShape)
                        .padding(1.dp)
                        .background(Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.menu3_text),
                        contentDescription = "Wolf Logo",
                        modifier = Modifier.size(500.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Content box dengan list notes dan placeholder
            Box(
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 280.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Tampilkan notes yang tersimpan dengan swipe to delete
                    items(
                        items = notesList,
                        key = { note -> note.id }
                    ) { note ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    // Tampilkan konfirmasi delete untuk note yang ada datanya
                                    noteToDelete = note
                                    showDeleteDialog = true
                                    false // Jangan langsung dismiss, tunggu konfirmasi
                                } else {
                                    false
                                }
                            }
                        )
                        
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = Color.Red.copy(alpha = 0.8f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true
                        ) {
                            NoteItemCard(
                                note = note,
                                onNoteClick = {
                                    val intent = Intent(context, NoteActivity::class.java)
                                    intent.putExtra("NOTE_ID", note.id)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                    
                    // Tampilkan placeholder - swipe langsung delete tanpa konfirmasi
                    items(
                        count = placeholderCount,
                        key = { index -> "placeholder_$index" }
                    ) { index ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    // Langsung hapus placeholder tanpa konfirmasi
                                    placeholderCount = maxOf(0, placeholderCount - 1)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = Color.Red.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(60.dp))
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true
                        ) {
                            PlaceholderNoteCard(
                                onNoteClick = {
                                    val intent = Intent(context, NoteActivity::class.java)
                                    intent.putExtra("NOTE_ID", -1L) // New note
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && noteToDelete != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false
                    noteToDelete = null
                },
                title = { Text("Delete Note?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete \"${noteToDelete?.title}\"? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            noteToDelete?.let { note ->
                                noteManager.deleteNote(note.id)
                                notesList = noteManager.getAllNotes()
                                placeholderCount = maxOf(0, 4 - notesList.size)
                            }
                            showDeleteDialog = false
                            noteToDelete = null
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDeleteDialog = false
                            noteToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // FAB untuk add placeholder atau note baru
        FloatingActionButton(
            onClick = {
                // Tambah placeholder baru
                placeholderCount++
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp),
            containerColor = PeriwinkleBlue,
            contentColor = CardTextBlack,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Placeholder",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun PlaceholderNoteCard(
    onNoteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        shape = RoundedCornerShape(60.dp),
        colors = CardDefaults.cardColors(containerColor = PeriwinkleBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onNoteClick)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TITLE...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CardTextBlack,
                fontFamily = FontFamily.Serif,
                letterSpacing = 1.sp
            )
            
            // Tombol + di kanan seperti mockup
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .background(Color.Black, CircleShape)
                    .clickable(onClick = onNoteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Open Note",
                    tint = PeriwinkleBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun NoteItemCard(
    note: Note,
    onNoteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val dateString = remember(note.timestamp) { dateFormat.format(Date(note.timestamp)) }
    
    val safeColor = note.color and 0xFFFFFFFFL
    val finalColor = if (safeColor == 0L) 0xFFA9CFFFL else safeColor
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNoteClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(finalColor)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CardTextBlack,
                    fontFamily = FontFamily.SansSerif,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (note.content.isNotEmpty()) {
                    Text(
                        text = note.content,
                        fontSize = 14.sp,
                        color = CardTextBlack.copy(alpha = 0.7f),
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateString,
                        fontSize = 12.sp,
                        color = CardTextBlack.copy(alpha = 0.5f),
                        fontFamily = FontFamily.SansSerif
                    )
                    
                    if (note.author.isNotEmpty()) {
                        Text(
                            text = "By: ${note.author}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CardTextBlack.copy(alpha = 0.6f),
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainMenuPreview() {
    SilentPadTheme {
        MainMenuScreen()
    }
}

