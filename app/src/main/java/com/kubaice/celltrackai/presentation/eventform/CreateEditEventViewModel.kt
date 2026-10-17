package com.kubaice.celltrackai.presentation.eventform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubaice.celltrackai.domain.ai.ObservationStructuringService
import com.kubaice.celltrackai.domain.ai.StructuredObservationResult
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.EventType
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import com.kubaice.celltrackai.domain.repository.CultureEventRepository
import com.kubaice.celltrackai.domain.repository.ImageStorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateEditEventUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val cultureId: String = "",
    val eventId: String? = null,
    val culture: CellCulture? = null,
    val type: EventType = EventType.OBSERVATION,
    val occurredAt: Long = System.currentTimeMillis(),
    val passageNumberText: String = "",
    val confluencyPercentage: Float? = null,
    val notes: String = "",
    val attachedImageUris: List<String> = emptyList(),
    val isStructuringLoading: Boolean = false,
    val structuringResult: StructuredObservationResult? = null,
    val showStructuringDialog: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class CreateEditEventViewModel @Inject constructor(
    private val cultureRepository: CellCultureRepository,
    private val eventRepository: CultureEventRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val structuringService: ObservationStructuringService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cultureIdArg: String = savedStateHandle.get<String>("cultureId") ?: ""
    private val eventIdArg: String? = savedStateHandle.get<String>("eventId")

    private val _uiState = MutableStateFlow(CreateEditEventUiState(cultureId = cultureIdArg))
    val uiState: StateFlow<CreateEditEventUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val culture = cultureRepository.getCultureById(cultureIdArg).firstOrNull()
            _uiState.update { it.copy(culture = culture) }

            if (!eventIdArg.isNullOrEmpty()) {
                val events = eventRepository.getEventsForCulture(cultureIdArg).firstOrNull() ?: emptyList()
                val existing = events.find { it.id == eventIdArg }
                if (existing != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            eventId = existing.id,
                            type = existing.type,
                            occurredAt = existing.occurredAt,
                            passageNumberText = existing.passageNumber?.toString() ?: "",
                            confluencyPercentage = existing.confluencyPercentage,
                            notes = existing.notes ?: "",
                            attachedImageUris = existing.imageUris
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, message = "Event not found") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTypeSelected(type: EventType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onOccurredAtChanged(timestamp: Long) {
        _uiState.update { it.copy(occurredAt = timestamp) }
    }

    fun onPassageNumberChanged(value: String) {
        _uiState.update { it.copy(passageNumberText = value) }
    }

    fun onConfluencyChanged(value: Float?) {
        _uiState.update { it.copy(confluencyPercentage = value) }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun addImageUri(uriString: String) {
        _uiState.update { state ->
            if (!state.attachedImageUris.contains(uriString)) {
                state.copy(attachedImageUris = state.attachedImageUris + uriString)
            } else state
        }
    }

    fun removeImageUri(uriString: String) {
        _uiState.update { state ->
            state.copy(attachedImageUris = state.attachedImageUris.filter { it != uriString })
        }
    }

    fun triggerAiStructuringHelper() {
        val currentNotes = _uiState.value.notes
        if (currentNotes.isBlank()) {
            _uiState.update { it.copy(message = "Please enter observation notes before structuring.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isStructuringLoading = true) }
            try {
                val result = structuringService.structureObservation(
                    rawText = currentNotes,
                    cultureContext = _uiState.value.culture
                )
                _uiState.update {
                    it.copy(
                        isStructuringLoading = false,
                        structuringResult = result,
                        showStructuringDialog = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStructuringLoading = false,
                        message = "AI Structuring error: ${e.message}"
                    )
                }
            }
        }
    }

    fun applyStructuredData(result: StructuredObservationResult) {
        _uiState.update { state ->
            val updatedPassage = result.parsedPassageNumber?.toString() ?: state.passageNumberText
            val updatedConfluency = result.parsedConfluencyPct ?: state.confluencyPercentage

            val structuredAdditions = buildString {
                if (result.observedPhenotypes.isNotEmpty()) {
                    append("\n[Phenotypes: ${result.observedPhenotypes.joinToString(", ")}]")
                }
                if (!result.contaminationRisk.isNullOrBlank()) {
                    append("\n[Contamination Risk: ${result.contaminationRisk}]")
                }
                if (result.recommendedActions.isNotEmpty()) {
                    append("\n[Recommended Actions: ${result.recommendedActions.joinToString("; ")}]")
                }
            }

            val updatedNotes = if (structuredAdditions.isNotBlank() && !state.notes.contains("[Phenotypes:")) {
                state.notes.trim() + "\n" + structuredAdditions.trim()
            } else {
                state.notes
            }

            state.copy(
                passageNumberText = updatedPassage,
                confluencyPercentage = updatedConfluency,
                notes = updatedNotes,
                showStructuringDialog = false
            )
        }
    }

    fun dismissStructuringDialog() {
        _uiState.update { it.copy(showStructuringDialog = false) }
    }

    fun saveEvent() {
        val currentState = _uiState.value
        val passage = currentState.passageNumberText.toIntOrNull()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Copy external content URIs to internal app storage
                val savedImageUris = currentState.attachedImageUris.map { uriString ->
                    if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                        imageStorageRepository.saveImageFromUri(uriString) ?: uriString
                    } else {
                        uriString
                    }
                }

                val eventToSave = CultureEvent(
                    id = currentState.eventId ?: UUID.randomUUID().toString(),
                    cultureId = currentState.cultureId,
                    occurredAt = currentState.occurredAt,
                    type = currentState.type,
                    passageNumber = passage,
                    confluencyPercentage = currentState.confluencyPercentage,
                    notes = currentState.notes.trim().ifBlank { null },
                    imageUris = savedImageUris
                )

                if (currentState.isEditMode) {
                    eventRepository.updateEvent(eventToSave)
                } else {
                    eventRepository.insertEvent(eventToSave)
                }

                // Update parent culture's updatedAt timestamp
                currentState.culture?.let { cult ->
                    cultureRepository.updateCulture(cult.copy(updatedAt = System.currentTimeMillis()))
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Failed to save event: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
