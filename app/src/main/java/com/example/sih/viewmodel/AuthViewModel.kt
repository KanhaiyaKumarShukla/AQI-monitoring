package com.example.sih.viewmodel

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Authentication state
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Current user email
    val currentUserEmail: String?
        get() = auth.currentUser?.email

    // Current user display name
    val currentUserName: String?
        get() = auth.currentUser?.displayName

    // Current user ID
    val currentUserId: String?
        get() = auth.currentUser?.uid

    // User profile data
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()


    private val idTokenListener = FirebaseAuth.IdTokenListener { firebaseAuth ->
        // Token changed - you can get the new token with:
        firebaseAuth.currentUser?.getIdToken(false)?.addOnCompleteListener { task ->
            val newToken = task.result?.token
            // Handle new token if needed
        }
    }

    init {
        // Add the auth state listener
        auth.addAuthStateListener { firebaseAuth ->
            _isLoggedIn.value = firebaseAuth.currentUser != null
            if (firebaseAuth.currentUser != null) {
                loadUserProfile()
            } else {
                _userProfile.value = null
            }
        }
        auth.addIdTokenListener(idTokenListener)
    }
    fun loadUserProfile(uid:String?=auth.currentUser?.uid) {
        if(uid.isNullOrEmpty())return;
        firestore.collection("profiles").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Failed to load profile"
                    return@addSnapshotListener
                }

                Log.d("profile", "loadUserProfile: $snapshot, ${snapshot?.exists()}, ${snapshot?.data}")
                if (snapshot != null && snapshot.exists()) {
                    _userProfile.value = snapshot.toObject(UserProfile::class.java)
                    Log.d("profile", "loadUserProfile if : ${_userProfile.value}")
                } else {
                    // Handle case where profile doesn't exist
                    _userProfile.value = UserProfile(
                        name = auth.currentUser?.displayName ?: "",
                        email = auth.currentUser?.email ?: "",
                        phone = "",
                        createdAt = Date()
                    )
                    Log.d("profile", "loadUserProfile else : ${_userProfile.value}")
                }
            }
    }

    fun login(email: String, password: String,) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loadUserProfile()
                            _isLoggedIn.value = true
                            Log.d("login", "login1: ${_isLoggedIn.value}")

                        } else {
                            _errorMessage.value = task.exception?.message ?: "Login failed"
                            _isLoggedIn.value = false
                            Log.d("login", "login2: ${_isLoggedIn.value}, ${_errorMessage.value}")
                        }
                        _isLoading.value=false
                        Log.d("login", "login: ${_isLoggedIn.value}")
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login failed"
                _isLoading.value = false
                Log.d("login", "login: ${_isLoggedIn.value}")
            }
        }
    }

    fun signup(name: String, email: String, phone: String, password: String, onSuccessfulSignup: () -> Unit, onFailedSignup: () -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Update user profile with name
                            val user = auth.currentUser
                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                                // You can add phone number to Firebase user if needed
                                // but typically phone numbers are stored in Firestore
                            }

                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    saveUserProfileToFirestore(
                                        userId = user.uid,
                                        name = name,
                                        email = email,
                                        phone = phone
                                    )
                                    onSuccessfulSignup()
                                } else {
                                    _errorMessage.value = updateTask.exception?.message ?: "Profile update failed"
                                    onFailedSignup()
                                }
                                _isLoading.value = false
                            }
                        } else {
                            _errorMessage.value = task.exception?.message ?: "Signup failed"
                            _isLoading.value = false
                        }
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Signup failed"
                _isLoading.value = false
            }
        }
    }

    private fun saveUserProfileToFirestore(userId: String, name: String, email: String, phone: String) {
        val userProfile = UserProfile(
            uid=userId,
            name = name,
            email = email,
            phone = phone,
            createdAt = Date(),
            role="viewer"
        )

        firestore.collection("profiles").document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message ?: "Failed to save profile"
                _isLoading.value = false
            }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _errorMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    override fun onCleared() {
        super.onCleared()
        // Remove the listener when ViewModel is cleared
        auth.removeIdTokenListener(idTokenListener)
    }
}


data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "viewer",
    val createdAt: Date = Date(),
    val assignedState: String? = null,  // For managers
    val managerId: String? = null,  // For technicians
    val stations: List<String> = emptyList()  // For technicians
) {

    constructor() : this("", "", "", "", "", Date(), null, null, emptyList())
}