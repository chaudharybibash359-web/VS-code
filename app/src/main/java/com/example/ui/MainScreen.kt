package com.example.ui

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActivityBar
import com.example.ui.components.BottomPanelView
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.components.DiffViewerDialog
import com.example.ui.components.EditorArea
import com.example.ui.components.NewFileDialog
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.ProjectFileTreeDrawer
import com.example.ui.components.SidebarView
import com.example.ui.components.StatusBar
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: CodeStudioViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Handle back button when drawer/panel is open
    BackHandler(enabled = drawerState.isOpen || state.activeBottomTab != null || state.isSidebarOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (state.activeBottomTab != null) {
            viewModel.toggleBottomPanel(state.activeBottomTab!!)
        } else if (state.isSidebarOpen) {
            viewModel.toggleSidebar()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1E1E1E),
                drawerContentColor = Color.White
            ) {
                ProjectFileTreeDrawer(
                    state = state,
                    viewModel = viewModel,
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(state.currentTheme.background)
                .windowInsetsPadding(WindowInsets.statusBars)
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { event ->
                    // Global hardware keyboard shortcut interceptor
                    if (event.isCtrlPressed) {
                        val code = event.key.nativeKeyCode
                        when (code) {
                            KeyEvent.KEYCODE_S -> {
                                viewModel.saveActiveFile()
                                true
                            }
                            KeyEvent.KEYCODE_P -> {
                                viewModel.setCommandPaletteOpen(true)
                                true
                            }
                            KeyEvent.KEYCODE_B -> {
                                viewModel.toggleSidebar()
                                true
                            }
                            KeyEvent.KEYCODE_GRAVE -> {
                                viewModel.toggleBottomPanel(BottomPanelTab.TERMINAL)
                                true
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main workspace row: ActivityBar + Sidebar + Editor + Panel
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Leftmost Activity Bar
                    ActivityBar(
                        activeTab = state.activeSidebarTab,
                        isSidebarOpen = state.isSidebarOpen,
                        theme = state.currentTheme,
                        gitChangesCount = state.dirtyFileBuffers.size,
                        onTabSelected = { viewModel.selectSidebarTab(it) },
                        onRunClicked = { viewModel.openPreview() },
                        onCommandPaletteClicked = { viewModel.setCommandPaletteOpen(true) },
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )

                    // Collapsible Sidebar
                    if (state.isSidebarOpen) {
                        SidebarView(state = state, viewModel = viewModel)
                    }

                    // Main Editor Area & Bottom Docked Panel
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        // Editor & tabs
                        EditorArea(
                            state = state,
                            viewModel = viewModel,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.weight(1f)
                        )

                        // Integrated Panel (Terminal / Preview / Console)
                        BottomPanelView(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }

                // Status Bar at the bottom with navigation bar safe padding
                StatusBar(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }

        // Overlay Dialogs
        CommandPaletteDialog(state = state, viewModel = viewModel)
        NewFileDialog(state = state, viewModel = viewModel)
        NewProjectDialog(state = state, viewModel = viewModel)
        com.example.ui.components.BuildInstallDialog(state = state, viewModel = viewModel)

        state.diffComparingFile?.let { diffFile ->
            val dirty = state.dirtyFileBuffers[diffFile.id] ?: diffFile.content
            DiffViewerDialog(
                file = diffFile,
                dirtyContent = dirty,
                onDismiss = { viewModel.setDiffComparingFile(null) }
            )
        }
    }
}
