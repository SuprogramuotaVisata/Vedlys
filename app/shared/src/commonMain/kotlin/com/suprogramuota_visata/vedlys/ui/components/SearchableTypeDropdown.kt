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
import androidx.compose.ui.text.font.FontWeight
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
    val scope = rememberCoroutineScope()

    // Užkrauname tipus iš serverio
    LaunchedEffect(typeName) {
        isLoading = true
        when (val result = apiClient.typeRepository.getAllByType(typeName)) {
            is ApiResult.Success -> {
                types = result.data ?: emptyList()
                val selectedType = types.find { it.id == selectedTypeId }
                if (selectedType != null) {
                    searchQuery = selectedType.name
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
            if (selectedType != null && searchQuery != selectedType.name) {
                searchQuery = selectedType.name
            }
        } else {
            searchQuery = ""
        }
    }

    val filteredTypes = types.filter {
        it.name.contains(searchQuery, ignoreCase = true)
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
                }
            },
            label = { Text(label) },
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
            modifier = Modifier.menuAnchor().fillMaxWidth()
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
                        text = { Text(type.name) },
                        onClick = {
                            searchQuery = type.name
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
                                    type = typeName
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
