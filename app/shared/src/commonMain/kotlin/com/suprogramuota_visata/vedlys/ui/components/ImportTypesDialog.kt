package com.suprogramuota_visata.vedlys.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.animation.AnimatedVisibility
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.TypeDTO
import com.suprogramuota_visata.api.domain.models.ExtTemplateDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.rivile.mapper.RivileMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import com.suprogramuota_visata.vedlys.utils.PlatformFilePicker
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTypesDialog(
    onDismiss: () -> Unit,
    typeName: String,
    apiClient: ApiSvClient
) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    val formats = listOf("JSON", "EIP", "PDF", "CSV", "XLSX", "Word", "XML", "TXT")
    var selectedFormat by remember { mutableStateOf("JSON") }
    var formatExpanded by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }
    val useAi = AppSettings.isImportAiEnabled(selectedFormat)
    val scope = rememberCoroutineScope()

    var templates by remember { mutableStateOf<List<ExtTemplateDTO>>(emptyList()) }
    var selectedTemplate by remember { mutableStateOf<ExtTemplateDTO?>(null) }
    var templateExpanded by remember { mutableStateOf(false) }

    val isTemplateRequired = selectedFormat == "EIP"
    val isTemplateValid = !isTemplateRequired || selectedTemplate != null

    LaunchedEffect(typeName) {
        val res = apiClient.extTemplateRepository.getTemplatesByOwnerType(typeName)
        if (res.isSuccess) {
            templates = res.getOrNull() ?: emptyList()
            selectedTemplate = templates.find { it.isDefault } ?: templates.firstOrNull()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Importuoti duomenis ($typeName)") },
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
                            isTransaction = false,
                            format = selectedFormat,
                            typeName = typeName,
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
                                        "Pvz.: N08_KODAS_PS atitinka Kodas, N08_PAV atitinka Pavadinimas, N08_SAVIKAINA atitinka Bazinė kaina..." 
                                    else 
                                        "Pvz.: praleisk įrašus be pavadinimo, išgauk papildomus kodus..."
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
                    Text(statusMessage, style = MaterialTheme.typography.bodySmall)
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
                            statusMessage = if (useAi) "Siunčiamas failas apdorojimui..." else "Skaitomas failas..."
                            try {
                                val types: List<TypeDTO> = if (selectedFormat == "EIP") {
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
                                    RivileMapper.parseToTypes(
                                        xmlContent = fileText,
                                        typeName = typeName,
                                        template = selectedTemplate,
                                        aiMappingRules = mappingRules
                                    )
                                } else if (useAi) {
                                    val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                                    val result = apiClient.typeRepository.importFile(
                                        fileBytes = bytes,
                                        filename = file.name,
                                        format = selectedFormat,
                                        useAi = useAi,
                                        templateId = selectedTemplate?.id,
                                        typeName = typeName,
                                        aiPrompt = aiPrompt.takeIf { it.isNotBlank() }
                                    )
                                    if (result is ApiResult.Success) {
                                        result.data
                                    } else {
                                        throw Exception((result as ApiResult.Error).message)
                                    }
                                } else {
                                    if (selectedFormat.lowercase() != "json") {
                                        throw Exception("Standartinis importas palaiko tik JSON ir EIP formatus. Kitiems formatams naudokite AI.")
                                    }
                                    var jsonString = withContext(Dispatchers.IO) { file.readText() }
                                    if (jsonString.startsWith("\uFEFF")) {
                                        jsonString = jsonString.substring(1)
                                    }
                                    val parsedTypes = Json { ignoreUnknownKeys = true }.decodeFromString<List<TypeDTO>>(jsonString)
                                    
                                    if (selectedTemplate != null) {
                                        val tpl = selectedTemplate!!
                                        parsedTypes.map { item ->
                                            val mergedAttributes = tpl.attributes.map { extAttr ->
                                                val existing = item.attributes.find { it.name == extAttr.name }
                                                val standardVal = when (extAttr.name) {
                                                    "Pavadinimas" -> item.name
                                                    "Kodas" -> item.code
                                                    "Brūkšninis kodas" -> item.barcode
                                                    "Matavimo vienetas" -> item.baseUnit
                                                    "Konvertavimo koeficientas" -> item.conversionFactor?.toString()
                                                    "Banko sąskaita" -> item.bankAccount
                                                    "Bankas / SWIFT" -> item.bankSwift
                                                    else -> null
                                                }
                                                val finalVal = existing?.value ?: standardVal ?: extAttr.defaultValue ?: ""
                                                com.suprogramuota_visata.api.domain.models.AttributeDTO(
                                                    id = existing?.id,
                                                    name = extAttr.name,
                                                    attributeType = extAttr.attributeType,
                                                    validate = extAttr.validateRule,
                                                    value = finalVal,
                                                    validations = extAttr.validations
                                                )
                                            }
                                            item.copy(
                                                extTemplateId = tpl.id,
                                                attributes = mergedAttributes
                                            )
                                        }
                                    } else {
                                        parsedTypes
                                    }
                                }

                                statusMessage = "Rasta ${types.size} įrašų. Siunčiama į serverį..."
                                val batchSize = 5000
                                val chunks = types.chunked(batchSize)
                                var totalImported = 0
                                
                                for ((index, chunk) in chunks.withIndex()) {
                                    statusMessage = "Siunčiama partija ${index + 1} iš ${chunks.size}..."
                                    val result = apiClient.typeRepository.bulkCreate(chunk, typeName)
                                    if (result is ApiResult.Success) {
                                        totalImported += result.data
                                        progress = (index + 1).toFloat() / chunks.size.toFloat()
                                    } else if (result is ApiResult.Error) {
                                        throw Exception(result.message)
                                    }
                                }
                                statusMessage = "Sėkmingai importuota $totalImported įrašų!"
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
