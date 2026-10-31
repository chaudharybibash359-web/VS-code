package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeStudioDao {

    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)

    // Files
    @Query("SELECT * FROM files WHERE projectId = :projectId ORDER BY isFolder DESC, name ASC")
    fun getFilesForProject(projectId: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE projectId = :projectId ORDER BY isFolder DESC, name ASC")
    suspend fun getFilesForProjectOnce(projectId: String): List<FileEntity>

    @Query("SELECT * FROM files WHERE id = :fileId LIMIT 1")
    suspend fun getFileById(fileId: String): FileEntity?

    @Query("SELECT * FROM files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: String, path: String): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Query("DELETE FROM files WHERE id = :fileId")
    suspend fun deleteFileById(fileId: String)

    @Query("DELETE FROM files WHERE projectId = :projectId AND (path = :folderPath OR parentPath = :folderPath OR parentPath LIKE :folderPrefix)")
    suspend fun deleteFolderAndChildren(projectId: String, folderPath: String, folderPrefix: String)

    @Query("DELETE FROM files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: String)

    // Git Commits
    @Query("SELECT * FROM git_commits WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getCommitsForProject(projectId: String): Flow<List<GitCommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: GitCommitEntity)

    // Cloud Backups
    @Query("SELECT * FROM cloud_backups ORDER BY backupTimestamp DESC")
    fun getAllBackups(): Flow<List<CloudBackupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: CloudBackupEntity)

    @Query("DELETE FROM cloud_backups WHERE id = :backupId")
    suspend fun deleteBackupById(backupId: String)
}
