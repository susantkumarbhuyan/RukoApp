package com.ruko.system.presentation.screens.signup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruko.system.presentation.navigation.Screen
import org.jetbrains.compose.resources.painterResource
import ruko.composeapp.generated.resources.Res
import ruko.composeapp.generated.resources.icon_customer
import ruko.composeapp.generated.resources.icon_store_owner


@Composable
fun SignUpScreen(navigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp), // Add horizontal padding for a clean look
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose your account type",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp) // Increase bottom padding for better spacing
        )

        // Using the reusable card for the Customer option
        AccountTypeCard(
            title = "I'm a Customer",
            subtitle = "Join queues for your favorite businesses.",
            icon = painterResource(Res.drawable.icon_customer), // Use a dedicated icon
            onClick = { navigate(Screen.OnBoarding.SignUp.User) }
        )

        Spacer(modifier = Modifier.height(16.dp)) // Spacing between cards

        // Using the reusable card for the Store Owner option
        AccountTypeCard(
            title = "I'm a Store Owner",
            subtitle = "Create and manage your own queues.",
            icon = painterResource(Res.drawable.icon_store_owner), // Use a dedicated icon
            onClick = { navigate(Screen.OnBoarding.SignUp.Owner) }
        )
    }
}

/**
 * A reusable Composable for an outlined account type card.
 * @param title The main title of the card.
 * @param subtitle A descriptive text below the title.
 * @param icon The painter resource for the card's icon.
 * @param onClick The action to perform when the card is clicked.
 */
@Composable
fun AccountTypeCard(
    title: String,
    subtitle: String,
    icon: Painter,
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface) // Use surface for a clean background
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = icon,
                contentDescription = "$title icon",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(20.dp)) // Increase spacing for better visual separation
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}