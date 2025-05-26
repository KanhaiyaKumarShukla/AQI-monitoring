package com.example.sih.ui.screens.blog
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sih.model.BlogPost
import com.example.sih.model.formatAsString
import com.example.sih.viewmodel.BlogViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogListScreen(
    viewModel: BlogViewModel,
    navController: NavController
) {
    val blogs by viewModel.blogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Blogs", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    FilledTonalButton(
                        onClick = { navController.navigate("my_blogs") },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSystemInDarkTheme()) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) // Darker in dark theme
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) // Slightly darker in light theme
                            },
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "My Blogs",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("My Blogs", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("editor") },
                icon = { Icon(Icons.Default.Edit, "New") },
                text = { Text("Create Blog") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            } else if (blogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Article,
                            contentDescription = "No blogs",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No blogs yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Be the first to share your thoughts!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(blogs) { blog ->
                        BlogCard(blog) { navController.navigate("blog_detail/${blog.id}") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlogCard(blog: BlogPost, onClick: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSystemInDarkTheme()) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Featured image if available
            blog.featuredImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Blog cover image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Title
            Text(
                text = blog.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Clean content preview (removes markdown syntax)
            val cleanContent = remember(blog.content) {
                blog.content
                    .replace(Regex("""^#+\s*"""), "") // Remove headings
                    .replace(Regex("""[*_]{1,2}"""), "") // Remove bold/italic
                    .replace(Regex("""\[(.*?)\]\(.*?\)"""), "$1") // Remove links
                    .replace(Regex("""`{1,3}"""), "") // Remove code markers
                    .replace(Regex("""^\s*[-*+]\s*"""), "") // Remove list markers
                    .trim()
            }

            Text(
                text = cleanContent.take(150) + if (cleanContent.length > 150) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Author and date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Author chip
                AssistChip(
                    onClick = {},
                    label = { Text(blog.authorName) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Author",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Date
                Text(
                    text = blog.createdAt.toDate().formatAsString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Tags if available
            if (blog.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    blog.tags.forEach { tag ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogEditorScreen(
    viewModel: BlogViewModel,
    navController: NavController,
    blogId: String? = null
) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var isPreview by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (isPreview) {
            isPreview = false // Switch to editor mode
        } else {
            navController.popBackStack() // Exit screen
        }
    }

    // Load existing blog if editing
    if (blogId != null) {
        LaunchedEffect(blogId) {
            viewModel.blogs.value.find { it.id == blogId }?.let { blog ->
                title = blog.title
                content = blog.content
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (blogId == null) "New Blog Post" else "Edit Post") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isPreview) {
                                isPreview = false // Switch to editor mode
                            } else {
                                navController.popBackStack() // Exit screen
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Preview toggle button with better visual feedback
                    FilledTonalIconButton(
                        onClick = { isPreview = !isPreview },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isPreview) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            }
                            else{
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            if (isPreview) Icons.Default.Edit else Icons.Default.Visibility,
                            if (isPreview) "Edit Mode" else "Preview Mode"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Save button with more prominent design
            FloatingActionButton(
                onClick = {
                    if (blogId == null) {
                        Log.d("Firebase", "creating blog with ID")
                        viewModel.createBlog(title, content) {
                            navController.popBackStack()
                        }
                    } else {
                        viewModel.updateBlog(
                            BlogPost(
                                id = blogId,
                                title = title,
                                content = content,
                                authorId = viewModel.user?.uid ?: "",
                                authorName = viewModel.user?.displayName ?: "Anonymous"
                            )
                        ) {
                            navController.popBackStack()
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Save, "Save Post")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!isPreview) {
                // Editor mode
                Column(modifier = Modifier.fillMaxSize()) {
                    // Title field with clear visual hierarchy
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Post Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            errorTextColor = MaterialTheme.colorScheme.error,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            errorContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            errorCursorColor = MaterialTheme.colorScheme.error,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                            disabledIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                            errorIndicatorColor = MaterialTheme.colorScheme.error,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            errorLeadingIconColor = MaterialTheme.colorScheme.error,
                            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            errorTrailingIconColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            errorLabelColor = MaterialTheme.colorScheme.error,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            errorPlaceholderColor = MaterialTheme.colorScheme.error
                        )
                    )

                    // Editor section with clear header
                    Text(
                        text = "Content (Markdown supported)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Markdown editor with better visual cues
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(16.dp)
                    ) {
                        BasicTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            decorationBox = { innerTextField ->
                                if (content.isEmpty()) {
                                    Text(
                                        "Start writing your blog content here...\n\n" +
                                                "Use # for headings\n" +
                                                "**bold** for bold text\n" +
                                                "*italic* for italic text",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    // Markdown quick reference
                    MarkdownQuickReference(modifier = Modifier.padding(16.dp))
                }
            } else {
                // Preview mode
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (title.isNotEmpty()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    MarkdownContent(content)
                }
            }
        }
    }
}

@Composable
fun MarkdownQuickReference(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Markdown Quick Reference",
                    style = MaterialTheme.typography.labelLarge
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text("- # Heading 1", style = MaterialTheme.typography.bodySmall)
                    Text("- ## Heading 2", style = MaterialTheme.typography.bodySmall)
                    Text("- **bold** text", style = MaterialTheme.typography.bodySmall)
                    Text("- *italic* text", style = MaterialTheme.typography.bodySmall)
                    Text("- [link](url)", style = MaterialTheme.typography.bodySmall)
                    Text("- `code` snippet", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun MarkdownContent(content: String) {
    RichText {
        Markdown(content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBlogsScreen(
    viewModel: BlogViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val blogs by viewModel.userBlogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Blogs") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("editor") }) {
                        Icon(Icons.Default.Add, "New Blog")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (blogs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You haven't written any blogs yet")
                    Button(
                        onClick = { navController.navigate("editor") },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Create Your First Blog")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(blogs) { blog ->
                    BlogCardWithActions(
                        blog = blog,
                        onEdit = { navController.navigate("editor/${blog.id}") },
                        onDelete = { viewModel.deleteBlog(blog.id) {
                            Toast.makeText(context, "Blog deleted", Toast.LENGTH_SHORT).show()
                        } }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlogCardWithActions(
    blog: BlogPost,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        onClick = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDarkTheme) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Featured image if available
            blog.featuredImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Blog cover image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Title and action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = blog.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Action buttons with subtle styling
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clean content preview
            val cleanContent = remember(blog.content) {
                blog.content
                    .replace(Regex("""^#+\s*"""), "")
                    .replace(Regex("""[*_]{1,2}"""), "")
                    .replace(Regex("""\[(.*?)\]\(.*?\)"""), "$1")
                    .replace(Regex("""`{1,3}"""), "")
                    .replace(Regex("""^\s*[-*+]\s*"""), "")
                    .trim()
            }

            Text(
                text = cleanContent.take(150) + if (cleanContent.length > 150) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Date with update indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = blog.updatedAt.toDate().formatAsString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (blog.createdAt != blog.updatedAt) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edited",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Tags if available
            if (blog.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    blog.tags.forEach { tag ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlogDetailScreen(
    blogId: String,
    viewModel: BlogViewModel,
    onBack: () -> Unit
) {
    val blog by remember(blogId) {
        derivedStateOf { viewModel.blogs.value.find { it.id == blogId } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = blog?.title?.take(20)?.let {
                            if (blog!!.title.length > 20) "$it..." else it
                        } ?: "Blog Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { padding ->
        if (blog == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = "Not found",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Blog not found",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                blog?.let { post ->
                    // Featured Image with shadow
                    if (post.featuredImageUrl != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            AsyncImage(
                                model = post.featuredImageUrl,
                                contentDescription = "Featured image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Title with proper spacing
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Author and date row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Author chip
                        AssistChip(
                            onClick = {},
                            label = { Text(post.authorName) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Author",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        // Date with icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Published date",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.createdAt.toDate().formatAsString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Tags (if available)
                    if (post.tags.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            post.tags.forEach { tag ->
                                FilterChip(
                                    selected = false,
                                    onClick = {},
                                    label = { Text(tag) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    // Divider before content
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Blog content with proper padding
                    MarkdownContent(
                        content = post.content,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Spacer at bottom
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

