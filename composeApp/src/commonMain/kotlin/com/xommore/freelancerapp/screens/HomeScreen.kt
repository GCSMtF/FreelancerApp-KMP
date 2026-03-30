package com.xommore.freelancerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.data.model.ProjectStatus
import com.xommore.freelancerapp.ui.components.*
import com.xommore.freelancerapp.ui.theme.*
import com.xommore.freelancerapp.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    onNavigateToProjects: (ProjectStatus?) -> Unit = {},
    viewModel: MainViewModel
) {
    val projects by viewModel.projects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val monthlyNetIncome = viewModel.getMonthlyNetIncome()
    val monthlyRevenue = viewModel.getMonthlyRevenue()
    val monthlyTax = viewModel.getMonthlyTax()
    val monthlyProps = viewModel.getMonthlyProps()
    val totalProjects = viewModel.getTotalProjectCount()

    // 상태별 프로젝트 수
    val inProgressCount = projects.count { it.status == ProjectStatus.IN_PROGRESS }
    val pendingCount = projects.count { it.status == ProjectStatus.PENDING }
    val paidCount = projects.count { it.status == ProjectStatus.PAID }

    val months = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        AppHeader()

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 연도 선택
                YearSelector(
                    selectedYear = selectedYear,
                    onYearChange = { viewModel.selectYear(it) }
                )

                // 월 선택 탭
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(months.size) { index ->
                        FilterChip(
                            onClick = { viewModel.selectMonth(index + 1) },
                            label = { Text(months[index], fontSize = 13.sp) },
                            selected = selectedMonth == index + 1,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // ==============================
                // 1. 프로젝트 현황 (최상단 강조)
                // ==============================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${selectedMonth}월 프로젝트", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            TextButton(onClick = { onNavigateToProjects(null) }) {
                                Text("전체보기 →", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 상태별 프로그레스바
                        if (totalProjects > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                if (paidCount > 0) {
                                    Box(modifier = Modifier.weight(paidCount.toFloat()).fillMaxHeight().background(StatusPaidText))
                                }
                                if (pendingCount > 0) {
                                    Box(modifier = Modifier.weight(pendingCount.toFloat()).fillMaxHeight().background(StatusPendingText))
                                }
                                if (inProgressCount > 0) {
                                    Box(modifier = Modifier.weight(inProgressCount.toFloat()).fillMaxHeight().background(StatusProgressText))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 상태별 카운트
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusCountItem("진행중", inProgressCount, StatusProgressText) { onNavigateToProjects(ProjectStatus.IN_PROGRESS) }
                            StatusCountItem("입금예정", pendingCount, StatusPendingText) { onNavigateToProjects(ProjectStatus.PENDING) }
                            StatusCountItem("입금완료", paidCount, StatusPaidText) { onNavigateToProjects(ProjectStatus.PAID) }
                        }
                    }
                }

                // 최근 프로젝트 목록
                if (projects.isNotEmpty()) {
                    projects.take(3).forEach { project ->
                        ProjectCard(
                            project = project,
                            onClick = { onNavigateToProjects(project.status) }
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📋", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("${selectedMonth}월 프로젝트가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }

                // ==============================
                // 2. 수익 현황 (하단)
                // ==============================
                MainCard {
                    Text("${selectedMonth}월 순수입", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(
                        formatCurrency(monthlyNetIncome),
                        color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("총 매출", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(formatCurrency(monthlyRevenue), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }
                        Column {
                            Text("세금", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("-${formatCurrency(monthlyTax)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }
                        Column {
                            Text("소품비", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("+${formatCurrency(monthlyProps)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun StatusCountItem(label: String, count: Int, color: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${count}건", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ProjectCard(project: Project, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(project.brand, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    StatusBadge(status = project.status)
                }
                Text(
                    "${project.workType.displayName} · ${formatDate(project.date)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(project.netIncome), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${project.cuts}컷 × ${formatCurrency(project.basePrice)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}