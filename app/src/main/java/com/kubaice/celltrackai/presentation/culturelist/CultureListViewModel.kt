package com.kubaice.celltrackai.presentation.culturelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatusFilter {
    ALL, ACTIVE, ARCHIVED
}

data class CultureListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val cultures: List<CellCulture> = emptyList(),
    val filteredCultures: List<CellCulture> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class CultureListViewModel @Inject constructor(
    private val cultureRepository: CellCultureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CultureListUiState())
    val uiState: StateFlow<CultureListUiState> = _uiState.asStateFlow()

    init {
        loadCultures()
    }

    fun loadCultures() {
        _uiState.update { it.copy(isLoading = true) }
        cultureRepository.getCultures()
            .onEach { culturesList ->
                _uiState.update { state ->
                    val filtered = applyFilter(culturesList, state.searchQuery, state.statusFilter)
                    state.copy(
                        isLoading = false,
                        cultures = culturesList,
                        filteredCultures = filtered
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Failed to load cultures"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = applyFilter(state.cultures, query, state.statusFilter)
            state.copy(searchQuery = query, filteredCultures = filtered)
        }
    }

    fun onStatusFilterSelected(filter: StatusFilter) {
        _uiState.update { state ->
            val filtered = applyFilter(state.cultures, state.searchQuery, filter)
            state.copy(statusFilter = filter, filteredCultures = filtered)
        }
    }

    fun toggleCultureStatus(culture: CellCulture) {
        viewModelScope.launch {
            try {
                if (culture.status == CultureStatus.ACTIVE) {
                    cultureRepository.archiveCulture(culture.id)
                    _uiState.update { it.copy(message = "Archived '${culture.name}'") }
                } else {
                    val updated = culture.copy(
                        status = CultureStatus.ACTIVE,
                        updatedAt = System.currentTimeMillis()
                    )
                    cultureRepository.updateCulture(updated)
                    _uiState.update { it.copy(message = "Unarchived '${culture.name}'") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to update status: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun applyFilter(
        cultures: List<CellCulture>,
        query: String,
        statusFilter: StatusFilter
    ): List<CellCulture> {
        return cultures.filter { culture ->
            val matchesSearch = query.isBlank() ||
                culture.name.contains(query, ignoreCase = true) ||
                (culture.cellType?.contains(query, ignoreCase = true) == true)

            val matchesStatus = when (statusFilter) {
                StatusFilter.ALL -> true
                StatusFilter.ACTIVE -> culture.status == CultureStatus.ACTIVE
                StatusFilter.ARCHIVED -> culture.status == CultureStatus.ARCHIVED
            }

            matchesSearch && matchesStatus
        }.sortedByDescending { it.updatedAt }
    }
}
