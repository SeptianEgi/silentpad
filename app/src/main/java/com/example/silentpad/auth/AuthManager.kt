package com.example.silentpad.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.silentpad.R
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient
    private val facebookCallbackManager: CallbackManager = CallbackManager.Factory.create()
    
    // Authentication state
    val isLoading: MutableState<Boolean> = mutableStateOf(false)
    val authError: MutableState<String?> = mutableStateOf(null)
    val currentUser: MutableState<FirebaseUser?> = mutableStateOf(auth.currentUser)
    
    init {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_web_client_id)) // Requires actual client ID from strings.xml
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        
        // Setup Facebook Login
        setupFacebookLogin()
    }
    
    private fun setupFacebookLogin() {
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d("FacebookAuth", "Facebook login success")
                    handleFacebookAccessToken(loginResult.accessToken)
                }
                
                override fun onCancel() {
                    Log.d("FacebookAuth", "Facebook login cancelled")
                    authError.value = "Facebook login cancelled"
                    isLoading.value = false
                }
                
                override fun onError(error: FacebookException) {
                    Log.e("FacebookAuth", "Facebook login error", error)
                    authError.value = "Facebook login failed: ${error.message}"
                    isLoading.value = false
                }
            })
    }
    
    // Google Sign In
    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        isLoading.value = true
        authError.value = null
        
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)!!
            Log.d("GoogleAuth", "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w("GoogleAuth", "Google sign in failed", e)
            authError.value = "Google sign in failed: ${e.message}"
            isLoading.value = false
        }
    }
    
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("GoogleAuth", "firebaseAuthWithGoogle:" + acct.id)
        
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleAuth", "signInWithCredential:success")
                    currentUser.value = auth.currentUser
                    isLoading.value = false
                } else {
                    Log.w("GoogleAuth", "signInWithCredential:failure", task.exception)
                    authError.value = "Authentication failed: ${task.exception?.message}"
                    isLoading.value = false
                }
            }
    }
    
    // Facebook Sign In
    fun signInWithFacebook() {
        isLoading.value = true
        authError.value = null
        
        LoginManager.getInstance().logInWithReadPermissions(
            context as android.app.Activity,
            listOf("email", "public_profile")
        )
    }
    
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("FacebookAuth", "handleFacebookAccessToken:" + token)
        
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FacebookAuth", "signInWithCredential:success")
                    currentUser.value = auth.currentUser
                    isLoading.value = false
                } else {
                    Log.w("FacebookAuth", "signInWithCredential:failure", task.exception)
                    authError.value = "Facebook authentication failed: ${task.exception?.message}"
                    isLoading.value = false
                }
            }
    }
    
    // Handle Facebook callback result
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }
    
    // Sign out
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        LoginManager.getInstance().logOut()
        currentUser.value = null
    }
    
    // Email/Password Authentication
    fun signInWithEmailPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        isLoading.value = true
        authError.value = null
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    currentUser.value = auth.currentUser
                    onComplete(true, null)
                } else {
                    val error = task.exception?.message ?: "Authentication failed"
                    authError.value = error
                    onComplete(false, error)
                }
            }
    }
    
    fun createUserWithEmailPassword(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        isLoading.value = true
        authError.value = null
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    currentUser.value = auth.currentUser
                    onComplete(true, null)
                } else {
                    val error = task.exception?.message ?: "Registration failed"
                    authError.value = error
                    onComplete(false, error)
                }
            }
    }
}