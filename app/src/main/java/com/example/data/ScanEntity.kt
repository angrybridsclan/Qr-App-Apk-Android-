package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawValue: String,
    val format: String = "QR_CODE",
    val type: String = "TEXT",
    val title: String = "Text",
    val customTitle: String? = null,
    val subtitle: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isCreated: Boolean = false
)
