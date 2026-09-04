package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.SystemStatusDto
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.AppLanguage
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getHelpString(key: String, language: AppLanguage): String {
    return com.suprogramuota_visata.vedlys.utils.Localization.getString("help", key, language)
}

@Composable
fun HelpScreen(
    apiClient: ApiSvClient,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val language by AppSettings.selectedLanguage.collectAsState()
    val isTermsAccepted by AppSettings.isLicensingTermsAccepted.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var statusState by remember { mutableStateOf<ApiResult<SystemStatusDto>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Mock updates states
    var isUpdatingApp by remember { mutableStateOf(false) }
    var appUpdateProgress by remember { mutableStateOf(0f) }
    var appUpdateStatus by remember { mutableStateOf("") }

    var isUpdatingApi by remember { mutableStateOf(false) }
    var apiUpdateProgress by remember { mutableStateOf(0f) }
    var apiUpdateStatus by remember { mutableStateOf("") }

    suspend fun fetchStatus() {
        isLoading = true
        statusState = apiClient.authRepository.getStatus()
        isLoading = false
    }

    LaunchedEffect(Unit) {
        fetchStatus()
    }

    fun startAppUpdate() {
        if (isUpdatingApp) return
        scope.launch {
            isUpdatingApp = true
            appUpdateStatus = getHelpString("update_checking", language)
            appUpdateProgress = 0.1f
            delay(500)
            appUpdateStatus = getHelpString("update_downloading", language)
            appUpdateProgress = 0.5f
            delay(600)
            appUpdateStatus = getHelpString("update_installing", language)
            appUpdateProgress = 0.8f
            delay(500)
            appUpdateStatus = getHelpString("update_done", language)
            appUpdateProgress = 1.0f
            delay(800)
            isUpdatingApp = false
            appUpdateProgress = 0f
            appUpdateStatus = ""
        }
    }

    fun startApiUpdate() {
        if (isUpdatingApi) return
        scope.launch {
            isUpdatingApi = true
            apiUpdateStatus = getHelpString("update_checking", language)
            apiUpdateProgress = 0.1f
            delay(500)
            apiUpdateStatus = getHelpString("update_downloading", language)
            apiUpdateProgress = 0.4f
            delay(600)
            apiUpdateStatus = getHelpString("update_installing", language)
            apiUpdateProgress = 0.8f
            delay(500)
            apiUpdateStatus = getHelpString("update_done", language)
            apiUpdateProgress = 1.0f
            delay(800)
            isUpdatingApi = false
            apiUpdateProgress = 0f
            apiUpdateStatus = ""
        }
    }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = getHelpString("title", language),
                onBack = onBack,
                onHome = onHome
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    when (statusState) {
                        is ApiResult.Error -> {
                            // Offline/Error banner
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = getHelpString("error_fetch", language),
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    IconButton(onClick = { scope.launch { fetchStatus() } }) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }

                    // 1. Systems information card
                    HelpSectionTitle(getHelpString("system_info", language))
                    HelpCard {
                        InfoRow(
                            icon = Icons.Default.Computer,
                            title = getHelpString("app_version", language),
                            value = "Vedlys (Desktop) 1.0.4"
                        )
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        
                        val apiVersion = when (val result = statusState) {
                            is ApiResult.Success -> result.data.apiVersion
                            else -> "Offline"
                        }
                        InfoRow(
                            icon = Icons.Default.Cloud,
                            title = getHelpString("api_version", language),
                            value = apiVersion
                        )
                    }

                    // 2. License and Key status card
                    val statusData = (statusState as? ApiResult.Success)?.data
                    if (statusData != null) {
                        HelpSectionTitle(getHelpString("license_info", language))
                        HelpCard {
                            // License status row with a beautiful badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = getHelpString("license_status", language),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                val isLicenseValid = statusData.licenseStatus.uppercase() == "VALID"
                                val badgeBg = if (isLicenseValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                val badgeText = if (isLicenseValid) Color(0xFF2E7D32) else Color(0xFFC62828)
                                val daysLeftText = statusData.daysLeft?.let { " ($it ${getHelpString("days", language)})" } ?: ""
                                val badgeLabel = if (isLicenseValid) {
                                    getHelpString("active", language) + daysLeftText
                                } else {
                                    getHelpString("expired", language)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = badgeLabel,
                                        color = badgeText,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                            // Licensed email row
                            val emailVal = statusData.licenseEmail
                            if (!emailVal.isNullOrBlank()) {
                                InfoRow(
                                    icon = Icons.Default.Email,
                                    title = getHelpString("license_email", language),
                                    value = emailVal
                                )
                                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }

                            // API Key expiration details
                            val formattedExpirationDate = remember(statusData.apiKeyExpiresAt) {
                                if (statusData.apiKeyExpiresAt > 0) {
                                    try {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        sdf.format(Date(statusData.apiKeyExpiresAt))
                                    } catch (e: Exception) {
                                        "-"
                                    }
                                } else {
                                    "-"
                                }
                            }
                            val apiKeyVal = if (statusData.apiKeyDaysLeft > 0) {
                                getHelpString("help_api_key_days_val", language)
                                    .format(statusData.apiKeyDaysLeft, formattedExpirationDate)
                            } else {
                                getHelpString("expired", language)
                            }

                            InfoRow(
                                icon = Icons.Default.Key,
                                title = getHelpString("api_key_days", language),
                                value = apiKeyVal
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 2.5 Import statistics card
                        val importStats = statusData.importStats
                        HelpSectionTitle(getHelpString("import_counters", language))
                        HelpCard {
                            if (importStats.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = getHelpString("no_imports", language),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    val formats = listOf("JSON", "EIP", "PDF", "CSV", "XLSX", "Word", "XML", "TXT")
                                    val sortedStats = formats.map { it.lowercase() to (importStats[it.lowercase()] ?: 0) }
                                    
                                    sortedStats.forEachIndexed { idx, (format, count) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Build,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = format.uppercase(),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = count.toString(),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                        }
                                        if (idx < sortedStats.size - 1) {
                                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                                        }
                                    }
                                    
                                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                                    val total = importStats.values.sum()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = getHelpString("total_imported", language),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = total.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Update execution block
                    HelpSectionTitle(getHelpString("settings_import_settings", language))
                    HelpCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Update Vedlys
                            Button(
                                onClick = { startAppUpdate() },
                                enabled = !isUpdatingApp && !isUpdatingApi,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(getHelpString("update_app", language))
                            }
                            
                            AnimatedVisibility(visible = isUpdatingApp) {
                                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                    Text(
                                        text = appUpdateStatus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = appUpdateProgress,
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Update API server
                            Button(
                                onClick = { startApiUpdate() },
                                enabled = !isUpdatingApp && !isUpdatingApi && statusState is ApiResult.Success,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(
                                    text = getHelpString("update_api", language),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }

                            AnimatedVisibility(visible = isUpdatingApi) {
                                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                    Text(
                                        text = apiUpdateStatus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = apiUpdateProgress,
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    // 4. Licensing T&C card
                    HelpSectionTitle(getHelpString("help_licensing_terms", language))
                    HelpCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = getHelpString("help_terms_text", language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Start
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            if (isTermsAccepted) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = getHelpString("help_terms_accepted", language),
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { AppSettings.setLicensingTermsAccepted(true) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Text(
                                        text = getHelpString("help_terms_agree", language),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun HelpSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun HelpCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun HelpScreenPreview() {
    VedlysTheme {
        Surface {
            HelpScreen(
                apiClient = ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                onBack = {},
                onHome = {}
            )
        }
    }
}

