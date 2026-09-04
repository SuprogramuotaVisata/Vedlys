package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar

import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.runtime.*
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.AppLanguage
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme

fun getHomeString(key: String, language: AppLanguage): String {
    return com.suprogramuota_visata.vedlys.utils.Localization.getString("home", key, language)
}

@Composable
fun HomeScreen(
    isAdmin: Boolean,
    onOpenTasks: () -> Unit,
    onOpenTypes: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenSeklys: () -> Unit,
    onOpenRastvedys: () -> Unit,
    onOpenPlanuoklis: () -> Unit,
    onLogout: () -> Unit
) {
    val selectedLanguage by AppSettings.selectedLanguage.collectAsState()
    var showLanguageMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = getHomeString("title", selectedLanguage),
                actions = {
                    Box {
                        TextButton(onClick = { showLanguageMenu = true }) {
                            Text(
                                text = selectedLanguage.code,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            AppLanguage.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.displayName) },
                                    onClick = {
                                        AppSettings.setLanguage(lang)
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = getHomeString("logout", selectedLanguage), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = getHomeString("select_action", selectedLanguage),
                style = MaterialTheme.typography.titleLarge
            )
            
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: Tasks, Types, Notifications
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuTile(
                        icon = Icons.Default.Assignment,
                        title = getHomeString("tasks", selectedLanguage),
                        description = getHomeString("tasks_desc", selectedLanguage),
                        onClick = onOpenTasks,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    MenuTile(
                        icon = Icons.Default.Category,
                        title = getHomeString("types", selectedLanguage),
                        description = getHomeString("types_desc", selectedLanguage),
                        onClick = onOpenTypes,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    MenuTile(
                        icon = Icons.Default.Notifications,
                        title = getHomeString("notifications", selectedLanguage),
                        description = getHomeString("notifications_desc", selectedLanguage),
                        onClick = onOpenNotifications,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                
                // Row 2: Vedlys Papildomos Formos (Seklys, Raštvedys, Planuoklis) - Tik ADMIN rolei
                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MenuTile(
                            icon = Icons.Default.Search,
                            title = "Seklys",
                            description = "Seklys (Įvykių sekimas ir registracija)",
                            onClick = onOpenSeklys,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        MenuTile(
                            icon = Icons.Default.Build,
                            title = "Raštvedys",
                            description = "Raštvedys (Sistemos stebėjimas ir stebėjimo įrašų registracija)",
                            onClick = onOpenRastvedys,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        MenuTile(
                            icon = Icons.Default.Category,
                            title = "Planuoklis",
                            description = "Planuoklis (Automatinių užduočių planavimas)",
                            onClick = onOpenPlanuoklis,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }

                // Row 3: Users (Admin only), Settings, Help
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isAdmin) {
                        MenuTile(
                            icon = Icons.Default.Group,
                            title = getHomeString("users", selectedLanguage),
                            description = getHomeString("users_desc", selectedLanguage),
                            onClick = onOpenAdmin,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        MenuTile(
                            icon = Icons.Default.Build,
                            title = getHomeString("settings", selectedLanguage),
                            description = getHomeString("settings_desc", selectedLanguage),
                            onClick = onOpenSettings,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        MenuTile(
                            icon = Icons.Default.Help,
                            title = getHomeString("help", selectedLanguage),
                            description = getHomeString("help_desc", selectedLanguage),
                            onClick = onOpenHelp,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    } else {
                        MenuTile(
                            icon = Icons.Default.Build,
                            title = getHomeString("settings", selectedLanguage),
                            description = getHomeString("settings_desc", selectedLanguage),
                            onClick = onOpenSettings,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        MenuTile(
                            icon = Icons.Default.Help,
                            title = getHomeString("help", selectedLanguage),
                            description = getHomeString("help_desc", selectedLanguage),
                            onClick = onOpenHelp,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuTile(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary, // Cyber Cyan
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                title, 
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary // Analytics Purple
            )
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    VedlysTheme {
        Surface {
            HomeScreen(
                isAdmin = true,
                onOpenTasks = {},
                onOpenTypes = {},
                onOpenNotifications = {},
                onOpenSettings = {},
                onOpenAdmin = {},
                onOpenHelp = {},
                onOpenSeklys = {},
                onOpenRastvedys = {},
                onOpenPlanuoklis = {},
                onLogout = {}
            )
        }
    }
}

