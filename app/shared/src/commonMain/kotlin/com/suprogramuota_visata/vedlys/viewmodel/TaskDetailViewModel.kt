package com.suprogramuota_visata.vedlys.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.AttributeDTO
import com.suprogramuota_visata.api.domain.models.TransactionDTO
import com.suprogramuota_visata.api.domain.models.TransactionDetailDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.api.domain.models.ExtTemplateDTO
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.ui.components.ensureStandardFields
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val apiClient: ApiSvClient,
    private val taskId: String
) {
    private val viewModelJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun setError(msg: String?) {
        errorMessage = msg
    }

    var transaction by mutableStateOf<TransactionDTO?>(null)
        private set

    var extTemplate by mutableStateOf<ExtTemplateDTO?>(null)
        private set

    var availableTemplates by mutableStateOf<List<ExtTemplateDTO>>(emptyList())
        private set

    var showSummarized by mutableStateOf(false)
        private set

    fun toggleShowSummarized(show: Boolean) {
        showSummarized = show
    }

    var isDraft by mutableStateOf(taskId.equals("new", ignoreCase = true))
        private set

    init {
        loadTask()
    }

    private fun getGroupForDocumentType(docType: String): String {
        val typeEnum = com.suprogramuota_visata.enums.DocumentType.fromId(docType)
        if (typeEnum != null) {
            return when (typeEnum.category) {
                com.suprogramuota_visata.enums.DocumentCategory.FINANCIAL -> "Financial"
                com.suprogramuota_visata.enums.DocumentCategory.OPERATIONAL -> "Operational"
                com.suprogramuota_visata.enums.DocumentCategory.DELIVERY -> "Delivery"
                com.suprogramuota_visata.enums.DocumentCategory.CRM -> "CRM"
            }
        }
        return when (docType.uppercase()) {
            "DELIVERY_NOTE", "WAYBILL", "CARRIER_MANIFEST", "PROOF_OF_DELIVERY" -> "Delivery"
            "LEAD_NOTE", "MEETING_LOG", "CALL_LOG", "SUPPORT_TICKET", "CONTRACT" -> "CRM"
            "STOCK_RECEIPT", "STOCK_DISPATCH", "STOCK_TRANSFER", "INVENTORY_COUNT", "PRODUCTION_ORDER", "GOODS_RECEIPT" -> "Operational"
            else -> "Financial"
        }
    }

    fun loadTask() {
        if (isLoading) return
        isLoading = true
        errorMessage = null

        scope.launch {
            if (taskId.equals("new", ignoreCase = true)) {
                val groupCat = getGroupForDocumentType("SALES_INVOICE")
                var nextDocNum = AppSettings.generateNextDocumentNumber(groupCat)

                val allTxResult = apiClient.transactionRepository.getTransactions()
                if (allTxResult is ApiResult.Success) {
                    val listData = allTxResult.data
                    if (listData != null) {
                        val prefix = when(groupCat) {
                            "Financial" -> AppSettings.docSeriesPrefixFinancial.value
                            "Operational" -> AppSettings.docSeriesPrefixOperational.value
                            "Delivery" -> AppSettings.docSeriesPrefixDelivery.value
                            "CRM" -> AppSettings.docSeriesPrefixCRM.value
                            else -> "TX-"
                        }
                        val existingNums = listData.mapNotNull { tx ->
                            val numStr = tx.documentNumber
                            if (numStr.startsWith(prefix, ignoreCase = true)) {
                                numStr.substring(prefix.length).toIntOrNull()
                            } else null
                        }
                        val maxNum = existingNums.maxOrNull() ?: 0
                        val currentNext = when(groupCat) {
                            "Financial" -> AppSettings.docSeriesNextNumberFinancial.value
                            "Operational" -> AppSettings.docSeriesNextNumberOperational.value
                            "Delivery" -> AppSettings.docSeriesNextNumberDelivery.value
                            "CRM" -> AppSettings.docSeriesNextNumberCRM.value
                            else -> 1
                        }
                        val targetNum = maxOf(maxNum + 1, currentNext)
                        AppSettings.setDocSeriesNextNumber(groupCat, targetNum)
                        nextDocNum = "$prefix${String.format(java.util.Locale.US, "%05d", targetNum)}"
                    }
                }

                var initialAttributes = emptyList<AttributeDTO>()
                var defaultTplId: Int? = null
                val tplListResult = apiClient.extTemplateRepository.getTemplatesByOwnerType("TransactionSv")
                if (tplListResult.isSuccess) {
                    val templates = tplListResult.getOrNull() ?: emptyList()
                    availableTemplates = templates
                    val defaultTpl = templates.find { it.isDefault } ?: templates.firstOrNull()
                    if (defaultTpl != null) {
                        extTemplate = defaultTpl
                        defaultTplId = defaultTpl.id?.toInt()
                    }
                }

                val templateAttrs = ensureStandardFields("TransactionSv", extTemplate?.attributes ?: emptyList())
                initialAttributes = templateAttrs.map { ext ->
                    AttributeDTO(
                        name = ext.name,
                        attributeType = ext.attributeType,
                        validate = ext.validateRule,
                        value = when (ext.name) {
                            "Dokumento numeris" -> nextDocNum
                            "Dokumento data" -> System.currentTimeMillis().toString()
                            else -> ext.defaultValue ?: ""
                        },
                        validations = ext.validations,
                        tag = ext.tag
                    )
                }

                val draftTx = TransactionDTO(
                    transactionId = java.util.UUID.randomUUID().toString(),
                    version = 1,
                    transactionTime = System.currentTimeMillis(),
                    status = "Nauja",
                    documentNumber = nextDocNum,
                    documentDate = System.currentTimeMillis(),
                    documentType = "SALES_INVOICE",
                    groupId = null,
                    createdByUserId = 1,
                    createdOnDeviceId = 1,
                    isBlocked = false,
                    enabled = true,
                    details = emptyList(),
                    attributes = initialAttributes,
                    extTemplateId = defaultTplId
                )
                transaction = draftTx
                isDraft = true
                isLoading = false
                return@launch
            }

            val txResult = apiClient.transactionRepository.getByTransactionId(taskId)

            when (txResult) {
                is ApiResult.Success -> {
                    var loadedTx = txResult.data
                    if (loadedTx != null) {
                        isDraft = false
                        var tplId = loadedTx.extTemplateId
                        var tplChanged = false
                        val tplListResult = apiClient.extTemplateRepository.getTemplatesByOwnerType("TransactionSv")
                        if (tplListResult.isSuccess) {
                            availableTemplates = tplListResult.getOrNull() ?: emptyList()
                        }
                        if (tplId == null) {
                            val defaultTpl = availableTemplates.find { it.isDefault } ?: availableTemplates.firstOrNull()
                            if (defaultTpl != null) {
                                extTemplate = defaultTpl
                                tplId = defaultTpl.id?.toInt()
                                loadedTx = loadedTx.copy(extTemplateId = tplId)
                                tplChanged = true
                            }
                        } else {
                            val tplResult = apiClient.extTemplateRepository.getTemplateById(tplId)
                            if (tplResult.isSuccess) {
                                extTemplate = tplResult.getOrNull()
                            } else {
                                extTemplate = availableTemplates.find { it.id == tplId }
                            }
                        }

                        val extAttrs = extTemplate?.attributes ?: emptyList()
                        val currentAttrsMap = loadedTx.attributes.associateBy { it.name }
                        val syncedAttrs = extAttrs.map { ext ->
                            val existing = currentAttrsMap[ext.name]
                            existing ?: AttributeDTO(
                                name = ext.name,
                                attributeType = ext.attributeType,
                                validate = ext.validateRule,
                                value = when (ext.name) {
                                    "Dokumento numeris" -> loadedTx.documentNumber
                                    "Dokumento data" -> loadedTx.documentDate.toString()
                                    else -> ext.defaultValue ?: ""
                                },
                                validations = ext.validations,
                                tag = ext.tag
                            )
                        }

                        val templateAllowedNames = extTemplate?.attributes?.map { it.name }?.toSet()
                        val validAttrs = if (templateAllowedNames != null) {
                            syncedAttrs.filter { it.name in templateAllowedNames }
                        } else {
                            syncedAttrs
                        }

                        loadedTx = loadedTx.copy(attributes = validAttrs)
                        if (tplChanged) {
                            val updateResult = apiClient.transactionRepository.update(loadedTx)
                            if (updateResult is ApiResult.Success) {
                                transaction = updateResult.data
                            } else {
                                transaction = loadedTx
                            }
                        } else {
                            transaction = loadedTx
                        }
                    }
                }
                is ApiResult.Error -> {
                    errorMessage = txResult.message ?: "Nepavyko užkrauti užduoties detalių."
                }
            }
            isLoading = false
        }
    }

    fun updateStatus(newStatus: String) {
        val currentTx = transaction ?: return
        if (currentTx.status == newStatus) return
        
        val updatedTx = currentTx.copy(status = newStatus)
        saveTransaction(updatedTx)
    }

    fun changeTemplate(newTemplate: ExtTemplateDTO) {
        extTemplate = newTemplate
        val currentTx = transaction ?: return
        val extAttrs = newTemplate.attributes
        val currentAttrsMap = currentTx.attributes.associateBy { it.name }
        val syncedAttrs = extAttrs.map { ext ->
            val existing = currentAttrsMap[ext.name]
            existing ?: AttributeDTO(
                name = ext.name,
                attributeType = ext.attributeType,
                validate = ext.validateRule,
                value = when (ext.name) {
                    "Dokumento numeris" -> currentTx.documentNumber
                    "Dokumento data" -> currentTx.documentDate.toString()
                    else -> ext.defaultValue ?: ""
                },
                validations = ext.validations,
                tag = ext.tag
            )
        }

        val updatedTx = currentTx.copy(
            extTemplateId = newTemplate.id?.toInt(),
            attributes = syncedAttrs
        )
        if (isDraft) {
            transaction = updatedTx
        } else {
            saveTransaction(updatedTx)
        }
    }

    fun updateDocumentType(newType: String) {
        val currentTx = transaction ?: return
        if (currentTx.documentType == newType) return
        val updatedTx = currentTx.copy(documentType = newType)
        if (isDraft) {
            transaction = updatedTx
        } else {
            saveTransaction(updatedTx)
        }
    }

    fun updateDocumentNumber(newNumber: String) {
        val currentTx = transaction ?: return
        if (currentTx.documentNumber == newNumber) return
        transaction = currentTx.copy(documentNumber = newNumber)
    }

    fun updateDocumentDate(newDate: Long) {
        val currentTx = transaction ?: return
        if (currentTx.documentDate == newDate) return
        transaction = currentTx.copy(documentDate = newDate)
    }

    fun updateAttribute(name: String, attributeType: String, value: String) {
        val currentTx = transaction ?: return
        val currentAttrs = currentTx.attributes.toMutableList()
        val existingIndex = currentAttrs.indexOfFirst { it.name == name }
        
        if (existingIndex >= 0) {
            currentAttrs[existingIndex] = currentAttrs[existingIndex].copy(value = value)
        } else {
            currentAttrs.add(AttributeDTO(name = name, attributeType = attributeType, value = value))
        }
        transaction = currentTx.copy(attributes = currentAttrs)
    }

    fun removeAttribute(name: String) {
        val currentTx = transaction ?: return
        val currentAttrs = currentTx.attributes.filterNot { it.name == name }
        transaction = currentTx.copy(attributes = currentAttrs)
    }

    fun addDetail(detail: TransactionDetailDTO) {
        val currentTx = transaction ?: return
        val updatedDetails = currentTx.details + detail
        saveTransaction(currentTx.copy(details = updatedDetails))
    }

    fun removeDetail(sequenceId: Int) {
        val currentTx = transaction ?: return
        val updatedDetails = currentTx.details.filterNot { it.sequenceId == sequenceId }
        saveTransaction(currentTx.copy(details = updatedDetails))
    }

    private var saveJob: Job? = null

    fun saveTransaction(updatedTx: TransactionDTO) {
        val syncedAttrs = updatedTx.attributes.map { attr ->
            when (attr.name) {
                "Dokumento numeris" -> attr.copy(value = updatedTx.documentNumber)
                "Dokumento data" -> attr.copy(value = updatedTx.documentDate.toString())
                else -> attr
            }
        }
        val templateAllowedNames = extTemplate?.attributes?.map { it.name }?.toSet()
        val validAttrs = if (templateAllowedNames != null) {
            syncedAttrs.filter { it.name in templateAllowedNames }
        } else {
            syncedAttrs
        }
        val finalTx = updatedTx.copy(attributes = validAttrs)

        transaction = finalTx
        isLoading = true
        errorMessage = null

        val previousJob = saveJob
        saveJob = scope.launch {
            previousJob?.join()

            val txToSave = (transaction ?: finalTx).let { latest ->
                latest.copy(
                    documentNumber = finalTx.documentNumber,
                    documentDate = finalTx.documentDate,
                    status = finalTx.status,
                    attributes = validAttrs,
                    details = finalTx.details
                )
            }

            if (isDraft) {
                when (val createResult = apiClient.transactionRepository.create(txToSave)) {
                    is ApiResult.Success -> {
                        transaction = createResult.data ?: txToSave
                        isDraft = false
                        val groupCat = getGroupForDocumentType(txToSave.documentType)
                        AppSettings.incrementNextDocumentNumber(groupCat, txToSave.documentNumber)
                    }
                    is ApiResult.Error -> {
                        errorMessage = createResult.message ?: "Nepavyko sukurti užduoties."
                    }
                }
            } else {
                when (val updateResult = apiClient.transactionRepository.update(txToSave)) {
                    is ApiResult.Success -> {
                        transaction = updateResult.data ?: txToSave
                    }
                    is ApiResult.Error -> {
                        val msg = updateResult.message ?: ""
                        if (msg.contains("Optimistic Lock", ignoreCase = true)) {
                            val freshRes = apiClient.transactionRepository.getByTransactionId(txToSave.transactionId)
                            if (freshRes is ApiResult.Success && freshRes.data != null) {
                                val freshTx = freshRes.data!!.copy(
                                    documentNumber = txToSave.documentNumber,
                                    documentDate = txToSave.documentDate,
                                    status = txToSave.status,
                                    attributes = txToSave.attributes,
                                    details = txToSave.details
                                )
                                when (val retryRes = apiClient.transactionRepository.update(freshTx)) {
                                    is ApiResult.Success -> {
                                        transaction = retryRes.data ?: freshTx
                                    }
                                    is ApiResult.Error -> {
                                        errorMessage = retryRes.message ?: "Nepavyko atnaujinti užduoties."
                                    }
                                }
                            } else {
                                errorMessage = updateResult.message ?: "Nepavyko atnaujinti užduoties."
                            }
                        } else {
                            errorMessage = updateResult.message ?: "Nepavyko atnaujinti užduoties."
                        }
                    }
                }
            }
            isLoading = false
        }
    }

    fun deleteTransaction(onSuccess: () -> Unit) {
        val currentTx = transaction ?: return
        isLoading = true
        errorMessage = null
        scope.launch {
            when (val result = apiClient.transactionRepository.delete(currentTx.transactionId)) {
                is ApiResult.Success -> {
                    isLoading = false
                    onSuccess()
                }
                is ApiResult.Error -> {
                    errorMessage = result.message ?: "Nepavyko pašalinti užduoties."
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
