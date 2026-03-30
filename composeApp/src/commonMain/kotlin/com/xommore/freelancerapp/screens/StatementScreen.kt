package com.xommore.freelancerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.getYear
import com.xommore.freelancerapp.data.getMonth
import com.xommore.freelancerapp.ui.components.*
import com.xommore.freelancerapp.ui.theme.*
import com.xommore.freelancerapp.viewmodel.MainViewModel
import com.xommore.freelancerapp.service.PdfRequest
import com.xommore.freelancerapp.service.PdfExportButton
import com.xommore.freelancerapp.service.ClipboardCopyButton
import com.xommore.freelancerapp.service.EmailSendButton

/**
 * 정산서 화면 (commonMain)
 * PDF 생성, 이메일 발송, 클립보드 복사 등은 Android 전용 (Step 10에서 추가)
 */
@Composable
fun StatementScreen(
    viewModel: MainViewModel
) {
    val projects by viewModel.projects.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    // 소품비 데이터
    var propsMap by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var propItemsMap by remember { mutableStateOf<Map<String, List<com.xommore.freelancerapp.data.model.PropItem>>>(emptyMap()) }
    LaunchedEffect(projects) {
        if (projects.isNotEmpty()) {
            val propsData = viewModel.getPropsForProjects(projects.map { it.id })
            propItemsMap = propsData
            propsMap = propsData.mapValues { (_, props) -> props.sumOf { it.amount } }
        }
    }

    // 정산 금액 계산
    val totalLabor = projects.sumOf { it.totalLabor }
    val totalTax = projects.sumOf { it.tax }
    val netIncome = projects.sumOf { it.netIncome }
    val totalProps = propsMap.values.sum()
    val finalAmount = netIncome + totalProps

    // 정산 기간 문자열
    val startDateStr = "$selectedYear-${selectedMonth.toString().padStart(2, '0')}-01"
    val endDateStr = remember(selectedYear, selectedMonth) {
        val lastDay = when (selectedMonth) {
            2 -> if (selectedYear % 4 == 0 && (selectedYear % 100 != 0 || selectedYear % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
        "$selectedYear-${selectedMonth.toString().padStart(2, '0')}-$lastDay"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // 헤더
        Box(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("정산서", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${selectedYear}년 ${selectedMonth}월", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
                YearDropdown(selectedYear = selectedYear, onYearChange = { viewModel.selectYear(it) })
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 월 선택
            item {
                val allProjects by viewModel.allProjects.collectAsState()
                val monthlyAmounts = remember(allProjects, selectedYear) {
                    (1..12).map { month ->
                        val mp = allProjects.filter { getYear(it.startDate) == selectedYear && getMonth(it.startDate) == month }
                        month to mp.sumOf { it.netIncome }
                    }
                }
                MonthSelectorDropdown(
                    selectedYear = selectedYear, selectedMonth = selectedMonth,
                    onYearChange = { viewModel.selectYear(it) }, onMonthChange = { viewModel.selectMonth(it) },
                    monthlyData = monthlyAmounts, dataLabel = "원"
                )
            }

            // 정산 기간
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("정산 기간", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = startDateStr, onValueChange = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), readOnly = true)
                            Text("~", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(value = endDateStr, onValueChange = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), readOnly = true)
                        }
                    }
                }
            }

            // 정산 요약 카드
            item {
                MainCard {
                    Text("${selectedMonth}월 최종 정산 금액", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(formatCurrency(finalAmount), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("인건비", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    StatementSummaryRow("총 인건비", totalLabor, false)
                    StatementSummaryRow("세금 (3.3%)", totalTax, true)
                    StatementSummaryRow("실수령 인건비", netIncome, false)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("소품비 (클라이언트 청구)", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    StatementSummaryRow("소품비 합계", totalProps, false)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("최종 정산 금액", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(formatCurrency(finalAmount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            // 상세 내역 헤더
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("상세 내역", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${projects.size}건", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }

            // 상세 내역 목록
            if (projects.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("이 달에는 프로젝트가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(projects) { project ->
                    StatementProjectCard(project = project, propAmount = propsMap[project.id] ?: 0L)
                }
            }

            // 내보내기 버튼
            item {
                val userProfile by viewModel.userProfile.collectAsState()

                // 정산서 텍스트 생성 (복사용)
                val statementText = remember(projects, propsMap, selectedYear, selectedMonth, netIncome, totalProps) {
                    buildString {
                        appendLine("=== 프리랜서 정산서 ===")
                        appendLine("정산 기간: $startDateStr ~ $endDateStr")
                        appendLine()
                        appendLine("[ 인건비 ]")
                        appendLine("총 인건비: ${formatCurrency(totalLabor)}")
                        appendLine("세금 (3.3%): -${formatCurrency(totalTax)}")
                        appendLine("실수령 인건비: ${formatCurrency(netIncome)}")
                        appendLine()
                        appendLine("[ 소품비 (청구) ]")
                        appendLine("소품비 합계: ${formatCurrency(totalProps)}")
                        appendLine()
                        appendLine("==================")
                        appendLine("최종 정산 금액: ${formatCurrency(finalAmount)}")
                        appendLine("==================")
                        appendLine()
                        appendLine("--- 상세 내역 ---")
                        projects.forEach { project ->
                            val propAmount = propsMap[project.id] ?: 0L
                            if (propAmount > 0) {
                                appendLine("${project.brand}: ${formatCurrency(project.netIncome)} + 소품비 ${formatCurrency(propAmount)} = ${formatCurrency(project.netIncome + propAmount)}")
                            } else {
                                appendLine("${project.brand}: ${formatCurrency(project.netIncome)}")
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClipboardCopyButton(
                                text = statementText,
                                onCopied = { /* 복사 완료 */ }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            PdfExportButton(
                                enabled = projects.isNotEmpty(),
                                pdfRequest = PdfRequest(
                                    year = selectedYear,
                                    month = selectedMonth,
                                    projects = projects,
                                    userProfile = userProfile,
                                    propsMap = propsMap,
                                    propItems = propItemsMap
                                ),
                                onResult = { success, message ->
                                    // 결과 처리
                                }
                            )
                        }
                    }

                    EmailSendButton(
                        projects = projects,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        propsMap = propsMap
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun StatementSummaryRow(label: String, amount: Long, isNegative: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.8f))
        Text(
            if (isNegative) "-${formatCurrency(amount)}" else formatCurrency(amount),
            color = if (isNegative) NegativeRed else Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatementProjectCard(project: Project, propAmount: Long = 0) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(project.brand, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(formatCurrency(project.netIncome + propAmount), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                "${formatDateShort(project.startDate)}~${formatDateShort(project.endDate)} · ${project.workType.displayName}",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)
            )
            if (propAmount > 0) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("인건비: ${formatCurrency(project.netIncome)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("소품비: ${formatCurrency(propAmount)}", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            if (project.clientCompany.isNotBlank() || project.clientName.isNotBlank()) {
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (project.clientEmail.isNotBlank()) {
                        Icon(Icons.Default.Email, contentDescription = "이메일", modifier = Modifier.size(12.dp), tint = Blue)
                    }
                    Text(
                        buildString {
                            if (project.clientCompany.isNotBlank()) append(project.clientCompany)
                            if (project.clientCompany.isNotBlank() && project.clientName.isNotBlank()) append(" · ")
                            if (project.clientName.isNotBlank()) append(project.clientName)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp
                    )
                }
            }
        }
    }
}