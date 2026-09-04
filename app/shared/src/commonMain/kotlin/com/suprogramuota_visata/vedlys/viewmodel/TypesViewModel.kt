package com.suprogramuota_visata.vedlys.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.TypeDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import kotlinx.coroutines.launch

class TypesViewModel(private val apiClient: ApiSvClient) : BaseViewModel() {

    var typeName by mutableStateOf("ProductSv")
        private set
        
    var showInactive by mutableStateOf(false)
        private set
        
    var groupFilter by mutableStateOf<TypeDTO?>(null)
        private set
        
    var pageSize by mutableStateOf(com.suprogramuota_visata.vedlys.AppSettings.paginationLimit.value)
    var currentOffset = 0L
        private set
    var isLastPage by mutableStateOf(false)
        private set
        
    private var allItems = emptyList<TypeDTO>()
    var items by mutableStateOf<List<TypeDTO>>(emptyList())
        private set
    var selected by mutableStateOf<TypeDTO?>(null)
        private set
    var selectedItemIds by mutableStateOf<Set<Int>>(emptySet())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadAll()
    }

    fun updateTypeName(value: String) {
        if (typeName != value) {
            typeName = value
            groupFilter = null
            loadInitial()
        }
    }

    fun toggleShowInactive(show: Boolean) {
        showInactive = show
        updateItems()
    }

    fun updateGroupFilter(group: TypeDTO?) {
        groupFilter = group
        updateItems()
    }

    private fun updateItems() {
        val activeFiltered = if (showInactive) allItems else allItems.filter { it.enabled }
        items = if (groupFilter == null) activeFiltered else activeFiltered.filter { it.groupId == groupFilter?.id }
    }

    fun loadInitial() {
        currentOffset = 0L
        isLastPage = false
        allItems = emptyList()
        loadAll()
    }

    fun loadMore() {
        if (!isLoading && !isLastPage) {
            currentOffset += pageSize
            loadAll()
        }
    }

    private fun loadAll() {
        val currentType = typeName
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = apiClient.typeRepository.getAllByType(currentType, pageSize, currentOffset)) {
                is ApiResult.Success -> {
                    if (result.data.isEmpty() || result.data.size < pageSize) {
                        isLastPage = true
                    }
                    val updatedList = (allItems + result.data).distinctBy { it.id ?: System.identityHashCode(it) }
                    allItems = updatedList
                    updateItems()
                    isLoading = false
                }
                is ApiResult.Error -> {
                    isLoading = false
                    updateItems()
                    // Paslepiame "failed to fetch" ar 404 klaidas, kaip pageidauta
                    if (!result.message.contains("fetch", ignoreCase = true) && 
                        !result.message.contains("404", ignoreCase = true)) {
                        errorMessage = "Nepavyko įkelti tipų: ${result.message}"
                    }
                }
            }
        }
    }

    fun select(item: TypeDTO?) { selected = item }

    fun create(newType: TypeDTO) {
        val originalType = typeName
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = apiClient.typeRepository.create(newType)) {
                is ApiResult.Success -> {
                    infoMessage = "Sukurta: ${result.data.name}"
                    selected = null
                    // Jei sukurtas tipas skiriasi nuo dabartinio filtro, perjungti filtrą
                    if (newType.type != originalType) {
                        typeName = newType.type
                    }
                    loadInitial()
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = "Nepavyko sukurti: ${result.message}"
                }
            }
        }
    }

    fun update(updated: TypeDTO) {
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = apiClient.typeRepository.update(updated)) {
                is ApiResult.Success -> {
                    infoMessage = "Atnaujinta: ${result.data.name}"
                    selected = null
                    loadInitial()
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = "Nepavyko atnaujinti: ${result.message}"
                }
            }
        }
    }

    fun delete(item: TypeDTO) {
        val id = item.id ?: run {
            errorMessage = "Įrašas neturi ID."
            return
        }
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = apiClient.typeRepository.delete(id, typeName)) {
                is ApiResult.Success -> {
                    infoMessage = "Ištrinta: ${item.name}"
                    if (selected?.id == id) selected = null
                    allItems = allItems.filter { it.id != id }
                    updateItems()
                    loadInitial()
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = "Nepavyko ištrinti: ${result.message}"
                }
            }
        }
    }

    fun toggleSelectAll() {
        val currentIds = items.mapNotNull { it.id }.toSet()
        selectedItemIds = if (currentIds.isNotEmpty() && selectedItemIds.containsAll(currentIds)) {
            emptySet()
        } else {
            currentIds
        }
    }

    fun toggleSelectItem(id: Int) {
        selectedItemIds = if (selectedItemIds.contains(id)) {
            selectedItemIds - id
        } else {
            selectedItemIds + id
        }
    }

    fun clearSelection() {
        selectedItemIds = emptySet()
    }

    fun deleteSelected() {
        val idsToDelete = selectedItemIds.toList()
        if (idsToDelete.isEmpty()) return
        val currentType = typeName
        
        scope.launch {
            isLoading = true
            errorMessage = null
            var deletedCount = 0
            var skippedCount = 0
            val skippedNames = mutableListOf<String>()

            for (id in idsToDelete) {
                val item = allItems.find { it.id == id }
                val res = apiClient.typeRepository.delete(id, currentType)
                if (res is ApiResult.Success) {
                    deletedCount++
                    allItems = allItems.filter { it.id != id }
                } else {
                    skippedCount++
                    item?.name?.let { skippedNames.add(it) }
                }
            }

            selectedItemIds = emptySet()
            updateItems()
            isLoading = false

            if (skippedCount == 0 && deletedCount > 0) {
                infoMessage = "Sėkmingai ištrinta $deletedCount įrašų."
            } else if (deletedCount > 0) {
                infoMessage = "Ištrinta $deletedCount įrašų. $skippedCount įrašų palikta dėl esamų ryšių ar duomenų saugumo taisyklių."
            } else if (skippedCount > 0) {
                errorMessage = "Nė vieno įrašo nepavyko ištrinti: įrašai turi susijusių duomenų (transakcijų, pošakių) arba yra saugomi sistemos."
            }
            loadInitial()
        }
    }

    fun clearError() { errorMessage = null }
    fun clearInfo() { infoMessage = null }
    fun setError(msg: String) { errorMessage = msg }
}

