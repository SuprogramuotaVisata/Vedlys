package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.EventDTO
import com.suprogramuota_visata.api.domain.models.TracerRuleDTO
import com.suprogramuota_visata.api.domain.models.TracerScriptDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme

@Composable
fun SeklysScreen(
    apiClient: ApiSvClient,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }

    // State for Tab 1: Events Log
    var events by remember { mutableStateOf<List<EventDTO>>(emptyList()) }
    var isLoadingEvents by remember { mutableStateOf(false) }
    var eventsErrorMessage by remember { mutableStateOf<String?>(null) }
    var eventsSearchQuery by remember { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf<EventDTO?>(null) }
    var showCreateEventDialog by remember { mutableStateOf(false) }

    // State for Tab 2: Rules Engine
    var rules by remember {
        mutableStateOf(
            listOf(
                TracerRuleDTO(
                    id = "1",
                    eventType = "USER_REGISTERED",
                    condition = "ALL",
                    actionType = "SIŲSTI_EMAIL",
                    actionTarget = "sveikinimai@imone.lt",
                    isActive = true,
                    description = "Siųsti pasveikinimo el. laišką naujai užsiregistravusiam vartotojui"
                ),
                TracerRuleDTO(
                    id = "2",
                    eventType = "PRE_TYPE_CREATE",
                    condition = "type == 'ProductSv'",
                    actionType = "VALIDUOTI_SKRIPTU",
                    actionTarget = "script_validate_sku",
                    isActive = true,
                    description = "Paleisti SKU validacijos skriptą prieš sukuriant prekę"
                ),
                TracerRuleDTO(
                    id = "3",
                    eventType = "SYSTEM_ERROR",
                    condition = "ALL",
                    actionType = "PALEISTI_SKRIPTA",
                    actionTarget = "Klaidos_Pranesiklis.py",
                    isActive = true,
                    description = "Vykdyti Python skriptą įvykus sisteminei klaidai"
                )
            )
        )
    }
    var rulesSearchQuery by remember { mutableStateOf("") }
    var showRuleDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<TracerRuleDTO?>(null) }

    // State for Tab 3: Scripts Management
    var scripts by remember {
        mutableStateOf(
            listOf(
                TracerScriptDTO(
                    id = "1",
                    name = "Klaidos_Pranesiklis.py",
                    language = "PYTHON",
                    scriptPathOrContent = "C:\\Scripts\\Klaidos_Pranesiklis.py",
                    description = "Siunčia Telegram pranešimą administratoriui apie sistemines klaidas",
                    isActive = true
                ),
                TracerScriptDTO(
                    id = "2",
                    name = "Buhalterinis_Export.ps1",
                    language = "POWERSHELL",
                    scriptPathOrContent = "C:\\Scripts\\Buhalterinis_Export.ps1",
                    description = "Eksportuoja operacijas į XML failą buhalterinei sistemai",
                    isActive = true
                )
            )
        )
    }
    var scriptsSearchQuery by remember { mutableStateOf("") }
    var showScriptDialog by remember { mutableStateOf(false) }
    var editingScript by remember { mutableStateOf<TracerScriptDTO?>(null) }
    var scriptStatusMessage by remember { mutableStateOf<String?>(null) }

    fun fetchEvents() {
        coroutineScope.launch {
            isLoadingEvents = true
            eventsErrorMessage = null
            when (val result = apiClient.tracerRepository.getEvents(limit = 100)) {
                is ApiResult.Success -> {
                    events = result.data
                    isLoadingEvents = false
                }
                is ApiResult.Error -> {
                    eventsErrorMessage = result.message
                    isLoadingEvents = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchEvents()
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = "Seklys (Įvykių sekimas ir automatizavimas)",
                onBack = onBack,
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { fetchEvents() }) {
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
                    text = { Text("📋 Įvykių žurnalas", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("⚙️ Įvykių Taisyklės (${rules.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📜 Skriptų valdymas (${scripts.size})", fontWeight = FontWeight.Bold) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                when (selectedTab) {
                    0 -> RenderEventsTab(
                        events = events,
                        isLoading = isLoadingEvents,
                        errorMessage = eventsErrorMessage,
                        searchQuery = eventsSearchQuery,
                        onSearchChange = { eventsSearchQuery = it },
                        onEventClick = { selectedEvent = it },
                        onOpenCreateDialog = { showCreateEventDialog = true },
                        dateFormat = dateFormat
                    )

                    1 -> RenderRulesTab(
                        rules = rules,
                        searchQuery = rulesSearchQuery,
                        onSearchChange = { rulesSearchQuery = it },
                        onToggleRule = { ruleId ->
                            rules = rules.map { if (it.id == ruleId) it.copy(isActive = !it.isActive) else it }
                        },
                        onDeleteRule = { ruleId ->
                            rules = rules.filter { it.id != ruleId }
                        },
                        onEditRule = { rule ->
                            editingRule = rule
                            showRuleDialog = true
                        },
                        onOpenCreateDialog = {
                            editingRule = null
                            showRuleDialog = true
                        }
                    )

                    2 -> RenderScriptsTab(
                        scripts = scripts,
                        searchQuery = scriptsSearchQuery,
                        onSearchChange = { scriptsSearchQuery = it },
                        onToggleScript = { scriptId ->
                            scripts = scripts.map { if (it.id == scriptId) it.copy(isActive = !it.isActive) else it }
                        },
                        onDeleteScript = { scriptId ->
                            scripts = scripts.filter { it.id != scriptId }
                        },
                        onEditScript = { script ->
                            editingScript = script
                            showScriptDialog = true
                        },
                        onOpenCreateDialog = {
                            editingScript = null
                            showScriptDialog = true
                        },
                        onTestRunScript = { script ->
                            val check = com.suprogramuota_visata.vedlys.utils.ScriptValidator.validateScript(script.name, script.language, script.scriptPathOrContent)
                            if (check is com.suprogramuota_visata.vedlys.utils.ScriptValidationResult.Invalid) {
                                scriptStatusMessage = "❌ Užblokuota: ${check.reason}"
                            } else {
                                scriptStatusMessage = "✅ Skriptas '${script.name}' patikrintas ir saugus vykdymui!"
                            }
                        },
                        statusMessage = scriptStatusMessage,
                        onClearStatusMessage = { scriptStatusMessage = null }
                    )
                }
            }
        }
    }

    // Detail Event Dialog
    selectedEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text("Įvykio informacija: ${event.type}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Servisas: ${event.serviceName}")
                    Text("Instancijos ID: ${event.instanceId}")
                    Text("Laikas: ${dateFormat.format(Date(event.timestamp))}")
                    HorizontalDivider()
                    Text("Įvykio duomenys (JSON):", fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = event.data,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEvent = null }) {
                    Text("Uždaryti")
                }
            }
        )
    }

    // Create Test Event Dialog
    if (showCreateEventDialog) {
        var eventType by remember { mutableStateOf("USER_REGISTERED") }
        var serviceName by remember { mutableStateOf("VedlysDesktop") }
        var eventData by remember { mutableStateOf("{\"email\":\"vartotojas@imone.lt\",\"name\":\"Jonas\"}") }

        AlertDialog(
            onDismissRequest = { showCreateEventDialog = false },
            title = { Text("Publikuoti bandomąjį įvykį") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = eventType,
                        onValueChange = { eventType = it },
                        label = { Text("Įvykio tipas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Servisas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = eventData,
                        onValueChange = { eventData = it },
                        label = { Text("Duomenys (JSON / Tekstas)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            apiClient.tracerRepository.publishEvent(
                                EventDTO(
                                    type = eventType.trim(),
                                    serviceName = serviceName.trim(),
                                    instanceId = "VEDLYS_DESKTOP",
                                    data = eventData.trim()
                                )
                            )
                            showCreateEventDialog = false
                            fetchEvents()
                        }
                    }
                ) {
                    Text("Siųsti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateEventDialog = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }

    // Rule Add/Edit Dialog
    if (showRuleDialog) {
        val rule = editingRule
        var eventType by remember { mutableStateOf(rule?.eventType ?: "USER_REGISTERED") }
        var condition by remember { mutableStateOf(rule?.condition ?: "ALL") }
        var actionType by remember { mutableStateOf(rule?.actionType ?: "SIŲSTI_EMAIL") }
        var actionTarget by remember { mutableStateOf(rule?.actionTarget ?: "pagalba@imone.lt") }
        var description by remember { mutableStateOf(rule?.description ?: "") }

        val eventTypes = listOf(
            "USER_REGISTERED" to "Vartotojo registracija",
            "USER_LOGIN" to "Vartotojo prisijungimas",
            "USER_LOGOUT" to "Vartotojo išsidokavimas",
            "PRE_TYPE_CREATE" to "Prieš sukuriant prekės tipą (Valdymas)",
            "TYPE_CREATED" to "Sukurtas prekės tipas",
            "PRE_TRANSACTION_CREATE" to "Prieš išsaugant operaciją (Valdymas)",
            "TRANSACTION_CREATED" to "Sukurta operacija",
            "SYSTEM_ERROR" to "Sisteminė klaida"
        )

        val actionTypes = listOf(
            "SIŲSTI_EMAIL" to "📧 Siųsti El. paštą / Pranešimą",
            "BLOKUOTI_OPERACIJĄ" to "🛑 Blokuoti operaciją (Pre-Event)",
            "WEBHOOK_URL" to "🌐 Šaukti Webhook URL",
            "PALEISTI_SKRIPTA" to "🐍 Paleisti Python / PowerShell Skriptą"
        )

        AlertDialog(
            onDismissRequest = { showRuleDialog = false },
            title = { Text(if (rule == null) "➕ Nauja įvykių taisyklė" else "✏️ Redaguoti taisyklę") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("1. Triggeris (Kada vykdyti?):", fontWeight = FontWeight.Bold)
                    var expandedEvent by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedEvent = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(eventTypes.find { it.first == eventType }?.second ?: eventType)
                        }
                        DropdownMenu(expanded = expandedEvent, onDismissRequest = { expandedEvent = false }) {
                            eventTypes.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text("$label ($code)") },
                                    onClick = {
                                        eventType = code
                                        expandedEvent = false
                                    }
                                )
                            }
                        }
                    }

                    Text("2. Sąlyga (Kada taikyti?):", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { condition = it },
                        label = { Text("Sąlyga (pvz., ALL arba DATA_CONTAINS:Tekstas)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("3. Veiksmas (Ką atlikti?):", fontWeight = FontWeight.Bold)
                    var expandedAction by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedAction = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(actionTypes.find { it.first == actionType }?.second ?: actionType)
                        }
                        DropdownMenu(expanded = expandedAction, onDismissRequest = { expandedAction = false }) {
                            actionTypes.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        actionType = code
                                        expandedAction = false
                                    }
                                )
                            }
                        }
                    }

                    Text("4. Veiksmo taikinys / parametrai:", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = actionTarget,
                        onValueChange = { actionTarget = it },
                        label = {
                            Text(
                                when (actionType) {
                                    "SIŲSTI_EMAIL" -> "El. pašto adresas arba šablono kodo pavadinimas"
                                    "BLOKUOTI_OPERACIJĄ" -> "Pranešimas vartotojui apie blokavimą"
                                    "WEBHOOK_URL" -> "Webhook URL adresas (https://...)"
                                    "PALEISTI_SKRIPTA" -> "Skripto pavadinimas (pvz., Pranesiklis.py)"
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
                        label = { Text("Taisyklės aprašymas / komentaras") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newRule = TracerRuleDTO(
                            id = rule?.id ?: UUID.randomUUID().toString(),
                            eventType = eventType,
                            condition = condition,
                            actionType = actionType,
                            actionTarget = actionTarget,
                            isActive = rule?.isActive ?: true,
                            description = description
                        )
                        rules = if (rule == null) rules + newRule
                                else rules.map { if (it.id == rule.id) newRule else it }
                        showRuleDialog = false
                    }
                ) {
                    Text("Išsaugoti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRuleDialog = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }

    // Script Add/Edit Dialog
    if (showScriptDialog) {
        val script = editingScript
        var name by remember { mutableStateOf(script?.name ?: "Naujas_Skriptas.py") }
        var language by remember { mutableStateOf(script?.language ?: "PYTHON") }
        var scriptPathOrContent by remember { mutableStateOf(script?.scriptPathOrContent ?: "C:\\Scripts\\Skriptas.py") }
        var description by remember { mutableStateOf(script?.description ?: "") }
        var validationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showScriptDialog = false },
            title = { Text(if (script == null) "➕ Pridėti naują skriptą" else "✏️ Redaguoti skriptą") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    validationError?.let { err ->
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

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            validationError = null
                        },
                        label = { Text("Skripto pavadinimas (pvz., Skriptas.py / Skriptas.ps1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kalba:", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = language == "PYTHON",
                                onClick = {
                                    language = "PYTHON"
                                    validationError = null
                                }
                            )
                            Text("Python 🐍")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = language == "POWERSHELL",
                                onClick = {
                                    language = "POWERSHELL"
                                    validationError = null
                                }
                            )
                            Text("PowerShell ⚡")
                        }
                    }

                    OutlinedTextField(
                        value = scriptPathOrContent,
                        onValueChange = {
                            scriptPathOrContent = it
                            validationError = null
                        },
                        label = { Text("Failo kelias arba kodo turinys") },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Skripto aprašymas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val check = com.suprogramuota_visata.vedlys.utils.ScriptValidator.validateScript(name, language, scriptPathOrContent)
                        if (check is com.suprogramuota_visata.vedlys.utils.ScriptValidationResult.Invalid) {
                            validationError = check.reason
                            return@Button
                        }

                        val newScript = TracerScriptDTO(
                            id = script?.id ?: UUID.randomUUID().toString(),
                            name = name,
                            language = language,
                            scriptPathOrContent = scriptPathOrContent,
                            description = description,
                            isActive = script?.isActive ?: true
                        )
                        scripts = if (script == null) scripts + newScript
                                  else scripts.map { if (it.id == script.id) newScript else it }
                        showScriptDialog = false
                    }
                ) {
                    Text("Išsaugoti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScriptDialog = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }
}

// Sub-component for Tab 1
@Composable
private fun RenderEventsTab(
    events: List<EventDTO>,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onEventClick: (EventDTO) -> Unit,
    onOpenCreateDialog: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    val filteredEvents = remember(events, searchQuery) {
        if (searchQuery.isBlank()) events
        else events.filter {
            it.type.contains(searchQuery, ignoreCase = true) ||
            it.serviceName.contains(searchQuery, ignoreCase = true) ||
            it.data.contains(searchQuery, ignoreCase = true)
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
                placeholder = { Text("Ieškoti įvykių pagal tipą, servisą ar duomenis...") },
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
                Text("Publikuoti bandomąjį įvykį")
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
        } else if (filteredEvents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Registruotų įvykių nerasta.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEvents) { event ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEventClick(event) },
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
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = event.type,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = event.serviceName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = event.data,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(event.timestamp)),
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
private fun RenderRulesTab(
    rules: List<TracerRuleDTO>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleRule: (String) -> Unit,
    onDeleteRule: (String) -> Unit,
    onEditRule: (TracerRuleDTO) -> Unit,
    onOpenCreateDialog: () -> Unit
) {
    val filteredRules = remember(rules, searchQuery) {
        if (searchQuery.isBlank()) rules
        else rules.filter {
            it.eventType.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.actionType.contains(searchQuery, ignoreCase = true) ||
            it.actionTarget.contains(searchQuery, ignoreCase = true)
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
                placeholder = { Text("Ieškoti taisyklių pagal įvykį, veiksmą ar aprašymą...") },
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
                Text("➕ Nauja Taisyklė")
            }
        }

        if (filteredRules.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Taisyklių nerasta. Sukurkite naują paspaudę „➕ Nauja Taisyklė“.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRules) { rule ->
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
                                            text = rule.eventType,
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
                                            text = when (rule.actionType) {
                                                "SIŲSTI_EMAIL" -> "📧 EMAIL"
                                                "BLOKUOTI_OPERACIJĄ" -> "🛑 BLOKAVIMAS"
                                                "WEBHOOK_URL" -> "🌐 WEBHOOK"
                                                "PALEISTI_SKRIPTA" -> "🐍 SKRIPTAS"
                                                else -> rule.actionType
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (rule.isActive) "Aktyvi" else "Išjungta",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rule.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Switch(
                                        checked = rule.isActive,
                                        onCheckedChange = { onToggleRule(rule.id) }
                                    )
                                }
                            }

                            if (rule.description.isNotBlank()) {
                                Text(
                                    text = rule.description,
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
                                        text = "Sąlyga: ${rule.condition}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Taikinys: ${rule.actionTarget}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row {
                                    IconButton(onClick = { onEditRule(rule) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Redaguoti")
                                    }
                                    IconButton(onClick = { onDeleteRule(rule.id) }) {
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
private fun RenderScriptsTab(
    scripts: List<TracerScriptDTO>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleScript: (String) -> Unit,
    onDeleteScript: (String) -> Unit,
    onEditScript: (TracerScriptDTO) -> Unit,
    onOpenCreateDialog: () -> Unit,
    onTestRunScript: (TracerScriptDTO) -> Unit,
    statusMessage: String?,
    onClearStatusMessage: () -> Unit
) {
    val filteredScripts = remember(scripts, searchQuery) {
        if (searchQuery.isBlank()) scripts
        else scripts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.language.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        statusMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = msg, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    TextButton(onClick = onClearStatusMessage) {
                        Text("Gerai")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Ieškoti skriptų pagal pavadinimą, kalbą ar aprašymą...") },
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
                Text("➕ Pridėti Skriptą")
            }
        }

        if (filteredScripts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Skriptų nerasta. Pridėkite naują paspaudę „➕ Pridėti Skriptą“.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredScripts) { script ->
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
                                        color = if (script.language == "PYTHON") Color(0xFF3572A5) else Color(0xFF012456),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (script.language == "PYTHON") "🐍 Python" else "⚡ PowerShell",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = script.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (script.isActive) "Aktyvus" else "Išjungtas",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (script.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Switch(
                                        checked = script.isActive,
                                        onCheckedChange = { onToggleScript(script.id) }
                                    )
                                }
                            }

                            if (script.description.isNotBlank()) {
                                Text(
                                    text = script.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = Color.Black.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = script.scriptPathOrContent,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onTestRunScript(script) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Išbandyti skriptą")
                                }

                                Row {
                                    IconButton(onClick = { onEditScript(script) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Redaguoti")
                                    }
                                    IconButton(onClick = { onDeleteScript(script.id) }) {
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

@Preview
@Composable
fun SeklysScreenPreview() {
    VedlysTheme {
        Surface {
            SeklysScreen(
                apiClient = ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                onBack = {},
                onHome = {}
            )
        }
    }
}

