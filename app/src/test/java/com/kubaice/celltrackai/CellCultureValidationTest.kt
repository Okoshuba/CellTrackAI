package com.kubaice.celltrackai

import androidx.lifecycle.SavedStateHandle
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import com.kubaice.celltrackai.presentation.screens.cultureform.CreateEditCultureViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeCellCultureRepository : CellCultureRepository {
    val currentCultures = MutableStateFlow<List<CellCulture>>(emptyList())

    override fun getCultures(): Flow<List<CellCulture>> = currentCultures

    override fun getCultureById(id: String): Flow<CellCulture?> =
        currentCultures.map { list -> list.find { it.id == id } }

    override suspend fun insertCulture(culture: CellCulture) {
        currentCultures.value = currentCultures.value + culture
    }

    override suspend fun updateCulture(culture: CellCulture) {
        currentCultures.value = currentCultures.value.map { if (it.id == culture.id) culture else it }
    }

    override suspend fun archiveCulture(id: String) {
        currentCultures.value = currentCultures.value.map {
            if (it.id == id) it.copy(status = CultureStatus.ARCHIVED) else it
        }
    }

    override suspend fun deleteCulture(id: String) {
        currentCultures.value = currentCultures.value.filter { it.id != id }
    }

    override suspend fun getCultureCount(): Int = currentCultures.value.size
}

@OptIn(ExperimentalCoroutinesApi::class)
class CellCultureValidationTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeCellCultureRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCellCultureRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testBlankNameHandlingInValidation() = runTest {
        val viewModel = CreateEditCultureViewModel(
            cultureRepository = repository,
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("")
        viewModel.saveCulture()

        val state = viewModel.uiState.value
        assertEquals("Culture Name/Identifier is required", state.nameError)
        assertFalse(state.isSaved)
        assertEquals(0, repository.getCultureCount())
    }

    @Test
    fun testWhitespaceOnlyNameHandlingInValidation() = runTest {
        val viewModel = CreateEditCultureViewModel(
            cultureRepository = repository,
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("   ")
        viewModel.saveCulture()

        val state = viewModel.uiState.value
        assertEquals("Culture Name/Identifier is required", state.nameError)
        assertFalse(state.isSaved)
        assertEquals(0, repository.getCultureCount())
    }

    @Test
    fun testValidCultureCreationAndSaving() = runTest {
        val viewModel = CreateEditCultureViewModel(
            cultureRepository = repository,
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("HeLa Line Batch 1")
        viewModel.onCellTypeChanged("HeLa")
        viewModel.onNotesChanged("Initial seeded culture")

        viewModel.saveCulture()

        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertTrue(state.isSaved)

        assertEquals(1, repository.getCultureCount())
        val saved = repository.getCultures().first().first()
        assertEquals("HeLa Line Batch 1", saved.name)
        assertEquals("HeLa", saved.cellType)
        assertEquals("Initial seeded culture", saved.notes)
        assertEquals(CultureStatus.ACTIVE, saved.status)
    }

    @Test
    fun testStartDateHandling() = runTest {
        val viewModel = CreateEditCultureViewModel(
            cultureRepository = repository,
            savedStateHandle = SavedStateHandle()
        )

        val targetTimestamp = 1700000000000L
        viewModel.onNameChanged("iPSC Monolayer")
        viewModel.onStartedAtChanged(targetTimestamp)

        assertEquals(targetTimestamp, viewModel.uiState.value.startedAt)

        viewModel.saveCulture()

        val saved = repository.getCultures().first().first()
        assertEquals(targetTimestamp, saved.startedAt)
    }

    @Test
    fun testStatusTransitions() = runTest {
        val viewModel = CreateEditCultureViewModel(
            cultureRepository = repository,
            savedStateHandle = SavedStateHandle()
        )

        viewModel.onNameChanged("Vero E6 Line")

        // Transition status to ARCHIVED
        viewModel.onStatusChanged(CultureStatus.ARCHIVED)
        assertEquals(CultureStatus.ARCHIVED, viewModel.uiState.value.status)

        // Save archived culture
        viewModel.saveCulture()

        val savedArchived = repository.getCultures().first().first()
        assertEquals(CultureStatus.ARCHIVED, savedArchived.status)

        // Transition status back to ACTIVE via edit mode
        val editViewModel = CreateEditCultureViewModel(
            cultureRepository = repository,
            savedStateHandle = SavedStateHandle(mapOf("cultureId" to savedArchived.id))
        )

        assertEquals(CultureStatus.ARCHIVED, editViewModel.uiState.value.status)
        editViewModel.onStatusChanged(CultureStatus.ACTIVE)
        assertEquals(CultureStatus.ACTIVE, editViewModel.uiState.value.status)

        editViewModel.saveCulture()

        val savedActive = repository.getCultures().first().find { it.id == savedArchived.id }
        assertNotNull(savedActive)
        assertEquals(CultureStatus.ACTIVE, savedActive!!.status)
    }
}
