package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.TokenComment
import com.example.ui.theme.TokenFunction
import com.example.ui.theme.TokenKeyword
import com.example.ui.theme.TokenNumber
import com.example.ui.theme.TokenProperty
import com.example.ui.theme.TokenString
import com.example.ui.theme.TokenTag
import com.example.ui.theme.TokenType
import com.example.ui.theme.VSCodeTextPrimary
import java.util.regex.Pattern

object SyntaxHighlighter {

    // General & Web Keywords (JS, TS, Python, Kotlin, Java, C/C++)
    private val KEYWORD_PATTERN = Pattern.compile(
        "\\b(const|let|var|function|return|if|else|for|while|do|switch|case|break|continue|class|extends|super|import|export|from|default|new|this|typeof|instanceof|void|async|await|try|catch|finally|throw|val|fun|package|interface|enum|object|abstract|override|open|def|pass|lambda|yield|public|private|protected|static|final|struct|match|fn|mut|impl|trait|echo|exit)\\b"
    )

    private val TYPE_PATTERN = Pattern.compile(
        "\\b(String|Number|Boolean|Array|Object|Promise|Int|Float|Double|Long|Short|Byte|Char|List|Map|Set|Unit|Any|true|false|null|undefined|None|void|int|float|double|char|bool|Self|Option|Result)\\b"
    )

    private val STRING_PATTERN = Pattern.compile(
        "(\"[^\"]*\"|'[^']*'|`[^`]*`)"
    )

    private val NUMBER_PATTERN = Pattern.compile(
        "\\b\\d+(\\.\\d+)?(f|L|u)?\\b"
    )

    private val COMMENT_PATTERN = Pattern.compile(
        "(//.*|/\\*[\\s\\S]*?\\*/|<!--[\\s\\S]*?-->|#.*)"
    )

    private val FUNCTION_PATTERN = Pattern.compile(
        "\\b([a-zA-Z_]\\w*)(?=\\s*\\()"
    )

    private val TAG_PATTERN = Pattern.compile(
        "(</?[a-zA-Z0-9\\-]+|/?>)"
    )

    private val CSS_PROPERTY_PATTERN = Pattern.compile(
        "\\b(color|background(-color)?|padding(-top|-bottom|-left|-right)?|margin(-top|-bottom|-left|-right)?|display|flex(-direction|-wrap)?|justify-content|align-items|width|height|border(-radius|-color|-width)?|position|top|bottom|left|right|font(-size|-family|-weight)?|z-index|opacity|overflow|box-shadow|text-align)\\b(?=\\s*:)"
    )

    private val MARKDOWN_HEADER_PATTERN = Pattern.compile(
        "^(#{1,6}\\s+.*)$", Pattern.MULTILINE
    )

    fun highlight(text: String, extension: String = "js"): AnnotatedString {
        if (text.length > 60000) {
            // Guard for extremely large files to prevent UI frame stutter
            return AnnotatedString(text)
        }

        val ext = extension.lowercase()

        return buildAnnotatedString {
            append(text)
            // Default base styling
            addStyle(
                SpanStyle(color = VSCodeTextPrimary, fontFamily = FontFamily.Monospace),
                0,
                text.length
            )

            // Markdown specific styling
            if (ext == "md" || ext == "markdown") {
                applyPattern(MARKDOWN_HEADER_PATTERN, text) { start, end ->
                    addStyle(SpanStyle(color = TokenKeyword, fontWeight = FontWeight.Bold), start, end)
                }
                applyPattern(STRING_PATTERN, text) { start, end ->
                    addStyle(SpanStyle(color = TokenString), start, end)
                }
                return@buildAnnotatedString
            }

            // CSS specific styling
            if (ext == "css" || ext == "scss") {
                applyPattern(CSS_PROPERTY_PATTERN, text) { start, end ->
                    addStyle(SpanStyle(color = TokenProperty), start, end)
                }
                applyPattern(NUMBER_PATTERN, text) { start, end ->
                    addStyle(SpanStyle(color = TokenNumber), start, end)
                }
                applyPattern(STRING_PATTERN, text) { start, end ->
                    addStyle(SpanStyle(color = TokenString), start, end)
                }
                applyPattern(COMMENT_PATTERN, text) { start, end ->
                    addStyle(SpanStyle(color = TokenComment, fontStyle = FontStyle.Italic), start, end)
                }
                return@buildAnnotatedString
            }

            // Code functions
            applyPattern(FUNCTION_PATTERN, text) { start, end ->
                addStyle(SpanStyle(color = TokenFunction), start, end)
            }

            // Keywords
            applyPattern(KEYWORD_PATTERN, text) { start, end ->
                addStyle(SpanStyle(color = TokenKeyword, fontWeight = FontWeight.SemiBold), start, end)
            }

            // Types & Constants
            applyPattern(TYPE_PATTERN, text) { start, end ->
                addStyle(SpanStyle(color = TokenType), start, end)
            }

            // HTML / XML / SVG Tags
            if (ext in listOf("html", "xml", "svg", "jsx", "tsx")) {
                applyPattern(TAG_PATTERN, text) { start, end ->
                    addStyle(SpanStyle(color = TokenTag), start, end)
                }
            }

            // Numbers
            applyPattern(NUMBER_PATTERN, text) { start, end ->
                addStyle(SpanStyle(color = TokenNumber), start, end)
            }

            // Strings
            applyPattern(STRING_PATTERN, text) { start, end ->
                addStyle(SpanStyle(color = TokenString), start, end)
            }

            // Comments (always highest precedence)
            applyPattern(COMMENT_PATTERN, text) { start, end ->
                addStyle(SpanStyle(color = TokenComment, fontStyle = FontStyle.Italic), start, end)
            }
        }
    }

    private inline fun applyPattern(
        pattern: Pattern,
        text: String,
        crossinline applyStyle: (start: Int, end: Int) -> Unit
    ) {
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            applyStyle(matcher.start(), matcher.end())
        }
    }
}
