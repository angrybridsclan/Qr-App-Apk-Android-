package com.example.model

import java.util.Locale

object QrDataParser {

    fun parse(rawValue: String, format: String = "QR_CODE"): ParsedQrResult {
        val trimmed = rawValue.trim()

        if (format != "QR_CODE" && (format.contains("EAN") || format.contains("UPC") || format.contains("CODE") || format.contains("ITF") || format.contains("DATA_MATRIX") || format.contains("PDF_417"))) {
            return ParsedQrResult.Product(code = trimmed, barcodeFormat = format)
        }

        // WIFI: WIFI:S:MySSID;T:WPA;P:MyPassword;;
        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            val ssid = extractWifiField(trimmed, "S:")
            val password = extractWifiField(trimmed, "P:")
            val type = extractWifiField(trimmed, "T:").ifEmpty { "WPA" }
            val hidden = extractWifiField(trimmed, "H:").equals("true", ignoreCase = true)
            return ParsedQrResult.Wifi(ssid = ssid, password = password, type = type, hidden = hidden)
        }

        // vCard / MeCard
        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.startsWith("MECARD:", ignoreCase = true)) {
            return parseContact(trimmed)
        }

        // URL
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            (trimmed.startsWith("www.", ignoreCase = true) && trimmed.contains("."))
        ) {
            val fullUrl = if (trimmed.startsWith("www.", ignoreCase = true)) "https://$trimmed" else trimmed
            return ParsedQrResult.Url(url = fullUrl)
        }

        // Tel
        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.substring(4)
            return ParsedQrResult.Phone(phoneNumber = phone)
        }

        // Mailto
        if (trimmed.startsWith("mailto:", ignoreCase = true) || trimmed.startsWith("MATMSG:", ignoreCase = true)) {
            return parseEmail(trimmed)
        }

        // SMS
        if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
            return parseSms(trimmed)
        }

        // Geo
        if (trimmed.startsWith("geo:", ignoreCase = true)) {
            return parseGeo(trimmed)
        }

        // Calendar VEVENT
        if (trimmed.startsWith("BEGIN:VEVENT", ignoreCase = true)) {
            return parseCalendar(trimmed)
        }

        // Numeric Product Code detection
        if (trimmed.matches(Regex("^[0-9]{8,14}$"))) {
            return ParsedQrResult.Product(code = trimmed, barcodeFormat = format)
        }

        return ParsedQrResult.Text(text = rawValue)
    }

    private fun extractWifiField(raw: String, prefix: String): String {
        val startIndex = raw.indexOf(prefix, ignoreCase = true)
        if (startIndex == -1) return ""
        val contentStart = startIndex + prefix.length
        val endIndex = raw.indexOf(';', contentStart)
        return if (endIndex != -1) raw.substring(contentStart, endIndex) else raw.substring(contentStart)
    }

    private fun parseContact(raw: String): ParsedQrResult.Contact {
        var name = ""
        var phone = ""
        var email = ""
        var address = ""
        var org = ""
        var title = ""

        val lines = raw.lines()
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("FN:", ignoreCase = true) -> name = trimmedLine.substring(3).trim()
                trimmedLine.startsWith("N:", ignoreCase = true) && name.isEmpty() -> {
                    val parts = trimmedLine.substring(2).split(";")
                    name = parts.reversed().filter { it.isNotBlank() }.joinToString(" ").trim()
                }
                trimmedLine.startsWith("TEL", ignoreCase = true) -> {
                    val colonIdx = trimmedLine.indexOf(':')
                    if (colonIdx != -1) phone = trimmedLine.substring(colonIdx + 1).trim()
                }
                trimmedLine.startsWith("EMAIL", ignoreCase = true) -> {
                    val colonIdx = trimmedLine.indexOf(':')
                    if (colonIdx != -1) email = trimmedLine.substring(colonIdx + 1).trim()
                }
                trimmedLine.startsWith("ADR", ignoreCase = true) -> {
                    val colonIdx = trimmedLine.indexOf(':')
                    if (colonIdx != -1) {
                        address = trimmedLine.substring(colonIdx + 1).replace(";", ", ").replace(", ,", ",").trim()
                    }
                }
                trimmedLine.startsWith("ORG:", ignoreCase = true) -> org = trimmedLine.substring(4).trim()
                trimmedLine.startsWith("TITLE:", ignoreCase = true) -> title = trimmedLine.substring(6).trim()
            }
        }

        if (name.isEmpty()) name = "Contact"
        return ParsedQrResult.Contact(
            name = name,
            address = address.ifEmpty { null },
            phone = phone.ifEmpty { null },
            email = email.ifEmpty { null },
            organization = org.ifEmpty { null },
            title = title.ifEmpty { null }
        )
    }

    private fun parseEmail(raw: String): ParsedQrResult.Email {
        if (raw.startsWith("mailto:", ignoreCase = true)) {
            val uriStr = raw.substring(7)
            val qIdx = uriStr.indexOf('?')
            val email = if (qIdx != -1) uriStr.substring(0, qIdx) else uriStr
            var subject: String? = null
            var body: String? = null
            if (qIdx != -1) {
                val query = uriStr.substring(qIdx + 1)
                val params = query.split("&")
                for (param in params) {
                    val kv = param.split("=")
                    if (kv.size == 2) {
                        if (kv[0].equals("subject", ignoreCase = true)) subject = java.net.URLDecoder.decode(kv[1], "UTF-8")
                        if (kv[0].equals("body", ignoreCase = true)) body = java.net.URLDecoder.decode(kv[1], "UTF-8")
                    }
                }
            }
            return ParsedQrResult.Email(email = email, subject = subject, body = body)
        }
        return ParsedQrResult.Email(email = raw)
    }

    private fun parseSms(raw: String): ParsedQrResult.Sms {
        val clean = if (raw.startsWith("smsto:", ignoreCase = true)) raw.substring(6) else raw.substring(4)
        val colonIdx = clean.indexOf(':')
        val qIdx = clean.indexOf('?')
        return when {
            colonIdx != -1 -> {
                val phone = clean.substring(0, colonIdx)
                val msg = clean.substring(colonIdx + 1)
                ParsedQrResult.Sms(phone, msg.ifEmpty { null })
            }
            qIdx != -1 -> {
                val phone = clean.substring(0, qIdx)
                val query = clean.substring(qIdx + 1)
                val msg = if (query.startsWith("body=", ignoreCase = true)) query.substring(5) else query
                ParsedQrResult.Sms(phone, msg.ifEmpty { null })
            }
            else -> ParsedQrResult.Sms(clean, null)
        }
    }

    private fun parseGeo(raw: String): ParsedQrResult.Geo {
        val geoData = raw.substring(4)
        val parts = geoData.split("?q=")
        val coords = parts[0].split(",")
        val lat = coords.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val lng = coords.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        val query = parts.getOrNull(1)
        return ParsedQrResult.Geo(lat, lng, query)
    }

    private fun parseCalendar(raw: String): ParsedQrResult.CalendarEvent {
        var summary = "Calendar Event"
        var location = ""
        var desc = ""
        var start = ""
        var end = ""
        for (line in raw.lines()) {
            val t = line.trim()
            if (t.startsWith("SUMMARY:", ignoreCase = true)) summary = t.substring(8)
            if (t.startsWith("LOCATION:", ignoreCase = true)) location = t.substring(9)
            if (t.startsWith("DESCRIPTION:", ignoreCase = true)) desc = t.substring(12)
            if (t.startsWith("DTSTART:", ignoreCase = true)) start = t.substring(8)
            if (t.startsWith("DTEND:", ignoreCase = true)) end = t.substring(6)
        }
        return ParsedQrResult.CalendarEvent(summary, location.ifEmpty { null }, desc.ifEmpty { null }, start.ifEmpty { null }, end.ifEmpty { null })
    }

    fun getCategoryType(parsed: ParsedQrResult): String = when (parsed) {
        is ParsedQrResult.Url -> "URL"
        is ParsedQrResult.Wifi -> "Wifi"
        is ParsedQrResult.Contact -> "Contact"
        is ParsedQrResult.Phone -> "Phone"
        is ParsedQrResult.Email -> "Email"
        is ParsedQrResult.Sms -> "SMS"
        is ParsedQrResult.Geo -> "Geo"
        is ParsedQrResult.CalendarEvent -> "Calendar"
        is ParsedQrResult.Product -> "Product"
        is ParsedQrResult.Text -> "Text"
    }
}
