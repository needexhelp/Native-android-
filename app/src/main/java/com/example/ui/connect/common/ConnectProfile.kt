package com.example.ui.connect.common

data class ConnectProfile(
    val id: String,
    val userId: String,
    val displayName: String,
    val photoUrl: String?,
    val statusBio: String?,
    val city: String,
    val locality: String?,
    val phone: String
)

data class ConnectBusinessProfile(
    val id: String,
    val userId: String,
    val businessName: String,
    val category: String,
    val logoUrl: String?,
    val description: String?,
    val city: String,
    val locality: String?,
    val address: String?,
    val contactNumber: String,
    val rating: Double? = null,
    val followerCount: Int = 0
)

enum class ActiveProfileType { PERSONAL, BUSINESS }
