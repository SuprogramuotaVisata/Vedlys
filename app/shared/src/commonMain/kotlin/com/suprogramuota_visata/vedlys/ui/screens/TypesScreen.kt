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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.Image
import androidx.compose.ui.window.Dialog
import com.suprogramuota_visata.vedlys.ui.components.ImageAttributeField
import com.suprogramuota_visata.vedlys.ui.components.ImagePreviewDialog
import com.suprogramuota_visata.vedlys.utils.ImageHelper
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
    "BankSv", "TransactionSv", 
    "FinancialTransactionDetailSv", "OperationalTransactionDetailSv", "DeliveryTransactionDetailSv", "CrmTransactionDetailSv",
    "GroupSv", "UnitSv",
    "SettingsSv", "OwnerSv", "UserSv"
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
        if (result == sysType || result == "Šakninė grupė - $sysType" || result == sysType.toFriendlyTypeName(AppLanguage.LT) || result == sysType.toFriendlyTypeName(AppLanguage.EN)) {
            result = sysType.toFriendlyTypeName(language)
        }
    }
    if (language == AppLanguage.EN) {
        result = result.replace("Šakninė grupė", "Root Group")
    }
    return result
}

data class GroupHierarchyNode(
    val group: TypeDTO,
    val depth: Int
)

fun isDescendantOf(candidateId: Int?, ancestorId: Int, all: List<TypeDTO>): Boolean {
    var curr = candidateId
    val seen = mutableSetOf<Int>()
    while (curr != null && curr != 0 && seen.add(curr)) {
        val parent = all.find { it.id == curr }
        val pId = parent?.parentGroupId
        if (pId == ancestorId) return true
        curr = pId
    }
    return false
}

fun getAllDescendantIds(pId: Int, all: List<TypeDTO>): Set<Int> {
    val direct = all.filter { it.parentGroupId == pId }.mapNotNull { it.id }
    return direct.toSet() + direct.flatMap { getAllDescendantIds(it, all) }
}

data class GroupTreeDisplayItem(
    val item: TypeDTO,
    val depth: Int,
    val path: String = "",
    val parentName: String? = null,
    val hasChildren: Boolean = false,
    val childCount: Int = 0,
    val parentId: Int? = null
)

fun orderGroupsHierarchically(rawList: List<TypeDTO>, language: AppLanguage = AppLanguage.LT): List<GroupTreeDisplayItem> {
    if (rawList.isEmpty()) return emptyList()
    val result = mutableListOf<GroupTreeDisplayItem>()
    val idMap = rawList.associateBy { it.id }

    val roots = rawList.filter { item ->
        item.parentGroupId == null || item.parentGroupId == 0 ||
        !idMap.containsKey(item.parentGroupId)
    }

    val visited = mutableSetOf<Int>()

    fun addNodeAndChildren(node: TypeDTO, currentDepth: Int, currentPath: String, parentNodeName: String?) {
        val nodeId = node.id
        if (nodeId != null && visited.contains(nodeId)) return
        if (nodeId != null) visited.add(nodeId)

        val nodeName = node.name.formatGroupName(language).ifBlank { "Grupė #${node.id}" }
        val fullPath = if (currentPath.isBlank()) nodeName else "$currentPath > $nodeName"

        val directChildren = if (nodeId != null) rawList.filter { it.parentGroupId == nodeId && it.id != nodeId } else emptyList()
        val hasChildren = directChildren.isNotEmpty()
        val childCount = if (nodeId != null) getAllDescendantIds(nodeId, rawList).size else 0

        result.add(GroupTreeDisplayItem(
            item = node,
            depth = currentDepth,
            path = fullPath,
            parentName = parentNodeName,
            hasChildren = hasChildren,
            childCount = childCount,
            parentId = node.parentGroupId
        ))

        directChildren.forEach { child ->
            addNodeAndChildren(child, currentDepth + 1, fullPath, nodeName)
        }
    }

    roots.forEach { root ->
        addNodeAndChildren(root, 0, "", null)
    }

    rawList.forEach { item ->
        val itemId = item.id
        if (itemId == null || !visited.contains(itemId)) {
            val pName = item.parentGroupId?.let { idMap[it]?.name?.formatGroupName(language) }
            val directChildren = if (itemId != null) rawList.filter { it.parentGroupId == itemId && it.id != itemId } else emptyList()
            val hasChildren = directChildren.isNotEmpty()
            val childCount = if (itemId != null) getAllDescendantIds(itemId, rawList).size else 0
            result.add(GroupTreeDisplayItem(
                item = item,
                depth = item.level ?: 0,
                path = "",
                parentName = pName,
                hasChildren = hasChildren,
                childCount = childCount,
                parentId = item.parentGroupId
            ))
        }
    }

    return result
}

fun filterCollapsedGroups(
    hierarchicalItems: List<GroupTreeDisplayItem>,
    collapsedIds: Set<Int>,
    allGroups: List<TypeDTO>
): List<GroupTreeDisplayItem> {
    if (collapsedIds.isEmpty()) return hierarchicalItems
    val hiddenIds = mutableSetOf<Int>()
    for (collapsedId in collapsedIds) {
        hiddenIds.addAll(getAllDescendantIds(collapsedId, allGroups))
    }
    return hierarchicalItems.filter { it.item.id == null || !hiddenIds.contains(it.item.id) }
}

data class GroupDropdownOption(
    val group: TypeDTO?, // null means "Root / Šaknis"
    val displayName: String,
    val depth: Int,
    val fullPath: String
)

fun buildGroupDropdownOptions(
    allGroups: List<TypeDTO>,
    excludeId: Int?,
    language: AppLanguage,
    allowRootOption: Boolean = true,
    filterTargetType: String? = null
): List<GroupDropdownOption> {
    val result = mutableListOf<GroupDropdownOption>()
    if (allowRootOption) {
        result.add(
            GroupDropdownOption(
                group = null,
                displayName = if (language == AppLanguage.EN) "— Root Category (Domain) —" else "— Šakninė kategorija (Root) —",
                depth = 0,
                fullPath = if (language == AppLanguage.EN) "Root" else "Šaknis"
            )
        )
    }

    val idMap = allGroups.associateBy { it.id }
    val roots = allGroups.filter {
        (it.level == 0 || it.isChild == false || it.parentGroupId == null || !idMap.containsKey(it.parentGroupId)) &&
        (filterTargetType.isNullOrBlank() || it.targetType.equals(filterTargetType, ignoreCase = true) || it.name.equals(filterTargetType.removeSuffix("Sv"), ignoreCase = true))
    }

    val visited = mutableSetOf<Int>()

    fun traverse(node: TypeDTO, depth: Int, parentPath: String) {
        val nId = node.id ?: return
        if (visited.contains(nId)) return
        if (excludeId != null && (nId == excludeId || isDescendantOf(nId, excludeId, allGroups))) return
        visited.add(nId)

        val formattedName = node.name.formatGroupName(language)
        val currentPath = if (parentPath.isBlank()) formattedName else "$parentPath > $formattedName"
        val indent = "    ".repeat(depth)
        val prefix = if (depth == 0) "📁 " else "└── 📁 "

        result.add(
            GroupDropdownOption(
                group = node,
                displayName = "$indent$prefix$formattedName",
                depth = depth,
                fullPath = currentPath
            )
        )

        val children = allGroups.filter { it.parentGroupId == nId && it.id != nId }
        children.forEach { child ->
            traverse(child, depth + 1, currentPath)
        }
    }

    roots.forEach { root ->
        traverse(root, 0, "")
    }

    allGroups.filter {
        filterTargetType.isNullOrBlank() || it.targetType.equals(filterTargetType, ignoreCase = true) || it.name.equals(filterTargetType.removeSuffix("Sv"), ignoreCase = true)
    }.forEach { g ->
        val gId = g.id
        if (gId != null && !visited.contains(gId)) {
            traverse(g, g.level ?: 1, "")
        }
    }

    return result
}

fun buildFlatGroupHierarchy(
    root: TypeDTO,
    allChildren: List<TypeDTO>
): List<GroupHierarchyNode> {
    val result = mutableListOf<GroupHierarchyNode>()
    val rootId = root.id

    val level1 = allChildren.filter {
        it.parentGroupId == null || it.parentGroupId == rootId || it.parentGroupId == 0 ||
        allChildren.none { p -> p.id == it.parentGroupId }
    }

    fun addSubtree(parentId: Int, currentDepth: Int) {
        val subs = allChildren.filter { it.parentGroupId == parentId }
        subs.forEach { sub ->
            if (result.none { it.group.id == sub.id }) {
                result.add(GroupHierarchyNode(sub, currentDepth))
                val subId = sub.id
                if (subId != null) {
                    addSubtree(subId, currentDepth + 1)
                }
            }
        }
    }

    level1.forEach { child ->
        if (result.none { it.group.id == child.id }) {
            result.add(GroupHierarchyNode(child, 1))
            val childId = child.id
            if (childId != null) {
                addSubtree(childId, 2)
            }
        }
    }

    allChildren.forEach { child ->
        if (result.none { it.group.id == child.id }) {
            result.add(GroupHierarchyNode(child, 1))
        }
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
    var parentForNewChild by remember { mutableStateOf<TypeDTO?>(null) }
    var deleteCandidate by remember { mutableStateOf<TypeDTO?>(null) }
    var previewImageUuid by remember { mutableStateOf<String?>(null) }

    var groups by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    fun reloadGroups() {
        scope.launch {
            when (val res = apiClient.typeRepository.getAllByType("GroupSv")) {
                is com.suprogramuota_visata.api.domain.util.ApiResult.Success -> {
                    groups = res.data ?: emptyList()
                }
                else -> {}
            }
        }
    }
    LaunchedEffect(Unit) {
        reloadGroups()
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
                parentForNewChild = null
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
                    
                    // Grupės filtras su Checked List
                    val isGroupCatalog = viewModel.typeName == "GroupSv"

                    val allGroupRoots = remember(groups) {
                        groups.filter {
                            (it.level == 0 || it.isChild == false || it.parentGroupId == null)
                        }
                    }

                    val allCatalogGroupIds = remember(groups) {
                        groups.mapNotNull { it.id }.toSet()
                    }

                    val currentRootGroup = remember(groups, viewModel.typeName) {
                        groups.find {
                            (it.targetType.equals(viewModel.typeName, ignoreCase = true) ||
                             it.targetType.equals(viewModel.typeName.removeSuffix("Sv"), ignoreCase = true)) &&
                            (it.level == 0 || it.isChild == false || it.parentGroupId == null)
                        } ?: groups.find {
                            (it.name.equals(viewModel.typeName.removeSuffix("Sv"), ignoreCase = true) ||
                             it.name.equals(viewModel.typeName, ignoreCase = true)) &&
                            (it.level == 0 || it.isChild == false || it.parentGroupId == null)
                        } ?: TypeDTO(
                            id = 0,
                            name = viewModel.typeName.removeSuffix("Sv"),
                            type = "GroupSv",
                            enabled = true,
                            isChild = false,
                            level = 0,
                            targetType = viewModel.typeName,
                            attributes = emptyList()
                        )
                    }

                    val currentChildGroups = remember(groups, viewModel.typeName, currentRootGroup) {
                        groups.filter {
                            (it.targetType.equals(viewModel.typeName, ignoreCase = true) ||
                             it.targetType.equals(viewModel.typeName.removeSuffix("Sv"), ignoreCase = true)) &&
                            it.id != currentRootGroup.id &&
                            (it.isChild == true || (it.level?.let { l -> l > 0 } == true) || it.parentGroupId != null)
                        }
                    }

                    val flatHierarchy = remember(currentRootGroup, currentChildGroups) {
                        buildFlatGroupHierarchy(currentRootGroup, currentChildGroups)
                    }

                    val allGroupIds = remember(currentRootGroup, currentChildGroups) {
                        setOfNotNull(currentRootGroup.id ?: 0) + currentChildGroups.mapNotNull { it.id }
                    }

                    var isAllSelectedMode by remember(viewModel.typeName) { mutableStateOf(true) }
                    var checkedGroupIds by remember(viewModel.typeName) {
                        mutableStateOf(if (isGroupCatalog) allCatalogGroupIds else allGroupIds)
                    }

                    LaunchedEffect(allGroupIds, allCatalogGroupIds, isGroupCatalog) {
                        val relevantAll = if (isGroupCatalog) allCatalogGroupIds else allGroupIds
                        if (isAllSelectedMode || checkedGroupIds.isEmpty()) {
                            checkedGroupIds = relevantAll
                            isAllSelectedMode = true
                        } else {
                            // If user filtered by specific groups, but a child was added to an already-checked parent, include the new child
                            val newChildren = groups.filter { it.parentGroupId != null && checkedGroupIds.contains(it.parentGroupId) }.mapNotNull { it.id }
                            if (newChildren.isNotEmpty() && !checkedGroupIds.containsAll(newChildren)) {
                                checkedGroupIds = checkedGroupIds + newChildren
                            }
                        }
                    }

                    val rootId = currentRootGroup.id ?: 0
                    val isAllSelectedNonGroup = isAllSelectedMode || (allGroupIds.isNotEmpty() && checkedGroupIds.containsAll(allGroupIds))
                    val isAllSelectedGroup = isAllSelectedMode || (allCatalogGroupIds.isNotEmpty() && checkedGroupIds.containsAll(allCatalogGroupIds))

                    LaunchedEffect(checkedGroupIds, currentRootGroup, currentChildGroups, isGroupCatalog, allCatalogGroupIds, isAllSelectedMode) {
                        if (isGroupCatalog) {
                            if (isAllSelectedGroup) {
                                viewModel.updateGroupMultiFilter(null, includeRoot = true, rootId = 0)
                            } else {
                                viewModel.updateGroupMultiFilter(checkedGroupIds, includeRoot = true, rootId = 0)
                            }
                        } else {
                            if (isAllSelectedNonGroup) {
                                viewModel.updateGroupMultiFilter(null, includeRoot = true, rootId = rootId)
                            } else {
                                val isRootChecked = checkedGroupIds.contains(rootId)
                                viewModel.updateGroupMultiFilter(
                                    selectedIds = checkedGroupIds,
                                    includeRoot = isRootChecked,
                                    rootId = rootId
                                )
                            }
                        }
                    }

                    fun toggleRoot() {
                        val isRootChecked = checkedGroupIds.contains(rootId)
                        if (isRootChecked) {
                            checkedGroupIds = emptySet()
                            isAllSelectedMode = false
                        } else {
                            checkedGroupIds = allGroupIds
                            isAllSelectedMode = true
                        }
                    }

                    fun toggleChild(child: TypeDTO) {
                        val cId = child.id ?: return
                        fun getDescendantIds(pId: Int): Set<Int> {
                            val direct = currentChildGroups.filter { it.parentGroupId == pId }.mapNotNull { it.id }
                            return direct.toSet() + direct.flatMap { getDescendantIds(it) }
                        }
                        val targetIds = setOf(cId) + getDescendantIds(cId)
                        val isCurrentlyChecked = checkedGroupIds.contains(cId)

                        val newSet = checkedGroupIds.toMutableSet()
                        if (isCurrentlyChecked) {
                            newSet.removeAll(targetIds)
                            newSet.remove(currentRootGroup.id ?: 0)
                        } else {
                            newSet.addAll(targetIds)
                            if (currentChildGroups.all { newSet.contains(it.id) }) {
                                newSet.add(currentRootGroup.id ?: 0)
                            }
                        }
                        checkedGroupIds = newSet
                        isAllSelectedMode = allGroupIds.isNotEmpty() && newSet.containsAll(allGroupIds)
                    }

                    val groupDisplayText = if (isGroupCatalog) {
                        when {
                            isAllSelectedGroup -> if (language == AppLanguage.EN) "All Groups" else "Visos grupės"
                            checkedGroupIds.isEmpty() -> if (language == AppLanguage.EN) "None selected" else "Nepasirinkta"
                            else -> {
                                val singleRoot = allGroupRoots.find { r ->
                                    val fam = setOfNotNull(r.id) + getAllDescendantIds(r.id ?: 0, groups)
                                    checkedGroupIds == fam
                                }
                                if (singleRoot != null) {
                                    singleRoot.name.formatGroupName(language)
                                } else {
                                    if (language == AppLanguage.EN) "${checkedGroupIds.size} selected" else "Pasirinkta (${checkedGroupIds.size})"
                                }
                            }
                        }
                    } else {
                        when {
                            isAllSelectedNonGroup -> currentRootGroup.name.formatGroupName(language)
                            checkedGroupIds.isEmpty() -> if (language == AppLanguage.EN) "None selected" else "Nepasirinkta"
                            checkedGroupIds.size == 1 -> {
                                val singleId = checkedGroupIds.first()
                                val g = if (singleId == rootId) currentRootGroup else currentChildGroups.find { it.id == singleId }
                                g?.name?.formatGroupName(language) ?: if (language == AppLanguage.EN) "1 selected" else "1 pasirinkta"
                            }
                            else -> {
                                if (language == AppLanguage.EN) "${checkedGroupIds.size} selected" else "Pasirinkta (${checkedGroupIds.size})"
                            }
                        }
                    }

                    var expandedGroup by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedGroup,
                        onExpandedChange = { expandedGroup = !expandedGroup },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = groupDisplayText,
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
                            if (isGroupCatalog) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = isAllSelectedGroup,
                                                onCheckedChange = {
                                                    val newAll = !isAllSelectedGroup
                                                    isAllSelectedMode = newAll
                                                    checkedGroupIds = if (newAll) allCatalogGroupIds else emptySet()
                                                }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = if (language == AppLanguage.EN) "All Groups" else "Visos grupės",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    },
                                    onClick = {
                                        val newAll = !isAllSelectedGroup
                                        isAllSelectedMode = newAll
                                        checkedGroupIds = if (newAll) allCatalogGroupIds else emptySet()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                )

                                Divider(modifier = Modifier.padding(vertical = 4.dp))

                                allGroupRoots.forEach { r ->
                                    val rId = r.id ?: 0
                                    val descendants = getAllDescendantIds(rId, groups)
                                    val rootFamily = setOf(rId) + descendants
                                    val isRootChecked = checkedGroupIds.contains(rId)

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Checkbox(
                                                    checked = isRootChecked,
                                                    onCheckedChange = {
                                                        val newSet = checkedGroupIds.toMutableSet()
                                                        if (isRootChecked) {
                                                            newSet.removeAll(rootFamily)
                                                        } else {
                                                            newSet.addAll(rootFamily)
                                                        }
                                                        checkedGroupIds = newSet
                                                        isAllSelectedMode = allCatalogGroupIds.isNotEmpty() && newSet.containsAll(allCatalogGroupIds)
                                                    }
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = r.name.formatGroupName(language),
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        },
                                        onClick = {
                                            val newSet = checkedGroupIds.toMutableSet()
                                            if (isRootChecked) {
                                                newSet.removeAll(rootFamily)
                                            } else {
                                                newSet.addAll(rootFamily)
                                            }
                                            checkedGroupIds = newSet
                                            isAllSelectedMode = allCatalogGroupIds.isNotEmpty() && newSet.containsAll(allCatalogGroupIds)
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    )

                                    val childrenOfRoot = groups.filter {
                                        (it.targetType.equals(r.targetType, ignoreCase = true) ||
                                         it.targetType.equals(r.name, ignoreCase = true)) &&
                                        it.id != r.id &&
                                        descendants.contains(it.id)
                                    }
                                    val rootHierarchy = buildFlatGroupHierarchy(r, childrenOfRoot)

                                    rootHierarchy.forEach { node ->
                                        val childId = node.group.id ?: 0
                                        val isChildChecked = checkedGroupIds.contains(childId)
                                        val childDescendants = getAllDescendantIds(childId, groups)
                                        val childFamily = setOf(childId) + childDescendants

                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = (node.depth * 16).dp)
                                                ) {
                                                    Checkbox(
                                                        checked = isChildChecked,
                                                        onCheckedChange = {
                                                            val newSet = checkedGroupIds.toMutableSet()
                                                            if (isChildChecked) {
                                                                newSet.removeAll(childFamily)
                                                            } else {
                                                                newSet.addAll(childFamily)
                                                            }
                                                            checkedGroupIds = newSet
                                                            isAllSelectedMode = allCatalogGroupIds.isNotEmpty() && newSet.containsAll(allCatalogGroupIds)
                                                        }
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = node.group.name.formatGroupName(language),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            },
                                            onClick = {
                                                val newSet = checkedGroupIds.toMutableSet()
                                                if (isChildChecked) {
                                                    newSet.removeAll(childFamily)
                                                } else {
                                                    newSet.addAll(childFamily)
                                                }
                                                checkedGroupIds = newSet
                                                isAllSelectedMode = allCatalogGroupIds.isNotEmpty() && newSet.containsAll(allCatalogGroupIds)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                // Šakninė grupė (Root branch)
                                val isRootChecked = checkedGroupIds.contains(rootId)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = isRootChecked,
                                                onCheckedChange = { toggleRoot() }
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = currentRootGroup.name.formatGroupName(language),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    },
                                    onClick = { toggleRoot() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                )

                                if (flatHierarchy.isNotEmpty()) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                }

                                flatHierarchy.forEach { node ->
                                    val isChecked = checkedGroupIds.contains(node.group.id)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = (node.depth * 16).dp)
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { toggleChild(node.group) }
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = node.group.name.formatGroupName(language),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        },
                                        onClick = { toggleChild(node.group) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.loadInitial() 
                            reloadGroups()
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

                    val isGroupCatalog = viewModel.typeName == "GroupSv"
                    val allGroupTreeItems = remember(viewModel.items, isGroupCatalog, language) {
                        if (isGroupCatalog) {
                            orderGroupsHierarchically(viewModel.items, language)
                        } else {
                            viewModel.items.map { GroupTreeDisplayItem(it, 0) }
                        }
                    }

                    var collapsedGroupIds by remember(viewModel.typeName) { mutableStateOf(setOf<Int>()) }

                    val displayItems = remember(allGroupTreeItems, collapsedGroupIds, isGroupCatalog, viewModel.items) {
                        if (isGroupCatalog) {
                            filterCollapsedGroups(allGroupTreeItems, collapsedGroupIds, viewModel.items)
                        } else {
                            allGroupTreeItems
                        }
                    }

                    if (isGroupCatalog && allGroupTreeItems.any { it.hasChildren }) {
                        val allParentIds = remember(allGroupTreeItems) {
                            allGroupTreeItems.filter { it.hasChildren }.mapNotNull { it.item.id }.toSet()
                        }
                        val isAllCollapsed = allParentIds.isNotEmpty() && collapsedGroupIds.containsAll(allParentIds)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN) "Hierarchy View:" else "Hierarchijos rodymas:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = {
                                    collapsedGroupIds = if (isAllCollapsed) emptySet() else allParentIds
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAllCollapsed) Icons.Default.UnfoldMore else Icons.Default.UnfoldLess,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isAllCollapsed) {
                                        if (language == AppLanguage.EN) "Expand all" else "Išskleisti visus"
                                    } else {
                                        if (language == AppLanguage.EN) "Collapse all" else "Suskleisti visus"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayItems, key = { it.item.id ?: it.item.hashCode() }) { displayNode ->
                            val item = displayNode.item
                            val depth = displayNode.depth
                            val itemId = item.id
                            val isCollapsed = itemId != null && collapsedGroupIds.contains(itemId)
                            val rawJson = item.attributes.find { 
                                it.name.equals("Nuotraukos Meta", ignoreCase = true) || 
                                it.attributeType == "JSON_STRING" || 
                                it.value?.contains("\"img_format\"") == true ||
                                (it.value?.contains("\"id\"") == true && it.value?.contains("-") == true)
                            }?.value
                            val imgUuid: String? = ImageHelper.extractImageUuid(rawJson)

                            TypeRow(
                                item = item,
                                depth = depth,
                                path = displayNode.path,
                                language = language,
                                isSelected = item.id != null && viewModel.selectedItemIds.contains(item.id),
                                onToggleSelect = { item.id?.let { id -> viewModel.toggleSelectItem(id) } },
                                hasChildren = displayNode.hasChildren,
                                childCount = displayNode.childCount,
                                isCollapsed = isCollapsed,
                                onToggleCollapse = if (itemId != null && displayNode.hasChildren) {
                                    {
                                        collapsedGroupIds = if (isCollapsed) {
                                            collapsedGroupIds - itemId
                                        } else {
                                            collapsedGroupIds + itemId
                                        }
                                    }
                                } else null,
                                onEdit = {
                                    editingItem = item
                                    parentForNewChild = null
                                    showEditor = true
                                },
                                onAddChild = if (isGroupCatalog) {
                                    {
                                        editingItem = null
                                        parentForNewChild = item
                                        showEditor = true
                                    }
                                } else null,
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
                    parentForNewChild = null
                },
                parentForNewChild = parentForNewChild,
                onReloadGroups = {
                    reloadGroups()
                    viewModel.loadInitial()
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
                            parentForNewChild = null
                            val savedGroup = res.data
                            if (checkedChildren.isNotEmpty()) {
                                checkedChildren.forEach { child ->
                                    apiClient.typeRepository.update(child.copy(groupId = savedGroup.id))
                                }
                            }
                            viewModel.loadInitial()
                            reloadGroups()
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
                apiClient = apiClient,
                onDismiss = { previewImageUuid = null }
            )
        }
    }
}

@Composable
private fun TypeRow(
    item: TypeDTO,
    depth: Int = 0,
    path: String = "",
    language: AppLanguage,
    isSelected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null,
    hasChildren: Boolean = false,
    childCount: Int = 0,
    isCollapsed: Boolean = false,
    onToggleCollapse: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onAddChild: (() -> Unit)? = null,
    onDelete: () -> Unit,
    onViewImage: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 20).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onToggleSelect != null && item.id != null) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() }
                )
                Spacer(Modifier.width(6.dp))
            }

            if (item.type == "GroupSv") {
                if (hasChildren) {
                    IconButton(
                        onClick = { onToggleCollapse?.invoke() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isCollapsed) "Išskleisti" else "Suskleisti",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.width(28.dp))
                }

                IconButton(
                    onClick = { if (hasChildren) onToggleCollapse?.invoke() },
                    enabled = hasChildren,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (hasChildren && !isCollapsed) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (depth == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.name.formatGroupName(language), 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    if (item.type == "GroupSv") {
                        val isRoot = depth == 0 || item.level == 0 || item.isChild == false || (item.parentGroupId == null && item.id != null)
                        if (isRoot) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Šaknis (0)") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.height(26.dp)
                            )
                        } else {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Lygis ${item.level ?: depth}") },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                        if (hasChildren) {
                            SuggestionChip(
                                onClick = { onToggleCollapse?.invoke() },
                                label = { Text("Pošakiai: $childCount") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                        if (!item.targetType.isNullOrBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Tikslinis: ${item.targetType!!.toFriendlyTypeName(language)}") },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    } else {
                        AssistChip(
                            onClick = {},
                            label = { Text(item.type.toFriendlyTypeName(language)) },
                            modifier = Modifier.height(26.dp)
                        )
                    }

                    if (!item.enabled) {
                        AssistChip(
                            onClick = {},
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer, 
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            label = { Text(getTypesString("inactive", language)) },
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val metaDetails = buildString {
                        if (item.type == "GroupSv" && path.isNotBlank() && depth > 0) {
                            append("Kelias: $path  •  ")
                        }
                        append("ID: ${item.id ?: "—"}")
                        if (item.type == "GroupSv") {
                            item.parentGroupId?.let { append("  •  Tėvas ID: $it") }
                        } else {
                            item.groupId?.let { append("  •  ${getTypesString("group", language)}: $it") }
                        }
                        item.code?.let { append("  •  Kodas: $it") }
                        item.barcode?.let { append("  •  Barkodas: $it") }
                        if (item.attributes.isNotEmpty()) {
                            val attrsStr = item.attributes.joinToString(", ") { "${it.name}: ${it.value ?: "—"}" }
                            append("  •  ${getTypesString("attributes", language)}: $attrsStr")
                        }
                    }
                    Text(
                        text = metaDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            val isProtectedSystemItem = (item.id == 0) || (item.level == 0) || (item.isChild == false) || (item.parentGroupId == null && item.type == "GroupSv")
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onAddChild != null) {
                    OutlinedButton(
                        onClick = onAddChild,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.EN) "Add subgroup" else "Pridėti pogrupį",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (onViewImage != null) {
                    IconButton(onClick = onViewImage, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Peržiūrėti nuotrauką",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = getTypesString("edit", language), modifier = Modifier.size(20.dp))
                }
                if (!isProtectedSystemItem) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = getTypesString("delete", language),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
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
    onSave: (TypeDTO, List<TypeDTO>) -> Unit,
    parentForNewChild: TypeDTO? = null,
    onReloadGroups: (() -> Unit)? = null
) {
    var name by remember(initial, language) {
        mutableStateOf(
            if (initial?.type == "GroupSv") (initial?.name?.formatGroupName(language) ?: "")
            else (initial?.name ?: "")
        )
    }
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
    var typeStr by remember { 
        mutableStateOf(if (parentForNewChild != null) "GroupSv" else (initial?.type ?: defaultTypeName)) 
    }

    val rootGroupForEditor = remember(allGroups, typeStr) {
        allGroups.find {
            (it.targetType.equals(typeStr, ignoreCase = true) ||
             it.targetType.equals(typeStr.removeSuffix("Sv"), ignoreCase = true)) &&
            (it.level == 0 || it.isChild == false || it.parentGroupId == null)
        } ?: allGroups.find {
            (it.name.equals(typeStr.removeSuffix("Sv"), ignoreCase = true) ||
             it.name.equals(typeStr, ignoreCase = true)) &&
            (it.level == 0 || it.isChild == false || it.parentGroupId == null)
        }
    }

    val childGroupsForEditor = remember(allGroups, typeStr, rootGroupForEditor) {
        allGroups.filter {
            (it.targetType.equals(typeStr, ignoreCase = true) ||
             it.targetType.equals(typeStr.removeSuffix("Sv"), ignoreCase = true)) &&
            it.id != rootGroupForEditor?.id &&
            (it.isChild == true || (it.level?.let { l -> l > 0 } == true) || it.parentGroupId != null)
        }
    }

    val editorGroupNodes = remember(rootGroupForEditor, childGroupsForEditor) {
        if (rootGroupForEditor != null) {
            buildFlatGroupHierarchy(rootGroupForEditor, childGroupsForEditor)
        } else {
            childGroupsForEditor.map { GroupHierarchyNode(it, 1) }
        }
    }

    val defaultTargetTypeForGroup = remember(parentForNewChild, initial, defaultTypeName) {
        if (parentForNewChild != null) parentForNewChild.targetType ?: ""
        else if (initial != null) initial.targetType ?: ""
        else if (defaultTypeName != "GroupSv") defaultTypeName
        else "ProductSv"
    }

    val defaultParentForGroup = remember(parentForNewChild, initial, defaultTargetTypeForGroup, allGroups) {
        if (parentForNewChild != null) parentForNewChild
        else if (initial != null) {
            val pId = initial.parentGroupId
            if (pId != null) allGroups.find { it.id == pId } else null
        } else {
            allGroups.find {
                (it.targetType.equals(defaultTargetTypeForGroup, ignoreCase = true) ||
                 it.name.equals(defaultTargetTypeForGroup.removeSuffix("Sv"), ignoreCase = true)) &&
                (it.level == 0 || it.isChild == false || it.parentGroupId == null)
            } ?: allGroups.firstOrNull { it.level == 0 || it.parentGroupId == null }
        }
    }

    val defaultEditorGroupId = if (typeStr == "GroupSv") (defaultParentForGroup?.id?.toString() ?: "") else (rootGroupForEditor?.id?.toString() ?: "0")

    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }
    var isChild by remember { 
        mutableStateOf(
            if (parentForNewChild != null) true 
            else if (initial != null) (initial.parentGroupId != null || initial.isChild == true || (initial.level ?: 0) > 0)
            else (defaultParentForGroup != null)
        ) 
    }
    var targetType by remember { 
        mutableStateOf(
            if (parentForNewChild != null) (parentForNewChild.targetType ?: defaultTargetTypeForGroup)
            else if (initial != null) (initial.targetType ?: defaultTargetTypeForGroup)
            else (defaultParentForGroup?.targetType ?: defaultTargetTypeForGroup)
        ) 
    }
    var groupId by remember {
        mutableStateOf(
            if (parentForNewChild != null) (parentForNewChild.id?.toString() ?: "")
            else if (initial != null) (initial.parentGroupId?.toString() ?: initial.groupId?.toString() ?: "")
            else defaultEditorGroupId
        )
    }
    
    val computedLevel = remember(groupId, allGroups, typeStr, isChild) {
        if (typeStr == "GroupSv") {
            if (!isChild || groupId.isBlank() || groupId == "0") 0
            else {
                val pId = groupId.toIntOrNull()
                val parent = allGroups.find { it.id == pId }
                (parent?.level ?: 0) + 1
            }
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

    LaunchedEffect(selectedTemplateId, templates) {
        localTypeQueries.clear()
        if (selectedTemplateId != null) {
            val tpl = templates.find { it.id == selectedTemplateId }
            if (tpl != null) {
                if (initial == null) {
                    attributes.clear()
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
                } else {
                    tpl.attributes.forEach { ext ->
                        if (attributes.none { it.name == ext.name }) {
                            attributes.add(
                                AttributeDTO(
                                    name = ext.name,
                                    attributeType = ext.attributeType,
                                    validate = ext.validateRule,
                                    value = ext.defaultValue ?: "",
                                    validations = ext.validations,
                                    tag = ext.tag
                                )
                            )
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
        title = { 
            Text(
                when {
                    parentForNewChild != null -> if (language == AppLanguage.EN) "New Subfolder in: ${parentForNewChild.name.formatGroupName(language)}" else "Naujas pogrupis aplanke: ${parentForNewChild.name.formatGroupName(language)}"
                    typeStr == "GroupSv" && initial == null -> if (language == AppLanguage.EN) "New Group (Folder)" else "Nauja grupė (Katalogas)"
                    typeStr == "GroupSv" && initial != null -> if (language == AppLanguage.EN) "Edit Group: ${initial.name.formatGroupName(language)}" else "Redaguoti grupę: ${initial.name.formatGroupName(language)}"
                    initial == null -> getTypesString("create_new", language)
                    else -> getTypesString("edit_type", language)
                }
            ) 
        },
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

                    OutlinedTextField(
                        value = typeStr.toFriendlyTypeName(language),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(getTypesString("type_group_req", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )

                    // Šablono pasirinkimas
                    var expandedTemplate by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedTemplate,
                        onExpandedChange = { expandedTemplate = !expandedTemplate },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        val selectedTplName = templates.find { it.id == selectedTemplateId }?.name ?: getTypesString("not_selected", language)
                        OutlinedTextField(
                            value = selectedTplName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(getTypesString("template", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTemplate) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        if (templates.isNotEmpty()) {
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

                if (typeStr == "GroupSv") {
                    val isExistingRootGroup = remember(initial) {
                        initial != null && ((initial.level ?: 0) == 0 && (initial.parentGroupId == null || initial.parentGroupId == 0))
                    }
                    val isExistingChildGroup = remember(initial, parentForNewChild, isChild) {
                        parentForNewChild != null || (initial != null && ((initial.level ?: 0) > 0 || (initial.parentGroupId != null && initial.parentGroupId != 0))) || isChild
                    }

                    val groupDropdownOptions = remember(allGroups, initial?.id, language, targetType, isExistingRootGroup, isExistingChildGroup) {
                        if (isExistingRootGroup) {
                            emptyList()
                        } else {
                            buildGroupDropdownOptions(
                                allGroups = allGroups,
                                excludeId = initial?.id,
                                language = language,
                                allowRootOption = !isExistingChildGroup,
                                filterTargetType = targetType.ifBlank { null }
                            )
                        }
                    }
                    var expandedGroupSelection by remember { mutableStateOf(false) }

                    val selectedParentObj = remember(groupId, allGroups) {
                        groupId.toIntOrNull()?.let { pId -> allGroups.find { it.id == pId } }
                    }

                    val selectedParentDisplay = when {
                        isExistingRootGroup -> {
                            if (language == AppLanguage.EN) "— Root Category (Domain) —" else "— Šakninė kategorija (Root) —"
                        }
                        !isChild || groupId.isBlank() || groupId == "0" -> {
                            if (language == AppLanguage.EN) "— Root Category (Domain) —" else "— Šakninė kategorija (Root) —"
                        }
                        selectedParentObj != null -> {
                            val indent = "    ".repeat(selectedParentObj.level ?: 0)
                            val prefix = if ((selectedParentObj.level ?: 0) == 0) "📁 " else "└── 📁 "
                            "$indent$prefix${selectedParentObj.name.formatGroupName(language)}"
                        }
                        else -> if (language == AppLanguage.EN) "Select parent folder..." else "Pasirinkite tėvinį aplanką..."
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📁", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (language == AppLanguage.EN) "Directory Placement (Parent Group)" else "Vieta katalogų struktūroje (Tėvinė grupė)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                                    Spacer(Modifier.width(6.dp))
                                    Text(getTypesString("enabled", language), style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isExistingRootGroup) {
                                    OutlinedTextField(
                                        value = selectedParentDisplay,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text(if (language == AppLanguage.EN) "Parent Folder" else "Tėvinis aplankas") },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "Fiksuota šaknis",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1.4f),
                                        singleLine = true
                                    )
                                } else {
                                    ExposedDropdownMenuBox(
                                        expanded = expandedGroupSelection,
                                        onExpandedChange = { expandedGroupSelection = !expandedGroupSelection },
                                        modifier = Modifier.weight(1.4f)
                                    ) {
                                        OutlinedTextField(
                                            value = selectedParentDisplay,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(if (language == AppLanguage.EN) "Parent Folder" else "Tėvinis aplankas") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroupSelection) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedGroupSelection,
                                            onDismissRequest = { expandedGroupSelection = false }
                                        ) {
                                            groupDropdownOptions.forEach { opt ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = opt.displayName,
                                                            fontWeight = if (opt.depth == 0) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (opt.group == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    },
                                                    onClick = {
                                                        if (opt.group == null) {
                                                            groupId = ""
                                                            isChild = false
                                                        } else {
                                                            groupId = opt.group.id?.toString() ?: ""
                                                            isChild = true
                                                            if (!opt.group.targetType.isNullOrBlank()) {
                                                                targetType = opt.group.targetType!!
                                                            }
                                                        }
                                                        expandedGroupSelection = false
                                                        focusManager.clearFocus()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isExistingRootGroup) {
                                    OutlinedTextField(
                                        value = targetType.toFriendlyTypeName(language),
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text(getTypesString("target_type", language)) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "Fiksuotas tipas",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                } else if (!isChild) {
                                    var expandedTargetType by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = expandedTargetType,
                                        onExpandedChange = { expandedTargetType = !expandedTargetType },
                                        modifier = Modifier.weight(1f)
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
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = targetType.toFriendlyTypeName(language),
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text(getTypesString("target_type", language)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }

                            if (isExistingRootGroup) {
                                Text(
                                    text = if (language == AppLanguage.EN) "ℹ Level 0 root category cannot be reassigned to another branch"
                                    else "ℹ 0 lygio šakninė grupė negali būti priskirta kitai atšakai",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            } else if (isExistingChildGroup) {
                                Text(
                                    text = if (language == AppLanguage.EN) "ℹ Can only be organized within ${targetType.toFriendlyTypeName(language)} branch"
                                    else "ℹ Galima keisti tik ${targetType.toFriendlyTypeName(language)} atšakos ribose",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            // Breadcrumbs / path preview
                            val currentBreadcrumb = remember(groupId, targetType, isChild, allGroups, name, language) {
                                val formattedCurrentName = name.formatGroupName(language).trim()
                                if (groupId.isBlank() || groupId == "0" || !isChild) {
                                    val rootDomain = targetType.ifBlank { "Root" }.toFriendlyTypeName(language)
                                    val selfName = formattedCurrentName.ifBlank { rootDomain }
                                    "📁 $selfName"
                                } else {
                                    val pId = groupId.toIntOrNull()
                                    val parts = mutableListOf<String>()
                                    var curr = allGroups.find { it.id == pId }
                                    val visited = mutableSetOf<Int>()
                                    while (curr != null && curr.id != null && !visited.contains(curr.id!!)) {
                                        visited.add(curr.id!!)
                                        parts.add(0, curr.name.formatGroupName(language))
                                        curr = allGroups.find { it.id == curr?.parentGroupId }
                                    }
                                    val prefix = parts.joinToString(" > ") { "📁 $it" }
                                    if (formattedCurrentName.isBlank()) prefix else "$prefix > 📁 $formattedCurrentName"
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.EN) "Path:" else "Kelias:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = currentBreadcrumb,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                if (language == AppLanguage.EN) "Level $computedLevel" else "Lygis $computedLevel",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )
                                    if (targetType.isNotBlank()) {
                                        Spacer(Modifier.width(4.dp))
                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    targetType.toFriendlyTypeName(language),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Row 2: Grupė, Brūkšninis kodas / Matavimo vienetas, Switch'ai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var expandedGroupSelection by remember { mutableStateOf(false) }
                        val productGroupOptions = remember(allGroups, typeStr, language) {
                            buildGroupDropdownOptions(
                                allGroups = allGroups,
                                excludeId = null,
                                language = language,
                                allowRootOption = false,
                                filterTargetType = typeStr
                            )
                        }

                        val selectedGroupObj = remember(groupId, allGroups) {
                            allGroups.find { it.id?.toString() == groupId } ?: rootGroupForEditor
                        }
                        val selectedGroupName = selectedGroupObj?.let { g ->
                            val indent = "    ".repeat(g.level ?: 0)
                            val prefix = if ((g.level ?: 0) == 0) "📁 " else "└── 📁 "
                            "$indent$prefix${g.name.formatGroupName(language)}"
                        } ?: if (groupId.isNotBlank() && groupId != "0") groupId else (rootGroupForEditor?.name?.formatGroupName(language) ?: "Pasirinkite grupę...")

                        ExposedDropdownMenuBox(
                            expanded = expandedGroupSelection,
                            onExpandedChange = { expandedGroupSelection = !expandedGroupSelection },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = selectedGroupName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(getTypesString("group_id_opt", language)) },
                                trailingIcon = { 
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroupSelection) 
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = expandedGroupSelection,
                                onDismissRequest = { expandedGroupSelection = false }
                            ) {
                                if (productGroupOptions.isEmpty()) {
                                    if (rootGroupForEditor != null) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "📁 " + rootGroupForEditor.name.formatGroupName(language),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            onClick = {
                                                groupId = rootGroupForEditor.id?.toString() ?: "0"
                                                expandedGroupSelection = false
                                            }
                                        )
                                    }
                                } else {
                                    productGroupOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = opt.displayName,
                                                    fontWeight = if (opt.depth == 0) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                groupId = opt.group?.id?.toString() ?: "0"
                                                expandedGroupSelection = false
                                            }
                                        )
                                    }
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
            }

            if (typeStr == "GroupSv" && !isChild && initial == null && unassignedChildren.isNotEmpty()) {
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

                    if (typeStr == "GroupSv" && initial?.id != null) {
                        val initialId = initial.id
                        val directChildren = remember(allGroups, initialId) {
                            if (initialId != null) {
                                allGroups.filter { it.parentGroupId == initialId }
                            } else emptyList()
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.EN) "Child Groups (Subbranches)" else "Vaikinės grupės (Pošakiai)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (directChildren.isEmpty()) {
                                    Text(
                                        text = if (language == AppLanguage.EN) "This group has no direct child groups yet." else "Ši grupė dar neturi tiesioginių vaikinių grupių.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        directChildren.forEach { child ->
                                            var showDeleteChildConfirm by remember { mutableStateOf(false) }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "↳ ${child.name.formatGroupName(language)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("Lygis ${child.level ?: 1}") },
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                IconButton(
                                                    onClick = { showDeleteChildConfirm = true },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Ištrinti pošakį",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            if (showDeleteChildConfirm) {
                                                AlertDialog(
                                                    onDismissRequest = { showDeleteChildConfirm = false },
                                                    title = { Text("Trinti vaikinę grupę") },
                                                    text = { Text("Ar tikrai norite ištrinti „${child.name}\"?") },
                                                    confirmButton = {
                                                        Button(
                                                            onClick = {
                                                                showDeleteChildConfirm = false
                                                                scope.launch {
                                                                    val childId = child.id
                                                                    if (childId != null) {
                                                                        apiClient.typeRepository.delete(childId, child.type)
                                                                        onReloadGroups?.invoke()
                                                                    }
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                        ) { Text("Ištrinti") }
                                                    },
                                                    dismissButton = {
                                                        TextButton(onClick = { showDeleteChildConfirm = false }) { Text("Atšaukti") }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                var quickChildName by remember { mutableStateOf("") }
                                var isQuickAdding by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quickChildName,
                                        onValueChange = { quickChildName = it },
                                        label = { Text(if (language == AppLanguage.EN) "New child group name" else "Naujo pošakio pavadinimas") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            if (quickChildName.isNotBlank() && initialId != null) {
                                                isQuickAdding = true
                                                scope.launch {
                                                    val newChild = TypeDTO(
                                                        name = quickChildName.trim(),
                                                        type = "GroupSv",
                                                        isChild = true,
                                                        parentGroupId = initialId,
                                                        groupId = null,
                                                        level = (initial.level ?: 0) + 1,
                                                        targetType = initial.targetType,
                                                        enabled = true,
                                                        attributes = emptyList()
                                                    )
                                                    val res = apiClient.typeRepository.create(newChild)
                                                    if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
                                                        quickChildName = ""
                                                        onReloadGroups?.invoke()
                                                    }
                                                    isQuickAdding = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.height(56.dp),
                                        enabled = quickChildName.isNotBlank() && !isQuickAdding
                                    ) {
                                        if (isQuickAdding) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (language == AppLanguage.EN) "Add child" else "Pridėti pošakį")
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
                                    if (attr.attributeType == "JSON_STRING") {
                                        ImageAttributeField(
                                            label = fieldLabel,
                                            value = attr.value,
                                            apiClient = apiClient,
                                            language = language,
                                            onValueChange = { newVal ->
                                                val updated = attr.copy(value = newVal)
                                                attributes[index] = updated
                                                dirtyFields[attr.name] = true
                                            }
                                        )
                                    } else if (attr.attributeType == "BOOLEAN") {
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
                                parentGroupId = if (typeStr == "GroupSv") {
                                    if (isChild) groupId.toIntOrNull() else null
                                } else null,
                                level = if (typeStr == "GroupSv") {
                                    if (isChild) computedLevel else 0
                                } else null,
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

