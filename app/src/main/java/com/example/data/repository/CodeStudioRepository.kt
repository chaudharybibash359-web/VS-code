package com.example.data.repository

import com.example.data.local.CloudBackupEntity
import com.example.data.local.CodeStudioDao
import com.example.data.local.FileEntity
import com.example.data.local.GitCommitEntity
import com.example.data.local.ProjectEntity
import com.example.data.templates.ProjectTemplates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class CodeStudioRepository(private val dao: CodeStudioDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val allBackups: Flow<List<CloudBackupEntity>> = dao.getAllBackups()

    fun getFilesForProject(projectId: String): Flow<List<FileEntity>> {
        return dao.getFilesForProject(projectId)
    }

    fun getCommitsForProject(projectId: String): Flow<List<GitCommitEntity>> {
        return dao.getCommitsForProject(projectId)
    }

    suspend fun initializeDefaultProjectsIfEmpty() {
        val projects = dao.getAllProjects().first()
        if (projects.isEmpty()) {
            val (spaceProject, spaceFiles) = ProjectTemplates.createSpaceShooterProject()
            dao.insertProject(spaceProject)
            dao.insertFiles(spaceFiles)
            dao.insertCommit(
                GitCommitEntity(
                    id = UUID.randomUUID().toString(),
                    projectId = spaceProject.id,
                    commitHash = "a1b2c3d",
                    message = "Initial commit: Space Shooter 2D game engine",
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )

            val (flappyProject, flappyFiles) = ProjectTemplates.createFlappyBirdProject()
            dao.insertProject(flappyProject)
            dao.insertFiles(flappyFiles)

            val (todoProject, todoFiles) = ProjectTemplates.createTodoAppProject()
            dao.insertProject(todoProject)
            dao.insertFiles(todoFiles)
        }
    }

    suspend fun createNewProject(name: String, description: String, templateType: String): ProjectEntity {
        val (proj, files) = when (templateType) {
            "flappy_game" -> ProjectTemplates.createFlappyBirdProject().copyFirst(name, description)
            "todo_app" -> ProjectTemplates.createTodoAppProject().copyFirst(name, description)
            "blank" -> {
                val pid = UUID.randomUUID().toString()
                val p = ProjectEntity(pid, name, description, "blank")
                val f = listOf(
                    FileEntity(UUID.randomUUID().toString(), pid, "index.html", "/index.html", "<h1>$name</h1>\n<p>Start coding!</p>"),
                    FileEntity(UUID.randomUUID().toString(), pid, "main.js", "/main.js", "// JavaScript entry point\nconsole.log('App ready');")
                )
                Pair(p, f)
            }
            else -> ProjectTemplates.createSpaceShooterProject().copyFirst(name, description)
        }
        dao.insertProject(proj)
        dao.insertFiles(files)
        dao.insertCommit(
            GitCommitEntity(
                id = UUID.randomUUID().toString(),
                projectId = proj.id,
                commitHash = "init001",
                message = "Initial commit for $name"
            )
        )
        return proj
    }

    suspend fun saveFileContent(fileId: String, newContent: String) {
        val file = dao.getFileById(fileId) ?: return
        dao.updateFile(file.copy(content = newContent, updatedAt = System.currentTimeMillis()))
    }

    suspend fun createFile(
        projectId: String,
        name: String,
        path: String,
        content: String = "",
        parentPath: String = "/"
    ): FileEntity {
        val newFile = FileEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            name = name,
            path = path,
            content = content,
            isFolder = false,
            parentPath = parentPath,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertFile(newFile)
        return newFile
    }

    suspend fun createFolder(
        projectId: String,
        name: String,
        path: String,
        parentPath: String = "/"
    ): FileEntity {
        val newFolder = FileEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            name = name,
            path = path,
            content = "",
            isFolder = true,
            parentPath = parentPath,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertFile(newFolder)
        return newFolder
    }

    suspend fun deleteFile(fileId: String) {
        dao.deleteFileById(fileId)
    }

    suspend fun deleteFolder(projectId: String, folderPath: String) {
        dao.deleteFolderAndChildren(projectId, folderPath, "$folderPath/%")
    }

    suspend fun createCommit(projectId: String, message: String, branch: String): GitCommitEntity {
        val hash = UUID.randomUUID().toString().substring(0, 7)
        val commit = GitCommitEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            commitHash = hash,
            message = message,
            branchName = branch
        )
        dao.insertCommit(commit)
        return commit
    }

    suspend fun createCloudSnapshot(projectId: String): CloudBackupEntity {
        val project = dao.getProjectById(projectId) ?: throw IllegalStateException("Project not found")
        val files = dao.getFilesForProjectOnce(projectId)

        val json = JSONObject()
        json.put("projectId", project.id)
        json.put("projectName", project.name)
        json.put("templateType", project.templateType)

        val filesArray = JSONArray()
        for (f in files) {
            val fObj = JSONObject()
            fObj.put("name", f.name)
            fObj.put("path", f.path)
            fObj.put("content", f.content)
            fObj.put("isFolder", f.isFolder)
            filesArray.put(fObj)
        }
        json.put("files", filesArray)

        val backup = CloudBackupEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            projectName = project.name,
            backupTimestamp = System.currentTimeMillis(),
            filesCount = files.size,
            snapshotJson = json.toString()
        )
        dao.insertBackup(backup)
        return backup
    }

    suspend fun restoreCloudSnapshot(backupId: String): String? {
        val backups = dao.getAllBackups().first()
        val backup = backups.find { it.id == backupId } ?: return null

        val json = JSONObject(backup.snapshotJson)
        val newProjectId = UUID.randomUUID().toString()
        val projectName = json.optString("projectName", "Restored Project")
        val templateType = json.optString("templateType", "restored")

        val newProject = ProjectEntity(
            id = newProjectId,
            name = "$projectName (Restored)",
            description = "Restored from cloud backup",
            templateType = templateType
        )
        dao.insertProject(newProject)

        val filesArray = json.optJSONArray("files")
        if (filesArray != null) {
            val restoredFiles = mutableListOf<FileEntity>()
            for (i in 0 until filesArray.length()) {
                val fObj = filesArray.getJSONObject(i)
                restoredFiles.add(
                    FileEntity(
                        id = UUID.randomUUID().toString(),
                        projectId = newProjectId,
                        name = fObj.optString("name", "file_$i"),
                        path = fObj.optString("path", "/file_$i"),
                        content = fObj.optString("content", ""),
                        isFolder = fObj.optBoolean("isFolder", false)
                    )
                )
            }
            dao.insertFiles(restoredFiles)
        }
        return newProjectId
    }

    // Virtual Shell Terminal command processor
    suspend fun executeTerminalCommand(
        projectId: String,
        currentDir: String,
        commandInput: String
    ): TerminalResult {
        val parts = commandInput.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (parts.isEmpty()) return TerminalResult("")

        val cmd = parts[0].lowercase()
        val args = parts.drop(1)
        val files = dao.getFilesForProjectOnce(projectId)

        return when (cmd) {
            "help" -> TerminalResult(
                """
VS Code Mobile Virtual Shell v1.1
Supported commands:
  ls, dir           List files in project
  cat <file>        Display content of a file
  touch <file>      Create a new file
  rm <file>         Delete a file
  build, build-apk  Build & compile project into standalone Mobile App / APK
  install           Install and launch the built app on mobile device
  run               Launch HTML5 live preview runner
  git status        Show working tree status
  git branch        List Git branches
  git log           Show commit history
  git commit -m     Commit staged changes
  eval <expr>       Evaluate JavaScript expression
  echo <text>       Print text to terminal
  clear             Clear terminal screen
  help              Display this command guide
                """.trimIndent()
            )
            "build", "build-apk" -> TerminalResult("TRIGGER_BUILD")
            "install" -> TerminalResult("TRIGGER_INSTALL")
            "npm" -> {
                if (args.contains("build") || args.contains("run")) TerminalResult("TRIGGER_BUILD")
                else TerminalResult("npm: executed '${args.joinToString(" ")}'")
            }
            "eval" -> {
                if (args.isEmpty()) TerminalResult("eval: missing JavaScript expression", isError = true)
                else {
                    val expr = args.joinToString(" ")
                    val result = when {
                        expr.contains("+") -> {
                            val nums = expr.split("+").mapNotNull { it.trim().toDoubleOrNull() }
                            if (nums.size > 1) "=> ${nums.sum()}" else "=> $expr"
                        }
                        expr.contains("*") -> {
                            val nums = expr.split("*").mapNotNull { it.trim().toDoubleOrNull() }
                            if (nums.size > 1) "=> ${nums.reduce { a, b -> a * b }}" else "=> $expr"
                        }
                        else -> "=> [JS Evaluation]: $expr"
                    }
                    TerminalResult(result)
                }
            }
            "ls", "dir" -> {
                val fileNames = files.map { it.name }
                TerminalResult(fileNames.joinToString("   "))
            }
            "cat" -> {
                if (args.isEmpty()) return TerminalResult("cat: missing operand", isError = true)
                val target = args[0]
                val file = files.find { it.name == target || it.path == target || it.path == "/$target" }
                if (file != null) {
                    TerminalResult(file.content)
                } else {
                    TerminalResult("cat: $target: No such file or directory", isError = true)
                }
            }
            "touch" -> {
                if (args.isEmpty()) return TerminalResult("touch: missing file operand", isError = true)
                val name = args[0]
                createFile(projectId, name, if (name.startsWith("/")) name else "/$name", "")
                TerminalResult("Created file: $name")
            }
            "rm" -> {
                if (args.isEmpty()) return TerminalResult("rm: missing operand", isError = true)
                val name = args[0]
                val file = files.find { it.name == name || it.path == name || it.path == "/$name" }
                if (file != null) {
                    deleteFile(file.id)
                    TerminalResult("Removed $name")
                } else {
                    TerminalResult("rm: cannot remove '$name': No such file", isError = true)
                }
            }
            "echo" -> TerminalResult(args.joinToString(" "))
            "git" -> handleGitCommand(projectId, args)
            "run" -> TerminalResult("TRIGGER_RUN")
            "node" -> {
                if (args.isEmpty()) return TerminalResult("node: specify a file (e.g., node game.js)")
                TerminalResult("[Node.js Sandbox] Executing ${args[0]}...\nProcess finished with exit code 0")
            }
            "python", "python3" -> {
                if (args.isEmpty()) return TerminalResult("python: specify a script file")
                TerminalResult("[Python 3.11 Sandbox] Executing ${args[0]}...\nDone.")
            }
            else -> TerminalResult("$cmd: command not found. Type 'help' for available commands.", isError = true)
        }
    }

    private suspend fun handleGitCommand(projectId: String, args: List<String>): TerminalResult {
        if (args.isEmpty()) return TerminalResult("git: command requires a subcommand (status, branch, log, commit)")
        val sub = args[0]
        val commits = dao.getCommitsForProject(projectId).first()

        return when (sub) {
            "status" -> {
                TerminalResult(
                    """
On branch main
Your branch is up to date with 'origin/main'.

Changes to be committed:
  (use "git restore --staged <file>..." to unstage)

Untracked files:
  (use "git add <file>..." to include in what will be committed)

nothing to commit, working tree clean
                    """.trimIndent()
                )
            }
            "branch" -> TerminalResult("* main\n  feature/canvas-game\n  release/v1.0")
            "log" -> {
                val sb = StringBuilder()
                commits.take(5).forEach {
                    sb.append("commit ${it.commitHash}\n")
                    sb.append("Author: ${it.author}\n")
                    sb.append("Date:   ${java.util.Date(it.timestamp)}\n\n")
                    sb.append("    ${it.message}\n\n")
                }
                TerminalResult(sb.toString().trimEnd())
            }
            "commit" -> {
                val msgIndex = args.indexOf("-m")
                val msg = if (msgIndex != -1 && msgIndex + 1 < args.size) {
                    args.drop(msgIndex + 1).joinToString(" ").removeSurrounding("\"")
                } else {
                    "Manual commit from terminal"
                }
                val newCommit = createCommit(projectId, msg, "main")
                TerminalResult("[main ${newCommit.commitHash}] $msg")
            }
            else -> TerminalResult("git: '$sub' is not a git command. See 'help'.")
        }
    }

    private fun Pair<ProjectEntity, List<FileEntity>>.copyFirst(name: String, desc: String): Pair<ProjectEntity, List<FileEntity>> {
        return Pair(first.copy(name = name, description = desc), second)
    }
}

data class TerminalResult(
    val output: String,
    val isError: Boolean = false
)
