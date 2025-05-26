package com.example.sih.ui.screens.blog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichText
import com.halilibo.richtext.ui.RichTextStyle

@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier
) {
    // Using compose-richtext for markdown rendering
    RichText(
        modifier = modifier,
        style = RichTextStyle(
            codeBlockStyle = CodeBlockStyle(
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    background = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(8.dp)
            )
        )
    ) {
        Markdown(content = content)
    }
}

@Composable
fun MarkdownEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = content,
        onValueChange = onContentChange,
        modifier = modifier.fillMaxSize(),
        textStyle = LocalTextStyle.current.copy(
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}