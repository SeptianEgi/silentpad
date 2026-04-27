package com.example.silentpad

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SilentPadAi {
    // API Key from BuildConfig (local.properties)
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    // We use gemini-flash-lite-latest to stay within free tier limits and avoid overload
    private val generativeModel = GenerativeModel(
        modelName = "gemini-flash-lite-latest",
        apiKey = apiKey
    )

    /**
     * AI Feature 1: "Continue Writing" (Asisten Penulis)
     * Membaca teks yang ada dan menghasilkan 1-2 kalimat kelanjutannya.
     */
    suspend fun continueWriting(currentText: String): String {
        if (currentText.trim().isEmpty()) {
            return "Start writing a few words first so I can feel your silence..."
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are 'Wolf's Echo', a poetic and reflective writing assistant for an app called SilentPad.
                    The app's theme is 'Silence writes memory', featuring a night wolf and moon aesthetic.
                    Read the following journal entry or note, and write the NEXT 1 or 2 sentences to help the writer continue their thought. 
                    Match the tone, language, and style of the existing text. Do not rewrite the existing text, ONLY provide the continuation.
                    
                    Existing text:
                    "$currentText"
                    
                    Continuation:
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                response.text ?: "The wolf remains silent..."
            } catch (e: Exception) {
                "Silence... (Error: ${e.localizedMessage})"
            }
        }
    }

    /**
     * AI Feature 2: "Auto-Generate Title" (Pemberi Judul Otomatis)
     * Membaca isi catatan dan merangkumnya menjadi judul yang pendek dan puitis.
     */
    suspend fun generateTitle(noteContent: String): String {
        if (noteContent.trim().isEmpty()) {
            return "Silent Memory"
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are 'Wolf's Whisper', an AI that creates poetic and meaningful titles for journal entries in the app SilentPad.
                    Read the following text and generate ONE short, poetic, and fitting title (maximum 5 words).
                    Do not use quotes around the title. Just output the title text. If the text is in Indonesian, provide an Indonesian title.
                    
                    Text:
                    "$noteContent"
                    
                    Title:
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                response.text?.trim()?.removeSurrounding("\"") ?: "Silent Memory"
            } catch (e: Exception) {
                "Silent Memory"
            }
        }
    }
}
