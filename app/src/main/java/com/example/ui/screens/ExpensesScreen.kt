package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val role by viewModel.currentRole.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    val totalExpenseAmount = remember(allExpenses) { allExpenses.sumOf { it.amount } }

    Scaffold(
        floatingActionButton = {
            if (role != UserRole.RESIDENT) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = SocietyNavy,
                    contentColor = SocietyGoldLight,
                    modifier = Modifier.testTag("add_expense_fab")
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = "Record Expense")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                                text = "Society Operational Expenses",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Electricity, Water Tanker, Security, Lift AMC & Audits",
                                fontSize = 11.sp,
                                color = SocietyGoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF8A80).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Total: ₹${totalExpenseAmount.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF8A80),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Expense List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(allExpenses, key = { it.id }) { exp ->
                    ExpenseCardItem(
                        expense = exp,
                        onViewVoucher = {
                            viewModel.showSnackbar("Opening attached voucher for '${exp.category.name}' (Bill #${exp.billNumber})...")
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newExp ->
                viewModel.addExpense(newExp)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExpenseCardItem(
    expense: ExpenseEntity,
    onViewVoucher: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SocietyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SocietyErrorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (expense.category) {
                                ExpenseCategory.ELECTRICITY, ExpenseCategory.ELECTRICAL -> Icons.Default.Bolt
                                ExpenseCategory.WATER -> Icons.Default.WaterDrop
                                ExpenseCategory.LIFT -> Icons.Default.Elevator
                                ExpenseCategory.SECURITY -> Icons.Default.Security
                                ExpenseCategory.CLEANING -> Icons.Default.CleaningServices
                                ExpenseCategory.PLUMBING -> Icons.Default.Plumbing
                                else -> Icons.Default.Receipt
                            },
                            contentDescription = null,
                            tint = SocietyError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(expense.category.name.replace("_", " "), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavyDark)
                        Text("${expense.date} • ${expense.vendorName}", fontSize = 11.sp, color = SocietySlate)
                    }
                }

                Text(
                    text = "₹${expense.amount.toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SocietyError
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = expense.description,
                fontSize = 11.sp,
                color = SocietySlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SocietySurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bill No: ${expense.billNumber} • Mode: ${expense.paymentMethod}", fontSize = 10.sp, color = SocietyNavy)
                    TextButton(
                        onClick = onViewVoucher,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("View Voucher", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (ExpenseEntity) -> Unit
) {
    var category by remember { mutableStateOf(ExpenseCategory.ELECTRICITY) }
    var amountText by remember { mutableStateOf("") }
    var vendorName by remember { mutableStateOf("") }
    var billNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Bank NEFT / Online") }
    var committeeApproval by remember { mutableStateOf("Managing Committee Meeting") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Society Expense", fontWeight = FontWeight.Bold, color = SocietyNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Expense Category:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavy)
                ExpenseCategory.values().take(6).forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { category = cat }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(selected = category == cat, onClick = { category = cat })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(cat.name.replace("_", " "), fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vendorName,
                    onValueChange = { vendorName = it },
                    label = { Text("Vendor / Service Provider Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = billNumber,
                    onValueChange = { billNumber = it },
                    label = { Text("Bill / Invoice Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Mode (Bank NEFT, Cheque, Cash)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val exp = ExpenseEntity(
                        category = category,
                        amount = amountText.toDoubleOrNull() ?: 0.0,
                        date = "22-Aug-2026",
                        vendorName = vendorName.ifBlank { "Authorized Society Vendor" },
                        billNumber = billNumber.ifBlank { "INV-${System.currentTimeMillis() % 1000}" },
                        description = description,
                        paymentMethod = paymentMethod,
                        committeeApproval = committeeApproval,
                        receiptDoc = "vouchers/exp_${System.currentTimeMillis()}.pdf"
                    )
                    onAdd(exp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Record Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
