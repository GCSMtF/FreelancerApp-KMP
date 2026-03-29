package com.xommore.freelancerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.model.Project
import com.xommore.freelancerapp.ui.components.*
import com.xommore.freelancerapp.ui.theme.*
import com.xommore.freelancerapp.viewmodel.MainViewModel

/**
 * 홈 화면
 * - 월별 수익 현황 대시보드
 * - 월 선택 탭
 * - 최근 프로젝트 목록
 */
@Composable
fun HomeScreen(
    onNavigateToProjects: () -> Unit = {},
    viewModel: MainViewModel
) {
    val projects by viewModel.projects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val monthlyNetIncome = viewModel.getMonthlyNetIncome()
    val monthlyRevenue = viewModel.getMonthlyRevenue()
    val monthlyTax = viewModel.getMonthlyTax()
    val totalProjects = viewModel.getTotalProjectCount()
    val paidProjects = viewModel.getPaidProjectCount()

    val months = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 상단 헤더
        AppHeader()

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 연도/월 헤더
                Column {
                    YearSelector(
                        selectedYear = selectedYear,
                        onYearChange = { viewModel.selectYear(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "수익 현황",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 월 선택 탭 (가로 스크롤)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(months.size) { index ->
                        FilterChip(
                            onClick = { viewModel.selectMonth(index + 1) },
                            label = { Text(months[index], fontSize = 13.sp) },
                            selected = selectedMonth == index + 1,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // 메인 카드 (순수입)
                MainCard {
                    Text(
                        text = "${selectedMonth}월 순수입",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = formatCurrency(monthlyNetIncome),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "총 매출",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = formatCurrency(monthlyRevenue),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "세금 (3.3%)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "-${formatCurrency(monthlyTax)}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // 통계 카드 그리드
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = "📋",
                        label = "총 프로젝트",
                        value = "${totalProjects}건",
                        iconBackgroundColor = StatusProgressBg,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = "✓",
                        label = "입금 완료",
                        value = "${paidProjects}건",
                        iconBackgroundColor = StatusPaidBg,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 최근 프로젝트 섹션 헤더
                SectionHeader(
                    title = "${selectedMonth}월 프로젝트",
                    actionText = "전체보기 →",
                    onActionClick = onNavigateToProjects
                )

                // 프로젝트 목록
                if (projects.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📋", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${selectedMonth}월 프로젝트가 없습니다",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    projects.take(3).forEach { project ->
                        ProjectCard(project = project)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * 프로젝트 카드 컴포넌트
 */
@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = project.brand,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusBadge(status = project.status)
                }
                Text(
                    text = "${project.workType.displayName} · ${formatDate(project.date)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCurrency(project.netIncome),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${project.cuts}컷 × ${formatCurrency(project.basePrice)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}