package com.example.ui.components

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.BottomPanelTab
import com.example.ui.CodeStudioUiState
import com.example.ui.CodeStudioViewModel

@Composable
fun BottomPanelView(
    state: CodeStudioUiState,
    viewModel: CodeStudioViewModel,
    modifier: Modifier = Modifier
) {
    if (state.activeBottomTab == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color(0xFF141414))
    ) {
        // Top tab header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF252526))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PanelTabItem(
                    title = "TERMINAL",
                    isSelected = state.activeBottomTab == BottomPanelTab.TERMINAL,
                    onClick = { viewModel.toggleBottomPanel(BottomPanelTab.TERMINAL) },
                    testTag = "panel_tab_terminal"
                )
                PanelTabItem(
                    title = "LIVE PREVIEW",
                    isSelected = state.activeBottomTab == BottomPanelTab.PREVIEW,
                    onClick = { viewModel.toggleBottomPanel(BottomPanelTab.PREVIEW) },
                    testTag = "panel_tab_preview"
                )
                PanelTabItem(
                    title = "CONSOLE (${state.consoleLogs.size})",
                    isSelected = state.activeBottomTab == BottomPanelTab.CONSOLE,
                    onClick = { viewModel.toggleBottomPanel(BottomPanelTab.CONSOLE) },
                    testTag = "panel_tab_console"
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Build button right in bottom panel
                IconButton(
                    onClick = { viewModel.buildAndPackageApp() },
                    modifier = Modifier.size(28.dp).testTag("panel_quick_build_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "Build App",
                        tint = Color(0xFF3DDC84),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleBottomPanel(state.activeBottomTab) },
                    modifier = Modifier.size(28.dp).testTag("panel_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close panel",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

        // Panel Body
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (state.activeBottomTab) {
                BottomPanelTab.TERMINAL -> TerminalContent(state, viewModel)
                BottomPanelTab.PREVIEW -> PreviewContent(state, viewModel)
                BottomPanelTab.CONSOLE -> ConsoleContent(state, viewModel)
            }
        }
    }
}

@Composable
private fun PanelTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun TerminalContent(state: CodeStudioUiState, viewModel: CodeStudioViewModel) {
    var cmdInput by remember { mutableStateOf("") }
    var historyIndex by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(state.terminalLines.size) {
        if (state.terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(state.terminalLines.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Terminal toolbar: Copy output, Clear terminal
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "bash 5.2 • ${state.currentProject?.name ?: "workspace"}",
                color = Color(0xFF007ACC),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Row {
                IconButton(
                    onClick = {
                        val text = state.terminalLines.joinToString("\n") { it.text }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Output", text))
                        Toast.makeText(context, "Terminal output copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy output",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.executeTerminalCommand("clear") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Terminal Output Screen
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0D0D0D), RoundedCornerShape(4.dp))
                .padding(6.dp)
        ) {
            items(state.terminalLines) { line ->
                val textColor = when {
                    line.isError -> Color(0xFFFF5252)
                    line.isPrompt -> Color(0xFF4EC9B0)
                    line.text.startsWith("✔") || line.text.contains("SUCCESSFUL") -> Color(0xFF3DDC84)
                    line.text.startsWith("[STAGE") || line.text.startsWith("[BUILD") -> Color(0xFF007ACC)
                    line.text.startsWith("╔") || line.text.startsWith("║") || line.text.startsWith("╚") -> Color(0xFFE5A93C)
                    else -> Color(0xFFD4D4D4)
                }

                Text(
                    text = line.text,
                    color = textColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Quick command chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chips = listOf(
                Pair("build", Color(0xFF3DDC84)),
                Pair("install", Color(0xFF3DDC84)),
                Pair("run", Color(0xFF007ACC)),
                Pair("ls", Color(0xFF90A4AE)),
                Pair("git status", Color(0xFFE5A93C)),
                Pair("git log", Color(0xFFE5A93C)),
                Pair("eval 12 * 8", Color(0xFF4EC9B0)),
                Pair("help", Color(0xFF90A4AE)),
                Pair("clear", Color(0xFF90A4AE))
            )

            chips.forEach { (chipText, chipColor) ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF252526), RoundedCornerShape(4.dp))
                        .clickable { viewModel.executeTerminalCommand(chipText) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = chipText,
                        color = chipColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Terminal Interactive Input Line with Command History Navigation (↑, ↓)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "vscode$ ",
                color = Color(0xFF4EC9B0),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            // History navigation buttons
            IconButton(
                onClick = {
                    if (state.commandHistory.isNotEmpty()) {
                        val newIdx = if (historyIndex == -1) state.commandHistory.size - 1 else maxOf(0, historyIndex - 1)
                        historyIndex = newIdx
                        cmdInput = state.commandHistory[newIdx]
                    }
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Prev command", tint = Color.Gray, modifier = Modifier.size(14.dp))
            }

            IconButton(
                onClick = {
                    if (state.commandHistory.isNotEmpty() && historyIndex != -1) {
                        val newIdx = historyIndex + 1
                        if (newIdx < state.commandHistory.size) {
                            historyIndex = newIdx
                            cmdInput = state.commandHistory[newIdx]
                        } else {
                            historyIndex = -1
                            cmdInput = ""
                        }
                    }
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Next command", tint = Color.Gray, modifier = Modifier.size(14.dp))
            }

            OutlinedTextField(
                value = cmdInput,
                onValueChange = { cmdInput = it },
                placeholder = { Text("e.g. build, install, run, ls, help", fontSize = 11.sp, color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("terminal_input_field"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (cmdInput.isNotBlank()) {
                        viewModel.executeTerminalCommand(cmdInput)
                        cmdInput = ""
                        historyIndex = -1
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedBorderColor = Color(0xFF007ACC),
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = {
                    if (cmdInput.isNotBlank()) {
                        viewModel.executeTerminalCommand(cmdInput)
                        cmdInput = ""
                        historyIndex = -1
                    }
                },
                modifier = Modifier.size(34.dp).testTag("terminal_send_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color(0xFF007ACC),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PreviewContent(state: CodeStudioUiState, viewModel: CodeStudioViewModel) {
    var isMobileViewport by remember { mutableStateOf(false) }

    val htmlFile = state.files.find { it.name.endsWith(".html") }
    val jsFiles = state.files.filter { it.name.endsWith(".js") }
    val cssFiles = state.files.filter { it.name.endsWith(".css") }

    val rawHtml = htmlFile?.let { state.dirtyFileBuffers[it.id] ?: it.content }
        ?: "<h3>No HTML file found in project</h3>"

    var combinedHtml = rawHtml
    cssFiles.forEach { css ->
        val cssContent = state.dirtyFileBuffers[css.id] ?: css.content
        combinedHtml = combinedHtml.replace(
            "<link rel=\"stylesheet\" href=\"${css.name}\">",
            "<style>\n$cssContent\n</style>"
        )
    }
    jsFiles.forEach { js ->
        val jsContent = state.dirtyFileBuffers[js.id] ?: js.content
        combinedHtml = combinedHtml.replace(
            "<script src=\"${js.name}\"></script>",
            "<script>\n$jsContent\n</script>"
        )
    }

    val consoleBridgeScript = """
<script>
  (function() {
    const origLog = console.log;
    const origWarn = console.warn;
    const origErr = console.error;
    console.log = function(...args) { origLog.apply(console, args); };
    console.warn = function(...args) { origWarn.apply(console, args); };
    console.error = function(...args) { origErr.apply(console, args); };
  })();
</script>
    """.trimIndent()
    combinedHtml = combinedHtml.replace("<head>", "<head>\n$consoleBridgeScript")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Live Runner", color = Color(0xFF4EC9B0), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMobileViewport) "Viewport: 360px" else "Viewport: Full Width",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            Row {
                IconButton(
                    onClick = { isMobileViewport = !isMobileViewport },
                    modifier = Modifier.size(28.dp).testTag("preview_viewport_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Toggle Viewport",
                        tint = if (isMobileViewport) Color(0xFF007ACC) else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.reloadPreview() },
                    modifier = Modifier.size(28.dp).testTag("preview_reload_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF111111)),
            contentAlignment = Alignment.Center
        ) {
            var webViewRef by remember { mutableStateOf<WebView?>(null) }
            var lastLoadedKey by remember { mutableStateOf(-1) }

            LaunchedEffect(state.previewReloadKey, webViewRef) {
                if (webViewRef != null && lastLoadedKey != state.previewReloadKey) {
                    lastLoadedKey = state.previewReloadKey
                    webViewRef?.loadDataWithBaseURL("https://vscode.app/", combinedHtml, "text/html", "UTF-8", null)
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    webViewRef?.stopLoading()
                    webViewRef?.destroy()
                    webViewRef = null
                }
            }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                                cm?.let {
                                    val level = when (it.messageLevel()) {
                                        ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                                        ConsoleMessage.MessageLevel.WARNING -> "WARN"
                                        else -> "LOG"
                                    }
                                    viewModel.addConsoleLog(level, it.message())
                                }
                                return super.onConsoleMessage(cm)
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                view?.stopLoading()
                                return true
                            }
                        }
                        loadDataWithBaseURL("https://vscode.app/", combinedHtml, "text/html", "UTF-8", null)
                        webViewRef = this
                    }
                },
                update = {
                    // Intentionally no-op to prevent continuous recomposition reload loops
                },
                modifier = if (isMobileViewport) Modifier.width(360.dp).fillMaxSize() else Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ConsoleContent(state: CodeStudioUiState, viewModel: CodeStudioViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.consoleLogs.size} logs recorded",
                color = Color.Gray,
                fontSize = 11.sp
            )
            IconButton(
                onClick = { viewModel.clearConsole() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Console",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
            items(state.consoleLogs) { log ->
                val badgeColor = when (log.level) {
                    "ERROR" -> Color(0xFFFF5252)
                    "WARN" -> Color(0xFFFFD700)
                    else -> Color(0xFF007ACC)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[${log.level}]",
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(52.dp)
                    )
                    Text(
                        text = log.message,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
