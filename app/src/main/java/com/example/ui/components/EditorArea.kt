package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CodeStudioUiState
import com.example.ui.CodeStudioViewModel
import com.example.ui.editor.SyntaxHighlighter

@Composable
fun EditorArea(
    state: CodeStudioUiState,
    viewModel: CodeStudioViewModel,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeFile = state.files.find { it.id == state.activeFileId }
    val currentContent = activeFile?.let { state.dirtyFileBuffers[it.id] ?: it.content } ?: ""
    val isDirty = activeFile?.let { state.dirtyFileBuffers.containsKey(it.id) } ?: false
    val extension = activeFile?.name?.substringAfterLast('.', "") ?: "js"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(state.currentTheme.background)
    ) {
        // Multi-Tab Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(state.currentTheme.tabInactiveBg)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.openTabIds.forEach { tabId ->
                val file = state.files.find { it.id == tabId }
                if (file != null) {
                    val isActive = state.activeFileId == tabId
                    val isTabDirty = state.dirtyFileBuffers.containsKey(tabId)

                    val tabExtColor = when {
                        file.name.endsWith(".html") -> Color(0xFFE44D26)
                        file.name.endsWith(".js") -> Color(0xFFF7DF1E)
                        file.name.endsWith(".css") -> Color(0xFF264DE4)
                        file.name.endsWith(".json") -> Color(0xFF00BC8C)
                        file.name.endsWith(".py") -> Color(0xFF3776AB)
                        file.name.endsWith(".kt") -> Color(0xFF7F52FF)
                        else -> Color(0xFF90A4AE)
                    }

                    Row(
                        modifier = Modifier
                            .background(if (isActive) state.currentTheme.tabActiveBg else state.currentTheme.tabInactiveBg)
                            .clickable { viewModel.openFile(file.id) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("editor_tab_${file.name}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Source,
                            contentDescription = null,
                            tint = tabExtColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = file.name,
                            color = if (isActive) Color.White else state.currentTheme.textPrimary.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (isTabDirty) {
                            Text(
                                text = "●",
                                color = state.currentTheme.accent,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close tab",
                            tint = if (isActive) Color.White else Color.Gray,
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { viewModel.closeTab(file.id) }
                        )
                    }
                }
            }

            // "+" Button to create a new file directly from tab bar
            IconButton(
                onClick = { viewModel.setShowNewFileDialog(true) },
                modifier = Modifier
                    .size(30.dp)
                    .padding(horizontal = 4.dp)
                    .testTag("editor_tab_new_file_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New File",
                    tint = state.currentTheme.textPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Breadcrumb & Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(state.currentTheme.background)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onOpenDrawer != null) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(24.dp).testTag("editor_open_file_drawer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open File Tree Drawer",
                            tint = state.currentTheme.textPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (activeFile != null) "${state.currentProject?.name ?: ""} > ${activeFile.name}" else "No file open",
                    color = state.currentTheme.textPrimary.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                // In-editor Find & Replace toggle
                IconButton(
                    onClick = { viewModel.setFindReplaceOpen(!state.isFindReplaceOpen) },
                    modifier = Modifier.size(28.dp).testTag("editor_find_replace_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.FindReplace,
                        contentDescription = "Find & Replace",
                        tint = if (state.isFindReplaceOpen) state.currentTheme.accent else state.currentTheme.textPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Format document button
                IconButton(
                    onClick = { viewModel.formatActiveDocument() },
                    modifier = Modifier.size(28.dp).testTag("editor_format_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Format Document",
                        tint = state.currentTheme.textPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Save button
                if (isDirty) {
                    IconButton(
                        onClick = { viewModel.saveActiveFile() },
                        modifier = Modifier.size(28.dp).testTag("editor_save_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save file",
                            tint = state.currentTheme.accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Live Preview run button
                IconButton(
                    onClick = { viewModel.openPreview() },
                    modifier = Modifier.size(28.dp).testTag("editor_run_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Live Preview",
                        tint = Color(0xFF4EC9B0),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Build & Install App on Mobile Button (Android Robot)
                Button(
                    onClick = { viewModel.buildAndPackageApp() },
                    modifier = Modifier.height(26.dp).testTag("editor_build_install_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Build & Install", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // In-Editor Find & Replace Banner
        if (state.isFindReplaceOpen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252526))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.findQuery,
                    onValueChange = { viewModel.updateFindQuery(it) },
                    placeholder = { Text("Find...", fontSize = 11.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(36.dp).testTag("editor_find_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedBorderColor = state.currentTheme.accent,
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = state.replaceQuery,
                    onValueChange = { viewModel.updateReplaceQuery(it) },
                    placeholder = { Text("Replace...", fontSize = 11.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(36.dp).testTag("editor_replace_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedBorderColor = state.currentTheme.accent,
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Button(
                    onClick = { viewModel.replaceAll() },
                    modifier = Modifier.height(34.dp).testTag("editor_replace_all_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = state.currentTheme.accent),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Replace All", fontSize = 10.sp, color = Color.White)
                }

                IconButton(
                    onClick = { viewModel.setFindReplaceOpen(false) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Find", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }

        HorizontalDivider(color = Color(0xFF282828), thickness = 1.dp)

        // Main Code Editor Area (Gutter + Text Field)
        if (activeFile != null) {
            val lines = remember(currentContent) { currentContent.lines() }
            val lineCount = remember(lines) { maxOf(1, lines.size) }
            val verticalScroll = rememberScrollState()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                ) {
                    // Line number gutter
                    Column(
                        modifier = Modifier
                            .background(state.currentTheme.gutterBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount) {
                            Text(
                                text = i.toString(),
                                color = state.currentTheme.lineNumberColor,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Multi-language Syntax Highlighting VisualTransformation
                    val syntaxTransformation = remember(extension) {
                        VisualTransformation { text ->
                            val highlighted = SyntaxHighlighter.highlight(text.text, extension)
                            TransformedText(highlighted, OffsetMapping.Identity)
                        }
                    }

                    BasicTextField(
                        value = currentContent,
                        onValueChange = { viewModel.updateActiveFileContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("code_editor_text_field"),
                        textStyle = TextStyle(
                            color = state.currentTheme.textPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(state.currentTheme.accent),
                        visualTransformation = syntaxTransformation
                    )
                }
            }

            // Mobile Quick-Code Symbol Helper Row (Above soft keyboard)
            MobileCodeSymbolRow(
                onInsertSymbol = { symbol ->
                    val newContent = currentContent + symbol
                    viewModel.updateActiveFileContent(newContent)
                }
            )
        } else {
            // Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VS Code Mobile",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select a file from Explorer to edit or create a new file",
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileCodeSymbolRow(onInsertSymbol: (String) -> Unit) {
    val symbols = listOf(
        "{", "}", "(", ")", "[", "]", ";", "=>", "=", "\"", "'", "/", "<", ">", ":", ".", "tab", "const", "let", "function", "return", "console.log"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252526))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        symbols.forEach { sym ->
            Box(
                modifier = Modifier
                    .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                    .clickable {
                        when (sym) {
                            "tab" -> onInsertSymbol("  ")
                            "console.log" -> onInsertSymbol("console.log();")
                            else -> onInsertSymbol(sym)
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sym,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
