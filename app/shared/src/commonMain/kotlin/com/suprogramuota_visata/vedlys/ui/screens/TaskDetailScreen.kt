package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.*
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.vedlys.ui.components.SearchableTypeDropdown
import com.suprogramuota_visata.vedlys.ui.components.SelectAllOutlinedTextField
import com.suprogramuota_visata.vedlys.ui.components.ErrorBanner
import com.suprogramuota_visata.vedlys.ui.components.LoadingOverlay
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import com.suprogramuota_visata.vedlys.ui.components.ImageAttributeField
import com.suprogramuota_visata.vedlys.ui.components.ImagePreviewDialog
import com.suprogramuota_visata.vedlys.utils.ImageHelper
import com.suprogramuota_visata.vedlys.viewmodel.TaskDetailViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import com.suprogramuota_visata.vedlys.AppLanguage
import com.suprogramuota_visata.vedlys.AppSettings
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import com.suprogramuota_visata.vedlys.utils.validateFieldValue
import com.suprogramuota_visata.vedlys.ui.components.PredefinedStandardFields

fun getTranslatedUnitName(code: String, name: String, language: AppLanguage): String {
    return if (language == AppLanguage.EN) {
        when (code) {
            "PCE" -> "pcs"
            "KGM" -> "kg"
            "LTR" -> "l"
            "MTR" -> "m"
            "BOX12" -> "Box (12 pcs)"
            "BAG50" -> "Bag (50 kg)"
            else -> name
        }
    } else {
        when (code) {
            "PCE" -> "vnt."
            "KGM" -> "kg"
            "LTR" -> "l"
            "MTR" -> "m"
            "BOX12" -> "Dėžutė (12 vnt.)"
            "BAG50" -> "Maišas (50 kg)"
            else -> name
        }
    }
}

private fun round2(v: Double): Double = Math.round(v * 100.0) / 100.0
private fun round4(v: Double): Double = Math.round(v * 10000.0) / 10000.0

private fun formatDouble(value: Double, decimals: Int): String {
    return String.format(Locale.US, "%.${decimals}f", value)
}

private fun formatTimestamp(time: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(Date(time))
}

private fun handleLinkClick(link: String, onOpenTask: (String, Int?) -> Unit) {
    if (link.startsWith("vedlys://transaction/")) {
        val parts = link.removePrefix("vedlys://transaction/").split("/detail/")
        val transactionId = parts.getOrNull(0)
        val seqId = parts.getOrNull(1)?.toIntOrNull()
        if (transactionId != null) {
            onOpenTask(transactionId, seqId)
        }
    }
}

private fun getGroupForDocumentType(type: String): String {
    val docType = com.suprogramuota_visata.enums.DocumentType.fromId(type)
    if (docType != null) {
        return when (docType.category) {
            com.suprogramuota_visata.enums.DocumentCategory.FINANCIAL -> "Financial"
            com.suprogramuota_visata.enums.DocumentCategory.OPERATIONAL -> "Operational"
            com.suprogramuota_visata.enums.DocumentCategory.DELIVERY -> "Delivery"
            com.suprogramuota_visata.enums.DocumentCategory.CRM -> "CRM"
        }
    }
    for ((group, types) in DocumentGroupTypesMap) {
        if (type in types) return group
    }
    return "Operational"
}

private fun convertDetail(
    source: TransactionDetailDTO,
    targetTransactionId: String,
    nextSeq: Int,
    targetGroup: String,
    sourceTxDocNumber: String? = null,
    sourceTxId: String? = null
): TransactionDetailDTO {
    val itemId = source.itemId
    val description = source.description
    val qty = when (source) {
        is FinancialTransactionDetailDTO -> source.quantity
        is OperationalTransactionDetailDTO -> source.quantity
        is DeliveryTransactionDetailDTO -> source.quantityDelivered
        is CrmTransactionDetailDTO -> 1.0
    }
    val detailTag = sourceTxDocNumber
    val detailLink = if (sourceTxId != null) "vedlys://transaction/$sourceTxId/detail/${source.sequenceId}" else null
    
    return when (targetGroup) {
        "Financial" -> {
            val price = when (source) {
                is FinancialTransactionDetailDTO -> source.price
                else -> 0.0
            }
            val vatRate = when (source) {
                is FinancialTransactionDetailDTO -> source.vatRate
                else -> 21.0
            }
            val vatAmount = when (source) {
                is FinancialTransactionDetailDTO -> source.vatAmount
                else -> round2(qty * price * (vatRate / 100.0))
            }
            val totalAmount = when (source) {
                is FinancialTransactionDetailDTO -> source.totalAmount
                else -> round2(qty * price + vatAmount)
            }
            FinancialTransactionDetailDTO(
                transactionId = targetTransactionId,
                sequenceId = nextSeq,
                itemId = itemId,
                code = source.code,
                barcode = source.barcode,
                description = description,
                quantity = qty,
                price = price,
                vatRate = vatRate,
                vatAmount = vatAmount,
                totalAmount = totalAmount,
                joinedDetailId = source.id,
                tag = detailTag,
                link = detailLink,
                attributes = source.attributes
            )
        }
        "Operational" -> {
            OperationalTransactionDetailDTO(
                transactionId = targetTransactionId,
                sequenceId = nextSeq,
                itemId = itemId,
                code = source.code,
                barcode = source.barcode,
                description = description,
                quantity = qty,
                locationId = 0,
                joinedDetailId = source.id,
                tag = detailTag,
                link = detailLink,
                attributes = source.attributes
            )
        }
        "Delivery" -> {
            DeliveryTransactionDetailDTO(
                transactionId = targetTransactionId,
                sequenceId = nextSeq,
                itemId = itemId,
                code = source.code,
                barcode = source.barcode,
                description = description,
                quantityDelivered = qty,
                joinedDetailId = source.id,
                tag = detailTag,
                link = detailLink,
                attributes = source.attributes
            )
        }
        "CRM" -> {
            CrmTransactionDetailDTO(
                transactionId = targetTransactionId,
                sequenceId = nextSeq,
                itemId = itemId,
                code = source.code,
                barcode = source.barcode,
                description = description,
                joinedDetailId = source.id,
                tag = detailTag,
                link = detailLink,
                attributes = source.attributes
            )
        }
        else -> {
            OperationalTransactionDetailDTO(
                transactionId = targetTransactionId,
                sequenceId = nextSeq,
                itemId = itemId,
                code = source.code,
                barcode = source.barcode,
                description = description,
                quantity = qty,
                locationId = 0,
                joinedDetailId = source.id,
                tag = detailTag,
                link = detailLink,
                attributes = source.attributes
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    apiClient: ApiSvClient,
    isAdmin: Boolean,
    highlightDetailSeq: Int? = null,
    onOpenTask: (String, Int?) -> Unit,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val viewModel = remember { TaskDetailViewModel(apiClient, taskId) }
    DisposableEffect(Unit) { onDispose { viewModel.dispose() } }
    var showAddDetailDialog by remember { mutableStateOf(false) }
    var showEditDetailDialog by remember { mutableStateOf(false) }
    var editingDetail by remember { mutableStateOf<TransactionDetailDTO?>(null) }

    val language by AppSettings.selectedLanguage.collectAsState()
    val allowCompletedDocumentEditing by AppSettings.allowCompletedDocumentEditing.collectAsState()
    val allowEditingOtherDevicesRecords by AppSettings.allowEditingOtherDevicesRecords.collectAsState()
    val allowEditingOtherUsersRecords by AppSettings.allowEditingOtherUsersRecords.collectAsState()
    val currentTerminalId by AppSettings.terminalId.collectAsState()

    var isHeaderExpanded by remember(taskId) {
        mutableStateOf(true)
    }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = if (language == AppLanguage.EN) "Task" else "Užduotis",
                onBack = onBack,
                onHome = onHome,
                actions = {
                    val currentTx = viewModel.transaction
                    if (currentTx != null) {
                        val editPermissionError = AppSettings.checkRecordEditPermission(
                            isCompleted = currentTx.status == "Baigta",
                            recordCreatedOnDeviceId = currentTx.createdOnDeviceId.toString(),
                            recordCreatedByUserId = currentTx.createdByUserId,
                            currentTerminalId = currentTerminalId,
                            isAdmin = isAdmin,
                            language = language
                        )
                        val isReadOnly = editPermissionError != null

                        if (currentTx.status == "Baigta") {
                            Button(
                                onClick = {
                                    if (isReadOnly) viewModel.setError(editPermissionError ?: "")
                                    else viewModel.updateStatus("Vykdoma")
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Koreguoti", style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.saveTransaction(currentTx)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Išsaugoti", style = MaterialTheme.typography.labelMedium)
                            }

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    viewModel.updateStatus("Baigta")
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Užbaigti", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(Modifier.width(4.dp))

                        var showDeleteDialog by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                if (isReadOnly) viewModel.setError(editPermissionError ?: "")
                                else showDeleteDialog = true
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Panaikinti",
                                tint = if (isReadOnly) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Panaikinti užduotį") },
                                text = { Text("Ar tikrai norite panaikinti dokumentą '${currentTx.documentNumber}'?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showDeleteDialog = false
                                            viewModel.deleteTransaction { onBack() }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Panaikinti")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Atšaukti")
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val transaction = viewModel.transaction

            if (transaction != null) {
                val editPermissionError = AppSettings.checkRecordEditPermission(
                    isCompleted = transaction.status == "Baigta",
                    recordCreatedOnDeviceId = transaction.createdOnDeviceId.toString(),
                    recordCreatedByUserId = transaction.createdByUserId,
                    currentTerminalId = currentTerminalId,
                    isAdmin = isAdmin,
                    language = language
                )
                val isReadOnly = editPermissionError != null

                val displayedDetails = remember(transaction.details, viewModel.showSummarized) {
                    if (viewModel.showSummarized) {
                        val groups = transaction.details.groupBy { it.itemId }
                        groups.map { (itemId, list) ->
                            val firstItem = list.first()
                            val totalQty = list.sumOf { detail ->
                                when (detail) {
                                    is OperationalTransactionDetailDTO -> detail.quantity
                                    is FinancialTransactionDetailDTO -> detail.quantity
                                    is DeliveryTransactionDetailDTO -> detail.quantityDelivered
                                    else -> 1.0
                                }
                            }
                            val firstAppearanceIndex = transaction.details.indexOfFirst { it.itemId == itemId }
                            
                            val groupedDetail = when (firstItem) {
                                is OperationalTransactionDetailDTO -> firstItem.copy(quantity = totalQty, sequenceId = firstAppearanceIndex + 1)
                                is FinancialTransactionDetailDTO -> firstItem.copy(quantity = totalQty, sequenceId = firstAppearanceIndex + 1)
                                is DeliveryTransactionDetailDTO -> firstItem.copy(quantityDelivered = totalQty, sequenceId = firstAppearanceIndex + 1)
                                is CrmTransactionDetailDTO -> firstItem.copy(sequenceId = firstAppearanceIndex + 1)
                            }
                            
                            firstAppearanceIndex to groupedDetail
                        }
                        .sortedBy { it.first }
                        .map { it.second }
                    } else {
                        transaction.details
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TransactionHeaderCard(
                            transaction = transaction,
                            isAdmin = isAdmin,
                            apiClient = apiClient,
                            viewModel = viewModel,
                            isExpanded = isHeaderExpanded,
                            onToggleExpand = { isHeaderExpanded = !isHeaderExpanded },
                            onStatusChange = { newStatus -> viewModel.updateStatus(newStatus) }
                        )
                    }

                    item {
                        TransactionLinkingCard(
                            transaction = transaction,
                            apiClient = apiClient,
                            viewModel = viewModel,
                            isAdmin = isAdmin,
                            onOpenTask = onOpenTask
                        )
                    }

                    item {
                        TransactionDetailsSection(
                            transaction = transaction,
                            apiClient = apiClient,
                            viewModel = viewModel,
                            displayedDetails = displayedDetails,
                            isReadOnly = isReadOnly,
                            highlightDetailSeq = highlightDetailSeq,
                            onOpenTask = onOpenTask,
                            onEditClick = { detail ->
                                editingDetail = detail
                                showEditDetailDialog = true
                            },
                            onOpenFullAddDialog = {
                                showAddDetailDialog = true
                            }
                        )
                    }
                }
            }

            if (viewModel.isLoading) {
                LoadingOverlay()
            }

            viewModel.errorMessage?.let {
                ErrorBanner(
                    message = it,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    onDismiss = { viewModel.clearError() }
                )
            }

            if (showAddDetailDialog && transaction != null) {
                TransactionDetailDialog(
                    apiClient = apiClient,
                    transaction = transaction,
                    isAdmin = isAdmin,
                    onDismiss = { showAddDetailDialog = false },
                    onConfirm = { newDetail ->
                        viewModel.addDetail(newDetail)
                        showAddDetailDialog = false
                    }
                )
            }

            if (showEditDetailDialog && transaction != null && editingDetail != null) {
                TransactionDetailDialog(
                    apiClient = apiClient,
                    transaction = transaction,
                    initialDetail = editingDetail,
                    isAdmin = isAdmin,
                    onDismiss = {
                        showEditDetailDialog = false
                        editingDetail = null
                    },
                    onConfirm = { updatedDetail ->
                        val updatedDetails = transaction.details.map { d ->
                            if (d.sequenceId == updatedDetail.sequenceId) {
                                updatedDetail
                            } else {
                                d
                            }
                        }
                        viewModel.saveTransaction(transaction.copy(details = updatedDetails))
                        showEditDetailDialog = false
                        editingDetail = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailsSection(
    transaction: TransactionDTO,
    apiClient: ApiSvClient,
    viewModel: TaskDetailViewModel,
    displayedDetails: List<TransactionDetailDTO>,
    isReadOnly: Boolean,
    highlightDetailSeq: Int?,
    onOpenTask: (String, Int?) -> Unit,
    onEditClick: (TransactionDetailDTO) -> Unit,
    onOpenFullAddDialog: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val detailGroup = getGroupForDocumentType(transaction.documentType)

    // Inline Fast Entry State
    var itemKind by remember { mutableStateOf(if (detailGroup == "CRM") "Service" else "Product") }
    var selectedItem by remember { mutableStateOf<TypeDTO?>(null) }
    var showTypeEditor by remember { mutableStateOf(false) }
    var typeEditorError by remember { mutableStateOf<String?>(null) }
    var allGroupsList by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var quantityStr by remember { mutableStateOf("1") }
    var matasStr by remember { mutableStateOf("vnt.") }
    var priceExStr by remember { mutableStateOf("0.0") }
    var vatRateStr by remember { mutableStateOf("21.0") }
    var sumExStr by remember { mutableStateOf("0.0") }
    var sumInStr by remember { mutableStateOf("0.0") }

    LaunchedEffect(showTypeEditor) {
        if (showTypeEditor && allGroupsList.isEmpty()) {
            when (val res = apiClient.typeRepository.getAllByType("GroupSv")) {
                is ApiResult.Success -> allGroupsList = res.data ?: emptyList()
                else -> {}
            }
        }
    }

    if (showTypeEditor) {
        TypeEditorDialog(
            initial = null,
            defaultTypeName = if (itemKind == "Service") "ServiceSv" else "ProductSv",
            apiClient = apiClient,
            language = com.suprogramuota_visata.vedlys.AppLanguage.LT,
            allGroups = allGroupsList,
            errorMessage = typeEditorError,
            onClearError = { typeEditorError = null },
            onDismiss = {
                showTypeEditor = false
                typeEditorError = null
            },
            onSave = { dto, checkedChildren ->
                scope.launch {
                    val res = apiClient.typeRepository.create(dto)
                    if (res is ApiResult.Success) {
                        val created = res.data
                        if (created != null) {
                            if (checkedChildren.isNotEmpty()) {
                                checkedChildren.forEach { child ->
                                    apiClient.typeRepository.update(child.copy(groupId = created.id))
                                }
                            }
                            selectedItem = created
                            itemKind = if (created.type in listOf("Service", "ServiceSv")) "Service" else "Product"
                            created.baseUnit?.takeIf { it.isNotBlank() }?.let { matasStr = it }
                            showTypeEditor = false
                            typeEditorError = null
                        }
                    } else if (res is ApiResult.Error) {
                        typeEditorError = res.message ?: "Nepavyko sukurti tipo"
                    }
                }
            }
        )
    }

    val itemFocusRequester = remember { FocusRequester() }
    val qtyFocusRequester = remember { FocusRequester() }
    val matasFocusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }
    val vatFocusRequester = remember { FocusRequester() }

    fun recalcSums(qStr: String, pStr: String, vStr: String) {
        val q = qStr.toDoubleOrNull() ?: 1.0
        val p = pStr.toDoubleOrNull() ?: 0.0
        val v = vStr.toDoubleOrNull() ?: 21.0
        val sEx = round2(q * p)
        val vatAmt = round2(sEx * (v / 100.0))
        val sIn = round2(sEx + vatAmt)
        sumExStr = formatDouble(sEx, 2)
        sumInStr = formatDouble(sIn, 2)
    }

    fun addInlineDetail() {
        val item = selectedItem
        if (item == null) {
            viewModel.setError("Pasirinkite prekę arba paslaugą naujai eilutei.")
            scope.launch {
                delay(50)
                itemFocusRequester.requestFocus()
            }
            return
        }
        val q = quantityStr.toDoubleOrNull() ?: 1.0
        if (q <= 0.0) return

        val p = priceExStr.toDoubleOrNull() ?: 0.0
        val v = vatRateStr.toDoubleOrNull() ?: 21.0
        val sEx = round2(q * p)
        val vatAmt = round2(sEx * (v / 100.0))
        val sIn = round2(sEx + vatAmt)

        val nextSeq = (transaction.details.maxOfOrNull { it.sequenceId } ?: 0) + 1
        val defaultAttrs = listOf(
            AttributeDTO(name = "Matas", attributeType = "STRING", value = matasStr)
        )

        val newDetail: TransactionDetailDTO = when (detailGroup) {
            "Financial" -> FinancialTransactionDetailDTO(
                transactionId = transaction.transactionId,
                sequenceId = nextSeq,
                itemId = item.id ?: 0,
                code = item.code,
                barcode = item.barcode,
                description = item.name,
                quantity = q,
                price = p,
                vatRate = v,
                vatAmount = vatAmt,
                totalAmount = sIn,
                attributes = defaultAttrs
            )
            "Operational" -> OperationalTransactionDetailDTO(
                transactionId = transaction.transactionId,
                sequenceId = nextSeq,
                itemId = item.id ?: 0,
                code = item.code,
                barcode = item.barcode,
                description = item.name,
                quantity = q,
                locationId = 0,
                attributes = defaultAttrs
            )
            "Delivery" -> DeliveryTransactionDetailDTO(
                transactionId = transaction.transactionId,
                sequenceId = nextSeq,
                itemId = item.id ?: 0,
                code = item.code,
                barcode = item.barcode,
                description = item.name,
                quantityDelivered = q,
                attributes = defaultAttrs
            )
            "CRM" -> CrmTransactionDetailDTO(
                transactionId = transaction.transactionId,
                sequenceId = nextSeq,
                itemId = item.id ?: 0,
                code = item.code,
                barcode = item.barcode,
                description = item.name,
                attributes = defaultAttrs
            )
            else -> FinancialTransactionDetailDTO(
                transactionId = transaction.transactionId,
                sequenceId = nextSeq,
                itemId = item.id ?: 0,
                code = item.code,
                barcode = item.barcode,
                description = item.name,
                quantity = q,
                price = p,
                vatRate = v,
                vatAmount = vatAmt,
                totalAmount = sIn,
                attributes = defaultAttrs
            )
        }

        viewModel.addDetail(newDetail)

        // Reset fast entry inputs
        selectedItem = null
        quantityStr = "1"
        priceExStr = "0.0"
        sumExStr = "0.0"
        sumInStr = "0.0"

        scope.launch {
            delay(50)
            itemFocusRequester.requestFocus()
        }
    }

    var isExpanded by remember { mutableStateOf(true) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Sudėtis Header Bar (Clickable to expand / collapse, matching other cards)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sudėtis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${displayedDetails.size} eil.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Suskleisti" else "Išskleisti"
                    )
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()

            // Inline Fast-Entry Bar (optimized for single line with maximum density & Tab/Enter navigation)
            if (!isReadOnly) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Prekė / Paslauga (su slankikliu išskleidžiamojo sąrašo viršuje)
                            SearchableTypeDropdown(
                                apiClient = apiClient,
                                typeName = itemKind,
                                label = if (itemKind == "Product") "Prekė" else "Paslauga",
                                selectedTypeId = selectedItem?.id,
                                onSelected = { item ->
                                    selectedItem = item
                                    if (item != null) {
                                        item.baseUnit?.takeIf { it.isNotBlank() }?.let { matasStr = it }
                                        scope.launch {
                                            delay(50)
                                            qtyFocusRequester.requestFocus()
                                        }
                                    }
                                },
                                onOpenFullCreate = {
                                    showTypeEditor = true
                                },
                                kindOptions = listOf("Product" to "Prekė", "Service" to "Paslauga"),
                                selectedKind = itemKind,
                                onKindSelected = { newKind ->
                                    itemKind = newKind
                                    selectedItem = null
                                },
                                modifier = Modifier
                                    .weight(2.4f)
                                    .focusRequester(itemFocusRequester)
                            )

                            // 2. Kiekis
                            SelectAllOutlinedTextField(
                                value = quantityStr,
                                onValueChange = {
                                    quantityStr = it
                                    recalcSums(it, priceExStr, vatRateStr)
                                },
                                label = { Text("Kiekis") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(0.8f)
                                    .focusRequester(qtyFocusRequester)
                                    .onPreviewKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                                            matasFocusRequester.requestFocus()
                                            true
                                        } else false
                                    }
                            )

                            // 3. Matas
                            SelectAllOutlinedTextField(
                                value = matasStr,
                                onValueChange = { matasStr = it },
                                label = { Text("Matas") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(0.7f)
                                    .focusRequester(matasFocusRequester)
                                    .onPreviewKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                                            priceFocusRequester.requestFocus()
                                            true
                                        } else false
                                    }
                            )

                            // 4. Kaina be PVM
                            SelectAllOutlinedTextField(
                                value = priceExStr,
                                onValueChange = {
                                    priceExStr = it
                                    recalcSums(quantityStr, it, vatRateStr)
                                },
                                label = { Text("Kaina be PVM") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(priceFocusRequester)
                                    .onPreviewKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                                            vatFocusRequester.requestFocus()
                                            true
                                        } else false
                                    }
                            )

                            // 5. PVM %
                            SelectAllOutlinedTextField(
                                value = vatRateStr,
                                onValueChange = {
                                    vatRateStr = it
                                    recalcSums(quantityStr, priceExStr, it)
                                },
                                label = { Text("PVM %") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(0.7f)
                                    .focusRequester(vatFocusRequester)
                                    .onPreviewKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                                            addInlineDetail()
                                            true
                                        } else false
                                    }
                            )

                            // 6. Suma be PVM
                            OutlinedTextField(
                                value = sumExStr,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Suma be PVM") },
                                singleLine = true,
                                modifier = Modifier.weight(1.1f)
                            )

                            // 7. Suma su PVM
                            OutlinedTextField(
                                value = sumInStr,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Suma su PVM") },
                                singleLine = true,
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    }
                }
            }

            // Details Table / List
            if (displayedDetails.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nėra įvestų užduoties eilučių. Užpildykite greitojo įvedimo laukus viršuje ir paspauskite '+' mygtuką.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    displayedDetails.forEach { detail ->
                        val isHighlighted = highlightDetailSeq != null && detail.sequenceId == highlightDetailSeq
                        TransactionDetailRow(
                            detail = detail,
                            isReadOnly = isReadOnly,
                            isHighlighted = isHighlighted,
                            apiClient = apiClient,
                            onOpenTask = onOpenTask,
                            onEditClick = { onEditClick(detail) },
                            onDelete = { viewModel.removeDetail(detail.sequenceId) },
                            onUpdateLinking = { joinedId, tag, link ->
                                val updated = when (detail) {
                                    is FinancialTransactionDetailDTO -> detail.copy(joinedDetailId = joinedId, tag = tag, link = link)
                                    is OperationalTransactionDetailDTO -> detail.copy(joinedDetailId = joinedId, tag = tag, link = link)
                                    is DeliveryTransactionDetailDTO -> detail.copy(joinedDetailId = joinedId, tag = tag, link = link)
                                    is CrmTransactionDetailDTO -> detail.copy(joinedDetailId = joinedId, tag = tag, link = link)
                                }
                                val updatedList = transaction.details.map { if (it.sequenceId == detail.sequenceId) updated else it }
                                viewModel.saveTransaction(transaction.copy(details = updatedList))
                            }
                        )
                    }
                }
            }

            // Totals Summary Footer & Floating Action Button
            val finDetails = displayedDetails.filterIsInstance<FinancialTransactionDetailDTO>()
            val totalEx = finDetails.sumOf { it.quantity * it.price }
            val totalVat = finDetails.sumOf { it.vatAmount }
            val totalIn = finDetails.sumOf { it.totalAmount }

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = if (!isReadOnly) 64.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Viso eilučių: ${displayedDetails.size}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (finDetails.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Suma be PVM: ${formatDouble(totalEx, 2)} €",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "PVM suma: ${formatDouble(totalVat, 2)} €",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Galutinė suma su PVM: ${formatDouble(totalIn, 2)} €",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (!isReadOnly) {
                    FloatingActionButton(
                        onClick = { addInlineDetail() },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pridėti naują eilutę",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionHeaderCard(
    transaction: TransactionDTO,
    isAdmin: Boolean,
    apiClient: ApiSvClient,
    viewModel: TaskDetailViewModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val allowCompletedDocumentEditing by AppSettings.allowCompletedDocumentEditing.collectAsState()
    val allowEditingOtherDevicesRecords by AppSettings.allowEditingOtherDevicesRecords.collectAsState()
    val allowEditingOtherUsersRecords by AppSettings.allowEditingOtherUsersRecords.collectAsState()
    val currentTerminalId by AppSettings.terminalId.collectAsState()

    val editPermissionError = AppSettings.checkRecordEditPermission(
        isCompleted = transaction.status == "Baigta",
        recordCreatedOnDeviceId = transaction.createdOnDeviceId.toString(),
        recordCreatedByUserId = transaction.createdByUserId,
        currentTerminalId = currentTerminalId,
        isAdmin = isAdmin,
        language = AppSettings.selectedLanguage.value
    )
    val isReadOnly = editPermissionError != null

    val validationErrors = remember { mutableStateMapOf<String, String>() }
    val validationSuccesses = remember { mutableStateMapOf<String, Boolean>() }
    val dirtyFields = remember { mutableStateMapOf<String, Boolean>() }
    var ignoreFocusLoss by remember { mutableStateOf(false) }

    fun validateTransactionField(fieldName: String, value: String): Boolean {
        val extAttr = viewModel.extTemplate?.attributes?.find { it.name == fieldName }
        if (extAttr == null || !extAttr.validateRule) {
            validationErrors.remove(fieldName)
            validationSuccesses.remove(fieldName)
            return true
        }
        val error = validateFieldValue(fieldName, value, extAttr, AppSettings.selectedLanguage.value)
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

    val focusRequesterDocNum = remember { FocusRequester() }
    val focusRequesterDocDate = remember { FocusRequester() }
    val focusRequesterPartner = remember { FocusRequester() }
    val focusRequesterWhFrom = remember { FocusRequester() }
    val focusRequesterWhTo = remember { FocusRequester() }
    val focusRequesterDeptFrom = remember { FocusRequester() }
    val focusRequesterDeptTo = remember { FocusRequester() }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val transactionValidationModifier = { fieldName: String, requester: FocusRequester, nextRequester: FocusRequester?, validateFn: () -> Boolean ->
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
                            if (nextRequester != null) {
                                nextRequester.requestFocus()
                            } else {
                                focusManager.moveFocus(FocusDirection.Next)
                            }
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

    val standardNames = setOf("Iš Sandėlio", "Į Sandėlį", "Iš Padalinio", "Į Padalinį", "Partneris", "Dokumento numeris", "Dokumento data")
    val customAttributes = remember(transaction.attributes, viewModel.extTemplate) {
        val tplAttrs = viewModel.extTemplate?.attributes ?: emptyList()
        val nonStandardTpl = tplAttrs.filter { it.name !in standardNames }
        val existingMap = transaction.attributes.associateBy { it.name }
        
        val result = mutableListOf<AttributeDTO>()
        val seenNames = mutableSetOf<String>()
        
        nonStandardTpl.forEach { ext ->
            val existing = existingMap[ext.name]
            result.add(
                existing ?: AttributeDTO(
                    name = ext.name,
                    attributeType = ext.attributeType,
                    validate = ext.validateRule,
                    value = ext.defaultValue ?: "",
                    validations = ext.validations,
                    tag = ext.tag
                )
            )
            seenNames.add(ext.name)
        }
        
        transaction.attributes.filter { it.name !in standardNames && it.name !in seenNames }.forEach {
            result.add(it)
        }
        result
    }
    val customFocusRequesters = remember(customAttributes.size) {
        List(customAttributes.size) { FocusRequester() }
    }

    DisposableEffect(Unit) {
        onDispose {
            ignoreFocusLoss = true
        }
    }

    fun validateAll(): Boolean {
        ignoreFocusLoss = true
        var allValid = true

        val activeFields = mutableListOf<Pair<String, String>>()
        activeFields.add("Dokumento numeris" to transaction.documentNumber)
        val dateVal = formatTimestamp(transaction.documentDate)
        activeFields.add("Dokumento data" to dateVal)

        val partnerVal = transaction.attributes.find { it.name == "Partneris" }?.value ?: ""
        val whFromVal = transaction.attributes.find { it.name == "Iš Sandėlio" }?.value ?: ""
        val whToVal = transaction.attributes.find { it.name == "Į Sandėlį" }?.value ?: ""
        val deptFromVal = transaction.attributes.find { it.name == "Iš Padalinio" }?.value ?: ""
        val deptToVal = transaction.attributes.find { it.name == "Į Padalinį" }?.value ?: ""

        activeFields.add("Partneris" to partnerVal)
        activeFields.add("Iš Sandėlio" to whFromVal)
        activeFields.add("Į Sandėlį" to whToVal)
        activeFields.add("Iš Padalinio" to deptFromVal)
        activeFields.add("Į Padalinį" to deptToVal)

        customAttributes.forEach { attr ->
            activeFields.add(attr.name to attr.value)
        }

        activeFields.forEach { (name, value) ->
            dirtyFields[name] = true
            val isValid = validateTransactionField(name, value)
            if (!isValid) allValid = false
        }

        if (!allValid) {
            val firstInvalidName = activeFields.firstOrNull { validationErrors.containsKey(it.first) }?.first
            if (firstInvalidName != null) {
                val req = when (firstInvalidName) {
                    "Dokumento numeris" -> focusRequesterDocNum
                    "Dokumento data" -> focusRequesterDocDate
                    "Partneris" -> focusRequesterPartner
                    "Iš Sandėlio" -> focusRequesterWhFrom
                    "Į Sandėlį" -> focusRequesterWhTo
                    "Iš Padalinio" -> focusRequesterDeptFrom
                    "Į Padalinį" -> focusRequesterDeptTo
                    else -> {
                        val customIndex = customAttributes.indexOfFirst { it.name == firstInvalidName }
                        if (customIndex >= 0) customFocusRequesters.getOrNull(customIndex) else null
                    }
                }
                scope.launch {
                    delay(50)
                    req?.requestFocus()
                    delay(100)
                    ignoreFocusLoss = false
                }
            } else {
                ignoreFocusLoss = false
            }
        } else {
            ignoreFocusLoss = false
        }

        return allValid
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Header Title Bar + Summary + Template Selector + Toggle Expand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Užduotis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (transaction.status.isNotBlank()) {
                        Surface(
                            color = when (transaction.status) {
                                "Nauja" -> MaterialTheme.colorScheme.primaryContainer
                                "Vykdoma" -> MaterialTheme.colorScheme.tertiaryContainer
                                "Baigta" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = transaction.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!isExpanded) {
                        val partnerVal = transaction.attributes.find { it.name == "Partneris" }?.value
                        val docTypeStr = getTranslatedDocumentType(transaction.documentType, AppSettings.selectedLanguage.value)
                        val summaryText = buildString {
                            if (transaction.documentNumber.isNotBlank()) append("Nr. ${transaction.documentNumber} • ")
                            append(docTypeStr)
                            if (!partnerVal.isNullOrBlank()) append(" • $partnerVal")
                            append(" • ${formatTimestamp(transaction.documentDate)}")
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Template Selector & Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isExpanded) {
                        var tplExpanded by remember { mutableStateOf(false) }
                        val currentTpl = viewModel.extTemplate
                        val templates = viewModel.availableTemplates

                        if (templates.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = tplExpanded,
                                onExpandedChange = { if (!isReadOnly) tplExpanded = !tplExpanded }
                            ) {
                                OutlinedTextField(
                                    value = currentTpl?.name ?: "Numatytasis dokumento šablonas",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Dokumento šablonas") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tplExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor().width(280.dp),
                                    singleLine = true,
                                    enabled = !isReadOnly
                                )
                                ExposedDropdownMenu(
                                    expanded = tplExpanded && !isReadOnly,
                                    onDismissRequest = { tplExpanded = false },
                                    modifier = Modifier.widthIn(min = 280.dp)
                                ) {
                                    templates.forEach { tpl ->
                                        val labelText = if (tpl.isDefault && !tpl.name.contains("Numatytas", ignoreCase = true)) {
                                            "${tpl.name} (Numatytasis)"
                                        } else {
                                            tpl.name
                                        }
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = labelText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            onClick = {
                                                viewModel.changeTemplate(tpl)
                                                tplExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        var showExtDialog by remember { mutableStateOf(false) }
                        if (isAdmin) {
                            IconButton(onClick = { showExtDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Šablono redagavimas")
                            }
                            if (showExtDialog) {
                                com.suprogramuota_visata.vedlys.ui.components.ExtTemplateDialog(
                                    apiClient = apiClient,
                                    ownerType = "TransactionSv",
                                    onDismiss = { showExtDialog = false },
                                    onChanged = { viewModel.loadTask() }
                                )
                            }
                        }
                    }

                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Suskleisti užduotį" else "Išskleisti užduotį"
                        )
                    }
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider()

            // Eilutė 1: Dokumento tipas, Dokumento numeris, Data, Statusas (Maksimaliai vienoje eilutėje su svoriais)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Dokumento tipas (Dropdown)
                var docTypeExpanded by remember { mutableStateOf(false) }
                val docTypes = remember { PredefinedDocumentTypes.filter { it != "Visi" } }
                ExposedDropdownMenuBox(
                    expanded = docTypeExpanded,
                    onExpandedChange = { if (!isReadOnly) docTypeExpanded = !docTypeExpanded },
                    modifier = Modifier.weight(1.2f)
                ) {
                    OutlinedTextField(
                        value = getTranslatedDocumentType(transaction.documentType, AppSettings.selectedLanguage.value),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dokumento tipas") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true,
                        enabled = !isReadOnly
                    )
                    ExposedDropdownMenu(
                        expanded = docTypeExpanded && !isReadOnly,
                        onDismissRequest = { docTypeExpanded = false }
                    ) {
                        docTypes.forEach { dt ->
                            DropdownMenuItem(
                                text = { Text(getTranslatedDocumentType(dt, AppSettings.selectedLanguage.value)) },
                                onClick = {
                                    viewModel.updateDocumentType(dt)
                                    docTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                if (isReadOnly) {
                    Text(text = "Nr. ${transaction.documentNumber}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1.1f))
                    Text(text = "Data: ${formatTimestamp(transaction.documentDate)}", modifier = Modifier.weight(1.1f))
                    Text(text = "Statusas: ${transaction.status} (Tik skaitymui)", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(0.9f))
                } else {
                    // 2. Dokumento numeris
                    val isDocNumError = validationErrors.containsKey("Dokumento numeris")
                    val isDocNumSuccess = validationSuccesses.containsKey("Dokumento numeris")
                    val docNumSupportingText = validationErrors["Dokumento numeris"]
                    val docNumColors = if (isDocNumSuccess) {
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                            focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                            unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                        )
                    } else null

                    SelectAllOutlinedTextField(
                        value = transaction.documentNumber,
                        onValueChange = {
                            viewModel.updateDocumentNumber(it)
                            dirtyFields["Dokumento numeris"] = true
                            validateTransactionField("Dokumento numeris", it)
                        },
                        label = { Text("Dokumento numeris") },
                        isError = isDocNumError,
                        supportingText = docNumSupportingText?.let { { Text(it) } },
                        colors = docNumColors ?: OutlinedTextFieldDefaults.colors(),
                        trailingIcon = if (isDocNumSuccess) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Valid",
                                    tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .weight(1.1f)
                            .then(transactionValidationModifier("Dokumento numeris", focusRequesterDocNum, focusRequesterDocDate) {
                                validateTransactionField("Dokumento numeris", transaction.documentNumber)
                            }),
                        singleLine = true
                    )

                    // 3. Dokumento data
                    var dateStr by remember(transaction.documentDate) { mutableStateOf(formatTimestamp(transaction.documentDate)) }
                    val isDocDateError = validationErrors.containsKey("Dokumento data")
                    val isDocDateSuccess = validationSuccesses.containsKey("Dokumento data")
                    val docDateSupportingText = validationErrors["Dokumento data"]
                    val docDateColors = if (isDocDateSuccess) {
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                            focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                            unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                        )
                    } else null

                    SelectAllOutlinedTextField(
                        value = dateStr,
                        onValueChange = {
                            dateStr = it
                            dirtyFields["Dokumento data"] = true
                            try {
                                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                val parsed = formatter.parse(it)?.time
                                if (parsed != null) viewModel.updateDocumentDate(parsed)
                            } catch (e: Exception) {}
                            validateTransactionField("Dokumento data", it)
                        },
                        label = { Text("Data (yyyy-MM-dd HH:mm)") },
                        isError = isDocDateError,
                        supportingText = docDateSupportingText?.let { { Text(it) } },
                        colors = docDateColors ?: OutlinedTextFieldDefaults.colors(),
                        trailingIcon = if (isDocDateSuccess) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Valid",
                                    tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .weight(1.1f)
                            .then(transactionValidationModifier("Dokumento data", focusRequesterDocDate, focusRequesterPartner) {
                                validateTransactionField("Dokumento data", dateStr)
                            }),
                        singleLine = true
                    )

                    // 4. Statusas
                    var statusExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        OutlinedTextField(
                            value = transaction.status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Statusas") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            PredefinedStatuses.forEach { status ->
                                if (status != "Visi") {
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            if (validateAll()) {
                                                onStatusChange(status)
                                                statusExpanded = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Eilutė 2: Visi standartiniai šablono laukai vienoje horizontalioje eilutėje (Partneris, Iš Sandėlio, Į Sandėlį, Iš Padalinio, Į Padalinį)
            val partnerId = transaction.attributes.find { it.name == "Partneris" }?.value?.toIntOrNull()
            val whFromId = transaction.attributes.find { it.name == "Iš Sandėlio" }?.value?.toIntOrNull()
            val whToId = transaction.attributes.find { it.name == "Į Sandėlį" }?.value?.toIntOrNull()
            val deptFromId = transaction.attributes.find { it.name == "Iš Padalinio" }?.value?.toIntOrNull()
            val deptToId = transaction.attributes.find { it.name == "Į Padalinį" }?.value?.toIntOrNull()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Partneris (weight 1.3f)
                val isPartnerError = validationErrors.containsKey("Partneris")
                val isPartnerSuccess = validationSuccesses.containsKey("Partneris")
                val partnerSupportingText = validationErrors["Partneris"]
                val partnerColors = if (isPartnerSuccess) {
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    )
                } else null

                SearchablePartnerLookup(
                    apiClient = apiClient,
                    selectedPartnerId = partnerId,
                    onSelected = { p ->
                        if (p != null) {
                            viewModel.updateAttribute("Partneris", "Partner", p.id.toString())
                            dirtyFields["Partneris"] = true
                            validateTransactionField("Partneris", p.id.toString())
                        } else {
                            viewModel.removeAttribute("Partneris")
                            dirtyFields["Partneris"] = true
                            validateTransactionField("Partneris", "")
                        }
                    },
                    isError = isPartnerError,
                    supportingText = partnerSupportingText,
                    colors = partnerColors,
                    modifier = Modifier
                        .weight(1.3f)
                        .then(transactionValidationModifier("Partneris", focusRequesterPartner, focusRequesterWhFrom) {
                            val cur = transaction.attributes.find { it.name == "Partneris" }?.value ?: ""
                            validateTransactionField("Partneris", cur)
                        }),
                    enabled = !isReadOnly
                )

                // 2. Iš Sandėlio (weight 1f)
                val isWhFromError = validationErrors.containsKey("Iš Sandėlio")
                val isWhFromSuccess = validationSuccesses.containsKey("Iš Sandėlio")
                val whFromColors = if (isWhFromSuccess) {
                    ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    )
                } else null

                SearchableTypeDropdown(
                    apiClient = apiClient,
                    typeName = "Warehouse",
                    label = "Iš Sandėlio",
                    selectedTypeId = whFromId,
                    onSelected = { t ->
                        if (t != null) {
                            viewModel.updateAttribute("Iš Sandėlio", "Warehouse", t.id.toString())
                            dirtyFields["Iš Sandėlio"] = true
                            validateTransactionField("Iš Sandėlio", t.id.toString())
                        } else {
                            viewModel.removeAttribute("Iš Sandėlio")
                            dirtyFields["Iš Sandėlio"] = true
                            validateTransactionField("Iš Sandėlio", "")
                        }
                    },
                    isError = isWhFromError,
                    supportingText = validationErrors["Iš Sandėlio"],
                    colors = whFromColors,
                    modifier = Modifier
                        .weight(1f)
                        .then(transactionValidationModifier("Iš Sandėlio", focusRequesterWhFrom, focusRequesterWhTo) {
                            val cur = transaction.attributes.find { it.name == "Iš Sandėlio" }?.value ?: ""
                            validateTransactionField("Iš Sandėlio", cur)
                        })
                )

                // 3. Į Sandėlį (weight 1f)
                val isWhToError = validationErrors.containsKey("Į Sandėlį")
                val isWhToSuccess = validationSuccesses.containsKey("Į Sandėlį")
                val whToColors = if (isWhToSuccess) {
                    ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    )
                } else null

                SearchableTypeDropdown(
                    apiClient = apiClient,
                    typeName = "Warehouse",
                    label = "Į Sandėlį",
                    selectedTypeId = whToId,
                    onSelected = { t ->
                        if (t != null) {
                            viewModel.updateAttribute("Į Sandėlį", "Warehouse", t.id.toString())
                            dirtyFields["Į Sandėlį"] = true
                            validateTransactionField("Į Sandėlį", t.id.toString())
                        } else {
                            viewModel.removeAttribute("Į Sandėlį")
                            dirtyFields["Į Sandėlį"] = true
                            validateTransactionField("Į Sandėlį", "")
                        }
                    },
                    isError = isWhToError,
                    supportingText = validationErrors["Į Sandėlį"],
                    colors = whToColors,
                    modifier = Modifier
                        .weight(1f)
                        .then(transactionValidationModifier("Į Sandėlį", focusRequesterWhTo, focusRequesterDeptFrom) {
                            val cur = transaction.attributes.find { it.name == "Į Sandėlį" }?.value ?: ""
                            validateTransactionField("Į Sandėlį", cur)
                        })
                )

                // 4. Iš Padalinio (weight 1f)
                val isDeptFromError = validationErrors.containsKey("Iš Padalinio")
                val isDeptFromSuccess = validationSuccesses.containsKey("Iš Padalinio")
                val deptFromColors = if (isDeptFromSuccess) {
                    ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    )
                } else null

                SearchableTypeDropdown(
                    apiClient = apiClient,
                    typeName = "Department",
                    label = "Iš Padalinio",
                    selectedTypeId = deptFromId,
                    onSelected = { t ->
                        if (t != null) {
                            viewModel.updateAttribute("Iš Padalinio", "Department", t.id.toString())
                            dirtyFields["Iš Padalinio"] = true
                            validateTransactionField("Iš Padalinio", t.id.toString())
                        } else {
                            viewModel.removeAttribute("Iš Padalinio")
                            dirtyFields["Iš Padalinio"] = true
                            validateTransactionField("Iš Padalinio", "")
                        }
                    },
                    isError = isDeptFromError,
                    supportingText = validationErrors["Iš Padalinio"],
                    colors = deptFromColors,
                    modifier = Modifier
                        .weight(1f)
                        .then(transactionValidationModifier("Iš Padalinio", focusRequesterDeptFrom, focusRequesterDeptTo) {
                            val cur = transaction.attributes.find { it.name == "Iš Padalinio" }?.value ?: ""
                            validateTransactionField("Iš Padalinio", cur)
                        })
                )

                // 5. Į Padalinį (weight 1f)
                val isDeptToError = validationErrors.containsKey("Į Padalinį")
                val isDeptToSuccess = validationSuccesses.containsKey("Į Padalinį")
                val deptToColors = if (isDeptToSuccess) {
                    ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    )
                } else null

                val nextAfterDeptTo = customFocusRequesters.firstOrNull()
                SearchableTypeDropdown(
                    apiClient = apiClient,
                    typeName = "Department",
                    label = "Į Padalinį",
                    selectedTypeId = deptToId,
                    onSelected = { t ->
                        if (t != null) {
                            viewModel.updateAttribute("Į Padalinį", "Department", t.id.toString())
                            dirtyFields["Į Padalinį"] = true
                            validateTransactionField("Į Padalinį", t.id.toString())
                        } else {
                            viewModel.removeAttribute("Į Padalinį")
                            dirtyFields["Į Padalinį"] = true
                            validateTransactionField("Į Padalinį", "")
                        }
                    },
                    isError = isDeptToError,
                    supportingText = validationErrors["Į Padalinį"],
                    colors = deptToColors,
                    modifier = Modifier
                        .weight(1f)
                        .then(transactionValidationModifier("Į Padalinį", focusRequesterDeptTo, nextAfterDeptTo) {
                            val cur = transaction.attributes.find { it.name == "Į Padalinį" }?.value ?: ""
                            validateTransactionField("Į Padalinį", cur)
                        })
                )
            }

            // Eilutė 3+: Papildomi šablono atributai (Išdėstyti po 4 į vieną eilutę maksimaliam tankumui)
            if (customAttributes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Papildomi šablono atributai:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                customAttributes.chunked(4).forEachIndexed { rowIndex, chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chunk.forEachIndexed { colIndex, attr ->
                            val index = rowIndex * 4 + colIndex
                            val focusRequester = customFocusRequesters.getOrNull(index) ?: FocusRequester()
                            val nextRequester = customFocusRequesters.getOrNull(index + 1)
                            val isError = validationErrors.containsKey(attr.name)
                            val isSuccess = validationSuccesses.containsKey(attr.name)
                            val supportingText = validationErrors[attr.name]
                            val colors = if (isSuccess) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            } else null

                            Box(modifier = Modifier.weight(1f)) {
                                if (attr.attributeType == "JSON_STRING") {
                                    if (isReadOnly) {
                                        val imgUuid = ImageHelper.extractImageUuid(attr.value)
                                        if (imgUuid != null) {
                                            var previewOpen by remember { mutableStateOf(false) }
                                            IconButton(onClick = { previewOpen = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.Visibility,
                                                    contentDescription = "Nuotrauka",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            if (previewOpen) {
                                                ImagePreviewDialog(
                                                    imageUuid = imgUuid,
                                                    apiClient = apiClient,
                                                    title = attr.name,
                                                    onDismiss = { previewOpen = false }
                                                )
                                            }
                                        } else {
                                            Text("${attr.name}: —", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    } else {
                                        ImageAttributeField(
                                            label = attr.name,
                                            value = attr.value,
                                            apiClient = apiClient,
                                            language = AppSettings.selectedLanguage.value,
                                            onValueChange = { newVal ->
                                                viewModel.updateAttribute(attr.name, attr.attributeType, newVal)
                                                dirtyFields[attr.name] = true
                                            }
                                        )
                                    }
                                } else if (isReadOnly) {
                                    Text("${attr.name}: ${attr.value}", style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    SelectAllOutlinedTextField(
                                        value = attr.value,
                                        onValueChange = {
                                            viewModel.updateAttribute(attr.name, attr.attributeType, it)
                                            dirtyFields[attr.name] = true
                                            validateTransactionField(attr.name, it)
                                        },
                                        label = { Text(attr.name) },
                                        isError = isError,
                                        supportingText = supportingText?.let { { Text(it) } },
                                        colors = colors ?: OutlinedTextFieldDefaults.colors(),
                                        trailingIcon = if (isSuccess) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Valid",
                                                    tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(transactionValidationModifier(attr.name, focusRequester, nextRequester) {
                                                validateTransactionField(attr.name, attr.value)
                                            }),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                        repeat(4 - chunk.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
}
}

@Composable
private fun TransactionDetailRow(
    detail: TransactionDetailDTO,
    isReadOnly: Boolean,
    isHighlighted: Boolean,
    apiClient: ApiSvClient,
    onOpenTask: (String, Int?) -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onUpdateLinking: (Int?, String?, String?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isHighlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = if (isHighlighted) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Seq ID
            Text(
                text = "#${detail.sequenceId}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            
            // 2. Name + Code / Barcode (weight 1.5f)
            Column(modifier = Modifier.weight(1.5f)) {
                Text(text = detail.description, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                val meta = listOfNotNull(
                    detail.code?.let { "Kodas: $it" },
                    detail.barcode?.let { "Brūkšninis: $it" } ?: "ID: ${detail.itemId}"
                ).joinToString(" | ")
                Text(text = meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val extraMeta = listOfNotNull(
                    detail.attributes.find { it.name == "Sandėlis" }?.value?.takeIf { it.isNotBlank() }?.let { "Sandėlis: $it" },
                    detail.attributes.find { it.name == "Padalinys" }?.value?.takeIf { it.isNotBlank() }?.let { "Padalinys: $it" }
                ).joinToString(" | ")
                if (extraMeta.isNotEmpty()) {
                    Text(text = extraMeta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 3. Subtype values (weight 1f)
            Column(modifier = Modifier.weight(1f)) {
                when (detail) {
                    is FinancialTransactionDetailDTO -> {
                        val matas = detail.attributes.find { it.name == "Matas" }?.value ?: "vnt."
                        Text("Kiekis: ${detail.quantity} $matas", style = MaterialTheme.typography.bodyMedium)
                        Text("Kaina be PVM: ${detail.price} €", style = MaterialTheme.typography.bodySmall)
                    }
                    is OperationalTransactionDetailDTO -> {
                        val matas = detail.attributes.find { it.name == "Matas" }?.value ?: "vnt."
                        Text("Kiekis: ${detail.quantity} $matas", style = MaterialTheme.typography.bodyMedium)
                        val lotSerial = listOfNotNull(
                            detail.lotNumber?.let { "Partija: $it" },
                            detail.serialNumber?.let { "Serija: $it" }
                        ).joinToString(", ")
                        if (lotSerial.isNotEmpty()) {
                            Text(lotSerial, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is DeliveryTransactionDetailDTO -> {
                        val matas = detail.attributes.find { it.name == "Matas" }?.value ?: "vnt."
                        Text("Kiekis: ${detail.quantityDelivered} $matas", style = MaterialTheme.typography.bodyMedium)
                        detail.trackingNumber?.let { Text("Sekimo Nr: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                    is CrmTransactionDetailDTO -> {
                        val crmInfo = listOfNotNull(
                            detail.serviceDurationMinutes?.let { "Trukmė: $it min" },
                            detail.priority?.let { "Prioritetas: $it" }
                        ).joinToString(", ")
                        Text(crmInfo, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 4. Total Sum (weight 0.8f)
            if (detail is FinancialTransactionDetailDTO) {
                Column(modifier = Modifier.weight(0.8f), horizontalAlignment = Alignment.End) {
                    Text("Suma su PVM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${detail.totalAmount} €", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Spacer(modifier = Modifier.weight(0.8f))
            }

            // 5. Link Badge (if linked)
            if (detail.joinedDetailId != null || !detail.tag.isNullOrBlank() || !detail.link.isNullOrBlank()) {
                val hasVedlysLink = detail.link?.startsWith("vedlys://transaction/") == true
                IconButton(
                    onClick = { if (hasVedlysLink) handleLinkClick(detail.link!!, onOpenTask) },
                    modifier = Modifier.size(32.dp),
                    enabled = hasVedlysLink
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Nuoroda",
                        tint = if (hasVedlysLink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(32.dp))
            }

            // Photo preview button if detail has a photo
            val photoUuid = detail.attributes.firstNotNullOfOrNull { attr ->
                if (attr.attributeType == "JSON_STRING" || attr.name.contains("Nuotrauk", ignoreCase = true)) {
                    ImageHelper.extractImageUuid(attr.value)
                } else null
            }
            if (photoUuid != null) {
                var showPhotoPreview by remember { mutableStateOf(false) }
                IconButton(onClick = { showPhotoPreview = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Nuotrauka",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (showPhotoPreview) {
                    ImagePreviewDialog(
                        imageUuid = photoUuid,
                        apiClient = apiClient,
                        title = detail.description,
                        onDismiss = { showPhotoPreview = false }
                    )
                }
            }

            // 6. Action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isReadOnly) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Redaguoti", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Sąsaja", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Trinti", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        var joinedIdStr by remember { mutableStateOf(detail.joinedDetailId?.toString() ?: "") }
        var rowTag by remember { mutableStateOf(detail.tag ?: "") }
        var rowLink by remember { mutableStateOf(detail.link ?: "") }
        val isDetailTagLinkEditable = detail.joinedDetailId == null && !isReadOnly

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Redaguoti eilutės sąsają") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = joinedIdStr,
                        onValueChange = { joinedIdStr = it },
                        label = { Text("Susieta su eilutės ID (skaičius)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rowTag,
                        onValueChange = { if (isDetailTagLinkEditable) rowTag = it },
                        label = { Text("Eilutės žymė (Tag)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = isDetailTagLinkEditable
                    )
                    OutlinedTextField(
                        value = rowLink,
                        onValueChange = { if (isDetailTagLinkEditable) rowLink = it },
                        label = { Text("Eilutės nuoroda (Link)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = isDetailTagLinkEditable
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idVal = joinedIdStr.toIntOrNull()
                        val nextTag = if (idVal == null && detail.joinedDetailId != null) null else rowTag.takeIf { it.isNotBlank() }
                        val nextLink = if (idVal == null && detail.joinedDetailId != null) null else rowLink.takeIf { it.isNotBlank() }
                        onUpdateLinking(idVal, nextTag, nextLink)
                        showEditDialog = false
                    }
                ) {
                    Text("Išsaugoti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailDialog(
    apiClient: ApiSvClient,
    transaction: TransactionDTO,
    initialDetail: TransactionDetailDTO? = null,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (TransactionDetailDTO) -> Unit
) {
    val detailGroup = getGroupForDocumentType(transaction.documentType)
    val scope = rememberCoroutineScope()
    
    var itemType by remember { mutableStateOf("Prekė") }
    var itemTypeExpanded by remember { mutableStateOf(false) }
    
    var selectedItemId by remember { mutableStateOf<Int?>(initialDetail?.itemId) }
    var selectedItemCode by remember { mutableStateOf<String?>(initialDetail?.code) }
    var selectedItemBarcode by remember { mutableStateOf<String?>(initialDetail?.barcode) }
    var itemDescription by remember { mutableStateOf(initialDetail?.description ?: "") }
    var searchQuery by remember { 
        mutableStateOf(
            initialDetail?.let { 
                "${it.itemId} - ${it.code ?: ""} - ${it.description}"
            } ?: ""
        ) 
    }
    
    var quantityStr by remember { 
        mutableStateOf(
            when (initialDetail) {
                is FinancialTransactionDetailDTO -> initialDetail.quantity.toString()
                is OperationalTransactionDetailDTO -> initialDetail.quantity.toString()
                is DeliveryTransactionDetailDTO -> initialDetail.quantityDelivered.toString()
                else -> "1.0"
            }
        ) 
    }
    var matasStr by remember {
        mutableStateOf(
            initialDetail?.attributes?.find { it.name == "Matas" }?.value ?: "vnt."
        )
    }
    
    val language by com.suprogramuota_visata.vedlys.AppSettings.selectedLanguage.collectAsState()
    
    var selectedWarehouseName by remember {
        mutableStateOf(
            initialDetail?.attributes?.find { it.name == "Sandėlis" }?.value ?: ""
        )
    }
    var selectedDivisionName by remember {
        mutableStateOf(
            initialDetail?.attributes?.find { it.name == "Padalinys" }?.value ?: ""
        )
    }
    
    // States for Financial fields
    var priceExStr by remember { 
        mutableStateOf(
            (initialDetail as? FinancialTransactionDetailDTO)?.price?.toString() ?: "0.0"
        ) 
    }
    var priceInStr by remember { 
        mutableStateOf(
            (initialDetail as? FinancialTransactionDetailDTO)?.let { 
                val pIn = it.price * (1 + it.vatRate / 100.0)
                formatDouble(pIn, 2)
            } ?: "0.0"
        ) 
    }
    var vatRateStr by remember { 
        mutableStateOf(
            (initialDetail as? FinancialTransactionDetailDTO)?.vatRate?.toString() ?: "21.0"
        ) 
    }
    var vatAmountStr by remember { 
        mutableStateOf(
            (initialDetail as? FinancialTransactionDetailDTO)?.vatAmount?.toString() ?: "0.0"
        ) 
    }
    var sumExStr by remember { 
        mutableStateOf(
            (initialDetail as? FinancialTransactionDetailDTO)?.let { 
                formatDouble(it.quantity * it.price, 2)
            } ?: "0.0"
        ) 
    }
    var sumInStr by remember { 
        mutableStateOf(
            (initialDetail as? FinancialTransactionDetailDTO)?.totalAmount?.toString() ?: "0.0"
        ) 
    }

    // Custom Attributes & Templates Logic
    var selectedTemplateId by remember { mutableStateOf<Int?>(initialDetail?.extTemplateId) }
    var templatesList by remember { mutableStateOf<List<ExtTemplateDTO>>(emptyList()) }
    var activeTemplate by remember { mutableStateOf<ExtTemplateDTO?>(null) }
    val customAttributeValues = remember { mutableStateMapOf<String, String>() }

    val detailOwnerType = remember(detailGroup) {
        when (detailGroup) {
            "Financial" -> "FinancialTransactionDetailSv"
            "Operational" -> "OperationalTransactionDetailSv"
            "Delivery" -> "DeliveryTransactionDetailSv"
            "CRM" -> "CrmTransactionDetailSv"
            else -> "OperationalTransactionDetailSv"
        }
    }

    LaunchedEffect(detailOwnerType) {
        val res = apiClient.extTemplateRepository.getTemplatesByOwnerType(detailOwnerType)
        if (res.isSuccess) {
            templatesList = res.getOrNull() ?: emptyList()
            if (selectedTemplateId == null) {
                selectedTemplateId = templatesList.find { it.isDefault }?.id
            }
        }
    }

    LaunchedEffect(selectedTemplateId) {
        val tplId = selectedTemplateId
        if (tplId != null) {
            val res = apiClient.extTemplateRepository.getTemplateById(tplId)
            if (res.isSuccess) {
                activeTemplate = res.getOrNull()
            }
        } else {
            activeTemplate = null
        }
    }

    LaunchedEffect(initialDetail) {
        if (initialDetail != null) {
            initialDetail.attributes.forEach { attr ->
                if (attr.name !in setOf("Matas", "Sandėlis", "Padalinys")) {
                    customAttributeValues[attr.name] = attr.value
                }
            }
        }
    }

    LaunchedEffect(activeTemplate) {
        val template = activeTemplate
        if (template != null) {
            template.attributes.forEach { attr ->
                if (attr.name !in setOf("Matas", "Sandėlis", "Padalinys") && !customAttributeValues.containsKey(attr.name)) {
                    customAttributeValues[attr.name] = attr.defaultValue ?: ""
                }
            }
        }
    }

    // Recalculation engine
    fun onQtyChanged(newQtyStr: String) {
        quantityStr = newQtyStr
        val qty = newQtyStr.toDoubleOrNull() ?: return
        val priceEx = priceExStr.toDoubleOrNull() ?: return
        val vatRate = vatRateStr.toDoubleOrNull() ?: return
        
        val sumEx = round2(qty * priceEx)
        val vatAmount = round2(sumEx * (vatRate / 100.0))
        val sumIn = round2(sumEx + vatAmount)
        
        sumExStr = formatDouble(sumEx, 2)
        vatAmountStr = formatDouble(vatAmount, 2)
        sumInStr = formatDouble(sumIn, 2)
    }

    fun onVatRateChanged(newVatStr: String) {
        vatRateStr = newVatStr
        val vatRate = newVatStr.toDoubleOrNull() ?: return
        val qty = quantityStr.toDoubleOrNull() ?: return
        val priceEx = priceExStr.toDoubleOrNull() ?: return
        
        val priceIn = round2(priceEx * (1 + vatRate / 100.0))
        val sumEx = round2(qty * priceEx)
        val vatAmount = round2(sumEx * (vatRate / 100.0))
        val sumIn = round2(sumEx + vatAmount)
        
        priceInStr = formatDouble(priceIn, 2)
        sumExStr = formatDouble(sumEx, 2)
        vatAmountStr = formatDouble(vatAmount, 2)
        sumInStr = formatDouble(sumIn, 2)
    }

    fun onPriceExChanged(newPriceExStr: String) {
        priceExStr = newPriceExStr
        val priceEx = newPriceExStr.toDoubleOrNull() ?: return
        val qty = quantityStr.toDoubleOrNull() ?: return
        val vatRate = vatRateStr.toDoubleOrNull() ?: return
        
        val priceIn = round2(priceEx * (1 + vatRate / 100.0))
        val sumEx = round2(qty * priceEx)
        val vatAmount = round2(sumEx * (vatRate / 100.0))
        val sumIn = round2(sumEx + vatAmount)
        
        priceInStr = formatDouble(priceIn, 2)
        sumExStr = formatDouble(sumEx, 2)
        vatAmountStr = formatDouble(vatAmount, 2)
        sumInStr = formatDouble(sumIn, 2)
    }

    fun onPriceInChanged(newPriceInStr: String) {
        priceInStr = newPriceInStr
        val priceIn = newPriceInStr.toDoubleOrNull() ?: return
        val qty = quantityStr.toDoubleOrNull() ?: return
        val vatRate = vatRateStr.toDoubleOrNull() ?: return
        
        val priceEx = round4(priceIn / (1 + vatRate / 100.0))
        val sumEx = round2(qty * priceEx)
        val vatAmount = round2(sumEx * (vatRate / 100.0))
        val sumIn = round2(sumEx + vatAmount)
        
        priceExStr = formatDouble(priceEx, 4)
        sumExStr = formatDouble(sumEx, 2)
        vatAmountStr = formatDouble(vatAmount, 2)
        sumInStr = formatDouble(sumIn, 2)
    }

    fun onSumExChanged(newSumExStr: String) {
        sumExStr = newSumExStr
        val sumEx = newSumExStr.toDoubleOrNull() ?: return
        val qty = quantityStr.toDoubleOrNull() ?: return
        val vatRate = vatRateStr.toDoubleOrNull() ?: return
        
        val vatAmount = round2(sumEx * (vatRate / 100.0))
        val sumIn = round2(sumEx + vatAmount)
        
        val priceEx = if (qty > 0.0) round4(sumEx / qty) else 0.0
        val priceIn = round2(priceEx * (1 + vatRate / 100.0))
        
        priceExStr = formatDouble(priceEx, 4)
        priceInStr = formatDouble(priceIn, 2)
        vatAmountStr = formatDouble(vatAmount, 2)
        sumInStr = formatDouble(sumIn, 2)
    }

    fun onSumInChanged(newSumInStr: String) {
        sumInStr = newSumInStr
        val sumIn = newSumInStr.toDoubleOrNull() ?: return
        val qty = quantityStr.toDoubleOrNull() ?: return
        val vatRate = vatRateStr.toDoubleOrNull() ?: return
        
        val sumEx = round2(sumIn / (1 + vatRate / 100.0))
        val vatAmount = round2(sumIn - sumEx)
        
        val priceEx = if (qty > 0.0) round4(sumEx / qty) else 0.0
        val priceIn = round2(priceEx * (1 + vatRate / 100.0))
        
        priceExStr = formatDouble(priceEx, 4)
        priceInStr = formatDouble(priceIn, 2)
        sumExStr = formatDouble(sumEx, 2)
        vatAmountStr = formatDouble(vatAmount, 2)
    }
    
    // Subtype-specific fields
    var lotNumberStr by remember { 
        mutableStateOf(
            (initialDetail as? OperationalTransactionDetailDTO)?.lotNumber ?: ""
        ) 
    }
    var serialNumberStr by remember { 
        mutableStateOf(
            (initialDetail as? OperationalTransactionDetailDTO)?.serialNumber ?: ""
        ) 
    }
    var trackingNumberStr by remember { 
        mutableStateOf(
            (initialDetail as? DeliveryTransactionDetailDTO)?.trackingNumber ?: ""
        ) 
    }
    var serviceDurationStr by remember { 
        mutableStateOf(
            (initialDetail as? CrmTransactionDetailDTO)?.serviceDurationMinutes?.toString() ?: ""
        ) 
    }
    var priorityStr by remember { 
        mutableStateOf(
            (initialDetail as? CrmTransactionDetailDTO)?.priority ?: ""
        ) 
    }
    
    var warehousesList by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var divisionsList by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        when (val res = apiClient.typeRepository.getAllByType("Warehouse")) {
            is ApiResult.Success -> warehousesList = res.data ?: emptyList()
            is ApiResult.Error -> {}
        }
        when (val res = apiClient.typeRepository.getAllByType("Division")) {
            is ApiResult.Success -> divisionsList = res.data ?: emptyList()
            is ApiResult.Error -> {}
        }
    }

    var itemsList by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var isLoadingItems by remember { mutableStateOf(false) }
    var showBrowseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(itemType) {
        isLoadingItems = true
        val typeName = if (itemType == "Prekė") "Product" else "Service"
        when (val result = apiClient.typeRepository.getAllByType(typeName)) {
            is ApiResult.Success -> {
                itemsList = result.data ?: emptyList()
            }
            is ApiResult.Error -> {
                itemsList = emptyList()
            }
        }
        isLoadingItems = false
    }

    val filteredItems = remember(searchQuery, itemsList) {
        if (searchQuery.length < 3) {
            emptyList()
        } else {
            itemsList.filter { item ->
                val displayStr = "${item.id} ${item.code ?: ""} ${item.barcode ?: ""} ${item.name}"
                displayStr.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val titleText = if (initialDetail != null) "Redaguoti eilutę" else "Pridėti eilutę"
    val confirmButtonText = if (initialDetail != null) "Išsaugoti" else "Pridėti"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // 0. Šablono pasirinkimas
                var templateExpanded by remember { mutableStateOf(false) }
                var showDetailExtDialog by remember { mutableStateOf(false) }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ExposedDropdownMenuBox(
                        expanded = templateExpanded,
                        onExpandedChange = { templateExpanded = !templateExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = templatesList.find { it.id == selectedTemplateId }?.name ?: "Nėra šablono",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Eilutės šablonas") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = templateExpanded,
                            onDismissRequest = { templateExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nėra šablono") },
                                onClick = {
                                    selectedTemplateId = null
                                    templateExpanded = false
                                }
                            )
                            templatesList.forEach { tpl ->
                                DropdownMenuItem(
                                    text = { Text(tpl.name + if (tpl.isDefault) " (Default)" else "") },
                                    onClick = {
                                        selectedTemplateId = tpl.id
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { showDetailExtDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Išplėsti")
                        }
                    }
                }

                if (showDetailExtDialog) {
                    com.suprogramuota_visata.vedlys.ui.components.ExtTemplateDialog(
                        apiClient = apiClient,
                        ownerType = detailOwnerType,
                        onDismiss = { showDetailExtDialog = false },
                        onChanged = {
                            scope.launch {
                                val res = apiClient.extTemplateRepository.getTemplatesByOwnerType(detailOwnerType)
                                if (res.isSuccess) {
                                    templatesList = res.getOrNull() ?: emptyList()
                                }
                            }
                        }
                    )
                }

                // 1. Type selection (Prekė / Paslauga)
                ExposedDropdownMenuBox(
                    expanded = itemTypeExpanded,
                    onExpandedChange = { itemTypeExpanded = !itemTypeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = itemType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipas") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemTypeExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = itemTypeExpanded,
                        onDismissRequest = { itemTypeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Prekė") },
                            onClick = {
                                itemType = "Prekė"
                                itemTypeExpanded = false
                                selectedItemId = null
                                itemDescription = ""
                                searchQuery = ""
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Paslauga") },
                            onClick = {
                                itemType = "Paslauga"
                                itemTypeExpanded = false
                                selectedItemId = null
                                itemDescription = ""
                                searchQuery = ""
                            }
                        )
                    }
                }

                // 2. Search Autocomplete with Browse Button
                Box(modifier = Modifier.fillMaxWidth()) {
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(filteredItems) {
                        dropdownExpanded = filteredItems.isNotEmpty()
                    }
                    
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded && searchQuery.length >= 3,
                        onExpandedChange = {}
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    if (it.isBlank()) {
                                        selectedItemId = null
                                        itemDescription = ""
                                    }
                                },
                                label = { Text("Pasirinkite prekę / paslaugą (min. 3 simb.)") },
                                modifier = Modifier.weight(1f).menuAnchor(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showBrowseDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Ieškoti / Naršyti"
                                )
                            }
                        }
                        
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded && searchQuery.length >= 3,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            filteredItems.take(10).forEach { item ->
                                val displayStr = "${item.id} - ${item.code ?: ""} - ${item.barcode ?: ""} - ${item.name}"
                                DropdownMenuItem(
                                    text = { Text(displayStr) },
                                    onClick = {
                                        selectedItemId = item.id
                                        selectedItemCode = item.code
                                        selectedItemBarcode = item.barcode
                                        itemDescription = item.name
                                        searchQuery = displayStr
                                        dropdownExpanded = false
                                        val bu = item.baseUnit
                                        val itemMatas = if (!bu.isNullOrBlank()) {
                                            getTranslatedUnitName(bu, bu, language)
                                        } else {
                                            item.attributes.find { it.name.contains("Matas", ignoreCase = true) || it.name.contains("Mato", ignoreCase = true) || it.name.contains("Unit", ignoreCase = true) }?.value ?: "vnt."
                                        }
                                        matasStr = itemMatas
                                        val itemPrice = item.attributes.find { it.name.contains("Kaina", ignoreCase = true) || it.name.contains("Price", ignoreCase = true) }?.value?.toDoubleOrNull()
                                        if (itemPrice != null) {
                                            onPriceExChanged(itemPrice.toString())
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (detailGroup != "CRM") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelectAllOutlinedTextField(
                            value = quantityStr,
                            onValueChange = { 
                                if (detailGroup == "Financial") {
                                    onQtyChanged(it)
                                } else {
                                    quantityStr = it
                                }
                            },
                            label = { Text("Kiekis") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = matasStr,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Matas") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var whExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = whExpanded,
                        onExpandedChange = { whExpanded = !whExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedWarehouseName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sandėlis") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = whExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = whExpanded,
                            onDismissRequest = { whExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Pasirinkite...") },
                                onClick = {
                                    selectedWarehouseName = ""
                                    whExpanded = false
                                }
                            )
                            warehousesList.forEach { wh ->
                                DropdownMenuItem(
                                    text = { Text(wh.name) },
                                    onClick = {
                                        selectedWarehouseName = wh.name
                                        whExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var divExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = divExpanded,
                        onExpandedChange = { divExpanded = !divExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedDivisionName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Padalinys") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = divExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = divExpanded,
                            onDismissRequest = { divExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Pasirinkite...") },
                                onClick = {
                                    selectedDivisionName = ""
                                    divExpanded = false
                                }
                            )
                            divisionsList.forEach { div ->
                                DropdownMenuItem(
                                    text = { Text(div.name) },
                                    onClick = {
                                        selectedDivisionName = div.name
                                        divExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                when (detailGroup) {
                    "Financial" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectAllOutlinedTextField(
                                value = priceExStr,
                                onValueChange = { onPriceExChanged(it) },
                                label = { Text("Kaina be PVM") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            SelectAllOutlinedTextField(
                                value = priceInStr,
                                onValueChange = { onPriceInChanged(it) },
                                label = { Text("Kaina su PVM") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectAllOutlinedTextField(
                                value = vatRateStr,
                                onValueChange = { onVatRateChanged(it) },
                                label = { Text("PVM (%)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = vatAmountStr,
                                onValueChange = {},
                                label = { Text("PVM suma") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                readOnly = true
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectAllOutlinedTextField(
                                value = sumExStr,
                                onValueChange = { onSumExChanged(it) },
                                label = { Text("Suma be PVM") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            SelectAllOutlinedTextField(
                                value = sumInStr,
                                onValueChange = { onSumInChanged(it) },
                                label = { Text("Suma su PVM") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    "Operational" -> {
                        SelectAllOutlinedTextField(
                            value = lotNumberStr,
                            onValueChange = { lotNumberStr = it },
                            label = { Text("Partijos numeris (Lot)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        SelectAllOutlinedTextField(
                            value = serialNumberStr,
                            onValueChange = { serialNumberStr = it },
                            label = { Text("Serijinis numeris (Serial)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    "Delivery" -> {
                        SelectAllOutlinedTextField(
                            value = trackingNumberStr,
                            onValueChange = { trackingNumberStr = it },
                            label = { Text("Sekimo numeris (Tracking Nr.)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    "CRM" -> {
                        SelectAllOutlinedTextField(
                            value = serviceDurationStr,
                            onValueChange = { serviceDurationStr = it },
                            label = { Text("Paslaugos trukmė (min.)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        SelectAllOutlinedTextField(
                            value = priorityStr,
                            onValueChange = { priorityStr = it },
                            label = { Text("Prioritetas") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Dinamiškai atvaizduojame papildomus šablono atributus
                val customAttrsList = activeTemplate?.attributes?.filter { it.name !in setOf("Matas", "Sandėlis", "Padalinys") } ?: emptyList()
                if (customAttrsList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Papildomi atributai", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    customAttrsList.forEach { attr ->
                        val currentVal = customAttributeValues[attr.name] ?: ""
                        if (attr.attributeType == "JSON_STRING") {
                            ImageAttributeField(
                                label = "${attr.name} (${attr.attributeType})",
                                value = currentVal,
                                apiClient = apiClient,
                                language = language,
                                onValueChange = { newVal ->
                                    customAttributeValues[attr.name] = newVal
                                }
                            )
                        } else {
                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { newVal ->
                                    customAttributeValues[attr.name] = newVal
                                },
                                label = { Text(attr.name) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val qty = quantityStr.toDoubleOrNull() ?: 1.0
            val seqId = initialDetail?.sequenceId ?: ((transaction.details.maxOfOrNull { it.sequenceId } ?: 0) + 1)
            
            val isValid = selectedItemId != null && when (detailGroup) {
                "Financial" -> quantityStr.toDoubleOrNull() != null && priceExStr.toDoubleOrNull() != null && vatRateStr.toDoubleOrNull() != null
                "Operational" -> quantityStr.toDoubleOrNull() != null
                "Delivery" -> quantityStr.toDoubleOrNull() != null
                "CRM" -> serviceDurationStr.isEmpty() || serviceDurationStr.toIntOrNull() != null
                else -> true
            }

            Button(
                onClick = {
                    val currentAttrs = initialDetail?.attributes?.toMutableList() ?: mutableListOf()
                    
                    // Matas
                    val matasIndex = currentAttrs.indexOfFirst { it.name == "Matas" }
                    if (matasIndex >= 0) {
                        currentAttrs[matasIndex] = currentAttrs[matasIndex].copy(value = matasStr)
                    } else {
                        currentAttrs.add(AttributeDTO(name = "Matas", attributeType = "STRING", value = matasStr))
                    }
                    
                    // Sandėlis
                    val whIndex = currentAttrs.indexOfFirst { it.name == "Sandėlis" }
                    if (whIndex >= 0) {
                        currentAttrs[whIndex] = currentAttrs[whIndex].copy(value = selectedWarehouseName)
                    } else {
                        currentAttrs.add(AttributeDTO(name = "Sandėlis", attributeType = "WarehouseSv", value = selectedWarehouseName))
                    }
                    
                    // Padalinys
                    val divIndex = currentAttrs.indexOfFirst { it.name == "Padalinys" }
                    if (divIndex >= 0) {
                        currentAttrs[divIndex] = currentAttrs[divIndex].copy(value = selectedDivisionName)
                    } else {
                        currentAttrs.add(AttributeDTO(name = "Padalinys", attributeType = "DivisionSv", value = selectedDivisionName))
                    }

                    // Dinaminiai šablono atributai
                    val standardNames = setOf("Matas", "Sandėlis", "Padalinys")
                    currentAttrs.removeAll { it.name !in standardNames }
                    customAttributeValues.forEach { (name, value) ->
                        val attrType = activeTemplate?.attributes?.find { it.name == name }?.attributeType ?: "STRING"
                        currentAttrs.add(AttributeDTO(name = name, attributeType = attrType, value = value))
                    }

                    val newDetail = when (detailGroup) {
                        "Financial" -> {
                            val price = priceExStr.toDoubleOrNull() ?: 0.0
                            val vatRate = vatRateStr.toDoubleOrNull() ?: 21.0
                            val vatAmount = vatAmountStr.toDoubleOrNull() ?: 0.0
                            val totalAmount = sumInStr.toDoubleOrNull() ?: 0.0
                            
                            FinancialTransactionDetailDTO(
                                id = initialDetail?.id,
                                transactionId = transaction.transactionId,
                                sequenceId = seqId,
                                itemId = selectedItemId ?: 0,
                                code = selectedItemCode,
                                barcode = selectedItemBarcode,
                                description = itemDescription.takeIf { it.isNotBlank() } ?: "Nežinoma prekė/paslauga",
                                quantity = qty,
                                price = price,
                                vatRate = vatRate,
                                vatAmount = vatAmount,
                                totalAmount = totalAmount,
                                attributes = currentAttrs,
                                extTemplateId = selectedTemplateId,
                                joinedDetailId = initialDetail?.joinedDetailId,
                                tag = initialDetail?.tag,
                                link = initialDetail?.link
                            )
                        }
                        "Operational" -> {
                            OperationalTransactionDetailDTO(
                                id = initialDetail?.id,
                                transactionId = transaction.transactionId,
                                sequenceId = seqId,
                                itemId = selectedItemId ?: 0,
                                code = selectedItemCode,
                                barcode = selectedItemBarcode,
                                description = itemDescription.takeIf { it.isNotBlank() } ?: "Nežinoma prekė/paslauga",
                                quantity = qty,
                                locationId = (initialDetail as? OperationalTransactionDetailDTO)?.locationId ?: 0,
                                lotNumber = lotNumberStr.takeIf { it.isNotBlank() },
                                serialNumber = serialNumberStr.takeIf { it.isNotBlank() },
                                expiryDate = (initialDetail as? OperationalTransactionDetailDTO)?.expiryDate,
                                attributes = currentAttrs,
                                extTemplateId = selectedTemplateId,
                                joinedDetailId = initialDetail?.joinedDetailId,
                                tag = initialDetail?.tag,
                                link = initialDetail?.link
                            )
                        }
                        "Delivery" -> {
                            DeliveryTransactionDetailDTO(
                                id = initialDetail?.id,
                                transactionId = transaction.transactionId,
                                sequenceId = seqId,
                                itemId = selectedItemId ?: 0,
                                code = selectedItemCode,
                                barcode = selectedItemBarcode,
                                description = itemDescription.takeIf { it.isNotBlank() } ?: "Nežinoma prekė/paslauga",
                                quantityDelivered = qty,
                                weight = (initialDetail as? DeliveryTransactionDetailDTO)?.weight,
                                dimensions = (initialDetail as? DeliveryTransactionDetailDTO)?.dimensions,
                                trackingNumber = trackingNumberStr.takeIf { it.isNotBlank() },
                                attributes = currentAttrs,
                                extTemplateId = selectedTemplateId,
                                joinedDetailId = initialDetail?.joinedDetailId,
                                tag = initialDetail?.tag,
                                link = initialDetail?.link
                            )
                        }
                        "CRM" -> {
                            CrmTransactionDetailDTO(
                                id = initialDetail?.id,
                                transactionId = transaction.transactionId,
                                sequenceId = seqId,
                                itemId = selectedItemId ?: 0,
                                code = selectedItemCode,
                                barcode = selectedItemBarcode,
                                description = itemDescription.takeIf { it.isNotBlank() } ?: "Nežinoma prekė/paslauga",
                                serviceDurationMinutes = serviceDurationStr.toIntOrNull(),
                                priority = priorityStr.takeIf { it.isNotBlank() },
                                assignedToUserId = (initialDetail as? CrmTransactionDetailDTO)?.assignedToUserId,
                                resolutionStatus = (initialDetail as? CrmTransactionDetailDTO)?.resolutionStatus,
                                attributes = currentAttrs,
                                extTemplateId = selectedTemplateId,
                                joinedDetailId = initialDetail?.joinedDetailId,
                                tag = initialDetail?.tag,
                                link = initialDetail?.link
                            )
                        }
                        else -> {
                            OperationalTransactionDetailDTO(
                                id = initialDetail?.id,
                                transactionId = transaction.transactionId,
                                sequenceId = seqId,
                                itemId = selectedItemId ?: 0,
                                code = selectedItemCode,
                                barcode = selectedItemBarcode,
                                description = itemDescription.takeIf { it.isNotBlank() } ?: "Nežinoma prekė/paslauga",
                                quantity = qty,
                                locationId = 0,
                                attributes = currentAttrs,
                                extTemplateId = selectedTemplateId,
                                joinedDetailId = initialDetail?.joinedDetailId,
                                tag = initialDetail?.tag,
                                link = initialDetail?.link
                            )
                        }
                    }
                    onConfirm(newDetail)
                },
                enabled = isValid
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Atšaukti") }
        }
    )

    if (showBrowseDialog) {
        var browseSearchQuery by remember { mutableStateOf(searchQuery) }
        val browseFilteredItems = remember(browseSearchQuery, itemsList) {
            itemsList.filter { item ->
                val displayStr = "${item.id} ${item.code ?: ""} ${item.barcode ?: ""} ${item.name}"
                displayStr.contains(browseSearchQuery, ignoreCase = true)
            }
        }
        
        AlertDialog(
            onDismissRequest = { showBrowseDialog = false },
            title = { Text("Pasirinkti prekę / paslaugą (${if (itemType == "Prekė") "Prekės" else "Paslaugos"})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.width(400.dp).height(300.dp)) {
                    OutlinedTextField(
                        value = browseSearchQuery,
                        onValueChange = { browseSearchQuery = it },
                        label = { Text("Ieškoti...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isLoadingItems) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (browseFilteredItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Rezultatų nerasta")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(browseFilteredItems) { item ->
                                val displayStr = "${item.id} - ${item.code ?: ""} - ${item.barcode ?: ""} - ${item.name}"
                                ListItem(
                                    headlineContent = { Text(displayStr) },
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedItemId = item.id
                                        selectedItemCode = item.code
                                        selectedItemBarcode = item.barcode
                                        itemDescription = item.name
                                        searchQuery = displayStr
                                        showBrowseDialog = false
                                        val bu = item.baseUnit
                                        val itemMatas = if (!bu.isNullOrBlank()) {
                                            getTranslatedUnitName(bu, bu, language)
                                        } else {
                                            item.attributes.find { it.name.contains("Matas", ignoreCase = true) || it.name.contains("Mato", ignoreCase = true) || it.name.contains("Unit", ignoreCase = true) }?.value ?: "vnt."
                                        }
                                        matasStr = itemMatas
                                        val itemPrice = item.attributes.find { it.name.contains("Kaina", ignoreCase = true) || it.name.contains("Price", ignoreCase = true) }?.value?.toDoubleOrNull()
                                        if (itemPrice != null) {
                                            onPriceExChanged(itemPrice.toString())
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBrowseDialog = false }) {
                    Text("Uždaryti")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionLinkingCard(
    transaction: TransactionDTO,
    apiClient: ApiSvClient,
    viewModel: TaskDetailViewModel,
    isAdmin: Boolean,
    onOpenTask: (String, Int?) -> Unit
) {
    val isReadOnly = !isAdmin && transaction.status == "Baigta"
    val scope = rememberCoroutineScope()
    
    // State for collapsing/expanding the card
    var isExpanded by remember { mutableStateOf(false) }
    
    // State for loading linked transaction
    var linkedTx by remember { mutableStateOf<TransactionDTO?>(null) }
    var isLoadingLink by remember { mutableStateOf(false) }
    
    // State for child/related transactions
    var childTxs by remember { mutableStateOf<List<TransactionDTO>>(emptyList()) }
    
    // Loading all transactions for link dropdown selector
    var allTxs by remember { mutableStateOf<List<TransactionDTO>>(emptyList()) }
    var selectedLinkTxId by remember { mutableStateOf("") }
    var linkGroupFilter by remember { mutableStateOf("CRM") }
    var copyDetailsCheck by remember { mutableStateOf(true) }
    
    // Tag and Link state
    var headerTag by remember(transaction.tag) { mutableStateOf(transaction.tag ?: "") }
    var headerLink by remember(transaction.link) { mutableStateOf(transaction.link ?: "") }
    
    // Dropdown expanded states
    var groupDropdownExpanded by remember { mutableStateOf(false) }
    var txDropdownExpanded by remember { mutableStateOf(false) }
    
    // Export state
    var exportGroup by remember { mutableStateOf("Financial") }
    var exportDocType by remember { mutableStateOf("SALES_INVOICE") }
    var exportGroupExpanded by remember { mutableStateOf(false) }
    var exportDocTypeExpanded by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Load linked transaction
    LaunchedEffect(transaction.joinedTransactionId) {
        val joinedId = transaction.joinedTransactionId
        if (!joinedId.isNullOrBlank()) {
            isLoadingLink = true
            when (val res = apiClient.transactionRepository.getByTransactionId(joinedId)) {
                is ApiResult.Success -> { linkedTx = res.data }
                is ApiResult.Error -> { linkedTx = null }
            }
            isLoadingLink = false
        } else {
            linkedTx = null
        }
    }

    // Load child transactions and all transactions (for dropdown)
    fun refreshLinks() {
        scope.launch {
            when (val res = apiClient.transactionRepository.getTransactions()) {
                is ApiResult.Success -> {
                    val list = res.data ?: emptyList()
                    allTxs = list.filter { it.transactionId != transaction.transactionId }
                    childTxs = list.filter { it.joinedTransactionId == transaction.transactionId }
                }
                is ApiResult.Error -> {
                    allTxs = emptyList()
                    childTxs = emptyList()
                }
            }
        }
    }

    LaunchedEffect(transaction.transactionId) {
        refreshLinks()
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dokumento sąsajos ir nukreipimas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Suskleisti" else "Išskleisti"
                )
            }
            
            if (isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Tag and Link Fields
                    val isHeaderTagLinkEditable = transaction.joinedTransactionId.isNullOrBlank() && transaction.status != "Baigta"
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = headerTag,
                            onValueChange = { if (isHeaderTagLinkEditable) headerTag = it },
                            label = { Text("Antraštės žymė (Tag)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = isHeaderTagLinkEditable,
                            trailingIcon = {
                                if (isHeaderTagLinkEditable) {
                                    IconButton(onClick = {
                                        viewModel.saveTransaction(transaction.copy(tag = headerTag.takeIf { it.isNotBlank() }))
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Saugoti žymę")
                                    }
                                }
                            }
                        )
                        
                        val hasHeaderLink = headerLink.startsWith("vedlys://transaction/")
                        OutlinedTextField(
                            value = headerLink,
                            onValueChange = { if (isHeaderTagLinkEditable) headerLink = it },
                            label = { Text("Antraštės nuoroda (Link/URL)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = isHeaderTagLinkEditable || hasHeaderLink,
                            readOnly = !isHeaderTagLinkEditable,
                            colors = if (hasHeaderLink) OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.primary
                            ) else OutlinedTextFieldDefaults.colors(),
                            trailingIcon = {
                                if (isHeaderTagLinkEditable) {
                                    IconButton(onClick = {
                                        viewModel.saveTransaction(transaction.copy(link = headerLink.takeIf { it.isNotBlank() }))
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Saugoti nuorodą")
                                    }
                                } else if (hasHeaderLink) {
                                    IconButton(onClick = {
                                        handleLinkClick(headerLink, onOpenTask)
                                    }) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = "Atidaryti nuorodą")
                                    }
                                }
                            }
                        )
                    }

                    HorizontalDivider()

                    // 2. Display Parent Link (Joined to)
                    if (linkedTx != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Susieta su (Tėvinis dokumentas):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                Text(
                                    "${linkedTx?.documentNumber ?: "ID: " + transaction.joinedTransactionId} (${linkedTx?.documentType})",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Statusas: ${linkedTx?.status}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!isReadOnly) {
                                TextButton(
                                    onClick = {
                                        viewModel.saveTransaction(transaction.copy(joinedTransactionId = null, tag = null, link = null))
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.LinkOff, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Panaikinti sąsają")
                                }
                            }
                        }
                    } else if (!transaction.joinedTransactionId.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Kraunamas susietas dokumentas: ${transaction.joinedTransactionId}...", style = MaterialTheme.typography.bodyMedium)
                            if (!isReadOnly) {
                                IconButton(onClick = { viewModel.saveTransaction(transaction.copy(joinedTransactionId = null, tag = null, link = null)) }) {
                                    Icon(Icons.Default.LinkOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    } else {
                        // Link Setup Form (dense layout in a single horizontal row)
                        if (!isReadOnly) {
                            Text("Sukurti sąsają su kitu dokumentu:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Group dropdown
                                ExposedDropdownMenuBox(
                                    expanded = groupDropdownExpanded,
                                    onExpandedChange = { groupDropdownExpanded = !groupDropdownExpanded },
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    OutlinedTextField(
                                        value = linkGroupFilter,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Žurnalas") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        singleLine = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = groupDropdownExpanded,
                                        onDismissRequest = { groupDropdownExpanded = false }
                                    ) {
                                        listOf("CRM", "Financial", "Operational", "Delivery").forEach { g ->
                                            DropdownMenuItem(
                                                text = { Text(g) },
                                                onClick = {
                                                    linkGroupFilter = g
                                                    selectedLinkTxId = ""
                                                    groupDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Transaction dropdown
                                val availableTxs = allTxs.filter { tx ->
                                    val groupTypes = DocumentGroupTypesMap[linkGroupFilter] ?: emptyList()
                                    tx.documentType in groupTypes
                                }
                                
                                ExposedDropdownMenuBox(
                                    expanded = txDropdownExpanded,
                                    onExpandedChange = { txDropdownExpanded = !txDropdownExpanded },
                                    modifier = Modifier.weight(2f)
                                ) {
                                    val selectedTx = availableTxs.find { it.transactionId == selectedLinkTxId }
                                    val displayVal = selectedTx?.let { "${it.documentNumber} (${it.documentType})" } ?: "Pasirinkite dokumentą"
                                    OutlinedTextField(
                                        value = displayVal,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Dokumentas") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = txDropdownExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        singleLine = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = txDropdownExpanded,
                                        onDismissRequest = { txDropdownExpanded = false }
                                    ) {
                                        if (availableTxs.isEmpty()) {
                                            DropdownMenuItem(text = { Text("Nėra dokumentų") }, onClick = {})
                                        } else {
                                            availableTxs.forEach { tx ->
                                                DropdownMenuItem(
                                                    text = { Text("${tx.documentNumber} (${tx.documentType})") },
                                                    onClick = {
                                                        selectedLinkTxId = tx.transactionId
                                                        txDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Checkbox for copy details
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1.5f)
                                ) {
                                    Checkbox(
                                        checked = copyDetailsCheck,
                                        onCheckedChange = { copyDetailsCheck = it }
                                    )
                                    Text("Kopijuoti eilutes", style = MaterialTheme.typography.bodySmall)
                                }

                                Button(
                                    onClick = {
                                        val sourceTx = allTxs.find { it.transactionId == selectedLinkTxId }
                                        if (sourceTx != null) {
                                            var updatedTx = transaction.copy(
                                                joinedTransactionId = sourceTx.transactionId,
                                                tag = sourceTx.documentNumber,
                                                link = "vedlys://transaction/${sourceTx.transactionId}"
                                            )
                                            if (copyDetailsCheck && sourceTx.details.isNotEmpty()) {
                                                val currentGroup = getGroupForDocumentType(transaction.documentType)
                                                val newDetails = sourceTx.details.mapIndexed { index, detail ->
                                                    convertDetail(detail, transaction.transactionId, index + 1, currentGroup, sourceTx.documentNumber, sourceTx.transactionId)
                                                }
                                                updatedTx = updatedTx.copy(details = transaction.details + newDetails)
                                            }
                                            viewModel.saveTransaction(updatedTx)
                                        }
                                    },
                                    enabled = selectedLinkTxId.isNotBlank(),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Susieti")
                                }
                            }
                        }
                    }

                    // 3. Related Child Documents
                    if (childTxs.isNotEmpty()) {
                        HorizontalDivider()
                        Text("Susiję dukteriniai dokumentai:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        childTxs.forEach { child ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• ${child.documentNumber} (${child.documentType}) - Statusas: ${child.status}", style = MaterialTheme.typography.bodyMedium)
                                if (!isReadOnly) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                apiClient.transactionRepository.update(child.copy(joinedTransactionId = null, tag = null, link = null))
                                                refreshLinks()
                                            }
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Atrišti")
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // 4. Bidirectional Export Section
                    Text("Nukreipimas (Eksportas) į naują dokumentą:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Group selector
                        ExposedDropdownMenuBox(
                            expanded = exportGroupExpanded,
                            onExpandedChange = { exportGroupExpanded = !exportGroupExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = exportGroup,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tikslinė grupė") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exportGroupExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = exportGroupExpanded,
                                onDismissRequest = { exportGroupExpanded = false }
                            ) {
                                listOf("CRM", "Financial", "Operational", "Delivery").forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        onClick = {
                                            exportGroup = g
                                            exportDocType = DocumentGroupTypesMap[g]?.firstOrNull() ?: "OTHER"
                                            exportGroupExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Doc type selector
                        ExposedDropdownMenuBox(
                            expanded = exportDocTypeExpanded,
                            onExpandedChange = { exportDocTypeExpanded = !exportDocTypeExpanded },
                            modifier = Modifier.weight(2f)
                        ) {
                            OutlinedTextField(
                                value = exportDocType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Dokumento tipas") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exportDocTypeExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = exportDocTypeExpanded,
                                onDismissRequest = { exportDocTypeExpanded = false }
                            ) {
                                val types = DocumentGroupTypesMap[exportGroup] ?: emptyList()
                                types.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t) },
                                        onClick = {
                                            exportDocType = t
                                            exportDocTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    val newTxId = UUID.randomUUID().toString()
                                    val targetGroup = exportGroup
                                    val convertedDetails = transaction.details.mapIndexed { idx, d ->
                                        convertDetail(d, newTxId, idx + 1, targetGroup, transaction.documentNumber, transaction.transactionId)
                                    }
                                    val newTx = TransactionDTO(
                                        transactionId = newTxId,
                                        version = 1,
                                        transactionTime = System.currentTimeMillis(),
                                        status = "Nauja",
                                        documentNumber = "EXP-" + transaction.documentNumber + "-" + (100 + java.util.Random().nextInt(900)),
                                        documentDate = System.currentTimeMillis(),
                                        documentType = exportDocType,
                                        createdByUserId = transaction.createdByUserId,
                                        createdOnDeviceId = transaction.createdOnDeviceId,
                                        isBlocked = false,
                                        enabled = true,
                                        groupId = transaction.groupId,
                                        details = convertedDetails,
                                        joinedTransactionId = transaction.transactionId, // New child links to current parent
                                        tag = transaction.documentNumber,
                                        link = "vedlys://transaction/${transaction.transactionId}"
                                    )
                                    when (val res = apiClient.transactionRepository.create(newTx)) {
                                        is ApiResult.Success -> {
                                            exportSuccessMessage = "Sėkmingai sukurta transakcija ${newTx.documentNumber}!"
                                            refreshLinks()
                                        }
                                        is ApiResult.Error -> {
                                            exportSuccessMessage = "Klaida eksportuojant: ${res.message}"
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Eksportuoti")
                        }
                    }

                    exportSuccessMessage?.let { msg ->
                        Spacer(Modifier.height(4.dp))
                        Text(msg, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchablePartnerLookup(
    apiClient: ApiSvClient,
    selectedPartnerId: Int?,
    onSelected: (TypeDTO?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    colors: TextFieldColors? = null
) {
    var partnersList by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var isLoadingPartners by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showBrowseDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Load partners
    LaunchedEffect(Unit) {
        isLoadingPartners = true
        when (val result = apiClient.typeRepository.getAllByType("Partner")) {
            is ApiResult.Success -> {
                partnersList = result.data ?: emptyList()
                val initial = partnersList.find { it.id == selectedPartnerId }
                if (initial != null) {
                    searchQuery = "${initial.id} - ${initial.code ?: ""} - ${initial.name}"
                }
            }
            is ApiResult.Error -> {
                partnersList = emptyList()
            }
        }
        isLoadingPartners = false
    }

    // Update search query when external selection changes
    LaunchedEffect(selectedPartnerId, partnersList) {
        if (selectedPartnerId != null) {
            val initial = partnersList.find { it.id == selectedPartnerId }
            if (initial != null) {
                searchQuery = "${initial.id} - ${initial.code ?: ""} - ${initial.name}"
            }
        } else {
            searchQuery = ""
        }
    }

    // Let's filter on typing queries
    var typingQuery by remember(searchQuery) { mutableStateOf(searchQuery) }

    val actualFiltered = remember(typingQuery, partnersList) {
        val selectedFormatRegex = Regex("^\\d+ - .* - .*")
        if (typingQuery.length < 3 || selectedFormatRegex.matches(typingQuery)) {
            emptyList()
        } else {
            partnersList.filter { p ->
                val displayStr = "${p.id} ${p.code ?: ""} ${p.name}"
                displayStr.contains(typingQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(actualFiltered) {
        dropdownExpanded = actualFiltered.isNotEmpty()
    }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded && enabled,
            onExpandedChange = {}
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typingQuery,
                    onValueChange = {
                        typingQuery = it
                        if (it.isBlank()) {
                            onSelected(null)
                        }
                    },
                    label = { Text("Partneris (min. 3 simb.)") },
                    modifier = Modifier.weight(1f).menuAnchor(),
                    singleLine = true,
                    enabled = enabled,
                    isError = isError,
                    supportingText = supportingText?.let { { Text(it) } },
                    colors = colors ?: OutlinedTextFieldDefaults.colors(),
                    trailingIcon = if (colors != null && !isError) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Valid",
                                tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                            )
                        }
                    } else {
                        if (typingQuery.isNotEmpty() && enabled) {
                            {
                                IconButton(onClick = {
                                    typingQuery = ""
                                    onSelected(null)
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Išvalyti")
                                }
                            }
                        } else null
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { showBrowseDialog = true },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ieškoti partnerio"
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = dropdownExpanded && enabled,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                actualFiltered.take(10).forEach { partner ->
                    val displayStr = "${partner.id} - ${partner.code ?: ""} - ${partner.name}"
                    DropdownMenuItem(
                        text = { Text(displayStr) },
                        onClick = {
                            typingQuery = displayStr
                            onSelected(partner)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
    }

    if (showBrowseDialog) {
        var browseSearchQuery by remember { mutableStateOf("") }
        val browseFilteredPartners = remember(browseSearchQuery, partnersList) {
            partnersList.filter { partner ->
                val displayStr = "${partner.id} ${partner.code ?: ""} ${partner.name}"
                displayStr.contains(browseSearchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showBrowseDialog = false },
            title = { Text("Pasirinkti partnerį") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.width(400.dp).height(300.dp)) {
                    OutlinedTextField(
                        value = browseSearchQuery,
                        onValueChange = { browseSearchQuery = it },
                        label = { Text("Ieškoti...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoadingPartners) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (browseFilteredPartners.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Rezultatų nerasta")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(browseFilteredPartners) { partner ->
                                val displayStr = "${partner.id} - ${partner.code ?: ""} - ${partner.name}"
                                ListItem(
                                    headlineContent = { Text(displayStr) },
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        typingQuery = displayStr
                                        onSelected(partner)
                                        showBrowseDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBrowseDialog = false }) {
                    Text("Uždaryti")
                }
            }
        )
    }
}

