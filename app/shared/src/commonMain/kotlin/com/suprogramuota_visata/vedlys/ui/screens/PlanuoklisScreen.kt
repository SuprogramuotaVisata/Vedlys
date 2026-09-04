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
import com.suprogramuota_visata.api.domain.models.JobDefinitionDTO
import com.suprogramuota_visata.api.domain.models.JobExecutionLogDTO
import com.suprogramuota_visata.api.domain.models.TaskEventLinkDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme

@Composable
fun PlanuoklisScreen(
    apiClient: ApiSvClient,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }

    // Tab 1 State: Jobs
    var jobs by remember { mutableStateOf<List<JobDefinitionDTO>>(emptyList()) }
    var isLoadingJobs by remember { mutableStateOf(false) }
    var jobsErrorMessage by remember { mutableStateOf<String?>(null) }
    var jobsSearchQuery by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showCreateJobDialog by remember { mutableStateOf(false) }

    // Tab 2 State: Task Event Links
    var taskLinks by remember {
        mutableStateOf(
            listOf(
                TaskEventLinkDTO(
                    id = "1",
                    jobName = "SyncLicensesJob",
                    triggerCondition = "ON_SUCCESS",
                    targetEventType = "GENERUOTI_ĮVYKĮ",
                    targetPayload = "LICENSE_SYNCED",
                    isActive = true,
                    description = "Po licencijų sinchronizavimo išspinduliuoti Seklio įvykį LICENSE_SYNCED"
                ),
                TaskEventLinkDTO(
                    id = "2",
                    jobName = "CleanupLogsJob",
                    triggerCondition = "ON_FAILURE",
                    targetEventType = "PRANEŠTI_EL_PAŠTU",
                    targetPayload = "admin@imone.lt",
                    isActive = true,
                    description = "Jei žurnalų valymas nepavyko, informuoti administratorių"
                ),
                TaskEventLinkDTO(
                    id = "3",
                    jobName = "DailyReportJob",
                    triggerCondition = "ON_COMPLETE",
                    targetEventType = "PALEISTI_SKRIPTĄ",
                    targetPayload = "script_enrich_address",
                    isActive = false,
                    description = "Užbaigus dienos ataskaitą paleisti adresų atnaujinimo skriptą"
                ),
                TaskEventLinkDTO(
                    id = "4",
                    jobName = "ImportProductsTask",
                    triggerCondition = "ON_SUCCESS",
                    targetEventType = "VYKDYTI_SKRIPTĄ",
                    targetPayload = "Buhalterinis_Export.ps1",
                    isActive = true,
                    description = "Paleisti PowerShell skriptą po prekių importo pabaigos"
                )
            )
        )
    }
    var linksSearchQuery by remember { mutableStateOf("") }
    var showLinkDialog by remember { mutableStateOf(false) }
    var editingLink by remember { mutableStateOf<TaskEventLinkDTO?>(null) }
    var selectedTaskLink by remember { mutableStateOf<TaskEventLinkDTO?>(null) }
    var showCreateTaskLinkDialog by remember { mutableStateOf(false) }

    // Tab 3 State: Execution History Logs
    var historyLogs by remember {
        mutableStateOf(
            listOf(
                JobExecutionLogDTO(
                    id = "101",
                    jobName = "SyncLicensesJob",
                    timestamp = System.currentTimeMillis() - 600000,
                    durationMs = 1420,
                    status = "SUCCESS",
                    details = "Sinchronizuotos 4 aktyvios licencijos. Galiojimas patikrintas."
                ),
                JobExecutionLogDTO(
                    id = "102",
                    jobName = "CleanupLogsJob",
                    timestamp = System.currentTimeMillis() - 3600000,
                    durationMs = 850,
                    status = "SUCCESS",
                    details = "Pašalinti senesni nei 30 dienų stebėjimo įrašai (Viso: 124 įrašai)."
                ),
                JobExecutionLogDTO(
                    id = "103",
                    jobName = "DailyReportJob",
                    timestamp = System.currentTimeMillis() - 86400000,
                    durationMs = 3100,
                    status = "SUCCESS",
                    details = "Dienos ataskaita sėkmingai sugeneruota ir išsiųsta į Webhook."
                )
            )
        )
    }
    var historySearchQuery by remember { mutableStateOf("") }
    var selectedHistoryLog by remember { mutableStateOf<JobExecutionLogDTO?>(null) }

    fun fetchJobs() {
        coroutineScope.launch {
            isLoadingJobs = true
            jobsErrorMessage = null
            val result = apiClient.schedulerRepository.getJobs()
            when (result) {
                is ApiResult.Success -> {
                    jobs = result.data
                    isLoadingJobs = false
                }
                is ApiResult.Error -> {
                    jobsErrorMessage = result.message
                    isLoadingJobs = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchJobs()
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = "Planuoklis (Automatinių užduočių planavimas ir įvykiai)",
                onBack = onBack,
                onHome = onHome,
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { fetchJobs() }) {
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
                    text = { Text("⏰ Planuotojo Užduotys", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🔗 Užduočių Sietuvai ir Įvykiai (${taskLinks.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📊 Vykdymo Istorija (${historyLogs.size})", fontWeight = FontWeight.Bold) }
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
                    0 -> RenderJobsTab(
                        jobs = jobs,
                        isLoading = isLoadingJobs,
                        errorMessage = jobsErrorMessage,
                        searchQuery = jobsSearchQuery,
                        onSearchChange = { jobsSearchQuery = it },
                        onToggleJob = { jobName, newActive ->
                            coroutineScope.launch {
                                apiClient.schedulerRepository.toggleJob(jobName, newActive)
                                fetchJobs()
                            }
                        },
                        onTriggerJob = { jobName ->
                            coroutineScope.launch {
                                when (val res = apiClient.schedulerRepository.triggerJob(jobName)) {
                                    is ApiResult.Success -> {
                                        statusMessage = "Užduotis '$jobName' sėkmingai paleista fone."
                                        fetchJobs()
                                    }
                                    is ApiResult.Error -> {
                                        jobsErrorMessage = res.message
                                    }
                                }
                            }
                        },
                        onOpenCreateDialog = { showCreateJobDialog = true },
                        dateFormat = dateFormat
                    )

                    1 -> RenderTaskLinksTab(
                        links = taskLinks,
                        jobs = jobs,
                        searchQuery = linksSearchQuery,
                        onSearchChange = { linksSearchQuery = it },
                        onToggleLink = { linkId ->
                            taskLinks = taskLinks.map { if (it.id == linkId) it.copy(isActive = !it.isActive) else it }
                        },
                        onDeleteLink = { linkId ->
                            taskLinks = taskLinks.filter { it.id != linkId }
                        },
                        onEditLink = { link ->
                            editingLink = link
                            showLinkDialog = true
                        },
                        onOpenCreateDialog = {
                            editingLink = null
                            showLinkDialog = true
                        }
                    )

                    2 -> RenderHistoryTab(
                        historyLogs = historyLogs,
                        searchQuery = historySearchQuery,
                        onSearchChange = { historySearchQuery = it },
                        onLogClick = { selectedHistoryLog = it },
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }

    // Detail Execution History Dialog
    selectedHistoryLog?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedHistoryLog = null },
            title = { Text("Vykdymo įrašas: ${log.jobName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Būsena: ${log.status}", fontWeight = FontWeight.Bold, color = if (log.status == "SUCCESS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    Text("Laikas: ${dateFormat.format(Date(log.timestamp))}")
                    Text("Trukmė: ${log.durationMs} ms")
                    HorizontalDivider()
                    Text("Vykdymo rezultatai / Išrašas:", fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedHistoryLog = null }) {
                    Text("Uždaryti")
                }
            }
        )
    }

    // Create New Job Dialog
    if (showCreateJobDialog) {
        var jobName by remember { mutableStateOf("NaujaFonoUzduotis") }
        var cronExpr by remember { mutableStateOf("0 0 * * *") }
        var description by remember { mutableStateOf("Periodinė fono užduotis") }

        AlertDialog(
            onDismissRequest = { showCreateJobDialog = false },
            title = { Text("➕ Registruoti naują fono užduotį") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = jobName,
                        onValueChange = { jobName = it },
                        label = { Text("Užduoties pavadinimas (Unikalus)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cronExpr,
                        onValueChange = { cronExpr = it },
                        label = { Text("CRON Išraiška (pvz., 0 0 * * * - Kasnakt)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Užduoties aprašymas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newJob = JobDefinitionDTO(
                            name = jobName.trim(),
                            cronExpression = cronExpr.trim(),
                            isActive = true,
                            description = description.trim()
                        )
                        jobs = jobs + newJob
                        showCreateJobDialog = false
                    }
                ) {
                    Text("Išsaugoti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateJobDialog = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }

    // Task Event Link Add/Edit Dialog
    if (showLinkDialog) {
        val link = editingLink
        var selectedJobName by remember { mutableStateOf(link?.jobName ?: if (jobs.isNotEmpty()) jobs.first().name else "SyncLicensesJob") }
        var triggerCondition by remember { mutableStateOf(link?.triggerCondition ?: "ON_SUCCESS") }
        var targetEventType by remember { mutableStateOf(link?.targetEventType ?: "GENERUOTI_ĮVYKĮ") }
        var targetPayload by remember { mutableStateOf(link?.targetPayload ?: "JOB_COMPLETED") }
        var description by remember { mutableStateOf(link?.description ?: "") }

        val conditions = listOf(
            "ON_SUCCESS" to "✅ Pasibaigus sėkmingai",
            "ON_FAILURE" to "❌ Įvykus klaidai",
            "EVERY_RUN" to "🔄 Kiekvieno vykdymo metu"
        )

        val targetTypes = listOf(
            "GENERUOTI_ĮVYKĮ" to "⚡ Generuoti Seklio įvykį",
            "PRANEŠTI_EL_PAŠTU" to "📧 Siųsti ataskaitą El. paštu",
            "ŠAUKTI_WEBHOOK" to "🌐 Šaukti Webhook URL",
            "VYKDYTI_SKRIPTĄ" to "🐍 Paleisti Python / PowerShell Skriptą"
        )

        var linkValidationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text(if (link == null) "🔗 Naujas Užduoties-Įvykio Sietuvas" else "✏️ Redaguoti sietuvą") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    linkValidationError?.let { err ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Text("1. Pasirinkite Planuotojo užduotį:", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = selectedJobName,
                        onValueChange = {
                            selectedJobName = it
                            linkValidationError = null
                        },
                        label = { Text("Užduoties pavadinimas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("2. Sietuvo sąlyga (Kada trigerinti?):", fontWeight = FontWeight.Bold)
                    var expandedCondition by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedCondition = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(conditions.find { it.first == triggerCondition }?.second ?: triggerCondition)
                        }
                        DropdownMenu(expanded = expandedCondition, onDismissRequest = { expandedCondition = false }) {
                            conditions.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        triggerCondition = code
                                        expandedCondition = false
                                        linkValidationError = null
                                    }
                                )
                            }
                        }
                    }

                    Text("3. Susietas Veiksmas / Įvykis:", fontWeight = FontWeight.Bold)
                    var expandedTarget by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedTarget = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(targetTypes.find { it.first == targetEventType }?.second ?: targetEventType)
                        }
                        DropdownMenu(expanded = expandedTarget, onDismissRequest = { expandedTarget = false }) {
                            targetTypes.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        targetEventType = code
                                        expandedTarget = false
                                        linkValidationError = null
                                    }
                                )
                            }
                        }
                    }

                    Text("4. Taikinio parametrai / Payload:", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = targetPayload,
                        onValueChange = {
                            targetPayload = it
                            linkValidationError = null
                        },
                        label = {
                            Text(
                                when (targetEventType) {
                                    "GENERUOTI_ĮVYKĮ" -> "Įvykio tipo pavadinimas (pvz., LICENSE_SYNCED)"
                                    "PRANEŠTI_EL_PAŠTU" -> "Gavėjo El. pašto adresas"
                                    "ŠAUKTI_WEBHOOK" -> "Webhook URL adresas (https://...)"
                                    "VYKDYTI_SKRIPTĄ" -> "Skripto pavadinimas (pvz., Export.ps1 / Export.py)"
                                    else -> "Reikšmė"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Sietuvo aprašymas / komentaras") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetEventType == "VYKDYTI_SKRIPTĄ") {
                            val lang = if (targetPayload.endsWith(".py", ignoreCase = true)) "PYTHON" else "POWERSHELL"
                            val check = com.suprogramuota_visata.vedlys.utils.ScriptValidator.validateScript(targetPayload, lang, targetPayload)
                            if (check is com.suprogramuota_visata.vedlys.utils.ScriptValidationResult.Invalid) {
                                linkValidationError = check.reason
                                return@Button
                            }
                        }

                        val newLink = TaskEventLinkDTO(
                            id = link?.id ?: UUID.randomUUID().toString(),
                            jobName = selectedJobName.trim(),
                            triggerCondition = triggerCondition,
                            targetEventType = targetEventType,
                            targetPayload = targetPayload.trim(),
                            isActive = link?.isActive ?: true,
                            description = description.trim()
                        )
                        taskLinks = if (link == null) taskLinks + newLink
                                    else taskLinks.map { if (it.id == link.id) newLink else it }
                        showLinkDialog = false
                    }
                ) {
                    Text("Išsaugoti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }
}

// Sub-component for Tab 1
@Composable
private fun RenderJobsTab(
    jobs: List<JobDefinitionDTO>,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleJob: (String, Boolean) -> Unit,
    onTriggerJob: (String) -> Unit,
    onOpenCreateDialog: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    val filteredJobs = remember(jobs, searchQuery) {
        if (searchQuery.isBlank()) jobs
        else jobs.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            (it.description?.contains(searchQuery, ignoreCase = true) == true) ||
            it.cronExpression.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Ieškoti užduočių pagal pavadinimą, CRON ar aprašymą...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            )

            Button(
                onClick = onOpenCreateDialog,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("➕ Nauja Užduotis")
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
        } else if (filteredJobs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Automatinių užduočių nerasta.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredJobs) { job ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = job.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val desc = job.description
                                    if (!desc.isNullOrBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (job.isActive) "Aktyvi" else "Išjungta",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (job.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Switch(
                                        checked = job.isActive,
                                        onCheckedChange = { newActive -> onToggleJob(job.name, newActive) }
                                    )
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "CRON: ${job.cronExpression}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    val lastRun = job.lastRun
                                    if (lastRun != null) {
                                        Text(
                                            text = "Paskutinis vykdymas: ${dateFormat.format(Date(lastRun))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onTriggerJob(job.name) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Vykdyti dabar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-component for Tab 2
@Composable
private fun RenderTaskLinksTab(
    links: List<TaskEventLinkDTO>,
    jobs: List<JobDefinitionDTO>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleLink: (String) -> Unit,
    onDeleteLink: (String) -> Unit,
    onEditLink: (TaskEventLinkDTO) -> Unit,
    onOpenCreateDialog: () -> Unit
) {
    val filteredLinks = remember(links, searchQuery) {
        if (searchQuery.isBlank()) links
        else links.filter {
            it.jobName.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.targetEventType.contains(searchQuery, ignoreCase = true) ||
            it.targetPayload.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Ieškoti sietuvų pagal užduotį, veiksmą ar aprašymą...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            )

            Button(
                onClick = onOpenCreateDialog,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("🔗 Naujas Sietuvas")
            }
        }

        if (filteredLinks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Užduočių sietuvų nerasta. Pridėkite naują paspaudę „🔗 Naujas Sietuvas“.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLinks) { link ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "⏰ ${link.jobName}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = when (link.targetEventType) {
                                                "GENERUOTI_ĮVYKĮ" -> "⚡ ĮVYKIS"
                                                "PRANEŠTI_EL_PAŠTU" -> "📧 EMAIL"
                                                "ŠAUKTI_WEBHOOK" -> "🌐 WEBHOOK"
                                                "VYKDYTI_SKRIPTĄ" -> "🐍 SKRIPTAS"
                                                else -> link.targetEventType
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (link.isActive) "Aktyvus" else "Išjungtas",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (link.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Switch(
                                        checked = link.isActive,
                                        onCheckedChange = { onToggleLink(link.id) }
                                    )
                                }
                            }

                            if (link.description.isNotBlank()) {
                                Text(
                                    text = link.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Sąlyga: ${
                                            when (link.triggerCondition) {
                                                "ON_SUCCESS" -> "✅ Pasibaigus sėkmingai"
                                                "ON_FAILURE" -> "❌ Įvykus klaidai"
                                                else -> "🔄 Kiekvieno vykdymo metu"
                                            }
                                        }",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Taikinys: ${link.targetPayload}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row {
                                    IconButton(onClick = { onEditLink(link) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Redaguoti")
                                    }
                                    IconButton(onClick = { onDeleteLink(link.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Šalinti", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-component for Tab 3
@Composable
private fun RenderHistoryTab(
    historyLogs: List<JobExecutionLogDTO>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLogClick: (JobExecutionLogDTO) -> Unit,
    dateFormat: SimpleDateFormat
) {
    val filteredLogs = remember(historyLogs, searchQuery) {
        if (searchQuery.isBlank()) historyLogs
        else historyLogs.filter {
            it.jobName.contains(searchQuery, ignoreCase = true) ||
            it.details.contains(searchQuery, ignoreCase = true) ||
            it.status.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Ieškoti vykdymo žurnaluose pagal užduotį ar rezultatą...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (filteredLogs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Vykdymo žurnalų nerasta.",
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
                    val isSuccess = log.status == "SUCCESS"
                    val statusBg = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer

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
                                        color = statusBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (isSuccess) "✅ SĖKMĖ" else "❌ KLAIDA",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = log.jobName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = log.details,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "${log.durationMs} ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PlanuoklisScreenPreview() {
    VedlysTheme {
        Surface {
            PlanuoklisScreen(
                apiClient = ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                onBack = {},
                onHome = {}
            )
        }
    }
}

