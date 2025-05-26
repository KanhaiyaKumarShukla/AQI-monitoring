package com.example.sih.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih.model.BlogPost
import com.example.sih.repository.BlogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlogViewModel @Inject constructor(
    private val repository: BlogRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val user= auth.currentUser
    val blogs: StateFlow<List<BlogPost>> = repository.getAllBlogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userBlogs: StateFlow<List<BlogPost>> = run {
        val userId = auth.currentUser?.uid ?: ""
        repository.getUserBlogs(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: String? get() = _errorMessage.value


    fun createBlog(title: String, content: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.currentUser?.let { user ->
                    val blog = BlogPost(
                        title = title,
                        content = content,
                        authorId = user.uid,
                        authorName = user.displayName ?: "Anonymous"
                    )
                    val id = repository.createBlog(blog)
                    Log.d("Firebase", "Created blog with ID: $id")
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to create blog"
            }
            _isLoading.value = false
        }
    }


    fun updateBlog(blog: BlogPost, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateBlog(blog)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update blog"
            }
            _isLoading.value = false
        }
    }

    fun deleteBlog(blogId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteBlog(blogId)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete blog"
            }
            _isLoading.value = false
        }
    }
}
