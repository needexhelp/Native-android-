package com.example.ui.partners.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationToggleButton(
    isOn: Boolean,
    partnerType: PartnerType,
    onToggle: (isOn: Boolean, reason: String?) -> Unit
) {
    var showReasonDialog by remember { mutableStateOf(false) }

    // The visible button
    FilterChip(
        selected = isOn,
        onClick = {
            if (isOn) {
                // Currently ON → turn OFF
                if (!partnerType.requiresNotifReason) {
                    // Delivery Boy: no reason needed
                    onToggle(false, null)
                } else {
                    showReasonDialog = true
                }
            } else {
                // Currently OFF → turn ON immediately
                onToggle(true, null)
            }
        },
        label = {
            Text(
                text = if (isOn) "Notif ON" else "Notif OFF",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = if (isOn) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                contentDescription = if (isOn) "Notifications on" else "Notifications off",
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PanelColors.Teal,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
            containerColor = Color(0xFFE0DED8),
            labelColor = PanelColors.TextPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isOn,
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent
        )
    )

    // Reason dialog (shown only for non-delivery-boy when turning OFF)
    if (showReasonDialog) {
        OffReasonDialog(
            title = "Turn off notifications?",
            subtitle = "Select a reason before turning off notifications",
            reasons = partnerType.notifOffReasons,
            onConfirm = { reason ->
                showReasonDialog = false
                onToggle(false, reason)
            },
            onDismiss = {
                showReasonDialog = false
                // Do NOT toggle — user cancelled
            }
        )
    }
}
