package com.example.tubesmobdev.util

object ProfileUtil {
    fun getCountryName(countryCode: String): String {
        return when (countryCode.uppercase()) {
            "ID" -> "Indonesia"
            "MY" -> "Malaysia"
            "US" -> "United States"
            "GB" -> "United Kingdom"
            "CH" -> "Switzerland"
            "DE" -> "Germany"
            "BR" -> "Brazil"
            else -> "Negara tidak tersedia"
        }
    }
}