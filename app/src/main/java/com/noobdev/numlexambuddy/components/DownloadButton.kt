package com.noobdev.numlexambuddy.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * A composable for a download button with different states.
 *
 * @param onClick Action to perform when the button is clicked
 * @param isLoading Whether the button should show a loading state
 * @param enabled Whether the button is enabled
 * @param text Text to display on the button
 */
@Composable
fun DownloadButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    text: String = "Download"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier.animateContentSize(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    strokeCap = StrokeCap.Round,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(text = if (isLoading) "Downloading..." else text)
        }
    }
}
