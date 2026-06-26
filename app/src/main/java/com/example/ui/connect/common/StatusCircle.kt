package com.example.ui.connect.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusCircle(
    photoUrl: String?,
    name: String,
    hasUnviewed: Boolean,
    modifier: Modifier = Modifier,
    isAd: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .width(70.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(56.dp)
        ) {
            val ringColor = if (hasUnviewed) ConnectColors.Primary else Color(0xFF94A3B8)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, ringColor, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null && photoUrl.isNotBlank()) {
                    Text(
                        text = photoUrl, // Render emoji/avatar directly
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = if (name.isNotEmpty()) name.take(1).uppercase() else "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ConnectColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (isAd) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(ConnectColors.Amber, RoundedCornerShape(4.dp))
                        .border(1.dp, ConnectColors.AmberDark, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Ad",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = ConnectColors.AmberDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            fontSize = 11.sp,
            color = ConnectColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
