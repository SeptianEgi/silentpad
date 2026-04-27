package com.example.silentpad

import android.content.Context
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silentpad.ui.theme.SilentPadTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Colors based on the mockup - exact colors from design
val TitleTextColor = Color(0xFF000514)
val NoteBackgroundColor = Color(0xFFA9CFFF)
val SaveButtonColor = Color(0xFFA9CFFF)
val BackgroundDark = Color(0xFF000000)
val ButtonCircleColor = Color(0xFFA9CFFF)
val LineColor = Color(0xFF6B9BD1)

// Data class untuk menyimpan note
data class Note(
    val id: Long = System.currentTimeMillis(),
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// Helper class untuk SharedPreferences
class NoteManager(context: Context) {
    private val prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveNote(note: Note) {
        val notes = getAllNotes().toMutableList()
        val existingIndex = notes.indexOfFirst { it.id == note.id }
        
        if (existingIndex != -1) {
            notes[existingIndex] = note
        } else {
            notes.add(0, note) // Add new note at the beginning
        }
        
        val json = gson.toJson(notes)
        prefs.edit().putString("notes_list", json).apply()
    }

    fun getAllNotes(): List<Note> {
        val json = prefs.getString("notes_list", null) ?: return emptyList()
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(json, type)
    }

    fun deleteNote(noteId: Long) {
        val notes = getAllNotes().toMutableList()
        notes.removeAll { it.id == noteId }
        val json = gson.toJson(notes)
        prefs.edit().putString("notes_list", json).apply()
    }

    fun getNoteById(noteId: Long): Note? {
        return getAllNotes().find { it.id == noteId }
    }
}

class NoteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SilentPadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    NoteScreen()
                }
            }
        }
    }
}

@Composable
fun NoteScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val noteManager = remember { NoteManager(context) }
    
    // Get note ID from intent (-1 means new note)
    val noteId = remember { activity?.intent?.getLongExtra("NOTE_ID", -1L) ?: -1L }
    val existingNote = remember { 
        if (noteId != -1L) noteManager.getNoteById(noteId) else null
    }
    
    // State untuk note content
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var noteContent by remember { mutableStateOf(existingNote?.content ?: "") }
    var currentNoteId by remember { mutableStateOf(existingNote?.id ?: System.currentTimeMillis()) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBackDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    
    val silentPadAi = remember { SilentPadAi() }
    val coroutineScope = rememberCoroutineScope()
    var isAiLoading by remember { mutableStateOf(false) }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }

    // Track changes
    LaunchedEffect(title, noteContent) {
        hasUnsavedChanges = title != (existingNote?.title ?: "") || 
                           noteContent != (existingNote?.content ?: "")
    }

    // Fungsi untuk save note
    fun saveNote() {
        if (title.isNotEmpty() || noteContent.isNotEmpty()) {
            val note = Note(
                id = currentNoteId,
                title = title.ifEmpty { "Untitled" },
                content = noteContent,
                timestamp = System.currentTimeMillis()
            )
            noteManager.saveNote(note)
            hasUnsavedChanges = false
            Toast.makeText(context, "Note saved successfully ✓", Toast.LENGTH_SHORT).show()
            
            // Kembali ke MainActivity
            activity?.finish()
        } else {
            Toast.makeText(context, "Please write something first", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle back button press
    BackHandler {
        if (hasUnsavedChanges) {
            showBackDialog = true
        } else {
            activity?.finish()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (noteId != -1L) {
                            noteManager.deleteNote(noteId)
                            Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteDialog = false
                        activity?.finish()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Back confirmation dialog
    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text("Unsaved Changes", fontWeight = FontWeight.Bold) },
            text = { Text("You have unsaved changes. Do you want to save before leaving?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        saveNote()
                        showBackDialog = false
                    }
                ) {
                    Text("Save & Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBackDialog = false
                        activity?.finish()
                    }
                ) {
                    Text("Discard", color = Color.Red)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Back button, Title field, and Save button - Professional design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button dengan shadow
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = ButtonCircleColor,
                    shadowElevation = 4.dp,
                    onClick = {
                        if (hasUnsavedChanges) {
                            showBackDialog = true
                        } else {
                            (context as? ComponentActivity)?.finish()
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TitleTextColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Title Input Field
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NoteBackgroundColor)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = TitleTextColor,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.SansSerif
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "TITLE...",
                                            fontSize = 18.sp,
                                            color = TitleTextColor.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        // AI Generate Title Button
                        IconButton(
                            onClick = {
                                if (noteContent.isNotBlank()) {
                                    isAiLoading = true
                                    coroutineScope.launch {
                                        title = silentPadAi.generateTitle(noteContent)
                                        isAiLoading = false
                                        Toast.makeText(context, "Wolf's Whisper received ✨", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Write a note first to generate a title", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = TitleTextColor,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Wolf's Whisper (Auto Title)",
                                    tint = TitleTextColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Save Button dengan shadow dan visual feedback
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = if (hasUnsavedChanges) SaveButtonColor else SaveButtonColor.copy(alpha = 0.6f),
                    shadowElevation = if (hasUnsavedChanges) 6.dp else 2.dp,
                    onClick = { saveNote() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = TitleTextColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Note content area - Clean design without lines
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NoteBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    BasicTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = TitleTextColor,
                            fontFamily = FontFamily.SansSerif,
                            lineHeight = 24.sp,
                            letterSpacing = 0.2.sp
                        ),
                        cursorBrush = SolidColor(TitleTextColor),
                        modifier = Modifier.fillMaxSize(),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (noteContent.isEmpty()) {
                                    Text(
                                        text = "Start writing your note here...",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = TitleTextColor.copy(alpha = 0.35f),
                                            fontFamily = FontFamily.SansSerif,
                                            lineHeight = 24.sp,
                                            letterSpacing = 0.2.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel/Back Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    color = Color(0xFF6B6B6B),
                    shadowElevation = 4.dp,
                    onClick = {
                        if (hasUnsavedChanges) {
                            showBackDialog = true
                        } else {
                            activity?.finish()
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Save Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    color = ButtonCircleColor,
                    shadowElevation = if (hasUnsavedChanges) 6.dp else 4.dp,
                    onClick = { saveNote() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save Note",
                            fontSize = 16.sp,
                            color = TitleTextColor,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Professional Floating Action Buttons dengan shadow dan animasi
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Wolf's Inspiration Button (Continue Writing)
            FloatingActionButton(
                onClick = { 
                    if (!isAiLoading) {
                        isAiLoading = true
                        Toast.makeText(context, "Wolf is listening...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            val continuation = silentPadAi.continueWriting(noteContent)
                            if (noteContent.isNotEmpty() && !noteContent.endsWith(" ") && !noteContent.endsWith("\n")) {
                                noteContent += " "
                            }
                            noteContent += continuation
                            isAiLoading = false
                        }
                    }
                },
                containerColor = Color(0xFFFFC107), // Golden color for AI
                contentColor = Color(0xFF000514),
                modifier = Modifier.size(52.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF000514),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Wolf's Inspiration (Continue Writing)",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Delete Button dengan elevation
            FloatingActionButton(
                onClick = { showDeleteDialog = true },
                containerColor = Color(0xFFEF5350),
                contentColor = Color.White,
                modifier = Modifier.size(52.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Note",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Share Button dengan elevation
            FloatingActionButton(
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "${title.ifEmpty { "Untitled" }}\n\n$noteContent")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share note via"))
                },
                containerColor = Color(0xFF66BB6A),
                contentColor = Color.White,
                modifier = Modifier.size(52.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Note",
                    modifier = Modifier.size(24.dp)
                )
            }

            // More Options Button dengan elevation
            Box {
                FloatingActionButton(
                    onClick = { showMoreOptionsMenu = true },
                    containerColor = Color(0xFFAB47BC),
                    contentColor = Color.White,
                    modifier = Modifier.size(52.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMoreOptionsMenu,
                    onDismissRequest = { showMoreOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Copy to Clipboard") },
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Note", "${title.ifEmpty { "Untitled" }}\n\n$noteContent")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            showMoreOptionsMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear Note") },
                        onClick = {
                            title = ""
                            noteContent = ""
                            showMoreOptionsMenu = false
                            Toast.makeText(context, "Note cleared", Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NoteScreenPreview() {
    SilentPadTheme {
        NoteScreen()
    }
}