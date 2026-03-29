package com.xommore.freelancerapp.ui.components

import com.xommore.freelancerapp.data.getYear
import com.xommore.freelancerapp.data.getMonth
import com.xommore.freelancerapp.data.getDay

/**
 * 금액 포맷팅 (예: 4544900 → "4,544,900원")
 */
fun formatCurrency(amount: Long): String {
    val isNegative = amount < 0
    val absStr = kotlin.math.abs(amount).toString()
    val formatted = buildString {
        absStr.reversed().forEachIndexed { index, c ->
            if (index > 0 && index % 3 == 0) append(',')
            append(c)
        }
    }.reversed()
    return if (isNegative) "-${formatted}원" else "${formatted}원"
}

/**
 * 타임스탬프를 "yyyy-MM-dd" 형식 문자열로 변환
 */
fun formatDate(timestamp: Long): String {
    val year = getYear(timestamp)
    val month = getMonth(timestamp)
    val day = getDay(timestamp)
    val m = month.toString().padStart(2, '0')
    val d = day.toString().padStart(2, '0')
    return "$year-$m-$d"
}

/**
 * 타임스탬프를 "M월 d일" 형식으로 변환
 */
fun formatDateShort(timestamp: Long): String {
    val month = getMonth(timestamp)
    val day = getDay(timestamp)
    return "${month}월 ${day}일"
}