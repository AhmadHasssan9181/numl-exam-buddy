package com.noobdev.numlexambuddy.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdev.numlexambuddy.model.DocumentStatus

/**
 * A composable that displays download progress for a document.
 *
 * @param status Current status of the document download
 * @param progress Download progress as a float between 0.0 and 1.0
 * @param fileName Name of the file being downloaded
 * @param onCancelClick Callback for when the cancel button is clicked
 */
@Composable
fun DownloadProgressIndicator(
    status: DocumentStatus,
    progress: Float,
    fileName: String,
    onCancelClick: () -> Unit
) {
    // Animate the progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress_animation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // File name with ellipsis for long names
            Text(
                text = fileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Display different icons based on download status
            when (status) {
                DocumentStatus.DOWNLOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onCancelClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Cancel download",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }                DocumentStatus.COMPLETE -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Download completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }                DocumentStatus.FAILED -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Download error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
                else -> { /* No icon for other states */ }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Show progress bar for downloading status
        if (status == DocumentStatus.DOWNLOADING) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Show percentage text
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )        } else if (status == DocumentStatus.FAILED) {
            Text(
                text = "Download failed. Tap to retry.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}
