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
                    close(error)
                    return@addSnapshotListener
                }

                val blogs = snapshot?.toObjects(BlogPost::class.java) ?: emptyList()
                trySend(blogs)
            }

        awaitClose { subscription.remove() }
    }

    override fun getUserBlogs(userId: String): Flow<List<BlogPost>> = callbackFlow {
        val subscription = blogsCollection
            .whereEqualTo("authorId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val blogs = snapshot?.toObjects(BlogPost::class.java) ?: emptyList()
                trySend(blogs)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun createBlog(blog: BlogPost): String {
        return try {
            // 1. Log the input
            Log.d("Firebase", "Attempting to create blog: ${blog.title}")

            // 2. Create document reference
            val document = firestore.collection("blogs").document()

            // 3. Create blog with ID
            val blogWithId = blog.copy(id = document.id)
            Log.d("Firebase", "Blog data to save: $blogWithId")

            // 4. Attempt to save
            document.set(blogWithId).await()

            // 5. Log success
            Log.i("Firebase", "Blog created successfully with ID: ${document.id}")

            // 6. Return ID
            document.id
        } catch (e: FirebaseFirestoreException) {
            // Handle Firestore-specific errors
            Log.e("Firebase", "Firestore error creating blog", e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    throw BlogCreationException("Permission denied. Check Firestore security rules.")
                }
                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    throw BlogCreationException("Network unavailable. Please check your connection.")
                }
                else -> {
                    throw BlogCreationException("Firestore error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            // Handle other exceptions
            Log.e("Firebase", "General error creating blog", e)
            throw BlogCreationException("Failed to create blog: ${e.message}")
        }
    }

    // Custom exception class
    class BlogCreationException(message: String) : Exception(message)

    override suspend fun updateBlog(blog: BlogPost): Result<Unit> = try {
        blogsCollection.document(blog.id).set(blog).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteBlog(blogId: String): Result<Unit> = try {
        blogsCollection.document(blogId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
