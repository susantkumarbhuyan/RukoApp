package com.ruko.system.presentation.review

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.Review
import com.google.api.services.androidpublisher.model.ReviewsReplyRequest
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

@Composable
fun ReviewReplyScreen(context: Context) {
    val packageName = "com.odiacalander"
    val reviews = remember { mutableStateListOf<Review>() }
    val scope = rememberCoroutineScope()
    var selectedReview by remember { mutableStateOf<Review?>(null) }
    var replyText by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Reply to Google Play Reviews", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                try {
                    val publisher = getPublisher(context)
                    val response = publisher.reviews().list(packageName).execute()
                    val newReviews = response.reviews ?: emptyList()
                    reviews.clear()
                    reviews.addAll(newReviews)
                    resultMessage = "Loaded ${newReviews.size} reviews."
                } catch (e: Exception) {
                    resultMessage = "Failed to load reviews: ${e.localizedMessage}"
                    Log.e("ReviewReply", "Load error", e)
                }
            }
        }) {
            Text("Load Reviews")
        }

        Spacer(Modifier.height(12.dp))

        reviews.forEach { review ->
            ReviewItem(review = review, onSelect = {
                selectedReview = review
                replyText = ""
                resultMessage = ""
            })
        }

        selectedReview?.let { review ->
            Spacer(Modifier.height(16.dp))
            Text("Reply to Review ID: ${review.reviewId}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                label = { Text("Your reply") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                scope.launch {
                    try {
                        val publisher = getPublisher(context)
                        val reply = ReviewsReplyRequest().setReplyText(replyText)
                        publisher.reviews()
                            .reply(packageName, review.reviewId, reply)
                            .execute()
                        resultMessage = "Reply sent!"
                    } catch (e: Exception) {
                        resultMessage = "Reply failed: ${e.localizedMessage}"
                        Log.e("ReviewReply", "Reply error", e)
                    }
                }
            }) {
                Text("Send Reply")
            }
        }

        if (resultMessage.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                resultMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ReviewItem(review: Review, onSelect: () -> Unit) {
    val comment = review.comments?.firstOrNull()?.userComment?.text ?: "(No Comment)"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onSelect
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = comment)
            review.comments?.firstOrNull()?.userComment?.starRating?.let {
                Text("⭐ Rating: $it", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun getPublisher(context: Context): AndroidPublisher {
    val keyStream: InputStream = context.assets.open("key.json")
    val credentials = GoogleCredentials.fromStream(keyStream)
        .createScoped(listOf("https://www.googleapis.com/auth/androidpublisher"))

    return AndroidPublisher.Builder(
        com.google.api.client.http.javanet.NetHttpTransport(),
        com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
        HttpCredentialsAdapter(credentials)
    ).setApplicationName("PlayReviewApp").build()
}
