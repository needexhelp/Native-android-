package com.example.ui.partners.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.*
import com.example.ui.partners.PartnerPasswordScreen
import com.example.ui.partners.PartnerRegistrationScreen
import com.example.ui.partners.common.IncomingOrderTimerScreen
import com.example.ui.partners.common.PartnerType
import com.example.ui.partners.common.buildSampleOrder

fun NavGraphBuilder.partnersNavGraph(navController: NavController) {

    // ── Entry ──────────────────────────────────────────────
    composable(PartnerRoutes.PARTNERS_HOME) {
        // PartnersHomeScreen defined below is the updated home version that navigates using NavController
        PartnersHomeScreen(
            onBack = { navController.popBackStack() },
            onPartnerSelected = { type ->
                navController.navigate(PartnerRoutes.passwordRoute(type))
            }
        )
    }

    // ── Auth ───────────────────────────────────────────────
    composable(
        route = PartnerRoutes.PARTNER_PASSWORD,
        arguments = listOf(navArgument("partnerType") { type = NavType.StringType })
    ) { backStackEntry ->
        val typeStr = backStackEntry.arguments?.getString("partnerType") ?: "DELIVERY_BOY"
        val partnerType = try {
            PartnerType.valueOf(typeStr)
        } catch (e: Exception) {
            PartnerType.DELIVERY_BOY
        }
        PartnerPasswordScreen(
            partnerType = partnerType,
            onPasswordSuccess = {
                val dest = panelRoute(partnerType)
                navController.navigate(dest) {
                    popUpTo(PartnerRoutes.passwordRoute(partnerType)) { inclusive = true }
                }
            },
            onRegistration = {
                navController.navigate(PartnerRoutes.registrationRoute(partnerType))
            },
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = PartnerRoutes.PARTNER_REGISTRATION,
        arguments = listOf(navArgument("partnerType") { type = NavType.StringType })
    ) { backStackEntry ->
        val typeStr = backStackEntry.arguments?.getString("partnerType") ?: "DELIVERY_BOY"
        val partnerType = try {
            PartnerType.valueOf(typeStr)
        } catch (e: Exception) {
            PartnerType.DELIVERY_BOY
        }
        PartnerRegistrationScreen(
            partnerType = partnerType,
            onRegistrationComplete = {
                val dest = panelRoute(partnerType)
                navController.navigate(dest) {
                    popUpTo(PartnerRoutes.PARTNERS_HOME) { inclusive = false }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    // ── Panels ─────────────────────────────────────────────
    composable(PartnerRoutes.DELIVERY_BOY_PANEL) {
        DeliveryBoyPanelScreen(
            onSignOut = {
                navController.navigate(PartnerRoutes.passwordRoute(PartnerType.DELIVERY_BOY)) {
                    popUpTo(PartnerRoutes.DELIVERY_BOY_PANEL) { inclusive = true }
                }
            }
        )
    }

    composable(PartnerRoutes.TAXI_PANEL) {
        TaxiPanelScreen(
            onSignOut = {
                navController.navigate(PartnerRoutes.passwordRoute(PartnerType.TAXI)) {
                    popUpTo(PartnerRoutes.TAXI_PANEL) { inclusive = true }
                }
            }
        )
    }

    composable(PartnerRoutes.AUTO_PANEL) {
        AutoPanelScreen(
            onSignOut = {
                navController.navigate(PartnerRoutes.passwordRoute(PartnerType.AUTO)) {
                    popUpTo(PartnerRoutes.AUTO_PANEL) { inclusive = true }
                }
            }
        )
    }

    composable(PartnerRoutes.AMBULANCE_PANEL) {
        AmbulancePanelScreen(
            onSignOut = {
                navController.navigate(PartnerRoutes.passwordRoute(PartnerType.AMBULANCE)) {
                    popUpTo(PartnerRoutes.AMBULANCE_PANEL) { inclusive = true }
                }
            }
        )
    }

    composable(PartnerRoutes.MEDICINE_PANEL) {
        MedicineShopPanelScreen(
            onSignOut = {
                navController.navigate(PartnerRoutes.passwordRoute(PartnerType.MEDICINE_SHOP)) {
                    popUpTo(PartnerRoutes.MEDICINE_PANEL) { inclusive = true }
                }
            }
        )
    }

    composable(PartnerRoutes.RESTAURANT_PANEL) {
        RestaurantPanelScreen(
            onSignOut = {
                navController.navigate(PartnerRoutes.passwordRoute(PartnerType.RESTAURANT)) {
                    popUpTo(PartnerRoutes.RESTAURANT_PANEL) { inclusive = true }
                }
            }
        )
    }

    composable(PartnerRoutes.HOSPITAL_PANEL) {
        HospitalPanelScreen(
            onSignOut = {
                navController.navigate(PartnerRoutes.passwordRoute(PartnerType.HOSPITAL)) {
                    popUpTo(PartnerRoutes.HOSPITAL_PANEL) { inclusive = true }
                }
            }
        )
    }

    composable(PartnerRoutes.BUS_PANEL) {
        BusAgencyPanelScreen(
            onSignOut = {
                navController.navigate(PartnerRoutes.passwordRoute(PartnerType.BUS_AGENCY)) {
                    popUpTo(PartnerRoutes.BUS_PANEL) { inclusive = true }
                }
            }
        )
    }

    // ── Incoming Order Timer (shared) ──────────────────────
    composable(
        route = PartnerRoutes.INCOMING_ORDER,
        arguments = listOf(navArgument("partnerType") { type = NavType.StringType })
    ) { backStackEntry ->
        val typeStr = backStackEntry.arguments?.getString("partnerType") ?: "DELIVERY_BOY"
        val partnerType = try {
            PartnerType.valueOf(typeStr)
        } catch (e: Exception) {
            PartnerType.DELIVERY_BOY
        }
        IncomingOrderTimerScreen(
            orderInfo = buildSampleOrder(partnerType),
            onAccept = {
                val destination = activeOrderRoute(partnerType)
                navController.navigate(destination) {
                    popUpTo(PartnerRoutes.incomingOrderRoute(partnerType)) { inclusive = true }
                }
            },
            onDecline = {
                navController.popBackStack()
            }
        )
    }

    // ── Active Order / Ride Screens ────────────────────────
    composable(PartnerRoutes.DELIVERY_ACTIVE) {
        DeliveryActiveOrderScreen(
            order = DeliveryOrder(
                id = "ORD-4421",
                customerName = "Rajesh Kumar",
                phone = "+91 98XXX XXXXX",
                distance = "3.2 km",
                items = "Grocery Package x1",
                status = "Pending pickup",
                statusColor = androidx.compose.ui.graphics.Color(0xFFEA580C),
                pickupAddress = "Sector 62 Store Warehouse, Noida",
                dropAddress = "Apt 402, Block C, Green Heights, Noida",
                estimatedEarnings = "₹48",
                totalAmount = "₹320",
                weight = "1.4 kg",
                itemsList = listOf("Fresh Tomatoes (1kg)" to "₹60", "Aashirvaad Atta (5kg)" to "₹210")
            ),
            onDeliverSuccess = { navController.popBackStack() },
            onBack = { navController.popBackStack() }
        )
    }

    composable(PartnerRoutes.TAXI_RIDE_START) {
        TaxiRideStartScreen(
            ride = TaxiRide(
                id = "TX-8822",
                customerName = "Amit Sharma",
                phone = "+91 98XXX XXXXX",
                pickup = "Airport Terminal 3, Delhi",
                drop = "DLF Cyber City, Phase 2, Gurugram",
                distance = "18.5 km",
                fare = "₹420",
                cabType = "Sedan",
                status = "Ongoing",
                statusColor = androidx.compose.ui.graphics.Color(0xFFCA8A04)
            ),
            onBack = { navController.popBackStack() },
            onRideComplete = { navController.popBackStack() }
        )
    }

    composable(PartnerRoutes.AUTO_RIDE_START) {
        AutoRideStartScreen(
            ride = AutoRide(
                id = "AU-3210",
                customerName = "Vikram Singh",
                phone = "+91 99XXX XXXXX",
                pickup = "Sector 18 Metro Station",
                drop = "Pacific Mall Entrance",
                distance = "4.2 km",
                fare = "₹70",
                cabType = "Auto",
                status = "Ongoing",
                statusColor = androidx.compose.ui.graphics.Color(0xFF047857)
            ),
            onBack = { navController.popBackStack() },
            onRideComplete = { navController.popBackStack() }
        )
    }

    composable(PartnerRoutes.AMBULANCE_ACTIVE) {
        AmbulanceActiveScreen(
            emergency = AmbulanceEmergency(
                id = "AMB-4421",
                patientName = "Suresh Sen",
                phone = "+91 88XXX XXXXX",
                pickup = "Block C, Sector 45, Noida",
                drop = "Apollo Hospital Emergency, Noida",
                distance = "5.2 km",
                priority = "CRITICAL",
                patientCondition = "Severe breathing difficulty",
                status = "En-route to Patient",
                statusColor = androidx.compose.ui.graphics.Color(0xFFE11D48)
            ),
            onBack = { navController.popBackStack() },
            onEmergencyComplete = { navController.popBackStack() }
        )
    }

    composable(PartnerRoutes.MEDICINE_ORDER_ACTIVE) {
        MedicineOrderActiveScreen(
            order = MedicineOrder(
                id = "MED-9921",
                customerName = "Kalyani Devi",
                phone = "+91 95XXX XXXXX",
                medicines = "Amoxicillin x2, Paracetamol x1",
                instruction = "Fragile medicine handle with care",
                total = 450,
                status = "Packing Medicines",
                address = "Flat 102, Block 4, Palm Springs, Noida",
                distance = "2.4 km",
                deliveryType = "Home Delivery",
                paymentType = "UPI",
                statusColor = androidx.compose.ui.graphics.Color(0xFF16A34A)
            ),
            onBack = { navController.popBackStack() },
            onOrderComplete = { navController.popBackStack() }
        )
    }

    composable(PartnerRoutes.RESTAURANT_ORDER_ACTIVE) {
        RestaurantOrderActiveScreen(
            order = RestaurantOrder(
                id = "ORD-881",
                orderType = "Dine-in",
                destination = "Table 4",
                items = listOf(
                    RestaurantOrderItem("Chicken Biryani", "Full", 1, 220),
                    RestaurantOrderItem("Paneer Biryani", "Half", 1, 140)
                ),
                specialInstruction = "No onion in Paneer Biryani",
                totalPrice = 420,
                timeAgo = "12 min ago",
                status = "Preparing"
            ),
            onBack = { navController.popBackStack() },
            onStatusTransition = { navController.popBackStack() }
        )
    }

    composable(PartnerRoutes.HOSPITAL_APPOINTMENT) {
        HospitalAppointmentDetailBottomSheet(
            appointment = HospitalAppointment(
                id = "APP-4421",
                patientName = "Rahul Verma",
                age = 34,
                gender = "Male",
                phone = "+91 91XXX XXXXX",
                doctorId = "DOC-104",
                timeSlot = "11:30 AM",
                status = "Confirmed",
                reason = "Chronic Back Pain",
                paymentStatus = "Paid"
            ),
            doctor = HospitalDoctor(
                id = "DOC-104",
                name = "Dr. Shashi Shekhar",
                specialization = "Orthopedic Surgeon",
                isOnline = true,
                statusText = "Available",
                avatarColor = androidx.compose.ui.graphics.Color(0xFFDC2626),
                initials = "SS"
            ),
            onDismiss = { navController.popBackStack() },
            onStatusChange = { navController.popBackStack() }
        )
    }

    composable(PartnerRoutes.BUS_ROUTE_DETAIL) {
        BusRouteDetailScreen(
            route = BusRoute(
                id = "RT-4421",
                name = "Pune Mumbai",
                busNumber = "MH-12 AB 4421",
                departureTime = "08:30 AM, Jun 13",
                seatCount = "32 booked/48 total",
                seatsBookedCount = 32,
                seatsTotalCount = 48,
                bookingIdsCount = 13,
                status = "On Time"
            ),
            passengerList = listOf(
                BusPassenger("Seat 4A", "Vikram Kelkar", "28, Male", "9876543210", "Paid"),
                BusPassenger("Seat 5B", "Sunita Kelkar", "26, Female", "9876543211", "Paid")
            ),
            onBack = { navController.popBackStack() },
            onUpdateStatus = { navController.popBackStack() }
        )
    }
}

// Helper to resolve route name from partner type
fun panelRoute(type: PartnerType): String {
    return when (type) {
        PartnerType.DELIVERY_BOY    -> PartnerRoutes.DELIVERY_BOY_PANEL
        PartnerType.TAXI            -> PartnerRoutes.TAXI_PANEL
        PartnerType.AUTO            -> PartnerRoutes.AUTO_PANEL
        PartnerType.AMBULANCE       -> PartnerRoutes.AMBULANCE_PANEL
        PartnerType.MEDICINE_SHOP   -> PartnerRoutes.MEDICINE_PANEL
        PartnerType.RESTAURANT      -> PartnerRoutes.RESTAURANT_PANEL
        PartnerType.HOSPITAL        -> PartnerRoutes.HOSPITAL_PANEL
        PartnerType.BUS_AGENCY      -> PartnerRoutes.BUS_PANEL
    }
}

fun activeOrderRoute(type: PartnerType): String {
    return when (type) {
        PartnerType.DELIVERY_BOY    -> PartnerRoutes.DELIVERY_ACTIVE
        PartnerType.TAXI            -> PartnerRoutes.TAXI_RIDE_START
        PartnerType.AUTO            -> PartnerRoutes.AUTO_RIDE_START
        PartnerType.AMBULANCE       -> PartnerRoutes.AMBULANCE_ACTIVE
        PartnerType.MEDICINE_SHOP   -> PartnerRoutes.MEDICINE_ORDER_ACTIVE
        PartnerType.RESTAURANT      -> PartnerRoutes.RESTAURANT_ORDER_ACTIVE
        PartnerType.HOSPITAL        -> PartnerRoutes.HOSPITAL_APPOINTMENT
        PartnerType.BUS_AGENCY      -> PartnerRoutes.BUS_ROUTE_DETAIL
    }
}
