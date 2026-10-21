package com.kubaice.celltrackai.presentation.home

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kubaice.celltrackai.R
import com.kubaice.celltrackai.domain.model.CellCulture

private const val CARD_TITLE_MAX_LINES = 2

@Composable
fun HomeScreen(
    onNavigateToCultureList: () -> Unit,
    onNavigateToCultureDetail: (String) -> Unit,
    onNavigateToCreateCulture: () -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        state = uiState,
        clearMessage = viewModel::clearMessage,
        populateDemoData = viewModel::populateDemoData,
        onNavigateToCultureList = onNavigateToCultureList,
        onNavigateToCultureDetail = onNavigateToCultureDetail,
        onNavigateToCreateCulture = onNavigateToCreateCulture,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    state: HomeUiState,
    clearMessage: () -> Unit,
    populateDemoData: () -> Unit,
    onNavigateToCultureList: () -> Unit,
    onNavigateToCultureDetail: (String) -> Unit,
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
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensionResource(R.dimen.padding_large)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
                ) {
                    item {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                        // Summary Cards Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
                        ) {
                            SummaryCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.header_active_cultures),
                                value = state.totalActiveCultures.toString(),
                                icon = Icons.Default.Science,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    item {
                        // Quick Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                            ) {
                                Button(
                                    onClick = onNavigateToCreateCulture,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("new_culture_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_tiny))
                                    )
                                    Text(stringResource(R.string.btn_new_culture))
                                }

                                OutlinedButton(
                                    onClick = onNavigateToCultureList,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("all_cultures_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_tiny))
                                    )
                                    Text(stringResource(R.string.btn_all_cultures))
                                }
                            }

                            OutlinedButton(
                                onClick = { populateDemoData() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isPopulatingDemo
                            ) {
                                if (state.isPopulatingDemo) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .height(18.dp)
                                            .width(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                                    Text(stringResource(R.string.btn_loading_demo))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Dataset,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))
                                    )
                                    Text(stringResource(R.string.btn_populate_demo))
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.header_recently_updated_active),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
                        )
                    }

                    if (state.isEmpty || state.recentActiveCultures.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = dimensionResource(R.dimen.padding_medium)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimensionResource(R.dimen.padding_extra_large)),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .height(dimensionResource(R.dimen.icon_size_huge))
                                            .width(dimensionResource(R.dimen.icon_size_huge)),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                                    Text(
                                        text = stringResource(R.string.header_no_active_cultures),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                                    Text(
                                        text = stringResource(R.string.header_no_active_cultures_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.recentActiveCultures, key = { it.id }) { culture ->
                            CultureQuickCard(
                                culture = culture,
                                onClick = { onNavigateToCultureDetail(culture.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large)))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.icon_size_medium))
                        .width(dimensionResource(R.dimen.icon_size_medium))
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CultureQuickCard(
    culture: CellCulture,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = remember(culture.updatedAt) { dateFormat.format(Date(culture.updatedAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("culture_card_${culture.name}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = culture.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = CARD_TITLE_MAX_LINES,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(R.string.label_status_active),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.padding_small),
                            vertical = dimensionResource(R.dimen.padding_tiny)
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!culture.cellType.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                Text(
                    text = culture.cellType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = stringResource(R.string.label_last_updated, formattedDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@Preview
private fun HomeScreenPreview() {
    MaterialTheme {
        Surface {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                ),
                populateDemoData = {},
                clearMessage = {},
                onNavigateToCultureList = {},
                onNavigateToCultureDetail = {},
                onNavigateToCreateCulture = {},
            )
        }
    }
}
