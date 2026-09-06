package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.vedlys.AppLanguage
import com.suprogramuota_visata.vedlys.utils.ImageHelper
import com.suprogramuota_visata.vedlys.utils.PlatformFilePicker
import com.suprogramuota_visata.vedlys.utils.decodeImageByteArray
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ImageAttributeField(
    label: String,
    value: String,
    apiClient: ApiSvClient,
    language: AppLanguage,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val currentUuid = remember(value) { ImageHelper.extractImageUuid(value) }

    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(currentUuid) {
        if (!currentUuid.isNullOrBlank()) {
            val res = ImageHelper.fetchImageBytes(apiClient, currentUuid)
            if (res.isSuccess) {
                val bytes = res.getOrNull()
                if (bytes != null && bytes.isNotEmpty()) {
                    try {
                        thumbnailBitmap = decodeImageByteArray(bytes)
                    } catch (_: Exception) {
                        thumbnailBitmap = null
                    }
                }
            } else {
                thumbnailBitmap = null
            }
        } else {
            thumbnailBitmap = null
        }
    }

    fun handlePickAndUpload(oldUuidToDelete: String? = null) {
        val pickedFile = PlatformFilePicker.pickFileToOpen(
            if (language == AppLanguage.EN) "Select image" else "Pasirinkti nuotrauką",
            if (language == AppLanguage.EN) "Images (*.jpg, *.jpeg, *.png)" else "Paveikslėliai (*.jpg, *.jpeg, *.png)",
            "jpg", "jpeg", "png"
        ) ?: return

        scope.launch {
            isUploading = true
            errorMessage = null
            try {
                val bytes = pickedFile.readBytes()
                val ext = if (pickedFile.name.endsWith(".png", ignoreCase = true)) "png" else "jpg"
                val newUuid = UUID.randomUUID().toString()

                val uploadResult = ImageHelper.uploadImageBytes(apiClient, newUuid, bytes, ext)
                if (uploadResult.isSuccess) {
                    if (!oldUuidToDelete.isNullOrBlank()) {
                        ImageHelper.deleteImage(apiClient, oldUuidToDelete)
                    }
                    val jsonMeta = ImageHelper.buildImageJson(newUuid, pickedFile.length(), ext)
                    onValueChange(jsonMeta)
                } else {
                    errorMessage = uploadResult.exceptionOrNull()?.message ?: "Nepavyko įkelti nuotraukos"
                }
            } catch (e: Exception) {
                errorMessage = "Klaida nuskaitant failą: ${e.message}"
            } finally {
                isUploading = false
            }
        }
    }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            if (currentUuid.isNullOrBlank()) {
                // Nuotrauka neprisegta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == AppLanguage.EN) "No image attached" else "Nuotrauka neprisegta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    } else {
                        Button(
                            onClick = { handlePickAndUpload(null) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (language == AppLanguage.EN) "Add Photo" else "Pridėti nuotrauką")
                        }
                    }
                }
            } else {
                // Nuotrauka prisegta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Miniatiūra
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (thumbnailBitmap != null) {
                            Image(
                                bitmap = thumbnailBitmap!!,
                                contentDescription = "Miniatiūra",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.EN) "Image attached" else "Nuotrauka prisegta",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "UUID: ${currentUuid.take(8)}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Peržiūrėti
                            IconButton(
                                onClick = { showPreviewDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = if (language == AppLanguage.EN) "View" else "Peržiūrėti",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Pakeisti
                            IconButton(
                                onClick = { handlePickAndUpload(currentUuid) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (language == AppLanguage.EN) "Change" else "Pakeisti",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Pašalinti
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = if (language == AppLanguage.EN) "Delete" else "Pašalinti",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showPreviewDialog && !currentUuid.isNullOrBlank()) {
        ImagePreviewDialog(
            imageUuid = currentUuid,
            apiClient = apiClient,
            title = label,
            onDismiss = { showPreviewDialog = false }
        )
    }

    if (showDeleteConfirmDialog && !currentUuid.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(if (language == AppLanguage.EN) "Delete photo" else "Pašalinti nuotrauką") },
            text = {
                Text(
                    if (language == AppLanguage.EN)
                        "Are you sure you want to delete this photo from the server?"
                    else
                        "Ar tikrai norite pašalinti šią nuotrauką iš serverio?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        scope.launch {
                            isUploading = true
                            try {
                                ImageHelper.deleteImage(apiClient, currentUuid)
                                onValueChange("")
                            } catch (e: Exception) {
                                errorMessage = "Klaida trinant: ${e.message}"
                            } finally {
                                isUploading = false
                            }
                        }
                    }
                ) {
                    Text(
                        if (language == AppLanguage.EN) "Delete" else "Pašalinti",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (language == AppLanguage.EN) "Cancel" else "Atšaukti")
                }
            }
        )
    }
}
