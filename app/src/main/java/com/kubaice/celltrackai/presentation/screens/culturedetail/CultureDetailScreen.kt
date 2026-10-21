package com.kubaice.celltrackai.presentation.screens.culturedetail

import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kubaice.celltrackai.R
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.model.ScientificPublication
import com.kubaice.celltrackai.presentation.elements.CellTrackTopAppBar
import com.kubaice.celltrackai.presentation.elements.CultureStatusBadge
import com.kubaice.celltrackai.presentation.elements.EventTypeIcon
import com.kubaice.celltrackai.presentation.elements.LoadingIndicator
import com.kubaice.celltrackai.presentation.elements.PublicationItemCard

private const val CULTURE_TITLE_MAX_LINES = 2

@Composable
fun CultureDetailScreen(
    viewModel: CultureDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditCulture: (String) -> Unit,
    onNavigateToAddEvent: (String) -> Unit,
    onNavigateToLiteratureSearch: (String) -> Unit,
    onNavigateToImagePreview: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val csvSuccessMsg = stringResource(R.string.msg_csv_exported)
    val csvFailedMsg = stringResource(R.string.msg_csv_export_failed)

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val csvText = viewModel.generateCsvContent()
                    outputStream.write(csvText.toByteArray())
                }
                scope.launch { snackbarHostState.showSnackbar(csvSuccessMsg) }
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar(String.format(csvFailedMsg, e.message ?: "")) }
            }
        }
    }

    CultureDetailScreenContent(
        state = uiState,
        snackbarHostState = snackbarHostState,
        onToggleArchiveStatus = viewModel::toggleArchiveStatus,
        onExportCsv = {
            val cultureName = uiState.culture?.name?.lowercase()?.replace(" ", "_") ?: "culture"
            val fileName = "culture_${cultureName}_export.csv"
            exportCsvLauncher.launch(fileName)
        },
        onRemovePublicationAssociation = viewModel::removePublicationAssociation,
        onDeleteEvent = viewModel::deleteEvent,
        clearMessage = viewModel::clearMessage,
        onNavigateBack = onNavigateBack,
        onNavigateToEditCulture = onNavigateToEditCulture,
        onNavigateToAddEvent = onNavigateToAddEvent,
        onNavigateToLiteratureSearch = onNavigateToLiteratureSearch,
        onNavigateToImagePreview = onNavigateToImagePreview
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CultureDetailScreenContent(
    state: CultureDetailUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onToggleArchiveStatus: () -> Unit,
    onExportCsv: () -> Unit,
    onRemovePublicationAssociation: (String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    clearMessage: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditCulture: (String) -> Unit,
    onNavigateToAddEvent: (String) -> Unit,
    onNavigateToLiteratureSearch: (String) -> Unit,
    onNavigateToImagePreview: (String) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CellTrackTopAppBar(
                title = state.culture?.name ?: stringResource(R.string.title_culture_details),
                onNavigateBack = onNavigateBack,
                actions = {
                    state.culture?.let { culture ->
                        IconButton(onClick = { onNavigateToEditCulture(culture.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.cd_edit_culture)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.culture == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_24)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.msg_culture_not_found),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                val culture = state.culture

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensionResource(R.dimen.padding_16)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
                ) {
                    item {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                        // Top Section: Metadata Card
                        CultureMetadataSection(
                            culture = culture,
                            onToggleArchive = onToggleArchiveStatus
                        )
                    }

                    item {
                        // Action Buttons Row
                        Column(
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                            ) {
                                Button(
                                    onClick = { onNavigateToAddEvent(culture.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_tiny)))
                                    Text(stringResource(R.string.btn_add_event))
                                }

                                OutlinedButton(
                                    onClick = { onNavigateToLiteratureSearch(culture.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_tiny)))
                                    Text(stringResource(R.string.btn_literature))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                            ) {
                                OutlinedButton(
                                    onClick = onExportCsv,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_tiny)))
                                    Text(stringResource(R.string.btn_export_csv))
                                }

                                OutlinedButton(
                                    onClick = onToggleArchiveStatus,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val isActive = culture.status == CultureStatus.ACTIVE
                                    Icon(
                                        imageVector = if (isActive) Icons.Default.Archive else Icons.Default.Unarchive,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_tiny)))
                                    Text(stringResource(if (isActive) R.string.btn_archive else R.string.btn_unarchive))
                                }
                            }
                        }
                    }

                    // Associated Publications Section
                    if (state.publications.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.header_associated_publications, state.publications.size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_8))
                            )
                        }

                        items(state.publications, key = { it.id }) { pub ->
                            PublicationItemCard(
                                publication = pub,
                                onOpenLink = { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                onRemoveAssociation = {
                                    onRemovePublicationAssociation(pub.id)
                                }
                            )
                        }
                    }

                    // Event Timeline Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = dimensionResource(R.dimen.padding_8)),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.header_event_timeline, state.events.size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (state.events.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimensionResource(R.dimen.padding_24)),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.header_no_events_logged),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                                    Button(onClick = { onNavigateToAddEvent(culture.id) }) {
                                        Text(stringResource(R.string.btn_log_first_event))
                                    }
                                }
                            }
                        }
                    } else {
                        items(state.events, key = { it.id }) { event ->
                            TimelineEventCard(
                                event = event,
                                onImageClick = { imageUri -> onNavigateToImagePreview(imageUri) },
                                onDeleteEvent = { onDeleteEvent(event.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_huge)))
                    }
                }
            }
        }
    }
}

@Composable
fun CultureMetadataSection(
    culture: CellCulture,
    onToggleArchive: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val startDateStr = remember(culture.startedAt) { dateFormat.format(Date(culture.startedAt)) }

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
                .padding(dimensionResource(R.dimen.padding_16))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = culture.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = CULTURE_TITLE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                CultureStatusBadge(
                    status = culture.status,
                    shape = MaterialTheme.shapes.small,
                    horizontalPadding = dimensionResource(R.dimen.padding_8),
                    verticalPadding = dimensionResource(R.dimen.padding_4),
                    modifier = Modifier.clickable(onClick = onToggleArchive)
                )
            }

            if (!culture.cellType.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_18))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = culture.cellType,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = stringResource(R.string.label_started, startDateStr),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!culture.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                Text(
                    text = culture.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun PublicationItemCard(
    publication: ScientificPublication,
    onOpenLink: (String) -> Unit,
    onRemoveAssociation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_12))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = publication.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onRemoveAssociation,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_remove_association),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (publication.authors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                Text(
                    text = publication.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sourceYear = listOfNotNull(publication.source, publication.publicationYear?.toString()).joinToString(" • ")
                Text(
                    text = sourceYear,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!publication.externalUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { onOpenLink(publication.externalUrl) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(dimensionResource(R.dimen.button_height_tiny))
                    ) {
                        Text(stringResource(R.string.btn_view_paper), style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_tiny)))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_tiny))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineEventCard(
    event: CultureEvent,
    onImageClick: (String) -> Unit,
    onDeleteEvent: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val formattedTime = remember(event.occurredAt) { dateFormat.format(Date(event.occurredAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation_small))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_16)),
            verticalAlignment = Alignment.Top
        ) {
            // Event Type Icon Container
            EventTypeIcon(type = event.type)

            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.type.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDeleteEvent,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete_event),
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))

                // Stats Row: Passage / Confluency
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    event.passageNumber?.let { passage ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = stringResource(R.string.label_passage_badge, passage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(R.dimen.padding_6),
                                    vertical = dimensionResource(R.dimen.padding_2),
                                ),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    event.confluencyPercentage?.let { confluency ->
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = stringResource(R.string.label_confluency_badge, confluency.toInt()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(R.dimen.padding_6),
                                    vertical = dimensionResource(R.dimen.padding_2),
                                ),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (!event.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Image Thumbnails Row
                if (event.imageUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                    ) {
                        event.imageUris.forEach { uriString ->
                            Box(
                                modifier = Modifier
                                    .size(dimensionResource(R.dimen.thumbnail_size))
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onImageClick(uriString) }
                            ) {
                                AsyncImage(
                                    model = uriString,
                                    contentDescription = stringResource(R.string.cd_event_attachment),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun CultureDetailScreenPreview() {
    MaterialTheme {
        Surface {
            CultureDetailScreenContent(
                state = CultureDetailUiState(
                    isLoading = false,
                    culture = CellCulture(
                        id = "1",
                        name = "iPSC Line #4",
                        cellType = "Human iPSC",
                        startedAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        notes = "Notes sample..."
                    )
                ),
                onToggleArchiveStatus = {},
                onExportCsv = {},
                onRemovePublicationAssociation = {},
                onDeleteEvent = {},
                clearMessage = {},
                onNavigateBack = {},
                onNavigateToEditCulture = {},
                onNavigateToAddEvent = {},
                onNavigateToLiteratureSearch = {},
                onNavigateToImagePreview = {}
            )
        }
    }
}
