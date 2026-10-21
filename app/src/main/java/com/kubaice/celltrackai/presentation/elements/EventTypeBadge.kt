package com.kubaice.celltrackai.presentation.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.kubaice.celltrackai.R
import com.kubaice.celltrackai.domain.model.EventType

@Composable
fun EventTypeIcon(
    type: EventType,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.avatar_size))
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getEventTypeImageVector(type),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
        )
    }
}

fun getEventTypeImageVector(type: EventType): ImageVector {
    return when (type) {
        EventType.OBSERVATION -> Icons.Default.Science
        EventType.MEDIA_CHANGE -> Icons.Default.Science
        EventType.PASSAGE -> Icons.Default.Add
        EventType.FREEZING -> Icons.Default.Archive
        EventType.THAWING -> Icons.Default.Unarchive
        EventType.OTHER -> Icons.Default.Science
    }
}
