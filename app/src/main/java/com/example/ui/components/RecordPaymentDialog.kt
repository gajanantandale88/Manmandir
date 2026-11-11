package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.Payment
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    onDismiss: () -> Unit,
    onSave: (Payment) -> Unit
) {
    var flatNumber by remember { mutableStateOf("") }
    var residentName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("500") }
    var paymentMonth by remember { mutableStateOf("August 2026") }
    var paymentMode by remember { mutableStateOf("UPI") } // "UPI", "Cash", "Cheque", "Bank Transfer"
    var transactionRef by remember { mutableStateOf("UPI/202608/${(1000..9999).random()}") }
    var remarks by remember { mutableStateOf("Monthly society maintenance") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("record_payment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Record Payment in Room DB",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SocietyNavy
                        )
                        Text(
                            text = "Stores in SQLite 'payments' table",
                            fontSize = 11.sp,
                            color = SocietySlate
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_record_payment_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SocietySlate)
                    }
                }

                HorizontalDivider(color = SocietyCardBorder)

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Payment Mode Selection
                Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SocietyNavy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("UPI", "Cash", "Bank Transfer", "Cheque").forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SocietyNavy,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Flat Number & Resident Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = flatNumber,
                        onValueChange = { flatNumber = it; errorMessage = null },
                        label = { Text("Flat No") },
                        placeholder = { Text("e.g. 201") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("payment_flat_input")
                    )
                    OutlinedTextField(
                        value = residentName,
                        onValueChange = { residentName = it; errorMessage = null },
                        label = { Text("Resident Name") },
                        placeholder = { Text("e.g. Amit Verma") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(2f)
                            .testTag("payment_resident_name_input")
                    )
                }

                // Amount & Month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it; errorMessage = null },
                        label = { Text("Amount (₹)") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = SocietyNavy) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("payment_amount_input")
                    )
                    OutlinedTextField(
                        value = paymentMonth,
                        onValueChange = { paymentMonth = it },
                        label = { Text("Month") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                // Transaction Ref
                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("Transaction Reference") },
                    placeholder = { Text("e.g. UPI/202608/...") },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = SocietyNavy) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (flatNumber.isBlank()) {
                                errorMessage = "Please enter flat number"
                                return@Button
                            }
                            if (residentName.isBlank()) {
                                errorMessage = "Please enter resident name"
                                return@Button
                            }
                            val amt = amount.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                errorMessage = "Please enter a valid amount"
                                return@Button
                            }

                            val receiptNo = "MM/26-27/${(200..999).random()}"
                            val dateStr = "26-Sep-2026"

                            onSave(
                                Payment(
                                    residentId = 0,
                                    flatNumber = flatNumber.trim(),
                                    residentName = residentName.trim(),
                                    amount = amt,
                                    paymentDate = dateStr,
                                    paymentMonth = paymentMonth.trim(),
                                    paymentMode = paymentMode,
                                    status = "PAID",
                                    transactionRef = transactionRef.trim(),
                                    receiptNumber = receiptNo,
                                    remarks = remarks.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SocietyNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_payment_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Payment")
                    }
                }
            }
        }
    }
}
