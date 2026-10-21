package com.kubaice.celltrackai

import androidx.lifecycle.SavedStateHandle
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.model.EventType
import com.kubaice.celltrackai.domain.model.ScientificPublication
import com.kubaice.celltrackai.domain.repository.CultureEventRepository
import com.kubaice.celltrackai.domain.repository.PublicationRepository
import com.kubaice.celltrackai.presentation.screens.culturedetail.CultureDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeCultureEventRepository : CultureEventRepository {
    val currentEvents = MutableStateFlow<List<CultureEvent>>(emptyList())

    override fun getEventsForCulture(cultureId: String): Flow<List<CultureEvent>> =
        currentEvents.map { list -> list.filter { it.cultureId == cultureId } }

    override suspend fun insertEvent(event: CultureEvent) {
        currentEvents.value = currentEvents.value + event
    }

    override suspend fun updateEvent(event: CultureEvent) {
        currentEvents.value = currentEvents.value.map { if (it.id == event.id) event else it }
    }

    override suspend fun deleteEvent(id: String) {
        currentEvents.value = currentEvents.value.filter { it.id != id }
    }
}

class FakePublicationRepository : PublicationRepository {
    private val publications = MutableStateFlow<List<ScientificPublication>>(emptyList())

    override suspend fun searchPublications(query: String): List<ScientificPublication> = emptyList()

    override suspend fun savePublication(publication: ScientificPublication) {
        publications.value = publications.value + publication
    }

    override fun getPublicationsForCulture(cultureId: String): Flow<List<ScientificPublication>> =
        publications

    override suspend fun associateWithCulture(cultureId: String, publicationId: String) {}

    override suspend fun removeAssociationWithCulture(cultureId: String, publicationId: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class CultureEventHistorySortingTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testChronologicalEventSortingNewestFirst() {
        val eventOld = CultureEvent(
            id = "E-1",
            cultureId = "C-100",
            occurredAt = 1000L,
            type = EventType.OBSERVATION
        )
        val eventMiddle = CultureEvent(
            id = "E-2",
            cultureId = "C-100",
            occurredAt = 5000L,
            type = EventType.MEDIA_CHANGE
        )
        val eventNewest = CultureEvent(
            id = "E-3",
            cultureId = "C-100",
            occurredAt = 9000L,
            type = EventType.PASSAGE
        )

        val unsortedList = listOf(eventOld, eventNewest, eventMiddle)
        val sortedList = unsortedList.sortedByDescending { it.occurredAt }

        assertEquals(listOf(eventNewest, eventMiddle, eventOld), sortedList)
        assertEquals(9000L, sortedList.first().occurredAt)
        assertEquals(1000L, sortedList.last().occurredAt)
    }

    @Test
    fun testEventFilteringByCultureId() {
        val eventCultureA1 = CultureEvent(id = "E-1", cultureId = "C-A", occurredAt = 1000L, type = EventType.OBSERVATION)
        val eventCultureA2 = CultureEvent(id = "E-2", cultureId = "C-A", occurredAt = 2000L, type = EventType.MEDIA_CHANGE)
        val eventCultureB1 = CultureEvent(id = "E-3", cultureId = "C-B", occurredAt = 3000L, type = EventType.PASSAGE)

        val allEvents = listOf(eventCultureA1, eventCultureA2, eventCultureB1)

        val cultureAEvents = allEvents.filter { it.cultureId == "C-A" }
        assertEquals(2, cultureAEvents.size)
        assertTrue(cultureAEvents.all { it.cultureId == "C-A" })

        val cultureBEvents = allEvents.filter { it.cultureId == "C-B" }
        assertEquals(1, cultureBEvents.size)
        assertEquals("E-3", cultureBEvents.first().id)
    }

    @Test
    fun testEventFilteringByEventType() {
        val obsEvent1 = CultureEvent(id = "E-1", cultureId = "C-1", occurredAt = 1000L, type = EventType.OBSERVATION)
        val obsEvent2 = CultureEvent(id = "E-2", cultureId = "C-1", occurredAt = 2000L, type = EventType.OBSERVATION)
        val passageEvent = CultureEvent(id = "E-3", cultureId = "C-1", occurredAt = 3000L, type = EventType.PASSAGE)

        val events = listOf(obsEvent1, obsEvent2, passageEvent)

        val observationsOnly = events.filter { it.type == EventType.OBSERVATION }
        assertEquals(2, observationsOnly.size)

        val passagesOnly = events.filter { it.type == EventType.PASSAGE }
        assertEquals(1, passagesOnly.size)
        assertEquals("E-3", passagesOnly.first().id)
    }

    @Test
    fun testCultureDetailViewModelSortsEventsDescending() = runTest {
        val cultureRepo = FakeCellCultureRepository()
        val eventRepo = FakeCultureEventRepository()
        val pubRepo = FakePublicationRepository()

        val culture = CellCulture(
            id = "C-100",
            name = "Test Line",
            cellType = "HeLa",
            startedAt = 1000L,
            status = CultureStatus.ACTIVE
        )
        cultureRepo.insertCulture(culture)

        val eventOld = CultureEvent(id = "E-1", cultureId = "C-100", occurredAt = 1000L, type = EventType.OBSERVATION)
        val eventNewest = CultureEvent(id = "E-2", cultureId = "C-100", occurredAt = 9000L, type = EventType.PASSAGE)
        eventRepo.insertEvent(eventOld)
        eventRepo.insertEvent(eventNewest)

        val viewModel = CultureDetailViewModel(
            cultureRepository = cultureRepo,
            eventRepository = eventRepo,
            publicationRepository = pubRepo,
            savedStateHandle = SavedStateHandle(mapOf("cultureId" to "C-100"))
        )

        val loadedState = viewModel.uiState.value
        val loadedEvents = loadedState.events

        assertEquals(2, loadedEvents.size)
        assertEquals("E-2", loadedEvents[0].id) // Newest first
        assertEquals("E-1", loadedEvents[1].id)
    }
}
