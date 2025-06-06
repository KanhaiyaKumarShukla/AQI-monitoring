package com.example.sih.repository

import com.example.sih.model.BlogPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestoreException
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BlogRepository {
    fun getAllBlogs(): Flow<List<BlogPost>>
    fun getUserBlogs(userId: String): Flow<List<BlogPost>>
    suspend fun createBlog(blog: BlogPost): String
    suspend fun updateBlog(blog: BlogPost): Result<Unit>
    suspend fun deleteBlog(blogId: String): Result<Unit>
}

class BlogRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BlogRepository {
    private val blogsCollection = firestore.collection("blogs")

    override fun getAllBlogs(): Flow<List<BlogPost>> = callbackFlow {
        val subscription = blogsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BlogRepository", "Error fetching blogs", error)
                    close(error)
                    return@addSnapshotListener
                }

                try {
                    val blogs = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(BlogPost::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("BlogRepository", "Error converting document ${doc.id}", e)
                            null
                        }
                    } ?: emptyList()
                    trySend(blogs)
                } catch (e: Exception) {
                    Log.e("BlogRepository", "Error processing blogs", e)
                    close(e)
                }
            }

        awaitClose { subscription.remove() }
    }

    override fun getUserBlogs(userId: String): Flow<List<BlogPost>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val subscription = blogsCollection
            .whereEqualTo("authorId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BlogRepository", "Error fetching user blogs", error)
                    close(error)
                    return@addSnapshotListener
                }

                try {
                    val blogs = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(BlogPost::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("BlogRepository", "Error converting document ${doc.id}", e)
                            null
                        }
                    } ?: emptyList()
                    trySend(blogs)
                } catch (e: Exception) {
                    Log.e("BlogRepository", "Error processing user blogs", e)
                    close(e)
                }
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun createBlog(blog: BlogPost): String = withContext(Dispatchers.IO) {
        try {
            // Validate blog data
            validateBlog(blog)

            // Create document reference
            val document = blogsCollection.document()
            
            // Create blog with ID and ensure all required fields are present
            val blogWithId = blog.copy(
                id = document.id,
                title = blog.title.trim(),
                content = blog.content.trim(),
                createdAt = blog.createdAt,
                updatedAt = blog.updatedAt
            )

            // Log the attempt
            Log.d("BlogRepository", "Attempting to create blog: ${blogWithId.title}")

            // Set the document data
            document.set(blogWithId.toMap()).await()

            // Log success
            Log.d("BlogRepository", "Successfully created blog with ID: ${document.id}")

            // Return the document ID
            document.id

        } catch (e: FirebaseFirestoreException) {
            Log.e("BlogRepository", "Firestore error creating blog", e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    throw BlogCreationException("Permission denied. Please check your authentication status.")
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    throw BlogCreationException("Network unavailable. Please check your connection.")
                else ->
                    throw BlogCreationException("Database error: ${e.message}")
            }
        } catch (e: IllegalArgumentException) {
            Log.e("BlogRepository", "Validation error", e)
            throw e
        } catch (e: Exception) {
            Log.e("BlogRepository", "Unexpected error creating blog", e)
            throw BlogCreationException("An unexpected error occurred: ${e.message}")
        }
    }

    private fun validateBlog(blog: BlogPost) {
        when {
            blog.title.isBlank() -> throw IllegalArgumentException("Title cannot be empty")
            blog.content.isBlank() -> throw IllegalArgumentException("Content cannot be empty")
            blog.authorId.isBlank() -> throw IllegalArgumentException("Author ID cannot be empty")
            blog.title.length > 100 -> throw IllegalArgumentException("Title cannot be longer than 100 characters")
            blog.content.length > 50000 -> throw IllegalArgumentException("Content cannot be longer than 50000 characters")
        }
    }

    override suspend fun updateBlog(blog: BlogPost): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            validateBlog(blog)
            blogsCollection.document(blog.id).set(blog.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BlogRepository", "Error updating blog", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteBlog(blogId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (blogId.isBlank()) {
                throw IllegalArgumentException("Blog ID cannot be empty")
            }
            blogsCollection.document(blogId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BlogRepository", "Error deleting blog", e)
            Result.failure(e)
        }
    }

    class BlogCreationException(message: String) : Exception(message)
}
