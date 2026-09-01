package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ScanEntity
import com.example.data.ScanRepository
import com.example.model.QrDataParser
import com.example.util.AppActions
import com.example.util.AppPreferences
import com.example.util.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository
    private val settingsManager: SettingsManager = SettingsManager(application)

    val settings: StateFlow<AppPreferences> = settingsManager.settings

    val allScans: StateFlow<List<ScanEntity>>
    val favoriteScans: StateFlow<List<ScanEntity>>
    val createdScans: StateFlow<List<ScanEntity>>

    private val _currentScan = MutableStateFlow<ScanEntity?>(null)
    val currentScan: StateFlow<ScanEntity?> = _currentScan.asStateFlow()

    private val _batchScans = MutableStateFlow<List<ScanEntity>>(emptyList())
    val batchScans: StateFlow<List<ScanEntity>> = _batchScans.asStateFlow()

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ScanRepository(db.scanDao())

        allScans = repository.allScans.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        favoriteScans = repository.favoriteScans.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        createdScans = repository.createdScans.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun onScanResult(rawValue: String, format: String = "QR_CODE", onNavigateToDetail: (Long) -> Unit) {
        val parsed = QrDataParser.parse(rawValue, format)
        val type = QrDataParser.getCategoryType(parsed)
        val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
        val timestampStr = "${sdf.format(Date())}, $format"

        val scan = ScanEntity(
            rawValue = rawValue,
            format = format,
            type = type,
            title = type,
            subtitle = timestampStr,
            timestamp = System.currentTimeMillis(),
            isFavorite = false,
            isCreated = false
        )

        val currentPref = settings.value
        if (currentPref.beep) AppActions.playBeep()
        if (currentPref.vibrate) AppActions.vibrate(getApplication())
        if (currentPref.copyToClipboard) AppActions.copyToClipboard(getApplication(), rawValue)

        viewModelScope.launch {
            val id = repository.insertScan(scan)
            val savedScan = scan.copy(id = id)
            _currentScan.value = savedScan

            if (currentPref.batchScanMode) {
                _batchScans.value = _batchScans.value + savedScan
            } else {
                onNavigateToDetail(id)
            }
        }
    }

    fun createAndSaveQr(rawValue: String, type: String, title: String, onNavigateToDetail: (Long) -> Unit) {
        val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
        val timestampStr = "${sdf.format(Date())}, QR_CODE"

        val scan = ScanEntity(
            rawValue = rawValue,
            format = "QR_CODE",
            type = type,
            title = title,
            subtitle = timestampStr,
            timestamp = System.currentTimeMillis(),
            isFavorite = false,
            isCreated = true
        )

        viewModelScope.launch {
            val id = repository.insertScan(scan)
            val saved = scan.copy(id = id)
            _currentScan.value = saved
            onNavigateToDetail(id)
        }
    }

    fun loadScanById(id: Long) {
        viewModelScope.launch {
            val scan = repository.getScanById(id)
            if (scan != null) {
                _currentScan.value = scan
            }
        }
    }

    fun toggleFavorite(scan: ScanEntity) {
        viewModelScope.launch {
            val newFav = !scan.isFavorite
            repository.setFavorite(scan.id, newFav)
            if (_currentScan.value?.id == scan.id) {
                _currentScan.value = _currentScan.value?.copy(isFavorite = newFav)
            }
        }
    }

    fun updateCustomTitle(scanId: Long, newTitle: String) {
        viewModelScope.launch {
            repository.updateCustomTitle(scanId, newTitle)
            if (_currentScan.value?.id == scanId) {
                _currentScan.value = _currentScan.value?.copy(customTitle = newTitle)
            }
        }
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScanById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllScans()
        }
    }

    fun clearBatch() {
        _batchScans.value = emptyList()
    }

    fun setZoom(zoom: Float) {
        _zoomLevel.value = zoom.coerceIn(1.0f, 5.0f)
    }

    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }

    fun toggleCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    // Settings actions
    fun setColorScheme(index: Int) = settingsManager.setColorIndex(index)
    fun setThemeMode(mode: String) = settingsManager.setThemeMode(mode)
    fun setBeep(enabled: Boolean) = settingsManager.setBeep(enabled)
    fun setVibrate(enabled: Boolean) = settingsManager.setVibrate(enabled)
    fun setCopyToClipboard(enabled: Boolean) = settingsManager.setCopyToClipboard(enabled)
    fun setUrlInfo(enabled: Boolean) = settingsManager.setUrlInfo(enabled)
    fun setBatchScanMode(enabled: Boolean) = settingsManager.setBatchScanMode(enabled)
}
