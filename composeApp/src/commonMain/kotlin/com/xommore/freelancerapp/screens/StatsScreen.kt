package com.xommore.freelancerapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xommore.freelancerapp.data.getYear
import com.xommore.freelancerapp.data.getMonth
import com.xommore.freelancerapp.ui.components.*
import com.xommore.freelancerapp.ui.theme.*
import com.xommore.freelancerapp.viewmodel.MainViewModel

@Composable
fun StatsScreen(
    viewModel: MainViewModel
) {
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()

    // 소품비 데이터
    var propsMap by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    LaunchedEffect(projects) {
        if (projects.isNotEmpty()) {
            val propsData = viewModel.getPropsForProjects(projects.map { it.id })
            propsMap = propsData.mapValues { (_, props) -> props.sumOf { it.amount } }
        } else {
            propsMap = emptyMap()
        }
    }

    val monthlyLabor = projects.sumOf { it.totalLabor }
    val monthlyTax = projects.sumOf { it.tax }
    val monthlyNetIncome = projects.sumOf { it.netIncome }
    val monthlyProps = propsMap.values.sum()

    val monthlyData = remember(allProjects, selectedYear) {
        (1..12).map { month ->
            val mp = allProjects.filter { getYear(it.startDate) == selectedYear && getMonth(it.startDate) == month }
            month to mp.sumOf { it.netIncome }
        }
    }

    val brandRevenue = remember(projects) {
        projects.groupBy { it.brand }
            .mapValues { (_, list) -> list.sumOf { it.netIncome } }
            .toList().sortedByDescending { it.second }.toMap()
    }

    val maxMonthlyAmount = monthlyData.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L
    val maxBrandRevenue = brandRevenue.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("통계", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthSelectorDropdown(
                    selectedYear = selectedYear, selectedMonth = selectedMonth,
                    onYearChange = { viewModel.selectYear(it) }, onMonthChange = { viewModel.selectMonth(it) },
                    monthlyData = monthlyData, dataLabel = "원"
                )
            }

            // 월별 수익 차트
            item {
                WhiteCard {
                    Text("월별 수익 추이", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("막대를 터치하여 월 선택", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        monthlyData.forEach { (month, amount) ->
                            val isSelected = month == selectedMonth
                            Column(
                                modifier = Modifier.weight(1f).clickable { viewModel.selectMonth(month) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isSelected && amount > 0) {
                                    Text("${amount / 10000}만", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .height(if (amount > 0) (110.dp * (amount.toFloat() / maxMonthlyAmount)) else 4.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (amount > 0)
                                                if (isSelected) Brush.verticalGradient(listOf(Blue, Navy))
                                                else Brush.verticalGradient(listOf(Blue.copy(alpha = 0.4f), Navy.copy(alpha = 0.4f)))
                                            else Brush.verticalGradient(listOf(CardBorder, CardBorder))
                                        )
                                )
                                Text(
                                    "$month", fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${selectedMonth}월 상세", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("프로젝트 ${projects.size}건", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 상세 카드 그리드
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailStatCard("💰", "총 인건비", formatCurrency(monthlyLabor), Color(0xFFFFF3E0), Modifier.weight(1f))
                    DetailStatCard("📊", "세금", "-${formatCurrency(monthlyTax)}", Color(0xFFE3F2FD), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailStatCard("✅", "실수령액", formatCurrency(monthlyNetIncome), Color(0xFFE8F5E9), Modifier.weight(1f))
                    DetailStatCard("🎁", "소품비", formatCurrency(monthlyProps), Color(0xFFFCE4EC), Modifier.weight(1f))
                }
            }

            // 최종 정산
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${selectedMonth}월 최종 정산", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text(formatCurrency(monthlyNetIncome + monthlyProps), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // 브랜드별 수익
            item {
                WhiteCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("브랜드별 수익", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${selectedMonth}월", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (brandRevenue.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📊", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${selectedMonth}월에는 데이터가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                        }
                    } else {
                        brandRevenue.forEach { (name, amount) ->
                            val pct = (amount.toFloat() / maxBrandRevenue * 100).toInt()
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(name, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(formatCurrency(amount), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(CardBorder)) {
                                    Box(modifier = Modifier.fillMaxWidth(pct / 100f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(Blue, Navy))))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun DetailStatCard(icon: String, label: String, value: String, iconBgColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(modifier = Modifier.size(40.dp).background(iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
        }
    }
}