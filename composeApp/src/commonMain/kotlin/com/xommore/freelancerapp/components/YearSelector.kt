package com.xommore.freelancerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 연도 선택 컴포넌트 (좌우 화살표 방식)
 */
@Composable
fun YearSelector(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minYear: Int = 2020,
    maxYear: Int = 2030
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = {
                if (selectedYear > minYear) {
                    onYearChange(selectedYear - 1)
                }
            },
            enabled = selectedYear > minYear,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "이전 연도",
                tint = if (selectedYear > minYear)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }

        Text(
            text = "${selectedYear}년",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        IconButton(
            onClick = {
                if (selectedYear < maxYear) {
                    onYearChange(selectedYear + 1)
                }
            },
            enabled = selectedYear < maxYear,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "다음 연도",
                tint = if (selectedYear < maxYear)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * 연도 선택 컴포넌트 (드롭다운 방식)
 */
@Composable
fun YearDropdown(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minYear: Int = 2020,
    maxYear: Int = 2030
) {
    var expanded by remember { mutableStateOf(false) }
    val years = (minYear..maxYear).toList()

    Box(modifier = modifier) {
        Card(
            modifier = Modifier.clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedYear}년",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "연도 선택",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${year}년",
                            fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                            color = if (year == selectedYear)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onYearChange(year)
                        expanded = false
                    }
                )
            }
        }
    }
}