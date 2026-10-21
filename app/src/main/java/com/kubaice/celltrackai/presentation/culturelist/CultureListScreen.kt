package com.kubaice.celltrackai.presentation.culturelist

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kubaice.celltrackai.R
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.presentation.elements.CellTrackTopAppBar
import com.kubaice.celltrackai.presentation.elements.CultureStatusBadge
import com.kubaice.celltrackai.presentation.elements.LoadingIndicator

@Composable
fun CultureListScreen(
    viewModel: CultureListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreateCulture: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CultureListScreenContent(
        state = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onStatusFilterSelected = viewModel::onStatusFilterSelected,
        onToggleStatus = viewModel::toggleCultureStatus,
        clearMessage = viewModel::clearMessage,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToCreateCulture = onNavigateToCreateCulture
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CultureListScreenContent(
    state: CultureListUiState,
    onSearchQueryChanged: (String) -> Unit,
    onStatusFilterSelected: (StatusFilter) -> Unit,
    onToggleStatus: (CellCulture) -> Unit,
    clearMessage: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreateCulture: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CellTrackTopAppBar(
                title = stringResource(R.string.title_cell_cultures),
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateCulture,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_culture)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.padding_large))
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_small)),
                placeholder = { Text(stringResource(R.string.placeholder_search_cultures)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear_search)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Status Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
            ) {
                FilterChip(
                    selected = state.statusFilter == StatusFilter.ALL,
                    onClick = { onStatusFilterSelected(StatusFilter.ALL) },
                    label = { Text(stringResource(R.string.label_all_count, state.cultures.size)) }
                )
                FilterChip(
                    selected = state.statusFilter == StatusFilter.ACTIVE,
                    onClick = { onStatusFilterSelected(StatusFilter.ACTIVE) },
                    label = { Text(stringResource(R.string.label_active_count, state.cultures.count { it.status == CultureStatus.ACTIVE })) }
                )
                FilterChip(
                    selected = state.statusFilter == StatusFilter.ARCHIVED,
                    onClick = { onStatusFilterSelected(StatusFilter.ARCHIVED) },
                    label = { Text(stringResource(R.string.label_archived_count, state.cultures.count { it.status == CultureStatus.ARCHIVED })) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    LoadingIndicator()
                } else if (state.filteredCultures.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_huge)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.header_no_cultures_filter),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredCultures, key = { it.id }) { culture ->
                            CultureListItemCard(
                                culture = culture,
                                onClick = { onNavigateToDetail(culture.id) },
                                onToggleStatus = { onToggleStatus(culture) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun CultureListItemCard(
    culture: CellCulture,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val startDateStr = remember(culture.startedAt) { dateFormat.format(Date(culture.startedAt)) }
    val updateDateStr = remember(culture.updatedAt) { dateFormat.format(Date(culture.updatedAt)) }
    val isActive = culture.status == CultureStatus.ACTIVE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    Text(
                        text = culture.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CultureStatusBadge(status = culture.status)
                }

                if (!culture.cellType.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                    Text(
                        text = culture.cellType,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
                ) {
                    Text(
                        text = stringResource(R.string.label_started, startDateStr),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.label_updated, updateDateStr),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Archive else Icons.Default.Unarchive,
                    contentDescription = stringResource(if (isActive) R.string.cd_archive else R.string.cd_unarchive),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
@Preview
private fun CultureListScreenPreview() {
    MaterialTheme {
        Surface {
            CultureListScreenContent(
                state = CultureListUiState(
                    isLoading = false,
                    cultures = listOf(
                        CellCulture(
                            id = "1",
                            name = "HeLa Line Passage #12",
                            cellType = "HeLa",
                            startedAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            status = CultureStatus.ACTIVE
                        )
                    )
                ),
                onSearchQueryChanged = {},
                onStatusFilterSelected = {},
                onToggleStatus = {},
                clearMessage = {},
                onNavigateBack = {},
                onNavigateToDetail = {},
                onNavigateToCreateCulture = {}
            )
        }
    }
}
