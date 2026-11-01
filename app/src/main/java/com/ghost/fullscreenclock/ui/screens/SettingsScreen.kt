package com.ghost.fullscreenclock.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.fullscreenclock.preferences.ClockPreferences
import com.ghost.fullscreenclock.theme.CharcoalCard
import com.ghost.fullscreenclock.theme.PureBlack
import com.ghost.fullscreenclock.theme.PureWhite
import com.ghost.fullscreenclock.theme.RobotoFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: ClockPreferences,
    is24Hour: Boolean,
    onTimeFormatChanged: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOnChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RobotoFont,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PureBlack
            )
        )

        HorizontalDivider(color = Color(0xFF222222))

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ================= 1. TIME FORMAT =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCard)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Time Format",
                    color = PureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = RobotoFont
                )

                Spacer(modifier = Modifier.height(14.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = !is24Hour,
                        onClick = { onTimeFormatChanged(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(text = "12-Hour", fontFamily = RobotoFont)
                    }

                    SegmentedButton(
                        selected = is24Hour,
                        onClick = { onTimeFormatChanged(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(text = "24-Hour", fontFamily = RobotoFont)
                    }
                }
            }

            // ================= 2. KEEP SCREEN ON =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCard)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Keep Screen On",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = RobotoFont
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prevent display from sleeping while clock is active",
                        color = PureWhite.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontFamily = RobotoFont
                    )
                }

                Switch(
                    checked = keepScreenOn,
                    onCheckedChange = onKeepScreenOnChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = Color(0xFF34C759),
                        uncheckedThumbColor = Color(0xFF888888),
                        uncheckedTrackColor = Color(0xFF333333)
                    )
                )
            }

            // ================= 3. PRIVACY & FEEDBACK =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCard)
            ) {
                // Feedback Row (opens user email client via mailto)
                SettingsActionRow(
                    icon = Icons.Default.Email,
                    title = "Feedback",
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_SUBJECT, "Fullscreen Clock Feedback")
                        }
                        try {
                            context.startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
                        } catch (_: Exception) {}
                    }
                )

                HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(horizontal = 16.dp))

                // Privacy Policy Row
                SettingsActionRow(
                    icon = Icons.Default.Lock,
                    title = "Privacy Policy",
                    onClick = { showPrivacyDialog = true }
                )
            }

            // ================= 4. ABOUT SECTION =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCard)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Fullscreen Clock",
                    color = PureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = RobotoFont
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Native Kotlin + Jetpack Compose Edition (v1.0.0)",
                    color = PureWhite.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    fontFamily = RobotoFont
                )
                Text(
                    text = "Target SDK 36 (Android 16)",
                    color = PureWhite.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = RobotoFont
                )
            }
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    fontFamily = RobotoFont
                )
            },
            text = {
                Text(
                    text = "Fullscreen Clock operates strictly on your local device. It does not collect, transmit, store, or share any personal information, location data, or telemetry.",
                    color = PureWhite.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontFamily = RobotoFont
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(
                        text = "Close",
                        color = Color(0xFF34C759),
                        fontWeight = FontWeight.Bold,
                        fontFamily = RobotoFont
                    )
                }
            },
            containerColor = CharcoalCard
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PureWhite.copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = RobotoFont
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open",
            tint = PureWhite.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}
