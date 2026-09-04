package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import com.suprogramuota_visata.vedlys.ui.theme.AppFontSize
import com.suprogramuota_visata.vedlys.AppLanguage
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme
import androidx.compose.runtime.*
import com.suprogramuota_visata.vedlys.utils.validateFieldValue
import com.suprogramuota_visata.api.domain.models.ExtTemplateDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
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
import androidx.compose.material.icons.filled.Check
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

fun getSettingsString(key: String, language: AppLanguage): String {
    return com.suprogramuota_visata.vedlys.utils.Localization.getString("settings", key, language)
}

@Composable
fun SettingsScreen(
    apiClient: com.suprogramuota_visata.api.ApiSvClient,
    isAdmin: Boolean = false,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var saveJob by remember { mutableStateOf<Job?>(null) }

    var ownerTemplate by remember { mutableStateOf<ExtTemplateDTO?>(null) }
    var bankTemplate by remember { mutableStateOf<ExtTemplateDTO?>(null) }

    LaunchedEffect(Unit) {
        AppSettings.syncWithServer(apiClient)
        
        val ownerRes = apiClient.extTemplateRepository.getTemplatesByOwnerType("OwnerSv")
        if (ownerRes.isSuccess) {
            ownerTemplate = ownerRes.getOrNull()?.find { it.isDefault } ?: ownerRes.getOrNull()?.firstOrNull()
        }
        val bankRes = apiClient.extTemplateRepository.getTemplatesByOwnerType("BankSv")
        if (bankRes.isSuccess) {
            bankTemplate = bankRes.getOrNull()?.find { it.isDefault } ?: bankRes.getOrNull()?.firstOrNull()
        }
    }

    val isDarkMode by AppSettings.isDarkMode.collectAsState()
    val isNotificationSoundEnabled by AppSettings.isNotificationSoundEnabled.collectAsState()
    val selectedFontSize by AppSettings.selectedFontSize.collectAsState()
    val terminalId by AppSettings.terminalId.collectAsState()
    val paginationLimit by AppSettings.paginationLimit.collectAsState()
    val allowCompletedDocumentEditing by AppSettings.allowCompletedDocumentEditing.collectAsState()
    val allowEditingOtherDevicesRecords by AppSettings.allowEditingOtherDevicesRecords.collectAsState()
    val allowEditingOtherUsersRecords by AppSettings.allowEditingOtherUsersRecords.collectAsState()
    val language by AppSettings.selectedLanguage.collectAsState()

    var errorName by remember { mutableStateOf<String?>(null) }
    var errorCode by remember { mutableStateOf<String?>(null) }
    var errorVatCode by remember { mutableStateOf<String?>(null) }
    var errorAddress by remember { mutableStateOf<String?>(null) }
    var errorBankAccount by remember { mutableStateOf<String?>(null) }
    var errorBankSwift by remember { mutableStateOf<String?>(null) }

    val focusRequesterName = remember { FocusRequester() }
    val focusRequesterCode = remember { FocusRequester() }
    val focusRequesterVatCode = remember { FocusRequester() }
    val focusRequesterAddress = remember { FocusRequester() }
    val focusRequesterBankAccount = remember { FocusRequester() }
    val focusRequesterBankSwift = remember { FocusRequester() }

    fun validateFieldWithTemplate(fieldName: String, value: String, template: ExtTemplateDTO?, staticCheck: () -> String?): String? {
        val extAttr = template?.attributes?.find { it.name == fieldName }
        if (extAttr == null) {
            return staticCheck()
        }
        return validateFieldValue(fieldName, value, extAttr, language)
    }

    fun validateName(value: String): String? {
        return validateFieldWithTemplate("Įmonės pavadinimas / Vardas, pavardė", value, ownerTemplate) {
            if (value.isBlank()) {
                if (language == AppLanguage.EN) "Field cannot be empty" else "Laukas negali būti tuščias"
            } else if (value.length < 3 || value.length > 256) {
                if (language == AppLanguage.EN) "Length must be between 3 and 256" else "Ilgis turi būti nuo 3 iki 256"
            } else null
        }
    }

    fun validateCode(value: String): String? {
        return validateFieldWithTemplate("Įmonės / asmens kodas", value, ownerTemplate) {
            if (value.isBlank()) {
                if (language == AppLanguage.EN) "Field cannot be empty" else "Laukas negali būti tuščias"
            } else if (value.length < 3 || value.length > 11) {
                if (language == AppLanguage.EN) "Length must be between 3 and 11" else "Ilgis turi būti nuo 3 iki 11"
            } else null
        }
    }

    fun validateVatCode(value: String): String? {
        return validateFieldWithTemplate("PVM mokėtojo kodas", value, ownerTemplate) { null }
    }

    fun validateAddress(value: String): String? {
        return validateFieldWithTemplate("Adresas", value, ownerTemplate) { null }
    }

    fun validateBankAccount(value: String): String? {
        return validateFieldWithTemplate("Banko sąskaita", value, bankTemplate) {
            if (value.isBlank()) {
                if (language == AppLanguage.EN) "Field cannot be empty" else "Laukas negali būti tuščias"
            } else {
                val clean = value.replace("\\s".toRegex(), "").uppercase()
                if (clean.length < 15 || clean.length > 34) {
                    if (language == AppLanguage.EN) "Invalid IBAN length (15 to 34 characters)" else "Neteisingas IBAN ilgis (turi būti 15-34)"
                } else if (!clean.matches(Regex("^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$"))) {
                    if (language == AppLanguage.EN) "Invalid IBAN format" else "Neteisingas IBAN formatas"
                } else {
                    val rearranged = clean.substring(4) + clean.substring(0, 4)
                    var remainder = 0
                    for (char in rearranged) {
                        val digitVal = if (char.isDigit()) char - '0' else char - 'A' + 10
                        remainder = (remainder * (if (digitVal < 10) 10 else 100) + digitVal) % 97
                    }
                    if (remainder != 1) {
                        if (language == AppLanguage.EN) "Invalid IBAN checksum" else "Neteisinga IBAN kontrolinė suma"
                    } else null
                }
            }
        }
    }

    fun validateBankSwift(value: String): String? {
        return validateFieldWithTemplate("Bankas / SWIFT", value, bankTemplate) {
            if (value.isBlank()) {
                if (language == AppLanguage.EN) "Field cannot be empty" else "Laukas negali būti tuščias"
            } else {
                val clean = value.replace("\\s".toRegex(), "").uppercase()
                if (!clean.matches(Regex("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$"))) {
                    if (language == AppLanguage.EN) "Invalid SWIFT/BIC format" else "Neteisingas SWIFT/BIC formatas"
                } else null
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val validationSuccesses = remember { mutableStateListOf<String>() }
    val dirtyFields = remember { mutableStateListOf<String>() }
    var ignoreFocusLoss by remember { mutableStateOf(false) }

    fun checkName(value: String): Boolean {
        val err = validateName(value)
        errorName = err
        if (err != null) {
            validationSuccesses.remove("name")
            return false
        } else {
            if (value.isNotBlank()) {
                if (!validationSuccesses.contains("name")) validationSuccesses.add("name")
            } else {
                validationSuccesses.remove("name")
            }
            return true
        }
    }

    fun checkCode(value: String): Boolean {
        val err = validateCode(value)
        errorCode = err
        if (err != null) {
            validationSuccesses.remove("code")
            return false
        } else {
            if (value.isNotBlank()) {
                if (!validationSuccesses.contains("code")) validationSuccesses.add("code")
            } else {
                validationSuccesses.remove("code")
            }
            return true
        }
    }

    fun checkVatCode(value: String): Boolean {
        val err = validateVatCode(value)
        errorVatCode = err
        if (err != null) {
            validationSuccesses.remove("vatCode")
            return false
        } else {
            if (value.isNotBlank()) {
                if (!validationSuccesses.contains("vatCode")) validationSuccesses.add("vatCode")
            } else {
                validationSuccesses.remove("vatCode")
            }
            return true
        }
    }

    fun checkAddress(value: String): Boolean {
        val err = validateAddress(value)
        errorAddress = err
        if (err != null) {
            validationSuccesses.remove("address")
            return false
        } else {
            if (value.isNotBlank()) {
                if (!validationSuccesses.contains("address")) validationSuccesses.add("address")
            } else {
                validationSuccesses.remove("address")
            }
            return true
        }
    }

    fun checkBankAccount(value: String): Boolean {
        val err = validateBankAccount(value)
        errorBankAccount = err
        if (err != null) {
            validationSuccesses.remove("bankAccount")
            return false
        } else {
            if (value.isNotBlank()) {
                if (!validationSuccesses.contains("bankAccount")) validationSuccesses.add("bankAccount")
            } else {
                validationSuccesses.remove("bankAccount")
            }
            return true
        }
    }

    fun checkBankSwift(value: String): Boolean {
        val err = validateBankSwift(value)
        errorBankSwift = err
        if (err != null) {
            validationSuccesses.remove("bankSwift")
            return false
        } else {
            if (value.isNotBlank()) {
                if (!validationSuccesses.contains("bankSwift")) validationSuccesses.add("bankSwift")
            } else {
                validationSuccesses.remove("bankSwift")
            }
            return true
        }
    }

    val validationModifier = { fieldId: String, requester: FocusRequester, nextRequester: FocusRequester?, validateFn: () -> Boolean ->
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
                    val hasError = when(fieldId) {
                        "name" -> errorName != null
                        "code" -> errorCode != null
                        "vatCode" -> errorVatCode != null
                        "address" -> errorAddress != null
                        "bankAccount" -> errorBankAccount != null
                        "bankSwift" -> errorBankSwift != null
                        else -> false
                    }
                    if (dirtyFields.contains(fieldId) || hasError) {
                        val isValid = validateFn()
                        if (!isValid) {
                            coroutineScope.launch {
                                delay(50)
                                requester.requestFocus()
                            }
                        }
                    }
                }
            }
    }

    fun saveSettings() {
        saveJob?.cancel()
        saveJob = coroutineScope.launch {
            delay(500)
            val reqNameVal = AppSettings.reqName.value
            val reqCodeVal = AppSettings.reqCode.value
            val reqVatCodeVal = AppSettings.reqVatCode.value
            val reqAddressVal = AppSettings.reqAddress.value
            val reqBankAccountVal = AppSettings.reqBankAccount.value
            val reqBankSwiftVal = AppSettings.reqBankSwift.value

            if (validateName(reqNameVal) == null &&
                validateCode(reqCodeVal) == null &&
                validateVatCode(reqVatCodeVal) == null &&
                validateAddress(reqAddressVal) == null &&
                validateBankAccount(reqBankAccountVal) == null &&
                validateBankSwift(reqBankSwiftVal) == null
            ) {
                AppSettings.saveToServer(apiClient)
            }
        }
    }
    
    // Requisites
    val reqName by AppSettings.reqName.collectAsState()
    val reqCode by AppSettings.reqCode.collectAsState()
    val reqVatCode by AppSettings.reqVatCode.collectAsState()
    val reqAddress by AppSettings.reqAddress.collectAsState()
    val reqBankAccount by AppSettings.reqBankAccount.collectAsState()
    val reqBankSwift by AppSettings.reqBankSwift.collectAsState()

    // Collapse states
    var isNotificationsExpanded by remember { mutableStateOf(true) }
    var isAppearanceExpanded by remember { mutableStateOf(true) }
    var isImportSettingsExpanded by remember { mutableStateOf(true) }
    var isRequisitesExpanded by remember { mutableStateOf(true) }
    var isInfoExpanded by remember { mutableStateOf(true) }
    var isNumberingExpanded by remember { mutableStateOf(true) }

    val docSeriesPrefixFinancial by AppSettings.docSeriesPrefixFinancial.collectAsState()
    val docSeriesNextNumberFinancial by AppSettings.docSeriesNextNumberFinancial.collectAsState()
    val docSeriesPrefixOperational by AppSettings.docSeriesPrefixOperational.collectAsState()
    val docSeriesNextNumberOperational by AppSettings.docSeriesNextNumberOperational.collectAsState()
    val docSeriesPrefixDelivery by AppSettings.docSeriesPrefixDelivery.collectAsState()
    val docSeriesNextNumberDelivery by AppSettings.docSeriesNextNumberDelivery.collectAsState()
    val docSeriesPrefixCRM by AppSettings.docSeriesPrefixCRM.collectAsState()
    val docSeriesNextNumberCRM by AppSettings.docSeriesNextNumberCRM.collectAsState()

    var errorPrefixFinancial by remember { mutableStateOf<String?>(null) }
    var errorNextNumberFinancial by remember { mutableStateOf<String?>(null) }
    var errorPrefixOperational by remember { mutableStateOf<String?>(null) }
    var errorNextNumberOperational by remember { mutableStateOf<String?>(null) }
    var errorPrefixDelivery by remember { mutableStateOf<String?>(null) }
    var errorNextNumberDelivery by remember { mutableStateOf<String?>(null) }
    var errorPrefixCRM by remember { mutableStateOf<String?>(null) }
    var errorNextNumberCRM by remember { mutableStateOf<String?>(null) }

    val focusRequesterPrefixFinancial = remember { FocusRequester() }
    val focusRequesterNextNumberFinancial = remember { FocusRequester() }
    val focusRequesterPrefixOperational = remember { FocusRequester() }
    val focusRequesterNextNumberOperational = remember { FocusRequester() }
    val focusRequesterPrefixDelivery = remember { FocusRequester() }
    val focusRequesterNextNumberDelivery = remember { FocusRequester() }
    val focusRequesterPrefixCRM = remember { FocusRequester() }
    val focusRequesterNextNumberCRM = remember { FocusRequester() }

    fun validatePrefix(value: String): String? {
        return if (value.length > 20) {
            if (language == AppLanguage.EN) "Too long (max 20 chars)" else "Per ilgas (maks. 20 simbolių)"
        } else null
    }

    fun validateNextNumber(value: String): String? {
        val num = value.toIntOrNull()
        return if (num == null || num <= 0) {
            if (language == AppLanguage.EN) "Must be a positive integer" else "Turi būti teigiamas sveikas skaičius"
        } else null
    }

    fun checkPrefix(group: String, value: String): Boolean {
        val err = validatePrefix(value)
        when (group) {
            "Financial" -> errorPrefixFinancial = err
            "Operational" -> errorPrefixOperational = err
            "Delivery" -> errorPrefixDelivery = err
            "CRM" -> errorPrefixCRM = err
        }
        return err == null
    }

    fun checkNextNumber(group: String, value: String): Boolean {
        val err = validateNextNumber(value)
        when (group) {
            "Financial" -> errorNextNumberFinancial = err
            "Operational" -> errorNextNumberOperational = err
            "Delivery" -> errorNextNumberDelivery = err
            "CRM" -> errorNextNumberCRM = err
        }
        return err == null
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = getSettingsString("title", language),
                onBack = {
                    ignoreFocusLoss = true
                    onBack()
                },
                onHome = if (onHome != null) {
                    {
                        ignoreFocusLoss = true
                        onHome()
                    }
                } else null
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 0. Serverio Prisijungimo Nustatymai (Terminal API Host / Port / HTTPS)
                var isServerConfigExpanded by remember { mutableStateOf(true) }
                var serverHostState by remember { mutableStateOf(AppSettings.getApiHost()) }
                var serverPortState by remember { mutableStateOf(AppSettings.getApiPort().toString()) }
                var serverUseHttpsState by remember { mutableStateOf(AppSettings.getUseHttps()) }
                var serverConfigSavedBanner by remember { mutableStateOf<String?>(null) }

                CollapsibleSettingsCard(
                    title = if (language == AppLanguage.EN) "Terminal API Server Connection Settings" else "Terminal API Serverio Prisijungimo Nustatymai",
                    isExpanded = isServerConfigExpanded,
                    onToggle = { isServerConfigExpanded = !isServerConfigExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        serverConfigSavedBanner?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = serverHostState,
                            onValueChange = { serverHostState = it },
                            label = { Text(if (language == AppLanguage.EN) "API Server Host / IP" else "Serverio Host / IP Adresas") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = serverPortState,
                            onValueChange = { serverPortState = it },
                            label = { Text(if (language == AppLanguage.EN) "API Server Port" else "Serverio Prievadas (Port)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val portNum = serverPortState.toIntOrNull() ?: 8081
                                AppSettings.saveServerConfig(serverHostState, portNum, true)
                                apiClient.updateServerConfig(serverHostState, portNum, true)
                                serverConfigSavedBanner = if (language == AppLanguage.EN) 
                                    "Server settings saved (${apiClient.baseUrl})!"
                                else 
                                    "Serverio nustatymai išsaugoti (${apiClient.baseUrl})!"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (language == AppLanguage.EN) "Save Server Settings" else "Išsaugoti Serverio Nustatymus")
                        }
                    }
                }

                // 1. Pranešimų nustatymai
                CollapsibleSettingsCard(
                    title = getSettingsString("notifications", language),
                    isExpanded = isNotificationsExpanded,
                    onToggle = { isNotificationsExpanded = !isNotificationsExpanded }
                ) {
                    SwitchSettingRow(
                        icon = Icons.Default.NotificationsActive,
                        title = getSettingsString("notification_sound", language),
                        subtitle = getSettingsString("notification_sound_desc", language),
                        isChecked = isNotificationSoundEnabled,
                        onCheckedChange = {
                            AppSettings.setNotificationSound(it)
                            saveSettings()
                        }
                    )
                }

                // 2. Išvaizdos Nustatymai
                CollapsibleSettingsCard(
                    title = getSettingsString("appearance", language),
                    isExpanded = isAppearanceExpanded,
                    onToggle = { isAppearanceExpanded = !isAppearanceExpanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = getSettingsString("text_size", language),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppFontSize.entries.forEach { option ->
                                val isSelected = selectedFontSize == option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            AppSettings.setFontSize(option)
                                            saveSettings()
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.name.take(1),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        
                        val fontSizeName = getSettingsString("font_${selectedFontSize.name}", language)
                        Text(
                            text = "${getSettingsString("selected", language)}$fontSizeName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    SwitchSettingRow(
                        icon = Icons.Default.DarkMode,
                        title = getSettingsString("dark_mode", language),
                        subtitle = getSettingsString("dark_mode_desc", language),
                        isChecked = isDarkMode,
                        onCheckedChange = {
                            AppSettings.setDarkMode(it)
                            saveSettings()
                        }
                    )

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = getSettingsString("pagination_limit", language),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = getSettingsString("pagination_limit_desc", language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(20, 50, 100).forEach { option ->
                                val isSelected = paginationLimit == option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            AppSettings.setPaginationLimit(option)
                                            saveSettings()
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.toString(),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Importo Nustatymai
                CollapsibleSettingsCard(
                    title = getSettingsString("import_settings", language),
                    isExpanded = isImportSettingsExpanded,
                    onToggle = { isImportSettingsExpanded = !isImportSettingsExpanded }
                ) {
                    val formats = listOf("JSON", "EIP", "PDF", "CSV", "XLSX", "Word", "XML", "TXT")
                    formats.forEachIndexed { index, fmt ->
                        var isChecked by remember { mutableStateOf(AppSettings.isImportAiEnabled(fmt)) }
                        SwitchSettingRow(
                            icon = Icons.Default.Devices,
                            title = "${getSettingsString("import_ai", language)} ($fmt)",
                            subtitle = getSettingsString("import_ai_desc", language),
                            isChecked = isChecked,
                            onCheckedChange = {
                                AppSettings.setImportAiEnabled(fmt, it)
                                isChecked = it
                            }
                        )
                        if (index < formats.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }

                // 4. Įmonės / Asmens Rekvizitai
                CollapsibleSettingsCard(
                    title = getSettingsString("requisites", language),
                    isExpanded = isRequisitesExpanded,
                    onToggle = { isRequisitesExpanded = !isRequisitesExpanded }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isNameSuccess = validationSuccesses.contains("name") && errorName == null
                        val isCodeSuccess = validationSuccesses.contains("code") && errorCode == null
                        val isVatCodeSuccess = validationSuccesses.contains("vatCode") && errorVatCode == null
                        val isAddressSuccess = validationSuccesses.contains("address") && errorAddress == null
                        val isBankAccountSuccess = validationSuccesses.contains("bankAccount") && errorBankAccount == null
                        val isBankSwiftSuccess = validationSuccesses.contains("bankSwift") && errorBankSwift == null

                        OutlinedTextField(
                            value = reqName,
                            onValueChange = {
                                AppSettings.setReqName(it)
                                if (errorName != null) {
                                    checkName(it)
                                } else {
                                    validationSuccesses.remove("name")
                                }
                                if (!dirtyFields.contains("name")) dirtyFields.add("name")
                                saveSettings()
                            },
                            label = { Text(getSettingsString("req_name", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(validationModifier("name", focusRequesterName, focusRequesterCode) { checkName(reqName) }),
                            isError = errorName != null,
                            supportingText = errorName?.let { { Text(it) } },
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
                            } else OutlinedTextFieldDefaults.colors(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reqCode,
                            onValueChange = {
                                AppSettings.setReqCode(it)
                                if (errorCode != null) {
                                    checkCode(it)
                                } else {
                                    validationSuccesses.remove("code")
                                }
                                if (!dirtyFields.contains("code")) dirtyFields.add("code")
                                saveSettings()
                            },
                            label = { Text(getSettingsString("req_code", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(validationModifier("code", focusRequesterCode, focusRequesterVatCode) { checkCode(reqCode) }),
                            isError = errorCode != null,
                            supportingText = errorCode?.let { { Text(it) } },
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
                            } else OutlinedTextFieldDefaults.colors(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reqVatCode,
                            onValueChange = {
                                AppSettings.setReqVatCode(it)
                                if (errorVatCode != null) {
                                    checkVatCode(it)
                                } else {
                                    validationSuccesses.remove("vatCode")
                                }
                                if (!dirtyFields.contains("vatCode")) dirtyFields.add("vatCode")
                                saveSettings()
                            },
                            label = { Text(getSettingsString("req_vat_code", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(validationModifier("vatCode", focusRequesterVatCode, focusRequesterAddress) { checkVatCode(reqVatCode) }),
                            isError = errorVatCode != null,
                            supportingText = errorVatCode?.let { { Text(it) } },
                            trailingIcon = if (isVatCodeSuccess) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Valid",
                                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    )
                                }
                            } else null,
                            colors = if (isVatCodeSuccess) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            } else OutlinedTextFieldDefaults.colors(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reqAddress,
                            onValueChange = {
                                AppSettings.setReqAddress(it)
                                if (errorAddress != null) {
                                    checkAddress(it)
                                } else {
                                    validationSuccesses.remove("address")
                                }
                                if (!dirtyFields.contains("address")) dirtyFields.add("address")
                                saveSettings()
                            },
                            label = { Text(getSettingsString("req_address", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(validationModifier("address", focusRequesterAddress, focusRequesterBankAccount) { checkAddress(reqAddress) }),
                            isError = errorAddress != null,
                            supportingText = errorAddress?.let { { Text(it) } },
                            trailingIcon = if (isAddressSuccess) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Valid",
                                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    )
                                }
                            } else null,
                            colors = if (isAddressSuccess) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            } else OutlinedTextFieldDefaults.colors(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reqBankAccount,
                            onValueChange = {
                                AppSettings.setReqBankAccount(it)
                                if (errorBankAccount != null) {
                                    checkBankAccount(it)
                                } else {
                                    validationSuccesses.remove("bankAccount")
                                }
                                if (!dirtyFields.contains("bankAccount")) dirtyFields.add("bankAccount")
                                saveSettings()
                            },
                            label = { Text(getSettingsString("req_bank_account", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(validationModifier("bankAccount", focusRequesterBankAccount, focusRequesterBankSwift) { checkBankAccount(reqBankAccount) }),
                            isError = errorBankAccount != null,
                            supportingText = errorBankAccount?.let { { Text(it) } },
                            trailingIcon = if (isBankAccountSuccess) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Valid",
                                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    )
                                }
                            } else null,
                            colors = if (isBankAccountSuccess) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            } else OutlinedTextFieldDefaults.colors(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reqBankSwift,
                            onValueChange = {
                                AppSettings.setReqBankSwift(it)
                                if (errorBankSwift != null) {
                                    checkBankSwift(it)
                                } else {
                                    validationSuccesses.remove("bankSwift")
                                }
                                if (!dirtyFields.contains("bankSwift")) dirtyFields.add("bankSwift")
                                saveSettings()
                            },
                            label = { Text(getSettingsString("req_bank_swift", language)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(validationModifier("bankSwift", focusRequesterBankSwift, null) { checkBankSwift(reqBankSwift) }),
                            isError = errorBankSwift != null,
                            supportingText = errorBankSwift?.let { { Text(it) } },
                            trailingIcon = if (isBankSwiftSuccess) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Valid",
                                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    )
                                }
                            } else null,
                            colors = if (isBankSwiftSuccess) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                    focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                    unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                )
                            } else OutlinedTextFieldDefaults.colors(),
                            singleLine = true
                        )
                    }
                }

                // 4b. Dokumentų numeracija
                CollapsibleSettingsCard(
                    title = getSettingsString("numbering", language),
                    isExpanded = isNumberingExpanded,
                    onToggle = { isNumberingExpanded = !isNumberingExpanded }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Financial
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = getSettingsString("group_financial", language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val isSuccess = errorPrefixFinancial == null
                                OutlinedTextField(
                                    value = docSeriesPrefixFinancial,
                                    onValueChange = {
                                        AppSettings.setDocSeriesPrefix("Financial", it)
                                        checkPrefix("Financial", it)
                                    },
                                    label = { Text(getSettingsString("doc_prefix", language)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterPrefixFinancial)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                                if (checkPrefix("Financial", docSeriesPrefixFinancial)) {
                                                    focusRequesterNextNumberFinancial.requestFocus()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            false
                                        },
                                    isError = errorPrefixFinancial != null,
                                    supportingText = errorPrefixFinancial?.let { { Text(it) } },
                                    trailingIcon = if (isSuccess) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) }
                                    } else null,
                                    singleLine = true
                                )
                                val isNumSuccess = errorNextNumberFinancial == null && docSeriesNextNumberFinancial > 0
                                OutlinedTextField(
                                    value = docSeriesNextNumberFinancial.toString(),
                                    onValueChange = {
                                        checkNextNumber("Financial", it)
                                        it.toIntOrNull()?.let { num ->
                                            if (num > 0) AppSettings.setDocSeriesNextNumber("Financial", num)
                                        }
                                    },
                                    label = { Text(getSettingsString("doc_next_number", language)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterNextNumberFinancial)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                                if (checkNextNumber("Financial", docSeriesNextNumberFinancial.toString())) {
                                                    focusRequesterPrefixOperational.requestFocus()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            false
                                        },
                                    isError = errorNextNumberFinancial != null,
                                    supportingText = errorNextNumberFinancial?.let { { Text(it) } },
                                    trailingIcon = if (isNumSuccess) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) }
                                    } else null,
                                    singleLine = true
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // 2. Operational
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = getSettingsString("group_operational", language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val isSuccess = errorPrefixOperational == null
                                OutlinedTextField(
                                    value = docSeriesPrefixOperational,
                                    onValueChange = {
                                        AppSettings.setDocSeriesPrefix("Operational", it)
                                        checkPrefix("Operational", it)
                                    },
                                    label = { Text(getSettingsString("doc_prefix", language)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterPrefixOperational)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                                if (checkPrefix("Operational", docSeriesPrefixOperational)) {
                                                    focusRequesterNextNumberOperational.requestFocus()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            false
                                        },
                                    isError = errorPrefixOperational != null,
                                    supportingText = errorPrefixOperational?.let { { Text(it) } },
                                    trailingIcon = if (isSuccess) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) }
                                    } else null,
                                    singleLine = true
                                )
                                val isNumSuccess = errorNextNumberOperational == null && docSeriesNextNumberOperational > 0
                                OutlinedTextField(
                                    value = docSeriesNextNumberOperational.toString(),
                                    onValueChange = {
                                        checkNextNumber("Operational", it)
                                        it.toIntOrNull()?.let { num ->
                                            if (num > 0) AppSettings.setDocSeriesNextNumber("Operational", num)
                                        }
                                    },
                                    label = { Text(getSettingsString("doc_next_number", language)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterNextNumberOperational)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                                if (checkNextNumber("Operational", docSeriesNextNumberOperational.toString())) {
                                                    focusRequesterPrefixDelivery.requestFocus()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            false
                                        },
                                    isError = errorNextNumberOperational != null,
                                    supportingText = errorNextNumberOperational?.let { { Text(it) } },
                                    trailingIcon = if (isNumSuccess) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) }
                                    } else null,
                                    singleLine = true
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // 3. Delivery
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = getSettingsString("group_delivery", language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val isSuccess = errorPrefixDelivery == null
                                OutlinedTextField(
                                    value = docSeriesPrefixDelivery,
                                    onValueChange = {
                                        AppSettings.setDocSeriesPrefix("Delivery", it)
                                        checkPrefix("Delivery", it)
                                    },
                                    label = { Text(getSettingsString("doc_prefix", language)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterPrefixDelivery)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                                if (checkPrefix("Delivery", docSeriesPrefixDelivery)) {
                                                    focusRequesterNextNumberDelivery.requestFocus()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            false
                                        },
                                    isError = errorPrefixDelivery != null,
                                    supportingText = errorPrefixDelivery?.let { { Text(it) } },
                                    trailingIcon = if (isSuccess) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) }
                                    } else null,
                                    singleLine = true
                                )
                                val isNumSuccess = errorNextNumberDelivery == null && docSeriesNextNumberDelivery > 0
                                OutlinedTextField(
                                    value = docSeriesNextNumberDelivery.toString(),
                                    onValueChange = {
                                        checkNextNumber("Delivery", it)
                                        it.toIntOrNull()?.let { num ->
                                            if (num > 0) AppSettings.setDocSeriesNextNumber("Delivery", num)
                                        }
                                    },
                                    label = { Text(getSettingsString("doc_next_number", language)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterNextNumberDelivery)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                                if (checkNextNumber("Delivery", docSeriesNextNumberDelivery.toString())) {
                                                    focusRequesterPrefixCRM.requestFocus()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            false
                                        },
                                    isError = errorNextNumberDelivery != null,
                                    supportingText = errorNextNumberDelivery?.let { { Text(it) } },
                                    trailingIcon = if (isNumSuccess) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) }
                                    } else null,
                                    singleLine = true
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // 4. CRM
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = getSettingsString("group_crm", language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val isSuccess = errorPrefixCRM == null
                                OutlinedTextField(
                                    value = docSeriesPrefixCRM,
                                    onValueChange = {
                                        AppSettings.setDocSeriesPrefix("CRM", it)
                                        checkPrefix("CRM", it)
                                    },
                                    label = { Text(getSettingsString("doc_prefix", language)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterPrefixCRM)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                                if (checkPrefix("CRM", docSeriesPrefixCRM)) {
                                                    focusRequesterNextNumberCRM.requestFocus()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            false
                                        },
                                    isError = errorPrefixCRM != null,
                                    supportingText = errorPrefixCRM?.let { { Text(it) } },
                                    trailingIcon = if (isSuccess) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) }
                                    } else null,
                                    singleLine = true
                                )
                                val isNumSuccess = errorNextNumberCRM == null && docSeriesNextNumberCRM > 0
                                OutlinedTextField(
                                    value = docSeriesNextNumberCRM.toString(),
                                    onValueChange = {
                                        checkNextNumber("CRM", it)
                                        it.toIntOrNull()?.let { num ->
                                            if (num > 0) AppSettings.setDocSeriesNextNumber("CRM", num)
                                        }
                                    },
                                    label = { Text(getSettingsString("doc_next_number", language)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequesterNextNumberCRM)
                                        .onPreviewKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                                if (checkNextNumber("CRM", docSeriesNextNumberCRM.toString())) {
                                                    focusManager.clearFocus()
                                                }
                                                return@onPreviewKeyEvent true
                                            }
                                            false
                                        },
                                    isError = errorNextNumberCRM != null,
                                    supportingText = errorNextNumberCRM?.let { { Text(it) } },
                                    trailingIcon = if (isNumSuccess) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) }
                                    } else null,
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Saugumo ir prieigos valdymo nustatymai (tik ADMIN gali keisti slankiklių padėtį)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = if (language == AppLanguage.EN) "Security & Access Controls" else "Saugumo ir prieigos valdymas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (!isAdmin) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (language == AppLanguage.EN) "Note: Only ADMIN users can change security control switches." else "Pastaba: Šių saugumo slankiklių padėtį gali keisti tik ADMIN rolę turintis vartotojas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // 1. Baigtų dokumentų slankiklis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.EN) "Allow editing and deleting completed (Baigta) documents" else "Leisti koreguoti ir šalinti baigtus (Baigta) dokumentus",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (language == AppLanguage.EN) "When enabled, only ADMIN users can edit or delete completed documents." else "Įjungus šią parinktį, tik ADMIN rolę turintys vartotojai galės redaguoti ar ištrinti dokumentus su statusu 'Baigta'.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Switch(
                                checked = allowCompletedDocumentEditing,
                                onCheckedChange = { AppSettings.setAllowCompletedDocumentEditing(it) },
                                enabled = isAdmin
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // 2. Kitų įrenginių įrašų slankiklis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.EN) "Allow editing other devices' records" else "Leisti koreguoti kitų įrenginių įrašus",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (language == AppLanguage.EN) "When enabled, only ADMIN users can edit records created on other devices/terminals." else "Įjungus šią parinktį, tik ADMIN rolę turintys vartotojai galės redaguoti ar ištrinti kito įrenginio sukurtus įrašus.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Switch(
                                checked = allowEditingOtherDevicesRecords,
                                onCheckedChange = { AppSettings.setAllowEditingOtherDevicesRecords(it) },
                                enabled = isAdmin
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // 3. Kitų vartotojų įrašų slankiklis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.EN) "Allow editing other users' records" else "Leisti koreguoti kitų vartotojų įrašus",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (language == AppLanguage.EN) "When enabled, only ADMIN users can edit records created by other users." else "Įjungus šią parinktį, tik ADMIN rolę turintys vartotojai galės redaguoti ar ištrinti kito vartotojo sukurtus įrašus.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Switch(
                                checked = allowEditingOtherUsersRecords,
                                onCheckedChange = { AppSettings.setAllowEditingOtherUsersRecords(it) },
                                enabled = isAdmin
                            )
                        }
                    }
                }

                // 5. Informacija
                CollapsibleSettingsCard(
                    title = getSettingsString("information", language),
                    isExpanded = isInfoExpanded,
                    onToggle = { isInfoExpanded = !isInfoExpanded }
                ) {
                    InfoSettingRow(
                        icon = Icons.Default.Info,
                        title = getSettingsString("app_version", language),
                        value = "Vedlys (Desktop) 1.0.4"
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    InfoSettingRow(
                        icon = Icons.Default.Devices,
                        title = getSettingsString("terminal_id", language),
                        value = terminalId
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CollapsibleSettingsCard(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Column(content = content)
                }
            }
        }
    }
}

@Composable
private fun SwitchSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun InfoSettingRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    VedlysTheme {
        Surface {
            SettingsScreen(
                apiClient = com.suprogramuota_visata.api.ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                isAdmin = true,
                onBack = {},
                onHome = {}
            )
        }
    }
}

