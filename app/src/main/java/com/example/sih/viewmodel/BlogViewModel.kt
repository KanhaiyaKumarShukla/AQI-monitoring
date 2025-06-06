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
import com.google.firebase.Timestamp
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

    val user = auth.currentUser
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
        if (title.isBlank()) {
            _errorMessage.value = "Title cannot be empty"
            return
        }

        if (content.isBlank()) {
            _errorMessage.value = "Content cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.currentUser?.let { user ->
                    val now = Timestamp.now()
                    val blog = BlogPost(
                        title = title.trim(),
                        content = content.trim(),
                        authorId = user.uid,
                        authorName = user.displayName ?: "Anonymous",
                        createdAt = now,
                        updatedAt = now,
                        tags = emptyList() // You can add tag support later
                    )
                    Log.d("BlogViewModel", "Creating blog: ${blog.title}")
                    val id = repository.createBlog(blog)
                    Log.d("BlogViewModel", "Blog created with ID: $id")
                    _errorMessage.value = null
                    onSuccess()
                } ?: run {
                    _errorMessage.value = "User not authenticated"
                }
            } catch (e: Exception) {
                Log.e("BlogViewModel", "Error creating blog", e)
                _errorMessage.value = e.message ?: "Failed to create blog"
            } finally {
                _isLoading.value = false
            }
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

    fun clearError() {
        _errorMessage.value = null
    }
}
