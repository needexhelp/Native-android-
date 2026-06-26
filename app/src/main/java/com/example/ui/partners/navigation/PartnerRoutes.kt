package com.example.ui.partners.navigation

import com.example.ui.partners.common.PartnerType

object PartnerRoutes {

    // Entry
    const val PARTNERS_HOME         = "partners_home"

    // Auth (shared, takes partnerType arg)
    const val PARTNER_PASSWORD      = "partner_password/{partnerType}"
    const val PARTNER_REGISTRATION  = "partner_registration/{partnerType}"

    // Panels
    const val DELIVERY_BOY_PANEL    = "panel_delivery_boy"
    const val TAXI_PANEL            = "panel_taxi"
    const val AUTO_PANEL            = "panel_auto"
    const val AMBULANCE_PANEL       = "panel_ambulance"
    const val MEDICINE_PANEL        = "panel_medicine_shop"
    const val RESTAURANT_PANEL      = "panel_restaurant"
    const val HOSPITAL_PANEL        = "panel_hospital"
    const val BUS_PANEL             = "panel_bus_agency"

    // Incoming order timer (shared, takes partnerType arg)
    const val INCOMING_ORDER        = "incoming_order/{partnerType}"

    // Active order / ride screens
    const val DELIVERY_ACTIVE       = "delivery_active_order"
    const val TAXI_RIDE_START       = "taxi_ride_start"
    const val AUTO_RIDE_START       = "auto_ride_start"
    const val AMBULANCE_ACTIVE      = "ambulance_active"
    const val MEDICINE_ORDER_ACTIVE = "medicine_order_active"
    const val RESTAURANT_ORDER_ACTIVE = "restaurant_order_active"
    const val HOSPITAL_APPOINTMENT  = "hospital_appointment_detail"
    const val BUS_ROUTE_DETAIL      = "bus_route_detail"

    // Helpers
    fun passwordRoute(type: PartnerType) = "partner_password/${type.name}"
    fun registrationRoute(type: PartnerType) = "partner_registration/${type.name}"
    fun incomingOrderRoute(type: PartnerType) = "incoming_order/${type.name}"
}
