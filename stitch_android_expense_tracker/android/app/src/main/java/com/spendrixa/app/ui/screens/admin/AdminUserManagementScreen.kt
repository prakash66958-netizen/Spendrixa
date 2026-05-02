package com.spendrixa.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendrixa.app.data.model.UserAccount
import com.spendrixa.app.data.repository.AdminRepository
import com.spendrixa.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    adminRepository: AdminRepository,
    onNavigateDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val users by adminRepository.observeAllUsers().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var editingUser by remember { mutableStateOf<UserAccount?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredUsers = remember(users, searchQuery) {
        users.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.id.contains(searchQuery, ignoreCase = true)
        }
    }

    if (editingUser != null) {
        EditUserDialog(
            user = editingUser!!,
            onDismiss = { editingUser = null },
            onConfirm = { updates ->
                scope.launch {
                    adminRepository.updateUser(editingUser!!.id, updates).onSuccess {
                        snackbarHostState.showSnackbar("User updated")
                    }.onFailure {
                        snackbarHostState.showSnackbar("Failed to update user")
                    }
                    editingUser = null
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("User Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnSurface
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter platform users...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OutlineVariant
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredUsers.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No users found", color = OnSurfaceVariant)
                        }
                    }
                } else {
                    items(filteredUsers) { user ->
                        UserItem(
                            user = user,
                            onEdit = { editingUser = user },
                            onDelete = {
                                scope.launch {
                                    adminRepository.deleteUser(user.id).onSuccess {
                                        snackbarHostState.showSnackbar("User deleted")
                                    }
                                }
                            },
                            onClick = { onNavigateDetail(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(
    user: UserAccount,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Any>) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update User Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(mapOf("name" to name, "email" to email)) }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UserItem(
    user: UserAccount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = user.name.ifEmpty { "Unnamed User" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
            Row {
                IconButton(onClick = { onEdit() }) {
                    Icon(Icons.Default.Edit, "Edit", tint = Primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { onDelete() }) {
                    Icon(Icons.Default.Delete, "Delete", tint = Error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
