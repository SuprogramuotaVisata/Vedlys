package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.ui.components.ErrorBanner
import com.suprogramuota_visata.vedlys.viewmodel.AuthViewModel
import com.suprogramuota_visata.vedlys.utils.Messages
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme

@Composable
fun LoginScreen(
    apiClient: ApiSvClient,
    onLoginSuccess: (Boolean) -> Unit,
    onGoToRegister: () -> Unit,
    onGoToSettings: () -> Unit
) {
    val viewModel = remember { AuthViewModel(apiClient) }
    DisposableEffect(Unit) { onDispose { viewModel.dispose() } }

    LoginScreenContent(
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        initialServerHost = AppSettings.getApiHost(),
        initialServerPort = AppSettings.getApiPort().toString(),
        initialServerUseHttps = AppSettings.getUseHttps(),
        onLogin = { email, password -> viewModel.login(email, password, onLoginSuccess) },
        onClearError = { viewModel.clearError() },
        onGoToRegister = onGoToRegister,
        onGoToSettings = onGoToSettings,
        onUpdateServerConfig = { host, port, https ->
            AppSettings.saveServerConfig(host, port, https)
            apiClient.updateServerConfig(host, port, https)
        }
    )
}

/**
 * Stateless version of LoginScreen for easier testing and previews.
 * Resolves the "Write access not allowed during rendering" issue by avoiding
 * ApiSvClient initialization in Previews.
 */
@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    initialServerHost: String,
    initialServerPort: String,
    initialServerUseHttps: Boolean,
    onLogin: (String, String) -> Unit,
    onClearError: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToSettings: () -> Unit,
    onUpdateServerConfig: (String, Int, Boolean) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showServerDialog by remember { mutableStateOf(false) }
    var serverHost by remember { mutableStateOf(initialServerHost) }
    var serverPort by remember { mutableStateOf(initialServerPort) }
    var serverUseHttps by remember { mutableStateOf(initialServerUseHttps) }
    var serverSavedMsg by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Top right controls: LanguageSelector and Settings Gear Icon
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            com.suprogramuota_visata.vedlys.ui.components.LanguageSelector()
            IconButton(
                onClick = onGoToSettings
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Nustatymai",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Card(
            modifier = Modifier.widthIn(max = 440.dp).padding(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Messages.APP_NAME,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = Messages.LOGIN_SUBTITLE,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                errorMessage?.let {
                    ErrorBanner(it, onDismiss = onClearError)
                    Spacer(Modifier.height(8.dp))
                }

                serverSavedMsg?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(Messages.LOGIN_EMAIL) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(Messages.LOGIN_PASSWORD) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onLogin(email.trim(), password) },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(Messages.LOGIN_BUTTON)
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onGoToRegister,
                    enabled = !isLoading
                ) {
                    Text(Messages.LOGIN_NO_ACCOUNT)
                }
                Spacer(Modifier.height(2.dp))
                TextButton(
                    onClick = { showServerDialog = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Terminal API nustatymai",
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Terminal API Serverio Nustatymai") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nustatykite Terminal API mikroserviso adresą pirmo paleidimo metu:")
                    OutlinedTextField(
                        value = serverHost,
                        onValueChange = { serverHost = it },
                        label = { Text("Serverio Host / IP Adresas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = serverPort,
                        onValueChange = { serverPort = it },
                        label = { Text("Serverio Prievadas (Port)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val portNum = serverPort.toIntOrNull() ?: 8081
                        onUpdateServerConfig(serverHost, portNum, serverUseHttps)
                        serverSavedMsg = "Serverio nustatymai išsaugoti!"
                        showServerDialog = false
                    }
                ) {
                    Text("Išsaugoti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Atšaukti")
                }
            }
        )
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    VedlysTheme {
        Surface {
            LoginScreenContent(
                isLoading = false,
                errorMessage = null,
                initialServerHost = "127.0.0.1",
                initialServerPort = "8081",
                initialServerUseHttps = false,
                onLogin = { _, _ -> },
                onClearError = {},
                onGoToRegister = {},
                onGoToSettings = {},
                onUpdateServerConfig = { _, _, _ -> }
            )
        }
    }
}
