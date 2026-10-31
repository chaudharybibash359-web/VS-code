package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SidebarTab
import com.example.ui.editor.EditorTheme
import com.example.ui.theme.VSCodeActivityBarActive
import com.example.ui.theme.VSCodeActivityBarInactive

@Composable
fun ActivityBar(
    activeTab: SidebarTab,
    isSidebarOpen: Boolean,
    theme: EditorTheme,
    gitChangesCount: Int,
    onTabSelected: (SidebarTab) -> Unit,
    onRunClicked: () -> Unit,
    onCommandPaletteClicked: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(52.dp)
            .fillMaxHeight()
            .background(theme.activityBarBg)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section navigation icons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Hamburger button to open hierarchical side-navigation drawer
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.size(44.dp).testTag("activity_bar_hamburger_menu")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open File Tree Drawer",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            ActivityBarItem(
                icon = Icons.Default.Folder,
                contentDescription = "Explorer",
                isSelected = isSidebarOpen && activeTab == SidebarTab.EXPLORER,
                theme = theme,
                onClick = { onTabSelected(SidebarTab.EXPLORER) },
                testTag = "activity_bar_explorer"
            )

            ActivityBarItem(
                icon = Icons.Default.Search,
                contentDescription = "Search",
                isSelected = isSidebarOpen && activeTab == SidebarTab.SEARCH,
                theme = theme,
                onClick = { onTabSelected(SidebarTab.SEARCH) },
                testTag = "activity_bar_search"
            )

            ActivityBarItem(
                icon = Icons.Default.AccountTree,
                contentDescription = "Source Control",
                isSelected = isSidebarOpen && activeTab == SidebarTab.GIT,
                theme = theme,
                badgeCount = gitChangesCount,
                onClick = { onTabSelected(SidebarTab.GIT) },
                testTag = "activity_bar_git"
            )

            ActivityBarItem(
                icon = Icons.Default.Extension,
                contentDescription = "Extensions",
                isSelected = isSidebarOpen && activeTab == SidebarTab.EXTENSIONS,
                theme = theme,
                onClick = { onTabSelected(SidebarTab.EXTENSIONS) },
                testTag = "activity_bar_extensions"
            )

            ActivityBarItem(
                icon = Icons.Default.CloudDone,
                contentDescription = "Cloud Sync",
                isSelected = isSidebarOpen && activeTab == SidebarTab.CLOUD,
                theme = theme,
                onClick = { onTabSelected(SidebarTab.CLOUD) },
                testTag = "activity_bar_cloud"
            )
        }

        // Bottom section: Run & Settings & Command Palette
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Run / Debug action
            IconButton(
                onClick = onRunClicked,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF0E639C), RoundedCornerShape(8.dp))
                    .testTag("activity_bar_run")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run and Preview",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onCommandPaletteClicked,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("activity_bar_command_palette")
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Command Palette",
                    tint = VSCodeActivityBarInactive,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityBarItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    theme: EditorTheme,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Active indicator line on the left
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .fillMaxHeight(0.6f)
                    .background(theme.accent)
            )
        }

        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = theme.accent,
                        contentColor = Color.White
                    ) {
                        Text(badgeCount.toString(), fontSize = 10.sp)
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isSelected) VSCodeActivityBarActive else VSCodeActivityBarInactive,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) VSCodeActivityBarActive else VSCodeActivityBarInactive,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
