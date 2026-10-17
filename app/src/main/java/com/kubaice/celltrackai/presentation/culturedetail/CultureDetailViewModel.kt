package com.kubaice.celltrackai.presentation.culturedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubaice.celltrackai.domain.export.CsvExporter
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.model.ScientificPublication
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import com.kubaice.celltrackai.domain.repository.CultureEventRepository
import com.kubaice.celltrackai.domain.repository.PublicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CultureDetailUiState(
    val isLoading: Boolean = true,
    val culture: CellCulture? = null,
    val events: List<CultureEvent> = emptyList(),
    val publications: List<ScientificPublication> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class CultureDetailViewModel @Inject constructor(
    private val cultureRepository: CellCultureRepository,
    private val eventRepository: CultureEventRepository,
    private val publicationRepository: PublicationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cultureId: String = savedStateHandle.get<String>("cultureId") ?: ""

    private val _uiState = MutableStateFlow(CultureDetailUiState())
    val uiState: StateFlow<CultureDetailUiState> = _uiState.asStateFlow()

    init {
        if (cultureId.isNotEmpty()) {
            observeData()
        } else {
            _uiState.update { it.copy(isLoading = false, message = "Invalid culture ID") }
        }
    }

    private fun observeData() {
        _uiState.update { it.copy(isLoading = true) }

        combine(
            cultureRepository.getCultureById(cultureId),
            eventRepository.getEventsForCulture(cultureId),
            publicationRepository.getPublicationsForCulture(cultureId)
        ) { culture, events, publications ->
            val sortedEvents = events.sortedByDescending { it.occurredAt }
            CultureDetailUiState(
                isLoading = false,
                culture = culture,
                events = sortedEvents,
                publications = publications,
                message = if (culture == null) "Culture not found" else null
            )
        }
        .catch { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = error.message ?: "Failed to load culture details"
                )
            }
        }
        .onEach { newState ->
            _uiState.value = newState
        }
        .launchIn(viewModelScope)
    }

    fun toggleArchiveStatus() {
        val currentCulture = _uiState.value.culture ?: return
        viewModelScope.launch {
            try {
                if (currentCulture.status == CultureStatus.ACTIVE) {
                    cultureRepository.archiveCulture(currentCulture.id)
                    _uiState.update { it.copy(message = "Culture archived") }
                } else {
                    val updated = currentCulture.copy(
                        status = CultureStatus.ACTIVE,
                        updatedAt = System.currentTimeMillis()
                    )
                    cultureRepository.updateCulture(updated)
                    _uiState.update { it.copy(message = "Culture unarchived") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to update status: ${e.message}") }
            }
        }
    }

    fun removePublicationAssociation(publicationId: String) {
        viewModelScope.launch {
            try {
                publicationRepository.removeAssociationWithCulture(cultureId, publicationId)
                _uiState.update { it.copy(message = "Removed publication association") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to remove association: ${e.message}") }
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                eventRepository.deleteEvent(eventId)
                _uiState.update { it.copy(message = "Event deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to delete event: ${e.message}") }
            }
        }
    }

    fun generateCsvContent(): String {
        val culture = _uiState.value.culture ?: return ""
        val events = _uiState.value.events
        return CsvExporter.exportToCsv(culture, events)
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
