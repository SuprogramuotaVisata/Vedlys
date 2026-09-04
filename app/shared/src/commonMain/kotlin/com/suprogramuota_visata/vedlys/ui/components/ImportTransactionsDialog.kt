package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.animation.AnimatedVisibility
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.ExtTemplateDTO
import com.suprogramuota_visata.api.domain.models.TransactionDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.rivile.mapper.RivileMapper
import com.suprogramuota_visata.vedlys.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.suprogramuota_visata.vedlys.utils.PlatformFilePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTransactionsDialog(
    onDismiss: () -> Unit,
    apiClient: ApiSvClient
) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Formats & Templates Selection
    val formats = listOf("JSON", "EIP", "PDF", "CSV", "XLSX", "Word", "XML", "TXT")
    var selectedFormat by remember { mutableStateOf("JSON") }
    var formatExpanded by remember { mutableStateOf(false) }

    var templates by remember { mutableStateOf<List<ExtTemplateDTO>>(emptyList()) }
    var selectedTemplate by remember { mutableStateOf<ExtTemplateDTO?>(null) }
    var templateExpanded by remember { mutableStateOf(false) }

    val isTemplateRequired = selectedFormat == "EIP"
    val isTemplateValid = !isTemplateRequired || selectedTemplate != null

    // Load templates for transactions
    LaunchedEffect(Unit) {
        val res = apiClient.extTemplateRepository.getTemplatesByOwnerType("TransactionSv")
        if (res.isSuccess) {
            templates = res.getOrNull() ?: emptyList()
            selectedTemplate = templates.find { it.isDefault } ?: templates.firstOrNull()
        }
    }

    val useAi = AppSettings.isImportAiEnabled(selectedFormat)

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Importuoti transakcijas (išorės tiekėjų)") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Pasirinkite failo formatą ir šabloną importui.")
                Spacer(modifier = Modifier.height(12.dp))

                // Format selection dropdown
                ExposedDropdownMenuBox(
                    expanded = formatExpanded,
                    onExpandedChange = { formatExpanded = !formatExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFormat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Formatas") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = formatExpanded,
                        onDismissRequest = { formatExpanded = false }
                    ) {
                        formats.forEach { fmt ->
                            DropdownMenuItem(
                                text = { Text(fmt) },
                                onClick = {
                                    selectedFormat = fmt
                                    selectedFile = null // Reset selected file on format change
                                    formatExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Template selection dropdown + JSON pavyzdys button in a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = templateExpanded,
                            onExpandedChange = { templateExpanded = !templateExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTemplate?.name ?: "Nepasirinktas",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isTemplateRequired) "Importo Šablonas (Privalomas)" else "Importo Šablonas") },
                                isError = isTemplateRequired && selectedTemplate == null,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = templateExpanded,
                                onDismissRequest = { templateExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nepasirinktas") },
                                    onClick = {
                                        selectedTemplate = null
                                        templateExpanded = false
                                    }
                                )
                                templates.forEach { tpl ->
                                    DropdownMenuItem(
                                        text = { Text(tpl.name) },
                                        onClick = {
                                            selectedTemplate = tpl
                                            templateExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    var showSampleDialog by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = { showSampleDialog = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Pavyzdys")
                    }

                    if (showSampleDialog) {
                        JsonSampleDialog(
                            template = selectedTemplate,
                            isTransaction = true,
                            format = selectedFormat,
                            onDismiss = { showSampleDialog = false }
                        )
                    }
                }

                if (isTemplateRequired && selectedTemplate == null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Dėmesio: Pasirinkus EIP formatą, privaloma pasirinkti importo šabloną!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // AI Prompt input field shown when useAi is true
                AnimatedVisibility(visible = useAi) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = aiPrompt,
                            onValueChange = { aiPrompt = it },
                            label = { Text("DI Agento nurodymai / Laukų atitikimas (Chat)") },
                            placeholder = { 
                                Text(
                                    if (selectedFormat == "EIP") 
                                        "Pvz.: I06_DOK_NR atitinka Dokumento numeris, I07_KODAS_PS atitinka Prekės kodas..." 
                                    else 
                                        "Pvz.: praleisk prekes be kainos, priskirk grupes pagal pavadinimą..."
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // File picker button
                Button(onClick = {
                    val (desc, exts) = when (selectedFormat) {
                        "JSON" -> Pair("JSON failai (*.json)", arrayOf("json"))
                        "EIP" -> Pair("Rivile EIP failai (*.xml, *.eip, *.txt, *.edi)", arrayOf("xml", "eip", "txt", "edi"))
                        "PDF" -> Pair("PDF failai (*.pdf)", arrayOf("pdf"))
                        "CSV" -> Pair("CSV failai (*.csv)", arrayOf("csv"))
                        "XLSX" -> Pair("Excel failai (*.xlsx)", arrayOf("xlsx"))
                        "Word" -> Pair("Word failai (*.docx, *.doc)", arrayOf("docx", "doc"))
                        "XML" -> Pair("XML failai (*.xml)", arrayOf("xml"))
                        "TXT" -> Pair("Tekstiniai failai (*.txt)", arrayOf("txt"))
                        else -> Pair("Visi failai", emptyArray())
                    }
                    selectedFile = PlatformFilePicker.pickFileToOpen("Pasirinkti failą", desc, *exts)
                }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selectedFile == null) "Pasirinkti failą" else "Failas: ${selectedFile?.name}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // AI Indicator status
                if (useAi) {
                    Text("Apdorojimui bus naudojamas DI agentas (Google Gemini API)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                } else {
                    if (selectedFormat == "EIP") {
                        Text("EIP apdorojimas atliekamas tiesiogiai per RivileSv biblioteką (reikalingas laukų sutapimas su šablonu)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("Apdorojimas atliekamas standartiniu būdu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                    Text(statusMessage, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                } else if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            if (isSuccess) {
                Button(onClick = onDismiss) {
                    Text("Baigti")
                }
            } else {
                Button(
                    onClick = {
                        val file = selectedFile ?: return@Button
                        scope.launch {
                            isLoading = true
                            statusMessage = "Siunčiamas failas..."
                            try {
                                val docType = "FINANCIAL" // Defaults to financial journal import
                                
                                val drafts: List<TransactionDTO> = if (selectedFormat == "EIP") {
                                    val fileText = withContext(Dispatchers.IO) {
                                        val bytes = file.readBytes()
                                        val preview = String(bytes.take(300).toByteArray(), Charsets.ISO_8859_1)
                                        val encodingMatch = Regex("encoding=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(preview)
                                        val charsetName = encodingMatch?.groupValues?.get(1)?.trim() ?: "windows-1257"
                                        
                                        var decoded = try {
                                            String(bytes, java.nio.charset.Charset.forName(charsetName))
                                        } catch (_: Exception) {
                                            String(bytes, Charsets.UTF_8)
                                        }
                                        
                                        if (decoded.contains("\uFFFD")) {
                                            try {
                                                decoded = String(bytes, java.nio.charset.Charset.forName("windows-1257"))
                                            } catch (_: Exception) {}
                                        }
                                        decoded
                                    }
                                    statusMessage = "Apdorojama per RivileSv biblioteką..."
                                    val mappingRules = if (useAi && aiPrompt.isNotBlank()) aiPrompt else null
                                    RivileMapper.parseToTransactions(
                                        xmlContent = fileText,
                                        template = selectedTemplate,
                                        documentType = docType,
                                        aiMappingRules = mappingRules
                                    )
                                } else {
                                    val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                                    val result = apiClient.transactionRepository.importFile(
                                        fileBytes = bytes,
                                        filename = file.name,
                                        format = selectedFormat,
                                        useAi = useAi,
                                        templateId = selectedTemplate?.id,
                                        documentType = docType,
                                        aiPrompt = if (useAi) aiPrompt.takeIf { it.isNotBlank() } else null
                                    )
                                    if (result is ApiResult.Success) {
                                        result.data ?: emptyList()
                                    } else {
                                        throw Exception((result as ApiResult.Error).message)
                                    }
                                }

                                statusMessage = "Apdorota: gauta ${drafts.size} dokumentų. Įrašoma..."
                                var totalImported = 0
                                
                                for ((index, tx) in drafts.withIndex()) {
                                    statusMessage = "Įrašoma transakcija ${index + 1} iš ${drafts.size}: ${tx.documentNumber}..."
                                    val saveResult = apiClient.transactionRepository.create(tx)
                                    if (saveResult is ApiResult.Success) {
                                        totalImported++
                                        progress = (index + 1).toFloat() / drafts.size.toFloat()
                                    } else if (saveResult is ApiResult.Error) {
                                        throw Exception("Klaida įrašant ${tx.documentNumber}: ${saveResult.message}")
                                    }
                                }
                                statusMessage = "Sėkmingai importuota $totalImported transakcijų!"
                                isSuccess = true
                            } catch (e: Exception) {
                                statusMessage = "Klaida: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                },
                enabled = selectedFile != null && isTemplateValid && !isLoading
            ) {
                Text("Importuoti")
            }
        }
        },
        dismissButton = {
            if (!isSuccess) {
                TextButton(onClick = onDismiss, enabled = !isLoading) {
                    Text("Atšaukti")
                }
            }
        }
    )
}
