package com.example.data.model

data class UserProfile(
    val phoneNumber: String = "",
    val userName: String = "",
    val bloodGroup: String = "B+",
    val age: String = "",
    val doctorName: String = "",
    val targetFastingMin: Int = 70,
    val targetFastingMax: Int = 130,
    val targetPostMealMax: Int = 180,
    val createdAt: Long = System.currentTimeMillis()
)
