package com.suprogramuota_visata.vedlys.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.TransactionDTO
import com.suprogramuota_visata.api.domain.models.TypeDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class TasksViewModel(private val apiClient: ApiSvClient) {
    private val viewModelJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var pageSize by mutableStateOf(com.suprogramuota_visata.vedlys.AppSettings.paginationLimit.value)
    var currentOffset = 0L
        private set
    var isLastPage by mutableStateOf(false)
        private set

    // Visi parsiųsti įrašai
    private var allTransactions = listOf<TransactionDTO>()

    // Atvaizduojami įrašai (pritaikius filtrus)
    var transactions by mutableStateOf(listOf<TransactionDTO>())
        private set

    // Filtrai
    var documentTypeFilter by mutableStateOf("Visi")
        private set
    var statusFilter by mutableStateOf("Visi")
        private set
    var groupFilter by mutableStateOf<TypeDTO?>(null)
        private set
    var showBlocked by mutableStateOf(false)
        private set

    var groups by mutableStateOf(listOf<TypeDTO>())
        private set

    init {
        loadInitialTasks()
        loadGroups()
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(5000)
                silentRefresh()
            }
        }
    }

    private fun silentRefresh() {
        if (isLoading) return
        scope.launch {
            val typesFilter = if (documentTypeFilter != "Visi") listOf(documentTypeFilter) else null
            val limitToFetch = currentOffset + pageSize
            when (val result = apiClient.transactionRepository.getTransactions(typesFilter, limitToFetch.toInt(), 0L)) {
                is ApiResult.Success -> {
                    val data = result.data ?: emptyList()
                    allTransactions = data
                    applyFilters()
                }
                is ApiResult.Error -> {
                    // Fail silently
                }
            }
        }
    }

    private fun loadGroups() {
        scope.launch {
            when (val result = apiClient.typeRepository.getAllByType("GroupSv")) {
                is ApiResult.Success -> {
                    val all = result.data ?: emptyList()
                    val validCategories = setOf("Financial", "Operational", "CRM", "Delivery", "Finansai", "Operacijos", "Pristatymas")
                    groups = all.filter { grp ->
                        val n = grp.name.trim()
                        (n in validCategories) && !n.endsWith("Detail", ignoreCase = true) && !n.endsWith("Transaction", ignoreCase = true)
                    }
                }
                is ApiResult.Error -> {
                    // Ignore or handle
                }
            }
        }
    }

    fun loadInitialTasks() {
        currentOffset = 0L
        isLastPage = false
        allTransactions = emptyList()
        loadTasks()
    }

    fun loadMoreTasks() {
        if (!isLoading && !isLastPage) {
            currentOffset += pageSize
            loadTasks()
        }
    }

    private fun loadTasks() {
        if (isLoading) return
        isLoading = true
        errorMessage = null

        scope.launch {
            val typesFilter = if (documentTypeFilter != "Visi") listOf(documentTypeFilter) else null
            
            when (val result = apiClient.transactionRepository.getTransactions(typesFilter, pageSize, currentOffset)) {
                is ApiResult.Success -> {
                    val data = result.data ?: emptyList()
                    if (data.isEmpty() || data.size < pageSize) {
                        isLastPage = true
                    }
                    allTransactions = (allTransactions + data).distinctBy { it.transactionId }
                    applyFilters()
                }
                is ApiResult.Error -> {
                    errorMessage = result.message ?: "Klaida gaunant užduotis"
                }
            }
            isLoading = false
        }
    }

    fun updateDocumentTypeFilter(type: String) {
        documentTypeFilter = type
        loadInitialTasks()
    }

    fun updateStatusFilter(status: String) {
        statusFilter = status
        applyFilters()
    }

    fun updateGroupFilter(group: TypeDTO?) {
        groupFilter = group
        documentTypeFilter = "Visi"
        applyFilters()
    }

    fun toggleShowBlocked(show: Boolean) {
        showBlocked = show
        applyFilters()
    }

    private fun applyFilters() {
        val sorted = allTransactions.sortedWith(
            compareBy<TransactionDTO> { tx ->
                when (tx.status) {
                    "Nauja" -> 1
                    "Vykdoma" -> 2
                    "Baigta" -> 3
                    else -> 4
                }
            }.thenByDescending { it.transactionTime }
        )
        val category = com.suprogramuota_visata.vedlys.ui.screens.getNormalizedGroupCategory(groupFilter?.name)
        val allowedDocTypesForGroup = if (category != null) com.suprogramuota_visata.vedlys.ui.screens.DocumentGroupTypesMap[category] else null

        transactions = sorted.filter { tx ->
            val matchesType = if (documentTypeFilter == "Visi") true else tx.documentType == documentTypeFilter
            val matchesStatus = if (statusFilter == "Visi") true else tx.status == statusFilter
            val matchesGroup = if (allowedDocTypesForGroup == null) {
                if (groupFilter == null) true else tx.groupId == groupFilter?.id
            } else {
                tx.groupId == groupFilter?.id || tx.documentType in allowedDocTypesForGroup
            }
            val matchesBlocked = showBlocked || !tx.isBlocked
            matchesType && matchesStatus && matchesGroup && matchesBlocked
        }
    }

    fun createTask(documentNumber: String, documentDate: Long, documentType: String, onSuccess: () -> Unit) {
        if (isLoading) return
        isLoading = true
        errorMessage = null

        val resolvedGroupName = when (documentType) {
            "FINANCIAL" -> "Financial"
            "OPERATIONAL" -> "Operational"
            "CRM" -> "CRM"
            "DELIVERY" -> "Delivery"
            else -> "Operational"
        }
        val resolvedGroupId = groups.find { it.name.equals(resolvedGroupName, ignoreCase = true) }?.id

        val newTx = TransactionDTO(
            transactionId = UUID.randomUUID().toString(),
            version = 1,
            transactionTime = System.currentTimeMillis(),
            status = "Nauja",
            documentNumber = documentNumber,
            documentDate = documentDate,
            documentType = documentType,
            groupId = resolvedGroupId,
            createdByUserId = 1, // Laikinas sprendimas, turėtų būti imamas iš sesijos
            createdOnDeviceId = 1,
            isBlocked = false,
            enabled = true,
            details = emptyList(),
            attributes = emptyList()
        )

        scope.launch {
            when (val result = apiClient.transactionRepository.create(newTx)) {
                is ApiResult.Success -> {
                    // Po sėkmingo sukūrimo perkrauname sąrašą
                    isLoading = false
                    loadInitialTasks()
                    onSuccess()
                }
                is ApiResult.Error -> {
                    errorMessage = result.message ?: "Klaida kuriant užduotį"
                    isLoading = false
                }
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun dispose() {
        viewModelJob.cancel()
    }
}
