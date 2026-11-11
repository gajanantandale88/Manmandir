package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MaintenancePayment
import com.example.data.model.MaintenancePaymentStatus
import com.example.data.model.ResidentWithMaintenancePayments
import com.example.ui.theme.*

/**
 * Dialog displaying a Resident's linked [MaintenancePayment] records from the Room Database,
 * allowing authorized committee members to track or log new unit payment entries directly linked via residentId.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentMaintenanceLedgerDialog(
    residentWithPayments: ResidentWithMaintenancePayments,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onAddPayment: (MaintenancePayment) -> Unit,
    onMarkPaid: (MaintenancePayment) -> Unit
) {
    val resident = residentWithPayments.resident
    val payments = remember(residentWithPayments.maintenancePayments) {
        residentWithPayments.maintenancePayments.sortedByDescending { it.id }
    }

    var showNewEntryForm by remember { mutableStateOf(false) }
    var billingMonth by remember { mutableStateOf("November 2026") }
    var amountText by remember { mutableStateOf(if (resident.residentType == "TENANT") "750" else "500") }
    var dueDateText by remember { mutableStateOf("10-Nov-2026") }
    var paymentDateText by remember { mutableStateOf("02-Nov-2026") }
    var selectedStatus by remember { mutableStateOf(MaintenancePaymentStatus.PAID) }
    var paymentMethod by remember { mutableStateOf("UPI") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .testTag("unit_maintenance_ledger_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietyNavy,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = resident.flatNumber,
                                    color = SocietyGoldLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = resident.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SocietyNavyDark
                            )
                            Text(
                                text = "Linked Resident #${resident.id} • ${resident.residentType} • ${payments.size} Records",
                                fontSize = 11.sp,
                                color = SocietySlate
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_ledger_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close ledger", tint = SocietySlate)
                    }
                }

                HorizontalDivider(color = SocietyCardBorder)

                if (canEdit) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showNewEntryForm) "New Linked MaintenancePayment" else "Unit Payment History (Room DB)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = SocietyNavy
                        )
                        FilledTonalButton(
                            onClick = { showNewEntryForm = !showNewEntryForm },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("toggle_new_maintenance_payment_btn")
                        ) {
                            Icon(
                                imageVector = if (showNewEntryForm) Icons.Default.List else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showNewEntryForm) "View History" else "Track Payment", fontSize = 11.sp)
                        }
                    }
                }

                if (showNewEntryForm) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SocietySurfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = billingMonth,
                                    onValueChange = { billingMonth = it },
                                    label = { Text("Billing Month", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("mp_billing_month_input")
                                )
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { amountText = it },
                                    label = { Text("Amount (₹)", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mp_amount_input")
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = dueDateText,
                                    onValueChange = { dueDateText = it },
                                    label = { Text("Due Date", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = paymentDateText,
                                    onValueChange = { paymentDateText = it },
                                    label = { Text("Payment Date", fontSize = 11.sp) },
                                    enabled = selectedStatus == MaintenancePaymentStatus.PAID || selectedStatus == MaintenancePaymentStatus.PARTIAL,
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mp_payment_date_input")
                                )
                            }

                            Text("Payment Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SocietyNavy)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    MaintenancePaymentStatus.PAID,
                                    MaintenancePaymentStatus.PENDING,
                                    MaintenancePaymentStatus.OVERDUE
                                ).forEach { st ->
                                    FilterChip(
                                        selected = selectedStatus == st,
                                        onClick = { selectedStatus = st },
                                        label = { Text(st.name, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val amt = amountText.toDoubleOrNull() ?: 500.0
                                    val isSettled = selectedStatus == MaintenancePaymentStatus.PAID
                                    onAddPayment(
                                        MaintenancePayment(
                                            residentId = resident.id,
                                            unitNumber = resident.flatNumber,
                                            billingMonth = billingMonth.trim(),
                                            amount = amt,
                                            penaltyAmount = 0.0,
                                            amountPaid = if (isSettled) amt else 0.0,
                                            dueDate = dueDateText.trim(),
                                            paymentDate = if (isSettled) paymentDateText.trim() else null,
                                            status = selectedStatus,
                                            paymentMethod = if (isSettled) paymentMethod else "",
                                            transactionReference = if (isSettled) "TXN/${resident.flatNumber}/${(1000..9999).random()}" else "",
                                            receiptNumber = if (isSettled) "MM/26-27/${(1000..9999).random()}" else "",
                                            remarks = "Linked to Resident #${resident.id}"
                                        )
                                    )
                                    showNewEntryForm = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_maintenance_payment_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Linked MaintenancePayment")
                            }
                        }
                    }
                } else {
                    if (payments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No MaintenancePayment entries recorded for Unit ${resident.flatNumber}.",
                                fontSize = 12.sp,
                                color = SocietySlate
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(payments, key = { it.id }) { mp ->
                                MaintenancePaymentRowCard(
                                    payment = mp,
                                    canEdit = canEdit,
                                    onMarkPaid = { onMarkPaid(mp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenancePaymentRowCard(
    payment: MaintenancePayment,
    canEdit: Boolean,
    onMarkPaid: () -> Unit
) {
    val (badgeBg, badgeFg) = when (payment.status) {
        MaintenancePaymentStatus.PAID -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        MaintenancePaymentStatus.PENDING -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        MaintenancePaymentStatus.OVERDUE -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        MaintenancePaymentStatus.PARTIAL -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
        MaintenancePaymentStatus.WAIVED -> SocietySurfaceVariant to SocietySlate
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = payment.billingMonth,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SocietyNavyDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = payment.status.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "₹${(payment.amount + payment.penaltyAmount).toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (payment.status == MaintenancePaymentStatus.PAID) SocietySuccess else SocietyNavy
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (payment.paymentDate != null) {
                            "Paid on: ${payment.paymentDate} (${payment.paymentMethod.ifBlank { "Recorded" }})"
                        } else {
                            "Due Date: ${payment.dueDate}"
                        },
                        fontSize = 11.sp,
                        color = SocietySlate
                    )
                    if (payment.penaltyAmount > 0) {
                        Text(
                            text = "Base ₹${payment.amount.toInt()} + Penalty ₹${payment.penaltyAmount.toInt()}",
                            fontSize = 10.sp,
                            color = SocietyError
                        )
                    }
                }

                if (canEdit && payment.status != MaintenancePaymentStatus.PAID) {
                    TextButton(
                        onClick = onMarkPaid,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
