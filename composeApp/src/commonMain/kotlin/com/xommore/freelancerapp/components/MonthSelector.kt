package com.xommore.freelancerapp.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * 월 선택 드롭다운 버튼
 */
@Composable
fun MonthSelectorDropdown(
    selectedYear: Int,
    selectedMonth: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    monthlyData: List<Pair<Int, Long>> = emptyList(),
    dataLabel: String = "원",
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val currentMonthData = monthlyData.find { it.first == selectedMonth }?.second ?: 0L

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "월 선택",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "${selectedYear}년 ${selectedMonth}월",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (currentMonthData > 0) {
                        Text(
                            text = if (dataLabel == "건") "${currentMonthData}건"
                            else formatCurrency(currentMonthData),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "데이터 없음",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "펼치기",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showDialog) {
        MonthPickerDialog(
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            monthlyData = monthlyData,
            dataLabel = dataLabel,
            onYearChange = onYearChange,
            onMonthSelect = { month ->
                onMonthChange(month)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * 월 선택 다이얼로그
 */
@Composable
private fun MonthPickerDialog(
    selectedYear: Int,
    selectedMonth: Int,
    monthlyData: List<Pair<Int, Long>>,
    dataLabel: String,
    onYearChange: (Int) -> Unit,
    onMonthSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "월 선택",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onYearChange(selectedYear - 1) }) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "이전 연도",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "${selectedYear}년",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = { onYearChange(selectedYear + 1) }) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "다음 연도",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    items((1..12).toList()) { month ->
                        val monthData = monthlyData.find { it.first == month }?.second ?: 0L
                        val isSelected = month == selectedMonth
                        val hasData = monthData > 0

                        MonthCell(
                            month = month,
                            data = monthData,
                            dataLabel = dataLabel,
                            isSelected = isSelected,
                            hasData = hasData,
                            onClick = { onMonthSelect(month) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("닫기", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * 월 셀 컴포넌트
 */
@Composable
private fun MonthCell(
    month: Int,
    data: Long,
    dataLabel: String,
    isSelected: Boolean,
    hasData: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        hasData -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = when {
        isSelected -> Color.White
        hasData -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    val subTextColor = when {
        isSelected -> Color.White.copy(alpha = 0.8f)
        hasData -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isSelected) null else BorderStroke(
            1.dp,
            if (hasData) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${month}월",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = textColor
            )

            if (hasData) {
                Text(
                    text = if (dataLabel == "건") "${data}건"
                    else "${data / 10000}만",
                    fontSize = 10.sp,
                    color = subTextColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}