package com.example.ui.components

import androidx.compose.foundation.background
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
import com.example.data.model.Resident
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResidentDialog(
    onDismiss: () -> Unit,
    onSave: (Resident) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var flatNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var residentType by remember { mutableStateOf("OWNER") } // "OWNER" or "TENANT"
    var occupantsCount by remember { mutableStateOf("2") }
    var emergencyContact by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("add_resident_dialog")
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
                            text = "Add Resident to Room DB",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SocietyNavy
                        )
                        Text(
                            text = "Stores in SQLite 'residents' table",
                            fontSize = 11.sp,
                            color = SocietySlate
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_resident_dialog")) {
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

                // Resident Type Selection
                Text("Resident Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SocietyNavy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = residentType == "OWNER",
                        onClick = { residentType = "OWNER" },
                        label = { Text("Owner", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SocietyNavy,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = SocietyGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = residentType == "TENANT",
                        onClick = { residentType = "TENANT" },
                        label = { Text("Tenant", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SocietyNavy,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = SocietyGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Resident Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Ramesh Kulkarni") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SocietyNavy) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resident_name_input")
                )

                // Flat Number
                OutlinedTextField(
                    value = flatNumber,
                    onValueChange = { flatNumber = it; errorMessage = null },
                    label = { Text("Flat / Unit Number") },
                    placeholder = { Text("e.g. 304") },
                    leadingIcon = { Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = SocietyNavy) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resident_flat_input")
                )

                // Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errorMessage = null },
                    label = { Text("Contact Phone") },
                    placeholder = { Text("e.g. +91 98200 12345") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SocietyNavy) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resident_phone_input")
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    placeholder = { Text("e.g. resident@gmail.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SocietyNavy) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Occupants Count & Emergency Contact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = occupantsCount,
                        onValueChange = { occupantsCount = it },
                        label = { Text("Occupants") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = { Text("Emergency Contact") },
                        placeholder = { Text("+91...") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
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
                            if (name.isBlank()) {
                                errorMessage = "Please enter resident name"
                                return@Button
                            }
                            if (flatNumber.isBlank()) {
                                errorMessage = "Please enter flat number"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMessage = "Please enter phone number"
                                return@Button
                            }
                            val flatInt = flatNumber.toIntOrNull() ?: 101
                            val floor = if (flatInt >= 100) flatInt / 100 else 1
                            val occupants = occupantsCount.toIntOrNull() ?: 1

                            onSave(
                                Resident(
                                    name = name.trim(),
                                    flatNumber = flatNumber.trim(),
                                    wing = "Wing A",
                                    floor = floor,
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    residentType = residentType,
                                    numberOfOccupants = occupants,
                                    emergencyContact = emergencyContact.trim(),
                                    moveInDate = "01-Sep-2026",
                                    isActive = true
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SocietyNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_resident_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Resident")
                    }
                }
            }
        }
    }
}
