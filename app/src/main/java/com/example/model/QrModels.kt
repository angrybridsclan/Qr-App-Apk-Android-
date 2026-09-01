package com.example.model

sealed class ParsedQrResult {
    data class Url(val url: String) : ParsedQrResult()
    data class Wifi(
        val ssid: String,
        val password: String,
        val type: String,
        val hidden: Boolean = false
    ) : ParsedQrResult()
    data class Contact(
        val name: String,
        val address: String? = null,
        val phone: String? = null,
        val email: String? = null,
        val organization: String? = null,
        val title: String? = null
    ) : ParsedQrResult()
    data class Phone(val phoneNumber: String) : ParsedQrResult()
    data class Email(
        val email: String,
        val subject: String? = null,
        val body: String? = null
    ) : ParsedQrResult()
    data class Sms(
        val phoneNumber: String,
        val message: String? = null
    ) : ParsedQrResult()
    data class Geo(
        val latitude: Double,
        val longitude: Double,
        val query: String? = null
    ) : ParsedQrResult()
    data class CalendarEvent(
        val title: String,
        val location: String? = null,
        val description: String? = null,
        val start: String? = null,
        val end: String? = null
    ) : ParsedQrResult()
    data class Product(val code: String, val barcodeFormat: String) : ParsedQrResult()
    data class Text(val text: String) : ParsedQrResult()
}
