package com.kubaice.celltrackai.presentation.screens.cultureform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateEditCultureUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val cultureId: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val cellType: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val status: CultureStatus = CultureStatus.ACTIVE,
    val notes: String = "",
    val isSaved: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class CreateEditCultureViewModel @Inject constructor(
    private val cultureRepository: CellCultureRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cultureIdArg: String? = savedStateHandle.get<String>("cultureId")

    private val _uiState = MutableStateFlow(CreateEditCultureUiState())
    val uiState: StateFlow<CreateEditCultureUiState> = _uiState.asStateFlow()

    init {
        if (!cultureIdArg.isNullOrEmpty()) {
            loadExistingCulture(cultureIdArg)
        }
    }

    private fun loadExistingCulture(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEditMode = true, cultureId = id) }
            val existing = cultureRepository.getCultureById(id).firstOrNull()
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = existing.name,
                        cellType = existing.cellType ?: "",
                        startedAt = existing.startedAt,
                        status = existing.status,
                        notes = existing.notes ?: ""
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, message = "Culture not found") }
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                nameError = if (value.isNotBlank()) null else it.nameError
            )
        }
    }

    fun onCellTypeChanged(value: String) {
        _uiState.update { it.copy(cellType = value) }
    }

    fun onStartedAtChanged(timestamp: Long) {
        _uiState.update { it.copy(startedAt = timestamp) }
    }

    fun onStatusChanged(status: CultureStatus) {
        _uiState.update { it.copy(status = status) }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun saveCulture() {
        val currentState = _uiState.value
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Culture Name/Identifier is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val now = System.currentTimeMillis()
                val cultureToSave = if (currentState.isEditMode && currentState.cultureId != null) {
                    CellCulture(
                        id = currentState.cultureId,
                        name = currentState.name.trim(),
                        cellType = currentState.cellType.trim().ifBlank { null },
                        startedAt = currentState.startedAt,
                        status = currentState.status,
                        notes = currentState.notes.trim().ifBlank { null },
                        updatedAt = now
                    )
                } else {
                    CellCulture(
                        id = UUID.randomUUID().toString(),
                        name = currentState.name.trim(),
                        cellType = currentState.cellType.trim().ifBlank { null },
                        startedAt = currentState.startedAt,
                        status = currentState.status,
                        notes = currentState.notes.trim().ifBlank { null },
                        createdAt = now,
                        updatedAt = now
                    )
                }

                if (currentState.isEditMode) {
                    cultureRepository.updateCulture(cultureToSave)
                } else {
                    cultureRepository.insertCulture(cultureToSave)
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Failed to save culture: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
