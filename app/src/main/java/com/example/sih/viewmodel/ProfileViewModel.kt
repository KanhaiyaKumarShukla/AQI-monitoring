package com.example.sih.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await


class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _tempProfile = MutableStateFlow<UserProfile?>(null)
    val tempProfile: StateFlow<UserProfile?> = _tempProfile.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun initialize(currentUser: FirebaseUser?) {
        currentUser?.let { user ->
            val profile = fetchUserProfile(user.uid)
            _userProfile.value = profile ?: UserProfile(
                name = user.displayName ?: "",
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                createdAt = Date()
            )
            _tempProfile.value = _userProfile.value?.copy()
        }
    }

    suspend fun fetchUserProfile(userId: String): UserProfile? {
        return try {
            _isLoading.value = true
            val document = firestore.collection("profiles")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(UserProfile::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            _isLoading.value = false
        }
    }

    // Rest of your existing functions...
    fun isProfileValid(): Boolean {
        return !tempProfile.value?.name.isNullOrBlank()
    }

    fun enterEditMode() {
        _editMode.value = true
    }

    fun cancelEdit() {
        _tempProfile.value = _userProfile.value?.copy()
        _editMode.value = false
    }

    fun updateTempProfile(name: String? = null, phone: String? = null) {
        _tempProfile.value = _tempProfile.value?.copy(
            name = name ?: _tempProfile.value?.name ?: "",
            phone = phone ?: _tempProfile.value?.phone ?: ""
        )
    }

    fun isPhoneValid(): Boolean {
        return tempProfile.value?.phone?.matches(Regex("^[0-9]{10}\$")) ?: true
    }

    suspend fun saveProfile(userId: String): Boolean {
        return try {
            _isLoading.value = true
            val updatedProfile = _tempProfile.value ?: return false

            firestore.collection("profiles")
                .document(userId)
                .set(updatedProfile)
                .await()


            _userProfile.value = updatedProfile
            _editMode.value = false
            true
        } catch (e: Exception) {
            false
        } finally {
            _isLoading.value = false
        }
    }
}