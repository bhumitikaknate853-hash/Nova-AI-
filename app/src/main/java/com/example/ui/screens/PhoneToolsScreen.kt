package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PhoneAction
import com.example.data.model.PhoneActionType
import com.example.viewmodel.NovaScreen
import com.example.viewmodel.NovaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneToolsScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pendingAction by viewModel.pendingPhoneAction.collectAsState()
    var promptInput by remember { mutableStateOf("") }

    val phoneActions = listOf(
        PhoneAction(
            title = "Open Web Browser",
            subtitle = "Launch Chrome or default browser to a URL",
            iconName = "browser",
            actionType = PhoneActionType.OPEN_BROWSER
        ),
        PhoneAction(
            title = "Search Google",
            subtitle = "Perform a Google query via system browser",
            iconName = "search",
            actionType = PhoneActionType.SEARCH_GOOGLE
        ),
        PhoneAction(
            title = "Open YouTube",
            subtitle = "Search and launch video topics in YouTube",
            iconName = "youtube",
            actionType = PhoneActionType.SEARCH_YOUTUBE
        ),
        PhoneAction(
            title = "Maps & Navigation",
            subtitle = "Find addresses, routes, or nearby points of interest",
            iconName = "maps",
            actionType = PhoneActionType.OPEN_MAPS
        ),
        PhoneAction(
            title = "Open Camera",
            subtitle = "Launch device camera via Android capture intent",
            iconName = "camera",
            actionType = PhoneActionType.OPEN_CAMERA
        ),
        PhoneAction(
            title = "Set Alarm / Reminder",
            subtitle = "Open system clock with message prefilled",
            iconName = "alarm",
            actionType = PhoneActionType.CREATE_REMINDER
        ),
        PhoneAction(
            title = "Send Text Message (SMS)",
            subtitle = "Prepare message in system SMS application",
            iconName = "sms",
            actionType = PhoneActionType.SEND_SMS
        ),
        PhoneAction(
            title = "Open Phone Dialer",
            subtitle = "Launch system dialer with number pre-filled",
            iconName = "dialer",
            actionType = PhoneActionType.DIAL_PHONE
        ),
        PhoneAction(
            title = "Android Settings",
            subtitle = "Open system settings for Wi-Fi, Bluetooth, Apps",
            iconName = "settings",
            actionType = PhoneActionType.SYSTEM_SETTINGS
        ),
        PhoneAction(
            title = "Share NOVA AI",
            subtitle = "Open Android system share sheet",
            iconName = "share",
            actionType = PhoneActionType.SHARE_APP
        )
    )

    // Action Confirmation Dialog
    if (pendingAction != null) {
        val action = pendingAction!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissPhoneAction() },
            title = {
                Text(
                    text = "Confirm: ${action.title}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "NOVA will open the native Android system handler for this operation. Sensitive operations always require your explicit review.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val inputLabel = when (action.actionType) {
                        PhoneActionType.OPEN_BROWSER -> "Website URL"
                        PhoneActionType.SEARCH_GOOGLE,
                        PhoneActionType.SEARCH_YOUTUBE -> "Search Query"
                        PhoneActionType.OPEN_MAPS -> "Location / Address"
                        PhoneActionType.CREATE_REMINDER -> "Alarm / Reminder Note"
                        PhoneActionType.SEND_SMS -> "Recipient Phone Number"
                        PhoneActionType.DIAL_PHONE -> "Phone Number"
                        else -> null
                    }

                    if (inputLabel != null) {
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            label = { Text(inputLabel) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_action_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = promptInput
                        promptInput = ""
                        viewModel.executePhoneAction(action, context, input)
                    },
                    modifier = Modifier.testTag("confirm_phone_action_button")
                ) {
                    Text("Execute Action")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissPhoneAction() },
                    modifier = Modifier.testTag("cancel_phone_action_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("phone_tools_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Android Phone Tools", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(NovaScreen.DASHBOARD) },
                        modifier = Modifier.testTag("phone_tools_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Actions use standard Android system intents. NOVA will never silently place calls or transmit private data without your confirmation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(phoneActions.size) { index ->
                val item = phoneActions[index]
                PhoneActionItem(
                    action = item,
                    onClick = {
                        promptInput = ""
                        viewModel.requestPhoneAction(item)
                    }
                )
            }
        }
    }
}

@Composable
fun PhoneActionItem(
    action: PhoneAction,
    onClick: () -> Unit
) {
    val icon = when (action.actionType) {
        PhoneActionType.OPEN_BROWSER -> Icons.Default.Public
        PhoneActionType.SEARCH_GOOGLE -> Icons.Default.Search
        PhoneActionType.SEARCH_YOUTUBE -> Icons.Default.PlayCircle
        PhoneActionType.OPEN_MAPS -> Icons.Default.Map
        PhoneActionType.OPEN_CAMERA -> Icons.Default.CameraAlt
        PhoneActionType.CREATE_REMINDER -> Icons.Default.Alarm
        PhoneActionType.SEND_SMS -> Icons.Default.Sms
        PhoneActionType.DIAL_PHONE -> Icons.Default.Call
        PhoneActionType.SYSTEM_SETTINGS -> Icons.Default.Settings
        PhoneActionType.SHARE_APP -> Icons.Default.Share
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("action_item_${action.actionType.name}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
