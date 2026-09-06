package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.vedlys.utils.ImageHelper
import com.suprogramuota_visata.vedlys.utils.decodeImageByteArray

@Composable
fun ImagePreviewDialog(
    imageUuid: String,
    apiClient: ApiSvClient,
    title: String = "Nuotraukos peržiūra",
    onDismiss: () -> Unit
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUuid) {
        isLoading = true
        errorMessage = null
        val result = ImageHelper.fetchImageBytes(apiClient, imageUuid)
        if (result.isSuccess) {
            val bytes = result.getOrNull()
            if (bytes != null && bytes.isNotEmpty()) {
                try {
                    imageBitmap = decodeImageByteArray(bytes)
                } catch (e: Exception) {
                    errorMessage = "Klaida dekoduojant paveikslėlį: ${e.message}"
                }
            } else {
                errorMessage = "Gauti tušti duomenys."
            }
        } else {
            errorMessage = result.exceptionOrNull()?.message ?: "Nepavyko užkrauti nuotraukos"
        }
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Uždaryti",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> CircularProgressIndicator()
                        errorMessage != null -> Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        imageBitmap != null -> {
                            Image(
                                bitmap = imageBitmap!!,
                                contentDescription = "Nuotrauka",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
