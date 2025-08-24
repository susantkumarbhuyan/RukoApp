package com.ruko.system.presentation.screens.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ruko.composeapp.generated.resources.Res
import ruko.composeapp.generated.resources.ic_enter_queue
import ruko.composeapp.generated.resources.ic_growth_chart
import ruko.composeapp.generated.resources.ic_live_tracking

// Data class to hold feature details, making the code more organized and scalable.
data class FeatureItem(
    val title: String,
    val description: String,
    val icon: DrawableResource
)

@Composable
fun FeatureScreen(onNext: () -> Unit) {
    val features = listOf(
        FeatureItem(
            title = "Live Queue Tracking",
            description = "Get real-time updates on your queue position and estimated wait time so you can manage your time better.",
            icon = Res.drawable.ic_live_tracking
        ),
        FeatureItem(
            title = "Instant Queue Joining",
            description = "Join queues for your favorite businesses directly from your phone. No more waiting in long lines.",
            icon = Res.drawable.ic_enter_queue
        ),
        FeatureItem(
            title = "For Store Owners: Better Experience",
            description = "Improve customer experience and grow your business with powerful analytics and user statistics.",
            icon = Res.drawable.ic_growth_chart
        )
    )

    val pagerState = rememberPagerState(pageCount = { features.size })

    // Auto-scroll coroutine (runs only once)
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // 3 seconds delay between scrolls
            val nextPage = (pagerState.currentPage + 1) % features.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(600, easing = FastOutSlowInEasing) // Smooth animation
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            FeaturePage(item = features[page])
        }

        PageIndicator(pageCount = features.size, currentPage = pagerState.currentPage)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 48.dp, end = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun FeaturePage(item: FeatureItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(item.icon),
            contentDescription = null,
            modifier = Modifier.size(150.dp) // Larger icon for more impact
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@Composable
fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { iteration ->
            val color =
                if (currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .size(10.dp)
                    .background(color)
            )
        }
    }
}