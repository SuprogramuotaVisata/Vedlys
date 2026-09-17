package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.TypeDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableTypeDropdown(
    apiClient: ApiSvClient,
    typeName: String, // pvz. "Partner", "Warehouse", "Item"
    label: String,
    selectedTypeId: Int?,
    onSelected: (TypeDTO?) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    colors: TextFieldColors? = null,
    onOpenFullCreate: (() -> Unit)? = null,
    kindOptions: List<Pair<String, String>>? = null,
    selectedKind: String? = null,
    onKindSelected: ((String) -> Unit)? = null
) {
    var types by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    val normalizedTypeName = when (typeName) {
        "Product", "Item", "Prekė", "ProductSv" -> "ProductSv"
        "Service", "Paslauga", "ServiceSv" -> "ServiceSv"
        "Partner", "Partneris", "PartnerSv" -> "PartnerSv"
        "Warehouse", "Sandėlis", "WarehouseSv" -> "WarehouseSv"
        "Division", "Department", "Padalinys", "DivisionSv" -> "DivisionSv"
        "Location", "Lokacija", "LocationSv" -> "LocationSv"
        "Unit", "Matas", "Matavimo vienetas", "UnitSv" -> "UnitSv"
        else -> typeName
    }

    val scope = rememberCoroutineScope()

    // Užkrauname tipus iš serverio
    LaunchedEffect(normalizedTypeName) {
        isLoading = true
        when (val result = apiClient.typeRepository.getAllByType(normalizedTypeName)) {
            is ApiResult.Success -> {
                types = result.data ?: emptyList()
                val selectedType = types.find { it.id == selectedTypeId }
                if (selectedType != null) {
                    searchQuery = selectedType.code?.takeIf { it.isNotBlank() } ?: selectedType.name
                }
            }
            is ApiResult.Error -> {
                // Klaida ignoruojama UI lygyje, tiesiog nebus pasirinkimų
            }
        }
        isLoading = false
    }

    // Kai atsinaujina selectedTypeId iš išorės
    LaunchedEffect(selectedTypeId, types) {
        if (selectedTypeId != null) {
            val selectedType = types.find { it.id == selectedTypeId }
            if (selectedType != null) {
                val targetVal = selectedType.code?.takeIf { it.isNotBlank() } ?: selectedType.name
                if (searchQuery != targetVal && searchQuery != selectedType.name) {
                    searchQuery = targetVal
                }
            }
        } else {
            searchQuery = ""
        }
    }

    val filteredTypes = types.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        (it.code?.contains(searchQuery, ignoreCase = true) == true) ||
        (it.barcode?.contains(searchQuery, ignoreCase = true) == true)
    }

    val currentSelectedType = types.find { it.id == selectedTypeId }
    val dynamicLabel = remember(label, currentSelectedType) {
        if (label.contains("(")) {
            label
        } else if (currentSelectedType != null && currentSelectedType.name.isNotBlank()) {
            "$label (${currentSelectedType.name})"
        } else {
            label
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                expanded = true
                if (it.isBlank()) {
                    onSelected(null)
                } else {
                    val exactMatch = types.find { t ->
                        t.code.equals(it.trim(), ignoreCase = true) ||
                        t.barcode.equals(it.trim(), ignoreCase = true)
                    }
                    if (exactMatch != null && exactMatch.id != selectedTypeId) {
                        onSelected(exactMatch)
                    }
                }
            },
            label = {
                Text(
                    text = dynamicLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onSelected(null)
                        expanded = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Išvalyti")
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            colors = colors ?: ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                        val match = types.find {
                            it.code.equals(searchQuery.trim(), ignoreCase = true) ||
                            it.barcode.equals(searchQuery.trim(), ignoreCase = true) ||
                            it.name.equals(searchQuery.trim(), ignoreCase = true)
                        } ?: filteredTypes.firstOrNull()
                        if (match != null) {
                            searchQuery = match.code?.takeIf { it.isNotBlank() } ?: match.name
                            expanded = false
                            onSelected(match)
                        }
                        true
                    } else false
                }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 320.dp)
        ) {
            if (isLoading) {
                DropdownMenuItem(
                    text = { Text("Kraunama...") },
                    onClick = { }
                )
            } else {
                if (kindOptions != null && onKindSelected != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kairėje: (+) Pridėti naują
                        Row(
                            modifier = Modifier
                                .clickable {
                                    expanded = false
                                    if (onOpenFullCreate != null) {
                                        onOpenFullCreate()
                                    } else {
                                        showCreateDialog = true
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "(+) Pridėti naują",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Dešinėje: Slankiklis perjungiantis Prekė -> Paslauga
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                kindOptions.forEach { (kKey, kLabel) ->
                                    val isSelected = kKey == selectedKind
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                        modifier = Modifier.clickable {
                                            if (!isSelected) {
                                                onKindSelected(kKey)
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = kLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = if (searchQuery.isNotBlank()) "(+) Pridėti naują: $searchQuery" else "(+) Pridėti naują",
                                color = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        onClick = {
                            expanded = false
                            if (onOpenFullCreate != null) {
                                onOpenFullCreate()
                            } else {
                                showCreateDialog = true
                            }
                        }
                    )
                }
                HorizontalDivider()

                filteredTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { 
                            val codePrefix = type.code?.takeIf { it.isNotBlank() }?.let { "[$it] " } ?: ""
                            Text("$codePrefix${type.name}") 
                        },
                        onClick = {
                            searchQuery = type.code?.takeIf { it.isNotBlank() } ?: type.name
                            expanded = false
                            onSelected(type)
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        var newTypeName by remember { mutableStateOf(searchQuery) }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Sukurti naują: $label") },
            text = {
                OutlinedTextField(
                    value = newTypeName,
                    onValueChange = { newTypeName = it },
                    label = { Text("Pavadinimas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTypeName.isNotBlank()) {
                            scope.launch {
                                val newType = TypeDTO(
                                    name = newTypeName,
                                    enabled = true,
                                    type = normalizedTypeName
                                )
                                when (val result = apiClient.typeRepository.create(newType)) {
                                    is ApiResult.Success -> {
                                        val created = result.data
                                        if (created != null) {
                                            types = types + created
                                            searchQuery = created.name
                                            onSelected(created)
                                            showCreateDialog = false
                                        }
                                    }
                                    is ApiResult.Error -> {
                                        // Ignore error for now
                                        showCreateDialog = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = newTypeName.isNotBlank()
                ) {
                    Text("Išsaugoti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }
}
