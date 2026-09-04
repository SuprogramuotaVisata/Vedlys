package com.suprogramuota_visata.vedlys

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.prefs.Preferences
import java.util.UUID
import com.suprogramuota_visata.vedlys.ui.theme.AppFontSize
import com.suprogramuota_visata.vedlys.utils.PlatformStorage

enum class AppLanguage(val code: String, val displayName: String) {
    LT("LT", "Lietuvių"),
    EN("EN", "English")
}

object AppSettings {
    private val prefs = Preferences.userNodeForPackage(AppSettings::class.java)
    
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("isDarkMode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _isNotificationSoundEnabled = MutableStateFlow(prefs.getBoolean("isNotificationSoundEnabled", true))
    val isNotificationSoundEnabled: StateFlow<Boolean> = _isNotificationSoundEnabled.asStateFlow()
    
    private val _selectedFontSize = MutableStateFlow(
        AppFontSize.valueOf(prefs.get("selectedFontSize", AppFontSize.Medium.name))
    )
    val selectedFontSize: StateFlow<AppFontSize> = _selectedFontSize.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(
        AppLanguage.valueOf(prefs.get("selectedLanguage", AppLanguage.LT.name))
    )
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _terminalId = MutableStateFlow(
        prefs.get("terminalId", null) ?: UUID.randomUUID().toString().take(8).also {
            prefs.put("terminalId", it)
        }
    )
    val terminalId: StateFlow<String> = _terminalId.asStateFlow()
    
    private val _readMessageIds = MutableStateFlow(
        prefs.get("readMessageIds", "")?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    )
    val readMessageIds: StateFlow<Set<String>> = _readMessageIds.asStateFlow()
    
    private val _paginationLimit = MutableStateFlow(prefs.getInt("paginationLimit", 50))
    val paginationLimit: StateFlow<Int> = _paginationLimit.asStateFlow()

    private val _allowCompletedDocumentEditing = MutableStateFlow(prefs.getBoolean("allowCompletedDocumentEditing", false))
    val allowCompletedDocumentEditing: StateFlow<Boolean> = _allowCompletedDocumentEditing.asStateFlow()

    private val _allowEditingOtherDevicesRecords = MutableStateFlow(prefs.getBoolean("allowEditingOtherDevicesRecords", false))
    val allowEditingOtherDevicesRecords: StateFlow<Boolean> = _allowEditingOtherDevicesRecords.asStateFlow()

    private val _allowEditingOtherUsersRecords = MutableStateFlow(prefs.getBoolean("allowEditingOtherUsersRecords", false))
    val allowEditingOtherUsersRecords: StateFlow<Boolean> = _allowEditingOtherUsersRecords.asStateFlow()
    
    fun setDarkMode(enabled: Boolean) {
        prefs.putBoolean("isDarkMode", enabled)
        _isDarkMode.value = enabled
    }

    fun setAllowCompletedDocumentEditing(enabled: Boolean) {
        prefs.putBoolean("allowCompletedDocumentEditing", enabled)
        _allowCompletedDocumentEditing.value = enabled
    }

    fun setAllowEditingOtherDevicesRecords(enabled: Boolean) {
        prefs.putBoolean("allowEditingOtherDevicesRecords", enabled)
        _allowEditingOtherDevicesRecords.value = enabled
    }

    fun setAllowEditingOtherUsersRecords(enabled: Boolean) {
        prefs.putBoolean("allowEditingOtherUsersRecords", enabled)
        _allowEditingOtherUsersRecords.value = enabled
    }

    fun checkRecordEditPermission(
        isCompleted: Boolean = false,
        recordCreatedOnDeviceId: String? = null,
        recordCreatedByUserId: Int? = null,
        currentTerminalId: String = terminalId.value,
        currentUserId: Int? = null,
        isAdmin: Boolean = false,
        language: AppLanguage = selectedLanguage.value
    ): String? {
        if (isCompleted) {
            if (!_allowCompletedDocumentEditing.value) {
                return if (language == AppLanguage.EN) "Editing or deleting completed (Baigta) documents is disabled in Settings." else "Užbaigtų (Baigta) dokumentų redagavimas ir šalinimas yra išjungtas Nustatymuose."
            }
            if (!isAdmin) {
                return if (language == AppLanguage.EN) "Only ADMIN users can edit or delete completed (Baigta) documents." else "Užbaigtus (Baigta) dokumentus redaguoti ar trinti gali tik ADMIN vartotojas."
            }
        }

        if (recordCreatedOnDeviceId != null && recordCreatedOnDeviceId.isNotBlank() && recordCreatedOnDeviceId != "0" && recordCreatedOnDeviceId != currentTerminalId) {
            if (!_allowEditingOtherDevicesRecords.value) {
                return if (language == AppLanguage.EN) "Editing or deleting records from other devices is disabled in Settings." else "Kitų įrenginių sukurtų įrašų redagavimas ir šalinimas yra išjungtas Nustatymuose."
            }
            if (!isAdmin) {
                return if (language == AppLanguage.EN) "Only ADMIN users can edit or delete records created on other devices." else "Kitų įrenginių sukurtus įrašus redaguoti ar trinti gali tik ADMIN vartotojas."
            }
        }

        if (recordCreatedByUserId != null && recordCreatedByUserId != 0 && (currentUserId == null || recordCreatedByUserId != currentUserId)) {
            if (!_allowEditingOtherUsersRecords.value && !isAdmin) {
                return if (language == AppLanguage.EN) "Only ADMIN users can edit or delete records created by other users." else "Kitų vartotojų sukurtus įrašus redaguoti ar trinti gali tik ADMIN vartotojas."
            }
        }

        return null
    }

    fun setNotificationSound(enabled: Boolean) {
        prefs.putBoolean("isNotificationSoundEnabled", enabled)
        _isNotificationSoundEnabled.value = enabled
    }

    fun setFontSize(size: AppFontSize) {
        prefs.put("selectedFontSize", size.name)
        _selectedFontSize.value = size
    }
    
    fun setLanguage(language: AppLanguage) {
        prefs.put("selectedLanguage", language.name)
        _selectedLanguage.value = language
    }
    
    fun setPaginationLimit(limit: Int) {
        prefs.putInt("paginationLimit", limit)
        _paginationLimit.value = limit
    }
    
    fun addReadMessageId(id: String) {
        val newSet = _readMessageIds.value.toMutableSet().apply { add(id) }
        prefs.put("readMessageIds", newSet.joinToString(","))
        _readMessageIds.value = newSet
    }
    
    fun addReadMessageIds(ids: List<String>) {
        val newSet = _readMessageIds.value.toMutableSet().apply { addAll(ids) }
        prefs.put("readMessageIds", newSet.joinToString(","))
        _readMessageIds.value = newSet
    }

    fun isImportAiEnabled(format: String): Boolean {
        val defaultValue = format.lowercase() == "pdf"
        return prefs.getBoolean("importAi_${format.lowercase()}", defaultValue)
    }

    fun setImportAiEnabled(format: String, enabled: Boolean) {
        prefs.putBoolean("importAi_${format.lowercase()}", enabled)
    }

    private val _isLicensingTermsAccepted = MutableStateFlow(prefs.getBoolean("isLicensingTermsAccepted", false))
    val isLicensingTermsAccepted: StateFlow<Boolean> = _isLicensingTermsAccepted.asStateFlow()

    fun setLicensingTermsAccepted(accepted: Boolean) {
        prefs.putBoolean("isLicensingTermsAccepted", accepted)
        _isLicensingTermsAccepted.value = accepted
    }

    private val _reqName = MutableStateFlow(prefs.get("reqName", ""))
    val reqName: StateFlow<String> = _reqName.asStateFlow()
    
    private val _reqCode = MutableStateFlow(prefs.get("reqCode", ""))
    val reqCode: StateFlow<String> = _reqCode.asStateFlow()
    
    private val _reqVatCode = MutableStateFlow(prefs.get("reqVatCode", ""))
    val reqVatCode: StateFlow<String> = _reqVatCode.asStateFlow()
    
    private val _reqAddress = MutableStateFlow(prefs.get("reqAddress", ""))
    val reqAddress: StateFlow<String> = _reqAddress.asStateFlow()
    
    private val _reqBankAccount = MutableStateFlow(prefs.get("reqBankAccount", ""))
    val reqBankAccount: StateFlow<String> = _reqBankAccount.asStateFlow()
    
    private val _reqBankSwift = MutableStateFlow(prefs.get("reqBankSwift", ""))
    val reqBankSwift: StateFlow<String> = _reqBankSwift.asStateFlow()
    
    fun setReqName(value: String) {
        prefs.put("reqName", value)
        _reqName.value = value
    }
    fun setReqCode(value: String) {
        prefs.put("reqCode", value)
        _reqCode.value = value
    }
    fun setReqVatCode(value: String) {
        prefs.put("reqVatCode", value)
        _reqVatCode.value = value
    }
    fun setReqAddress(value: String) {
        prefs.put("reqAddress", value)
        _reqAddress.value = value
    }
    fun setReqBankAccount(value: String) {
        prefs.put("reqBankAccount", value)
        _reqBankAccount.value = value
    }
    fun setReqBankSwift(value: String) {
        prefs.put("reqBankSwift", value)
        _reqBankSwift.value = value
    }

    suspend fun syncWithServer(apiClient: com.suprogramuota_visata.api.ApiSvClient) {
        try {
            var ownerTemplateId: Int? = null
            var addressTemplateId: Int? = null
            var bankTemplateId: Int? = null
            
            val templatesOwnerRes = apiClient.extTemplateRepository.getTemplatesByOwnerType("OwnerSv")
            if (templatesOwnerRes.isSuccess) {
                ownerTemplateId = templatesOwnerRes.getOrNull()?.firstOrNull { it.name == "Aplikacijos nustatymai" }?.id
            }
            val templatesAddressRes = apiClient.extTemplateRepository.getTemplatesByOwnerType("AddressSv")
            if (templatesAddressRes.isSuccess) {
                addressTemplateId = templatesAddressRes.getOrNull()?.firstOrNull { it.name == "Aplikacijos nustatymai" }?.id
            }
            val templatesBankRes = apiClient.extTemplateRepository.getTemplatesByOwnerType("BankSv")
            if (templatesBankRes.isSuccess) {
                bankTemplateId = templatesBankRes.getOrNull()?.firstOrNull { it.name == "Aplikacijos nustatymai" }?.id
            }

            val res = apiClient.typeRepository.getAllByType("SettingsSv")
            if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
                val settingsList = res.data
                val serverSetting = settingsList.firstOrNull { it.id == 1 } ?: settingsList.firstOrNull()
                
                if (serverSetting != null) {
                    serverSetting.notificationSound?.let { setNotificationSound(it) }
                    serverSetting.darkMode?.let { setDarkMode(it) }
                    serverSetting.selectedFontSize?.let {
                        try { setFontSize(AppFontSize.valueOf(it)) } catch (e: Exception) {}
                    }
                    serverSetting.selectedLanguage?.let {
                        try { setLanguage(AppLanguage.valueOf(it)) } catch (e: Exception) {}
                    }
                    serverSetting.paginationLimit?.let { setPaginationLimit(it) }
                    
                    val owner = serverSetting.owner
                    if (owner != null) {
                        val nameAttr = owner.attributes.firstOrNull { it.name == "Įmonės pavadinimas / Vardas, pavardė" }?.value
                        setReqName(nameAttr ?: owner.name)
                        
                        val codeAttr = owner.attributes.firstOrNull { it.name == "Įmonės / asmens kodas" }?.value
                        setReqCode(codeAttr ?: owner.code ?: "")
                        
                        val vatAttr = owner.attributes.firstOrNull { it.name == "PVM mokėtojo kodas" }?.value
                        setReqVatCode(vatAttr ?: owner.vatCode ?: "")
                        
                        val address = owner.address
                        if (address != null) {
                            val addrAttr = address.attributes.firstOrNull { it.name == "Adresas" }?.value
                            setReqAddress(addrAttr ?: "")
                        } else {
                            setReqAddress("")
                        }
                        
                        val bank = owner.bank
                        if (bank != null) {
                            val accAttr = bank.attributes.firstOrNull { it.name == "Banko sąskaita" }?.value
                            setReqBankAccount(accAttr ?: bank.bankAccount ?: "")
                            
                            val swiftAttr = bank.attributes.firstOrNull { it.name == "Bankas / SWIFT" }?.value
                            setReqBankSwift(swiftAttr ?: bank.bankSwift ?: "")
                        } else {
                            setReqBankAccount("")
                            setReqBankSwift("")
                        }
                    }
                } else {
                    val defaultSetting = com.suprogramuota_visata.api.domain.models.TypeDTO(
                        id = 1,
                        name = "Global Settings",
                        enabled = true,
                        type = "SettingsSv",
                        notificationSound = isNotificationSoundEnabled.value,
                        darkMode = isDarkMode.value,
                        selectedFontSize = selectedFontSize.value.name,
                        selectedLanguage = selectedLanguage.value.name,
                        paginationLimit = paginationLimit.value,
                        terminalId = terminalId.value,
                        owner = com.suprogramuota_visata.api.domain.models.TypeDTO(
                            id = 1,
                            name = reqName.value.ifBlank { "Owner" },
                            enabled = true,
                            type = "OwnerSv",
                            extTemplateId = ownerTemplateId,
                            code = reqCode.value,
                            vatCode = reqVatCode.value,
                            attributes = listOf(
                                com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                    name = "Įmonės pavadinimas / Vardas, pavardė",
                                    attributeType = "STRING",
                                    value = reqName.value
                                ),
                                com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                    name = "Įmonės / asmens kodas",
                                    attributeType = "STRING",
                                    value = reqCode.value
                                ),
                                com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                    name = "PVM mokėtojo kodas",
                                    attributeType = "STRING",
                                    value = reqVatCode.value
                                ),
                                com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                    name = "Adresas",
                                    attributeType = "Local_Type",
                                    value = "1 - Aplikacijos nustatymai"
                                ),
                                com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                    name = "Bankas",
                                    attributeType = "Local_Type",
                                    value = "1 - Aplikacijos nustatymai"
                                )
                            ),
                            address = com.suprogramuota_visata.api.domain.models.TypeDTO(
                                id = 1,
                                name = "Address",
                                enabled = true,
                                type = "AddressSv",
                                extTemplateId = addressTemplateId,
                                attributes = listOf(
                                    com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                        name = "Adresas",
                                        attributeType = "STRING",
                                        value = reqAddress.value
                                    )
                                )
                            ),
                            bank = com.suprogramuota_visata.api.domain.models.TypeDTO(
                                id = 1,
                                name = "Bank",
                                enabled = true,
                                type = "BankSv",
                                extTemplateId = bankTemplateId,
                                bankAccount = reqBankAccount.value,
                                bankSwift = reqBankSwift.value,
                                attributes = listOf(
                                    com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                        name = "Banko sąskaita",
                                        attributeType = "STRING",
                                        value = reqBankAccount.value
                                    ),
                                    com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                        name = "Bankas / SWIFT",
                                        attributeType = "STRING",
                                        value = reqBankSwift.value
                                    )
                                )
                            )
                        )
                    )
                    apiClient.typeRepository.create(defaultSetting)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveToServer(apiClient: com.suprogramuota_visata.api.ApiSvClient) {
        try {
            var ownerTemplateId: Int? = null
            var addressTemplateId: Int? = null
            var bankTemplateId: Int? = null
            
            val templatesOwnerRes = apiClient.extTemplateRepository.getTemplatesByOwnerType("OwnerSv")
            if (templatesOwnerRes.isSuccess) {
                ownerTemplateId = templatesOwnerRes.getOrNull()?.firstOrNull { it.name == "Aplikacijos nustatymai" }?.id
            }
            val templatesAddressRes = apiClient.extTemplateRepository.getTemplatesByOwnerType("AddressSv")
            if (templatesAddressRes.isSuccess) {
                addressTemplateId = templatesAddressRes.getOrNull()?.firstOrNull { it.name == "Aplikacijos nustatymai" }?.id
            }
            val templatesBankRes = apiClient.extTemplateRepository.getTemplatesByOwnerType("BankSv")
            if (templatesBankRes.isSuccess) {
                bankTemplateId = templatesBankRes.getOrNull()?.firstOrNull { it.name == "Aplikacijos nustatymai" }?.id
            }

            val res = apiClient.typeRepository.getAllByType("SettingsSv")
            var settingsId = 1
            var ownerId = 1
            var addressId = 1
            var bankId = 1
            
            if (res is com.suprogramuota_visata.api.domain.util.ApiResult.Success) {
                val serverSetting = res.data.firstOrNull { it.id == 1 } ?: res.data.firstOrNull()
                if (serverSetting != null) {
                    settingsId = serverSetting.id ?: 1
                    serverSetting.owner?.let { owner ->
                        ownerId = owner.id ?: 1
                        owner.address?.let { addr -> addressId = addr.id ?: 1 }
                        owner.bank?.let { b -> bankId = b.id ?: 1 }
                    }
                }
            }

            val updated = com.suprogramuota_visata.api.domain.models.TypeDTO(
                id = settingsId,
                name = "Global Settings",
                enabled = true,
                type = "SettingsSv",
                notificationSound = isNotificationSoundEnabled.value,
                darkMode = isDarkMode.value,
                selectedFontSize = selectedFontSize.value.name,
                selectedLanguage = selectedLanguage.value.name,
                paginationLimit = paginationLimit.value,
                terminalId = terminalId.value,
                owner = com.suprogramuota_visata.api.domain.models.TypeDTO(
                    id = ownerId,
                    name = reqName.value.ifBlank { "Owner" },
                    enabled = true,
                    type = "OwnerSv",
                    extTemplateId = ownerTemplateId,
                    code = reqCode.value,
                    vatCode = reqVatCode.value,
                    attributes = listOf(
                        com.suprogramuota_visata.api.domain.models.AttributeDTO(
                            name = "Įmonės pavadinimas / Vardas, pavardė",
                            attributeType = "STRING",
                            value = reqName.value
                        ),
                        com.suprogramuota_visata.api.domain.models.AttributeDTO(
                            name = "Įmonės / asmens kodas",
                            attributeType = "STRING",
                            value = reqCode.value
                        ),
                        com.suprogramuota_visata.api.domain.models.AttributeDTO(
                            name = "PVM mokėtojo kodas",
                            attributeType = "STRING",
                            value = reqVatCode.value
                        ),
                        com.suprogramuota_visata.api.domain.models.AttributeDTO(
                            name = "Adresas",
                            attributeType = "Local_Type",
                            value = "$addressId - Aplikacijos nustatymai"
                        ),
                        com.suprogramuota_visata.api.domain.models.AttributeDTO(
                            name = "Bankas",
                            attributeType = "Local_Type",
                            value = "$bankId - Aplikacijos nustatymai"
                        )
                    ),
                    address = com.suprogramuota_visata.api.domain.models.TypeDTO(
                        id = addressId,
                        name = "Address",
                        enabled = true,
                        type = "AddressSv",
                        extTemplateId = addressTemplateId,
                        attributes = listOf(
                            com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                name = "Adresas",
                                attributeType = "STRING",
                                value = reqAddress.value
                            )
                        )
                    ),
                    bank = com.suprogramuota_visata.api.domain.models.TypeDTO(
                        id = bankId,
                        name = "Bank",
                        enabled = true,
                        type = "BankSv",
                        extTemplateId = bankTemplateId,
                        bankAccount = reqBankAccount.value,
                        bankSwift = reqBankSwift.value,
                        attributes = listOf(
                            com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                name = "Banko sąskaita",
                                attributeType = "STRING",
                                value = reqBankAccount.value
                            ),
                            com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                name = "Bankas / SWIFT",
                                attributeType = "STRING",
                                value = reqBankSwift.value
                            )
                        )
                    )
                )
            )
            apiClient.typeRepository.update(updated)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _docSeriesPrefixFinancial = MutableStateFlow(prefs.get("docSeriesPrefix_Financial", "FIN-"))
    val docSeriesPrefixFinancial: StateFlow<String> = _docSeriesPrefixFinancial.asStateFlow()

    private val _docSeriesNextNumberFinancial = MutableStateFlow(prefs.getInt("docSeriesNextNumber_Financial", 1))
    val docSeriesNextNumberFinancial: StateFlow<Int> = _docSeriesNextNumberFinancial.asStateFlow()

    private val _docSeriesPrefixOperational = MutableStateFlow(prefs.get("docSeriesPrefix_Operational", "OPR-"))
    val docSeriesPrefixOperational: StateFlow<String> = _docSeriesPrefixOperational.asStateFlow()

    private val _docSeriesNextNumberOperational = MutableStateFlow(prefs.getInt("docSeriesNextNumber_Operational", 1))
    val docSeriesNextNumberOperational: StateFlow<Int> = _docSeriesNextNumberOperational.asStateFlow()

    private val _docSeriesPrefixDelivery = MutableStateFlow(prefs.get("docSeriesPrefix_Delivery", "DEL-"))
    val docSeriesPrefixDelivery: StateFlow<String> = _docSeriesPrefixDelivery.asStateFlow()

    private val _docSeriesNextNumberDelivery = MutableStateFlow(prefs.getInt("docSeriesNextNumber_Delivery", 1))
    val docSeriesNextNumberDelivery: StateFlow<Int> = _docSeriesNextNumberDelivery.asStateFlow()

    private val _docSeriesPrefixCRM = MutableStateFlow(prefs.get("docSeriesPrefix_CRM", "CRM-"))
    val docSeriesPrefixCRM: StateFlow<String> = _docSeriesPrefixCRM.asStateFlow()

    private val _docSeriesNextNumberCRM = MutableStateFlow(prefs.getInt("docSeriesNextNumber_CRM", 1))
    val docSeriesNextNumberCRM: StateFlow<Int> = _docSeriesNextNumberCRM.asStateFlow()

    fun setDocSeriesPrefix(group: String, value: String) {
        val cleanGroup = group.trim()
        prefs.put("docSeriesPrefix_$cleanGroup", value)
        when (cleanGroup) {
            "Financial" -> _docSeriesPrefixFinancial.value = value
            "Operational" -> _docSeriesPrefixOperational.value = value
            "Delivery" -> _docSeriesPrefixDelivery.value = value
            "CRM" -> _docSeriesPrefixCRM.value = value
        }
    }

    fun setDocSeriesNextNumber(group: String, value: Int) {
        val cleanGroup = group.trim()
        prefs.putInt("docSeriesNextNumber_$cleanGroup", value)
        when (cleanGroup) {
            "Financial" -> _docSeriesNextNumberFinancial.value = value
            "Operational" -> _docSeriesNextNumberOperational.value = value
            "Delivery" -> _docSeriesNextNumberDelivery.value = value
            "CRM" -> _docSeriesNextNumberCRM.value = value
        }
    }

    fun generateNextDocumentNumber(group: String): String {
        val cleanGroup = group.trim()
        val prefix = when (cleanGroup) {
            "Financial" -> _docSeriesPrefixFinancial.value
            "Operational" -> _docSeriesPrefixOperational.value
            "Delivery" -> _docSeriesPrefixDelivery.value
            "CRM" -> _docSeriesPrefixCRM.value
            else -> "TX-"
        }
        val nextNum = when (cleanGroup) {
            "Financial" -> _docSeriesNextNumberFinancial.value
            "Operational" -> _docSeriesNextNumberOperational.value
            "Delivery" -> _docSeriesNextNumberDelivery.value
            "CRM" -> _docSeriesNextNumberCRM.value
            else -> 1
        }
        return "$prefix${String.format("%05d", nextNum)}"
    }

    fun incrementNextDocumentNumber(group: String, usedNumber: String) {
        val cleanGroup = group.trim()
        val prefix = when (cleanGroup) {
            "Financial" -> _docSeriesPrefixFinancial.value
            "Operational" -> _docSeriesPrefixOperational.value
            "Delivery" -> _docSeriesPrefixDelivery.value
            "CRM" -> _docSeriesPrefixCRM.value
            else -> "TX-"
        }
        val nextNum = when (cleanGroup) {
            "Financial" -> _docSeriesNextNumberFinancial.value
            "Operational" -> _docSeriesNextNumberOperational.value
            "Delivery" -> _docSeriesNextNumberDelivery.value
            "CRM" -> _docSeriesNextNumberCRM.value
            else -> 1
        }
        val expected = "$prefix${String.format("%05d", nextNum)}"
        if (usedNumber == expected) {
            setDocSeriesNextNumber(cleanGroup, nextNum + 1)
        }
    }

    private val localPropertiesFile: java.io.File by lazy {
        val appDataDir = PlatformStorage.getAppDataDir()
        java.io.File(appDataDir, "local.properties")
    }

    fun getApiHost(): String {
        val props = java.util.Properties()
        if (localPropertiesFile.exists()) {
            try { localPropertiesFile.inputStream().use { props.load(it) } } catch (e: Exception) {}
        }
        return props.getProperty("API_HOST", "127.0.0.1")
    }

    fun getApiPort(): Int {
        val props = java.util.Properties()
        if (localPropertiesFile.exists()) {
            try { localPropertiesFile.inputStream().use { props.load(it) } } catch (e: Exception) {}
        }
        return props.getProperty("API_PORT", "8081").toIntOrNull() ?: 8081
    }

    fun getUseHttps(): Boolean {
        val props = java.util.Properties()
        if (localPropertiesFile.exists()) {
            try { localPropertiesFile.inputStream().use { props.load(it) } } catch (e: Exception) {}
        }
        return props.getProperty("USE_HTTPS", "true").toBoolean()
    }

    fun saveServerConfig(host: String, port: Int, useHttps: Boolean) {
        val props = java.util.Properties()
        if (localPropertiesFile.exists()) {
            try { localPropertiesFile.inputStream().use { props.load(it) } } catch (e: Exception) {}
        }
        props.setProperty("API_HOST", host.ifBlank { "127.0.0.1" })
        props.setProperty("API_PORT", port.toString())
        props.setProperty("USE_HTTPS", useHttps.toString())
        try {
            localPropertiesFile.outputStream().use { props.store(it, "Vedlys local server config") }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

