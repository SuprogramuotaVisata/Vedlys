package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.rememberLazyListState
import com.suprogramuota_visata.vedlys.utils.LazyListVerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.ExtAttributeDTO
import com.suprogramuota_visata.api.domain.models.ExtTemplateDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import kotlinx.coroutines.launch

fun String.toFriendlyTypeName(): String {
    return when (this) {
        "ProductSv" -> "Prekė"
        "ServiceSv" -> "Paslauga"
        "LocationSv" -> "Lokacija"
        "WarehouseSv" -> "Sandėlis"
        "DivisionSv" -> "Padalinys"
        "PartnerSv" -> "Partneris"
        "UnitSv" -> "Matavimo vienetas"
        "DocSv" -> "Dokumentas"
        "EmployeeSv" -> "Darbuotojas"
        "BankSv" -> "Bankas"
        "AccountSv" -> "Sąskaita"
        "TaxSv" -> "Mokestis"
        "CurrencySv" -> "Valiuta"
        "CountrySv" -> "Šalis"
        else -> this
    }
}

data class StandardFieldInfo(val name: String, val type: String)

val PredefinedStandardFields = mapOf(
    "ProductSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Kodas", "STRING"),
        StandardFieldInfo("Brūkšninis kodas", "STRING"),
        StandardFieldInfo("Matavimo vienetas", "STRING")
    ),
    "ServiceSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Kodas", "STRING"),
        StandardFieldInfo("Brūkšninis kodas", "STRING"),
        StandardFieldInfo("Matavimo vienetas", "STRING")
    ),
    "LocationSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Kodas", "STRING"),
        StandardFieldInfo("Brūkšninis kodas", "STRING")
    ),
    "WarehouseSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Kodas", "STRING"),
        StandardFieldInfo("Brūkšninis kodas", "STRING")
    ),
    "DivisionSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Kodas", "STRING"),
        StandardFieldInfo("Brūkšninis kodas", "STRING")
    ),
    "PartnerSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Kodas", "STRING")
    ),
    "UnitSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Kodas", "STRING"),
        StandardFieldInfo("Brūkšninis kodas", "STRING"),
        StandardFieldInfo("Konvertavimo koeficientas", "DECIMAL")
    ),
    "BankSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Banko sąskaita", "STRING"),
        StandardFieldInfo("Bankas / SWIFT", "STRING")
    ),
    "TransactionSv" to listOf(
        StandardFieldInfo("Dokumento numeris", "STRING"),
        StandardFieldInfo("Dokumento data", "DATE_TIME"),
        StandardFieldInfo("Partneris", "STRING"),
        StandardFieldInfo("Iš Sandėlio", "STRING"),
        StandardFieldInfo("Į Sandėlį", "STRING"),
        StandardFieldInfo("Iš Padalinio", "STRING"),
        StandardFieldInfo("Į Padalinį", "STRING")
    ),
    "FinancialTransactionDetailSv" to listOf(
        StandardFieldInfo("Matas", "STRING"),
        StandardFieldInfo("Sandėlis", "WarehouseSv"),
        StandardFieldInfo("Padalinys", "DivisionSv")
    ),
    "OperationalTransactionDetailSv" to listOf(
        StandardFieldInfo("Matas", "STRING"),
        StandardFieldInfo("Sandėlis", "WarehouseSv"),
        StandardFieldInfo("Padalinys", "DivisionSv")
    ),
    "DeliveryTransactionDetailSv" to listOf(
        StandardFieldInfo("Matas", "STRING"),
        StandardFieldInfo("Sandėlis", "WarehouseSv"),
        StandardFieldInfo("Padalinys", "DivisionSv")
    ),
    "CrmTransactionDetailSv" to listOf(
        StandardFieldInfo("Matas", "STRING"),
        StandardFieldInfo("Sandėlis", "WarehouseSv"),
        StandardFieldInfo("Padalinys", "DivisionSv")
    ),
    "OwnerSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Įmonės pavadinimas / Vardas, pavardė", "STRING"),
        StandardFieldInfo("Įmonės / asmens kodas", "STRING"),
        StandardFieldInfo("PVM mokėtojo kodas", "STRING"),
        StandardFieldInfo("Adresas", "Local_Type"),
        StandardFieldInfo("Bankas", "Local_Type")
    ),
    "AddressSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING"),
        StandardFieldInfo("Adresas", "STRING")
    ),
    "GroupSv" to listOf(
        StandardFieldInfo("Pavadinimas", "STRING")
    )
)

fun normalizeOwnerType(ownerType: String): String {
    val clean = if (ownerType.endsWith("Sv")) ownerType else "${ownerType}Sv"
    return when (clean) {
        "PartnerisSv" -> "PartnerSv"
        "PrekėSv" -> "ProductSv"
        "PaslaugaSv" -> "ServiceSv"
        "LokacijaSv" -> "LocationSv"
        "SandėlisSv" -> "WarehouseSv"
        "PadalinysSv" -> "DivisionSv"
        "Matavimo vienetasSv" -> "UnitSv"
        "SavininkasSv" -> "OwnerSv"
        "AdresasSv" -> "AddressSv"
        "BankasSv" -> "BankSv"
        "DokumentasSv" -> "TransactionSv"
        "GrupėSv" -> "GroupSv"
        else -> clean
    }
}

fun ensureStandardFields(ownerType: String, existing: List<ExtAttributeDTO>): List<ExtAttributeDTO> {
    val stdFields = PredefinedStandardFields[normalizeOwnerType(ownerType)] ?: emptyList()
    val stdNames = stdFields.map { it.name }.toSet()
    
    val existingStd = existing.filter { it.name in stdNames }
    val existingCustom = existing.filter { it.name !in stdNames }
    
    val stdResult = stdFields.map { std ->
        existingStd.find { it.name == std.name } ?: ExtAttributeDTO(
            name = std.name,
            attributeType = std.type,
            defaultValue = null,
            validateRule = false,
            validations = emptyList()
        )
    }
    
    return stdResult + existingCustom
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtAttributeRow(
    attr: ExtAttributeDTO,
    isStandard: Boolean,
    onUpdate: (ExtAttributeDTO) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp).fillMaxWidth()) {
            // Visi 4 laukai vienoje eilutėje
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Pavadinimas
                OutlinedTextField(
                    value = attr.name,
                    onValueChange = { newVal -> if (!isStandard) onUpdate(attr.copy(name = newVal)) },
                    readOnly = isStandard,
                    label = { Text("Pavadinimas", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // 2. Tipas (Dropdown)
                val dataTypes = listOf("INTEGER", "LONG", "DECIMAL", "DATE", "DATE_TIME", "TIMESTAMP", "STRING", "BOOL", "STRING_LIST", "URL_LIST", "JSON_STRING", "XML_STRING", "EMAIL_LIST", "Local_Type")
                var expandedType by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = attr.attributeType,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tipas", style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        colors = OutlinedTextFieldDefaults.colors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        dataTypes.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption, style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    onUpdate(attr.copy(attributeType = selectionOption))
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                // 3. Tag
                OutlinedTextField(
                    value = attr.tag ?: "",
                    onValueChange = { newVal -> onUpdate(attr.copy(tag = newVal.ifEmpty { null })) },
                    label = { Text("Tag", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // 4. Pradinė reikšmė (su [V] Validuoti dešinėje)
                OutlinedTextField(
                    value = attr.defaultValue ?: "",
                    onValueChange = { newVal -> onUpdate(attr.copy(defaultValue = newVal.ifEmpty { null })) },
                    label = { Text("Pradinė reikšmė", style = MaterialTheme.typography.bodySmall) },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Checkbox(
                                checked = attr.validateRule,
                                onCheckedChange = { newVal -> onUpdate(attr.copy(validateRule = newVal)) },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                "Validuoti",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (attr.validateRule) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.weight(1.3f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // 5. Veiksmai (tik nestandartiniams atributams)
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Pašalinti", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Vidinis tipas (Local_Type)
            if (attr.attributeType == "Local_Type") {
                Spacer(Modifier.height(4.dp))
                val existingVal = attr.validations.find { it.ruleId == 12 }
                var selectedTargetTypeGroup by remember(attr.validations) {
                    mutableStateOf(existingVal?.args?.firstOrNull() ?: "")
                }
                var expandedTypeGroup by remember { mutableStateOf(false) }
                val applicableGroups = listOf("ProductSv", "PartnerSv", "ServiceSv", "DivisionSv", "WarehouseSv", "LocationSv", "AddressSv", "UnitSv", "SettingsSv", "OwnerSv", "BankSv")

                ExposedDropdownMenuBox(
                    expanded = expandedTypeGroup,
                    onExpandedChange = { expandedTypeGroup = !expandedTypeGroup },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTargetTypeGroup.toFriendlyTypeName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vidinis Tipas (Local Type Group)", style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeGroup) },
                        colors = OutlinedTextFieldDefaults.colors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeGroup,
                        onDismissRequest = { expandedTypeGroup = false }
                    ) {
                        applicableGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.toFriendlyTypeName(), style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    selectedTargetTypeGroup = group
                                    val currentVals = attr.validations.toMutableList()
                                    currentVals.removeAll { it.ruleId == 12 }
                                    currentVals.add(com.suprogramuota_visata.api.domain.models.AttributeValidationDTO(12, listOf(group)))
                                    onUpdate(attr.copy(validations = currentVals))
                                    expandedTypeGroup = false
                                }
                            )
                        }
                    }
                }
            }

            // Validacijos taisyklės
            if (attr.validateRule) {
                var rulesExpanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Papildomos validacijos taisyklės:", 
                        fontWeight = FontWeight.SemiBold, 
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(
                        onClick = { rulesExpanded = !rulesExpanded },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(
                            text = if (rulesExpanded) "Suskleisti ▲" else "Išskleisti ▼", 
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (rulesExpanded) {
                    ValidationUIRules.forEach { rule ->
                        val existingVal = attr.validations.find { it.ruleId == rule.id }
                        val isChecked = existingVal != null

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                checked = isChecked, 
                                onCheckedChange = { checked ->
                                    val currentVals = attr.validations.toMutableList()
                                    if (checked) {
                                        currentVals.add(com.suprogramuota_visata.api.domain.models.AttributeValidationDTO(rule.id, List(rule.getArgNames(com.suprogramuota_visata.vedlys.AppLanguage.LT).size) { "" }))
                                    } else {
                                        currentVals.removeAll { it.ruleId == rule.id }
                                    }
                                    onUpdate(attr.copy(validations = currentVals))
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("${rule.id}. ${rule.getDescription(com.suprogramuota_visata.vedlys.AppLanguage.LT)}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        }

                        if (isChecked && rule.getArgNames(com.suprogramuota_visata.vedlys.AppLanguage.LT).isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(start = 28.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rule.getArgNames(com.suprogramuota_visata.vedlys.AppLanguage.LT).forEachIndexed { argIdx, argName ->
                                    OutlinedTextField(
                                        value = existingVal?.args?.getOrNull(argIdx) ?: "",
                                        onValueChange = { newArgVal ->
                                            val currentVals = attr.validations.toMutableList()
                                            val valIdx = currentVals.indexOfFirst { it.ruleId == rule.id }
                                            if (valIdx >= 0) {
                                                val updatedArgs = currentVals[valIdx].args.toMutableList()
                                                if (argIdx < updatedArgs.size) {
                                                    updatedArgs[argIdx] = newArgVal
                                                }
                                                currentVals[valIdx] = currentVals[valIdx].copy(args = updatedArgs)
                                                onUpdate(attr.copy(validations = currentVals))
                                            }
                                        },
                                        label = { Text(argName, style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val activeRules = attr.validations.mapNotNull { v -> ValidationUIRules.find { it.id == v.ruleId } }
                    if (activeRules.isNotEmpty()) {
                        Text(
                            text = "Aktyvios taisyklės: " + activeRules.joinToString { "${it.id}. ${it.getDescription(com.suprogramuota_visata.vedlys.AppLanguage.LT)}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Nėra parinktų taisyklių",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtTemplateDialog(
    apiClient: ApiSvClient,
    ownerType: String,
    onDismiss: () -> Unit,
    onChanged: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val normalizedOwnerType = remember(ownerType) { normalizeOwnerType(ownerType) }
    val scope = rememberCoroutineScope()
    var templates by remember { mutableStateOf<List<ExtTemplateDTO>>(emptyList()) }
    var selectedTemplate by remember { mutableStateOf<ExtTemplateDTO?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    var templateName by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    var attributes by remember { mutableStateOf<List<ExtAttributeDTO>>(ensureStandardFields(normalizedOwnerType, emptyList())) }

    var isSaving by remember { mutableStateOf(false) }

    fun refreshTemplates() {
        scope.launch {
            isLoading = true
            val result = apiClient.extTemplateRepository.getTemplatesByOwnerType(normalizedOwnerType)
            if (result.isSuccess) {
                templates = result.getOrNull() ?: emptyList()
                if (selectedTemplate != null) {
                    val updated = templates.find { it.id == selectedTemplate?.id }
                    if (updated != null) {
                        selectedTemplate = updated
                        templateName = updated.name
                        isDefault = updated.isDefault
                        attributes = ensureStandardFields(normalizedOwnerType, updated.attributes)
                    }
                }
            } else {
                errorMsg = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(normalizedOwnerType) {
        refreshTemplates()
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize(0.96f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isEditing) {
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Grįžti")
                    }
                    if (onHome != null) {
                        IconButton(onClick = { onDismiss(); onHome() }) {
                            Icon(Icons.Default.Home, contentDescription = "Pagrindinis")
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Šablonai: ${normalizedOwnerType.toFriendlyTypeName()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    IconButton(onClick = { isEditing = false; errorMsg = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Grįžti į sąrašą")
                    }
                    if (onHome != null) {
                        IconButton(onClick = { onDismiss(); onHome() }) {
                            Icon(Icons.Default.Home, contentDescription = "Pagrindinis")
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (selectedTemplate == null) "Naujas šablonas" else "Redaguojamas šablonas: ${selectedTemplate?.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@AlertDialog
            }

            if (!isEditing) {
                // ==========================================
                // 1. ESAMŲ ŠABLONŲ SĄRAŠAS (Katalogo stilius)
                // ==========================================
                Column(modifier = Modifier.fillMaxSize()) {
                    // Po Title sekanti eilutė: Tekstas "Esami šablonai" ir "+" mygtukas
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Esami šablonai",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                selectedTemplate = null
                                templateName = ""
                                isDefault = false
                                attributes = ensureStandardFields(normalizedOwnerType, emptyList())
                                errorMsg = null
                                isEditing = true
                            },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, "Kurti naują", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Naujas šablonas", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    val listState = rememberLazyListState()
                    val stdFields = PredefinedStandardFields[normalizedOwnerType] ?: emptyList()
                    val stdNames = stdFields.map { it.name }.toSet()

                    if (templates.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nėra sukurtų šablonų. Paspauskite „+ Naujas šablonas“, kad sukurtumėte.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize().padding(end = 8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(templates) { _, tpl ->
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Checkbox / žyma
                                            Checkbox(
                                                checked = tpl.isDefault,
                                                onCheckedChange = null,
                                                enabled = false,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))

                                            // Šablono pavadinimas ir metaduomenys
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = tpl.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    AssistChip(
                                                        onClick = {},
                                                        label = { Text(normalizedOwnerType.toFriendlyTypeName()) }
                                                    )
                                                    if (tpl.isDefault) {
                                                        AssistChip(
                                                            onClick = {},
                                                            label = { Text("Numatytasis (Default)") },
                                                            colors = AssistChipDefaults.assistChipColors(
                                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                            )
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                val stdCount = tpl.attributes.count { it.name in stdNames }
                                                val customCount = tpl.attributes.size - stdCount
                                                Text(
                                                    text = "ID: ${tpl.id ?: "—"}  •  Standartiniai atributai: $stdCount  •  Papildomi: $customCount  •  Iš viso: ${tpl.attributes.size}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            // Veiksmų mygtukai dešinėje (tik Koreguoti ir Ištrinti)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        selectedTemplate = tpl
                                                        templateName = tpl.name
                                                        isDefault = tpl.isDefault
                                                        attributes = ensureStandardFields(normalizedOwnerType, tpl.attributes)
                                                        errorMsg = null
                                                        isEditing = true
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Edit, "Koreguoti", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            isSaving = true
                                                            val res = apiClient.extTemplateRepository.deleteTemplate(tpl.id!!)
                                                            if (res.isFailure) {
                                                                errorMsg = res.exceptionOrNull()?.message
                                                            } else {
                                                                refreshTemplates()
                                                                onChanged()
                                                            }
                                                            isSaving = false
                                                        }
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, "Ištrinti", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            LazyListVerticalScrollbar(
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                state = listState
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // 2. ŠABLONO FORMA: DVI PAGRINDINĖS KORTELĖS
                // ==========================================
                val editorScrollState = rememberLazyListState()
                val stdFields = PredefinedStandardFields[normalizedOwnerType] ?: emptyList()
                val stdNames = stdFields.map { it.name }.toSet()
                val standardAttrs = attributes.filter { it.name in stdNames }
                val customAttrs = attributes.filter { it.name !in stdNames }

                Column(modifier = Modifier.fillMaxSize()) {
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Šablono pavadinimas + Default varnelė (išlygiuotas pagal LazyColumn dešinį kraštą)
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Šablono pavadinimas", style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Checkbox(
                                    checked = isDefault,
                                    onCheckedChange = { isDefault = it },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Numatytasis (Default)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            state = editorScrollState,
                            modifier = Modifier.fillMaxSize().padding(end = 8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // -------------------------------------------------------------
                            // a) Kortelė "Standartiniai atributai"
                            // -------------------------------------------------------------
                            item {
                                Text(
                                    text = "Standartiniai atributai",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                                )
                            }

                            items(standardAttrs.size) { i ->
                                val stdAttr = standardAttrs[i]
                                val fullIdx = attributes.indexOfFirst { it.name == stdAttr.name }
                                ExtAttributeRow(
                                    attr = stdAttr,
                                    isStandard = true,
                                    onUpdate = { updated ->
                                        val mut = attributes.toMutableList()
                                        if (fullIdx >= 0) mut[fullIdx] = updated
                                        attributes = mut
                                    }
                                )
                            }

                            // -------------------------------------------------------------
                            // b) Kortelė "Papildomi atributai"
                            // -------------------------------------------------------------
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Papildomi atributai",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Button(
                                        onClick = {
                                            attributes = attributes + ExtAttributeDTO(
                                                name = "",
                                                attributeType = "STRING",
                                                defaultValue = null,
                                                validateRule = false,
                                                tag = null
                                            )
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "Papildyti", modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Papildyti", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }

                            if (customAttrs.isEmpty()) {
                                item {
                                    Text(
                                        text = "Nėra papildomų atributų. Paspauskite „+ Papildyti“, kad sukurtumėte naują atributą.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            } else {
                                items(customAttrs.size) { i ->
                                    val customAttr = customAttrs[i]
                                    val fullIdx = attributes.indexOf(customAttr)
                                    ExtAttributeRow(
                                        attr = customAttr,
                                        isStandard = false,
                                        onUpdate = { updated ->
                                            val mut = attributes.toMutableList()
                                            if (fullIdx >= 0) mut[fullIdx] = updated
                                            attributes = mut
                                        },
                                        onDelete = {
                                            val mut = attributes.toMutableList()
                                            if (fullIdx >= 0) mut.removeAt(fullIdx)
                                            attributes = mut
                                        }
                                    )
                                }
                            }
                        }
                        LazyListVerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            state = editorScrollState
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                Button(
                    onClick = {
                        if (templateName.isBlank()) {
                            errorMsg = "Šablono pavadinimas privalomas"
                            return@Button
                        }
                        if (attributes.any { it.name.isBlank() }) {
                            errorMsg = "Visi atributai turi turėti pavadinimus"
                            return@Button
                        }
                        val listTypes = listOf("STRING_LIST", "EMAIL_LIST", "URL_LIST")
                        if (attributes.any { attr -> 
                            attr.attributeType in listTypes && 
                            (attr.validations.find { it.ruleId == 6 }?.args?.firstOrNull()?.isBlank() != false) 
                        }) {
                            errorMsg = "Sąrašo tipo atributams (STRING_LIST, EMAIL_LIST, URL_LIST) privaloma nurodyti pasirinkimų sąrašą (Validacijos taisyklė Nr. 6)"
                            return@Button
                        }
                        try {
                            attributes.forEach { attr ->
                                val rule6Args = attr.validations.find { it.ruleId == 6 }?.args?.firstOrNull()
                                if (!rule6Args.isNullOrBlank()) {
                                    if (attr.attributeType == "EMAIL_LIST") {
                                        val emails = rule6Args.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
                                        emails.forEach { email ->
                                            if (!email.matches(emailRegex)) throw IllegalArgumentException("Atributo '${attr.name}' reikšmė neteisinga: neteisingas el. pašto formatas (\$email)")
                                        }
                                    } else if (attr.attributeType == "URL_LIST") {
                                        val urls = rule6Args.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                        val urlRegex = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?\$".toRegex()
                                        urls.forEach { url ->
                                            if (!url.matches(urlRegex)) throw IllegalArgumentException("Atributo '${attr.name}' reikšmė neteisinga: neteisingas URL formatas (\$url)")
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            errorMsg = e.message
                            return@Button
                        }
                        scope.launch {
                            isSaving = true
                            errorMsg = null
                            val dto = ExtTemplateDTO(
                                id = selectedTemplate?.id,
                                name = templateName,
                                ownerType = normalizedOwnerType,
                                isDefault = isDefault,
                                attributes = attributes
                            )
                            val res = if (dto.id == null) {
                                apiClient.extTemplateRepository.createTemplate(dto)
                            } else {
                                apiClient.extTemplateRepository.updateTemplate(dto.id!!, dto)
                            }

                            if (res.isSuccess) {
                                selectedTemplate = res.getOrNull()
                                isEditing = false
                                refreshTemplates()
                                onChanged()
                            } else {
                                errorMsg = res.exceptionOrNull()?.message
                            }
                            isSaving = false
                        }
                    },
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "Saugoma..." else "Išsaugoti šabloną", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (isEditing) {
                TextButton(onClick = { isEditing = false; errorMsg = null }, enabled = !isSaving) {
                    Text("Atšaukti")
                }
            }
        }
    )
}
