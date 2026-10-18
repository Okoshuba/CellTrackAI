package com.kubaice.celltrackai.presentation.literature

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubaice.celltrackai.domain.literature.LiteratureSearchService
import com.kubaice.celltrackai.domain.model.ScientificPublication
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import com.kubaice.celltrackai.domain.repository.PublicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiteratureSearchUiState(
    val cultureId: String? = null,
    val cultureName: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val publications: List<ScientificPublication> = emptyList(),
    val associatedPublicationIds: Set<String> = emptySet(),
    val message: String? = null
)

@HiltViewModel
class LiteratureSearchViewModel @Inject constructor(
    private val literatureSearchService: LiteratureSearchService,
    private val publicationRepository: PublicationRepository,
    private val cultureRepository: CellCultureRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cultureIdArg: String? = savedStateHandle.get<String>("cultureId")

    private val _uiState = MutableStateFlow(LiteratureSearchUiState(cultureId = cultureIdArg))
    val uiState: StateFlow<LiteratureSearchUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            if (!cultureIdArg.isNullOrEmpty()) {
                val culture = cultureRepository.getCultureById(cultureIdArg).firstOrNull()
                _uiState.update { it.copy(cultureName = culture?.name) }

                launch {
                    publicationRepository.getPublicationsForCulture(cultureIdArg).collect { list ->
                        val ids = list.map { it.id }.toSet()
                        _uiState.update { state -> state.copy(associatedPublicationIds = ids) }
                    }
                }
            }
            performSearch("")
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun performSearch(query: String = _uiState.value.searchQuery) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = literatureSearchService.searchPublications(query)
                _uiState.update { it.copy(isLoading = false, publications = results) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Search failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleAssociation(publication: ScientificPublication) {
        val cultureId = _uiState.value.cultureId
        if (cultureId.isNullOrEmpty()) return

        viewModelScope.launch {
            try {
                // Ensure publication is persisted in local database first
                publicationRepository.savePublication(publication)

                val isCurrentlyAssociated = _uiState.value.associatedPublicationIds.contains(publication.id)
                if (isCurrentlyAssociated) {
                    publicationRepository.removeAssociationWithCulture(cultureId, publication.id)
                    _uiState.update { it.copy(message = "Publication removed from culture") }
                } else {
                    publicationRepository.associateWithCulture(cultureId, publication.id)
                    _uiState.update { it.copy(message = "Publication associated with culture") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to update association: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
