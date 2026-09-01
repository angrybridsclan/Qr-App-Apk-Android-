package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.ContactsContract
import android.widget.Toast
import com.example.model.ParsedQrResult

object AppActions {

    fun playBeep() {
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            toneG.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrate(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "QR Scan Result") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, text: String, title: String = "Share QR Code") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    fun openUrl(context: Context, url: String) {
        try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open URL: $url", Toast.LENGTH_SHORT).show()
        }
    }

    fun dialPhone(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not dial phone", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, email: String, subject: String? = null, body: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                if (!subject.isNullOrBlank()) putExtra(Intent.EXTRA_SUBJECT, subject)
                if (!body.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
                if (!message.isNullOrBlank()) putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMap(context: Context, queryOrCoords: String) {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(queryOrCoords))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No map application found", Toast.LENGTH_SHORT).show()
        }
    }

    fun addContact(context: Context, contact: ParsedQrResult.Contact) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.NAME, contact.name)
                contact.phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
                contact.email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
                contact.address?.let { putExtra(ContactsContract.Intents.Insert.POSTAL, it) }
                contact.organization?.let { putExtra(ContactsContract.Intents.Insert.COMPANY, it) }
                contact.title?.let { putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it) }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Contacts app", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchWeb(context: Context, query: String) {
        try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(android.app.SearchManager.QUERY, query)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openUrl(context, "https://www.google.com/search?q=" + Uri.encode(query))
        }
    }
}
