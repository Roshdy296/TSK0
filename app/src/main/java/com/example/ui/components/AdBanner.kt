package com.example.ui.components

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

// Standard official Google AdMob sample test banner ad unit ID
const val ADMOB_TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

data class AdItem(
  val id: Int,
  val sponsorName: String,
  val headline: String,
  val tagLine: String,
  val callToAction: String,
  val category: String,
  val rating: Float,
  val badgeColor: Color
)

val sampleAds = listOf(
  AdItem(
    id = 1,
    sponsorName = "CloudSync Pro",
    headline = "Effortless Multi-Device Sync",
    tagLine = "Backup tasks & collaborate in real-time with team boards.",
    callToAction = "Try Free",
    category = "Productivity",
    rating = 4.8f,
    badgeColor = Color(0xFF3B82F6)
  ),
  AdItem(
    id = 2,
    sponsorName = "FocusFlow Audio",
    headline = "Binaural Beats for Deep Work",
    tagLine = "Science-backed soundscapes to eliminate distractions.",
    callToAction = "Listen Now",
    category = "Focus & Health",
    rating = 4.9f,
    badgeColor = Color(0xFF8B5CF6)
  ),
  AdItem(
    id = 3,
    sponsorName = "HabitCraft 365",
    headline = "Build Atomic Daily Streaks",
    tagLine = "Transform routines into lifelong habits effortlessly.",
    callToAction = "Install",
    category = "Self Improvement",
    rating = 4.7f,
    badgeColor = Color(0xFF10B981)
  )
)

@Composable
fun AdBanner(
  modifier: Modifier = Modifier,
  adUnitId: String = ADMOB_TEST_BANNER_AD_UNIT_ID,
  isAdVisible: Boolean = true,
  onCloseAd: () -> Unit = {}
) {
  var isAdLoaded by remember { mutableStateOf(false) }
  var adErrorMessage by remember { mutableStateOf<String?>(null) }
  var showAdMobInfoDialog by remember { mutableStateOf(false) }
  var showDismissConfirm by remember { mutableStateOf(false) }
  var currentAdIndex by remember { mutableIntStateOf(0) }

  val fallbackAd = sampleAds[currentAdIndex % sampleAds.size]

  AnimatedVisibility(
    visible = isAdVisible,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    Surface(
      modifier = modifier
        .fillMaxWidth()
        .testTag("ad_banner_container"),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
      shape = RoundedCornerShape(12.dp),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top banner header with AdMob label and info/close controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // "Ad" badge
            Box(
              modifier = Modifier
                .background(
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(4.dp)
                )
                .border(
                  width = 0.5.dp,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                  shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
              Text(
                text = "AdMob",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }

            Text(
              text = if (isAdLoaded) "Google Mobile Ads" else (adErrorMessage?.let { "AdMob Active" } ?: "AdMob Loading..."),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            IconButton(
              onClick = { showAdMobInfoDialog = true },
              modifier = Modifier
                .size(24.dp)
                .testTag("ad_info_button")
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Ad Info",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
              )
            }

            IconButton(
              onClick = { showDismissConfirm = true },
              modifier = Modifier
                .size(24.dp)
                .testTag("ad_close_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Hide Ad",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // AdMob Native AdView wrapped in AndroidView
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          AndroidView(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admob_ad_view"),
            factory = { ctx ->
              AdView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                  override fun onAdLoaded() {
                    isAdLoaded = true
                    adErrorMessage = null
                  }

                  override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdLoaded = false
                    adErrorMessage = error.message
                  }
                }
                loadAd(AdRequest.Builder().build())
              }
            }
          )

          // Fallback UI if AdMob view is loading or in development emulator without play services
          if (!isAdLoaded) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { showAdMobInfoDialog = true },
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    Brush.linearGradient(
                      colors = listOf(
                        fallbackAd.badgeColor,
                        fallbackAd.badgeColor.copy(alpha = 0.7f)
                      )
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = fallbackAd.sponsorName.take(1),
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              Column(
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = fallbackAd.headline,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = fallbackAd.tagLine,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Spacer(modifier = Modifier.width(6.dp))

              Button(
                onClick = { showAdMobInfoDialog = true },
                modifier = Modifier
                  .height(30.dp)
                  .testTag("ad_cta_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                  horizontal = 8.dp,
                  vertical = 0.dp
                )
              ) {
                Text(
                  text = fallbackAd.callToAction,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog when clicking AdMob info
  if (showAdMobInfoDialog) {
    AlertDialog(
      onDismissRequest = { showAdMobInfoDialog = false },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AdUnits,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Text("Google AdMob Integration")
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "AdMob Banner Status: ${if (isAdLoaded) "Active & Loaded" else "Ready (Test Mode)"}",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (isAdLoaded) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
            )
          )

          Text(
            text = "Using official Google Mobile Ads SDK (play-services-ads).",
            style = MaterialTheme.typography.bodyMedium
          )

          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "Ad Unit ID:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = adUnitId,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
              if (adErrorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Note: $adErrorMessage",
                  style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            currentAdIndex++
            showAdMobInfoDialog = false
          },
          modifier = Modifier.testTag("ad_info_dismiss_button")
        ) {
          Text("Got it")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAdMobInfoDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // Dismiss confirmation
  if (showDismissConfirm) {
    AlertDialog(
      onDismissRequest = { showDismissConfirm = false },
      title = { Text("Ad Preferences") },
      text = {
        Text("Would you like to hide this AdMob banner for your current session?")
      },
      confirmButton = {
        Button(
          onClick = {
            showDismissConfirm = false
            onCloseAd()
          },
          modifier = Modifier.testTag("confirm_hide_ad_button")
        ) {
          Text("Hide Banner")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDismissConfirm = false }) {
          Text("Keep Visible")
        }
      }
    )
  }
}
