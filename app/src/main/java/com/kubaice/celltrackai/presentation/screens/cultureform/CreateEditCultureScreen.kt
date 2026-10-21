package com.kubaice.celltrackai.presentation.screens.cultureform

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kubaice.celltrackai.R
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.presentation.elements.CellTrackTopAppBar
import com.kubaice.celltrackai.presentation.elements.LoadingIndicator

@Composable
fun CreateEditCultureScreen(
    viewModel: CreateEditCultureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    CreateEditCultureScreenContent(
        state = uiState,
        onNameChanged = viewModel::onNameChanged,
        onCellTypeChanged = viewModel::onCellTypeChanged,
        onStartedAtChanged = viewModel::onStartedAtChanged,
        onStatusChanged = viewModel::onStatusChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSaveCulture = viewModel::saveCulture,
        clearMessage = viewModel::clearMessage,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEditCultureScreenContent(
    state: CreateEditCultureUiState,
    onNameChanged: (String) -> Unit,
    onCellTypeChanged: (String) -> Unit,
    onStartedAtChanged: (Long) -> Unit,
    onStatusChanged: (CultureStatus) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveCulture: () -> Unit,
    clearMessage: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val startDateStr = remember(state.startedAt) { dateFormat.format(Date(state.startedAt)) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CellTrackTopAppBar(
                title = stringResource(if (state.isEditMode) R.string.title_edit_culture else R.string.title_new_culture),
                onNavigateBack = onNavigateBack
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_16))
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
                ) {
                    // Culture Name Input
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChanged,
                        label = { Text(stringResource(R.string.label_culture_name)) },
                        placeholder = { Text(stringResource(R.string.placeholder_culture_name)) },
                        isError = state.nameError != null,
                        supportingText = {
                            if (state.nameError != null) {
                                Text(
                                    text = state.nameError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("culture_name_input"),
                        singleLine = true
                    )

                    // Cell Type Input
                    OutlinedTextField(
                        value = state.cellType,
                        onValueChange = onCellTypeChanged,
                        label = { Text(stringResource(R.string.label_cell_type)) },
                        placeholder = { Text(stringResource(R.string.placeholder_cell_type)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cell_type_input"),
                        singleLine = true
                    )

                    // Start Date Picker Field
                    OutlinedTextField(
                        value = startDateStr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_start_date)) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = stringResource(R.string.cd_select_date)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Status Selector Chips
                    Column {
                        Text(
                            text = stringResource(R.string.header_culture_status),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
                            FilterChip(
                                selected = state.status == CultureStatus.ACTIVE,
                                onClick = { onStatusChanged(CultureStatus.ACTIVE) },
                                label = { Text(stringResource(R.string.label_status_active)) }
                            )
                            FilterChip(
                                selected = state.status == CultureStatus.ARCHIVED,
                                onClick = { onStatusChanged(CultureStatus.ARCHIVED) },
                                label = { Text(stringResource(R.string.label_status_archived)) }
                            )
                        }
                    }

                    // Notes Field
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = onNotesChanged,
                        label = { Text(stringResource(R.string.label_notes_protocol)) },
                        placeholder = { Text(stringResource(R.string.placeholder_culture_notes)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("culture_notes_input"),
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    // Save Button
                    Button(
                        onClick = onSaveCulture,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.button_height))
                            .testTag("save_culture_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_8))
                        )
                        Text(
                            text = stringResource(if (state.isEditMode) R.string.btn_update_culture else R.string.btn_save_culture),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large)))
                }
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.startedAt
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onStartedAtChanged(it)
                        }
                        showDatePicker = false
                    }) {
                        Text(stringResource(R.string.btn_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
@Preview
private fun CreateEditCultureScreenPreview() {
    MaterialTheme {
        Surface {
            CreateEditCultureScreenContent(
                state = CreateEditCultureUiState(
                    isLoading = false
                ),
                onNameChanged = {},
                onCellTypeChanged = {},
                onStartedAtChanged = {},
                onStatusChanged = {},
                onNotesChanged = {},
                onSaveCulture = {},
                clearMessage = {},
                onNavigateBack = {}
            )
        }
    }
}
