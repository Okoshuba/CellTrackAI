package com.kubaice.celltrackai.presentation.literature

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kubaice.celltrackai.R
import com.kubaice.celltrackai.domain.model.ScientificPublication

@Composable
fun LiteratureSearchScreen(
    viewModel: LiteratureSearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LiteratureSearchScreenContent(
        state = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onPerformSearch = viewModel::performSearch,
        onToggleAssociation = viewModel::toggleAssociation,
        clearMessage = viewModel::clearMessage,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiteratureSearchScreenContent(
    state: LiteratureSearchUiState,
    onSearchQueryChanged: (String) -> Unit,
    onPerformSearch: () -> Unit,
    onToggleAssociation: (ScientificPublication) -> Unit,
    clearMessage: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.title_literature_search), fontWeight = FontWeight.Bold)
                        if (!state.cultureName.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.label_associating_with, state.cultureName),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.padding_large))
        ) {
            // Search Bar & Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_small)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.placeholder_search_pubmed)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.cd_clear)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            onPerformSearch()
                        }
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Button(
                    onClick = {
                        keyboardController?.hide()
                        onPerformSearch()
                    },
                    enabled = !state.isLoading
                ) {
                    Text(stringResource(R.string.btn_search))
                }
            }

            // Mock Service Notice Banner
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_tiny))
            ) {
                Row(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))
                    )
                    Text(
                        text = stringResource(R.string.header_mock_literature_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.publications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_huge)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.icon_size_huge))
                                .width(dimensionResource(R.dimen.icon_size_huge)),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                        Text(
                            text = stringResource(R.string.header_no_publications_found),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                        Text(
                            text = stringResource(R.string.header_no_publications_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.publications, key = { it.id }) { publication ->
                            val isAssociated = state.associatedPublicationIds.contains(publication.id)
                            PublicationCard(
                                publication = publication,
                                isAssociated = isAssociated,
                                canAssociate = !state.cultureId.isNullOrEmpty(),
                                onToggleAssociation = { onToggleAssociation(publication) },
                                onOpenUrl = {
                                    publication.externalUrl?.let { url ->
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large))) }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicationCard(
    publication: ScientificPublication,
    isAssociated: Boolean,
    canAssociate: Boolean,
    onToggleAssociation: () -> Unit,
    onOpenUrl: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            Text(
                text = publication.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (publication.authors.isNotEmpty()) {
                Text(
                    text = publication.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            val metaText = buildString {
                publication.publicationYear?.let { append("$it") }
                if (!publication.source.isNullOrBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(publication.source)
                }
            }
            if (metaText.isNotBlank()) {
                Text(
                    text = metaText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!publication.abstractText.isNullOrBlank()) {
                Text(
                    text = publication.abstractText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.padding_tiny)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!publication.externalUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = onOpenUrl,
                        modifier = Modifier.height(dimensionResource(R.dimen.button_height_small))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.icon_size_small))
                                .width(dimensionResource(R.dimen.icon_size_small))
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_tiny)))
                        Text(stringResource(R.string.btn_view_paper), style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (canAssociate) {
                    Button(
                        onClick = onToggleAssociation,
                        modifier = Modifier.height(dimensionResource(R.dimen.button_height_small))
                    ) {
                        Icon(
                            imageVector = if (isAssociated) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier
                                .height(dimensionResource(R.dimen.icon_size_small))
                                .width(dimensionResource(R.dimen.icon_size_small))
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_tiny)))
                        Text(
                            text = stringResource(if (isAssociated) R.string.btn_associated else R.string.btn_associate),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun LiteratureSearchScreenPreview() {
    MaterialTheme {
        Surface {
            LiteratureSearchScreenContent(
                state = LiteratureSearchUiState(
                    isLoading = false,
                    searchQuery = "iPSC",
                    publications = listOf(
                        ScientificPublication(
                            id = "pub1",
                            title = "Stem cell differentiation protocols in 3D culture",
                            authors = listOf("Smith J.", "Doe A."),
                            source = "Cell Biology Journal",
                            publicationYear = 2023,
                            abstractText = "Abstract snippet describing the research..."
                        )
                    )
                ),
                onSearchQueryChanged = {},
                onPerformSearch = {},
                onToggleAssociation = {},
                clearMessage = {},
                onNavigateBack = {}
            )
        }
    }
}
