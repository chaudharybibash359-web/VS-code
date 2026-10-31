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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.FileEntity
import com.example.ui.BottomPanelTab
import com.example.ui.CodeStudioUiState
import com.example.ui.CodeStudioViewModel
import com.example.ui.SidebarTab

data class CommandItem(
    val title: String,
    val shortcut: String,
    val action: () -> Unit
)

@Composable
fun CommandPaletteDialog(
    state: CodeStudioUiState,
    viewModel: CodeStudioViewModel
) {
    if (!state.isCommandPaletteOpen) return

    var query by remember { mutableStateOf("") }

    val allCommands = remember {
        listOf(
            CommandItem("> Build: Compile & Install Mobile App (APK)", "") { viewModel.buildAndPackageApp() },
            CommandItem("> Run: Launch Installed Standalone App", "") { viewModel.launchInstalledApp() },
            CommandItem("> File: Save File", "Ctrl+S") { viewModel.saveActiveFile() },
            CommandItem("> Edit: Find and Replace in File", "Ctrl+F") { viewModel.setFindReplaceOpen(true) },
            CommandItem("> Edit: Format Document", "") { viewModel.formatActiveDocument() },
            CommandItem("> File: New File", "") { viewModel.setShowNewFileDialog(true) },
            CommandItem("> File: New Project from Template", "") { viewModel.setShowNewProjectDialog(true) },
            CommandItem("> Terminal: Toggle Integrated Terminal", "Ctrl+`") { viewModel.toggleBottomPanel(BottomPanelTab.TERMINAL) },
            CommandItem("> Run: Open Live Game & App Preview", "") { viewModel.openPreview() },
            CommandItem("> Git: Open Source Control", "") { viewModel.selectSidebarTab(SidebarTab.GIT) },
            CommandItem("> Git: Commit Staged Changes", "") { viewModel.selectSidebarTab(SidebarTab.GIT) },
            CommandItem("> Cloud: Sync Project to Cloud", "") { viewModel.triggerCloudSync() },
            CommandItem("> View: Toggle Primary Sidebar", "Ctrl+B") { viewModel.toggleSidebar() },
            CommandItem("> Preferences: Color Themes", "") { viewModel.selectSidebarTab(SidebarTab.EXTENSIONS) }
        )
    }

    val filtered = remember(query) {
        if (query.isBlank()) allCommands
        else allCommands.filter { it.title.contains(query, ignoreCase = true) }
    }

    Dialog(onDismissRequest = { viewModel.setCommandPaletteOpen(false) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252526)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Type a command or '>' to filter...", fontSize = 12.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("command_palette_input"),
                    leadingIcon = {
                        Icon(Icons.Default.Code, contentDescription = null, tint = state.currentTheme.accent, modifier = Modifier.size(18.dp))
                    },
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

                LazyColumn(modifier = Modifier.height(240.dp)) {
                    items(filtered) { cmd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCommandPaletteOpen(false)
                                    cmd.action()
                                }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cmd.title, color = Color.White, fontSize = 12.sp)
                            if (cmd.shortcut.isNotEmpty()) {
                                Text(
                                    cmd.shortcut,
                                    color = state.currentTheme.accent,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewFileDialog(
    state: CodeStudioUiState,
    viewModel: CodeStudioViewModel
) {
    if (!state.showNewFileDialog) return

    var fileName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { viewModel.setShowNewFileDialog(false) }) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252526)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Create New File", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    placeholder = { Text("e.g., utils.js, styles.css", fontSize = 12.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_file_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF3C3C3C),
                        unfocusedContainerColor = Color(0xFF3C3C3C),
                        focusedBorderColor = state.currentTheme.accent,
                        unfocusedBorderColor = Color(0xFF555555),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.setShowNewFileDialog(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C3C3C))
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fileName.isNotBlank()) {
                                viewModel.createNewFile(fileName.trim(), "/${fileName.trim()}")
                            }
                        },
                        enabled = fileName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = state.currentTheme.accent)
                    ) {
                        Text("Create", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NewProjectDialog(
    state: CodeStudioUiState,
    viewModel: CodeStudioViewModel
) {
    if (!state.showNewProjectDialog) return

    var projectName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("space_game") }

    val templates = listOf(
        Pair("space_game", "Space Shooter 2D Arcade Game"),
        Pair("flappy_game", "Flappy Pixel Bird Game"),
        Pair("todo_app", "Developer Todo & Notes App"),
        Pair("blank", "Empty Project (HTML/JS/CSS)")
    )

    Dialog(onDismissRequest = { viewModel.setShowNewProjectDialog(false) }) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252526)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("New Project from Template", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    placeholder = { Text("Project Name (e.g. My Game)", fontSize = 12.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_project_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF3C3C3C),
                        unfocusedContainerColor = Color(0xFF3C3C3C),
                        focusedBorderColor = state.currentTheme.accent,
                        unfocusedBorderColor = Color(0xFF555555),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Template:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                templates.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTemplate = key }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTemplate == key,
                            onClick = { selectedTemplate = key },
                            colors = RadioButtonDefaults.colors(selectedColor = state.currentTheme.accent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.setShowNewProjectDialog(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C3C3C))
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val name = if (projectName.isNotBlank()) projectName.trim() else "My Project"
                            viewModel.createNewProject(name, "Built with VS Code Mobile", selectedTemplate)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = state.currentTheme.accent)
                    ) {
                        Text("Create Project", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DiffViewerDialog(
    file: FileEntity?,
    dirtyContent: String?,
    onDismiss: () -> Unit
) {
    if (file == null || dirtyContent == null) return

    val oldLines = file.content.lines()
    val newLines = dirtyContent.lines()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Working Tree Diff: ${file.name}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                HorizontalDivider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(newLines) { line ->
                        val isAdded = !oldLines.contains(line)
                        val bg = if (isAdded) Color(0x3328A745) else Color.Transparent
                        val prefix = if (isAdded) "+ " else "  "
                        val textColor = if (isAdded) Color(0xFF48D368) else Color(0xFFCCCCCC)

                        Text(
                            text = prefix + line,
                            color = textColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth().background(bg).padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
