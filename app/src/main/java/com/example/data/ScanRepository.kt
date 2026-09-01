package com.example.data

import kotlinx.coroutines.flow.Flow

class ScanRepository(private val scanDao: ScanDao) {
    val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()
    val favoriteScans: Flow<List<ScanEntity>> = scanDao.getFavoriteScans()
    val createdScans: Flow<List<ScanEntity>> = scanDao.getCreatedScans()

    suspend fun getScanById(id: Long): ScanEntity? = scanDao.getScanById(id)

    suspend fun insertScan(scan: ScanEntity): Long = scanDao.insertScan(scan)

    suspend fun updateScan(scan: ScanEntity) = scanDao.updateScan(scan)

    suspend fun deleteScanById(id: Long) = scanDao.deleteScanById(id)

    suspend fun deleteAllScans() = scanDao.deleteAllScans()

    suspend fun setFavorite(id: Long, isFavorite: Boolean) = scanDao.setFavorite(id, isFavorite)

    suspend fun updateCustomTitle(id: Long, customTitle: String) = scanDao.updateCustomTitle(id, customTitle)
}
