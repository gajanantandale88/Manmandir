package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Payment
import com.example.data.model.ReceiptEntity
import com.example.data.model.UserRole
import com.example.ui.components.DigitalReceiptDialog
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

@Composable
fun ReceiptsScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val allReceipts by viewModel.allReceipts.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    val residentRoom by viewModel.currentResidentRoom.collectAsState()
    val selectedReceipt by viewModel.selectedReceipt.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }

    val displayReceipts = remember(allReceipts, role, residentRoom, searchQuery) {
        val baseList = if (role == UserRole.RESIDENT) {
            allReceipts.filter { it.roomNumber == residentRoom }
        } else {
            allReceipts
        }

        if (searchQuery.isBlank()) {
            baseList.sortedByDescending { it.receiptNumber }
        } else {
            val q = searchQuery.trim().lowercase()
            baseList.filter {
                it.receiptNumber.lowercase().contains(q) ||
                        it.roomNumber.toString().contains(q) ||
                        it.residentName.lowercase().contains(q) ||
                        it.maintenanceMonth.lowercase().contains(q) ||
                        it.transactionRef.lowercase().contains(q)
            }.sortedByDescending { it.receiptNumber }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SocietyBg)
    ) {
        // Header
        Surface(
            color = SocietyNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (role == UserRole.RESIDENT) "My Maintenance Receipts" else "Digital Receipts Repository",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (role == UserRole.RESIDENT) "Official receipts for Room $residentRoom" else "Auditable digital payment vouchers",
                            fontSize = 11.sp,
                            color = SocietyGoldLight
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SocietyGold
                        ) {
                            Text(
                                text = "Room DB: ${allPayments.size} Payments",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocietyNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (role != UserRole.RESIDENT) {
                            Button(
                                onClick = { showRecordPaymentDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SocietyGold,
                                    contentColor = SocietyNavyDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("record_payment_header_btn")
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Record Payment", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        Box(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by receipt no, room no, resident or txn ref...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SocietySlate) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("search_receipts_input")
            )
        }

        // Receipts List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (displayReceipts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No digital receipts found.",
                            color = SocietySlate,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(displayReceipts, key = { it.id }) { receipt ->
                    ReceiptCardItem(
                        receipt = receipt,
                        onClick = { viewModel.setSelectedReceipt(receipt) }
                    )
                }
            }
        }
    }

    // Modal Receipt Viewer
    if (selectedReceipt != null) {
        DigitalReceiptDialog(
            receipt = selectedReceipt!!,
            onDismiss = { viewModel.setSelectedReceipt(null) },
            onShare = {
                viewModel.showSnackbar("Digital Receipt ${selectedReceipt!!.receiptNumber} shared via WhatsApp / Email!")
            },
            onDownload = {
                viewModel.showSnackbar("Downloaded official PDF receipt: ${selectedReceipt!!.receiptNumber}.pdf")
            }
        )
    }

    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            onDismiss = { showRecordPaymentDialog = false },
            onSave = { newPayment ->
                viewModel.recordPayment(newPayment)
                showRecordPaymentDialog = false
            }
        )
    }
}

@Composable
fun ReceiptCardItem(
    receipt: ReceiptEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SocietySuccessContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = SocietySuccess,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = receipt.receiptNumber,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = SocietyNavy
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SocietyNavy
                        ) {
                            Text(
                                text = "Room ${receipt.roomNumber}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = receipt.residentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SocietyNavyDark
                    )
                    Text(
                        text = "${receipt.maintenanceMonth} • ${receipt.paymentMethod}",
                        fontSize = 11.sp,
                        color = SocietySlate
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${receipt.totalAmountReceived.toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SocietySuccess
                )
                Text(
                    text = receipt.date,
                    fontSize = 10.sp,
                    color = SocietySlate
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SocietyNavy)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SocietyNavy, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}
