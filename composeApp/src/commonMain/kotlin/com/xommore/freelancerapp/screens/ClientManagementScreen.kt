package com.xommore.freelancerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xommore.freelancerapp.data.model.Client
import com.xommore.freelancerapp.data.currentTimeMillis
import com.xommore.freelancerapp.ui.theme.*

@Composable
fun ClientManagementScreen(
    clients: List<Client>,
    onBack: () -> Unit,
    onAddClient: (Client) -> Unit,
    onUpdateClient: (Client) -> Unit,
    onDeleteClient: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingClient by remember { mutableStateOf<Client?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Client?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("클라이언트 관리", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            TextButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("추가", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
        }

        if (clients.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().clickable { showAddDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "추가", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("등록된 클라이언트가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("여기를 눌러 클라이언트를 추가하세요", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("총 ${clients.size}개", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }

                items(clients, key = { it.id }) { client ->
                    ClientCard(
                        client = client,
                        onEdit = { editingClient = client },
                        onDelete = { showDeleteDialog = client }
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }

    // 추가/수정 다이얼로그
    if (showAddDialog || editingClient != null) {
        ClientDialog(
            client = editingClient,
            onDismiss = { showAddDialog = false; editingClient = null },
            onSave = { client ->
                if (editingClient != null) onUpdateClient(client) else onAddClient(client)
                showAddDialog = false; editingClient = null
            }
        )
    }

    // 삭제 다이얼로그
    showDeleteDialog?.let { client ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("클라이언트 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("'${client.displayName}'을(를) 삭제하시겠습니까?") },
            confirmButton = { Button(onClick = { onDeleteClient(client.id); showDeleteDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("삭제") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("취소") } }
        )
    }
}

@Composable
private fun ClientCard(client: Client, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(46.dp).background(Blue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, contentDescription = "회사", tint = Blue, modifier = Modifier.size(24.dp))
                }

                Column {
                    Text(client.company.ifBlank { client.name }, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (client.company.isNotBlank() && client.name.isNotBlank()) {
                        Text(client.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                    if (client.email.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.Email, contentDescription = "이메일", modifier = Modifier.size(12.dp), tint = Blue)
                            Text(client.email, color = Blue, fontSize = 12.sp)
                        }
                    }
                    if (client.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = "연락처", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(client.phone, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "더보기", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("수정") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "수정") },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("삭제", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.Red) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDialog(client: Client?, onDismiss: () -> Unit, onSave: (Client) -> Unit) {
    val isEdit = client != null
    var company by remember { mutableStateOf(client?.company ?: "") }
    var name by remember { mutableStateOf(client?.name ?: "") }
    var email by remember { mutableStateOf(client?.email ?: "") }
    var phone by remember { mutableStateOf(client?.phone ?: "") }
    var memo by remember { mutableStateOf(client?.memo ?: "") }
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "닫기", tint = MaterialTheme.colorScheme.onSurface) }
                    Text(if (isEdit) "클라이언트 수정" else "클라이언트 추가", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    TextButton(
                        onClick = {
                            if (company.isNotBlank()) {
                                onSave(Client(
                                    id = client?.id ?: currentTimeMillis().toString(),
                                    userId = client?.userId ?: "",
                                    company = company.trim(), name = name.trim(),
                                    email = email.trim(), phone = phone.trim(),
                                    memo = memo.trim(),
                                    createdAt = client?.createdAt ?: currentTimeMillis()
                                ))
                            }
                        },
                        enabled = company.isNotBlank()
                    ) {
                        Text(if (isEdit) "수정" else "추가", fontWeight = FontWeight.SemiBold,
                            color = if (company.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = company, onValueChange = { company = it }, label = { Text("회사명 *") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = "회사", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    OutlinedTextField(
                        value = name, onValueChange = { name = it }, label = { Text("담당자 이름") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "담당자", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    OutlinedTextField(
                        value = email, onValueChange = { email = it }, label = { Text("이메일") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "이메일", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        placeholder = { Text("example@company.com") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it }, label = { Text("연락처") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "연락처", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        placeholder = { Text("010-0000-0000") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    OutlinedTextField(
                        value = memo, onValueChange = { memo = it }, label = { Text("메모") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        minLines = 3, maxLines = 5,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}