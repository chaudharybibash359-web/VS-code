package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FileEntity
import com.example.ui.CodeStudioUiState
import com.example.ui.CodeStudioViewModel
import com.example.ui.SidebarTab
import com.example.ui.editor.ThemeEngine

@Composable
fun SidebarView(
    state: CodeStudioUiState,
    viewModel: CodeStudioViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(state.currentTheme.sidebarBg)
    ) {
        // Sidebar header with title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when (state.activeSidebarTab) {
                    SidebarTab.EXPLORER -> "EXPLORER"
                    SidebarTab.SEARCH -> "SEARCH"
                    SidebarTab.GIT -> "SOURCE CONTROL"
                    SidebarTab.EXTENSIONS -> "EXTENSIONS"
                    SidebarTab.CLOUD -> "CLOUD SYNC"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = state.currentTheme.textPrimary.copy(alpha = 0.7f),
                letterSpacing = 1.2.sp
            )

            // Right-side actions based on tab
            when (state.activeSidebarTab) {
                SidebarTab.EXPLORER -> {
                    Row {
                        IconButton(
                            onClick = { viewModel.setShowNewFileDialog(true) },
                            modifier = Modifier.size(28.dp).testTag("sidebar_new_file_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New File",
                                tint = state.currentTheme.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setShowNewProjectDialog(true) },
                            modifier = Modifier.size(28.dp).testTag("sidebar_new_project_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = "New Project",
                                tint = state.currentTheme.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                SidebarTab.GIT -> {
                    IconButton(
                        onClick = { viewModel.stageAllFiles() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Stage All",
                            tint = state.currentTheme.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                else -> {}
            }
        }

        HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

        // Panel contents
        Box(modifier = Modifier.weight(1f)) {
            when (state.activeSidebarTab) {
                SidebarTab.EXPLORER -> ExplorerSection(state, viewModel)
                SidebarTab.SEARCH -> SearchSection(state, viewModel)
                SidebarTab.GIT -> GitSection(state, viewModel)
                SidebarTab.EXTENSIONS -> ExtensionsSection(state, viewModel)
                SidebarTab.CLOUD -> CloudSection(state, viewModel)
            }
        }
    }
}

@Composable
private fun ExplorerSection(state: CodeStudioUiState, viewModel: CodeStudioViewModel) {
    var projectMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Project Switcher Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { projectMenuExpanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(Color(0xFF2A2D2E), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color(0xFFE5A93C),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.currentProject?.name ?: "Select Project",
                    color = state.currentTheme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = state.currentTheme.textPrimary,
                modifier = Modifier.size(16.dp)
            )

            DropdownMenu(
                expanded = projectMenuExpanded,
                onDismissRequest = { projectMenuExpanded = false },
                modifier = Modifier.background(Color(0xFF252526))
            ) {
                state.projects.forEach { proj ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(proj.name, color = Color.White, fontSize = 13.sp)
                                Text(proj.description, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                            }
                        },
                        onClick = {
                            viewModel.loadProject(proj)
                            projectMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hierarchical File & Folder Tree
        Box(modifier = Modifier.fillMaxSize()) {
            HierarchicalFileTree(
                state = state,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun SearchSection(state: CodeStudioUiState, viewModel: CodeStudioViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search in project...", fontSize = 12.sp, color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("search_query_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF3C3C3C),
                unfocusedContainerColor = Color(0xFF3C3C3C),
                focusedBorderColor = state.currentTheme.accent,
                unfocusedBorderColor = Color(0xFF555555),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${state.searchResults.size} results found",
            fontSize = 11.sp,
            color = Color.Gray
        )

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(state.searchResults) { res ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openFile(res.file.id) }
                        .padding(vertical = 6.dp)
                        .background(Color(0xFF2A2D2E), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${res.file.name}:${res.lineNumber}",
                        color = state.currentTheme.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = res.lineText,
                        color = state.currentTheme.textPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun GitSection(state: CodeStudioUiState, viewModel: CodeStudioViewModel) {
    var commitMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Current branch badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Commit,
                contentDescription = null,
                tint = state.currentTheme.accent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Branch: ${state.currentProject?.activeBranch ?: "main"}",
                color = Color.White,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Changes list
        Text(
            text = "CHANGES (${state.dirtyFileBuffers.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        state.files.filter { state.dirtyFileBuffers.containsKey(it.id) }.forEach { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "M",
                        color = Color(0xFFE5A93C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.width(18.dp)
                    )
                    Text(file.name, color = state.currentTheme.textPrimary, fontSize = 12.sp)
                }
                Row {
                    IconButton(
                        onClick = { viewModel.setDiffComparingFile(file) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Difference,
                            contentDescription = "View Diff",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleStageFile(file.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Stage",
                            tint = if (state.stagedFileIds.contains(file.id)) state.currentTheme.accent else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Commit message input
        OutlinedTextField(
            value = commitMessage,
            onValueChange = { commitMessage = it },
            placeholder = { Text("Commit message (Ctrl+Enter)", fontSize = 11.sp, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().testTag("git_commit_msg_input"),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF3C3C3C),
                unfocusedContainerColor = Color(0xFF3C3C3C),
                focusedBorderColor = state.currentTheme.accent,
                unfocusedBorderColor = Color(0xFF555555),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.commitStaged(commitMessage)
                commitMessage = ""
            },
            modifier = Modifier.fillMaxWidth().testTag("git_commit_btn"),
            enabled = commitMessage.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = state.currentTheme.accent)
        ) {
            Text("Commit & Push", fontSize = 12.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Commits
        Text(
            text = "COMMITS (${state.commits.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 6.dp)) {
            items(state.commits) { commit ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D2E))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(commit.commitHash, color = state.currentTheme.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(commit.branchName, color = Color.Gray, fontSize = 10.sp)
                        }
                        Text(commit.message, color = Color.White, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtensionsSection(state: CodeStudioUiState, viewModel: CodeStudioViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "INSTALLED & MARKETPLACE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Theme Switcher Quick List
        Text(
            text = "COLOR THEMES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = state.currentTheme.accent
        )

        ThemeEngine.AllThemes.forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setTheme(theme) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(theme.accent, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = theme.name,
                        color = if (state.currentTheme.id == theme.id) state.currentTheme.accent else state.currentTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (state.currentTheme.id == theme.id) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (state.currentTheme.id == theme.id) {
                    Text("Active", color = state.currentTheme.accent, fontSize = 10.sp)
                }
            }
        }

        HorizontalDivider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 10.dp))

        // Developer Tool Extensions
        Text(
            text = "DEVELOPER EXTENSIONS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = state.currentTheme.accent
        )

        val tools = listOf(
            Triple("tool.livereload", "Live HTML5 Game Runner", "Auto reload canvas games on change"),
            Triple("tool.prettier", "Prettier Code Formatter", "Format HTML, CSS, JS with 2 spaces"),
            Triple("tool.gamedev", "Arcade Game Snippets Pack", "Canvas 2D game loops & touch controllers")
        )

        tools.forEach { (id, name, desc) ->
            val isEnabled = state.installedExtensions.contains(id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(desc, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.toggleExtension(id) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = state.currentTheme.accent
                    )
                )
            }
        }
    }
}

@Composable
private fun CloudSection(state: CodeStudioUiState, viewModel: CodeStudioViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "CLOUD PROJECT BACKUP & SYNC",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.triggerCloudSync() },
            modifier = Modifier.fillMaxWidth().testTag("cloud_sync_now_btn"),
            enabled = !state.isCloudSyncing,
            colors = ButtonDefaults.buttonColors(containerColor = state.currentTheme.accent)
        ) {
            if (state.isCloudSyncing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Syncing to Cloud...", fontSize = 12.sp)
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync Project Snapshot", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AVAILABLE CLOUD REVISIONS (${state.cloudBackups.size})",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(state.cloudBackups) { backup ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D2E))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(backup.projectName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Date: ${java.util.Date(backup.backupTimestamp)} (${backup.filesCount} files)",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.restoreBackup(backup.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restore",
                                tint = state.currentTheme.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
