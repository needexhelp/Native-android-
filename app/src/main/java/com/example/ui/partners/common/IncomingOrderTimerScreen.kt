package com.example.ui.partners.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrowmixElevation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IncomingOrderTimerScreen(
    orderInfo: OrderInfo,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(30) }
    val progress by animateFloatAsState(
        targetValue = secondsLeft / 30f,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "timer_progress"
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
        // Auto decline at 0
        try {
            scope.launch { snackbarHostState.showSnackbar("Order timed out — auto declined") }
        } catch(e: Exception) {}
        delay(1500L)
        onDecline()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))

            // Title
            Text(
                text = incomingTitle(orderInfo.partnerType),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PanelColors.TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = orderInfo.orderId,
                fontSize = 13.sp,
                color = PanelColors.TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            // Countdown ring
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(88.dp),
                    color = if (secondsLeft > 10) PanelColors.Teal else PanelColors.RedDark,
                    trackColor = PanelColors.TealLight,
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = secondsLeft.toString(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (secondsLeft > 10) PanelColors.TealDark else PanelColors.RedDeep
                )
            }
            Text(
                text = "seconds to respond",
                fontSize = 12.sp,
                color = PanelColors.TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Order info card — content differs by partner type
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PanelColors.Card),
                border = BorderStroke(0.5.dp, PanelColors.Border),
                elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Low)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OrderInfoContent(orderInfo)
                }
            }

            Spacer(Modifier.weight(1f))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PanelColors.RedDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PanelColors.RedDark)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Decline", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Decline", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PanelColors.Teal)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Accept", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Accept", fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

// Helper: title per partner type
fun incomingTitle(type: PartnerType): String = when (type) {
    PartnerType.DELIVERY_BOY  -> "New Delivery Order!"
    PartnerType.TAXI          -> "New Ride Request!"
    PartnerType.AUTO          -> "New Auto Request!"
    PartnerType.AMBULANCE     -> "🚨 Emergency Request!"
    PartnerType.MEDICINE_SHOP -> "New Medicine Order!"
    PartnerType.RESTAURANT    -> "New Order!"
    PartnerType.HOSPITAL      -> "New Appointment Request!"
    PartnerType.BUS_AGENCY    -> "New Booking Request!"
}

@Composable
fun OrderInfoContent(info: OrderInfo) {
    when (info.partnerType) {

        PartnerType.DELIVERY_BOY -> {
            InfoRow("Customer", info.customerName)
            InfoRow("Phone", info.customerPhone)
            InfoRow("Pickup", info.pickupAddress)
            InfoRow("Drop", info.dropAddress)
            InfoRow("Distance", "${info.distanceKm} km")
            if (info.weightKg != null) InfoRow("Weight", "${info.weightKg} kg")
            InfoRow("Items", info.items.joinToString { "${it.name} × ${it.quantity}" })
            InfoRow("Total", "₹${info.totalAmount}")
            if (info.estimatedEarnings != null) InfoRow("Your earnings", "₹${info.estimatedEarnings}")
        }

        PartnerType.TAXI, PartnerType.AUTO -> {
            InfoRow("Customer", info.customerName)
            InfoRow("Phone", info.customerPhone)
            InfoRow("Pickup", info.pickupAddress)
            InfoRow("Drop", info.dropAddress)
            InfoRow("Distance", "${info.distanceKm} km")
            if (info.cabType != null) InfoRow("Type", info.cabType)
            InfoRow("Fare", "₹${info.totalAmount}")
        }

        PartnerType.AMBULANCE -> {
            // Emergency styling — red label
            InfoRow("Patient condition", info.patientCondition ?: "-", valueColor = PanelColors.RedDark)
            InfoRow("Priority", info.priority ?: "URGENT", valueColor = PanelColors.RedDark)
            InfoRow("Pickup", info.pickupAddress)
            InfoRow("Drop (Hospital)", info.dropAddress)
            InfoRow("Distance", "${info.distanceKm} km")
            InfoRow("Contact person", info.contactPersonName ?: "-")
            InfoRow("Contact phone", info.contactPersonPhone ?: "-")
        }

        PartnerType.MEDICINE_SHOP -> {
            InfoRow("Customer", info.customerName)
            InfoRow("Phone", info.customerPhone)
            InfoRow("Address", info.dropAddress)
            InfoRow("Distance", "${info.distanceKm} km")
            // Items
            info.items.forEach { item ->
                InfoRow(item.name, "${item.size ?: ""} × ${item.quantity}  ₹${item.unitPrice * item.quantity}")
            }
            if (!info.specialInstructions.isNullOrBlank())
                InfoRow("Instructions", info.specialInstructions, valueColor = PanelColors.AmberDark)
            InfoRow("Total", "₹${info.totalAmount}")
            InfoRow("Payment", "${info.paymentMode} — ${info.paymentStatus}")
        }

        PartnerType.RESTAURANT -> {
            InfoRow("Order type", if (info.pickupAddress.contains("Table")) "Dine-in" else "Delivery")
            if (!info.pickupAddress.contains("Table")) {
                InfoRow("Customer", info.customerName)
                InfoRow("Phone", info.customerPhone)
                InfoRow("Address", info.dropAddress)
            } else {
                InfoRow("Table", info.pickupAddress)
            }
            // Items with size
            info.items.forEach { item ->
                val label = "${item.name}${if (item.size != null) " (${item.size})" else ""}"
                InfoRow(label, "× ${item.quantity}  ₹${item.unitPrice * item.quantity}")
                if (!item.instruction.isNullOrBlank())
                    InfoRow("  ⚠ Note", item.instruction, valueColor = PanelColors.AmberDark)
            }
            InfoRow("Total", "₹${info.totalAmount}")
        }

        PartnerType.HOSPITAL -> {
            InfoRow("Patient", info.customerName)
            if (info.customerAge != null) InfoRow("Age / Gender", "${info.customerAge} / ${info.customerGender ?: "-"}")
            if (!info.customerBloodGroup.isNullOrBlank()) InfoRow("Blood group", info.customerBloodGroup)
            InfoRow("Phone", info.customerPhone)
            if (!info.customerEmail.isNullOrBlank()) InfoRow("Email", info.customerEmail)
            InfoRow("Appointment ID", info.appointmentId ?: "-")
            InfoRow("Doctor", "${info.doctorName} — ${info.doctorSpecialization}")
            InfoRow("Time slot", info.appointmentTimeSlot ?: "-")
            InfoRow("Reason", info.reasonForVisit ?: "-")
            InfoRow("Payment", "${info.paymentStatus} — ₹${info.totalAmount}")
            if (info.uploadedDocuments.isNotEmpty())
                InfoRow("Documents", info.uploadedDocuments.joinToString(", "))
        }

        PartnerType.BUS_AGENCY -> {
            InfoRow("Booking ID", info.bookingId ?: "-")
            InfoRow("Passenger", info.customerName)
            if (info.customerAge != null) InfoRow("Age / Gender", "${info.customerAge} / ${info.customerGender ?: "-"}")
            InfoRow("Phone", info.customerPhone)
            if (!info.customerEmail.isNullOrBlank()) InfoRow("Email", info.customerEmail)
            InfoRow("Route", "${info.pickupAddress} → ${info.dropAddress}")
            InfoRow("Bus", info.busNumber ?: "-")
            InfoRow("Departure", info.departureTime ?: "-")
            InfoRow("Arrival (est.)", info.arrivalTime ?: "-")
            InfoRow("Seat", "${info.seatNumber} — ${info.seatType}")
            InfoRow("Class", info.busClass ?: "-")
            InfoRow("Luggage", "${info.luggageCount ?: 0} bag(s)")
            InfoRow("Fare", "₹${info.totalAmount}")
            InfoRow("Payment", "${info.paymentMode} — ${info.paymentStatus}")
        }
    }
}

// Reusable info row
@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = PanelColors.TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = PanelColors.TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor, textAlign = TextAlign.End)
    }
    HorizontalDivider(color = PanelColors.BorderLight, thickness = 0.5.dp)
}
