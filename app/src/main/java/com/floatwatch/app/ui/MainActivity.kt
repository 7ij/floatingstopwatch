package com.floatwatch.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floatwatch.app.data.OverlayTheme
import com.floatwatch.app.data.PreferencesManager
import com.floatwatch.app.service.StopwatchOverlayService
import com.floatwatch.app.ui.theme.getOverlayThemeColors
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsManager = PreferencesManager(this)

        setContent {
            MaterialTheme {
                MainScreen(
                    prefsManager = prefsManager,
                    hasOverlayPermission = Settings.canDrawOverlays(this),
                    onRequestOverlayPermission = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    },
                    onStartFloatingService = {
                        StopwatchOverlayService.start(this)
                    },
                    onStopFloatingService = {
                        StopwatchOverlayService.stop(this)
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    prefsManager: PreferencesManager,
    hasOverlayPermission: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onStartFloatingService: () -> Unit,
    onStopFloatingService: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isProUser by prefsManager.isProUserFlow.collectAsState(initial = false)
    val selectedTheme by prefsManager.selectedThemeFlow.collectAsState(initial = OverlayTheme.MINIMAL_DARK)
    val opacity by prefsManager.opacityFlow.collectAsState(initial = 0.95f)
    val hapticEnabled by prefsManager.hapticEnabledFlow.collectAsState(initial = true)
    var showPaywallDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0C0E14)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FloatWatch",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Floating Stopwatch & Precision Overlay",
                            fontSize = 13.sp,
                            color = Color(0xFF8E99A8)
                        )
                    }

                    if (isProUser) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2A2000))
                                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "PRO UNLOCKED",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { showPaywallDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Go Pro", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Overlay Permission Card
            if (!hasOverlayPermission) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF281C16)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Permission Required",
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "FloatWatch needs 'Display over other apps' to float the stopwatch while you play games or study.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onRequestOverlayPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Allow Overlay Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Launch Floating Controls
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Overlay Controller",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Launch floating bubble. You can move, minimize, or lap over any application.",
                            color = Color(0xFF8E99A8),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    if (hasOverlayPermission) {
                                        onStartFloatingService()
                                    } else {
                                        onRequestOverlayPermission()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38EF7D)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Float", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onStopFloatingService,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282C38)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop Float", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Theme Selection
            item {
                Text(
                    text = "Overlay Skins & Themes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            items(OverlayTheme.values()) { theme ->
                val isSelected = (theme == selectedTheme)
                val colors = getOverlayThemeColors(theme)
                val canUse = !theme.isPro || isProUser

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF202636) else Color(0xFF141720)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF38EF7D) else Color(0xFF222733),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            if (canUse) {
                                coroutineScope.launch { prefsManager.setSelectedTheme(theme) }
                            } else {
                                showPaywallDialog = true
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = theme.displayName,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (theme.isPro) "PRO Skin" else "Free Skin",
                                    color = if (theme.isPro) Color(0xFFFFD700) else Color(0xFF8E99A8),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (!canUse) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFFD700))
                        } else if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF38EF7D))
                        }
                    }
                }
            }

            // Customization sliders
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Overlay Transparency",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(opacity * 100).toInt()}% Opacity",
                            color = Color(0xFF8E99A8),
                            fontSize = 13.sp
                        )
                        Slider(
                            value = opacity,
                            onValueChange = { newOpacity ->
                                coroutineScope.launch { prefsManager.setOpacity(newOpacity) }
                            },
                            valueRange = 0.3f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF38EF7D),
                                activeTrackColor = Color(0xFF38EF7D)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Haptic Vibrations",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Vibrate on play, pause, lap",
                                    color = Color(0xFF8E99A8),
                                    fontSize = 13.sp
                                )
                            }
                            Switch(
                                checked = hapticEnabled,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch { prefsManager.setHapticEnabled(checked) }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF38EF7D)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Paywall Dialog
    if (showPaywallDialog) {
        val context = LocalContext.current
        PaywallDialog(
            onDismiss = { showPaywallDialog = false },
            onUnlockPro = {
                coroutineScope.launch {
                    prefsManager.setProUser(true)
                    showPaywallDialog = false
                    Toast.makeText(context, "Pro features unlocked!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onUnlockPro: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E28)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF282C38))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Upgrade to FloatWatch Pro",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Unlock premium overlay skins, unlimited laps, and advanced HUD customization.",
                    color = Color(0xFF9EAAAF),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProPerkItem("Cyberpunk, Glassmorphism & OLED Skins")
                    ProPerkItem("Unlimited Lap Recording & Split Times")
                    ProPerkItem("Fine-tuned Opacity & Compact Pill Mode")
                    ProPerkItem("100% Ad-Free Forever")
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onUnlockPro,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38EF7D)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Unlock Pro ($2.99 Lifetime)",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProPerkItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF38EF7D), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.White, fontSize = 13.sp)
    }
}
