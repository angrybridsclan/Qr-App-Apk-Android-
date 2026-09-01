package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParsedQrResult
import com.example.model.QrDataParser
import com.example.ui.MainViewModel
import com.example.util.AppActions
import com.example.util.QrCodeGenerator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScanResultScreen(
    scanId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentScan by viewModel.currentScan.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editTitleText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(scanId) {
        viewModel.loadScanById(scanId)
    }

    LaunchedEffect(currentScan) {
        currentScan?.let { scan ->
            qrBitmap = QrCodeGenerator.generateQrBitmap(scan.rawValue, 400)
            editTitleText = scan.customTitle ?: scan.title
        }
    }

    val scan = currentScan
    val parsedResult = remember(scan) {
        if (scan != null) QrDataParser.parse(scan.rawValue, scan.format) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("result_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("result_overflow_menu")) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share Raw Text") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                scan?.let { AppActions.shareText(context, it.rawValue) }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy to Clipboard") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                scan?.let { AppActions.copyToClipboard(context, it.rawValue) }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                            onClick = {
                                showMenu = false
                                scan?.let { viewModel.deleteScan(it.id) }
                                onBack()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        if (scan == null || parsedResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading result...", color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                // Sub-header Row: Icon, Title, Format & Date, Edit, Star (matching Screenshot 2, 3, 5)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type Icon in subtle circular badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getResultIcon(parsedResult),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = scan.customTitle ?: scan.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = scan.subtitle.ifEmpty { "${scan.type}, ${scan.format}" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Edit / Rename button
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.testTag("edit_title_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit title",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Star / Favorite toggle
                        IconButton(
                            onClick = { viewModel.toggleFavorite(scan) },
                            modifier = Modifier.testTag("favorite_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (scan.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (scan.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Content Block (Formatted Data fields)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        when (parsedResult) {
                            is ParsedQrResult.Contact -> {
                                Text(
                                    text = parsedResult.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                parsedResult.organization?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                parsedResult.address?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = it, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                parsedResult.phone?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = it, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                parsedResult.email?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = it, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            is ParsedQrResult.Wifi -> {
                                Text(
                                    text = "Network Name: ${parsedResult.ssid}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Type: ${parsedResult.type}",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Password: ${parsedResult.password.ifEmpty { "(None)" }}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            is ParsedQrResult.Url -> {
                                Text(
                                    text = parsedResult.url,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable { AppActions.openUrl(context, parsedResult.url) }
                                )
                            }

                            is ParsedQrResult.Product -> {
                                Text(
                                    text = parsedResult.code,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Barcode Format: ${parsedResult.barcodeFormat}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            is ParsedQrResult.Phone -> {
                                Text(
                                    text = parsedResult.phoneNumber,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            is ParsedQrResult.Email -> {
                                Text(
                                    text = parsedResult.email,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                parsedResult.subject?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Subject: $it", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                parsedResult.body?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Body: $it", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            is ParsedQrResult.Sms -> {
                                Text(
                                    text = "Send SMS to: ${parsedResult.phoneNumber}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                parsedResult.message?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Message: $it", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            is ParsedQrResult.Geo -> {
                                Text(
                                    text = "Coordinates: ${parsedResult.latitude}, ${parsedResult.longitude}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                parsedResult.query?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Query: $it", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            is ParsedQrResult.CalendarEvent -> {
                                Text(
                                    text = parsedResult.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                parsedResult.location?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Location: $it", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                parsedResult.description?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            is ParsedQrResult.Text -> {
                                Text(
                                    text = parsedResult.text,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Action Buttons matching screenshots (Add contact, Show map, Call, Send email, Share, Copy)
                ActionButtonsGrid(
                    parsed = parsedResult,
                    rawValue = scan.rawValue,
                    onShare = { AppActions.shareText(context, scan.rawValue) },
                    onCopy = { AppActions.copyToClipboard(context, scan.rawValue) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // High quality QR Code Image Card
                if (qrBitmap != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code Image",
                                modifier = Modifier
                                    .size(220.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Rename Title") },
            text = {
                OutlinedTextField(
                    value = editTitleText,
                    onValueChange = { editTitleText = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editTitleText.isNotBlank()) {
                            viewModel.updateCustomTitle(scanId, editTitleText.trim())
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionButtonsGrid(
    parsed: ParsedQrResult,
    rawValue: String,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    val context = LocalContext.current

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        maxItemsInEachRow = 4
    ) {
        when (parsed) {
            is ParsedQrResult.Contact -> {
                ActionButtonItem(Icons.Default.PersonAdd, "Add contact") {
                    AppActions.addContact(context, parsed)
                }
                parsed.address?.let { addr ->
                    ActionButtonItem(Icons.Default.LocationOn, "Show map") {
                        AppActions.openMap(context, addr)
                    }
                }
                parsed.phone?.let { phone ->
                    ActionButtonItem(Icons.Default.Call, "Call") {
                        AppActions.dialPhone(context, phone)
                    }
                }
                parsed.email?.let { email ->
                    ActionButtonItem(Icons.Default.Email, "Send email") {
                        AppActions.sendEmail(context, email)
                    }
                }
            }

            is ParsedQrResult.Wifi -> {
                ActionButtonItem(Icons.Default.Wifi, "Connect") {
                    AppActions.copyToClipboard(context, parsed.password, "WiFi Password")
                }
                if (parsed.password.isNotEmpty()) {
                    ActionButtonItem(Icons.Default.Password, "Copy Password") {
                        AppActions.copyToClipboard(context, parsed.password, "WiFi Password")
                    }
                }
            }

            is ParsedQrResult.Url -> {
                ActionButtonItem(Icons.AutoMirrored.Filled.OpenInNew, "Open") {
                    AppActions.openUrl(context, parsed.url)
                }
            }

            is ParsedQrResult.Phone -> {
                ActionButtonItem(Icons.Default.Call, "Call") {
                    AppActions.dialPhone(context, parsed.phoneNumber)
                }
                ActionButtonItem(Icons.Default.Sms, "SMS") {
                    AppActions.sendSms(context, parsed.phoneNumber)
                }
            }

            is ParsedQrResult.Email -> {
                ActionButtonItem(Icons.Default.Email, "Send email") {
                    AppActions.sendEmail(context, parsed.email, parsed.subject, parsed.body)
                }
            }

            is ParsedQrResult.Sms -> {
                ActionButtonItem(Icons.Default.Sms, "Send SMS") {
                    AppActions.sendSms(context, parsed.phoneNumber, parsed.message)
                }
            }

            is ParsedQrResult.Geo -> {
                ActionButtonItem(Icons.Default.LocationOn, "Show map") {
                    AppActions.openMap(context, "${parsed.latitude},${parsed.longitude}")
                }
            }

            is ParsedQrResult.Product -> {
                ActionButtonItem(Icons.Default.Search, "Search") {
                    AppActions.searchWeb(context, parsed.code)
                }
            }

            is ParsedQrResult.Text -> {
                ActionButtonItem(Icons.Default.Search, "Search") {
                    AppActions.searchWeb(context, parsed.text)
                }
            }

            is ParsedQrResult.CalendarEvent -> {
                ActionButtonItem(Icons.Default.Share, "Share") {
                    onShare()
                }
            }
        }

        // Always show Share and Copy buttons (matching screenshots)
        ActionButtonItem(Icons.Default.Share, "Share") {
            onShare()
        }
        ActionButtonItem(Icons.Default.ContentCopy, "Copy") {
            onCopy()
        }
    }
}

@Composable
private fun ActionButtonItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .clickable(onClick = onClick)
            .testTag("action_${label.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

private fun getResultIcon(parsed: ParsedQrResult): ImageVector {
    return when (parsed) {
        is ParsedQrResult.Contact -> Icons.Default.Person
        is ParsedQrResult.Wifi -> Icons.Default.Wifi
        is ParsedQrResult.Url -> Icons.Default.Link
        is ParsedQrResult.Product -> Icons.Default.Inventory2
        is ParsedQrResult.Phone -> Icons.Default.Phone
        is ParsedQrResult.Email -> Icons.Default.Email
        is ParsedQrResult.Sms -> Icons.Default.Sms
        is ParsedQrResult.Geo -> Icons.Default.LocationOn
        is ParsedQrResult.CalendarEvent -> Icons.Default.LocationOn
        is ParsedQrResult.Text -> Icons.Default.TextFields
    }
}
