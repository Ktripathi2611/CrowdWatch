package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auth.AuthState
import com.example.data.model.IncidentReport
import com.example.data.model.ReportStatus
import com.example.data.model.UserProfile
import com.example.engine.FreshnessEngine
import com.example.ui.components.ConfidencePill
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryBlue

/**
 * Production-grade User Profile UI with:
 * - Firebase Auth integration (Google Sign-In, Anonymous Guest, credentials, UID, verification badges)
 * - Complete Account Status & Device Cryptographic Identity monitoring
 * - User Reporting History with community verification statistics and impact tracking
 */
@Composable
fun ProfileScreen(
    profile: UserProfile,
    authState: AuthState = AuthState(),
    userReports: List<IncidentReport> = emptyList(),
    onReportClick: (String) -> Unit = {},
    onCreateReportClick: () -> Unit = {},
    onSignInGoogle: () -> Unit = {},
    onSignInAnonymous: () -> Unit = {},
    onSignInEmail: (String, String) -> Unit = { _, _ -> },
    onSignUpEmail: (String, String, String) -> Unit = { _, _, _ -> },
    onSignOut: () -> Unit = {},
    onShowOnboarding: () -> Unit = {},
    onOpenNetworkSimulator: () -> Unit = {},
    onClearCache: () -> Unit = {},
    onUpdatePrivacy: (String, Boolean, Int) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrowdWatchColors.Background)
    ) {
        // App Bar
        Surface(
            color = CrowdWatchColors.Surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "User Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = CrowdWatchColors.TextPrimary
                    )
                    Text(
                        text = "Account Status & History",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrowdWatchColors.TextSecondary
                    )
                }

                IconButton(
                    onClick = onShowOnboarding,
                    modifier = Modifier.testTag("profile_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CrowdWatchColors.TextSecondary
                    )
                }
            }
        }

        // Segmented Tabs: [Account Status & Identity] / [Reporting History (N)]
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = CrowdWatchColors.Surface,
            contentColor = CrowdWatchColors.PrimaryBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = CrowdWatchColors.PrimaryBlue,
                    height = 3.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CrowdWatchColors.Border)
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ManageAccounts,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Account & Status",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_account_status")
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reporting History (${userReports.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_reporting_history")
            )
        }

        // Tab Content
        if (selectedTabIndex == 0) {
            AccountStatusTabContent(
                profile = profile,
                authState = authState,
                reportsCount = userReports.size,
                onSignInGoogle = onSignInGoogle,
                onSignInAnonymous = onSignInAnonymous,
                onSignInEmail = onSignInEmail,
                onSignUpEmail = onSignUpEmail,
                onSignOut = onSignOut,
                onOpenNetworkSimulator = onOpenNetworkSimulator,
                onClearCache = onClearCache,
                onCopyUid = { uid ->
                    clipboardManager.setText(AnnotatedString(uid))
                    Toast.makeText(context, "Copied UID to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            ReportingHistoryTabContent(
                userReports = userReports,
                onReportClick = onReportClick,
                onCreateReportClick = onCreateReportClick
            )
        }
    }
}

/**
 * Tab 1: Account Status, Firebase Authentication details, and Cryptographic Node Status.
 */
@Composable
private fun AccountStatusTabContent(
    profile: UserProfile,
    authState: AuthState,
    reportsCount: Int,
    onSignInGoogle: () -> Unit,
    onSignInAnonymous: () -> Unit,
    onSignInEmail: (String, String) -> Unit,
    onSignUpEmail: (String, String, String) -> Unit,
    onSignOut: () -> Unit,
    onOpenNetworkSimulator: () -> Unit,
    onClearCache: () -> Unit,
    onCopyUid: (String) -> Unit
) {
    var showEmailAuthDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Identity Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar (Photo from Google if available, or fallback)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(CrowdWatchColors.PrimaryBlueBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!authState.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = authState.photoUrl,
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = CrowdWatchColors.PrimaryBlue,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val displayName = when {
                        authState.displayName.isNotBlank() -> authState.displayName
                        profile.displayName.isNotBlank() -> profile.displayName
                        else -> "Civic Contributor"
                    }

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = CrowdWatchColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Account Status Pill
                    val (statusLabel, statusColor, statusBg, statusIcon) = when {
                        authState.isAuthenticated && !authState.isAnonymous -> Quadruple(
                            "Google Account Verified",
                            CrowdWatchColors.ConfidenceHighText,
                            CrowdWatchColors.ConfidenceHighBg,
                            Icons.Default.CheckCircle
                        )
                        authState.isAuthenticated && authState.isAnonymous -> Quadruple(
                            "Anonymous Civic Node",
                            CrowdWatchColors.PrimaryBlue,
                            CrowdWatchColors.PrimaryBlueBg,
                            Icons.Default.Shield
                        )
                        else -> Quadruple(
                            "Unlinked Offline Node",
                            CrowdWatchColors.ConfidenceMediumText,
                            CrowdWatchColors.ConfidenceMediumBg,
                            Icons.Default.Info
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }

        // Firebase Auth Integration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .background(CrowdWatchColors.PrimaryBlueBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = CrowdWatchColors.PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firebase Auth Status",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CrowdWatchColors.TextPrimary
                                )
                                Text(
                                    text = "Decentralized reputation & account sync",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrowdWatchColors.TextSecondary
                                )
                            }
                        }

                        // Active status badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (authState.isAuthenticated) CrowdWatchColors.ConfidenceHighBg else CrowdWatchColors.ConfidenceLowBg
                        ) {
                            Text(
                                text = if (authState.isAuthenticated) "ACTIVE" else "GUEST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (authState.isAuthenticated) CrowdWatchColors.ConfidenceHighText else CrowdWatchColors.ConfidenceLowText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account details breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrowdWatchColors.Background, RoundedCornerShape(14.dp))
                            .border(1.dp, CrowdWatchColors.Border, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Email Field
                        AccountDetailRow(
                            icon = Icons.Default.Email,
                            label = "Account Email",
                            value = authState.email ?: "Operating without email (Anonymous Node)",
                            hasBadge = authState.email != null,
                            badgeText = "Verified"
                        )

                        // Auth Provider
                        val providerName = when {
                            !authState.isAnonymous && authState.email != null -> "Google Sign-In (Credential Manager)"
                            authState.isAnonymous -> "Firebase Anonymous Authentication"
                            else -> "Local Node (Decentralized Mesh Only)"
                        }
                        AccountDetailRow(
                            icon = Icons.Default.ManageAccounts,
                            label = "Identity Provider",
                            value = providerName
                        )

                        // UID with Copy Action
                        if (authState.uid.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = CrowdWatchColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Firebase UID",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CrowdWatchColors.TextSecondary
                                        )
                                        Text(
                                            text = authState.uid,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            color = CrowdWatchColors.TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onCopyUid(authState.uid) },
                                    modifier = Modifier.size(32.dp).testTag("copy_uid_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy UID",
                                        tint = CrowdWatchColors.PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Local Node Identity
                        AccountDetailRow(
                            icon = Icons.Default.Lock,
                            label = "Hardware Key Identity",
                            value = profile.nodeId,
                            badgeText = "NIST P-256",
                            hasBadge = true
                        )
                    }

                    // Error Message Banner (if any)
                    authState.errorMessage?.let { errorMsg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CrowdWatchColors.Emergency.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Emergency.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = CrowdWatchColors.Emergency,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrowdWatchColors.Emergency
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign-In Actions
                    if (authState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp),
                                color = CrowdWatchColors.PrimaryBlue
                            )
                        }
                    } else if (authState.isAuthenticated && !authState.isAnonymous) {
                        // User is signed in with Google
                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = EmergencyRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("profile_sign_out_button")
                        ) {
                            Text("Sign Out of Account", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // User is anonymous or unlinked
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onSignInGoogle,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrowdWatchColors.PrimaryBlue),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(46.dp)
                                        .testTag("profile_sign_in_google_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign In with Google", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = onSignInAnonymous,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("profile_guest_node_button")
                                ) {
                                    Text("Guest Node", color = CrowdWatchColors.TextPrimary, fontSize = 13.sp)
                                }
                            }

                            // Email & Password Sign-In Button
                            OutlinedButton(
                                onClick = { showEmailAuthDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.PrimaryBlue.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("profile_email_login_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = CrowdWatchColors.PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In with Email / Password", color = CrowdWatchColors.PrimaryBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Email Authentication Dialog
        if (showEmailAuthDialog) {
            item {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showEmailAuthDialog = false },
                    title = {
                        Text(
                            text = if (isRegisterMode) "Create Civic Account" else "Sign In to Account",
                            fontWeight = FontWeight.Bold,
                            color = CrowdWatchColors.TextPrimary
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isRegisterMode) {
                                OutlinedTextField(
                                    value = displayNameInput,
                                    onValueChange = { displayNameInput = it },
                                    label = { Text("Display Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                androidx.compose.material3.TextButton(
                                    onClick = { isRegisterMode = !isRegisterMode }
                                ) {
                                    Text(
                                        text = if (isRegisterMode) "Already registered? Sign In" else "New node? Create Account",
                                        fontSize = 12.sp,
                                        color = CrowdWatchColors.PrimaryBlue
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                    if (isRegisterMode) {
                                        onSignUpEmail(emailInput.trim(), passwordInput.trim(), displayNameInput.trim())
                                    } else {
                                        onSignInEmail(emailInput.trim(), passwordInput.trim())
                                    }
                                    showEmailAuthDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrowdWatchColors.PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isRegisterMode) "Create Account" else "Sign In")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showEmailAuthDialog = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        // Community Reliability & Trust Score Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CrowdWatchColors.ConfidenceHighBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = CrowdWatchColors.ConfidenceHigh,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Community Reliability",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CrowdWatchColors.TextPrimary
                                )
                                Text(
                                    text = "Trust level in peer-to-peer consensus",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrowdWatchColors.TextSecondary
                                )
                            }
                        }

                        Text(
                            text = "${profile.reliabilityScore} / 100",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CrowdWatchColors.ConfidenceHigh
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { profile.reliabilityScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CrowdWatchColors.ConfidenceHigh,
                        trackColor = CrowdWatchColors.Border
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4-Column Activity Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatColumn(
                            count = "${maxOf(profile.reportsSubmitted, reportsCount)}",
                            label = "Reports",
                            countColor = CrowdWatchColors.TextPrimary
                        )
                        ProfileStatColumn(
                            count = "${profile.verificationsCount}",
                            label = "Verifications",
                            countColor = CrowdWatchColors.PrimaryBlue
                        )
                        ProfileStatColumn(
                            count = "${profile.confirmedCount}",
                            label = "Confirmed",
                            countColor = CrowdWatchColors.ConfidenceHigh
                        )
                        ProfileStatColumn(
                            count = "${profile.disputedCount}",
                            label = "Disputed",
                            countColor = CrowdWatchColors.ConfidenceLow
                        )
                    }
                }
            }
        }

        // P2P Simulator & Cache Management
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenNetworkSimulator,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = CrowdWatchColors.PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("P2P Mesh", color = CrowdWatchColors.PrimaryBlue, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onClearCache,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = CrowdWatchColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Cache", color = CrowdWatchColors.TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Tab 2: User's Reporting History with verification status and impact metrics.
 */
@Composable
private fun ReportingHistoryTabContent(
    userReports: List<IncidentReport>,
    onReportClick: (String) -> Unit,
    onCreateReportClick: () -> Unit
) {
    var historyFilter by remember { mutableStateOf<ReportStatus?>(null) }

    val filteredList = if (historyFilter == null) {
        userReports
    } else {
        userReports.filter { it.status == historyFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Reporting Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Reports Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CrowdWatchColors.TextPrimary
                        )

                        TextButton(
                            onClick = onCreateReportClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CrowdWatchColors.PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Report", color = CrowdWatchColors.PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatColumn(
                            count = "${userReports.size}",
                            label = "Total Sent",
                            countColor = CrowdWatchColors.TextPrimary
                        )
                        ProfileStatColumn(
                            count = "${userReports.count { it.status == ReportStatus.VERIFIED || it.confidenceScore >= 70 }}",
                            label = "Verified",
                            countColor = CrowdWatchColors.ConfidenceHigh
                        )
                        ProfileStatColumn(
                            count = "${userReports.count { it.status == ReportStatus.ACTIVE }}",
                            label = "Active",
                            countColor = CrowdWatchColors.PrimaryBlue
                        )
                        ProfileStatColumn(
                            count = "${userReports.count { it.status == ReportStatus.RESOLVED }}",
                            label = "Resolved",
                            countColor = CrowdWatchColors.TextSecondary
                        )
                    }
                }
            }
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = historyFilter == null,
                    onClick = { historyFilter = null },
                    label = { Text("All (${userReports.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrowdWatchColors.PrimaryBlueBg,
                        selectedLabelColor = CrowdWatchColors.PrimaryBlue
                    )
                )

                FilterChip(
                    selected = historyFilter == ReportStatus.ACTIVE,
                    onClick = { historyFilter = if (historyFilter == ReportStatus.ACTIVE) null else ReportStatus.ACTIVE },
                    label = { Text("Active") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrowdWatchColors.PrimaryBlueBg,
                        selectedLabelColor = CrowdWatchColors.PrimaryBlue
                    )
                )

                FilterChip(
                    selected = historyFilter == ReportStatus.VERIFIED,
                    onClick = { historyFilter = if (historyFilter == ReportStatus.VERIFIED) null else ReportStatus.VERIFIED },
                    label = { Text("Verified") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrowdWatchColors.ConfidenceHighBg,
                        selectedLabelColor = CrowdWatchColors.ConfidenceHighText
                    )
                )

                FilterChip(
                    selected = historyFilter == ReportStatus.RESOLVED,
                    onClick = { historyFilter = if (historyFilter == ReportStatus.RESOLVED) null else ReportStatus.RESOLVED },
                    label = { Text("Resolved") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrowdWatchColors.Surface,
                        selectedLabelColor = CrowdWatchColors.TextPrimary
                    )
                )
            }
        }

        // Empty state or Report list
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(CrowdWatchColors.PrimaryBlueBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = CrowdWatchColors.PrimaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (userReports.isEmpty()) "No Reports Submitted Yet" else "No Matching Reports",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CrowdWatchColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (userReports.isEmpty())
                                "Reports you broadcast to the decentralized mesh network will be tracked here with live community verification updates."
                            else
                                "No reports found matching this status filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrowdWatchColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (userReports.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCreateReportClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrowdWatchColors.PrimaryBlue),
                                modifier = Modifier.testTag("profile_create_first_report_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit First Report", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { report ->
                UserHistoryReportCard(
                    report = report,
                    onClick = { onReportClick(report.id) }
                )
            }
        }
    }
}

/**
 * Individual Report Item Card for User's History.
 */
@Composable
private fun UserHistoryReportCard(
    report: IncidentReport,
    onClick: () -> Unit
) {
    val categoryColor = getCategoryColor(report.category)
    val timeAgo = FreshnessEngine.formatTimeAgo(report.createdAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("user_history_report_${report.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CrowdWatchColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Category + Timestamp + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, categoryColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(report.category),
                            contentDescription = report.category.displayName,
                            tint = categoryColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = report.category.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeAgo,
                        fontSize = 11.sp,
                        color = CrowdWatchColors.TextTertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View Details",
                        tint = CrowdWatchColors.TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title and Subcategory
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = CrowdWatchColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (report.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CrowdWatchColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: Area tag + Confidence score + Verification status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${report.areaName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CrowdWatchColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ConfidencePill(score = report.confidenceScore)

                    // Verifications count
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CrowdWatchColors.PrimaryBlueBg
                    ) {
                        Text(
                            text = "${report.verificationsCount} verified",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrowdWatchColors.PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    hasBadge: Boolean = false,
    badgeText: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CrowdWatchColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = CrowdWatchColors.TextSecondary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = CrowdWatchColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (hasBadge && badgeText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CrowdWatchColors.ConfidenceHighBg
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrowdWatchColors.ConfidenceHighText,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(count: String, label: String, countColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = countColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = CrowdWatchColors.TextSecondary
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
