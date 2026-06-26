package com.example.ui.partners.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OffReasonDialog(
    title: String = "Going offline?",
    subtitle: String = "Select a reason so customers are informed",
    reasons: List<String>,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var customReason by remember { mutableStateOf("") }

    // Confirm is enabled when a chip is selected OR custom field is non-empty
    val canConfirm = selectedReason != null || customReason.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelColors.Card,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 13.sp, color = PanelColors.TextSecondary)
            }
        },
        text = {
            Column {
                // Reason chips — single select
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All reasons except "Other" as chips
                    reasons.filter { it != "Other" }.forEach { reason ->
                        FilterChip(
                            selected = selectedReason == reason,
                            onClick = {
                                selectedReason = if (selectedReason == reason) null else reason
                                if (selectedReason != null) customReason = ""
                            },
                            label = { Text(reason, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PanelColors.TealLight,
                                selectedLabelColor = PanelColors.TealDark
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Custom / Other reason field
                OutlinedTextField(
                    value = customReason,
                    onValueChange = {
                        customReason = it
                        if (it.isNotBlank()) selectedReason = null
                    },
                    label = { Text("Other reason…", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PanelColors.Teal
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = selectedReason ?: customReason.trim()
                    onConfirm(finalReason)
                },
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PanelColors.Teal,
                    disabledContainerColor = PanelColors.TextDisabled
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, PanelColors.Border)
            ) {
                Text("Cancel", color = PanelColors.TextPrimary)
            }
        }
    )
}
