package com.example.ui.partners.common

enum class PartnerType(
    val displayName: String,
    val codePrefix: String,
    val requiresOffReason: Boolean,          // false only for DELIVERY_BOY
    val requiresNotifReason: Boolean,        // false only for DELIVERY_BOY
    val requiresRideCode: Boolean,           // true only for TAXI and AUTO
    val offReasons: List<String>,
    val notifOffReasons: List<String>
) {
    DELIVERY_BOY(
        displayName = "Delivery Boy",
        codePrefix = "DB-",
        requiresOffReason = false,
        requiresNotifReason = false,
        requiresRideCode = false,
        offReasons = emptyList(),
        notifOffReasons = emptyList()
    ),
    MEDICINE_SHOP(
        displayName = "Medicine Shop",
        codePrefix = "MED-",
        requiresOffReason = true,
        requiresNotifReason = true,
        requiresRideCode = false,
        offReasons = listOf("Break time", "Stock check in progress", "Closing soon", "Public holiday", "Other"),
        notifOffReasons = listOf("Break time", "Stock check in progress", "Closing soon", "Public holiday", "Other")
    ),
    RESTAURANT(
        displayName = "Restaurant",
        codePrefix = "REST-",
        requiresOffReason = true,
        requiresNotifReason = true,
        requiresRideCode = false,
        offReasons = listOf("Kitchen busy", "Break time", "Closed today", "Staff shortage", "Other"),
        notifOffReasons = listOf("Kitchen busy", "Break time", "Closed today", "Staff shortage", "Other")
    ),
    HOSPITAL(
        displayName = "Hospital / Doctor Panel",
        codePrefix = "HOSP-",
        requiresOffReason = true,
        requiresNotifReason = true,
        requiresRideCode = false,
        offReasons = listOf("All doctors busy", "Emergency cases only", "Closed today", "Equipment maintenance", "Other"),
        notifOffReasons = listOf("All doctors busy", "Emergency cases only", "Closed today", "Equipment maintenance", "Other")
    ),
    BUS_AGENCY(
        displayName = "Bus Agency",
        codePrefix = "BUS-",
        requiresOffReason = true,
        requiresNotifReason = true,
        requiresRideCode = false,
        offReasons = listOf("No bus available", "Route closed", "Vehicle maintenance", "Public holiday", "Other"),
        notifOffReasons = listOf("No bus available", "Route closed", "Vehicle maintenance", "Public holiday", "Other")
    ),
    TAXI(
        displayName = "Taxi",
        codePrefix = "TAXI-",
        requiresOffReason = true,
        requiresNotifReason = true,
        requiresRideCode = true,
        offReasons = listOf("No driver available", "Break time", "Vehicle service", "Area restricted", "Other"),
        notifOffReasons = listOf("No driver available", "Break time", "Vehicle service", "Area restricted", "Other")
    ),
    AUTO(
        displayName = "Auto",
        codePrefix = "AUTO-",
        requiresOffReason = true,
        requiresNotifReason = true,
        requiresRideCode = true,
        offReasons = listOf("No auto available", "Break time", "Vehicle service", "Area restricted", "Other"),
        notifOffReasons = listOf("No auto available", "Break time", "Vehicle service", "Area restricted", "Other")
    ),
    AMBULANCE(
        displayName = "Ambulance",
        codePrefix = "AMB-",
        requiresOffReason = true,
        requiresNotifReason = true,
        requiresRideCode = false,
        offReasons = listOf("No ambulance available", "All units busy", "Maintenance", "Shift end", "Other"),
        notifOffReasons = listOf("No ambulance available", "All units busy", "Maintenance", "Shift end", "Other")
    )
}
