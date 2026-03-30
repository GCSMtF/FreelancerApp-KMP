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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.xommore.freelancerapp.data.model.*
import com.xommore.freelancerapp.data.currentTimeMillis
import com.xommore.freelancerapp.data.getYear
import com.xommore.freelancerapp.data.getMonth
import com.xommore.freelancerapp.ui.components.*
import com.xommore.freelancerapp.ui.theme.*
import com.xommore.freelancerapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: MainViewModel
) {
    val projects by viewModel.projects.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<Project?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var statusFilter by remember { mutableStateOf<ProjectStatus?>(null) }

    // 필터링된 프로젝트
    val filteredProjects = remember(projects, statusFilter) {
        if (statusFilter == null) projects
        else projects.filter { it.status == statusFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "프로젝트",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selectedYear}년 ${selectedMonth}월",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(onClick = { showFilterSheet = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "필터")
                }
                FilledIconButton(
                    onClick = { showAddDialog = true },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "추가", tint = Color.White)
                }
            }
        }

        // 필터 칩
        if (statusFilter != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { statusFilter = null },
                    label = { Text(statusFilter!!.displayName) },
                    selected = true,
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "필터 해제", modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        // 프로젝트 목록
        if (filteredProjects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = "빈 목록",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("프로젝트가 없습니다", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "+ 버튼을 눌러 프로젝트를 추가해보세요",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val allProjects by viewModel.allProjects.collectAsState()
                    val monthlyProjectCounts = remember(allProjects, selectedYear) {
                        (1..12).map { month ->
                            val count = allProjects.count { project ->
                                getYear(project.startDate) == selectedYear &&
                                        getMonth(project.startDate) == month
                            }
                            month to count.toLong()
                        }
                    }

                    MonthSelectorDropdown(
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        onYearChange = { viewModel.selectYear(it) },
                        onMonthChange = { viewModel.selectMonth(it) },
                        monthlyData = monthlyProjectCounts,
                        dataLabel = "건"
                    )
                }

                items(filteredProjects, key = { it.id }) { project ->
                    ProjectListCard(
                        project = project,
                        clients = clients,
                        onClick = { editingProject = project },
                        onStatusChange = { newStatus ->
                            viewModel.updateProjectStatus(project.id, newStatus)
                        },
                        onDelete = { viewModel.deleteProject(project.id) }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // 필터 바텀 시트
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text("상태 필터", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(16.dp))

                FilterOptionItem(text = "전체", selected = statusFilter == null, onClick = {
                    statusFilter = null; showFilterSheet = false
                })
                ProjectStatus.entries.forEach { status ->
                    FilterOptionItem(text = status.displayName, selected = statusFilter == status, onClick = {
                        statusFilter = status; showFilterSheet = false
                    })
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 프로젝트 추가/수정 다이얼로그
    if (showAddDialog || editingProject != null) {
        ProjectDialog(
            project = editingProject,
            clients = clients,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false; editingProject = null },
            onSave = { project, propItems ->
                if (editingProject != null) {
                    viewModel.updateProjectWithProps(project, propItems)
                } else {
                    viewModel.saveProjectWithProps(project, propItems)
                }
                showAddDialog = false; editingProject = null
            }
        )
    }
}

// =====================================================
// 필터 옵션 아이템
// =====================================================

@Composable
private fun FilterOptionItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 16.sp, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = "선택됨", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// =====================================================
// 프로젝트 카드 (목록용 — HomeScreen의 ProjectCard와 구분)
// =====================================================

@Composable
private fun ProjectListCard(
    project: Project,
    clients: List<Client>,
    onClick: () -> Unit,
    onStatusChange: (ProjectStatus) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val client = project.clientId?.let { id -> clients.find { it.id == id } }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Navy.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (project.workType) {
                                WorkType.STYLING -> Icons.Default.Style
                                WorkType.PLAN_STYLING -> Icons.Default.DesignServices
                            },
                            contentDescription = project.workType.displayName,
                            tint = Navy,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(project.brand, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "${formatDateShort(project.startDate)} - ${formatDateShort(project.endDate)} · ${project.workType.displayName}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (client != null) {
                            Text(client.displayName, fontSize = 12.sp, color = Blue)
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "메뉴", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        ProjectStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (project.status == status) Icon(Icons.Default.Check, contentDescription = "선택됨", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        else Spacer(modifier = Modifier.size(18.dp))
                                        Text(status.displayName)
                                    }
                                },
                                onClick = { onStatusChange(status); showMenu = false }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    Text("삭제", color = Color.Red)
                                }
                            },
                            onClick = { showMenu = false; showDeleteConfirm = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = project.status)
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatCurrency(project.netIncome), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${project.cuts}컷 × ${formatCurrency(project.basePrice)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("프로젝트 삭제") },
            text = { Text("'${project.brand}' 프로젝트를 삭제하시겠습니까?\n삭제된 프로젝트는 복구할 수 없습니다.") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteConfirm = false }) { Text("삭제", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") } }
        )
    }
}

// =====================================================
// 프로젝트 추가/수정 다이얼로그
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDialog(
    project: Project?,
    clients: List<Client>,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSave: (Project, List<PropItem>) -> Unit
) {
    val isEdit = project != null
    val focusManager = LocalFocusManager.current

    var brand by remember { mutableStateOf(project?.brand ?: "") }
    var workType by remember { mutableStateOf(project?.workType ?: WorkType.entries.first()) }
    var cuts by remember { mutableStateOf(project?.cuts?.toString() ?: "") }
    var basePrice by remember { mutableStateOf(project?.basePrice?.toString() ?: "") }
    var startDate by remember { mutableStateOf(project?.startDate ?: currentTimeMillis()) }
    var endDate by remember { mutableStateOf(project?.endDate ?: currentTimeMillis()) }
    var status by remember { mutableStateOf(project?.status ?: ProjectStatus.IN_PROGRESS) }
    var memo by remember { mutableStateOf(project?.memo ?: "") }
    var selectedClientId by remember { mutableStateOf(project?.clientId) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // 소품비 관련
    var showPropDialog by remember { mutableStateOf(false) }
    var propItems by remember { mutableStateOf<List<PropItem>>(emptyList()) }
    var isLoadingProps by remember { mutableStateOf(false) }

    // 기존 프로젝트의 소품비 로드
    LaunchedEffect(project?.id) {
        if (project != null) {
            isLoadingProps = true
            propItems = viewModel.getPropsForProjectOnce(project.id)
            isLoadingProps = false
        }
    }

    val totalPropsAmount = propItems.sumOf { it.amount }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = if (isEdit) "프로젝트 수정" else "프로젝트 추가",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = {
                            if (brand.isNotBlank() && cuts.isNotBlank() && basePrice.isNotBlank()) {
                                val selectedClient = selectedClientId?.let { id -> clients.find { it.id == id } }
                                val newProject = Project(
                                    id = project?.id ?: kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
                                    userId = project?.userId ?: "",
                                    brand = brand.trim(),
                                    workType = workType,
                                    cuts = cuts.toIntOrNull() ?: 0,
                                    basePrice = basePrice.toLongOrNull() ?: 0,
                                    startDate = startDate,
                                    endDate = endDate,
                                    status = status,
                                    memo = memo.trim(),
                                    createdAt = project?.createdAt ?: currentTimeMillis(),
                                    clientId = selectedClientId,
                                    clientName = selectedClient?.name ?: "",
                                    clientCompany = selectedClient?.company ?: "",
                                    clientEmail = selectedClient?.email ?: ""
                                )
                                onSave(newProject, propItems)
                            }
                        },
                        enabled = brand.isNotBlank() && cuts.isNotBlank() && basePrice.isNotBlank()
                    ) {
                        Text(
                            if (isEdit) "수정" else "추가",
                            fontWeight = FontWeight.SemiBold,
                            color = if (brand.isNotBlank() && cuts.isNotBlank() && basePrice.isNotBlank())
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // 폼
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 브랜드명
                    OutlinedTextField(
                        value = brand, onValueChange = { brand = it },
                        label = { Text("브랜드명 *") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = "브랜드", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // 클라이언트 선택
                    var clientExpanded by remember { mutableStateOf(false) }
                    val selectedClient = selectedClientId?.let { id -> clients.find { it.id == id } }

                    ExposedDropdownMenuBox(expanded = clientExpanded, onExpandedChange = { clientExpanded = it }) {
                        OutlinedTextField(
                            value = selectedClient?.displayName ?: "선택 안함",
                            onValueChange = {}, readOnly = true,
                            label = { Text("클라이언트") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = "클라이언트", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = clientExpanded, onDismissRequest = { clientExpanded = false }) {
                            DropdownMenuItem(text = { Text("선택 안함") }, onClick = { selectedClientId = null; clientExpanded = false })
                            clients.forEach { client ->
                                DropdownMenuItem(text = { Text(client.displayName) }, onClick = { selectedClientId = client.id; clientExpanded = false })
                            }
                        }
                    }

                    // 작업 유형
                    Text("작업 유형", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WorkType.entries.forEach { type ->
                            FilterChip(
                                onClick = { workType = type },
                                label = { Text(type.displayName) },
                                selected = workType == type,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Navy, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    // 컷수 및 단가
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = cuts, onValueChange = { cuts = it.filter { c -> c.isDigit() } },
                            label = { Text("컷수 *") }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) })
                        )
                        OutlinedTextField(
                            value = basePrice, onValueChange = { basePrice = it.filter { c -> c.isDigit() } },
                            label = { Text("단가 *") }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            suffix = { Text("원") }
                        )
                    }

                    // 예상 금액 표시
                    if (cuts.isNotBlank() && basePrice.isNotBlank()) {
                        val totalLabor = (cuts.toIntOrNull() ?: 0) * (basePrice.toLongOrNull() ?: 0)
                        val tax = (totalLabor * 0.033).toLong()
                        val netIncome = totalLabor - tax

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("총 인건비", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatCurrency(totalLabor), fontWeight = FontWeight.Medium)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("세금 (3.3%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("-${formatCurrency(tax)}", color = NegativeRed)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("실수령액", fontWeight = FontWeight.Bold)
                                    Text(formatCurrency(netIncome), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // 소품비 섹션
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("소품비", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            if (propItems.isNotEmpty()) {
                                Text("${propItems.size}개 항목 · ${formatCurrency(totalPropsAmount)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Button(
                            onClick = { showPropDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (propItems.isEmpty()) MaterialTheme.colorScheme.primary else StatusPaidText
                            )
                        ) {
                            Icon(if (propItems.isEmpty()) Icons.Default.Add else Icons.Default.Edit, contentDescription = "소품비", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (propItems.isEmpty()) "소품비 추가" else "소품비 수정")
                        }
                    }

                    // 소품비 미리보기
                    if (propItems.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                propItems.take(3).forEach { prop ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(prop.name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text(formatCurrency(prop.amount), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                if (propItems.size > 3) {
                                    Text("외 ${propItems.size - 3}개 항목", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 날짜 선택
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = formatDate(startDate), onValueChange = {},
                            label = { Text("시작일") },
                            modifier = Modifier.weight(1f).clickable { showStartDatePicker = true },
                            shape = RoundedCornerShape(12.dp), readOnly = true, enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        OutlinedTextField(
                            value = formatDate(endDate), onValueChange = {},
                            label = { Text("종료일") },
                            modifier = Modifier.weight(1f).clickable { showEndDatePicker = true },
                            shape = RoundedCornerShape(12.dp), readOnly = true, enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.DateRange, contentDescription = "시작일 선택", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp)); Text("시작일")
                        }
                        OutlinedButton(onClick = { showEndDatePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.DateRange, contentDescription = "종료일 선택", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp)); Text("종료일")
                        }
                    }

                    // 상태 선택
                    Text("상태", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProjectStatus.entries.forEach { s ->
                            FilterChip(
                                onClick = { status = s },
                                label = { Text(s.displayName) },
                                selected = status == s,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (s) {
                                        ProjectStatus.PAID -> StatusPaidText
                                        ProjectStatus.PENDING -> StatusPendingText
                                        ProjectStatus.IN_PROGRESS -> StatusProgressText
                                    },
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // 메모
                    OutlinedTextField(
                        value = memo, onValueChange = { memo = it },
                        label = { Text("메모") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        minLines = 3, maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    // DatePicker (시작일)
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { startDate = it }; showStartDatePicker = false }) { Text("확인") } },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState) }
    }

    // DatePicker (종료일)
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { endDate = it }; showEndDatePicker = false }) { Text("확인") } },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState) }
    }

    // 소품비 입력 다이얼로그
    if (showPropDialog) {
        PropItemInputDialog(
            projectId = project?.id ?: kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
            existingProps = propItems,
            onSave = { newProps -> propItems = newProps; showPropDialog = false },
            onDismiss = { showPropDialog = false }
        )
    }
}