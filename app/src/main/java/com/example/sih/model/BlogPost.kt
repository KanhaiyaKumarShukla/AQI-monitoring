package com.example.sih.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BlogPost(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val tags: List<String> = emptyList(),
    val featuredImageUrl: String? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "content" to content,
            "authorId" to authorId,
            "authorName" to authorName,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "tags" to tags,
            "featuredImageUrl" to featuredImageUrl
        )
    }
}

fun Timestamp.toDate(): Date = this.toDate()
fun Date.formatAsString(): String = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(this)