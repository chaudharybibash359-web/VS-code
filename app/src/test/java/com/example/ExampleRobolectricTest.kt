package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.build.AppBuilder
import com.example.data.templates.ProjectTemplates
import com.example.ui.editor.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VS Code", appName)
  }

  @Test
  fun `verify space shooter project template generated correctly`() {
    val (project, files) = ProjectTemplates.createSpaceShooterProject()
    assertEquals("Space Shooter 2D", project.name)
    assertEquals("space_game", project.templateType)
    assertTrue("Project should contain at least 4 files", files.size >= 4)
    assertNotNull(files.find { it.name == "index.html" })
    assertNotNull(files.find { it.name == "game.js" })
  }

  @Test
  fun `verify syntax highlighter highlights keywords and functions across languages`() {
    val jsCode = "function restartGame() { const score = 100; return score; }"
    val jsAnnotated = SyntaxHighlighter.highlight(jsCode, "js")
    assertEquals(jsCode, jsAnnotated.text)
    assertTrue("Should have span styles for JS", jsAnnotated.spanStyles.isNotEmpty())

    val pyCode = "def calculate_points(level):\n    return level * 10"
    val pyAnnotated = SyntaxHighlighter.highlight(pyCode, "py")
    assertEquals(pyCode, pyAnnotated.text)
    assertTrue("Should have span styles for Python", pyAnnotated.spanStyles.isNotEmpty())

    val htmlCode = "<div class=\"game\"><button>Play</button></div>"
    val htmlAnnotated = SyntaxHighlighter.highlight(htmlCode, "html")
    assertEquals(htmlCode, htmlAnnotated.text)
    assertTrue("Should have span styles for HTML", htmlAnnotated.spanStyles.isNotEmpty())
  }

  @Test
  fun `verify AppBuilder compiles and packages project into standalone bundle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val (project, files) = ProjectTemplates.createSpaceShooterProject()

    val result = AppBuilder.buildProject(context, project, files)
    assertTrue("Build should succeed", result.isSuccess)
    assertNotNull("Bundle HTML should not be null", result.bundleHtml)
    assertTrue("Bundle should contain inlined game.js content", result.bundleHtml.contains("Space Shooter 2D Engine"))
    assertTrue("Bundle should have APK package created", result.apkFile != null && result.apkFile.exists())
  }

  @Test
  fun `verify buildFileTree constructs hierarchical tree with correct depths and nesting`() {
    val (_, files) = ProjectTemplates.createSpaceShooterProject()
    val tree = com.example.ui.components.buildFileTree(files)

    // Verify root items exist (e.g., index.html, /src folder, /styles folder, /assets folder)
    assertTrue("Tree should not be empty", tree.isNotEmpty())
    val srcFolder = tree.find { it.file.isFolder && it.file.name == "src" }
    assertNotNull("src folder should be in root nodes", srcFolder)
    assertEquals(0, srcFolder!!.depth)
    assertTrue("src folder should contain game.js child", srcFolder.children.any { it.file.name == "game.js" })
    assertEquals(1, srcFolder.children.first { it.file.name == "game.js" }.depth)

    val stylesFolder = tree.find { it.file.isFolder && it.file.name == "styles" }
    assertNotNull("styles folder should exist", stylesFolder)
    assertTrue("styles should contain style.css", stylesFolder!!.children.any { it.file.name == "style.css" })
  }
}
