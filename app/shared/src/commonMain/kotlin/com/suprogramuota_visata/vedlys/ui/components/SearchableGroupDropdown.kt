package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.TypeDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableGroupDropdown(
    apiClient: ApiSvClient,
    targetType: String, // pvz. "ProductSv", "PartnerSv", "WarehouseSv"
    label: String = "Grupė",
    selectedGroupId: Int?,
    onSelected: (TypeDTO?) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var groups by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Užkrauname grupes iš serverio ir filtruojame pagal targetType bei isChild == true
    LaunchedEffect(targetType) {
        isLoading = true
        when (val result = apiClient.typeRepository.getAllByType("GroupSv")) {
            is ApiResult.Success -> {
                val allGroups = result.data ?: emptyList()
                // Leidžiama pasirinkti tik šio tipo grupes su isChild = true
                groups = allGroups.filter { g ->
                    g.targetType.equals(targetType, ignoreCase = true) && g.isChild == true
                }
                val selectedGroup = groups.find { it.id == selectedGroupId }
                if (selectedGroup != null) {
                    searchQuery = selectedGroup.name
                }
            }
            is ApiResult.Error -> {
                groups = emptyList()
            }
        }
        isLoading = false
    }

    LaunchedEffect(selectedGroupId, groups) {
        if (selectedGroupId != null) {
            val selectedGroup = groups.find { it.id == selectedGroupId }
            if (selectedGroup != null && searchQuery != selectedGroup.name) {
                searchQuery = selectedGroup.name
            }
        } else {
            searchQuery = ""
        }
    }

    val filteredGroups = groups.filter {
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
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (isLoading) {
                DropdownMenuItem(
                    text = { Text("Kraunama...") },
                    onClick = { }
                )
            } else {
                // Nėra (None) pasirinkimas
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = "Nėra (None)",
                            color = MaterialTheme.colorScheme.secondary
                        ) 
                    },
                    onClick = {
                        searchQuery = ""
                        expanded = false
                        onSelected(null)
                    }
                )
                HorizontalDivider()

                if (filteredGroups.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Nerasta šio tipo vaikinių grupių (isChild = true)") },
                        onClick = { }
                    )
                } else {
                    filteredGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                searchQuery = group.name
                                expanded = false
                                onSelected(group)
                            }
                        )
                    }
                }
            }
        }
    }
}
