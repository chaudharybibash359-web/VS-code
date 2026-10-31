package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val templateType: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val activeBranch: String = "main"
)

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val path: String,
    val content: String,
    val isFolder: Boolean = false,
    val parentPath: String = "/",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "git_commits")
data class GitCommitEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val commitHash: String,
    val message: String,
    val author: String = "Developer <dev@vscode.mobile>",
    val timestamp: Long = System.currentTimeMillis(),
    val branchName: String = "main"
)

@Entity(tableName = "cloud_backups")
data class CloudBackupEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val projectName: String,
    val backupTimestamp: Long = System.currentTimeMillis(),
    val filesCount: Int,
    val snapshotJson: String
)
