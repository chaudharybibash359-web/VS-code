package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

data class FileTreeNode(
    val file: FileEntity,
    val children: List<FileTreeNode> = emptyList(),
    val depth: Int = 0
)

fun buildFileTree(files: List<FileEntity>): List<FileTreeNode> {
    val itemsByParent = files.groupBy { it.parentPath }

    fun buildSubTree(parentPath: String, depth: Int): List<FileTreeNode> {
        val children = itemsByParent[parentPath] ?: emptyList()
        val (folders, nonFolders) = children.partition { it.isFolder }

        val sortedFolders = folders.sortedBy { it.name.lowercase() }
        val sortedFiles = nonFolders.sortedBy { it.name.lowercase() }

        val result = mutableListOf<FileTreeNode>()

        for (folder in sortedFolders) {
            val nested = buildSubTree(folder.path, depth + 1)
            result.add(FileTreeNode(folder, nested, depth))
        }

        for (file in sortedFiles) {
            result.add(FileTreeNode(file, emptyList(), depth))
        }

        return result
    }

    return buildSubTree("/", 0)
}

@Composable
fun HierarchicalFileTree(
    state: CodeStudioUiState,
    viewModel: CodeStudioViewModel,
    onFileSelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val treeNodes = remember(state.files) { buildFileTree(state.files) }

    // Start with all folders expanded
    var expandedPaths by remember(state.currentProject?.id) {
        mutableStateOf(state.files.filter { it.isFolder }.map { it.path }.toSet())
    }

    var targetFolderForNewItem by remember { mutableStateOf<String?>(null) }
    var showNewFileModal by remember { mutableStateOf(false) }
    var showNewFolderModal by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Toolbar with quick actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "FILES & FOLDERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = state.currentTheme.textPrimary.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )

            Row {
                IconButton(
                    onClick = {
                        targetFolderForNewItem = "/"
                        showNewFileModal = true
                    },
                    modifier = Modifier.size(26.dp).testTag("tree_add_file_root")
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "New File",
                        tint = state.currentTheme.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        targetFolderForNewItem = "/"
                        showNewFolderModal = true
                    },
                    modifier = Modifier.size(26.dp).testTag("tree_add_folder_root")
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "New Folder",
                        tint = state.currentTheme.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Tree nodes list
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
            items(treeNodes) { node ->
                FileTreeNodeItem(
                    node = node,
                    state = state,
                    expandedPaths = expandedPaths,
                    onToggleFolder = { path ->
                        expandedPaths = if (expandedPaths.contains(path)) {
                            expandedPaths - path
                        } else {
                            expandedPaths + path
                        }
                    },
                    onFileClick = { fileId ->
                        viewModel.openFile(fileId)
                        onFileSelected?.invoke(fileId)
                    },
                    onDeleteFile = { fileId -> viewModel.deleteFile(fileId) },
                    onDeleteFolder = { folderPath ->
                        state.currentProject?.let { proj ->
                            // Delete folder and its contents
                            viewModel.executeTerminalCommand("rm $folderPath")
                        }
                    },
                    onAddFileInFolder = { folderPath ->
                        targetFolderForNewItem = folderPath
                        showNewFileModal = true
                    },
                    onAddSubFolder = { folderPath ->
                        targetFolderForNewItem = folderPath
                        showNewFolderModal = true
                    }
                )
            }
        }
    }

    // Modal to create file in selected folder
    if (showNewFileModal) {
        val parent = targetFolderForNewItem ?: "/"
        CreateFileInFolderDialog(
            parentPath = parent,
            onDismiss = { showNewFileModal = false },
            onCreate = { name ->
                val fullPath = if (parent == "/") "/$name" else "$parent/$name"
                viewModel.createNewFile(name, fullPath)
                showNewFileModal = false
            }
        )
    }

    // Modal to create folder in selected folder
    if (showNewFolderModal) {
        val parent = targetFolderForNewItem ?: "/"
        CreateFolderDialog(
            parentPath = parent,
            onDismiss = { showNewFolderModal = false },
            onCreate = { folderName ->
                val fullPath = if (parent == "/") "/$folderName" else "$parent/$folderName"
                viewModel.executeTerminalCommand("mkdir $fullPath")
                expandedPaths = expandedPaths + fullPath
                showNewFolderModal = false
            }
        )
    }
}

@Composable
fun FileTreeNodeItem(
    node: FileTreeNode,
    state: CodeStudioUiState,
    expandedPaths: Set<String>,
    onToggleFolder: (String) -> Unit,
    onFileClick: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onAddFileInFolder: (String) -> Unit,
    onAddSubFolder: (String) -> Unit
) {
    val file = node.file
    val isExpanded = expandedPaths.contains(file.path)
    val isSelected = state.activeFileId == file.id
    val isDirty = state.dirtyFileBuffers.containsKey(file.id)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSelected && !file.isFolder) Color(0xFF37373D) else Color.Transparent)
                .clickable {
                    if (file.isFolder) {
                        onToggleFolder(file.path)
                    } else {
                        onFileClick(file.id)
                    }
                }
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .padding(start = (node.depth * 14).dp)
                .testTag("tree_item_${file.name}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (file.isFolder) {
                    // Chevron expand/collapse arrow
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    // Folder icon
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(0xFFE5A93C),
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                    // File icon based on extension
                    val extColor = when {
                        file.name.endsWith(".html") -> Color(0xFFE44D26)
                        file.name.endsWith(".js") -> Color(0xFFF7DF1E)
                        file.name.endsWith(".css") -> Color(0xFF264DE4)
                        file.name.endsWith(".json") -> Color(0xFF00BC8C)
                        file.name.endsWith(".py") -> Color(0xFF3776AB)
                        file.name.endsWith(".kt") -> Color(0xFF7F52FF)
                        file.name.endsWith(".md") -> Color(0xFF42A5F5)
                        else -> Color(0xFF90A4AE)
                    }

                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = extColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = file.name,
                    color = if (file.isFolder) Color.White else state.currentTheme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = if (file.isFolder) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isDirty) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "●",
                        color = state.currentTheme.accent,
                        fontSize = 10.sp
                    )
                }

                if (file.isFolder && node.children.isNotEmpty() && !isExpanded) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${node.children.size})",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            // Trailing actions for folder or file
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (file.isFolder) {
                    IconButton(
                        onClick = { onAddFileInFolder(file.path) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add File", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = { onAddSubFolder(file.path) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Add Subfolder", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }

                IconButton(
                    onClick = {
                        if (file.isFolder) onDeleteFolder(file.path) else onDeleteFile(file.id)
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(13.dp))
                }
            }
        }

        // Render children if folder is expanded
        if (file.isFolder && isExpanded) {
            node.children.forEach { childNode ->
                FileTreeNodeItem(
                    node = childNode,
                    state = state,
                    expandedPaths = expandedPaths,
                    onToggleFolder = onToggleFolder,
                    onFileClick = onFileClick,
                    onDeleteFile = onDeleteFile,
                    onDeleteFolder = onDeleteFolder,
                    onAddFileInFolder = onAddFileInFolder,
                    onAddSubFolder = onAddSubFolder
                )
            }
        }
    }
}

@Composable
fun CreateFileInFolderDialog(
    parentPath: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252526)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("New File in '$parentPath'", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                androidx.compose.material3.OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    placeholder = { Text("filename (e.g. app.js, style.css)", fontSize = 12.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_new_file_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    androidx.compose.material3.Button(
                        onClick = onDismiss,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF3C3C3C))
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        onClick = { if (fileName.isNotBlank()) onCreate(fileName.trim()) },
                        enabled = fileName.isNotBlank(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC))
                    ) {
                        Text("Create", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateFolderDialog(
    parentPath: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252526)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("New Folder in '$parentPath'", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                androidx.compose.material3.OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    placeholder = { Text("Folder name (e.g. components, utils)", fontSize = 12.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_new_folder_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    androidx.compose.material3.Button(
                        onClick = onDismiss,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF3C3C3C))
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        onClick = { if (folderName.isNotBlank()) onCreate(folderName.trim()) },
                        enabled = folderName.isNotBlank(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC))
                    ) {
                        Text("Create Folder", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
