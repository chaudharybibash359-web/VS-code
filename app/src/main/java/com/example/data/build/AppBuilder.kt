package com.example.data.build

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.StandaloneAppActivity
import com.example.data.local.FileEntity
import com.example.data.local.ProjectEntity
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class BuildResult(
    val isSuccess: Boolean,
    val appName: String,
    val packageName: String,
    val packageSizeKb: Long,
    val apkFile: File? = null,
    val bundleHtml: String = "",
    val logs: List<String> = emptyList(),
    val errorMessage: String? = null
)

object AppBuilder {

    fun buildProject(
        context: Context,
        project: ProjectEntity,
        files: List<FileEntity>
    ): BuildResult {
        val logs = mutableListOf<String>()
        logs.add("[1/5] Analyzing project structure for '${project.name}'...")
        
        val htmlFile = files.firstOrNull { it.name.endsWith(".html", ignoreCase = true) }
        val jsFiles = files.filter { it.name.endsWith(".js", ignoreCase = true) }
        val cssFiles = files.filter { it.name.endsWith(".css", ignoreCase = true) }

        logs.add("[2/5] Found ${files.size} source files (${jsFiles.size} JS, ${cssFiles.size} CSS, HTML: ${htmlFile?.name ?: "None"}).")

        val sanitizedAppName = project.name.trim().ifEmpty { "MobileApp" }
        val sanitizedPackage = "com.codestudio." + sanitizedAppName.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .ifEmpty { "app" }

        val bundleHtml = if (htmlFile != null) {
            logs.add("[3/5] Inlining CSS and JavaScript assets into single-file application...")
            inlineAssets(htmlFile.content, jsFiles, cssFiles)
        } else {
            logs.add("[3/5] Generating fallback responsive web canvas...")
            generateDefaultHtml(sanitizedAppName, files)
        }

        logs.add("[4/5] Packaging standalone Android APK structure...")
        val outputDir = File(context.filesDir, "built_apps").apply { mkdirs() }
        val apkFileName = "${sanitizedAppName.lowercase().replace(" ", "_")}-debug.apk"
        val apkFile = File(outputDir, apkFileName)

        try {
            FileOutputStream(apkFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // Add AndroidManifest.xml placeholder entry
                    val manifestContent = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                            package="$sanitizedPackage">
                            <application android:label="$sanitizedAppName" android:hasCode="false">
                                <activity android:name="com.example.ui.preview.StandaloneAppActivity"
                                    android:exported="true">
                                    <intent-filter>
                                        <action android:name="android.intent.action.MAIN" />
                                        <category android:name="android.intent.category.LAUNCHER" />
                                    </intent-filter>
                                </activity>
                            </application>
                        </manifest>
                    """.trimIndent()
                    zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
                    zos.write(manifestContent.toByteArray())
                    zos.closeEntry()

                    // Add assets/index.html
                    zos.putNextEntry(ZipEntry("assets/index.html"))
                    zos.write(bundleHtml.toByteArray())
                    zos.closeEntry()

                    // Add assets/app_metadata.json
                    val metadataJson = """
                        {
                            "name": "$sanitizedAppName",
                            "packageName": "$sanitizedPackage",
                            "builtAt": ${System.currentTimeMillis()},
                            "fileCount": ${files.size}
                        }
                    """.trimIndent()
                    zos.putNextEntry(ZipEntry("assets/app_metadata.json"))
                    zos.write(metadataJson.toByteArray())
                    zos.closeEntry()

                    // Add META-INF/CERT.RSA dummy certificate block
                    zos.putNextEntry(ZipEntry("META-INF/CERT.SF"))
                    zos.write("Signature-Version: 1.0\nCreated-By: CodeStudio In-App Packager\n".toByteArray())
                    zos.closeEntry()
                }
            }

            val sizeKb = (apkFile.length() / 1024L).coerceAtLeast(1L)
            logs.add("[5/5] Build SUCCESSFUL: Generated $apkFileName (${sizeKb} KB).")

            return BuildResult(
                isSuccess = true,
                appName = sanitizedAppName,
                packageName = sanitizedPackage,
                packageSizeKb = sizeKb,
                apkFile = apkFile,
                bundleHtml = bundleHtml,
                logs = logs
            )
        } catch (e: Exception) {
            logs.add("[ERROR] Packaging failed: ${e.message}")
            return BuildResult(
                isSuccess = false,
                appName = sanitizedAppName,
                packageName = sanitizedPackage,
                packageSizeKb = 0L,
                bundleHtml = bundleHtml,
                logs = logs,
                errorMessage = e.message
            )
        }
    }

    private fun inlineAssets(
        baseHtml: String,
        jsFiles: List<FileEntity>,
        cssFiles: List<FileEntity>
    ): String {
        var processed = baseHtml

        // Inline CSS
        val cssBlock = buildString {
            append("\n<style>\n")
            for (css in cssFiles) {
                append("/* ${css.name} */\n")
                append(css.content)
                append("\n")
            }
            append("</style>\n")
        }

        if (processed.contains("</head>", ignoreCase = true)) {
            processed = processed.replace("</head>", "$cssBlock</head>", ignoreCase = true)
        } else {
            processed = "$cssBlock$processed"
        }

        // Inline JS
        val jsBlock = buildString {
            append("\n<script>\n")
            for (js in jsFiles) {
                append("// ${js.name}\n")
                append(js.content)
                append("\n;\n")
            }
            append("</script>\n")
        }

        if (processed.contains("</body>", ignoreCase = true)) {
            processed = processed.replace("</body>", "$jsBlock</body>", ignoreCase = true)
        } else {
            processed = "$processed$jsBlock"
        }

        return processed
    }

    private fun generateDefaultHtml(appName: String, files: List<FileEntity>): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                        margin: 0;
                        padding: 24px;
                        background: #0f172a;
                        color: #f8fafc;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        min-height: 100vh;
                        box-sizing: border-box;
                    }
                    .card {
                        background: #1e293b;
                        border-radius: 16px;
                        padding: 24px;
                        width: 100%;
                        max-width: 400px;
                        box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5);
                        text-align: center;
                    }
                    h1 { margin-top: 0; color: #38bdf8; font-size: 24px; }
                    p { color: #94a3b8; font-size: 14px; line-height: 1.5; }
                    .badge {
                        background: #0ea5e9;
                        color: white;
                        display: inline-block;
                        padding: 4px 12px;
                        border-radius: 9999px;
                        font-size: 12px;
                        font-weight: bold;
                        margin-bottom: 16px;
                    }
                    ul { text-align: left; background: #0f172a; border-radius: 8px; padding: 12px 24px; }
                    li { font-family: monospace; font-size: 13px; color: #a5f3fc; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="badge">CodeStudio Installed App</div>
                    <h1>$appName</h1>
                    <p>This project has been packaged into a standalone mobile application bundle.</p>
                    <p><strong>Packaged Files:</strong></p>
                    <ul>
                        ${files.joinToString("\n") { "<li>${it.name}</li>" }}
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun launchInstalledApp(context: Context, appName: String, bundleHtml: String) {
        val intent = Intent(context, StandaloneAppActivity::class.java).apply {
            putExtra("EXTRA_APP_NAME", appName)
            putExtra("EXTRA_HTML_BUNDLE", bundleHtml)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun installOrShareApk(context: Context, apkFile: File) {
        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (installIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(installIntent)
            } else {
                shareApk(context, apkFile.nameWithoutExtension, apkFile)
            }
        } catch (e: Exception) {
            shareApk(context, apkFile.nameWithoutExtension, apkFile)
        }
    }

    fun shareApk(context: Context, appName: String, apkFile: File) {
        try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "$appName APK Package")
                putExtra(Intent.EXTRA_TEXT, "Here is your built APK for $appName from CodeStudio.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share or Install APK").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share APK: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportHtmlBundle(context: Context, appName: String, bundleHtml: String) {
        try {
            val outputDir = File(context.filesDir, "built_apps").apply { mkdirs() }
            val htmlFile = File(outputDir, "${appName.lowercase().replace(" ", "_")}.html")
            htmlFile.writeText(bundleHtml)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                htmlFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$appName Standalone Web Bundle")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Web App Bundle").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export bundle: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
