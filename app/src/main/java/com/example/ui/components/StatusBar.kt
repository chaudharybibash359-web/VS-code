package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BottomPanelTab
import com.example.ui.CodeStudioUiState
import com.example.ui.CodeStudioViewModel

@Composable
fun StatusBar(
    state: CodeStudioUiState,
    viewModel: CodeStudioViewModel,
    modifier: Modifier = Modifier
) {
    val activeFile = state.files.find { it.id == state.activeFileId }
    val ext = activeFile?.name?.substringAfterLast('.', "")?.uppercase() ?: "PLAIN TEXT"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(state.currentTheme.statusBarBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left items: Git Branch, Sync status
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clickable { viewModel.selectSidebarTab(com.example.ui.SidebarTab.GIT) }
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Commit,
                    contentDescription = "Git Branch",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = state.currentProject?.activeBranch ?: "main",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cloud sync indicator
            Row(
                modifier = Modifier
                    .clickable { viewModel.triggerCloudSync() }
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (state.isCloudSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                    contentDescription = "Cloud Status",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (state.isCloudSyncing) "Syncing..." else "Cloud Synced",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = state.statusMessage,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp
            )
        }

        // Right items: Line/Col, Encoding, Language, Quick panel toggles
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Ln 1, Col 1",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = "Spaces: 2",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = "UTF-8",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = ext,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Quick toggle for terminal
            Row(
                modifier = Modifier
                    .clickable { viewModel.toggleBottomPanel(BottomPanelTab.TERMINAL) }
                    .padding(horizontal = 4.dp)
                    .testTag("status_bar_terminal_toggle"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Quick toggle for preview
            Row(
                modifier = Modifier
                    .clickable { viewModel.toggleBottomPanel(BottomPanelTab.PREVIEW) }
                    .padding(horizontal = 4.dp)
                    .testTag("status_bar_preview_toggle"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Preview",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
