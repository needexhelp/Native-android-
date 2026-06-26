package com.example.ui.connect.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CommentSection(
    targetType: String,    // "news_article" | "status_post"
    targetId: String,
    modifier: Modifier = Modifier
) {
    com.example.ui.CommentSection(
        targetType = targetType,
        targetId = targetId,
        modifier = modifier
    )
}
