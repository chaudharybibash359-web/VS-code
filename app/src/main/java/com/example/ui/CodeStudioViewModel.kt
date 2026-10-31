package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.build.AppBuilder
import com.example.data.build.BuildResult
import com.example.data.local.CloudBackupEntity
import com.example.data.local.CodeStudioDatabase
import com.example.data.local.FileEntity
import com.example.data.local.GitCommitEntity
import com.example.data.local.ProjectEntity
import com.example.data.repository.CodeStudioRepository
import com.example.ui.editor.EditorTheme
import com.example.ui.editor.ThemeEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SidebarTab {
    EXPLORER, SEARCH, GIT, EXTENSIONS, CLOUD
}

enum class BottomPanelTab {
    TERMINAL, PREVIEW, CONSOLE
}

data class ConsoleLogItem(
    val level: String, // LOG, WARN, ERROR
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TerminalLine(
    val text: String,
    val isPrompt: Boolean = false,
    val isError: Boolean = false
)

data class SearchResult(
    val file: FileEntity,
    val lineNumber: Int,
    val lineText: String
)

data class CodeStudioUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val currentProject: ProjectEntity? = null,
    val files: List<FileEntity> = emptyList(),
    val openTabIds: List<String> = emptyList(),
    val activeFileId: String? = null,
    val dirtyFileBuffers: Map<String, String> = emptyMap(),
    val isSidebarOpen: Boolean = true,
    val activeSidebarTab: SidebarTab = SidebarTab.EXPLORER,
    val activeBottomTab: BottomPanelTab? = null,
    val terminalLines: List<TerminalLine> = emptyList(),
    val consoleLogs: List<ConsoleLogItem> = emptyList(),
    val currentTheme: EditorTheme = ThemeEngine.DarkPlus,
    val isCommandPaletteOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val commits: List<GitCommitEntity> = emptyList(),
    val stagedFileIds: Set<String> = emptySet(),
    val installedExtensions: Set<String> = setOf("theme.darkplus", "tool.livereload", "lang.html5"),
    val cloudBackups: List<CloudBackupEntity> = emptyList(),
    val isCloudSyncing: Boolean = false,
    val statusMessage: String = "Ready",
    val showNewFileDialog: Boolean = false,
    val showNewProjectDialog: Boolean = false,
    val showCloudDialog: Boolean = false,
    val diffComparingFile: FileEntity? = null,
    val previewReloadKey: Int = 0,
    // Build & Install state
    val isBuilding: Boolean = false,
    val latestBuildResult: BuildResult? = null,
    val showBuildInstallDialog: Boolean = false,
    // Terminal history
    val commandHistory: List<String> = emptyList(),
    val historyPointer: Int = -1,
    // Editor Find & Replace
    val isFindReplaceOpen: Boolean = false,
    val findQuery: String = "",
    val replaceQuery: String = ""
)

class CodeStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CodeStudioRepository
    private val _uiState = MutableStateFlow(CodeStudioUiState())
    val uiState: StateFlow<CodeStudioUiState> = _uiState.asStateFlow()

    init {
        val db = CodeStudioDatabase.getDatabase(application)
        repository = CodeStudioRepository(db.codeStudioDao())

        // Welcome banner in terminal emulator
        _uiState.update {
            it.copy(
                terminalLines = listOf(
                    TerminalLine("╔═══════════════════════════════════════════════════════════╗"),
                    TerminalLine("║  VS Code Mobile Integrated Terminal & Compiler v1.2        ║"),
                    TerminalLine("║  Commands: 'run' (preview), 'build' (compile APK),         ║"),
                    TerminalLine("║            'install' (launch on phone), 'help'            ║"),
                    TerminalLine("╚═══════════════════════════════════════════════════════════╝")
                )
            )
        }

        viewModelScope.launch {
            repository.initializeDefaultProjectsIfEmpty()
        }

        viewModelScope.launch {
            repository.allProjects.collectLatest { list ->
                _uiState.update { state ->
                    val curr = state.currentProject?.let { p -> list.find { it.id == p.id } }
                        ?: list.firstOrNull()
                    state.copy(projects = list, currentProject = curr)
                }
                _uiState.value.currentProject?.let { loadProject(it) }
            }
        }

        viewModelScope.launch {
            repository.allBackups.collectLatest { backups ->
                _uiState.update { it.copy(cloudBackups = backups) }
            }
        }
    }

    fun loadProject(project: ProjectEntity) {
        _uiState.update { it.copy(currentProject = project) }
        viewModelScope.launch {
            repository.getFilesForProject(project.id).collectLatest { fileList ->
                _uiState.update { state ->
                    val newOpenTabs = if (state.openTabIds.isEmpty()) {
                        fileList.find { it.name == "index.html" || it.name == "game.js" }?.let { listOf(it.id) }
                            ?: fileList.firstOrNull()?.let { listOf(it.id) }
                            ?: emptyList()
                    } else {
                        state.openTabIds.filter { tabId -> fileList.any { it.id == tabId } }
                    }
                    val activeId = if (state.activeFileId == null || !fileList.any { it.id == state.activeFileId }) {
                        newOpenTabs.firstOrNull()
                    } else {
                        state.activeFileId
                    }

                    state.copy(
                        files = fileList,
                        openTabIds = newOpenTabs,
                        activeFileId = activeId
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getCommitsForProject(project.id).collectLatest { commitList ->
                _uiState.update { it.copy(commits = commitList) }
            }
        }
    }

    fun selectSidebarTab(tab: SidebarTab) {
        _uiState.update {
            if (it.activeSidebarTab == tab && it.isSidebarOpen) {
                it.copy(isSidebarOpen = false)
            } else {
                it.copy(activeSidebarTab = tab, isSidebarOpen = true)
            }
        }
    }

    fun toggleSidebar() {
        _uiState.update { it.copy(isSidebarOpen = !it.isSidebarOpen) }
    }

    fun openFile(fileId: String) {
        _uiState.update { state ->
            val tabs = if (!state.openTabIds.contains(fileId)) {
                state.openTabIds + fileId
            } else {
                state.openTabIds
            }
            state.copy(openTabIds = tabs, activeFileId = fileId)
        }
    }

    fun closeTab(fileId: String) {
        _uiState.update { state ->
            val tabs = state.openTabIds - fileId
            val nextActive = if (state.activeFileId == fileId) {
                tabs.lastOrNull()
            } else {
                state.activeFileId
            }
            state.copy(openTabIds = tabs, activeFileId = nextActive)
        }
    }

    fun updateActiveFileContent(newText: String) {
        val activeId = _uiState.value.activeFileId ?: return
        _uiState.update { state ->
            val dirty = state.dirtyFileBuffers.toMutableMap()
            dirty[activeId] = newText
            state.copy(dirtyFileBuffers = dirty)
        }
    }

    fun saveActiveFile() {
        val activeId = _uiState.value.activeFileId ?: return
        val content = _uiState.value.dirtyFileBuffers[activeId] ?: return
        viewModelScope.launch {
            repository.saveFileContent(activeId, content)
            _uiState.update { state ->
                val dirty = state.dirtyFileBuffers.toMutableMap()
                dirty.remove(activeId)
                state.copy(dirtyFileBuffers = dirty, statusMessage = "Saved")
            }
        }
    }

    fun createNewFile(name: String, path: String) {
        val projId = _uiState.value.currentProject?.id ?: return
        viewModelScope.launch {
            val file = repository.createFile(projId, name, path, "")
            openFile(file.id)
            _uiState.update { it.copy(showNewFileDialog = false, statusMessage = "Created $name") }
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
            closeTab(fileId)
            _uiState.update { it.copy(statusMessage = "File deleted") }
        }
    }

    fun createNewProject(name: String, description: String, templateType: String) {
        viewModelScope.launch {
            val p = repository.createNewProject(name, description, templateType)
            _uiState.update { it.copy(showNewProjectDialog = false, currentProject = p) }
            loadProject(p)
        }
    }

    fun toggleBottomPanel(tab: BottomPanelTab) {
        _uiState.update {
            val next = if (it.activeBottomTab == tab) null else tab
            it.copy(activeBottomTab = next)
        }
    }

    fun openPreview() {
        _uiState.update {
            it.copy(
                activeBottomTab = BottomPanelTab.PREVIEW,
                previewReloadKey = it.previewReloadKey + 1
            )
        }
    }

    fun reloadPreview() {
        _uiState.update { it.copy(previewReloadKey = it.previewReloadKey + 1) }
    }

    fun addConsoleLog(level: String, message: String) {
        _uiState.update {
            val newLogs = (it.consoleLogs + ConsoleLogItem(level, message)).takeLast(200)
            it.copy(consoleLogs = newLogs)
        }
    }

    fun clearConsole() {
        _uiState.update { it.copy(consoleLogs = emptyList()) }
    }

    fun executeTerminalCommand(input: String) {
        val proj = _uiState.value.currentProject ?: return
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        // Save command into history
        _uiState.update {
            it.copy(
                terminalLines = it.terminalLines + TerminalLine("vscode@android:~/${proj.name}$ $trimmed", isPrompt = true),
                commandHistory = it.commandHistory + trimmed,
                historyPointer = -1
            )
        }

        if (trimmed.lowercase() == "clear") {
            _uiState.update { it.copy(terminalLines = emptyList()) }
            return
        }

        viewModelScope.launch {
            val result = repository.executeTerminalCommand(proj.id, "/", trimmed)
            when (result.output) {
                "TRIGGER_RUN" -> {
                    openPreview()
                    _uiState.update {
                        it.copy(
                            terminalLines = it.terminalLines + TerminalLine("✔ Live preview runner launched successfully.")
                        )
                    }
                }
                "TRIGGER_BUILD" -> {
                    buildAndPackageApp()
                }
                "TRIGGER_INSTALL" -> {
                    val buildRes = _uiState.value.latestBuildResult
                    if (buildRes != null && buildRes.isSuccess) {
                        launchInstalledApp()
                    } else {
                        buildAndPackageApp(autoLaunch = true)
                    }
                }
                else -> {
                    if (result.output.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                terminalLines = it.terminalLines + TerminalLine(result.output, isError = result.isError)
                            )
                        }
                    }
                }
            }
        }
    }

    // Build, Package, and Install project onto mobile device
    fun buildAndPackageApp(autoLaunch: Boolean = false) {
        val proj = _uiState.value.currentProject ?: return
        val currentFiles = _uiState.value.files.map { f ->
            val dirty = _uiState.value.dirtyFileBuffers[f.id]
            if (dirty != null) f.copy(content = dirty) else f
        }

        _uiState.update {
            it.copy(
                isBuilding = true,
                terminalLines = it.terminalLines + TerminalLine("[BUILD] Compiling project '${proj.name}' into standalone Mobile App...")
            )
        }

        viewModelScope.launch {
            val buildResult = AppBuilder.buildProject(getApplication(), proj, currentFiles)

            val newTerminalLines = mutableListOf<TerminalLine>()
            buildResult.logs.forEach { log ->
                newTerminalLines.add(TerminalLine(log, isError = log.startsWith("[ERROR]")))
            }

            _uiState.update {
                it.copy(
                    isBuilding = false,
                    latestBuildResult = buildResult,
                    showBuildInstallDialog = !autoLaunch,
                    terminalLines = it.terminalLines + newTerminalLines,
                    statusMessage = if (buildResult.isSuccess) "Build Success" else "Build Failed"
                )
            }

            if (autoLaunch && buildResult.isSuccess) {
                launchInstalledApp()
            }
        }
    }

    fun launchInstalledApp() {
        val res = _uiState.value.latestBuildResult ?: return
        AppBuilder.launchInstalledApp(getApplication(), res.appName, res.bundleHtml)
    }

    fun installOrShareApk() {
        val res = _uiState.value.latestBuildResult ?: return
        val apk = res.apkFile ?: return
        AppBuilder.installOrShareApk(getApplication(), apk)
    }

    fun setShowBuildInstallDialog(show: Boolean) {
        _uiState.update { it.copy(showBuildInstallDialog = show) }
    }

    // In-editor Find & Replace
    fun setFindReplaceOpen(open: Boolean) {
        _uiState.update { it.copy(isFindReplaceOpen = open) }
    }

    fun updateFindQuery(query: String) {
        _uiState.update { it.copy(findQuery = query) }
    }

    fun updateReplaceQuery(query: String) {
        _uiState.update { it.copy(replaceQuery = query) }
    }

    fun replaceAll() {
        val activeId = _uiState.value.activeFileId ?: return
        val findQ = _uiState.value.findQuery
        val replaceQ = _uiState.value.replaceQuery
        if (findQ.isEmpty()) return

        val file = _uiState.value.files.find { it.id == activeId } ?: return
        val currentContent = _uiState.value.dirtyFileBuffers[activeId] ?: file.content
        val newContent = currentContent.replace(findQ, replaceQ)
        updateActiveFileContent(newContent)
    }

    fun formatActiveDocument() {
        val activeId = _uiState.value.activeFileId ?: return
        val file = _uiState.value.files.find { it.id == activeId } ?: return
        val currentContent = _uiState.value.dirtyFileBuffers[activeId] ?: file.content

        // Basic clean indentation formatter
        val lines = currentContent.lines()
        var indentLevel = 0
        val formatted = lines.joinToString("\n") { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("}") || trimmed.startsWith("]") || trimmed.startsWith("</")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            val indented = "  ".repeat(indentLevel) + trimmed
            if (trimmed.endsWith("{") || trimmed.endsWith("[") || (trimmed.startsWith("<") && !trimmed.contains("</") && !trimmed.endsWith("/>") && !trimmed.startsWith("<!"))) {
                indentLevel++
            }
            indented
        }
        updateActiveFileContent(formatted)
        _uiState.update { it.copy(statusMessage = "Document Formatted") }
    }

    fun toggleStageFile(fileId: String) {
        _uiState.update { state ->
            val set = state.stagedFileIds.toMutableSet()
            if (set.contains(fileId)) set.remove(fileId) else set.add(fileId)
            state.copy(stagedFileIds = set)
        }
    }

    fun stageAllFiles() {
        _uiState.update { state ->
            val modifiedIds = state.dirtyFileBuffers.keys
            state.copy(stagedFileIds = state.stagedFileIds + modifiedIds)
        }
    }

    fun commitStaged(message: String) {
        val proj = _uiState.value.currentProject ?: return
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.createCommit(proj.id, message.trim(), proj.activeBranch)
            _uiState.update {
                it.copy(
                    stagedFileIds = emptySet(),
                    statusMessage = "Committed: $message"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            if (query.isBlank()) {
                state.copy(searchQuery = query, searchResults = emptyList())
            } else {
                val results = mutableListOf<SearchResult>()
                state.files.forEach { file ->
                    val content = state.dirtyFileBuffers[file.id] ?: file.content
                    content.lines().forEachIndexed { idx, line ->
                        if (line.contains(query, ignoreCase = true)) {
                            results.add(SearchResult(file, idx + 1, line.trim()))
                        }
                    }
                }
                state.copy(searchQuery = query, searchResults = results)
            }
        }
    }

    fun setTheme(theme: EditorTheme) {
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun toggleExtension(extId: String) {
        _uiState.update { state ->
            val exts = state.installedExtensions.toMutableSet()
            if (exts.contains(extId)) exts.remove(extId) else exts.add(extId)
            state.copy(installedExtensions = exts)
        }
    }

    fun setCommandPaletteOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isCommandPaletteOpen = isOpen) }
    }

    fun setShowNewFileDialog(show: Boolean) {
        _uiState.update { it.copy(showNewFileDialog = show) }
    }

    fun setShowNewProjectDialog(show: Boolean) {
        _uiState.update { it.copy(showNewProjectDialog = show) }
    }

    fun setShowCloudDialog(show: Boolean) {
        _uiState.update { it.copy(showCloudDialog = show) }
    }

    fun setDiffComparingFile(file: FileEntity?) {
        _uiState.update { it.copy(diffComparingFile = file) }
    }

    fun triggerCloudSync() {
        val proj = _uiState.value.currentProject ?: return
        _uiState.update { it.copy(isCloudSyncing = true, statusMessage = "Syncing with cloud...") }
        viewModelScope.launch {
            try {
                repository.createCloudSnapshot(proj.id)
                _uiState.update {
                    it.copy(
                        isCloudSyncing = false,
                        statusMessage = "Cloud backup synced successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCloudSyncing = false,
                        statusMessage = "Sync failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun restoreBackup(backupId: String) {
        viewModelScope.launch {
            val newProjId = repository.restoreCloudSnapshot(backupId)
            _uiState.update {
                it.copy(
                    showCloudDialog = false,
                    statusMessage = "Backup restored successfully"
                )
            }
        }
    }
}
