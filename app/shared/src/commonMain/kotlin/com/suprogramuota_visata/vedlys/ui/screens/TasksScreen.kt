package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import com.suprogramuota_visata.vedlys.ui.components.ImportTransactionsDialog
import com.suprogramuota_visata.vedlys.ui.components.ExportTransactionsDialog
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.TransactionDTO
import com.suprogramuota_visata.vedlys.ui.components.ErrorBanner
import com.suprogramuota_visata.vedlys.ui.components.LoadingOverlay
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import com.suprogramuota_visata.vedlys.viewmodel.TasksViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.AppLanguage
import com.suprogramuota_visata.enums.DocumentCategory
import com.suprogramuota_visata.enums.DocumentType
import java.text.SimpleDateFormat
import java.util.*

val PredefinedStatuses = listOf("Visi", "Nauja", "Vykdoma", "Baigta")
val PredefinedDocumentTypes = listOf("Visi") + DocumentType.entries.map { it.id } + listOf("OTHER")

val DocumentGroupTypesMap: Map<String, List<String>> = mapOf(
    "Financial" to DocumentType.getByCategory(DocumentCategory.FINANCIAL).map { it.id },
    "FINANCIAL" to DocumentType.getByCategory(DocumentCategory.FINANCIAL).map { it.id },
    "Finansai" to DocumentType.getByCategory(DocumentCategory.FINANCIAL).map { it.id },

    "Operational" to DocumentType.getByCategory(DocumentCategory.OPERATIONAL).map { it.id },
    "OPERATIONAL" to DocumentType.getByCategory(DocumentCategory.OPERATIONAL).map { it.id },
    "Operacijos" to DocumentType.getByCategory(DocumentCategory.OPERATIONAL).map { it.id },

    "Delivery" to DocumentType.getByCategory(DocumentCategory.DELIVERY).map { it.id },
    "DELIVERY" to DocumentType.getByCategory(DocumentCategory.DELIVERY).map { it.id },
    "Pristatymas" to DocumentType.getByCategory(DocumentCategory.DELIVERY).map { it.id },

    "CRM" to DocumentType.getByCategory(DocumentCategory.CRM).map { it.id } + listOf("OTHER"),
    "Other" to listOf("OTHER")
)

fun getNormalizedGroupCategory(groupName: String?): String? {
    if (groupName == null) return null
    val name = groupName.trim()
    val category = DocumentCategory.fromId(name)
    if (category != null) return category.id

    return when {
        name.equals("Financial", ignoreCase = true) || name.equals("Finansai", ignoreCase = true) -> "Financial"
        name.equals("Operational", ignoreCase = true) || name.equals("Operacijos", ignoreCase = true) -> "Operational"
        name.equals("Delivery", ignoreCase = true) || name.equals("Pristatymas", ignoreCase = true) -> "Delivery"
        name.equals("CRM", ignoreCase = true) -> "CRM"
        else -> null
    }
}

fun getTranslatedGroupName(groupName: String, language: AppLanguage = AppLanguage.LT): String {
    val category = DocumentCategory.fromId(groupName)
    if (category != null) {
        return when(category) {
            DocumentCategory.FINANCIAL -> if (language == AppLanguage.EN) "Financial" else "Finansai"
            DocumentCategory.OPERATIONAL -> if (language == AppLanguage.EN) "Operational" else "Operacijos"
            DocumentCategory.DELIVERY -> if (language == AppLanguage.EN) "Delivery" else "Pristatymas"
            DocumentCategory.CRM -> "CRM"
        }
    }
    if (language == AppLanguage.EN) return groupName
    return when(groupName) {
        "Financial" -> "Finansai"
        "Operational" -> "Operacijos"
        "Delivery" -> "Pristatymas"
        "CRM" -> "CRM"
        else -> groupName
    }
}

fun getTranslatedDocumentType(type: String, language: AppLanguage = AppLanguage.LT): String {
    val docType = DocumentType.fromId(type)
    if (docType != null) {
        return when (language) {
            AppLanguage.LT -> when (docType) {
                DocumentType.SALES_INVOICE -> "Pardavimo sąskaita faktūra"
                DocumentType.PURCHASE_INVOICE -> "Pirkimo sąskaita faktūra"
                DocumentType.PROFORMA_INVOICE -> "Išankstinė sąskaita"
                DocumentType.RECEIPT -> "Kvitas"
                DocumentType.CREDIT_NOTE -> "Kreditinė sąskaita"
                DocumentType.DEBIT_NOTE -> "Debetinė sąskaita"
                DocumentType.REFUND_RECEIPT -> "Pinigų grąžinimo kvitas"
                DocumentType.QUOTATION -> "Komercinis pasiūlymas"
                DocumentType.INVENTORY_COUNT -> "Inventorizacija"
                DocumentType.REPLENISHMENT_ORDER -> "Papildymo užsakymas"
                DocumentType.WRITE_OFF_ACT -> "Nurašymo aktas"
                DocumentType.QUALITY_INSPECTION -> "Kokybės patikrinimas"
                DocumentType.INTERNAL_TRANSFER -> "Vidinis perkėlimas"
                DocumentType.GOODS_RECEIPT_NOTE -> "Prekių priėmimo aktas"
                DocumentType.PICK_LIST -> "Surinkimo lapas"
                DocumentType.PUTAWAY_LIST -> "Padėjimo lapas"
                DocumentType.SHIPMENT_CONFIRMATION -> "Išsiuntimo patvirtinimas"
                DocumentType.DELIVERY_NOTE -> "Pristatymo važtaraštis"
                DocumentType.DISPATCH_NOTE -> "Išsiuntimo važtaraštis"
                DocumentType.WAYBILL -> "Krovinio važtaraštis"
                DocumentType.BILL_OF_LADING -> "Konosamentas"
                DocumentType.PACKING_SLIP -> "Pakavimo lapas"
                DocumentType.PROOF_OF_DELIVERY -> "Pristatymo patvirtinimas"
                DocumentType.RETURN_AUTHORIZATION -> "Grąžinimo autorizacija"
                DocumentType.PURCHASE_ORDER -> "Pirkimo užsakymas"
                DocumentType.SALES_ORDER -> "Pardavimo užsakymas"
                DocumentType.WORK_ORDER -> "Darbų užsakymas"
                DocumentType.CONTRACT -> "Sutartis"
                DocumentType.SERVICE_LEVEL_AGREEMENT -> "SLA sutartis"
                DocumentType.SUPPORT_TICKET -> "Palaikymo bilietas"
                DocumentType.CUSTOMER_COMPLAINT -> "Kliento skundas"
            }
            AppLanguage.EN -> when (docType) {
                DocumentType.SALES_INVOICE -> "Sales Invoice"
                DocumentType.PURCHASE_INVOICE -> "Purchase Invoice"
                DocumentType.PROFORMA_INVOICE -> "Proforma Invoice"
                DocumentType.RECEIPT -> "Receipt"
                DocumentType.CREDIT_NOTE -> "Credit Note"
                DocumentType.DEBIT_NOTE -> "Debit Note"
                DocumentType.REFUND_RECEIPT -> "Refund Receipt"
                DocumentType.QUOTATION -> "Quotation"
                DocumentType.INVENTORY_COUNT -> "Inventory Count"
                DocumentType.REPLENISHMENT_ORDER -> "Replenishment Order"
                DocumentType.WRITE_OFF_ACT -> "Write-off Act"
                DocumentType.QUALITY_INSPECTION -> "Quality Inspection"
                DocumentType.INTERNAL_TRANSFER -> "Internal Transfer"
                DocumentType.GOODS_RECEIPT_NOTE -> "Goods Receipt Note"
                DocumentType.PICK_LIST -> "Pick List"
                DocumentType.PUTAWAY_LIST -> "Putaway List"
                DocumentType.SHIPMENT_CONFIRMATION -> "Shipment Confirmation"
                DocumentType.DELIVERY_NOTE -> "Delivery Note"
                DocumentType.DISPATCH_NOTE -> "Dispatch Note"
                DocumentType.WAYBILL -> "Waybill"
                DocumentType.BILL_OF_LADING -> "Bill of Lading"
                DocumentType.PACKING_SLIP -> "Packing Slip"
                DocumentType.PROOF_OF_DELIVERY -> "Proof of Delivery"
                DocumentType.RETURN_AUTHORIZATION -> "Return Authorization"
                DocumentType.PURCHASE_ORDER -> "Purchase Order"
                DocumentType.SALES_ORDER -> "Sales Order"
                DocumentType.WORK_ORDER -> "Work Order"
                DocumentType.CONTRACT -> "Contract"
                DocumentType.SERVICE_LEVEL_AGREEMENT -> "SLA"
                DocumentType.SUPPORT_TICKET -> "Support Ticket"
                DocumentType.CUSTOMER_COMPLAINT -> "Customer Complaint"
            }
        }
    }
    return when(type) {
        "Visi" -> if (language == AppLanguage.EN) "All" else "Visi"
        "OTHER" -> if (language == AppLanguage.EN) "Other" else "Kita"
        else -> type
    }
}

// Dok. Tipo filtras logikos atnaujinimas:
// ... (tęsinys faile)


fun getTranslatedStatus(status: String, language: AppLanguage): String {
    if (language == AppLanguage.EN) {
        return when(status) {
            "Nauja" -> "New"
            "Vykdoma" -> "In Progress"
            "Baigta" -> "Completed"
            "Blokuota", "Blocked" -> "Blocked"
            "Visi" -> "All"
            else -> status
        }
    }
    return when(status) {
        "Blocked" -> "Blokuota"
        else -> status
    }
}

fun getTasksString(key: String, language: AppLanguage): String {
    return com.suprogramuota_visata.vedlys.utils.Localization.getString("tasks", key, language)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    apiClient: ApiSvClient,
    isAdmin: Boolean,
    onOpenTask: (String) -> Unit,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val viewModel = remember { TasksViewModel(apiClient) }
    DisposableEffect(Unit) { onDispose { viewModel.dispose() } }
    val focusManager = LocalFocusManager.current
    val language by AppSettings.selectedLanguage.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = getTasksString("title", language),
                onBack = onBack,
                onHome = onHome,
                actions = {
                    IconButton(onClick = { viewModel.loadInitialTasks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = getTasksString("refresh", language))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onOpenTask("new") }) {
                Icon(Icons.Default.Add, contentDescription = getTasksString("add_task", language))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filtrai
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dok. Grupės filtras
                    var expandedFilterGroup by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedFilterGroup,
                        onExpandedChange = { expandedFilterGroup = !expandedFilterGroup },
                        modifier = Modifier.weight(1f)
                    ) {
                        val groupText = viewModel.groupFilter?.name?.let { getTranslatedGroupName(it, language) } ?: getTasksString("all_groups", language)
                        OutlinedTextField(
                            value = groupText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTasksString("doc_group", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilterGroup) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFilterGroup,
                            onDismissRequest = { expandedFilterGroup = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(getTasksString("all_groups", language)) },
                                onClick = {
                                    viewModel.updateGroupFilter(null)
                                    expandedFilterGroup = false
                                    focusManager.clearFocus()
                                }
                            )
                            viewModel.groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(getTranslatedGroupName(group.name, language)) },
                                    onClick = {
                                        viewModel.updateGroupFilter(group)
                                        expandedFilterGroup = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }

                    // Dok. Tipo filtras
                    val availableFilterDocumentTypes = remember(viewModel.groupFilter) {
                        val categoryName = viewModel.groupFilter?.name
                        if (categoryName == null || categoryName == "Visi") {
                            PredefinedDocumentTypes
                        } else {
                            val categoryKey = getNormalizedGroupCategory(categoryName)
                            val docTypes = (DocumentGroupTypesMap[categoryName]
                                ?: DocumentGroupTypesMap[categoryKey ?: ""]
                                ?: DocumentCategory.fromId(categoryName)?.let { cat -> DocumentType.getByCategory(cat).map { it.id } }
                                ?: DocumentCategory.fromId(categoryKey ?: "")?.let { cat -> DocumentType.getByCategory(cat).map { it.id } }
                                ?: emptyList())
                            listOf("Visi") + docTypes
                        }
                    }
                    var expandedType by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = getTranslatedDocumentType(viewModel.documentTypeFilter, language),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTasksString("doc_type", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            availableFilterDocumentTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(getTranslatedDocumentType(type, language)) },
                                    onClick = {
                                        viewModel.updateDocumentTypeFilter(type)
                                        expandedType = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }

                    // Statuso filtras
                    var expandedStatus by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = getTranslatedStatus(viewModel.statusFilter, language),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTasksString("status", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            PredefinedStatuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(getTranslatedStatus(status, language)) },
                                    onClick = {
                                        viewModel.updateStatusFilter(status)
                                        expandedStatus = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Import")
                        Spacer(Modifier.width(4.dp))
                        Text("Importuoti")
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Export")
                        Spacer(Modifier.width(4.dp))
                        Text("Eksportuoti")
                    }

                    Spacer(Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = viewModel.showBlocked,
                            onCheckedChange = { viewModel.toggleShowBlocked(it) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(getTasksString("show_blocked", language))
                    }
                }
            }

            viewModel.errorMessage?.let {
                ErrorBanner(it, onDismiss = { viewModel.clearError() })
            }

            when {
                viewModel.isLoading && viewModel.transactions.isEmpty() -> LoadingOverlay()
                viewModel.transactions.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        getTasksString("no_tasks", language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                    
                    LaunchedEffect(listState) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .collect { lastVisibleIndex ->
                                if (lastVisibleIndex != null && lastVisibleIndex >= viewModel.transactions.size - 5) {
                                    if (!viewModel.isLoading && !viewModel.isLastPage) {
                                        viewModel.loadMoreTasks()
                                    }
                                }
                            }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.transactions) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                onClick = { onOpenTask(transaction.transactionId) }
                            )
                        }
                        
                        if (viewModel.isLoading && viewModel.transactions.isNotEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        
                        item { Spacer(Modifier.height(80.dp)) } // Space for FAB
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            groups = viewModel.groups.filter { it.isChild == true },
            onDismiss = { showCreateDialog = false },
            onCreate = { docNum, docDate, docType ->
                viewModel.createTask(
                    documentNumber = docNum,
                    documentDate = docDate,
                    documentType = docType,
                    onSuccess = {
                        val groupName = DocumentGroupTypesMap.entries.find { it.value.contains(docType) }?.key ?: "Financial"
                        AppSettings.incrementNextDocumentNumber(groupName, docNum)
                        showCreateDialog = false
                    }
                )
            }
        )
    }

    if (showImportDialog) {
        ImportTransactionsDialog(
            onDismiss = { 
                showImportDialog = false 
                viewModel.loadInitialTasks() 
            },
            apiClient = apiClient
        )
    }

    if (showExportDialog) {
        ExportTransactionsDialog(
            onDismiss = { showExportDialog = false },
            transactions = viewModel.transactions
        )
    }
}

@Composable
private fun TransactionRow(transaction: TransactionDTO, onClick: () -> Unit) {
    val language by AppSettings.selectedLanguage.collectAsState()
    
    val filledAttrs = transaction.attributes.filter { 
        it.value.isNotBlank() && it.name !in setOf("Dokumento numeris", "Dokumento data") 
    }
    val attrSummary = if (filledAttrs.isNotEmpty()) {
        " • Atributai: " + filledAttrs.joinToString(", ") { "${it.name}: ${it.value}" }
    } else ""

    val isBlockedTx = transaction.isBlocked || transaction.status == "Blokuota" || transaction.status == "Blocked"
    val (containerColor, contentColor) = when {
        isBlockedTx -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        transaction.status == "Nauja" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        transaction.status == "Vykdoma" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        transaction.status == "Baigta" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusText = if (isBlockedTx) {
        if (language == AppLanguage.EN) "Blocked" else "Blokuota"
    } else {
        getTranslatedStatus(transaction.status, language)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = transaction.documentNumber,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    append(getTranslatedDocumentType(transaction.documentType, language))
                    append(" • Data: ")
                    append(formatTimestamp(transaction.transactionTime))
                    append(" • Statusas: ")
                    append(statusText)
                    if (attrSummary.isNotEmpty()) {
                        append(attrSummary)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(Modifier.width(8.dp))

            AssistChip(
                onClick = { },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = containerColor,
                    labelColor = contentColor
                ),
                label = { Text(statusText, fontWeight = FontWeight.SemiBold) }
            )
        }
    }
}

private fun formatTimestamp(time: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(Date(time))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTaskDialog(
    groups: List<com.suprogramuota_visata.api.domain.models.TypeDTO>,
    onDismiss: () -> Unit,
    onCreate: (documentNumber: String, documentDate: Long, documentType: String) -> Unit
) {
    val language by AppSettings.selectedLanguage.collectAsState()
    
    val availableDocumentTypes = remember {
        PredefinedDocumentTypes.filter { it != "Visi" }
    }
    
    var documentType by remember { 
        mutableStateOf(availableDocumentTypes.firstOrNull() ?: "") 
    }
    
    val selectedGroup = remember(documentType) {
        val groupName = DocumentGroupTypesMap.entries.find { it.value.contains(documentType) }?.key ?: "Financial"
        groups.find { it.name.equals(groupName, ignoreCase = true) }
    }
    
    val defaultDocNum = remember(selectedGroup) { AppSettings.generateNextDocumentNumber(selectedGroup?.name ?: "Financial") }
    var documentNumber by remember(defaultDocNum) { mutableStateOf(defaultDocNum) }
    var documentDateStr by remember { mutableStateOf(formatTimestamp(System.currentTimeMillis())) }
    
    var expandedType by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(getTasksString("new_task", language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = documentNumber,
                    onValueChange = { documentNumber = it },
                    label = { Text(getTasksString("doc_number", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = documentDateStr,
                    onValueChange = { documentDateStr = it },
                    label = { Text(getTasksString("doc_date", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = getTranslatedDocumentType(documentType, language),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(getTasksString("doc_type_full", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        availableDocumentTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(getTranslatedDocumentType(type, language)) },
                                onClick = {
                                    documentType = type
                                    expandedType = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (documentNumber.isNotBlank()) {
                        val parsedDate = try {
                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            formatter.parse(documentDateStr)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        onCreate(documentNumber, parsedDate, documentType)
                    }
                },
                enabled = documentNumber.isNotBlank()
            ) {
                Text(getTasksString("create", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(getTasksString("cancel", language))
            }
        }
    )
}

@Preview
@Composable
fun TasksScreenPreview() {
    VedlysTheme {
        Surface {
            TasksScreen(
                apiClient = ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                isAdmin = true,
                onOpenTask = {},
                onBack = {},
                onHome = {}
            )
        }
    }
}

