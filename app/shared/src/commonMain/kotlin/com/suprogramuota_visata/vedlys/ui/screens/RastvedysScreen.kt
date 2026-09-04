package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.LogEntryDTO
import com.suprogramuota_visata.api.domain.models.LogRetentionConfigDTO
import com.suprogramuota_visata.api.domain.models.LogRotationConfigDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme

@Composable
fun RastvedysScreen(
    apiClient: ApiSvClient,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }

    // Tab 1 State: Logs Viewer
    var logs by remember { mutableStateOf<List<LogEntryDTO>>(emptyList()) }
    var isLoadingLogs by remember { mutableStateOf(false) }
    var logsErrorMessage by remember { mutableStateOf<String?>(null) }
    var logsSearchQuery by remember { mutableStateOf("") }
    var selectedLevelFilter by remember { mutableStateOf("ALL") } // "ALL", "INFO", "WARN", "ERROR"
    var selectedLog by remember { mutableStateOf<LogEntryDTO?>(null) }
    var showClearLogsConfirm by remember { mutableStateOf(false) }

    // Tab 2 State: Log Levels
    var currentLogLevel by remember { mutableStateOf("INFO") }
    var componentLoggers by remember {
        mutableStateOf(
            listOf(
                "root" to "INFO",
                "com.suprogramuota_visata.controllers" to "DEBUG",
                "com.suprogramuota_visata.services" to "INFO",
                "io.ktor.server.engine" to "WARN",
                "org.jetbrains.exposed.sql" to "WARN"
            )
        )
    }

    // Tab 3 State: Log Rotation & Size Limits Config
    var rotationConfig by remember { mutableStateOf(LogRotationConfigDTO()) }
    var retentionConfig by remember { mutableStateOf(LogRetentionConfigDTO()) }
    var isLoadingConfig by remember { mutableStateOf(false) }
    var configSuccessMessage by remember { mutableStateOf<String?>(null) }
    var configErrorMessage by remember { mutableStateOf<String?>(null) }

    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun fetchLogs() {
        coroutineScope.launch {
            isLoadingLogs = true
            logsErrorMessage = null
            when (val result = apiClient.loggerRepository.getLogErrors(limit = 150)) {
                is ApiResult.Success -> {
                    logs = result.data
                    isLoadingLogs = false
                }
                is ApiResult.Error -> {
                    logsErrorMessage = result.message
                    isLoadingLogs = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchLogs()
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()) }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = "Raštvedys (Sistemos stebėjimas ir žurnalų valdymas)",
                onBack = onBack,
                onHome = onHome,
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { fetchLogs() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Atnaujinti")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Navigation Tabs Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📋 Stebėjimo Įrašai", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("⚙️ Loginimo Lygiai", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("🗓️ Rotacija ir Dydis", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("🧹 Istorija ir Valymas", fontWeight = FontWeight.Bold) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                statusMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = msg, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            TextButton(onClick = { statusMessage = null }) {
                                Text("Gerai")
                            }
                        }
                    }
                }

                when (selectedTab) {
                    0 -> RenderLogsTab(
                        logs = logs,
                        isLoading = isLoadingLogs,
                        errorMessage = logsErrorMessage,
                        searchQuery = logsSearchQuery,
                        onSearchChange = { logsSearchQuery = it },
                        levelFilter = selectedLevelFilter,
                        onLevelFilterChange = { selectedLevelFilter = it },
                        onLogClick = { selectedLog = it },
                        onClearLogs = { showClearLogsConfirm = true },
                        dateFormat = dateFormat
                    )

                    1 -> RenderLogLevelsTab(
                        currentLogLevel = currentLogLevel,
                        onChangeLogLevel = { level ->
                            coroutineScope.launch {
                                apiClient.loggerRepository.changeLogLevel(level)
                                currentLogLevel = level
                                statusMessage = "Loginimo lygis sėkmingai pakeistas į '$level'."
                            }
                        },
                        componentLoggers = componentLoggers,
                        onChangeComponentLogLevel = { name, level ->
                            componentLoggers = componentLoggers.map { if (it.first == name) name to level else it }
                            statusMessage = "Komponento '$name' loginimo lygis pakeistas į '$level'."
                        }
                    )

                    2 -> RenderRotationConfigTab(
                        config = rotationConfig,
                        onSaveConfig = { updated ->
                            rotationConfig = updated
                            statusMessage = "Logų rotacijos ir dydžio ribojimo nustatymai išsaugoti."
                        }
                    )

                    3 -> RenderRetentionConfigTab(
                        config = retentionConfig,
                        onSaveConfig = { updated ->
                            retentionConfig = updated
                            statusMessage = "Istorijos saugojimo politika sėkmingai atnaujinta."
                        },
                        onCleanupNow = {
                            coroutineScope.launch {
                                logs = emptyList()
                                statusMessage = "Senesni nei ${retentionConfig.retentionDays} dienų stebėjimo įrašai sėkmingai išvalyti."
                            }
                        }
                    )
                }
            }
        }
    }

    // Detail Log Dialog
    selectedLog?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedLog = null },
            title = { Text("Stebėjimo įrašas: [${log.level}] ${log.serviceName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Laikas: ${dateFormat.format(Date(log.timestamp))}")
                    Text("Pranešimas: ${log.message}", fontWeight = FontWeight.SemiBold)
                    val st = log.stackTrace
                    if (!st.isNullOrBlank()) {
                        HorizontalDivider()
                        Text("Klaidos išrašas (Stack Trace):", fontWeight = FontWeight.Bold)
                        Surface(
                            color = Color.Black.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                        ) {
                            Text(
                                text = st,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLog = null }) {
                    Text("Uždaryti")
                }
            }
        )
    }

    // Clear Logs Confirmation Dialog
    if (showClearLogsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearLogsConfirm = false },
            title = { Text("🗑️ Išvalyti stebėjimo žurnalą?") },
            text = { Text("Ar tikrai norite pašalinti visus rodomus stebėjimo įrašus? Šio veiksmo atšaukti negalima.") },
            confirmButton = {
                Button(
                    onClick = {
                        logs = emptyList()
                        showClearLogsConfirm = false
                        statusMessage = "Stebėjimo žurnalas išvalytas."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Taip, išvalyti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogsConfirm = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }
}

// Sub-component for Tab 1
@Composable
private fun RenderLogsTab(
    logs: List<LogEntryDTO>,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    levelFilter: String,
    onLevelFilterChange: (String) -> Unit,
    onLogClick: (LogEntryDTO) -> Unit,
    onClearLogs: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    val filteredLogs = remember(logs, searchQuery, levelFilter) {
        logs.filter { log ->
            val matchesLevel = when (levelFilter) {
                "INFO" -> log.level.equals("INFO", ignoreCase = true)
                "WARN" -> log.level.equals("WARN", ignoreCase = true)
                "ERROR" -> log.level.equals("ERROR", ignoreCase = true)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                log.serviceName.contains(searchQuery, ignoreCase = true) ||
                log.message.contains(searchQuery, ignoreCase = true) ||
                log.level.contains(searchQuery, ignoreCase = true)
            matchesLevel && matchesSearch
        }
    }

    val infoCount = remember(logs) { logs.count { it.level.equals("INFO", ignoreCase = true) } }
    val warnCount = remember(logs) { logs.count { it.level.equals("WARN", ignoreCase = true) } }
    val errorCount = remember(logs) { logs.count { it.level.equals("ERROR", ignoreCase = true) } }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Level Sub-Tabs / Filters
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = levelFilter == "ALL",
                onClick = { onLevelFilterChange("ALL") },
                label = { Text("Visi (${logs.size})") }
            )
            FilterChip(
                selected = levelFilter == "INFO",
                onClick = { onLevelFilterChange("INFO") },
                label = { Text("💬 Pranešimai ($infoCount)") }
            )
            FilterChip(
                selected = levelFilter == "WARN",
                onClick = { onLevelFilterChange("WARN") },
                label = { Text("⚠️ Perspėjimai ($warnCount)") }
            )
            FilterChip(
                selected = levelFilter == "ERROR",
                onClick = { onLevelFilterChange("ERROR") },
                label = { Text("❌ Klaidos ($errorCount)") }
            )
        }

        // Header Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Ieškoti stebėjimo įrašų pagal pranešimą ar servisą...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            )

            Button(
                onClick = onClearLogs,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Išvalyti žurnalą")
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Klaida: $errorMessage",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (filteredLogs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Stebėjimo įrašų nerasta.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLogs) { log ->
                    val isError = log.level.equals("ERROR", ignoreCase = true)
                    val isWarn = log.level.equals("WARN", ignoreCase = true)
                    val levelBg = if (isError) MaterialTheme.colorScheme.errorContainer
                    else if (isWarn) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogClick(log) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = levelBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = log.level,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = log.serviceName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = log.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sub-component for Tab 2
@Composable
private fun RenderLogLevelsTab(
    currentLogLevel: String,
    onChangeLogLevel: (String) -> Unit,
    componentLoggers: List<Pair<String, String>>,
    onChangeComponentLogLevel: (String, String) -> Unit
) {
    val levels = listOf("DEBUG", "INFO", "WARN", "ERROR")

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🌐 Pagrindinis sistemos loginimo lygis (Global Root Logger)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Pasirinkite, kokio lygio stebėjimo pranešimus Ktor serveris registruoja sistemoje:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    levels.forEach { level ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onChangeLogLevel(level) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentLogLevel == level,
                                onClick = { onChangeLogLevel(level) }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(level, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🧩 Atskirų komponentų stebėjimo lygiai (Logger Scopes)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                ) {
                    items(componentLoggers) { (name, level) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                levels.forEach { lvl ->
                                    FilterChip(
                                        selected = level == lvl,
                                        onClick = { onChangeComponentLogLevel(name, lvl) },
                                        label = { Text(lvl, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// Sub-component for Tab 3
@Composable
private fun RenderRotationConfigTab(
    config: LogRotationConfigDTO,
    onSaveConfig: (LogRotationConfigDTO) -> Unit
) {
    var splitByDays by remember { mutableStateOf(config.splitByDays) }
    var maxFileSizeMb by remember { mutableStateOf(config.maxFileSizeMb.toString()) }
    var maxTotalStorageMb by remember { mutableStateOf(config.maxTotalStorageMb.toString()) }
    var dateFormatPattern by remember { mutableStateOf(config.dateFormatPattern) }
    var captureStackTrace by remember { mutableStateOf(config.captureStackTrace) }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🗓️ Automatinis Logų Skaidymas Dienomis (Daily Rotation)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Įjungti automatinį žurnalo failų skaidymą dienomis", fontWeight = FontWeight.SemiBold)
                        Text("Kiekvieną dieną sukuriamas atskiras stebėjimo failas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = splitByDays, onCheckedChange = { splitByDays = it })
                }

                OutlinedTextField(
                    value = dateFormatPattern,
                    onValueChange = { dateFormatPattern = it },
                    label = { Text("Dienos failo pavadinimo šablonas (Pattern)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📏 Dydžio Ribojimas ir Klaidų Sekimas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = maxFileSizeMb,
                        onValueChange = { maxFileSizeMb = it },
                        label = { Text("Maksimalus vieno failo dydis (MB)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxTotalStorageMb,
                        onValueChange = { maxTotalStorageMb = it },
                        label = { Text("Maksimali bendra vieta (MB)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fiksuoti klaidų išrašus (Stack Trace Capture)", fontWeight = FontWeight.SemiBold)
                        Text("Registruoti pilną kodo klaidų sekimo išrašą prie ERROR įrašų", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = captureStackTrace, onCheckedChange = { captureStackTrace = it })
                }

                Button(
                    onClick = {
                        onSaveConfig(
                            LogRotationConfigDTO(
                                splitByDays = splitByDays,
                                maxFileSizeMb = maxFileSizeMb.toIntOrNull() ?: 50,
                                maxTotalStorageMb = maxTotalStorageMb.toIntOrNull() ?: 500,
                                dateFormatPattern = dateFormatPattern.trim(),
                                captureStackTrace = captureStackTrace
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Išsaugoti rotacijos nustatymus")
                }
            }
        }
    }
}

// Sub-component for Tab 4
@Composable
private fun RenderRetentionConfigTab(
    config: LogRetentionConfigDTO,
    onSaveConfig: (LogRetentionConfigDTO) -> Unit,
    onCleanupNow: () -> Unit
) {
    var retentionDays by remember { mutableStateOf(config.retentionDays.toString()) }
    var autoCleanupEnabled by remember { mutableStateOf(config.autoCleanupEnabled) }
    var cleanupScheduleCron by remember { mutableStateOf(config.cleanupScheduleCron) }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📊 Disko Vietos Naudojimas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Šiuo metu naudojama žurnalams:", fontWeight = FontWeight.SemiBold)
                    Text("${config.currentStorageUsedMb} MB / 500 MB", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }

                LinearProgressIndicator(
                    progress = { (config.currentStorageUsedMb / 500.0).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🧹 Istorijos Saugojimas ir Periodinis Valymas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = retentionDays,
                    onValueChange = { retentionDays = it },
                    label = { Text("Saugojimo trukmė (Dienomis)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatinis senų įrašų valymas", fontWeight = FontWeight.SemiBold)
                        Text("Automatiškai šalinti senesnius nei nurodyta dienų žurnalus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = autoCleanupEnabled, onCheckedChange = { autoCleanupEnabled = it })
                }

                OutlinedTextField(
                    value = cleanupScheduleCron,
                    onValueChange = { cleanupScheduleCron = it },
                    label = { Text("Valymo tvarkaraštis (CRON Pattern)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            onSaveConfig(
                                LogRetentionConfigDTO(
                                    retentionDays = retentionDays.toIntOrNull() ?: 30,
                                    autoCleanupEnabled = autoCleanupEnabled,
                                    cleanupScheduleCron = cleanupScheduleCron.trim(),
                                    currentStorageUsedMb = config.currentStorageUsedMb
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Išsaugoti politikos nustatymus")
                    }

                    Button(
                        onClick = onCleanupNow,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("🧹 Valyti senesnius įrašus dabar")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RastvedysScreenPreview() {
    VedlysTheme {
        Surface {
            RastvedysScreen(
                apiClient = ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                onBack = {},
                onHome = {}
            )
        }
    }
}

