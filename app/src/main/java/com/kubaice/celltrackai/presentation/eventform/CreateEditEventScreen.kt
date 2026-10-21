package com.kubaice.celltrackai.presentation.eventform

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kubaice.celltrackai.R
import com.kubaice.celltrackai.domain.ai.StructuredObservationResult
import com.kubaice.celltrackai.domain.model.EventType
import com.kubaice.celltrackai.presentation.elements.CellTrackTopAppBar
import com.kubaice.celltrackai.presentation.elements.LoadingIndicator

@Composable
fun CreateEditEventScreen(
    viewModel: CreateEditEventViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImageUri(it.toString()) }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    CreateEditEventScreenContent(
        state = uiState,
        onTypeSelected = viewModel::onTypeSelected,
        onOccurredAtChanged = viewModel::onOccurredAtChanged,
        onPassageNumberChanged = viewModel::onPassageNumberChanged,
        onConfluencyChanged = viewModel::onConfluencyChanged,
        onNotesChanged = viewModel::onNotesChanged,
        triggerAiStructuringHelper = viewModel::triggerAiStructuringHelper,
        onAddImage = { imagePickerLauncher.launch("image/*") },
        onRemoveImageUri = viewModel::removeImageUri,
        onSaveEvent = viewModel::saveEvent,
        applyStructuredData = viewModel::applyStructuredData,
        dismissStructuringDialog = viewModel::dismissStructuringDialog,
        clearMessage = viewModel::clearMessage,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEditEventScreenContent(
    state: CreateEditEventUiState,
    onTypeSelected: (EventType) -> Unit,
    onOccurredAtChanged: (Long) -> Unit,
    onPassageNumberChanged: (String) -> Unit,
    onConfluencyChanged: (Float?) -> Unit,
    onNotesChanged: (String) -> Unit,
    triggerAiStructuringHelper: () -> Unit,
    onAddImage: () -> Unit,
    onRemoveImageUri: (String) -> Unit,
    onSaveEvent: () -> Unit,
    applyStructuredData: (StructuredObservationResult) -> Unit,
    dismissStructuringDialog: () -> Unit,
    clearMessage: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var eventTypeDropdownExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy - HH:mm", Locale.getDefault()) }
    val dateStr = remember(state.occurredAt) { dateFormat.format(Date(state.occurredAt)) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CellTrackTopAppBar(
                title = stringResource(if (state.isEditMode) R.string.title_edit_event else R.string.title_log_event),
                subtitle = state.culture?.name,
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
                        .padding(dimensionResource(R.dimen.padding_large))
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
                ) {
                    // Event Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = eventTypeDropdownExpanded,
                        onExpandedChange = { eventTypeDropdownExpanded = !eventTypeDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.type.name.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.label_event_type)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventTypeDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded = eventTypeDropdownExpanded,
                            onDismissRequest = { eventTypeDropdownExpanded = false }
                        ) {
                            EventType.entries.forEach { typeOption ->
                                DropdownMenuItem(
                                    text = { Text(typeOption.name.replace("_", " ")) },
                                    onClick = {
                                        onTypeSelected(typeOption)
                                        eventTypeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Occurred At Date Picker Field
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_date_time)) },
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

                    // Passage Number
                    OutlinedTextField(
                        value = state.passageNumberText,
                        onValueChange = onPassageNumberChanged,
                        label = { Text(stringResource(R.string.label_passage_number_optional)) },
                        placeholder = { Text(stringResource(R.string.placeholder_passage_number)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Confluency Slider & Input
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                                    text = stringResource(R.string.header_confluency_percentage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = state.confluencyPercentage?.let { "%.0f%%".format(it) } ?: stringResource(R.string.label_not_set),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                            Slider(
                                value = state.confluencyPercentage ?: 0f,
                                onValueChange = { onConfluencyChanged(it) },
                                valueRange = 0f..100f,
                                steps = 99
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onConfluencyChanged(null) },
                                    enabled = state.confluencyPercentage != null
                                ) {
                                    Text(stringResource(R.string.btn_clear_confluency))
                                }
                            }
                        }
                    }

                    // Notes & Observations
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = onNotesChanged,
                        label = { Text(stringResource(R.string.header_observation_notes)) },
                        placeholder = { Text(stringResource(R.string.placeholder_event_notes)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 5
                    )

                    // AI Structuring Helper Button
                    OutlinedButton(
                        onClick = triggerAiStructuringHelper,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isStructuringLoading && state.notes.isNotBlank(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        if (state.isStructuringLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                            Text(stringResource(R.string.btn_structuring_ai))
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))
                            )
                            Text(stringResource(R.string.btn_ai_structuring_helper), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Image Attachments Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.header_attached_images, state.attachedImageUris.size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = onAddImage) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_tiny))
                                )
                                Text(stringResource(R.string.btn_add_image))
                            }
                        }

                        if (state.attachedImageUris.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))) {
                                items(state.attachedImageUris) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(dimensionResource(R.dimen.image_box_size))
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = stringResource(R.string.cd_attached_image),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        IconButton(
                                            onClick = { onRemoveImageUri(uri) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(dimensionResource(R.dimen.padding_extra_small))
                                                .size(dimensionResource(R.dimen.icon_size_large))
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.cd_remove_image),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    // Save Button
                    Button(
                        onClick = onSaveEvent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.button_height))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_small))
                        )
                        Text(
                            text = stringResource(if (state.isEditMode) R.string.btn_update_event else R.string.btn_save_event),
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
                initialSelectedDateMillis = state.occurredAt
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onOccurredAtChanged(it)
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

        // AI Structuring Review Dialog
        if (state.showStructuringDialog && state.structuringResult != null) {
            StructuringResultDialog(
                result = state.structuringResult,
                onConfirm = { applyStructuredData(state.structuringResult) },
                onDismiss = dismissStructuringDialog
            )
        }
    }
}

@Composable
fun StructuringResultDialog(
    result: StructuredObservationResult,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        title = {
            Text(stringResource(R.string.header_ai_review_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                Text(
                    text = result.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_medium)),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.parsedCellType?.let {
                            Text(stringResource(R.string.ai_label_cell_type, it), fontWeight = FontWeight.Medium)
                        }
                        result.parsedPassageNumber?.let {
                            Text(stringResource(R.string.ai_label_passage_number, it), fontWeight = FontWeight.Medium)
                        }
                        result.parsedConfluencyPct?.let {
                            Text(stringResource(R.string.ai_label_confluency, it), fontWeight = FontWeight.Medium)
                        }
                        if (result.observedPhenotypes.isNotEmpty()) {
                            Text(
                                stringResource(R.string.ai_label_phenotypes, result.observedPhenotypes.joinToString(", ")),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (!result.contaminationRisk.isNullOrBlank()) {
                            Text(
                                stringResource(R.string.ai_label_contamination_risk, result.contaminationRisk),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (result.recommendedActions.isNotEmpty()) {
                            Text(
                                stringResource(R.string.ai_label_recommendations, result.recommendedActions.joinToString("; ")),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.btn_apply_to_form))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_discard))
            }
        }
    )
}

@Composable
@Preview
private fun CreateEditEventScreenPreview() {
    MaterialTheme {
        Surface {
            CreateEditEventScreenContent(
                state = CreateEditEventUiState(
                    isLoading = false
                ),
                onTypeSelected = {},
                onOccurredAtChanged = {},
                onPassageNumberChanged = {},
                onConfluencyChanged = {},
                onNotesChanged = {},
                triggerAiStructuringHelper = {},
                onAddImage = {},
                onRemoveImageUri = {},
                onSaveEvent = {},
                applyStructuredData = {},
                dismissStructuringDialog = {},
                clearMessage = {},
                onNavigateBack = {}
            )
        }
    }
}
