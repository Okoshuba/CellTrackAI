package com.kubaice.celltrackai.presentation.elements

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

private const val TITLE_MAX_LINES = 1

@Composable
internal fun OneLineText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    Text(
        modifier = modifier,
        text = text,
        style = style,
        color = color,
        overflow = TextOverflow.Ellipsis,
        maxLines = TITLE_MAX_LINES
    )
}