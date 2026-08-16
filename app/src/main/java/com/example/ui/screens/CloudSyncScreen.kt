package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.StoreSettingsEntity
import com.example.data.model.SyncStatusState
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.DarkChartBg
import com.example.ui.theme.PurplePill
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryDark
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockGreenBg
import com.example.ui.theme.StockRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudSyncScreen(
    syncState: SyncStatusState,
    settings: StoreSettingsEntity,
    onManualSync: () -> Unit,
    onSaveSettings: (StoreSettingsEntity) -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onResetDemoData: () -> Unit
) {
    var storeName by remember(settings) { mutableStateOf(settings.storeName) }
    var phone by remember(settings) { mutableStateOf(settings.phone) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var taxRate by remember(settings) { mutableStateOf(settings.taxRatePercent.toString()) }
    var currency by remember(settings) { mutableStateOf(settings.currencySymbol) }
    var footer by remember(settings) { mutableStateOf(settings.receiptFooter) }
    var autoSync by remember(settings) { mutableStateOf(settings.autoSyncEnabled) }

    var showResetConfirm by remember { mutableStateOf(false) }
    var settingsSavedNotification by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault()) }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Restablecer Datos de Demostración") },
            text = { Text("Esto restaurará el inventario inicial con productos de muestra listos para vender. ¿Desea continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetDemoData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StockRed)
                ) {
                    Text("Restablecer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cloud Sync Status Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkChartBg)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (syncState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Sincronización en la Nube",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (syncState.isSyncing) "Sincronizando..." else "Respaldo y Estado Activo",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (syncState.pendingSyncCount == 0) StockGreenBg else Color(0xFFFFEDD5))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (syncState.pendingSyncCount == 0) "Al día" else "${syncState.pendingSyncCount} Pendientes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (syncState.pendingSyncCount == 0) StockGreen else Color(0xFFC2410C)
                        )
                    }
                }

                Text(
                    text = "Última sincronización: ${if (syncState.lastSyncTime > 0) dateFormat.format(Date(syncState.lastSyncTime)) else "Recién iniciado"}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                if (syncState.errorMessage != null) {
                    Text(
                        text = syncState.errorMessage ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFFFCA5A5)
                    )
                }

                Button(
                    onClick = onManualSync,
                    enabled = !syncState.isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PurplePrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_sync_button")
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Sincronizar Ahora con la Nube", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Backup & Restore Utilities Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Respaldos y Portabilidad JSON",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Exporta tu inventario y catálogo para guardarlo en la nube o importarlo en otro dispositivo.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportJson,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Exportar", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onImportJson,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Importar", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StockRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Cargar Productos de Prueba (Demo)", fontSize = 12.sp)
                }
            }
        }

        // Store Settings Configuration Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = PurplePrimary)
                        Text(
                            text = "Configuración del Negocio",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (settingsSavedNotification) {
                        Text(
                            text = "¡Guardado!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StockGreen
                        )
                    }
                }

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it; settingsSavedNotification = false },
                    label = { Text("Nombre del Comercio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it; settingsSavedNotification = false },
                        label = { Text("Símbolo Moneda") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = taxRate,
                        onValueChange = { taxRate = it; settingsSavedNotification = false },
                        label = { Text("Tasa Impuesto (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; settingsSavedNotification = false },
                    label = { Text("Teléfono de Contacto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; settingsSavedNotification = false },
                    label = { Text("Dirección") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = footer,
                    onValueChange = { footer = it; settingsSavedNotification = false },
                    label = { Text("Mensaje en Ticket") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sincronización Automática en segundo plano",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = autoSync,
                        onCheckedChange = { autoSync = it; settingsSavedNotification = false },
                        colors = SwitchDefaults.colors(checkedThumbColor = PurplePrimary)
                    )
                }

                Button(
                    onClick = {
                        val newSettings = settings.copy(
                            storeName = storeName.ifBlank { "Mi Negocio" },
                            phone = phone,
                            address = address,
                            taxRatePercent = taxRate.toDoubleOrNull() ?: 0.0,
                            currencySymbol = currency.ifBlank { "$" },
                            receiptFooter = footer,
                            autoSyncEnabled = autoSync
                        )
                        onSaveSettings(newSettings)
                        settingsSavedNotification = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_settings_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Guardar Configuración")
                }
            }
        }
    }
}
