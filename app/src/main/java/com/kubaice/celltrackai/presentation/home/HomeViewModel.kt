package com.kubaice.celltrackai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kubaice.celltrackai.data.initializer.DemoDataInitializer
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

data class HomeUiState(
    val isLoading: Boolean = true,
    val totalActiveCultures: Int = 0,
    val recentUpdatesCount: Int = 0,
    val recentActiveCultures: List<CellCulture> = emptyList(),
    val isEmpty: Boolean = false,
    val isPopulatingDemo: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cultureRepository: CellCultureRepository,
    private val demoDataInitializer: DemoDataInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _uiState.update { it.copy(isLoading = true) }
        cultureRepository.getCultures()
            .onEach { cultures ->
                val activeCultures = cultures.filter { it.status == CultureStatus.ACTIVE }
                val sortedRecent = activeCultures.sortedByDescending { it.updatedAt }
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 86_400_000L)
                val recentCount = cultures.count { it.updatedAt >= sevenDaysAgo }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalActiveCultures = activeCultures.size,
                        recentUpdatesCount = recentCount,
                        recentActiveCultures = sortedRecent.take(5),
                        isEmpty = cultures.isEmpty(),
                        message = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "Failed to load dashboard data"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun populateDemoData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPopulatingDemo = true) }
            try {
                demoDataInitializer.populateDemoData()
                _uiState.update { it.copy(isPopulatingDemo = false, message = "Demo data loaded successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isPopulatingDemo = false, message = "Error loading demo data: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
