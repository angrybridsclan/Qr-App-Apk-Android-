package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch

enum class CreateType {
    CLIPBOARD, URL, TEXT, CONTACT, EMAIL, SMS, GEO, PHONE, CALENDAR, WIFI
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQrScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    var activeCreateType by remember { mutableStateOf<CreateType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("create_drawer_button")) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Header matching Screenshot 7
            Text(
                text = "Create QR",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column {
                    val createItems = listOf(
                        CreateOption(CreateType.CLIPBOARD, "Content from clipboard", Icons.Default.Assignment),
                        CreateOption(CreateType.URL, "URL", Icons.Default.Link),
                        CreateOption(CreateType.TEXT, "Text", Icons.Default.TextFields),
                        CreateOption(CreateType.CONTACT, "Contact", Icons.Default.Person),
                        CreateOption(CreateType.EMAIL, "Email", Icons.Default.Email),
                        CreateOption(CreateType.SMS, "SMS", Icons.Default.Sms),
                        CreateOption(CreateType.GEO, "Geo", Icons.Default.LocationOn),
                        CreateOption(CreateType.PHONE, "Phone", Icons.Default.Phone),
                        CreateOption(CreateType.CALENDAR, "Calendar", Icons.Default.CalendarMonth),
                        CreateOption(CreateType.WIFI, "Wifi", Icons.Default.Wifi)
                    )

                    createItems.forEachIndexed { index, item ->
                        CreateItemRow(
                            title = item.title,
                            icon = item.icon,
                            onClick = {
                                if (item.type == CreateType.CLIPBOARD) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                    if (clipText.isNotBlank()) {
                                        viewModel.createAndSaveQr(clipText, "Text", "Clipboard", onNavigateToDetail)
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    activeCreateType = item.type
                                }
                            }
                        )
                        if (index < createItems.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(start = 56.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (activeCreateType != null) {
        ModalBottomSheet(
            onDismissRequest = { activeCreateType = null },
            sheetState = sheetState
        ) {
            CreateQrFormSheet(
                type = activeCreateType!!,
                onCreate = { rawValue, typeName, title ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        activeCreateType = null
                        viewModel.createAndSaveQr(rawValue, typeName, title, onNavigateToDetail)
                    }
                }
            )
        }
    }
}

private data class CreateOption(val type: CreateType, val title: String, val icon: ImageVector)

@Composable
private fun CreateItemRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("create_item_${title.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CreateQrFormSheet(
    type: CreateType,
    onCreate: (rawValue: String, typeName: String, title: String) -> Unit
) {
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }
    var field4 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Create ${type.name.lowercase().replaceFirstChar { it.uppercase() }} QR",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (type) {
            CreateType.URL -> {
                OutlinedTextField(
                    value = field1,
                    onValueChange = { field1 = it },
                    label = { Text("URL (e.g. https://example.com)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            CreateType.TEXT -> {
                OutlinedTextField(
                    value = field1,
                    onValueChange = { field1 = it },
                    label = { Text("Text Message") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            CreateType.CONTACT -> {
                OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field4, onValueChange = { field4 = it }, label = { Text("Address / City") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            CreateType.WIFI -> {
                OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Network Name (SSID)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            CreateType.EMAIL -> {
                OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Subject (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Body (Optional)") }, modifier = Modifier.fillMaxWidth())
            }
            CreateType.SMS -> {
                OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth())
            }
            CreateType.PHONE -> {
                OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            CreateType.GEO -> {
                OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Latitude (e.g. 37.7749)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Longitude (e.g. -122.4194)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            CreateType.CALENDAR -> {
                OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Event Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val (raw, typeStr, titleStr) = buildRawString(type, field1, field2, field3, field4)
                if (raw.isNotBlank()) {
                    onCreate(raw, typeStr, titleStr)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_qr_button")
        ) {
            Text("Create QR Code", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun buildRawString(
    type: CreateType,
    f1: String,
    f2: String,
    f3: String,
    f4: String
): Triple<String, String, String> {
    return when (type) {
        CreateType.URL -> {
            val url = if (!f1.startsWith("http://") && !f1.startsWith("https://")) "https://$f1" else f1
            Triple(url, "URL", "URL")
        }
        CreateType.TEXT -> Triple(f1, "Text", "Text")
        CreateType.CONTACT -> {
            val vcard = buildString {
                appendLine("BEGIN:VCARD")
                appendLine("VERSION:3.0")
                appendLine("FN:$f1")
                if (f2.isNotBlank()) appendLine("TEL:$f2")
                if (f3.isNotBlank()) appendLine("EMAIL:$f3")
                if (f4.isNotBlank()) appendLine("ADR:;;$f4")
                appendLine("END:VCARD")
            }
            Triple(vcard, "Contact", f1.ifEmpty { "Contact" })
        }
        CreateType.WIFI -> {
            val wifi = "WIFI:S:$f1;T:WPA;P:$f2;;"
            Triple(wifi, "Wifi", "Wifi: $f1")
        }
        CreateType.EMAIL -> {
            val mailto = buildString {
                append("mailto:$f1")
                if (f2.isNotBlank() || f3.isNotBlank()) {
                    append("?")
                    if (f2.isNotBlank()) append("subject=${java.net.URLEncoder.encode(f2, "UTF-8")}&")
                    if (f3.isNotBlank()) append("body=${java.net.URLEncoder.encode(f3, "UTF-8")}")
                }
            }
            Triple(mailto, "Email", "Email")
        }
        CreateType.SMS -> {
            val sms = if (f2.isNotBlank()) "smsto:$f1:$f2" else "smsto:$f1"
            Triple(sms, "SMS", "SMS: $f1")
        }
        CreateType.PHONE -> Triple("tel:$f1", "Phone", "Phone: $f1")
        CreateType.GEO -> Triple("geo:$f1,$f2", "Geo", "Location")
        CreateType.CALENDAR -> {
            val cal = buildString {
                appendLine("BEGIN:VEVENT")
                appendLine("SUMMARY:$f1")
                if (f2.isNotBlank()) appendLine("LOCATION:$f2")
                if (f3.isNotBlank()) appendLine("DESCRIPTION:$f3")
                appendLine("END:VEVENT")
            }
            Triple(cal, "Calendar", f1.ifEmpty { "Event" })
        }
        CreateType.CLIPBOARD -> Triple(f1, "Text", "Clipboard")
    }
}
