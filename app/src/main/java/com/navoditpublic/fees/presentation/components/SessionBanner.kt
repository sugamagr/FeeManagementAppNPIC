package com.navoditpublic.fees.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.navoditpublic.fees.domain.session.SelectedSessionInfo
import com.navoditpublic.fees.domain.session.SelectedSessionManager
import com.navoditpublic.fees.domain.usecase.SessionAccessLevel

// Color palette
private val AccentPurple = Color(0xFF7C4DFF)
private val AccentPurpleLight = Color(0xFFEDE7F6)
private val InactiveGray = Color(0xFF9E9E9E)
private val InactiveGrayLight = Color(0xFFF5F5F5)

/**
 * Banner that shows when viewing a historical session (not the current session).
 * 
 * @param selectedSessionManager The session manager to get current selection from
 * @param onSwitchClick Called when user clicks the switch button to go to Academic Sessions
 * @param modifier Optional modifier
 */
@Composable
fun SessionBanner(
    selectedSessionManager: SelectedSessionManager,
    onSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessionInfo by selectedSessionManager.selectedSessionInfo.collectAsState()
    
    // Only show if viewing a historical session
    val isHistorical = sessionInfo?.isHistoricalSession == true
    
    AnimatedVisibility(
        visible = isHistorical,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        sessionInfo?.let { info ->
            SessionBannerContent(
                sessionInfo = info,
                onSwitchClick = onSwitchClick,
                modifier = modifier
            )
        }
    }
}

/**
 * Standalone banner content without animation.
 * Use this when you need direct control over visibility.
 */
@Composable
fun SessionBannerContent(
    sessionInfo: SelectedSessionInfo,
    onSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreviousSession = sessionInfo.accessLevel == SessionAccessLevel.PREVIOUS_SESSION
    val bgColor = if (isPreviousSession) AccentPurpleLight else InactiveGrayLight
    val accentColor = if (isPreviousSession) AccentPurple else InactiveGray
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(0.dp) // Full width, no rounded corners
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSwitchClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPreviousSession) Icons.Outlined.History else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Viewing: ${sessionInfo.session.sessionName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = sessionInfo.sessionTypeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isPreviousSession) {
                            "Editing allowed • Dues for active students only"
                        } else {
                            "Read-only • Dues for active students only"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Switch button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.clickable { onSwitchClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Switch",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Switch Session",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Simplified banner that shows just the essential info.
 * Use in screens where space is limited.
 */
@Composable
fun SessionBannerCompact(
    sessionInfo: SelectedSessionInfo?,
    onSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessionInfo == null || sessionInfo.isCurrentSession) return
    
    val isPreviousSession = sessionInfo.accessLevel == SessionAccessLevel.PREVIOUS_SESSION
    val bgColor = if (isPreviousSession) AccentPurpleLight else InactiveGrayLight
    val accentColor = if (isPreviousSession) AccentPurple else InactiveGray
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSwitchClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Viewing ${sessionInfo.session.sessionName} (${sessionInfo.sessionTypeLabel})",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = accentColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Switch →",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
        }
    }
}
