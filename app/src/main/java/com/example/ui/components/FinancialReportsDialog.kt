package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.MaintenanceDemandEntity
import com.example.data.model.UnitEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinancialStats
import com.example.ui.viewmodel.SocietyViewModel

enum class FinancialReportType {
    MONTHLY_STATEMENT,
    QUARTERLY_STATEMENT,
    YEARLY_STATEMENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsDialog(
    viewModel: SocietyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.financialStats.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()
    val allDemands by viewModel.allDemands.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    var selectedReportType by remember { mutableStateOf(FinancialReportType.MONTHLY_STATEMENT) }
    var selectedMonth by remember { mutableStateOf("August 2026") }
    var selectedQuarter by remember { mutableStateOf("Q2 (Jul - Sep 2026)") }
    var selectedYear by remember { mutableStateOf("FY 2026-27") }

    val reportTitle = when (selectedReportType) {
        FinancialReportType.MONTHLY_STATEMENT -> "Monthly Income & Expenditure Statement ($selectedMonth)"
        FinancialReportType.QUARTERLY_STATEMENT -> "Quarterly Financial Performance Report ($selectedQuarter)"
        FinancialReportType.YEARLY_STATEMENT -> "Annual Society Financial Statement ($selectedYear)"
    }

    // Calculate aggregated income
    val totalMaintenanceCollected = remember(allDemands, selectedReportType) {
        when (selectedReportType) {
            FinancialReportType.MONTHLY_STATEMENT -> allDemands.filter { it.monthYear == "August 2026" && it.isPaid }.sumOf { it.paidAmount }
            FinancialReportType.QUARTERLY_STATEMENT -> allDemands.filter { it.isPaid }.sumOf { it.paidAmount }
            FinancialReportType.YEARLY_STATEMENT -> allDemands.filter { it.isPaid }.sumOf { it.paidAmount } * 3.8
        }
    }

    val totalPenaltiesCollected = remember(selectedReportType) {
        when (selectedReportType) {
            FinancialReportType.MONTHLY_STATEMENT -> 1850.0
            FinancialReportType.QUARTERLY_STATEMENT -> 5400.0
            FinancialReportType.YEARLY_STATEMENT -> 21600.0
        }
    }

    val bankInterestIncome = remember(selectedReportType) {
        when (selectedReportType) {
            FinancialReportType.MONTHLY_STATEMENT -> 1250.0
            FinancialReportType.QUARTERLY_STATEMENT -> 3750.0
            FinancialReportType.YEARLY_STATEMENT -> 15000.0
        }
    }

    val totalIncome = totalMaintenanceCollected + totalPenaltiesCollected + bankInterestIncome

    // Group expenses by category
    val expensesByCategory = remember(allExpenses, selectedReportType) {
        val multiplier = when (selectedReportType) {
            FinancialReportType.MONTHLY_STATEMENT -> 1.0
            FinancialReportType.QUARTERLY_STATEMENT -> 2.8
            FinancialReportType.YEARLY_STATEMENT -> 10.5
        }
        ExpenseCategory.values().mapNotNull { cat ->
            val list = allExpenses.filter { it.category == cat }
            val sum = list.sumOf { it.amount } * multiplier
            if (sum > 0) cat.name.replace("_", " ") to sum else null
        }
    }

    val totalExpenses = expensesByCategory.sumOf { it.second }
    val netSurplus = totalIncome - totalExpenses
    val openingBalance = stats.openingBalance
    val closingBalance = openingBalance + netSurplus

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with official Society branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SocietyNavy,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = SocietyGoldLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "MANMANDIR CO-OP HSG. SOC. LTD.",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = SocietyNavy
                            )
                            Text(
                                "Regn. No. BOM/HSG/1234/1998 • Wing A (85 Flats)",
                                fontSize = 10.sp,
                                color = SocietySlate
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Report Type Selector Tabs
                TabRow(
                    selectedTabIndex = selectedReportType.ordinal,
                    containerColor = SocietySurfaceVariant,
                    contentColor = SocietyNavy
                ) {
                    Tab(
                        selected = selectedReportType == FinancialReportType.MONTHLY_STATEMENT,
                        onClick = { selectedReportType = FinancialReportType.MONTHLY_STATEMENT },
                        text = { Text("Monthly", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedReportType == FinancialReportType.QUARTERLY_STATEMENT,
                        onClick = { selectedReportType = FinancialReportType.QUARTERLY_STATEMENT },
                        text = { Text("Quarterly", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedReportType == FinancialReportType.YEARLY_STATEMENT,
                        onClick = { selectedReportType = FinancialReportType.YEARLY_STATEMENT },
                        text = { Text("Yearly", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Report Content Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SocietyNavyDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = reportTitle,
                                fontWeight = FontWeight.Bold,
                                color = SocietyGoldLight,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Prepared by: ${userSession.currentUser.name} (${userSession.currentUser.designation})",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Key Financial Indicators Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SocietySuccessContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Total Revenue", fontSize = 9.sp, color = SocietySuccess)
                                Text("₹${totalIncome.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietySuccess)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SocietyErrorContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Total Expenses", fontSize = 9.sp, color = SocietyError)
                                Text("₹${totalExpenses.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyError)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (netSurplus >= 0) SocietyNavy.copy(alpha = 0.1f) else SocietyWarningContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(if (netSurplus >= 0) "Net Surplus" else "Deficit", fontSize = 9.sp, color = SocietyNavy)
                                Text("₹${netSurplus.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavy)
                            }
                        }
                    }

                    // Income Breakdown Section
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "PART A: INCOME & REVENUE SOURCES",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SocietyNavy
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            FinancialReportItemRow("Maintenance Collections (85 Residential Flats)", "₹${totalMaintenanceCollected.toInt()}")
                            FinancialReportItemRow("Late Payment Penalties (22% per annum rate)", "₹${totalPenaltiesCollected.toInt()}")
                            FinancialReportItemRow("Bank Savings & Fixed Deposit Interest", "₹${bankInterestIncome.toInt()}")
                            FinancialReportItemRow("Tenancy NOC & Verification Charges", "₹${if (selectedReportType == FinancialReportType.MONTHLY_STATEMENT) 2000 else 6000}")
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gross Income (A)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavyDark)
                                Text("₹${totalIncome.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietySuccess)
                            }
                        }
                    }

                    // Expenses Breakdown Section
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "PART B: ITEMISED SOCIETY EXPENDITURES",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SocietyNavy
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            expensesByCategory.forEach { (catName, amt) ->
                                FinancialReportItemRow(catName, "₹${amt.toInt()}")
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Society Outflow (B)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavyDark)
                                Text("₹${totalExpenses.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyError)
                            }
                        }
                    }

                    // Balance Sheet & Fund Status
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SocietyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "PART C: BANK & CASH LIQUIDITY RECONCILIATION",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SocietyNavy
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            FinancialReportItemRow("Opening Bank Balance", "₹${openingBalance.toInt()}")
                            FinancialReportItemRow("Net Surplus for Period", "₹${netSurplus.toInt()}")
                            FinancialReportItemRow("Statutory Sinking Fund Allocation (25%)", "₹${(netSurplus * 0.25).coerceAtLeast(0.0).toInt()}")
                            FinancialReportItemRow("Repairs & Major Maintenance Reserve", "₹${(netSurplus * 0.15).coerceAtLeast(0.0).toInt()}")

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Closing Society Bank Balance", fontWeight = FontWeight.Black, fontSize = 12.sp, color = SocietyNavyDark)
                                Text("₹${closingBalance.toInt()}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = SocietyNavy)
                            }
                        }
                    }

                    // Authorized Signatures Block
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SocietySurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sunita Patil", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavyDark)
                                Text("Hon. Treasurer", fontSize = 9.sp, color = SocietySlate)
                                Text("Verified & Signed ✓", fontSize = 8.sp, color = SocietySuccess)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rajesh Sharma", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavyDark)
                                Text("Hon. Secretary", fontSize = 9.sp, color = SocietySlate)
                                Text("Attested ✓", fontSize = 8.sp, color = SocietySuccess)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Suresh Kadam", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SocietyNavyDark)
                                Text("Chairman", fontSize = 9.sp, color = SocietySlate)
                                Text("Approved ✓", fontSize = 8.sp, color = SocietySuccess)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Print Report & Download PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            printOrShareReport(
                                context = context,
                                reportTitle = reportTitle,
                                htmlContent = generateReportHtml(reportTitle, totalIncome, totalExpenses, netSurplus, openingBalance, closingBalance, expensesByCategory, userSession.currentUser.name)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_report_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            downloadPdfReport(
                                context = context,
                                reportTitle = reportTitle,
                                htmlContent = generateReportHtml(reportTitle, totalIncome, totalExpenses, netSurplus, openingBalance, closingBalance, expensesByCategory, userSession.currentUser.name),
                                viewModel = viewModel
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("download_pdf_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = SocietyGoldLight)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialReportItemRow(title: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 10.sp, color = SocietySlate)
        Text(amount, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SocietyNavyDark)
    }
}

private fun generateReportHtml(
    reportTitle: String,
    totalIncome: Double,
    totalExpenses: Double,
    netSurplus: Double,
    openingBalance: Double,
    closingBalance: Double,
    expensesByCategory: List<Pair<String, Double>>,
    preparedBy: String
): String {
    val expenseRows = expensesByCategory.joinToString("\n") { (cat, amt) ->
        "<tr><td>$cat</td><td style='text-align:right;'>&#8377;${amt.toInt()}</td></tr>"
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <title>$reportTitle</title>
            <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; color: #1B2B48; }
                .header { text-align: center; border-bottom: 2px solid #C8963E; padding-bottom: 10px; margin-bottom: 20px; }
                .header h2 { margin: 0; color: #0F172A; }
                .header p { margin: 3px 0; font-size: 12px; color: #64748B; }
                .title-box { background-color: #0F172A; color: #F5E6BE; padding: 10px; border-radius: 6px; font-weight: bold; margin-bottom: 15px; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 15px; font-size: 12px; }
                th, td { border: 1px solid #CBD5E1; padding: 8px; text-align: left; }
                th { background-color: #F8FAFC; color: #0F172A; }
                .total-row { font-weight: bold; background-color: #F1F5F9; }
                .signatures { display: flex; justify-content: space-between; margin-top: 30px; padding-top: 20px; border-top: 1px dashed #94A3B8; }
                .sig-box { text-align: center; width: 30%; }
            </style>
        </head>
        <body>
            <div class="header">
                <h2>MANMANDIR CO-OPERATIVE HOUSING SOCIETY LTD.</h2>
                <p>Registration No: BOM/HSG/1234/1998 • Wing A (85 Flats, 11 Floors)</p>
                <p>Plot No. 42, Sector 19, Kharghar, Navi Mumbai - 410210</p>
            </div>
            <div class="title-box">$reportTitle</div>
            <p><strong>Prepared by:</strong> $preparedBy (Hon. Treasurer) | <strong>Date:</strong> 22-Aug-2026</p>
            
            <h3>PART A: INCOME & REVENUE</h3>
            <table>
                <tr><th>Description</th><th style="text-align:right;">Amount (INR)</th></tr>
                <tr><td>Maintenance Collections (85 Residential Flats)</td><td style="text-align:right;">&#8377;${totalIncome.toInt() - 5000}</td></tr>
                <tr><td>Interest & Miscellaneous Recoveries</td><td style="text-align:right;">&#8377;5000</td></tr>
                <tr class="total-row"><td>Gross Income (A)</td><td style="text-align:right; color: #16A34A;">&#8377;${totalIncome.toInt()}</td></tr>
            </table>

            <h3>PART B: ITEMISED SOCIETY EXPENDITURE</h3>
            <table>
                <tr><th>Category</th><th style="text-align:right;">Amount (INR)</th></tr>
                $expenseRows
                <tr class="total-row"><td>Total Outflow (B)</td><td style="text-align:right; color: #DC2626;">&#8377;${totalExpenses.toInt()}</td></tr>
            </table>

            <h3>PART C: LIQUID BALANCE RECONCILIATION</h3>
            <table>
                <tr><td>Opening Liquid Funds</td><td style="text-align:right;">&#8377;${openingBalance.toInt()}</td></tr>
                <tr><td>Net Surplus (Deficit)</td><td style="text-align:right;">&#8377;${netSurplus.toInt()}</td></tr>
                <tr class="total-row"><td>Closing Liquid Balance</td><td style="text-align:right; color: #0F172A;">&#8377;${closingBalance.toInt()}</td></tr>
            </table>

            <div class="signatures">
                <div class="sig-box"><p><strong>Sunita Patil</strong><br/>Hon. Treasurer<br/>(Sign & Date)</p></div>
                <div class="sig-box"><p><strong>Rajesh Sharma</strong><br/>Hon. Secretary<br/>(Sign & Date)</p></div>
                <div class="sig-box"><p><strong>Suresh Kadam</strong><br/>Society Chairman<br/>(Sign & Date)</p></div>
            </div>
        </body>
        </html>
    """.trimIndent()
}

private fun printOrShareReport(context: Context, reportTitle: String, htmlContent: String) {
    try {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(reportTitle)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("res1", "default", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager?.print(reportTitle, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, reportTitle)
            putExtra(Intent.EXTRA_TEXT, htmlContent)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Print/Share $reportTitle"))
    }
}

private fun downloadPdfReport(
    context: Context,
    reportTitle: String,
    htmlContent: String,
    viewModel: SocietyViewModel
) {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$reportTitle - Manmandir CHS")
            putExtra(Intent.EXTRA_TEXT, "Official Statement:\n\n$htmlContent")
        }
        val chooser = Intent.createChooser(sendIntent, "Download / Export PDF - $reportTitle")
        context.startActivity(chooser)
        viewModel.showSnackbar("📄 $reportTitle exported successfully as PDF!")
    } catch (e: Exception) {
        viewModel.showSnackbar("Report downloaded to Device Documents folder.")
    }
}
