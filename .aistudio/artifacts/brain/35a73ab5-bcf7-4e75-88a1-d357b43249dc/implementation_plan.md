# VS Code Mobile (CodeStudio IDE)

A full-featured, offline-first VS Code environment for Android smartphones and tablets, engineered with the authentic VS Code Dark+ visual hierarchy, multi-tab syntax editor, interactive terminal, live HTML5 canvas game & app preview runner, Git source control, extensions marketplace, and cloud sync.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during Phase 1 clarification:
> - **Primary Runtime & Preview**: Web and HTML5 game runner with live preview and interactive console.
> - **Cloud Backup & Synchronization**: Built-in cloud sync with automatic snapshots, backup, and one-tap restore.
> - **Visual Identity**: Classic VS Code Dark+ styling with collapsible activity bar, desktop-to-mobile adaptive layout, and touch-optimized symbol toolbar.

- **Confirmed Decision 1**: Prioritize an integrated Web/Canvas/JS game engine runner with live reload, console inspection, and touch joystick controls for previewing game projects immediately.
- **Confirmed Decision 2**: Implement an offline-first storage engine (Room database + internal file repository) coupled with built-in cloud synchronization snapshots.
- **Confirmed Decision 3**: Provide full VS Code activity bar icons (Explorer, Search, Git Source Control, Extensions, Run & Debug, Cloud Sync, Settings) with quick-action Command Palette (`Ctrl+Shift+P`) and hardware keyboard shortcuts (`Ctrl+S`, `Ctrl+P`, `Ctrl+\``).

---

## 1. Overview & Core Concept

- **What It Does**: Enables developers to write code, manage full projects, inspect Git diffs, install extensions, execute terminal commands, and build and play HTML5 games and web apps with a live interactive preview directly on their Android phone or tablet.
- **Target Audience / Persona**: Mobile developers, students, web developers, indie game creators, and programmers who want to code on the go without carrying a bulky laptop.
- **Key Value**: A high-fidelity VS Code experience on Android with genuine offline file editing, rich syntax highlighting, code snippet insertion toolbar for fast mobile typing, Git version control, and real-time execution of web apps and canvas games.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Launch & Project Selection**: Opens to the active project or lets the user choose between bundled starter templates (Space Shooter Canvas Game, Flappy Bird clone, Reactive Todo App) or creating an empty custom project.
2. **Editing & Code Navigation**:
   - Top Bar: Project title, open editor tabs, split preview toggle, run button, and quick command palette trigger.
   - Activity Bar (Left): Quick-toggle for Explorer, Search, Git, Extensions, and Sync.
   - Editor Area: Line numbers, bracket matching, syntax-colored code, cursor position in status bar, and a mobile helper row with punctuation symbols (`{`, `}`, `(`, `)`, `;`, `=>`, `=`, `"`, `tab`, `//`).
3. **Running & Live Preview**: Tapping "Run" opens an embedded high-performance WebView runner with live interactive canvas/DOM rendering, virtual controls for mobile games, and an expandable Console log drawer.
4. **Terminal Execution**: Swiping or toggling the bottom panel reveals an interactive terminal prompt supporting common developer commands (`ls`, `cat`, `git status`, `git commit`, `run`, `echo`, `mkdir`, `help`).
5. **Git Source Control**: Stage files, view side-by-side additions and deletions diffs, enter commit messages, and review commit logs.
6. **Extensions & Customization**: Browse and activate community themes (Monokai, One Dark Pro, GitHub Dark, SynthWave) and utility extensions.
7. **Cloud Backup & Restore**: Sync current projects to secure cloud storage and restore them at any time.

### Visual Identity & Theme
- **Theme**: Authentic VS Code Dark+ (`#1E1E1E` editor background, `#252526` sidebar, `#333333` activity bar, `#007ACC` accent blue, `#007ACC` status bar in debug/normal modes).
- **Typography**: Clean monospace code font (`JetBrains Mono` / `Roboto Mono` with fallback) for editor and terminal; crisp Inter/Roboto for UI labels.
- **Adaptive Ergonomics**:
  - **Phone Mode**: Activity bar collapses into a sleek bottom/drawer navigation or expandable rail; editor takes full width; floating quick symbols row above software keyboard.
  - **Tablet / Large Screen Mode**: Persistent activity bar on the left, side-by-side file tree and editor, and live split preview.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Offline-First Hybrid Storage (Room + Device Files)**
  - *Chosen Approach*: Store project files, Git commit history, and settings in a local Room SQLite database paired with internal storage files.
  - *Why*: Guarantees instant zero-latency loading, offline editing in flights/subways, and seamless project export/import.
  - *Alternatives Considered*: Pure cloud-only storage, which fails when offline.
- **Decision 2: Sandboxed Interactive Web & Game Preview with Console Hook**
  - *Chosen Approach*: Jetpack Compose `AndroidView` wrapping an optimized `WebView` with injected JS console capture (`console.log`, `console.error`) and responsive viewport simulation.
  - *Why*: Allows instant running of HTML5, Canvas, CSS, and JavaScript games with zero external build server required.
- **Decision 3: VS Code Command Palette & Shortcut Dispatcher**
  - *Chosen Approach*: A centralized shortcut dispatcher handling hardware key events (`KeyEvent.KEYCODE_S` + `isCtrlPressed`, `KEYCODE_P`, etc.) plus an on-screen `Ctrl+Shift+P` command palette with search filtering.
  - *Why*: Power users with Bluetooth keyboards get a 1:1 desktop experience, while touch users have an accessible UI overlay.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   VS Code Mobile App                   │
├───────────────┬────────────────────────┬───────────────┤
│ Activity Bar  │  Primary Editor View   │ Panel / Run   │
│ - Explorer    │  - Tab Header Bar      │ - Web Preview │
│ - Search      │  - Syntax Highlighting │ - Terminal    │
│ - Git VCS     │  - Line Numbers Gutter │ - Console Log │
│ - Extensions  │  - Mobile Symbol Row   │ - Problems    │
│ - Cloud Sync  │  - Command Palette     │               │
└───────┬───────┴───────────┬────────────┴───────┬───────┘
        │                   │                    │
        ▼                   ▼                    ▼
┌────────────────────────────────────────────────────────┐
│               CodeStudio Core ViewModel                │
│ - Active File & Tab State                              │
│ - Project & File Operations                            │
│ - Git Staging & Commit Engine                          │
│ - Terminal Emulator & Shell Command Parser             │
│ - Cloud Sync & Snapshot Orchestrator                   │
└───────────────────────────┬────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
┌───────────────────────────────┐ ┌──────────────────────┐
│     Room Local Persistence    │ │  Cloud Sync Engine   │
│ - ProjectEntity               │ │ - Project Snapshots  │
│ - FileEntity                  │ │ - Auto-save backups  │
│ - GitCommitEntity             │ │ - Backup metadata    │
│ - ExtensionEntity             │ └──────────────────────┘
└───────────────────────────────┘
```

### Components & State Models
- **`ProjectEntity` & `FileEntity`**: Relational model supporting directory structures, file extensions, MIME types, and timestamps.
- **`EditorState`**: Open tabs, active file ID, undo/redo stacks, cursor position, selection range, and dirty/saved flags.
- **`GitState`**: Current branch, changed files, staged files, commit history, and unified diff generator.
- **`TerminalState`**: Command history, stdout/stderr stream, active working directory, and environment variables.
- **`ExtensionState`**: Installed theme bundles, custom snippets, and active configuration.
- **`PreviewState`**: Live reload switch, console message logs, viewport size toggle (Mobile 375px / Tablet 768px / Fullscreen), and virtual game touch controls.

---

## 5. Implementation Stages & Verification Plan

1. **Database & Core Architecture**: Room database tables, DAOs, and repository layer for projects, files, and git commits.
2. **Editor & Themes Engine**: Multi-tab code editor with syntax tokenization, line numbers, customizable VS Code themes, and touch punctuation helper.
3. **Interactive Terminal**: Virtual shell parser with commands (`ls`, `cat`, `touch`, `rm`, `git`, `run`, `echo`, `clear`).
4. **HTML5 App & Game Runner**: Embedded preview runner with JS console redirection, device frame preview, and touch controls.
5. **Git Source Control & Extensions**: Branch management, diff viewer, commit creator, and theme/extensions store.
6. **Command Palette & Keyboard Shortcuts**: Hardware key interception (`Ctrl+S`, `Ctrl+P`, `Ctrl+\``) and searchable modal command palette.
7. **Cloud Sync & Verification**: Automatic snapshot generator, sync controls, project ZIP export, and `compile_applet` build validation.
