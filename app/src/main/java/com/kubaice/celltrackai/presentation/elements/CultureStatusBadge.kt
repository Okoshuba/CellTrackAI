package com.kubaice.celltrackai.presentation.elements

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kubaice.celltrackai.R
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.presentation.theme.StatusActiveContainerDark
import com.kubaice.celltrackai.presentation.theme.StatusActiveContainerLight
import com.kubaice.celltrackai.presentation.theme.StatusActiveDark
import com.kubaice.celltrackai.presentation.theme.StatusActiveLight

@Composable
fun CultureStatusBadge(
    status: CultureStatus,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    horizontalPadding: Dp = dimensionResource(R.dimen.padding_6),
    verticalPadding: Dp = dimensionResource(R.dimen.padding_extra_small),
) {
    val isDark = isSystemInDarkTheme()
    val isActive = status == CultureStatus.ACTIVE

    val badgeBg = if (isActive) {
        if (isDark) StatusActiveContainerDark else StatusActiveContainerLight
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val badgeFg = if (isActive) {
        if (isDark) StatusActiveDark else StatusActiveLight
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    CultureStatusBadge(
        statusText = status.name,
        containerColor = badgeBg,
        contentColor = badgeFg,
        modifier = modifier,
        shape = shape,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding
    )
}

@Composable
fun CultureStatusBadge(
    statusText: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    horizontalPadding: Dp = dimensionResource(R.dimen.padding_6),
    verticalPadding: Dp = dimensionResource(R.dimen.padding_extra_small),
) {
    Surface(
        color = containerColor,
        shape = shape,
        modifier = modifier
    ) {
        OneLineText(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
        )
    }
}
