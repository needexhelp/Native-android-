package com.example.ui.partners.common

data class OrderInfo(
    val orderId: String,
    val partnerType: PartnerType,

    // Customer / Passenger info
    val customerName: String,
    val customerPhone: String,          // store masked: "+91 98XXX XXXXX"
    val customerAge: Int? = null,
    val customerGender: String? = null,
    val customerBloodGroup: String? = null,
    val customerEmail: String? = null,

    // Location
    val pickupAddress: String,
    val dropAddress: String,
    val distanceKm: Double,

    // Order specifics
    val items: List<OrderItem> = emptyList(),       // for delivery / restaurant / medicine
    val specialInstructions: String? = null,
    val totalAmount: Double,
    val paymentMode: String = "Cash",               // "Cash" / "Online" / "UPI"
    val paymentStatus: String = "Paid",             // "Paid" / "Pending" / "COD"

    // Ride specifics (Taxi / Auto)
    val fareEstimate: Double? = null,
    val cabType: String? = null,                    // "Standard Taxi" / "Auto Rickshaw"
    val rideCode: String? = null,                   // 6-digit code for Taxi/Auto

    // Bus specifics
    val bookingId: String? = null,
    val busNumber: String? = null,
    val seatNumber: String? = null,
    val seatType: String? = null,                   // "Window" / "Aisle"
    val busClass: String? = null,                   // "Sleeper" / "Semi-Sleeper" / "Seater"
    val departureTime: String? = null,
    val arrivalTime: String? = null,
    val luggageCount: Int? = null,

    // Hospital specifics
    val appointmentId: String? = null,
    val doctorName: String? = null,
    val doctorSpecialization: String? = null,
    val appointmentTimeSlot: String? = null,
    val reasonForVisit: String? = null,
    val insuranceInfo: String? = null,
    val uploadedDocuments: List<String> = emptyList(),

    // Ambulance specifics
    val patientCondition: String? = null,
    val priority: String? = null,                   // "URGENT" / "HIGH" / "NORMAL"
    val contactPersonName: String? = null,
    val contactPersonPhone: String? = null,

    // Weight (delivery)
    val weightKg: Double? = null,

    // Meta
    val estimatedEarnings: Double? = null,
    val orderTime: String = ""
)

data class OrderItem(
    val name: String,
    val size: String? = null,           // "Full" / "Half" / "Large" / "Regular" / "500mg" etc.
    val quantity: Int,
    val unitPrice: Double,
    val instruction: String? = null
)

fun buildSampleOrder(type: PartnerType): OrderInfo {
    return when (type) {
        PartnerType.DELIVERY_BOY -> OrderInfo(
            orderId = "DB-1234",
            partnerType = type,
            customerName = "Rajesh Kumar",
            customerPhone = "+91 98XXX XXXXX",
            pickupAddress = "Sector 62 Store Warehouse, Noida",
            dropAddress = "Apt 402, Block C, Green Heights, Noida",
            distanceKm = 3.2,
            items = listOf(OrderItem(name = "Grocery Package", quantity = 1, unitPrice = 320.0)),
            totalAmount = 320.0,
            estimatedEarnings = 48.0,
            weightKg = 1.4
        )
        PartnerType.TAXI -> OrderInfo(
            orderId = "TAXI-5521",
            partnerType = type,
            customerName = "Anjali Sharma",
            customerPhone = "+91 98XXX XXXXX",
            pickupAddress = "Cyber City Wave 1, Gurugram",
            dropAddress = "Golf Course Road, DLF Phase 5, Gurugram",
            distanceKm = 6.4,
            totalAmount = 180.0,
            cabType = "Standard Taxi",
            rideCode = "481023",
            estimatedEarnings = 150.0
        )
        PartnerType.AUTO -> OrderInfo(
            orderId = "AUTO-9912",
            partnerType = type,
            customerName = "Amit Patel",
            customerPhone = "+91 98XXX XXXXX",
            pickupAddress = "Metro Station Gateway, Delhi",
            dropAddress = "Connaught Place Block G, Delhi",
            distanceKm = 2.8,
            totalAmount = 50.0,
            cabType = "Auto Rickshaw",
            rideCode = "109382",
            estimatedEarnings = 40.0
        )
        PartnerType.AMBULANCE -> OrderInfo(
            orderId = "AMB-2091",
            partnerType = type,
            customerName = "Ravi Shankar",
            customerPhone = "+91 98XXX XXXXX",
            patientCondition = "Severe chest pain, needs cardiac support",
            priority = "URGENT",
            pickupAddress = "House 102, Pocket B, Shalimar Bagh, Delhi",
            dropAddress = "Fortis Hospital Emergency Wing, Shalimar Bagh",
            distanceKm = 4.5,
            totalAmount = 1200.0,
            contactPersonName = "Neha Shankar (Daughter)",
            contactPersonPhone = "+91 98XXX XXXXX",
            estimatedEarnings = 1000.0
        )
        PartnerType.MEDICINE_SHOP -> OrderInfo(
            orderId = "MED-0091",
            partnerType = type,
            customerName = "Sita Devi",
            customerPhone = "+91 98XXX XXXXX",
            pickupAddress = "City Care Pharmacy Main, Delhi",
            dropAddress = "Flat 12A, Tower 3, Royal Residency, Delhi",
            distanceKm = 1.5,
            items = listOf(
                OrderItem(name = "Paracetamol 650mg", quantity = 2, unitPrice = 30.0),
                OrderItem(name = "Amoxicillin 500mg", quantity = 1, unitPrice = 120.0)
            ),
            specialInstructions = "Keep medicine refrigeration if possible",
            totalAmount = 180.0,
            estimatedEarnings = 150.0
        )
        PartnerType.RESTAURANT -> OrderInfo(
            orderId = "REST-8812",
            partnerType = type,
            customerName = "Vikram Singh",
            customerPhone = "+91 98XXX XXXXX",
            pickupAddress = "Table 4 (Dine-in)",
            dropAddress = "N/A - Dine-in Table 4",
            distanceKm = 0.0,
            items = listOf(
                OrderItem(name = "Paneer Butter Masala", size = "Full", quantity = 1, unitPrice = 280.0),
                OrderItem(name = "Butter Naan", size = "Regular", quantity = 3, unitPrice = 40.0)
            ),
            totalAmount = 400.0,
            estimatedEarnings = 380.0
        )
        PartnerType.HOSPITAL -> OrderInfo(
            orderId = "HOSP-4421",
            partnerType = type,
            customerName = "Karan Malhotra",
            customerAge = 42,
            customerGender = "Male",
            customerBloodGroup = "O+",
            customerPhone = "+91 98XXX XXXXX",
            pickupAddress = "Inpatient OPD Desk",
            dropAddress = "Consultation Room 102",
            distanceKm = 0.0,
            appointmentId = "APP-44910",
            doctorName = "Dr. Shashi Shekhar",
            doctorSpecialization = "Cardiologist",
            appointmentTimeSlot = "10:30 AM - 11:00 AM",
            reasonForVisit = "Routine heart checkup",
            totalAmount = 500.0,
            paymentStatus = "Paid"
        )
        PartnerType.BUS_AGENCY -> OrderInfo(
            orderId = "BUS-7712",
            partnerType = type,
            customerName = "Deepak Verma",
            customerAge = 29,
            customerGender = "Male",
            customerPhone = "+91 98XXX XXXXX",
            pickupAddress = "Bus Stand Gateway, Pune",
            dropAddress = "Koyambedu Bus Terminus, Chennai",
            distanceKm = 800.0,
            bookingId = "BKG-99210",
            busNumber = "MH-12-CU-4421",
            departureTime = "06:00 PM",
            arrivalTime = "10:00 AM (Next Day)",
            seatNumber = "24B",
            seatType = "Aisle",
            busClass = "Sleeper",
            luggageCount = 2,
            totalAmount = 1450.0,
            paymentMode = "Online",
            paymentStatus = "Paid"
        )
    }
}
