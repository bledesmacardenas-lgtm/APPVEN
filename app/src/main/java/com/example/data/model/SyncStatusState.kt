package com.example.data.model

data class SyncStatusState(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0L,
    val pendingSyncCount: Int = 0,
    val errorMessage: String? = null,
    val autoSyncEnabled: Boolean = true
)
