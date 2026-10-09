package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CommunitiesVerifyIllustration
import com.example.ui.components.ConfidenceChangesIllustration
import com.example.ui.components.DataLocalIllustration
import com.example.ui.components.PeopleNodesIllustration
import com.example.ui.components.RealtimeAlertsIllustration
import com.example.ui.components.SplashCityIllustration
import com.example.ui.theme.PrimaryBlue

@Composable
fun OnboardingScreen(
    currentStep: Int,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Content area based on step
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (currentStep) {
                0 -> SplashWelcomeStep(onGetStarted = onNextStep, onExploreGuest = onDismiss)
                1 -> HowItWorksStep()
                2 -> PeopleBecomeNodesStep()
                3 -> DataStaysLocalStep()
                4 -> CommunitiesVerifyStep()
                5 -> ConfidenceChangesStep()
                6 -> RealtimeAlertsStep()
                else -> RealtimeAlertsStep()
            }
        }

        // Bottom Navigation Controls (Steps 1 to 6)
        if (currentStep > 0) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    for (i in 1..6) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == currentStep) PrimaryBlue else Color(0xFFCBD5E1)
                                )
                        )
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        if (currentStep >= 6) {
                            onDismiss()
                        } else {
                            onNextStep()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = if (currentStep == 6) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 1. Splash / Welcome
@Composable
private fun SplashWelcomeStep(
    onGetStarted: () -> Unit,
    onExploreGuest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            // Logo emblem: Location pin with eye inside
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "CrowdWatch Emblem",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CrowdWatch",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "See what's happening.\nVerify what's real.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                ),
                color = Color(0xFF334155),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A decentralized real-time\ncommunity intelligence network.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }

        // City landscape illustration
        SplashCityIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        // Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("splash_get_started"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
                onClick = onExploreGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("splash_explore_guest"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
            ) {
                Text("Explore as Guest", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// 2. How It Works
@Composable
private fun HowItWorksStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "How CrowdWatch Works",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 20.dp, top = 8.dp)
        )

        // 6 steps
        HowItWorksRow(
            number = "1. Observe",
            description = "Notice something around you.",
            icon = Icons.Default.Visibility,
            iconBg = Color(0xFFFEE2E2),
            iconTint = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(14.dp))

        HowItWorksRow(
            number = "2. Report",
            description = "Add evidence and location.",
            icon = Icons.Default.Edit,
            iconBg = Color(0xFFDBEAFE),
            iconTint = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(14.dp))

        HowItWorksRow(
            number = "3. Verify",
            description = "Community confirms or disputes.",
            icon = Icons.Default.CheckCircle,
            iconBg = Color(0xFFF3E8FF),
            iconTint = Color(0xFF9333EA)
        )
        Spacer(modifier = Modifier.height(14.dp))

        HowItWorksRow(
            number = "4. Build Confidence",
            description = "More evidence = higher trust.",
            icon = Icons.Default.Group,
            iconBg = Color(0xFFFEF3C7),
            iconTint = Color(0xFFD97706)
        )
        Spacer(modifier = Modifier.height(14.dp))

        HowItWorksRow(
            number = "5. Inform",
            description = "Relevant people get notified.",
            icon = Icons.Default.Notifications,
            iconBg = Color(0xFFDCFCE7),
            iconTint = Color(0xFF16A34A)
        )
        Spacer(modifier = Modifier.height(14.dp))

        HowItWorksRow(
            number = "6. Take Action",
            description = "Stay safe and informed.",
            icon = Icons.Default.Security,
            iconBg = Color(0xFFCFFAFE),
            iconTint = Color(0xFF0891B2)
        )
    }
}

@Composable
private fun HowItWorksRow(
    number: String,
    description: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = number,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
        }
    }
}

// 3. People become nodes
@Composable
private fun PeopleBecomeNodesStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PeopleNodesIllustration(
            modifier = Modifier
                .size(240.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "People become nodes",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Every device is a node in the network. Together we create a real-time picture of what's happening.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// 4. Data stays local
@Composable
private fun DataStaysLocalStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DataLocalIllustration(
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Data stays local",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your reports and evidence are stored on your device. You control what you share and when.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// 5. Communities verify
@Composable
private fun CommunitiesVerifyStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CommunitiesVerifyIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Communities verify",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Nearby users confirm, add evidence, or dispute information. This builds trust and accuracy.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// 6. Confidence changes
@Composable
private fun ConfidenceChangesStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ConfidenceChangesIllustration(
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Confidence changes",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "As more evidence and independent observations arrive, the community confidence grows (or decreases).",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// 7. Get real-time alerts
@Composable
private fun RealtimeAlertsStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RealtimeAlertsIllustration(
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Get real-time alerts",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Receive relevant information based on your location, interests, and safety needs.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
