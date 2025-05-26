package com.example.sih.viewmodel.admin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sih.repository.AqiRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih.util.AqiData
import com.example.sih.viewmodel.UserProfile
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@HiltViewModel
class RoleManagementViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _managers = MutableStateFlow<List<UserProfile>>(emptyList())
    val managers: StateFlow<List<UserProfile>> = _managers.asStateFlow()

    private val _technicians = MutableStateFlow<List<UserProfile>>(emptyList())
    val technicians: StateFlow<List<UserProfile>> = _technicians.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _states = MutableStateFlow(listOf(
        "Andhra Pradesh", "Assam", "Bihar", "Chandigarh", "Delhi",
        "Gujarat", "Haryana", "Jharkhand", "Karnataka", "Kerala",
        "Madhya Pradesh", "Maharashtra", "Meghalaya", "Mizoram",
        "Odisha", "Punjab", "Rajasthan", "Tamil Nadu", "Telangana",
        "Uttar Pradesh", "West Bengal"
    ))
    val states: StateFlow<List<String>> = _states.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun fetchManagersForState(state: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestore.collection("profiles")
                    .whereEqualTo("role", "manager")
                    .whereEqualTo("assignedState", state)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _snackbarMessage.value = "Failed to load managers: ${error.message}"
                            return@addSnapshotListener
                        }

                        _managers.value = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(UserProfile::class.java)?.copy(uid = doc.id)
                        } ?: emptyList()
                    }
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to load managers: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun appointManager(
        email: String,
        name: String,
        state: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check if profile exists
                val query = firestore.collection("profiles")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()

                if (query.documents.isNotEmpty()) {
                    // Update existing profile
                    val userDoc = query.documents[0]
                    firestore.collection("profiles").document(userDoc.id)
                        .update(
                            "role", "manager",
                            "assignedState", state
                        ).await()
                    _snackbarMessage.value = "$name appointed as manager for $state"
                    onComplete(true)
                } else {
                    // Check if user exists in auth system
                    try {
                        val signInMethods = auth.fetchSignInMethodsForEmail(email).await()

                        if (signInMethods.signInMethods?.isNotEmpty() == true) {
                            // User exists in auth but no profile - create one
                            val newProfile = UserProfile(
                                uid = "", // Will be set when we find the user
                                name = name,
                                email = email,
                                role = "manager",
                                assignedState = state,
                                createdAt = Date()
                            )

                            // Find the user by email to get UID
                            val userRecords = firestore.collection("profiles")
                                .whereEqualTo("email", email)
                                .limit(1)
                                .get()
                                .await()
                            if (userRecords.documents.isNotEmpty()) {
                                val uid = userRecords.documents[0].id
                                firestore.collection("profiles").document(uid)
                                    .set(newProfile.copy(uid = uid))
                                    .await()
                                _snackbarMessage.value = "$name appointed as manager for $state"
                                onComplete(true)
                            } else {
                                _snackbarMessage.value = "User account exists but profile not found"
                                onComplete(false)
                            }
                        } else {
                            _snackbarMessage.value = "User must create an account first"
                            onComplete(false)
                        }
                    } catch (e: Exception) {
                        _snackbarMessage.value = "Error checking user account: ${e.message}"
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error: ${e.message}"
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeManager(managerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestore.collection("profiles").document(managerId)
                    .update("role", "viewer", "assignedState", null)
                    .await()
                _snackbarMessage.value = "Manager removed successfully"
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to remove manager: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun appointTechnician(
        email: String,
        managerId: String,
        selectedStations: List<String>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Check if profile exists
                val profileQuery = firestore.collection("profiles")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()

                if (profileQuery.documents.isEmpty()) {
                    _snackbarMessage.value = "User profile not found"
                    onComplete(false, "User profile not found")
                    return@launch
                }

                val profileDoc = profileQuery.documents[0]
                val userId = profileDoc.id

                // 2. Check if user exists in auth system
                try {
                    auth.fetchSignInMethodsForEmail(email).await()
                } catch (e: Exception) {
                    _snackbarMessage.value = "User must create an account first"
                    onComplete(false, "User must create an account first")
                    return@launch
                }

                // 3. Update technician's profile
                firestore.collection("profiles").document(userId)
                    .update(
                        mapOf(
                            "role" to "technician",
                            "managerId" to managerId,
                            "stations" to selectedStations
                        )
                    ).await()

                // 4. Add technician to manager's technicians list
                // First check if manager document exists
                val managerDocRef = firestore.collection("managers").document(managerId)

                firestore.runTransaction { transaction ->
                    val managerDoc = transaction.get(managerDocRef)

                    if (!managerDoc.exists()) {
                        // Create new manager document with the technician
                        transaction.set(
                            managerDocRef,
                            mapOf(
                                "technicians" to listOf(userId)
                            )
                        )
                    } else {
                        // Update existing manager document
                        val currentTechnicians = managerDoc.get("technicians") as? List<String> ?: emptyList()
                        if (!currentTechnicians.contains(userId)) {
                            transaction.update(managerDocRef,
                                "technicians", FieldValue.arrayUnion(userId)
                            )
                        }
                    }
                }.await()

                _snackbarMessage.value = "Technician appointed successfully"
                onComplete(true, null)
            } catch (e: Exception) {
                _snackbarMessage.value = "Error appointing technician: ${e.message}"
                onComplete(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTechniciansForManager(managerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Use snapshot listener for real-time updates
                firestore.collection("managers").document(managerId)
                    .addSnapshotListener { managerSnapshot, error ->
                        if (error != null) {
                            _snackbarMessage.value = "Failed to load technicians: ${error.message}"
                            _technicians.value = emptyList()
                            return@addSnapshotListener
                        }

                        val technicianIds = managerSnapshot?.get("technicians") as? List<String> ?: emptyList()

                        if (technicianIds.isEmpty()) {
                            _technicians.value = emptyList()
                            return@addSnapshotListener
                        }

                        // Fetch technician profiles in real-time
                        firestore.collection("profiles")
                            .whereIn(FieldPath.documentId(), technicianIds)
                            .addSnapshotListener { profilesSnapshot, profilesError ->
                                if (profilesError != null) {
                                    _snackbarMessage.value = "Failed to load technician profiles: ${profilesError.message}"
                                    _technicians.value = emptyList()
                                    return@addSnapshotListener
                                }

                                _technicians.value = profilesSnapshot?.documents?.mapNotNull { doc ->
                                    doc.toObject(UserProfile::class.java)?.copy(uid = doc.id)
                                } ?: emptyList()
                            }
                    }
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to setup listeners: ${e.message}"
                _technicians.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeTechnician(technicianId: String, managerId: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Update technician's role to viewer
                firestore.collection("profiles").document(technicianId)
                    .update(
                        "role", "viewer",
                        "managerId", null,
                        "stations", emptyList<String>()
                    ).await()

                // 2. Remove technician from manager's list
                firestore.collection("managers").document(managerId)
                    .update("technicians", FieldValue.arrayRemove(technicianId))
                    .await()

                _snackbarMessage.value = "Technician removed successfully"
                onComplete(true, null)
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to remove technician: ${e.message}"
                onComplete(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}