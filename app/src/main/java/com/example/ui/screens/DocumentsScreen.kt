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
import com.example.data.model.DocumentCategory
import com.example.data.model.SocietyDocumentEntity
import com.example.data.model.UnitType
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.SocietyViewModel

enum class DocumentViewTab {
    CENTRAL_LIBRARY,
    RESIDENT_CHECKLIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: SocietyViewModel,
    modifier: Modifier = Modifier
) {
    val role by viewModel.currentRole.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val allDocs by viewModel.allDocuments.collectAsState()
    val residentDocs by viewModel.residentDocuments.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()

    var activeTab by remember { mutableStateOf(DocumentViewTab.CENTRAL_LIBRARY) }
    var selectedCategoryFilter by remember { mutableStateOf<DocumentCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showUploadDialog by remember { mutableStateOf(false) }
    var docToDelete by remember { mutableStateOf<SocietyDocumentEntity?>(null) }

    val canRemoveDocuments = userSession.privileges.contains(com.example.data.session.UserPrivilege.REMOVE_SOCIETY_DOCUMENTS) ||
            userSession.currentUser.role == UserRole.SUPER_ADMIN ||
            userSession.currentUser.role == UserRole.CHAIRMAN ||
            userSession.currentUser.role == UserRole.SECRETARY

    val docsToDisplay = remember(allDocs, residentDocs, role, selectedCategoryFilter, searchQuery) {
        val base = if (role == UserRole.RESIDENT) residentDocs else allDocs
        base.filter { doc ->
            selectedCategoryFilter == null || doc.category == selectedCategoryFilter
        }.filter { doc ->
            if (searchQuery.isBlank()) true
            else {
                val q = searchQuery.trim().lowercase()
                doc.title.lowercase().contains(q) ||
                        doc.description.lowercase().contains(q) ||
                        doc.fileRef.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (role != UserRole.RESIDENT && activeTab == DocumentViewTab.CENTRAL_LIBRARY) {
                FloatingActionButton(
                    onClick = { showUploadDialog = true },
                    containerColor = SocietyNavy,
                    contentColor = SocietyGoldLight,
                    modifier = Modifier.testTag("upload_doc_fab")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Upload Document")
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
                                text = "Society Documents & Records",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Digital Central Archives & Resident Verification Files",
                                fontSize = 11.sp,
                                color = SocietyGoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SocietyGold
                        ) {
                            Text(
                                text = "${docsToDisplay.size} Docs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SocietyNavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (role != UserRole.RESIDENT) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = activeTab == DocumentViewTab.CENTRAL_LIBRARY,
                                onClick = { activeTab = DocumentViewTab.CENTRAL_LIBRARY },
                                label = { Text("Central Library (${allDocs.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SocietyGold,
                                    selectedLabelColor = SocietyNavyDark,
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    labelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = activeTab == DocumentViewTab.RESIDENT_CHECKLIST,
                                onClick = { activeTab = DocumentViewTab.RESIDENT_CHECKLIST },
                                label = { Text("Resident 85 Flats Checklist", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SocietyGold,
                                    selectedLabelColor = SocietyNavyDark,
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            if (activeTab == DocumentViewTab.CENTRAL_LIBRARY) {
                // Category Filter chips & Search
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search documents (BMC, MMRDA, NOC, AGM)...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SocietySlate) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("All Folders", fontSize = 10.sp) }
                            )
                        }
                        items(DocumentCategory.values()) { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                },
                                label = { Text(cat.name.replace("_", " "), fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Document List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (docsToDisplay.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No documents in this folder.", color = SocietySlate, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(docsToDisplay, key = { it.id }) { doc ->
                            SocietyDocCard(
                                doc = doc,
                                canRemove = canRemoveDocuments,
                                onOpen = {
                                    viewModel.showSnackbar("Opening document '${doc.title}' (${doc.fileSize})...")
                                },
                                onDelete = {
                                    docToDelete = doc
                                }
                            )
                        }
                    }
                }
            } else {
                // Resident 85 Flats Checklist View
                val residentialFlats = allUnits.filter { it.unitType == UnitType.RESIDENTIAL }
                val fullySubmitted = residentialFlats.count { it.pendingDocs.isBlank() || it.pendingDocs == "None" }
                val pendingSubmissions = residentialFlats.size - fullySubmitted

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, SocietyCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("85 Flats Verification Audit", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SocietyNavy)
                                    Text("Index II, ID Proofs & Tenancy NOC status", fontSize = 10.sp, color = SocietySlate)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = SocietySuccessContainer) {
                                        Text("$fullySubmitted Complete", color = SocietySuccess, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = SocietyErrorContainer) {
                                        Text("$pendingSubmissions Pending", color = SocietyError, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                                    }
                                }
                            }
                        }
                    }

                    items(residentialFlats, key = { it.roomNumber }) { flat ->
                        val isPending = flat.pendingDocs.isNotBlank() && flat.pendingDocs != "None"
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, if (isPending) SocietyWarning.copy(alpha = 0.4f) else SocietyCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SocietyNavy
                                        ) {
                                            Text(
                                                "Flat ${flat.roomNumber}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            if (flat.occupancyType == com.example.data.model.OccupancyType.TENANT) flat.tenantName else flat.ownerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = SocietyNavyDark
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isPending) SocietyWarningContainer else SocietySuccessContainer
                                    ) {
                                        Text(
                                            if (isPending) "Pending Action" else "Verified ✓",
                                            color = if (isPending) SocietyWarning else SocietySuccess,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Submitted: ${flat.submittedDocs.ifBlank { "None" }}", fontSize = 10.sp, color = SocietySlate)
                                if (isPending) {
                                    Text("Pending Dues/Compliance: ${flat.pendingDocs}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SocietyError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadDocumentDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { newDoc ->
                viewModel.addDocument(newDoc)
                showUploadDialog = false
            }
        )
    }

    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = SocietyError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove Document", fontWeight = FontWeight.Bold, color = SocietyNavy)
                }
            },
            text = {
                Column {
                    Text("Are you sure you want to permanently delete this document from the official society archives?", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SocietySurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SocietyNavyDark)
                            Text("Category: ${doc.category.name.replace('_', ' ')} • Ref: ${doc.fileRef}", fontSize = 10.sp, color = SocietySlate)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Authorized by: ${userSession.currentUser.name} (${userSession.currentUser.designation})", fontSize = 10.sp, color = SocietyGoldDark, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDocument(doc.id, doc.title)
                        docToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SocietyError)
                ) {
                    Text("Delete Document", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SocietyDocCard(
    doc: SocietyDocumentEntity,
    canRemove: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onOpen,
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SocietyNavyLight.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (doc.category) {
                            DocumentCategory.SOCIETY_REGISTRATION -> Icons.Default.Gavel
                            DocumentCategory.BMC_CORRESPONDENCE -> Icons.Default.LocationCity
                            DocumentCategory.MMRDA_CORRESPONDENCE -> Icons.Default.Engineering
                            DocumentCategory.POLICE_CORRESPONDENCE -> Icons.Default.LocalPolice
                            DocumentCategory.MEETING_MINUTES -> Icons.Default.EventNote
                            DocumentCategory.RESOLUTIONS -> Icons.Default.FactCheck
                            DocumentCategory.MAINTENANCE_RECORDS -> Icons.Default.FireExtinguisher
                            DocumentCategory.BILLS_AND_QUOTATIONS -> Icons.Default.Receipt
                            else -> Icons.Default.Folder
                        },
                        contentDescription = null,
                        tint = SocietyNavy,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SocietyGoldContainer
                        ) {
                            Text(
                                text = doc.category.name.replace("_", " "),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8D6E00),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        if (doc.isResidentAccessible) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SocietySuccessContainer
                            ) {
                                Text(
                                    text = "Resident Viewable",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocietySuccess,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = doc.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SocietyNavyDark
                    )
                    Text(
                        text = "Ref: ${doc.fileRef} • Date: ${doc.dateUploaded} • ${doc.fileSize}",
                        fontSize = 10.sp,
                        color = SocietySlate
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Default.Download, contentDescription = "Download", tint = SocietyNavy)
                }
                if (canRemove) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete (Secretary/Admin)", tint = SocietyError)
                    }
                }
            }
        }
    }
}

@Composable
fun UploadDocumentDialog(
    onDismiss: () -> Unit,
    onUpload: (SocietyDocumentEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var fileRef by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DocumentCategory.SOCIETY_REGISTRATION) }
    var isResidentAccessible by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Upload Society Document", fontWeight = FontWeight.Bold, color = SocietyNavy)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    placeholder = { Text("e.g. BMC Water Pipe Inspection Certificate") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fileRef,
                    onValueChange = { fileRef = it },
                    label = { Text("Reference / Inward No.") },
                    placeholder = { Text("e.g. BMC/WD/2026/910") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Summary / Scope") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Folder Category:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SocietyNavy)

                DocumentCategory.values().take(6).forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(cat.name.replace("_", " "), fontSize = 11.sp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isResidentAccessible,
                        onCheckedChange = { isResidentAccessible = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Allow Residents to View & Download", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = SocietyDocumentEntity(
                        title = title.ifBlank { "Untitled Document" },
                        category = selectedCategory,
                        description = description,
                        fileRef = fileRef.ifBlank { "DOC-${System.currentTimeMillis() % 10000}" },
                        dateUploaded = "22-Aug-2026",
                        uploadedBy = "Rajesh Sharma (Secretary)",
                        fileSize = "1.8 MB",
                        isResidentAccessible = isResidentAccessible
                    )
                    onUpload(doc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SocietyNavy)
            ) {
                Text("Upload to Repository")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
