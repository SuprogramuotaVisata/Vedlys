package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.domain.models.TypeDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import com.suprogramuota_visata.vedlys.utils.PlatformFilePicker

@Composable
fun ExportTypesDialog(
    onDismiss: () -> Unit,
    typeName: String,
    items: List<TypeDTO>
) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Eksportuoti duomenis ($typeName)") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Pasirinkite failą eksportui.")
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(onClick = {
                    selectedFile = PlatformFilePicker.pickFileToSave(
                        title = "Eksportuoti duomenis",
                        defaultFileName = "${typeName}_export.json",
                        description = "JSON failai (*.json)",
                        "json"
                    )
                }, enabled = !isLoading) {
                    Text(if (selectedFile == null) "Pasirinkti failą" else "Failas: ${selectedFile?.name}")
                }

                if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            if (isSuccess) {
                Button(onClick = onDismiss) {
                    Text("Baigti")
                }
            } else {
                Button(
                    onClick = {
                        val file = selectedFile ?: return@Button
                        scope.launch {
                            isLoading = true
                            statusMessage = "Eksportuojama..."
                            try {
                                val jsonString = Json { prettyPrint = true }.encodeToString(kotlinx.serialization.serializer(), items)
                                withContext(Dispatchers.IO) {
                                    file.writeText(jsonString)
                                }
                                statusMessage = "Sėkmingai eksportuota ${items.size} įrašų!"
                                isSuccess = true
                            } catch (e: Exception) {
                                statusMessage = "Klaida: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = selectedFile != null && !isLoading
                ) {
                    Text("Eksportuoti")
                }
            }
        },
        dismissButton = {
            if (!isSuccess) {
                TextButton(onClick = onDismiss, enabled = !isLoading) {
                    Text("Atšaukti")
                }
            }
        }
    )
}
