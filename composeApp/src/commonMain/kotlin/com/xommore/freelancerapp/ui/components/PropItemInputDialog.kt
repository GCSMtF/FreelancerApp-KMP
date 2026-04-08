package com.xommore.freelancerapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xommore.freelancerapp.data.model.PropItem
import com.xommore.freelancerapp.ui.theme.*
import com.xommore.freelancerapp.service.OcrScanButtons

/**
 * 편집용 소품비 아이템
 */
data class EditablePropItem(
    val id: String = com.xommore.freelancerapp.data.currentTimeMillis().toString(),
    val name: String = "",
    val amount: String = "",
    val memo: String = "",
    val receiptUri: String? = null
)

/**
 * 소품비 입력 다이얼로그 (commonMain)
 * OCR/카메라 기능은 Android 전용 (Step 10에서 추가)
 */
@Composable
fun PropItemInputDialog(
    projectId: String,
    existingProps: List<PropItem>,
    onSave: (List<PropItem>) -> Unit,
    onDismiss: () -> Unit
) {
    // 편집 가능한 아이템 목록으로 변환
    var propItems by remember {
        mutableStateOf(
            if (existingProps.isEmpty()) {
                mutableListOf(EditablePropItem())
            } else {
                existingProps.map { prop ->
                    EditablePropItem(
                        id = prop.id,
                        name = prop.name,
                        amount = prop.amount.toString(),
                        memo = prop.memo,
                        receiptUri = prop.receiptUri
                    )
                }.toMutableList()
            }
        )
    }

    // 총 금액 계산
    val totalAmount = propItems.sumOf { it.amount.toLongOrNull() ?: 0L }

    // OCR 스캔 상태
    var isScanning by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "소품비 입력",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = {
                            val validItems = propItems
                                .filter { it.name.isNotBlank() && (it.amount.toLongOrNull() ?: 0L) > 0 }
                                .map { editableItem ->
                                    PropItem(
                                        id = editableItem.id,
                                        projectId = projectId,
                                        name = editableItem.name,
                                        amount = editableItem.amount.toLongOrNull() ?: 0L,
                                        memo = editableItem.memo,
                                        receiptUri = editableItem.receiptUri
                                    )
                                }
                            onSave(validItems)
                        }
                    ) {
                        Text(
                            "저장",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider()

                // OCR 스캔 버튼 (플랫폼별)
                OcrScanButtons(
                    isScanning = isScanning,
                    onScanResult = { newItems ->
                        propItems = propItems.toMutableList().apply {
                            // 마지막 빈 항목 제거
                            if (lastOrNull()?.name?.isBlank() == true && lastOrNull()?.amount?.isBlank() == true) {
                                removeAt(lastIndex)
                            }
                            addAll(newItems)
                        }
                    },
                    onScanningChange = { isScanning = it }
                )

                // 소품비 목록
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(propItems) { index, item ->
                        PropItemCard(
                            item = item,
                            index = index + 1,
                            onNameChange = { newName ->
                                propItems = propItems.toMutableList().apply {
                                    this[index] = this[index].copy(name = newName)
                                }
                            },
                            onAmountChange = { newAmount ->
                                propItems = propItems.toMutableList().apply {
                                    this[index] = this[index].copy(amount = newAmount.filter { it.isDigit() })
                                }
                            },
                            onMemoChange = { newMemo ->
                                propItems = propItems.toMutableList().apply {
                                    this[index] = this[index].copy(memo = newMemo)
                                }
                            },
                            onDelete = {
                                if (propItems.size > 1) {
                                    propItems = propItems.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            },
                            canDelete = propItems.size > 1
                        )
                    }

                    // 항목 추가 버튼
                    item {
                        OutlinedButton(
                            onClick = {
                                propItems = propItems.toMutableList().apply {
                                    add(EditablePropItem())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "추가",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("항목 추가")
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                // 하단 합계
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "소품비 합계",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${propItems.count { it.name.isNotBlank() && (it.amount.toLongOrNull() ?: 0) > 0 }}개 항목",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = formatCurrency(totalAmount),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 소품비 항목 카드
 */
@Composable
private fun PropItemCard(
    item: EditablePropItem,
    index: Int,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "소품비 항목",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 입력 필드들
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = onNameChange,
                    label = { Text("소품명") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Right) }
                    )
                )

                OutlinedTextField(
                    value = item.amount,
                    onValueChange = onAmountChange,
                    label = { Text("금액") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    suffix = { Text("원") }
                )
            }

            // 메모
            OutlinedTextField(
                value = item.memo,
                onValueChange = onMemoChange,
                label = { Text("메모 (선택)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // 영수증 이미지 표시 (있는 경우)
            if (item.receiptUri != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "영수증",
                        tint = StatusPaidText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "영수증 첨부됨",
                        fontSize = 12.sp,
                        color = StatusPaidText
                    )
                }
            }
        }
    }
}