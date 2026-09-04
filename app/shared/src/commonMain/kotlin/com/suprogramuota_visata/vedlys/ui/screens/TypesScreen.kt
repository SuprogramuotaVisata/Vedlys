package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.Image
import androidx.compose.ui.window.Dialog
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.suprogramuota_visata.vedlys.utils.decodeImageByteArray
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.AttributeDTO
import com.suprogramuota_visata.api.domain.models.TypeDTO
import com.suprogramuota_visata.api.domain.models.ExtTemplateDTO
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.window.DialogProperties
import com.suprogramuota_visata.vedlys.ui.components.ErrorBanner
import com.suprogramuota_visata.vedlys.ui.components.LoadingOverlay
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import com.suprogramuota_visata.vedlys.viewmodel.TypesViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.suprogramuota_visata.vedlys.AppLanguage
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.utils.validateTypeConcept
import com.suprogramuota_visata.vedlys.utils.validateFieldValue
import com.suprogramuota_visata.vedlys.ui.components.PredefinedStandardFields

val PredefinedTypeGroups = listOf(
    "ProductSv", "PartnerSv", "ServiceSv", "DivisionSv", "WarehouseSv", "LocationSv", "AddressSv", 
    "TransactionSv", "TransactionDetailSv", 
    "FinancialTransactionDetailSv", "OperationalTransactionDetailSv", "DeliveryTransactionDetailSv", "CrmTransactionDetailSv",
    "GroupSv", "UnitSv", "CATEGORY", "STATUS", "ATTRIBUTE", "USER_ROLE",
    "SettingsSv", "OwnerSv", "BankSv"
)
val PredefinedAttributeTypes = listOf("INTEGER", "LONG", "DECIMAL", "DATE", "DATE_TIME", "TIMESTAMP", "STRING", "BOOL", "STRING_LIST", "URL_LIST", "JSON_STRING", "XML_STRING", "EMAIL_LIST", "Local_Type")

fun getTypesString(key: String, language: AppLanguage): String {
    return com.suprogramuota_visata.vedlys.utils.Localization.getString("types", key, language)
}

fun String.toFriendlyTypeName(language: AppLanguage = AppLanguage.LT): String {
    if (language == AppLanguage.EN) {
        return when (this) {
            "ProductSv", "Product" -> "Product"
            "PartnerSv", "Partner" -> "Partner"
            "ServiceSv", "Service" -> "Service"
            "DivisionSv", "Division" -> "Division"
            "WarehouseSv", "Warehouse" -> "Warehouse"
            "LocationSv", "Location" -> "Location"
            "AddressSv", "Address" -> "Address Details"
            "BankSv", "Bank" -> "Bank Details"
            "TransactionSv", "Transaction" -> "Transaction"
            "TransactionDetailSv" -> "Transaction Detail"
            "FinancialTransactionDetailSv", "Financial Transaction Detail" -> "Financial Transaction Detail"
            "OperationalTransactionDetailSv", "Operational Transaction Detail" -> "Operational Transaction Detail"
            "DeliveryTransactionDetailSv", "Delivery Transaction Detail" -> "Delivery Transaction Detail"
            "CrmTransactionDetailSv", "CRM Transaction Detail" -> "CRM Transaction Detail"
            "GroupSv", "Group" -> "Group"
            "UnitSv", "Unit" -> "Unit"
            "SettingsSv", "Settings" -> "Settings"
            "OwnerSv", "Owner" -> "Owner"
            "UserSv", "User" -> "User"
            "CATEGORY" -> "Category"
            "STATUS" -> "Status"
            "ATTRIBUTE" -> "Attribute"
            "USER_ROLE" -> "User Role"
            else -> if (this.endsWith("Sv")) this.removeSuffix("Sv") else this
        }
    }
    return when (this) {
        "ProductSv", "Product" -> "Prekė"
        "PartnerSv", "Partner" -> "Partneris"
        "ServiceSv", "Service" -> "Paslauga"
        "DivisionSv", "Division" -> "Padalinys"
        "WarehouseSv", "Warehouse" -> "Sandėlis"
        "LocationSv", "Location" -> "Lokacija"
        "AddressSv", "Address" -> "Adreso rekvizitai"
        "BankSv", "Bank" -> "Banko rekvizitai"
        "TransactionSv", "Transaction" -> "Dokumentas"
        "TransactionDetailSv" -> "Dokumento eilutė"
        "FinancialTransactionDetailSv", "Financial Transaction Detail" -> "Finansinio dokumento eilutė"
        "OperationalTransactionDetailSv", "Operational Transaction Detail" -> "Ūkinio dokumento eilutė"
        "DeliveryTransactionDetailSv", "Delivery Transaction Detail" -> "Gabenimo dokumento eilutė"
        "CrmTransactionDetailSv", "CRM Transaction Detail" -> "CRM dokumento eilutė"
        "GroupSv", "Group" -> "Grupė"
        "UnitSv", "Unit" -> "Matavimo vienetas"
        "SettingsSv", "Settings" -> "Nustatymai"
        "OwnerSv", "Owner" -> "Savininkas"
        "UserSv", "User" -> "Naudotojas"
        "CATEGORY" -> "Kategorija"
        "STATUS" -> "Būsena"
        "ATTRIBUTE" -> "Atributas"
        "USER_ROLE" -> "Naudotojo rolė"
        else -> if (this.endsWith("Sv")) this.removeSuffix("Sv") else this
    }
}

fun String.formatGroupName(language: AppLanguage = AppLanguage.LT): String {
    var result = this
    listOf(
        "ProductSv", "Product", "PartnerSv", "Partner", "ServiceSv", "Service", "DivisionSv", "Division",
        "WarehouseSv", "Warehouse", "LocationSv", "Location", "AddressSv", "Address", "BankSv", "Bank",
        "TransactionSv", "Transaction", "TransactionDetailSv",
        "FinancialTransactionDetailSv", "Financial Transaction Detail",
        "OperationalTransactionDetailSv", "Operational Transaction Detail",
        "DeliveryTransactionDetailSv", "Delivery Transaction Detail",
        "CrmTransactionDetailSv", "CRM Transaction Detail", "GroupSv", "Group",
        "UnitSv", "Unit", "SettingsSv", "Settings", "OwnerSv", "Owner", "PhoneSv", "EmailSv", "UrlSv", "DeviceSv"
    ).forEach { sysType ->
        if (result == sysType || result == "Šakninė grupė - $sysType") {
            result = sysType.toFriendlyTypeName(language)
        }
    }
    if (language == AppLanguage.EN) {
        result = result.replace("Šakninė grupė", "Root Group")
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypesScreen(
    apiClient: ApiSvClient,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val viewModel = remember { TypesViewModel(apiClient) }
    DisposableEffect(Unit) { onDispose { viewModel.dispose() } }
    val focusManager = LocalFocusManager.current
    val language by AppSettings.selectedLanguage.collectAsState()
    val scope = rememberCoroutineScope()

    var showEditor by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<TypeDTO?>(null) }
    var deleteCandidate by remember { mutableStateOf<TypeDTO?>(null) }
    var previewImageUuid by remember { mutableStateOf<String?>(null) }

    val groupsViewModel = remember { TypesViewModel(apiClient) }
    var groups by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    LaunchedEffect(Unit) {
        groupsViewModel.updateTypeName("GroupSv")
        groupsViewModel.loadInitial()
    }
    LaunchedEffect(groupsViewModel.items) {
        groups = groupsViewModel.items
    }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = getTypesString("title", language),
                onBack = onBack,
                onHome = onHome,
                actions = {
                    IconButton(onClick = { viewModel.loadInitial() }) {
                        Icon(Icons.Default.Refresh, contentDescription = getTypesString("refresh", language))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingItem = null
                showEditor = true
            }) {
                Icon(Icons.Default.Add, contentDescription = getTypesString("new_type", language))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tipo pavadinimo filtras su Dropdown
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = viewModel.typeName.toFriendlyTypeName(language),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTypesString("type_group", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            PredefinedTypeGroups.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption.toFriendlyTypeName(language)) },
                                    onClick = {
                                        viewModel.updateTypeName(selectionOption)
                                        expanded = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                    
                    // Grupės filtras
                    var expandedGroup by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedGroup,
                        onExpandedChange = { expandedGroup = !expandedGroup },
                        modifier = Modifier.weight(1f)
                    ) {
                        val groupText = viewModel.groupFilter?.name ?: getTypesString("all_groups", language)
                        OutlinedTextField(
                            value = groupText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTypesString("group", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGroup,
                            onDismissRequest = { expandedGroup = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(getTypesString("all_groups", language)) },
                                onClick = {
                                    viewModel.updateGroupFilter(null)
                                    expandedGroup = false
                                    focusManager.clearFocus()
                                }
                            )
                            val applicableGroups = if (viewModel.typeName == "GroupSv") {
                                groups.filter { it.isChild == false }
                            } else {
                                groups.filter { it.targetType == viewModel.typeName }
                            }
                            applicableGroups.forEach { grp ->
                                DropdownMenuItem(
                                    text = { Text(grp.name) },
                                    onClick = {
                                        viewModel.updateGroupFilter(grp)
                                        expandedGroup = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.loadInitial() 
                            groupsViewModel.loadInitial()
                        },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = getTypesString("filter", language))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var showImportDialog by remember { mutableStateOf(false) }
                    var showExportDialog by remember { mutableStateOf(false) }
                    var showTemplatesDialog by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Import")
                        Spacer(Modifier.width(4.dp))
                        Text("Importuoti")
                    }
                    
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Export")
                        Spacer(Modifier.width(4.dp))
                        Text("Eksportuoti")
                    }

                    Button(
                        onClick = { showTemplatesDialog = true },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Templates")
                        Spacer(Modifier.width(4.dp))
                        Text("Šablonai")
                    }
                    
                    if (showImportDialog) {
                        com.suprogramuota_visata.vedlys.ui.components.ImportTypesDialog(
                            onDismiss = { 
                                showImportDialog = false 
                                viewModel.loadInitial() // Atsinaujinti sąrašą po importo
                            },
                            typeName = viewModel.typeName,
                            apiClient = apiClient
                        )
                    }
                    
                    if (showExportDialog) {
                        com.suprogramuota_visata.vedlys.ui.components.ExportTypesDialog(
                            onDismiss = { showExportDialog = false },
                            typeName = viewModel.typeName,
                            items = viewModel.items
                        )
                    }

                    if (showTemplatesDialog) {
                        com.suprogramuota_visata.vedlys.ui.components.ExtTemplateDialog(
                            apiClient = apiClient,
                            ownerType = viewModel.typeName,
                            onDismiss = { showTemplatesDialog = false },
                            onChanged = {
                                viewModel.loadInitial()
                            },
                            onHome = {
                                showTemplatesDialog = false
                                onHome?.invoke()
                            }
                        )
                    }
                    
                    val allFilteredSelected = viewModel.items.isNotEmpty() && 
                        viewModel.items.mapNotNull { it.id }.all { viewModel.selectedItemIds.contains(it) }
                    
                    OutlinedButton(
                        onClick = { viewModel.toggleSelectAll() },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            if (allFilteredSelected) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = if (allFilteredSelected) "Atžymėti visus" else "Pažymėti visus"
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (allFilteredSelected) "Atžymėti visus" else "Pažymėti visus (${viewModel.items.size})")
                    }

                    var showBulkDeleteDialog by remember { mutableStateOf(false) }
                    if (viewModel.selectedItemIds.isNotEmpty()) {
                        Button(
                            onClick = { showBulkDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Ištrinti pažymėtus")
                            Spacer(Modifier.width(4.dp))
                            Text("Ištrinti pažymėtus (${viewModel.selectedItemIds.size})")
                        }
                    }

                    if (showBulkDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showBulkDeleteDialog = false },
                            title = { Text("Trinti pažymėtus įrašus") },
                            text = { 
                                Text("Ar tikrai norite ištrinti ${viewModel.selectedItemIds.size} pažymėtus įrašus?\n\nPagal duomenų saugumo ir vientisumo taisykles, įrašai, turintys ryšių su kitais objektais (pvz., transakcijomis ar pošakiais) arba saugomi sistemos, nebus ištrinti ir bus saugiai palikti.")
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showBulkDeleteDialog = false
                                        viewModel.deleteSelected()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Ištrinti")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showBulkDeleteDialog = false }) {
                                    Text("Atšaukti")
                                }
                            }
                        )
                    }

                    Spacer(Modifier.weight(1f))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = viewModel.showInactive,
                            onCheckedChange = { viewModel.toggleShowInactive(it) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(getTypesString("show_inactive", language))
                    }
                }
            }

            viewModel.errorMessage?.let {
                ErrorBanner(it, onDismiss = { viewModel.clearError() })
            }
            viewModel.infoMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.clearInfo()
                }
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(msg, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.clearInfo() }) { Text(getTypesString("ok", language)) }
                    }
                }
            }

            when {
                viewModel.isLoading && viewModel.items.isEmpty() -> LoadingOverlay()
                viewModel.items.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        getTypesString("no_items", language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                    
                    // Infinite scrolling logika
                    LaunchedEffect(listState) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .collect { lastVisibleIndex ->
                                if (lastVisibleIndex != null && lastVisibleIndex >= viewModel.items.size - 5) {
                                    if (!viewModel.isLoading && !viewModel.isLastPage) {
                                        viewModel.loadMore()
                                    }
                                }
                            }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.items) { item ->
                            val rawJson = item.attributes.find { 
                                it.name.equals("Nuotraukos Meta", ignoreCase = true) || 
                                it.attributeType == "JSON_STRING" || 
                                it.value?.contains("\"img_format\"") == true ||
                                (it.value?.contains("\"id\"") == true && it.value?.contains("-") == true)
                            }?.value
                            val imgUuid: String? = if (!rawJson.isNullOrBlank()) {
                                Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(rawJson)?.groupValues?.getOrNull(1)
                            } else {
                                null
                            }

                            TypeRow(
                                item = item,
                                language = language,
                                isSelected = item.id != null && viewModel.selectedItemIds.contains(item.id),
                                onToggleSelect = { item.id?.let { id -> viewModel.toggleSelectItem(id) } },
                                onEdit = {
                                    editingItem = item
                                    showEditor = true
                                },
                                onDelete = { deleteCandidate = item },
                                onViewImage = if (imgUuid != null) {
                                    { previewImageUuid = imgUuid }
                                } else {
                                    null
                                }
                            )
                        }
                        
                        if (viewModel.isLoading && viewModel.items.isNotEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        
                        item { Spacer(Modifier.height(80.dp)) } // vietos po FAB
                    }
                }
            }
        }

        if (showEditor) {
            TypeEditorDialog(
                initial = editingItem,
                defaultTypeName = viewModel.typeName,
                apiClient = apiClient,
                language = language,
                allGroups = groups,
                errorMessage = viewModel.errorMessage,
                onClearError = { viewModel.clearError() },
                onDismiss = { 
                    viewModel.clearError()
                    showEditor = false 
                },
                onSave = { dto, checkedChildren ->
                    scope.launch {
                        val res = if (dto.id == null) {
                            apiClient.typeRepository.create(dto)
                        } else {
                            apiClient.typeRepository.update(dto)
                        }
                        if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
                            showEditor = false
                            val savedGroup = res.data
                            if (checkedChildren.isNotEmpty()) {
                                checkedChildren.forEach { child ->
                                    apiClient.typeRepository.update(child.copy(groupId = savedGroup.id))
                                }
                            }
                            viewModel.loadInitial()
                        } else if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Error) {
                            viewModel.setError(res.message)
                        }
                    }
                }
            )
        }

        deleteCandidate?.let { item ->
            AlertDialog(
                onDismissRequest = { deleteCandidate = null },
                title = { Text(getTypesString("confirm_deletion_title", language)) },
                text = { Text("${getTypesString("confirm_deletion_text", language)} „${item.name}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.delete(item)
                        deleteCandidate = null
                    }) { Text(getTypesString("delete", language)) }
                },
                dismissButton = {
                    TextButton(onClick = { deleteCandidate = null }) { Text(getTypesString("cancel", language)) }
                }
            )
        }

        if (previewImageUuid != null) {
            ImagePreviewDialog(
                imageUuid = previewImageUuid!!,
                onDismiss = { previewImageUuid = null }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeRow(
    item: TypeDTO,
    language: AppLanguage,
    isSelected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewImage: (() -> Unit)? = null
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onToggleSelect != null && item.id != null) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() }
                )
                Spacer(Modifier.width(8.dp))
            }

            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name.formatGroupName(language), 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(item.type.toFriendlyTypeName(language)) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                if (!item.enabled) {
                    AssistChip(
                        onClick = {},
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer, 
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        label = { Text(getTypesString("inactive", language)) },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Text(
                    text = buildString {
                        append("ID: ${item.id ?: "—"}")
                        item.groupId?.let { append("  •  ${getTypesString("group", language)}: $it") }
                        item.code?.let { append("  •  Kodas: $it") }
                        item.barcode?.let { append("  •  Barkodas: $it") }
                        if (item.attributes.isNotEmpty()) {
                            val attrsStr = item.attributes.joinToString(", ") { "${it.name}: ${it.value ?: "—"}" }
                            append("  •  ${getTypesString("attributes", language)} $attrsStr")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            val isProtectedSystemItem = (item.id == 0) || (item.level == 0) || (item.isChild == false) || (item.parentGroupId == null && item.type == "GroupSv")
            
            if (onViewImage != null) {
                IconButton(onClick = onViewImage) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Peržiūrėti nuotrauką",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = getTypesString("edit", language))
            }
            if (!isProtectedSystemItem) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = getTypesString("delete", language),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ImagePreviewDialog(
    imageUuid: String,
    onDismiss: () -> Unit
) {
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUuid) {
        withContext(Dispatchers.IO) {
            try {
                val url = "https://localhost:8081/images/$imageUuid"
                val conn = (URL(url).openConnection() as HttpsURLConnection).apply {
                    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                    })
                    val sc = SSLContext.getInstance("TLS")
                    sc.init(null, trustAllCerts, java.security.SecureRandom())
                    sslSocketFactory = sc.socketFactory
                    hostnameVerifier = javax.net.ssl.HostnameVerifier { _, _ -> true }
                    requestMethod = "GET"
                    connectTimeout = 7000
                    readTimeout = 7000
                }
                if (conn.responseCode == 200) {
                    val bytes = conn.inputStream.use { it.readBytes() }
                    imageBitmap = decodeImageByteArray(bytes)
                } else {
                    errorMessage = "Nepavyko užkrauti nuotraukos (HTTP ${conn.responseCode})"
                }
            } catch (e: Exception) {
                errorMessage = "Klaida: ${e.message}"
            } finally {
                isLoading = false
            }
        }
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
                        text = "Nuotraukos peržiūra",
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
                            color = MaterialTheme.colorScheme.error
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TypeEditorDialog(
    initial: TypeDTO?,
    defaultTypeName: String,
    apiClient: ApiSvClient,
    language: AppLanguage,
    allGroups: List<TypeDTO>,
    errorMessage: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (TypeDTO, List<TypeDTO>) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var nameDirty by remember { mutableStateOf(false) }
    val focusRequesterName = remember { FocusRequester() }
    val focusRequesterCode = remember { FocusRequester() }
    val focusRequesterBarcode = remember { FocusRequester() }
    val focusRequesterBaseUnit = remember { FocusRequester() }
    val focusRequesterConversionFactor = remember { FocusRequester() }

    var templates by remember { mutableStateOf<List<ExtTemplateDTO>>(emptyList()) }
    var selectedTemplateId by remember { mutableStateOf<Int?>(initial?.extTemplateId) }
    
    val validationErrors = remember { mutableStateMapOf<String, String>() }
    val validationSuccesses = remember { mutableStateMapOf<String, Boolean>() }
    val dirtyFields = remember { mutableStateMapOf<String, Boolean>() }
    var ignoreFocusLoss by remember { mutableStateOf(false) }

    fun validateStandardField(fieldName: String, value: String): Boolean {
        val extAttr = templates.find { it.id == selectedTemplateId }?.attributes?.find { it.name == fieldName }
        if (extAttr == null || !extAttr.validateRule) {
            val error = when (fieldName) {
                "Pavadinimas" -> {
                    if (value.isBlank()) {
                        if (language == AppLanguage.EN) "Name cannot be empty" else "Pavadinimas negali būti tuščias"
                    } else null
                }
                "Konvertavimo koeficientas" -> {
                    if (value.isNotBlank() && value.toDoubleOrNull() == null) {
                        if (language == AppLanguage.EN) "Must be a valid decimal number" else "Turi būti skaičius"
                    } else null
                }
                else -> null
            }
            if (error != null) {
                validationErrors[fieldName] = error
                validationSuccesses.remove(fieldName)
                return false
            } else {
                validationErrors.remove(fieldName)
                if (value.isNotBlank()) {
                    validationSuccesses[fieldName] = true
                } else {
                    validationSuccesses.remove(fieldName)
                }
                return true
            }
        }
        val error = validateFieldValue(fieldName, value, extAttr, language)
        if (error != null) {
            validationErrors[fieldName] = error
            validationSuccesses.remove(fieldName)
            return false
        } else {
            validationErrors.remove(fieldName)
            if (value.isNotBlank()) {
                validationSuccesses[fieldName] = true
            } else {
                validationSuccesses.remove(fieldName)
            }
            return true
        }
    }

    fun validateName(value: String): Boolean = validateStandardField("Pavadinimas", value)
    var typeStr by remember { mutableStateOf(initial?.type ?: defaultTypeName) }
    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }
    var isChild by remember { mutableStateOf(initial?.isChild ?: false) }
    var targetType by remember { mutableStateOf(initial?.targetType ?: "") }
    var groupId by remember { mutableStateOf(initial?.parentGroupId?.toString() ?: initial?.groupId?.toString() ?: "") }
    
    val computedLevel = remember(groupId, allGroups, typeStr) {
        if (typeStr == "GroupSv" && groupId.isNotBlank()) {
            val pId = groupId.toIntOrNull()
            val parent = allGroups.find { it.id == pId }
            (parent?.level ?: 0) + 1
        } else 0
    }
    
    var isCompany by remember { mutableStateOf(initial?.isCompany ?: false) }
    
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var barcode by remember { mutableStateOf(initial?.barcode ?: "") }
    
    var baseUnit by remember { mutableStateOf(initial?.baseUnit ?: "") }
    var baseUnitId by remember { mutableStateOf<Int?>(initial?.baseUnitId) }
    var conversionFactor by remember { mutableStateOf(initial?.conversionFactor?.toString() ?: "1.0") }

    var allUnits by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var nestedTypeToCreate by remember { mutableStateOf<String?>(null) }
    var nestedAttributeIndex by remember { mutableStateOf<Int?>(null) }
    var showNestedEditor by remember { mutableStateOf(false) }
    var nestedErrorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    fun validateAttribute(attr: AttributeDTO, index: Int): Boolean {
        val extAttr = templates.find { it.id == selectedTemplateId }?.attributes?.find { it.name == attr.name }
        val error = validateFieldValue(attr.name, attr.value, extAttr, language)
        if (error != null) {
            validationErrors[attr.name] = error
            validationSuccesses.remove(attr.name)
            return false
        } else {
            validationErrors.remove(attr.name)
            if (attr.value.isNotBlank()) {
                validationSuccesses[attr.name] = true
            } else {
                validationSuccesses.remove(attr.name)
            }
            return true
        }
    }
    LaunchedEffect(Unit) {
        val res = apiClient.typeRepository.getAllByType("UnitSv")
        if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
            allUnits = res.data ?: emptyList()
        }
    }

    val attributes = remember {
        mutableStateListOf<AttributeDTO>().apply { addAll(initial?.attributes ?: emptyList()) }
    }
    val focusRequesters = remember(attributes.size) {
        List(attributes.size) { FocusRequester() }
    }
    
    var unassignedChildren by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var checkedChildren = remember { mutableStateListOf<TypeDTO>() }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val validationModifier = { index: Int, attr: AttributeDTO ->
        Modifier
            .focusRequester(focusRequesters[index])
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (keyEvent.key == Key.Tab) {
                        val isValid = validateAttribute(attributes[index], index)
                        if (!isValid) return@onPreviewKeyEvent true
                    } else if (keyEvent.key == Key.Enter) {
                        val isValid = validateAttribute(attributes[index], index)
                        if (isValid) {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                        return@onPreviewKeyEvent true
                    }
                }
                false
            }
            .onFocusChanged { focusState ->
                if (!focusState.isFocused && !ignoreFocusLoss) {
                    if (dirtyFields.containsKey(attr.name) || validationErrors.containsKey(attr.name)) {
                        val isValid = validateAttribute(attributes[index], index)
                        if (!isValid) {
                            scope.launch {
                                delay(50)
                                focusRequesters[index].requestFocus()
                            }
                        }
                    }
                }
            }
    }

    val standardValidationModifier = { fieldName: String, requester: FocusRequester, validateFn: () -> Boolean ->
        Modifier
            .focusRequester(requester)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (keyEvent.key == Key.Tab) {
                        val isValid = validateFn()
                        if (!isValid) return@onPreviewKeyEvent true
                    } else if (keyEvent.key == Key.Enter) {
                        val isValid = validateFn()
                        if (isValid) {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                        return@onPreviewKeyEvent true
                    }
                }
                false
            }
            .onFocusChanged { focusState ->
                if (!focusState.isFocused && !ignoreFocusLoss) {
                    if (dirtyFields.containsKey(fieldName) || validationErrors.containsKey(fieldName)) {
                        val isValid = validateFn()
                        if (!isValid) {
                            scope.launch {
                                delay(50)
                                requester.requestFocus()
                            }
                        }
                    }
                }
            }
    }

    val localTypeQueries = remember { mutableStateMapOf<String, String>() }
    val localTypeOptions = remember { mutableStateMapOf<String, List<TypeDTO>>() }
    val localTypeGroups = remember(attributes, templates, selectedTemplateId) {
        attributes.mapNotNull { attr ->
            val ext = templates.find { it.id == selectedTemplateId }?.attributes?.find { it.name == attr.name }
            if (ext?.attributeType == "Local_Type") {
                ext.validations.find { it.ruleId == 12 }?.args?.firstOrNull()
            } else null
        }.distinct()
    }
    LaunchedEffect(localTypeGroups) {
        localTypeGroups.forEach { group ->
            if (!localTypeOptions.containsKey(group)) {
                val res = apiClient.typeRepository.getAllByType(group)
                if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
                    localTypeOptions[group] = res.data ?: emptyList()
                }
            }
        }
    }

    fun loadTemplates(type: String) {
        if (type.isBlank()) return
        scope.launch {
            val result = apiClient.extTemplateRepository.getTemplatesByOwnerType(type)
            if (result.isSuccess) {
                templates = result.getOrNull() ?: emptyList()
                if (selectedTemplateId == null) {
                    val defTpl = templates.find { it.isDefault } ?: templates.firstOrNull()
                    if (defTpl != null) {
                        selectedTemplateId = defTpl.id
                    }
                }
            }
        }
    }

    LaunchedEffect(typeStr) {
        if (initial == null) {
            attributes.clear()
            selectedTemplateId = null
        }
        loadTemplates(typeStr)
    }

    LaunchedEffect(selectedTemplateId) {
        localTypeQueries.clear()
        if (initial == null) {
            attributes.clear()
            if (selectedTemplateId != null) {
                val tpl = templates.find { it.id == selectedTemplateId }
                if (tpl != null) {
                    tpl.attributes.forEach { ext ->
                        val valToUse = ext.defaultValue ?: ""
                        attributes.add(
                            AttributeDTO(
                                name = ext.name,
                                attributeType = ext.attributeType,
                                validate = ext.validateRule,
                                value = valToUse,
                                validations = ext.validations,
                                tag = ext.tag
                            )
                        )
                        when (ext.name) {
                            "Pavadinimas" -> if (name.isEmpty()) name = valToUse
                            "Kodas" -> if (code.isEmpty()) code = valToUse
                            "Brūkšninis kodas" -> if (barcode.isEmpty()) barcode = valToUse
                            "Matavimo vienetas" -> if (baseUnit.isEmpty()) baseUnit = valToUse
                            "Konvertavimo koeficientas" -> if (conversionFactor.isEmpty() || conversionFactor == "1.0") conversionFactor = valToUse.ifEmpty { "1.0" }
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            ignoreFocusLoss = true
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.95f),
        title = { Text(if (initial == null) getTypesString("create_new", language) else getTypesString("edit_type", language)) },
        text = {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage != null) {
                    com.suprogramuota_visata.vedlys.ui.components.ErrorBanner(
                        message = errorMessage,
                        onDismiss = onClearError
                    )
                }

                // Row 1: Pavadinimas, Kodas (if applicable), Tipas, Šablonas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isNameError = validationErrors.containsKey("Pavadinimas")
                    val isNameSuccess = validationSuccesses.containsKey("Pavadinimas") && !isNameError

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            dirtyFields["Pavadinimas"] = true
                            val idx = attributes.indexOfFirst { it.name == "Pavadinimas" }
                            if (idx >= 0) {
                                attributes[idx] = attributes[idx].copy(value = it)
                            }
                            if (validationErrors.containsKey("Pavadinimas")) {
                                validateStandardField("Pavadinimas", it)
                            } else {
                                validationSuccesses.remove("Pavadinimas")
                            }
                        },
                        label = { Text(getTypesString("name", language)) },
                        singleLine = true,
                        isError = isNameError,
                        supportingText = validationErrors["Pavadinimas"]?.let { { Text(it) } },
                        modifier = Modifier
                            .weight(1.8f)
                            .then(standardValidationModifier("Pavadinimas", focusRequesterName) {
                                validateStandardField("Pavadinimas", name)
                            }),
                        trailingIcon = if (isNameSuccess) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Valid",
                                    tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            }
                        } else null,
                        colors = if (isNameSuccess) {
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                            )
                        } else OutlinedTextFieldDefaults.colors()
                    )

                    if (typeStr in listOf("ProductSv", "ServiceSv", "LocationSv", "WarehouseSv", "DivisionSv", "PartnerSv", "UnitSv")) {
                        val isCodeError = validationErrors.containsKey("Kodas")
                        val isCodeSuccess = validationSuccesses.containsKey("Kodas") && !isCodeError
                        OutlinedTextField(
                            value = code,
                            onValueChange = {
                                code = it
                                dirtyFields["Kodas"] = true
                                val idx = attributes.indexOfFirst { it.name == "Kodas" }
                                if (idx >= 0) {
                                    attributes[idx] = attributes[idx].copy(value = it)
                                }
                                if (validationErrors.containsKey("Kodas")) {
                                    validateStandardField("Kodas", it)
                                } else {
                                    validationSuccesses.remove("Kodas")
                                }
                            },
                            label = { Text("Kodas (Code)") },
                            singleLine = true,
                            isError = isCodeError,
                            supportingText = validationErrors["Kodas"]?.let { { Text(it) } },
                            modifier = Modifier.weight(1f)
                                .then(standardValidationModifier("Kodas", focusRequesterCode) {
                                    validateStandardField("Kodas", code)
                                }),
                            trailingIcon = if (isCodeSuccess) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Valid",
                                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    )
                                }
                            } else null,
                            colors = if (isCodeSuccess) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            } else OutlinedTextFieldDefaults.colors()
                        )
                    }

                    var expandedType by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = typeStr.toFriendlyTypeName(language),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTypesString("type_group_req", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            PredefinedTypeGroups.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.toFriendlyTypeName(language)) },
                                    onClick = {
                                        typeStr = opt
                                        selectedTemplateId = null // Reset template when type changes
                                        expandedType = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }

                    // Šablono pasirinkimas
                    var expandedTemplate by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedTemplate,
                        onExpandedChange = { if (initial == null) expandedTemplate = !expandedTemplate },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        val selectedTplName = templates.find { it.id == selectedTemplateId }?.name ?: getTypesString("not_selected", language)
                        OutlinedTextField(
                            value = selectedTplName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTypesString("template", language)) },
                            trailingIcon = { if (initial == null) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTemplate) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        if (initial == null && templates.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expandedTemplate,
                                onDismissRequest = { expandedTemplate = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(getTypesString("not_selected", language)) },
                                    onClick = {
                                        selectedTemplateId = null
                                        expandedTemplate = false
                                        focusManager.clearFocus()
                                    }
                                )
                                templates.forEach { tpl ->
                                    DropdownMenuItem(
                                        text = { Text(tpl.name + if(tpl.isDefault) " (Default)" else "") },
                                        onClick = {
                                            selectedTemplateId = tpl.id
                                            expandedTemplate = false
                                            focusManager.clearFocus()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Row 2: Grupė, Brūkšninis kodas / Matavimo vienetas, Switch'ai
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var expandedGroupSelection by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedGroupSelection,
                        onExpandedChange = { expandedGroupSelection = !expandedGroupSelection },
                        modifier = Modifier.weight(1.3f)
                    ) {
                        val applicableGroups = if (typeStr == "GroupSv") {
                            allGroups.filter { it.isChild == false }
                        } else {
                            allGroups.filter { it.targetType == typeStr && it.isChild == true }
                        }
                        
                        val selectedGroupObj = applicableGroups.find { it.id?.toString() == groupId }
                        val selectedGroupName = selectedGroupObj?.name?.formatGroupName(language) ?: groupId
                        
                        OutlinedTextField(
                            value = selectedGroupName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTypesString("group_id_opt", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroupSelection) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGroupSelection,
                            onDismissRequest = { expandedGroupSelection = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    groupId = ""
                                    expandedGroupSelection = false
                                }
                            )
                            applicableGroups.forEach { grp ->
                                DropdownMenuItem(
                                    text = { Text(grp.name.formatGroupName(language)) },
                                    onClick = {
                                        groupId = grp.id.toString()
                                        if (typeStr == "GroupSv" && !grp.targetType.isNullOrBlank()) {
                                            targetType = grp.targetType!!
                                        }
                                        expandedGroupSelection = false
                                    }
                                )
                            }
                        }
                    }

                    if (typeStr in listOf("ProductSv", "ServiceSv", "LocationSv", "WarehouseSv", "DivisionSv", "UnitSv")) {
                        val isBarcodeError = validationErrors.containsKey("Brūkšninis kodas")
                        val isBarcodeSuccess = validationSuccesses.containsKey("Brūkšninis kodas") && !isBarcodeError
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = {
                                barcode = it
                                dirtyFields["Brūkšninis kodas"] = true
                                val idx = attributes.indexOfFirst { it.name == "Brūkšninis kodas" }
                                if (idx >= 0) {
                                    attributes[idx] = attributes[idx].copy(value = it)
                                }
                                if (validationErrors.containsKey("Brūkšninis kodas")) {
                                    validateStandardField("Brūkšninis kodas", it)
                                } else {
                                    validationSuccesses.remove("Brūkšninis kodas")
                                }
                            },
                            label = { Text("Brūkšninis kodas (Barcode)") },
                            singleLine = true,
                            isError = isBarcodeError,
                            supportingText = validationErrors["Brūkšninis kodas"]?.let { { Text(it) } },
                            modifier = Modifier.weight(1.3f)
                                .then(standardValidationModifier("Brūkšninis kodas", focusRequesterBarcode) {
                                    validateStandardField("Brūkšninis kodas", barcode)
                                }),
                            trailingIcon = if (isBarcodeSuccess) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Valid",
                                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    )
                                }
                            } else null,
                            colors = if (isBarcodeSuccess) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            } else OutlinedTextFieldDefaults.colors()
                        )
                    }

                    if (typeStr == "ProductSv" || typeStr == "ServiceSv") {
                        var unitDropdownExpanded by remember { mutableStateOf(false) }
                        val selectedUnitObj = allUnits.find { it.code == baseUnit }
                        val selectedUnitLabel = if (selectedUnitObj != null) {
                            getTranslatedUnitName(selectedUnitObj.code ?: "", selectedUnitObj.name, language)
                        } else {
                            baseUnit
                        }
                        val isUnitError = validationErrors.containsKey("Matavimo vienetas")
                        val isUnitSuccess = validationSuccesses.containsKey("Matavimo vienetas") && !isUnitError
                        
                        ExposedDropdownMenuBox(
                            expanded = unitDropdownExpanded,
                            onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = selectedUnitLabel ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Matavimo vienetas (Unit)") },
                                trailingIcon = if (isUnitSuccess) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Valid",
                                            tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                        )
                                    }
                                } else {
                                    { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) }
                                },
                                colors = if (isUnitSuccess) {
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    )
                                } else ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                isError = isUnitError,
                                supportingText = validationErrors["Matavimo vienetas"]?.let { { Text(it) } },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                    .then(standardValidationModifier("Matavimo vienetas", focusRequesterBaseUnit) {
                                        validateStandardField("Matavimo vienetas", baseUnit)
                                    }),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Pasirinkite...") },
                                    onClick = {
                                        baseUnit = ""
                                        dirtyFields["Matavimo vienetas"] = true
                                        val idx = attributes.indexOfFirst { it.name == "Matavimo vienetas" }
                                        if (idx >= 0) {
                                            attributes[idx] = attributes[idx].copy(value = "")
                                        }
                                        validateStandardField("Matavimo vienetas", "")
                                        unitDropdownExpanded = false
                                    }
                                )
                                allUnits.forEach { u ->
                                    val label = getTranslatedUnitName(u.code ?: "", u.name, language)
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            val codeVal = u.code ?: ""
                                            baseUnit = codeVal
                                            dirtyFields["Matavimo vienetas"] = true
                                            val idx = attributes.indexOfFirst { it.name == "Matavimo vienetas" }
                                            if (idx >= 0) {
                                                attributes[idx] = attributes[idx].copy(value = codeVal)
                                            }
                                            validateStandardField("Matavimo vienetas", codeVal)
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (typeStr == "UnitSv") {
                        var baseUnitIdDropdownExpanded by remember { mutableStateOf(false) }
                        val selectedBaseUnitObj = allUnits.find { it.id == baseUnitId }
                        val selectedBaseUnitLabel = if (selectedBaseUnitObj != null) {
                            getTranslatedUnitName(selectedBaseUnitObj.code ?: "", selectedBaseUnitObj.name, language)
                        } else {
                            "Nėra (Bazinis vienetas)"
                        }
                        
                        ExposedDropdownMenuBox(
                            expanded = baseUnitIdDropdownExpanded,
                            onExpandedChange = { baseUnitIdDropdownExpanded = !baseUnitIdDropdownExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = selectedBaseUnitLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Bazinis vnt. (Base Unit)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = baseUnitIdDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = baseUnitIdDropdownExpanded,
                                onDismissRequest = { baseUnitIdDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nėra (Bazinis vienetas)") },
                                    onClick = {
                                        baseUnitId = null
                                        baseUnitIdDropdownExpanded = false
                                    }
                                )
                                allUnits.filter { it.baseUnitId == null && it.id != initial?.id }.forEach { u ->
                                    val label = getTranslatedUnitName(u.code ?: "", u.name, language)
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            baseUnitId = u.id
                                            baseUnitIdDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        val isConversionError = validationErrors.containsKey("Konvertavimo koeficientas")
                        val isConversionSuccess = validationSuccesses.containsKey("Konvertavimo koeficientas") && !isConversionError
                        OutlinedTextField(
                            value = conversionFactor,
                            onValueChange = {
                                conversionFactor = it
                                dirtyFields["Konvertavimo koeficientas"] = true
                                val idx = attributes.indexOfFirst { it.name == "Konvertavimo koeficientas" }
                                if (idx >= 0) {
                                    attributes[idx] = attributes[idx].copy(value = it)
                                }
                                if (validationErrors.containsKey("Konvertavimo koeficientas")) {
                                    validateStandardField("Konvertavimo koeficientas", it)
                                } else {
                                    validationSuccesses.remove("Konvertavimo koeficientas")
                                }
                            },
                            label = { Text("Konv. koeficientas") },
                            singleLine = true,
                            isError = isConversionError,
                            supportingText = validationErrors["Konvertavimo koeficientas"]?.let { { Text(it) } },
                            modifier = Modifier.weight(1.1f)
                                .then(standardValidationModifier("Konvertavimo koeficientas", focusRequesterConversionFactor) {
                                    validateStandardField("Konvertavimo koeficientas", conversionFactor)
                                }),
                            trailingIcon = if (isConversionSuccess) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Valid",
                                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    )
                                }
                            } else null,
                            colors = if (isConversionSuccess) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            } else OutlinedTextFieldDefaults.colors()
                        )
                    }

                    if (typeStr == "PartnerSv") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Switch(checked = isCompany, onCheckedChange = { isCompany = it })
                            Spacer(Modifier.width(6.dp))
                            Text("Įmonė", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                        Spacer(Modifier.width(6.dp))
                        Text(getTypesString("enabled", language), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // If type is GroupSv, show isChild and targetType fields
                if (typeStr == "GroupSv") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Switch(checked = isChild, onCheckedChange = { isChild = it })
                            Spacer(Modifier.width(8.dp))
                            Text(getTypesString("is_child", language))
                        }

                        OutlinedTextField(
                            value = computedLevel.toString(),
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(if (language == AppLanguage.EN) "Level" else "Lygis") },
                            colors = OutlinedTextFieldDefaults.colors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        if (!isChild && initial == null) {
                            var expandedTargetType by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedTargetType,
                                onExpandedChange = { expandedTargetType = !expandedTargetType },
                                modifier = Modifier.weight(1.5f)
                            ) {
                                OutlinedTextField(
                                    value = if (targetType.isEmpty()) getTypesString("select_target_type", language) else targetType.toFriendlyTypeName(language),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(getTypesString("target_type", language)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTargetType) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedTargetType,
                                    onDismissRequest = { expandedTargetType = false }
                                ) {
                                    PredefinedTypeGroups.filter { it != "GroupSv" }.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt.toFriendlyTypeName(language)) },
                                            onClick = {
                                                targetType = opt
                                                expandedTargetType = false
                                                focusManager.clearFocus()
                                                
                                                scope.launch {
                                                    val res = apiClient.typeRepository.getAllByType(opt)
                                                    if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
                                                        unassignedChildren = res.data.filter { it.groupId == null }
                                                        checkedChildren.clear()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!isChild && initial == null && unassignedChildren.isNotEmpty()) {
                        Text(getTypesString("unassigned_children", language), style = MaterialTheme.typography.titleSmall)
                        Card(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                            LazyColumn(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                items(unassignedChildren) { child ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = checkedChildren.contains(child),
                                            onCheckedChange = { chk -> 
                                                if (chk) checkedChildren.add(child) else checkedChildren.remove(child)
                                            }
                                        )
                                        Text(child.name)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(getTypesString("attributes", language), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    var showExtDialog by remember { mutableStateOf(false) }
                    TextButton(onClick = { showExtDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(getTypesString("templates_management", language))
                    }

                    if (showExtDialog) {
                        com.suprogramuota_visata.vedlys.ui.components.ExtTemplateDialog(
                            apiClient = apiClient,
                            ownerType = typeStr,
                            onDismiss = { showExtDialog = false },
                            onChanged = { 
                                loadTemplates(typeStr)
                            }
                        )
                    }
                }

                val standardNamesForType = PredefinedStandardFields[typeStr]?.map { it.name }?.toSet() ?: emptySet()
                val nonStandardAttributes = attributes.filter { it.name !in standardNamesForType }

                if (nonStandardAttributes.isEmpty()) {
                    Text(
                        text = if (language == AppLanguage.EN) "No additional attributes defined in template." else "Šablone nėra papildomų atributų.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    nonStandardAttributes.chunked(2).forEach { rowAttrs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowAttrs.forEach { attr ->
                                val index = attributes.indexOfFirst { it.name == attr.name }
                                val extAttr = templates.find { it.id == selectedTemplateId }?.attributes?.find { it.name == attr.name }
                                val isListType = attr.attributeType in listOf("STRING_LIST", "EMAIL_LIST", "URL_LIST")
                                val optionsFromRule6 = extAttr?.validations?.find { it.ruleId == 6 }?.args?.firstOrNull()
                                val optionsStr = optionsFromRule6 ?: extAttr?.defaultValue
                                val options = optionsStr?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                                val localTypeGroup = extAttr?.validations?.find { it.ruleId == 12 }?.args?.firstOrNull()

                                val isError = validationErrors.containsKey(attr.name)
                                val isSuccess = validationSuccesses.containsKey(attr.name) && !isError
                                val fieldLabel = "${attr.name} (${attr.attributeType})"

                                Box(modifier = Modifier.weight(1f)) {
                                    if (attr.attributeType == "BOOLEAN") {
                                        val boolChecked = attr.value.equals("true", ignoreCase = true) || attr.value == "1"
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                        ) {
                                            Switch(
                                                checked = boolChecked,
                                                onCheckedChange = { chk ->
                                                    val updated = attr.copy(value = chk.toString())
                                                    attributes[index] = updated
                                                    dirtyFields[attr.name] = true
                                                }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(fieldLabel, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    } else if (extAttr?.attributeType == "Local_Type" && localTypeGroup != null) {
                                        var expandedVal by remember { mutableStateOf(false) }
                                        val query = localTypeQueries[attr.name] ?: attr.value
                                        val optionsList = localTypeOptions[localTypeGroup] ?: emptyList()
                                        val filtered = optionsList.filter {
                                            query.isBlank() ||
                                            it.id.toString().contains(query, ignoreCase = true) ||
                                            (it.code?.contains(query, ignoreCase = true) == true) ||
                                            it.name.contains(query, ignoreCase = true)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ExposedDropdownMenuBox(
                                                expanded = expandedVal,
                                                onExpandedChange = { expandedVal = !expandedVal },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                OutlinedTextField(
                                                    value = query,
                                                    onValueChange = { newValue ->
                                                        val updated = attr.copy(value = newValue)
                                                        attributes[index] = updated
                                                        localTypeQueries[updated.name] = newValue
                                                        dirtyFields[updated.name] = true
                                                        expandedVal = true
                                                        if (validationErrors.containsKey(updated.name)) {
                                                            validateAttribute(updated, index)
                                                        } else {
                                                            validationSuccesses.remove(updated.name)
                                                        }
                                                    },
                                                    label = { Text(fieldLabel) },
                                                    singleLine = true,
                                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                                        .then(validationModifier(index, attr)),
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVal) },
                                                    colors = if (isSuccess) {
                                                        OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                                            unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                                            focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                                            unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                                        )
                                                    } else {
                                                        ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                                    },
                                                    isError = isError,
                                                    supportingText = validationErrors[attr.name]?.let { { Text(it) } }
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = expandedVal,
                                                    onDismissRequest = { expandedVal = false }
                                                ) {
                                                    if (filtered.isEmpty()) {
                                                        DropdownMenuItem(
                                                            text = { Text(if (language == AppLanguage.EN) "No results" else "Nėra rezultatų") },
                                                            onClick = {}
                                                        )
                                                    } else {
                                                        filtered.forEach { item ->
                                                            val displayStr = listOfNotNull(
                                                                item.id?.toString(),
                                                                item.code?.takeIf { it.isNotBlank() },
                                                                item.name.takeIf { it.isNotBlank() }
                                                            ).joinToString(" - ")
                                                            DropdownMenuItem(
                                                                text = { Text(displayStr) },
                                                                onClick = {
                                                                    attributes[index] = attr.copy(value = displayStr)
                                                                    localTypeQueries[attr.name] = displayStr
                                                                    expandedVal = false
                                                                    focusManager.clearFocus()
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.width(4.dp))
                                            IconButton(
                                                onClick = {
                                                    nestedTypeToCreate = localTypeGroup
                                                    nestedAttributeIndex = index
                                                    showNestedEditor = true
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = if (language == AppLanguage.EN) "Create New" else "Sukurti naują",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    } else if (isListType && options.isNotEmpty()) {
                                        var expandedVal by remember { mutableStateOf(false) }
                                        ExposedDropdownMenuBox(
                                            expanded = expandedVal,
                                            onExpandedChange = { expandedVal = !expandedVal },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedTextField(
                                                value = attr.value,
                                                onValueChange = { newValue ->
                                                    val updated = attr.copy(value = newValue)
                                                    attributes[index] = updated
                                                    dirtyFields[updated.name] = true
                                                    if (validationErrors.containsKey(updated.name)) {
                                                        validateAttribute(updated, index)
                                                    } else {
                                                        validationSuccesses.remove(updated.name)
                                                    }
                                                },
                                                label = { Text(fieldLabel) },
                                                singleLine = true,
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                                    .then(validationModifier(index, attr)),
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVal) },
                                                colors = if (isSuccess) {
                                                    OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                                    )
                                                } else {
                                                    ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                                },
                                                isError = isError,
                                                supportingText = validationErrors[attr.name]?.let { { Text(it) } }
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expandedVal,
                                                onDismissRequest = { expandedVal = false }
                                            ) {
                                                options.forEach { opt ->
                                                    DropdownMenuItem(
                                                        text = { Text(opt) },
                                                        onClick = {
                                                            attributes[index] = attr.copy(value = opt)
                                                            expandedVal = false
                                                            focusManager.clearFocus()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        OutlinedTextField(
                                            value = attr.value,
                                            onValueChange = { newValue ->
                                                val updated = attr.copy(value = newValue)
                                                attributes[index] = updated
                                                dirtyFields[updated.name] = true
                                                if (validationErrors.containsKey(updated.name)) {
                                                    validateAttribute(updated, index)
                                                } else {
                                                    validationSuccesses.remove(updated.name)
                                                }
                                            },
                                            label = { Text(fieldLabel) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                                .then(validationModifier(index, attr)),
                                            trailingIcon = if (isSuccess) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Valid",
                                                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                                    )
                                                }
                                            } else null,
                                            colors = if (isSuccess) {
                                                OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                                )
                                            } else OutlinedTextFieldDefaults.colors(),
                                            isError = isError,
                                            supportingText = validationErrors[attr.name]?.let { { Text(it) } }
                                        )
                                    }
                                }
                            }
                            if (rowAttrs.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    ignoreFocusLoss = true
                    
                    // Validate standard fields that are applicable to current typeStr
                    val activeStdFields = mutableListOf<String>()
                    activeStdFields.add("Pavadinimas")
                    if (typeStr in listOf("ProductSv", "ServiceSv", "LocationSv", "WarehouseSv", "DivisionSv", "PartnerSv", "UnitSv")) {
                        activeStdFields.add("Kodas")
                    }
                    if (typeStr in listOf("ProductSv", "ServiceSv", "LocationSv", "WarehouseSv", "DivisionSv", "UnitSv")) {
                        activeStdFields.add("Brūkšninis kodas")
                    }
                    if (typeStr == "ProductSv" || typeStr == "ServiceSv") {
                        activeStdFields.add("Matavimo vienetas")
                    }
                    if (typeStr == "UnitSv") {
                        activeStdFields.add("Konvertavimo koeficientas")
                    }

                    var allValid = true
                    val standardValues = mapOf(
                        "Pavadinimas" to name,
                        "Kodas" to code,
                        "Brūkšninis kodas" to barcode,
                        "Matavimo vienetas" to baseUnit,
                        "Konvertavimo koeficientas" to conversionFactor
                    )

                    activeStdFields.forEach { fName ->
                        val isValid = validateStandardField(fName, standardValues[fName] ?: "")
                        if (!isValid) allValid = false
                    }

                    val standardNamesForType = PredefinedStandardFields[typeStr]?.map { it.name }?.toSet() ?: emptySet()
                    attributes.forEachIndexed { i, a ->
                        if (a.name in standardNamesForType) return@forEachIndexed
                        val isValid = validateAttribute(a, i)
                        if (!isValid) allValid = false
                    }

                    if (allValid) {
                        val finalAttrs = attributes.map { attr ->
                            if (attr.name in standardNamesForType) {
                                val newVal = when (attr.name) {
                                    "Pavadinimas" -> name
                                    "Kodas" -> code
                                    "Brūkšninis kodas" -> barcode
                                    "Matavimo vienetas" -> baseUnit
                                    "Konvertavimo koeficientas" -> conversionFactor
                                    else -> attr.value
                                }
                                attr.copy(value = newVal)
                            } else attr
                        }
                        onSave(
                            TypeDTO(
                                id = initial?.id,
                                name = name,
                                groupId = if (typeStr == "GroupSv") null else groupId.toIntOrNull(),
                                parentGroupId = if (typeStr == "GroupSv") groupId.toIntOrNull() else null,
                                level = if (typeStr == "GroupSv") computedLevel else null,
                                enabled = enabled,
                                type = typeStr,
                                isChild = if (typeStr == "GroupSv") isChild else null,
                                targetType = if (typeStr == "GroupSv") {
                                    targetType.takeIf { it.isNotBlank() }
                                        ?: groupId.toIntOrNull()?.let { pId -> allGroups.find { it.id == pId }?.targetType }
                                } else null,
                                extTemplateId = selectedTemplateId,
                                attributes = finalAttrs,
                                code = code.takeIf { it.isNotBlank() },
                                barcode = barcode.takeIf { it.isNotBlank() },
                                isCompany = if (typeStr == "PartnerSv") isCompany else null,
                                baseUnit = if (typeStr == "ProductSv" || typeStr == "ServiceSv") baseUnit.takeIf { it.isNotBlank() } else null,
                                baseUnitId = if (typeStr == "UnitSv") baseUnitId else null,
                                conversionFactor = if (typeStr == "UnitSv") conversionFactor.toDoubleOrNull() ?: 1.0 else null
                            ),
                            checkedChildren.toList()
                        )
                        ignoreFocusLoss = false
                    } else {
                        // Focus first invalid field
                        val firstInvalidStd = activeStdFields.firstOrNull { validationErrors.containsKey(it) }
                        if (firstInvalidStd != null) {
                            val req = when (firstInvalidStd) {
                                "Pavadinimas" -> focusRequesterName
                                "Kodas" -> focusRequesterCode
                                "Brūkšninis kodas" -> focusRequesterBarcode
                                "Matavimo vienetas" -> focusRequesterBaseUnit
                                "Konvertavimo koeficientas" -> focusRequesterConversionFactor
                                else -> focusRequesterName
                            }
                            scope.launch {
                                delay(50)
                                req.requestFocus()
                                delay(100)
                                ignoreFocusLoss = false
                            }
                        } else {
                            val firstInvalidAttrIndex = attributes.indexOfFirst { it.name !in standardNamesForType && validationErrors.containsKey(it.name) }
                            if (firstInvalidAttrIndex >= 0) {
                                scope.launch {
                                    delay(50)
                                    focusRequesters[firstInvalidAttrIndex].requestFocus()
                                    delay(100)
                                    ignoreFocusLoss = false
                                }
                            } else {
                                ignoreFocusLoss = false
                            }
                        }
                    }
                }
            ) { Text(if (initial == null) getTypesString("create", language) else getTypesString("save", language)) }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    ignoreFocusLoss = true
                    onDismiss()
                }
            ) { Text(getTypesString("cancel", language)) }
        }
    )

    if (showNestedEditor && nestedTypeToCreate != null) {
        val attrGroup = nestedTypeToCreate!!
        val index = nestedAttributeIndex!!
        val attrName = attributes[index].name
        TypeEditorDialog(
            initial = null,
            defaultTypeName = attrGroup,
            apiClient = apiClient,
            language = language,
            allGroups = allGroups,
            errorMessage = nestedErrorMessage,
            onClearError = { nestedErrorMessage = null },
            onDismiss = { 
                showNestedEditor = false
                nestedTypeToCreate = null
                nestedAttributeIndex = null
                nestedErrorMessage = null
            },
            onSave = { dto, _ ->
                scope.launch {
                    val res = apiClient.typeRepository.create(dto)
                    if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
                        val created = res.data
                        if (created != null) {
                            val currentOptions = localTypeOptions[attrGroup] ?: emptyList()
                            localTypeOptions[attrGroup] = currentOptions + created
                            
                            val displayStr = listOfNotNull(
                                created.id?.toString(),
                                created.code?.takeIf { it.isNotBlank() },
                                created.name.takeIf { it.isNotBlank() }
                            ).joinToString(" - ")
                            
                            attributes[index] = attributes[index].copy(value = displayStr)
                            localTypeQueries[attrName] = displayStr
                            
                            showNestedEditor = false
                            nestedTypeToCreate = null
                            nestedAttributeIndex = null
                            nestedErrorMessage = null
                        }
                    } else if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Error) {
                        nestedErrorMessage = res.message
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun TypesScreenPreview() {
    VedlysTheme {
        Surface {
            TypesScreen(
                apiClient = ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                onBack = {},
                onHome = {}
            )
        }
    }
}

