package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.ChatMessage
import com.suprogramuota_visata.api.domain.models.MessagePriority
import com.suprogramuota_visata.vedlys.ui.components.ErrorBanner
import com.suprogramuota_visata.vedlys.ui.components.LoadingOverlay
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import com.suprogramuota_visata.vedlys.viewmodel.NotificationsViewModel
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.AppLanguage
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme

fun getNotificationsString(key: String, language: AppLanguage): String {
    return com.suprogramuota_visata.vedlys.utils.Localization.getString("notifications", key, language)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    apiClient: ApiSvClient,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val viewModel = remember { NotificationsViewModel(apiClient) }
    val language by AppSettings.selectedLanguage.collectAsState()
    DisposableEffect(Unit) { onDispose { viewModel.dispose() } }

    var messageText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(MessagePriority.NORMAL.name) }
    var priorityExpanded by remember { mutableStateOf(false) }
    
    var selectedReceiverId by remember { mutableStateOf<String?>(null) }
    var receiverExpanded by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.chatItems.size) {
        if (viewModel.chatItems.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = getNotificationsString("title", language),
                onBack = onBack,
                onHome = onHome,
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = getNotificationsString("refresh", language))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            viewModel.errorMessage?.let {
                ErrorBanner(it, onDismiss = { viewModel.clearError() })
            }

            val priorityColor = when (selectedPriority) {
                "URGENT" -> Color(0xFFB3261E)
                "WARNING" -> Color(0xFFFF9900)
                "REMINDER" -> Color(0xFF00FFFF)
                else -> MaterialTheme.colorScheme.primary
            }

            // Input Block Moved to Top
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp), 
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text(getNotificationsString("placeholder", language)) },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = priorityColor,
                            unfocusedIndicatorColor = priorityColor.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = priorityExpanded,
                            onExpandedChange = { priorityExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedPriority,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                                modifier = Modifier.width(130.dp).menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = priorityColor),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = priorityColor,
                                    unfocusedIndicatorColor = priorityColor.copy(alpha = 0.5f),
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = priorityExpanded,
                                onDismissRequest = { priorityExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                listOf("NORMAL", "REMINDER", "WARNING", "URGENT").forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p) },
                                        onClick = { selectedPriority = p; priorityExpanded = false }
                                    )
                                }
                            }
                        }
                        
                        ExposedDropdownMenuBox(
                            expanded = receiverExpanded,
                            onExpandedChange = { receiverExpanded = it }
                        ) {
                            val receiverName = if (selectedReceiverId == null) getNotificationsString("all", language) else viewModel.chatUsers.find { it["id"] == selectedReceiverId }?.get("name") ?: selectedReceiverId
                            OutlinedTextField(
                                value = receiverName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = receiverExpanded) },
                                modifier = Modifier.width(130.dp).menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = priorityColor),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = priorityColor,
                                    unfocusedIndicatorColor = priorityColor.copy(alpha = 0.5f),
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = receiverExpanded,
                                onDismissRequest = { receiverExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(getNotificationsString("all", language)) },
                                    onClick = { selectedReceiverId = null; receiverExpanded = false }
                                )
                                viewModel.chatUsers.forEach { user ->
                                    DropdownMenuItem(
                                        text = { Text(user["name"] ?: "") },
                                        onClick = { selectedReceiverId = user["id"]; receiverExpanded = false }
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                viewModel.send(messageText.trim(), selectedPriority, selectedReceiverId)
                                messageText = ""
                            },
                            enabled = messageText.isNotBlank() && !viewModel.isSending,
                            modifier = Modifier.height(50.dp).width(130.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = priorityColor 
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = getNotificationsString("send", language))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(getNotificationsString("send", language))
                        }
                    }
                }
            }

            // Atskyrėjas priderintas prie kosminės temos
            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getNotificationsString("history", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getNotificationsString("show_read", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = viewModel.showReadMessages,
                        onCheckedChange = { viewModel.toggleShowReadMessages(it) },
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    viewModel.isLoading && viewModel.chatItems.isEmpty() -> LoadingOverlay()
                    viewModel.chatItems.isEmpty() -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            getNotificationsString("no_messages", language),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(viewModel.chatItems.reversed(), key = { it.id }) { msg ->
                            val isSentByMe = msg.senderId == "VedlysUser" // Pavyzdinis
                            if (isSentByMe) {
                                SentBubble(msg, language, onDismiss = { viewModel.dismiss(msg.id) })
                            } else {
                                ReceivedBubble(msg, language, onDismiss = { viewModel.dismiss(msg.id) })
                            }
                        }
                    }
                }
            }
            

        }
    }
}

@Composable
private fun ReceivedBubble(msg: ChatMessage, language: AppLanguage, onDismiss: () -> Unit) {
    val (bg, fg) = when (msg.priority.name) {
        "URGENT" -> Color(0xFFB3261E) to Color.White
        "WARNING" -> Color(0xFFFF9900).copy(alpha = 0.9f) to Color.Black // Lazer Orange
        "REMINDER" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderStroke = if (msg.priority.name == "REMINDER") {
        androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FFFF)) // Cyber Cyan border
    } else null
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = bg,
            contentColor = fg,
            shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp),
            border = borderStroke,
            modifier = Modifier.widthIn(max = 480.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${getNotificationsString("from", language)} ${msg.senderId}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = fg.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    if (msg.priority.name != "NORMAL") {
                        Spacer(Modifier.width(6.dp))
                        PriorityBadge(msg.priority.name, fg)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Check, contentDescription = getNotificationsString("mark_read", language), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(msg.message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SentBubble(msg: ChatMessage, language: AppLanguage, onDismiss: () -> Unit) {
    val borderStroke = if (msg.priority.name == "REMINDER") {
        androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FFFF))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            contentColor = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp),
            border = borderStroke,
            modifier = Modifier.widthIn(max = 480.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        getNotificationsString("you", language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary, // Analytics Purple
                        modifier = Modifier.weight(1f)
                    )
                    if (msg.priority.name != "NORMAL") {
                        Spacer(Modifier.width(6.dp))
                        PriorityBadge(msg.priority.name, MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Check, contentDescription = getNotificationsString("mark_read", language), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(msg.message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: String, parentFg: Color) {
    Surface(
        color = parentFg.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            priority,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = parentFg
        )
    }
}

@Preview
@Composable
fun NotificationsScreenPreview() {
    VedlysTheme {
        Surface {
            NotificationsScreen(
                apiClient = ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                onBack = {},
                onHome = {}
            )
        }
    }
}

