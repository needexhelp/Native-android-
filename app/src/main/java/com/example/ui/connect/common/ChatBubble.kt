package com.example.ui.connect.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ReadStatus { SENT, DELIVERED, READ }

@Composable
fun ChatBubble(
    message: String,
    isSentByMe: Boolean,
    senderName: String? = null,   // shown only in group chats, only on received messages
    timestamp: String,
    readStatus: ReadStatus = ReadStatus.SENT,  // SENT | DELIVERED | READ
    replyPreview: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
        val bubbleBg = if (isSentByMe) ConnectColors.BubbleSent else ConnectColors.BubbleReceived
        val bubbleShape = if (isSentByMe) {
            RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
        } else {
            RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)
        }

        Box(
            modifier = Modifier
                .align(alignment)
                .widthIn(max = 280.dp)
                .background(bubbleBg, bubbleShape)
                .border(0.5.dp, ConnectColors.Border.copy(alpha = 0.5f), bubbleShape)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column {
                if (!isSentByMe && senderName != null) {
                    Text(
                        text = senderName,
                        fontWeight = FontWeight.Bold,
                        color = ConnectColors.PrimaryDark,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (replyPreview != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                            .height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(ConnectColors.Primary, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                        )
                        Text(
                            text = replyPreview,
                            fontSize = 11.sp,
                            color = ConnectColors.TextSecondary,
                            maxLines = 2,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                Text(
                    text = message,
                    color = ConnectColors.TextPrimary,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timestamp,
                        fontSize = 10.sp,
                        color = ConnectColors.TextSecondary
                    )
                    if (isSentByMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val (icon, color) = when (readStatus) {
                            ReadStatus.SENT -> "✓" to ConnectColors.TextSecondary
                            ReadStatus.DELIVERED -> "✓✓" to ConnectColors.TextSecondary
                            ReadStatus.READ -> "✓✓" to ConnectColors.Primary
                        }
                        Text(
                            text = icon,
                            fontSize = 10.sp,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
