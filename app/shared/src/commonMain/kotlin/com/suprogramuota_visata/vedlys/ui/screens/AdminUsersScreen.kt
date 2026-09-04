package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.ApproveUserRequest
import com.suprogramuota_visata.api.domain.models.PendingUserDto
import com.suprogramuota_visata.api.domain.util.ApiResult
import kotlinx.coroutines.launch
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.AppLanguage
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import com.suprogramuota_visata.vedlys.viewmodel.TypesViewModel
import com.suprogramuota_visata.api.domain.models.TypeDTO
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme

fun getUsersString(key: String, language: AppLanguage): String {
    return com.suprogramuota_visata.vedlys.utils.Localization.getString("users", key, language)
}

@Composable
fun AdminUsersScreen(
    apiClient: ApiSvClient,
    onBack: () -> Unit,
    onHome: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val language by AppSettings.selectedLanguage.collectAsState()
    var pendingUsers by remember { mutableStateOf<List<PendingUserDto>>(emptyList()) }
    var approvedUsers by remember { mutableStateOf<List<PendingUserDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    val groupsViewModel = remember { TypesViewModel(apiClient) }
    var groups by remember { mutableStateOf<List<TypeDTO>>(emptyList()) }
    var selectedGroupFilter by remember { mutableStateOf<TypeDTO?>(null) }

    LaunchedEffect(Unit) {
        groupsViewModel.updateTypeName("GroupSv")
        groupsViewModel.loadInitial()
    }
    LaunchedEffect(groupsViewModel.items) {
        groups = groupsViewModel.items
    }

    val filteredPending = if (selectedGroupFilter == null) pendingUsers else pendingUsers.filter { it.groupId == selectedGroupFilter?.id }
    val filteredApproved = if (selectedGroupFilter == null) approvedUsers else approvedUsers.filter { it.groupId == selectedGroupFilter?.id }

    fun fetchUsers() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            val pendingResult = apiClient.adminRepository.getPendingUsers()
            val approvedResult = apiClient.adminRepository.getUsers()
            
            if (pendingResult is ApiResult.Success && approvedResult is ApiResult.Success) {
                pendingUsers = pendingResult.data
                approvedUsers = approvedResult.data
            } else {
                errorMessage = getUsersString("error_fetch", language)
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchUsers()
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = getUsersString("title", language),
                onBack = onBack,
                onHome = onHome
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("+ Sukurti naują vartotoją", fontWeight = FontWeight.Bold)
                }
            }

            if (showCreateDialog) {
                var newName by remember { mutableStateOf("") }
                var newEmail by remember { mutableStateOf("") }
                var newPassword by remember { mutableStateOf("") }
                var newRole by remember { mutableStateOf("USER") }
                var selectedGroup by remember { mutableStateOf<TypeDTO?>(null) }
                var isCreating by remember { mutableStateOf(false) }
                var dialogError by remember { mutableStateOf<String?>(null) }
                @OptIn(ExperimentalMaterial3Api::class)
                var expandedRole by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { if (!isCreating) showCreateDialog = false },
                    title = { Text("Sukurti naują vartotoją", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (dialogError != null) {
                                Text(dialogError!!, color = MaterialTheme.colorScheme.error)
                            }
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Vardas (Name)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newEmail,
                                onValueChange = { newEmail = it },
                                label = { Text("El. paštas (Email)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Slaptažodis (Password)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            @OptIn(ExperimentalMaterial3Api::class)
                            ExposedDropdownMenuBox(
                                expanded = expandedRole,
                                onExpandedChange = { expandedRole = !expandedRole },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = if (newRole == "ADMIN") "ADMIN (Administratorius)" else "USER (Vartotojas)",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Rolė") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedRole,
                                    onDismissRequest = { expandedRole = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("USER (Vartotojas)") },
                                        onClick = { newRole = "USER"; expandedRole = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("ADMIN (Administratorius)") },
                                        onClick = { newRole = "ADMIN"; expandedRole = false }
                                    )
                                }
                            }

                            com.suprogramuota_visata.vedlys.ui.components.SearchableGroupDropdown(
                                apiClient = apiClient,
                                targetType = "UserSv",
                                label = "Vartotojo grupė (neprivaloma)",
                                selectedGroupId = selectedGroup?.id,
                                onSelected = { selectedGroup = it }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newName.isBlank() || newEmail.isBlank() || newPassword.isBlank()) {
                                    dialogError = "Vardas, el. paštas ir slaptažodis yra privalomi!"
                                    return@Button
                                }
                                coroutineScope.launch {
                                    isCreating = true
                                    dialogError = null
                                    val req = com.suprogramuota_visata.api.domain.models.AdminCreateUserRequest(
                                        name = newName,
                                        email = newEmail,
                                        password = newPassword,
                                        role = newRole,
                                        groupId = selectedGroup?.id
                                    )
                                    when (val res = apiClient.adminRepository.createUser(req)) {
                                        is ApiResult.Success -> {
                                            showCreateDialog = false
                                            successMessage = "Vartotojas $newName sėkmingai sukurtas!"
                                            fetchUsers()
                                        }
                                        is ApiResult.Error -> {
                                            dialogError = res.message
                                        }
                                    }
                                    isCreating = false
                                }
                            },
                            enabled = !isCreating
                        ) {
                            if (isCreating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Sukurti")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCreateDialog = false },
                            enabled = !isCreating
                        ) {
                            Text("Atšaukti")
                        }
                    }
                )
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text("${getUsersString("error", language)} $errorMessage", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { fetchUsers() }) {
                    Text(getUsersString("try_again", language))
                }
            } else {
                if (successMessage != null) {
                    Text(successMessage!!, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Group filter dropdown
                @OptIn(ExperimentalMaterial3Api::class)
                var expandedGroup by remember { mutableStateOf(false) }
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = expandedGroup,
                    onExpandedChange = { expandedGroup = !expandedGroup },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    val groupText = selectedGroupFilter?.name ?: getUsersString("all_groups", language)
                    OutlinedTextField(
                        value = groupText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(getUsersString("group", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGroup,
                        onDismissRequest = { expandedGroup = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(getUsersString("all_groups", language)) },
                            onClick = {
                                selectedGroupFilter = null
                                expandedGroup = false
                                focusManager.clearFocus()
                            }
                        )
                        groups.forEach { grp ->
                            DropdownMenuItem(
                                text = { Text(grp.name) },
                                onClick = {
                                    selectedGroupFilter = grp
                                    expandedGroup = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Text(getUsersString("pending", language), style = MaterialTheme.typography.titleMedium)
                        if (filteredPending.isEmpty()) {
                            Text(getUsersString("no_pending", language))
                        }
                    }

                    items(filteredPending) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "ID: ${user.id} | ${getUsersString("name", language)} ${user.name}", fontWeight = FontWeight.Bold)
                                    Text(text = "${getUsersString("email", language)} ${user.email}")
                                    Text(text = "${getUsersString("device_id", language)} ${user.deviceId ?: getUsersString("none", language)}")
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            val result = apiClient.adminRepository.approveUser(
                                                user.id,
                                                ApproveUserRequest(role = "USER")
                                            )
                                            if (result is ApiResult.Success) {
                                                successMessage = "${user.name} ${getUsersString("approved_as", language)} USER."
                                                fetchUsers()
                                            } else if (result is ApiResult.Error) {
                                                errorMessage = result.message
                                            }
                                        }
                                    }) {
                                        Text(getUsersString("approve_user", language))
                                    }
                                    
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val result = apiClient.adminRepository.approveUser(
                                                    user.id,
                                                    ApproveUserRequest(role = "ADMIN")
                                                )
                                                if (result is ApiResult.Success) {
                                                    successMessage = "${user.name} ${getUsersString("approved_as", language)} ADMIN."
                                                    fetchUsers()
                                                } else if (result is ApiResult.Error) {
                                                    errorMessage = result.message
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text(getUsersString("approve_admin", language))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text(getUsersString("approved_users", language), style = MaterialTheme.typography.titleMedium)
                        if (filteredApproved.isEmpty()) {
                            Text(getUsersString("no_approved", language))
                        }
                    }

                    items(filteredApproved) { user ->
                        val isLt = language == AppLanguage.LT
                        val roleLabel = if (isLt) "Rolė" else "Role"
                        val requestBadge = if (isLt) "Prašo Admin teisių!" else "Requests Admin rights!"
                        val promoteLabel = if (isLt) "Suteikti Admin" else "Grant Admin"
                        val demoteLabel = if (isLt) "Atimti Admin" else "Revoke Admin"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "ID: ${user.id} | ${getUsersString("name", language)} ${user.name}", fontWeight = FontWeight.Bold)
                                    Text(text = "${getUsersString("email", language)} ${user.email}")
                                    Text(text = "${getUsersString("device_id", language)} ${user.deviceId ?: getUsersString("none", language)}")
                                    Text(text = "$roleLabel: ${user.role}", style = MaterialTheme.typography.bodyMedium)
                                    if (user.hasPendingAdminRequest) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = requestBadge,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (user.role == "ADMIN") {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val result = apiClient.adminRepository.approveUser(
                                                        user.id,
                                                        ApproveUserRequest(role = "USER")
                                                    )
                                                    if (result is ApiResult.Success) {
                                                        successMessage = if (isLt) "${user.name} sėkmingai pakeistas į USER." else "${user.name} demoted to USER."
                                                        fetchUsers()
                                                    } else if (result is ApiResult.Error) {
                                                        errorMessage = result.message
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Text(demoteLabel)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val result = apiClient.adminRepository.approveUser(
                                                        user.id,
                                                        ApproveUserRequest(role = "ADMIN")
                                                    )
                                                    if (result is ApiResult.Success) {
                                                        successMessage = if (isLt) "${user.name} sėkmingai suteiktos ADMIN teisės." else "${user.name} promoted to ADMIN."
                                                        fetchUsers()
                                                    } else if (result is ApiResult.Error) {
                                                        errorMessage = result.message
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(promoteLabel)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val result = apiClient.adminRepository.blockUser(user.id)
                                                if (result is ApiResult.Success) {
                                                    successMessage = "${user.name} ${getUsersString("user_blocked", language).lowercase()}"
                                                    fetchUsers()
                                                } else if (result is ApiResult.Error) {
                                                    errorMessage = result.message
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text(getUsersString("block", language))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AdminUsersScreenPreview() {
    VedlysTheme {
        Surface {
            AdminUsersScreen(
                apiClient = ApiSvClient(host = "127.0.0.1", port = 8081, useHttps = false),
                onBack = {},
                onHome = {}
            )
        }
    }
}

