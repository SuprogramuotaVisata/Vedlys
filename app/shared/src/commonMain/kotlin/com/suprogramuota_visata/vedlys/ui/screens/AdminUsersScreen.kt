package com.suprogramuota_visata.vedlys.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.ApproveUserRequest
import com.suprogramuota_visata.api.domain.models.PendingUserDto
import com.suprogramuota_visata.api.domain.models.TypeDTO
import com.suprogramuota_visata.api.domain.util.ApiResult
import com.suprogramuota_visata.vedlys.AppLanguage
import com.suprogramuota_visata.vedlys.AppSettings
import com.suprogramuota_visata.vedlys.ui.components.VedlysTopBar
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme
import com.suprogramuota_visata.vedlys.viewmodel.TypesViewModel
import kotlinx.coroutines.launch

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

    var showCreateDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<PendingUserDto?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

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

            var hasError = false
            val errorParts = mutableListOf<String>()

            if (pendingResult is ApiResult.Success) {
                pendingUsers = pendingResult.data
            } else if (pendingResult is ApiResult.Error) {
                hasError = true
                errorParts.add("Pending: ${pendingResult.message}")
            }

            if (approvedResult is ApiResult.Success) {
                approvedUsers = approvedResult.data
            } else if (approvedResult is ApiResult.Error) {
                hasError = true
                errorParts.add("Approved: ${approvedResult.message}")
            }

            if (hasError) {
                val details = errorParts.joinToString(" | ")
                errorMessage = "${getUsersString("error_fetch", language)} ($details)"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchUsers()
    }

    Scaffold(
        topBar = {
            VedlysTopBar(
                title = getUsersString("title", language),
                onBack = onBack,
                onHome = onHome,
                actions = {
                    IconButton(onClick = { fetchUsers() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = getUsersString("filter", language)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Controls: Group filter + Create User action
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                @OptIn(ExperimentalMaterial3Api::class)
                var expandedGroup by remember { mutableStateOf(false) }
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = expandedGroup,
                    onExpandedChange = { expandedGroup = !expandedGroup },
                    modifier = Modifier.weight(1f)
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

                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(getUsersString("create_new", language))
                }
            }

            // Messages
            if (errorMessage != null) {
                Text(
                    text = "${getUsersString("error", language)} $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // User Creation Dialog (Vedlys Form Style)
            if (showCreateDialog) {
                var newName by remember { mutableStateOf("") }
                var newEmail by remember { mutableStateOf("") }
                var newPassword by remember { mutableStateOf("") }
                var passwordVisible by remember { mutableStateOf(false) }
                var newRole by remember { mutableStateOf("USER") }
                var selectedGroup by remember { mutableStateOf<TypeDTO?>(null) }
                var isCreating by remember { mutableStateOf(false) }
                var dialogError by remember { mutableStateOf<String?>(null) }
                @OptIn(ExperimentalMaterial3Api::class)
                var expandedRole by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { if (!isCreating) showCreateDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    modifier = Modifier.widthIn(min = 320.dp, max = 500.dp).padding(16.dp),
                    title = {
                        Column {
                            Text(
                                text = getUsersString("create_title", language),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = getUsersString("create_subtitle", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (dialogError != null) {
                                Text(
                                    text = dialogError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text(getUsersString("field_name", language)) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newEmail,
                                onValueChange = { newEmail = it },
                                label = { Text(getUsersString("field_email", language)) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text(getUsersString("field_password", language)) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) getUsersString("hide_password", language) else getUsersString("show_password", language),
                                            tint = if (passwordVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                                    value = if (newRole == "ADMIN") getUsersString("role_admin", language) else getUsersString("role_user", language),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(getUsersString("field_role", language)) },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedRole,
                                    onDismissRequest = { expandedRole = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(getUsersString("role_user", language)) },
                                        onClick = { newRole = "USER"; expandedRole = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(getUsersString("role_admin", language)) },
                                        onClick = { newRole = "ADMIN"; expandedRole = false }
                                    )
                                }
                            }

                            com.suprogramuota_visata.vedlys.ui.components.SearchableGroupDropdown(
                                apiClient = apiClient,
                                targetType = "UserSv",
                                label = getUsersString("field_group", language),
                                selectedGroupId = selectedGroup?.id,
                                onSelected = { selectedGroup = it }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newName.isBlank() || newEmail.isBlank() || newPassword.isBlank()) {
                                    dialogError = getUsersString("required_fields", language)
                                    return@Button
                                }
                                coroutineScope.launch {
                                    isCreating = true
                                    dialogError = null
                                    val req = com.suprogramuota_visata.api.domain.models.AdminCreateUserRequest(
                                        name = newName.trim(),
                                        email = newEmail.trim(),
                                        password = newPassword,
                                        role = newRole,
                                        groupId = selectedGroup?.id
                                    )
                                    when (val res = apiClient.adminRepository.createUser(req)) {
                                        is ApiResult.Success -> {
                                            showCreateDialog = false
                                            successMessage = "${newName.trim()} - ${getUsersString("created_success", language)}"
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
                                Text(getUsersString("create_btn", language))
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCreateDialog = false },
                            enabled = !isCreating
                        ) {
                            Text(getUsersString("cancel_btn", language))
                        }
                    }
                )
            }

            // User Deletion Confirmation Dialog
            if (userToDelete != null) {
                val target = userToDelete!!
                AlertDialog(
                    onDismissRequest = { if (!isDeleting) userToDelete = null },
                    title = { Text(getUsersString("delete_confirm_title", language), fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            if (language == AppLanguage.LT)
                                "Ar tikrai norite pašalinti vartotoją „${target.name}“ (${target.email})? Šio veiksmo atšaukti negalima."
                            else
                                "Are you sure you want to delete user \"${target.name}\" (${target.email})? This action cannot be undone."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isDeleting = true
                                    when (val res = apiClient.adminRepository.deleteUser(target.id)) {
                                        is ApiResult.Success -> {
                                            successMessage = "${target.name} - ${getUsersString("delete_success", language)}"
                                            userToDelete = null
                                            fetchUsers()
                                        }
                                        is ApiResult.Error -> {
                                            errorMessage = res.message
                                            userToDelete = null
                                        }
                                    }
                                    isDeleting = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            enabled = !isDeleting
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                            } else {
                                Text(getUsersString("delete_btn", language))
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { userToDelete = null },
                            enabled = !isDeleting
                        ) {
                            Text(getUsersString("cancel_btn", language))
                        }
                    }
                )
            }

            // Content List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pending Users Section
                    item {
                        Text(
                            text = getUsersString("pending", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                        if (filteredPending.isEmpty()) {
                            Text(
                                text = getUsersString("no_pending", language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(filteredPending) { user ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassEmpty,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = user.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(getUsersString("status_pending", language)) }
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "ID: ${user.id}  •  ${user.email}${if (user.deviceId != null) "  •  ${getUsersString("device_id", language)} ${user.deviceId}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalButton(
                                        onClick = {
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
                                        },
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                    ) {
                                        Text(getUsersString("approve_user", language), style = MaterialTheme.typography.labelMedium)
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
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                    ) {
                                        Text(getUsersString("approve_admin", language), style = MaterialTheme.typography.labelMedium)
                                    }

                                    IconButton(
                                        onClick = { userToDelete = user }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = getUsersString("delete_tooltip", language),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Approved Users Section
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = getUsersString("approved_users", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (filteredApproved.isEmpty()) {
                            Text(
                                text = getUsersString("no_approved", language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(filteredApproved) { user ->
                        val requestBadge = getUsersString("status_admin_request", language)
                        val promoteLabel = getUsersString("grant_admin", language)
                        val demoteLabel = getUsersString("revoke_admin", language)

                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (user.role == "ADMIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (user.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (user.role == "ADMIN") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = user.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(user.role) },
                                            colors = if (user.role == "ADMIN") {
                                                AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            } else {
                                                AssistChipDefaults.assistChipColors()
                                            }
                                        )
                                        if (user.hasPendingAdminRequest) {
                                            AssistChip(
                                                onClick = {},
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                                ),
                                                label = { Text(requestBadge) }
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "ID: ${user.id}  •  ${user.email}${if (user.deviceId != null) "  •  ${getUsersString("device_id", language)} ${user.deviceId}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (user.role == "ADMIN") {
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val result = apiClient.adminRepository.approveUser(
                                                        user.id,
                                                        ApproveUserRequest(role = "USER")
                                                    )
                                                    if (result is ApiResult.Success) {
                                                        successMessage = "${user.name} ${getUsersString("demoted_success", language)}"
                                                        fetchUsers()
                                                    } else if (result is ApiResult.Error) {
                                                        errorMessage = result.message
                                                    }
                                                }
                                            },
                                            modifier = Modifier.height(36.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Text(demoteLabel, style = MaterialTheme.typography.labelMedium)
                                        }
                                    } else {
                                        FilledTonalButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val result = apiClient.adminRepository.approveUser(
                                                        user.id,
                                                        ApproveUserRequest(role = "ADMIN")
                                                    )
                                                    if (result is ApiResult.Success) {
                                                        successMessage = "${user.name} ${getUsersString("promoted_success", language)}"
                                                        fetchUsers()
                                                    } else if (result is ApiResult.Error) {
                                                        errorMessage = result.message
                                                    }
                                                }
                                            },
                                            modifier = Modifier.height(36.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Text(promoteLabel, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }

                                    IconButton(
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
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = getUsersString("block", language),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    if (user.id != 0) {
                                        IconButton(
                                            onClick = { userToDelete = user }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = getUsersString("delete_btn", language),
                                                tint = MaterialTheme.colorScheme.error
                                            )
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
