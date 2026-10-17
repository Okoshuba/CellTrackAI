package com.kubaice.celltrackai.data.initializer

import com.kubaice.celltrackai.domain.literature.LiteratureSearchService
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.model.EventType
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import com.kubaice.celltrackai.domain.repository.CultureEventRepository
import com.kubaice.celltrackai.domain.repository.PublicationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoDataInitializer @Inject constructor(
    private val cultureRepository: CellCultureRepository,
    private val eventRepository: CultureEventRepository,
    private val publicationRepository: PublicationRepository,
    private val literatureSearchService: LiteratureSearchService
) {

    suspend fun populateDemoDataIfEmpty() {
        if (cultureRepository.getCultureCount() == 0) {
            populateDemoData()
        }
    }

    suspend fun populateDemoData() {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        // Sample Publications
        val samplePapers = literatureSearchService.getPrepopulatedPublications()
        samplePapers.forEach { paper ->
            publicationRepository.savePublication(paper)
        }

        // 1. HeLa Line Passage #12
        val helaCulture = CellCulture(
            id = "CULTURE-HELA-P12",
            name = "HeLa Line Passage #12",
            cellType = "HeLa (Human Epithelial Carcinoma)",
            startedAt = now - 14 * dayMs,
            status = CultureStatus.ACTIVE,
            notes = "Standard human epithelial monolayer culture maintained in DMEM + 10% FBS + 1% P/S. Rapid growth rate (~22h doubling time).",
            createdAt = now - 14 * dayMs,
            updatedAt = now - 1 * dayMs
        )
        cultureRepository.insertCulture(helaCulture)

        val helaEvents = listOf(
            CultureEvent(
                id = "EVT-HELA-01",
                cultureId = helaCulture.id,
                occurredAt = now - 14 * dayMs,
                type = EventType.THAWING,
                passageNumber = 12,
                confluencyPercentage = 30.0f,
                notes = "Thawed cryovial (1x10^6 cells) from liquid nitrogen storage tank B2 into T-75 flask containing 15mL pre-warmed complete DMEM."
            ),
            CultureEvent(
                id = "EVT-HELA-02",
                cultureId = helaCulture.id,
                occurredAt = now - 10 * dayMs,
                type = EventType.MEDIA_CHANGE,
                passageNumber = 12,
                confluencyPercentage = 65.0f,
                notes = "Media change with 15mL fresh DMEM + 10% FBS. Cells exhibit tight cobblestone monolayer morphology."
            ),
            CultureEvent(
                id = "EVT-HELA-03",
                cultureId = helaCulture.id,
                occurredAt = now - 6 * dayMs,
                type = EventType.PASSAGE,
                passageNumber = 13,
                confluencyPercentage = 90.0f,
                notes = "Subcultured at 1:4 split ratio into three new T-75 flasks using 0.25% Trypsin-EDTA (3 min incubation at 37°C)."
            ),
            CultureEvent(
                id = "EVT-HELA-04",
                cultureId = helaCulture.id,
                occurredAt = now - 1 * dayMs,
                type = EventType.OBSERVATION,
                passageNumber = 13,
                confluencyPercentage = 78.5f,
                notes = "Microscopic observation: high density, healthy cell attachments, uniform cobblestone pattern, zero debris."
            )
        )
        helaEvents.forEach { eventRepository.insertEvent(it) }
        samplePapers.getOrNull(2)?.let { publicationRepository.associateWithCulture(helaCulture.id, it.id) }

        // 2. iPSC Cardiac Differentiation
        val ipscCulture = CellCulture(
            id = "CULTURE-IPSC-CARD",
            name = "iPSC Cardiac Differentiation",
            cellType = "Human iPSC (Line WTC11)",
            startedAt = now - 21 * dayMs,
            status = CultureStatus.ACTIVE,
            notes = "Directed differentiation into cardiomyocytes using CHIR99021 Wnt activation and IWP2 inhibition protocol on Geltrex matrix.",
            createdAt = now - 21 * dayMs,
            updatedAt = now - 2 * dayMs
        )
        cultureRepository.insertCulture(ipscCulture)

        val ipscEvents = listOf(
            CultureEvent(
                id = "EVT-IPSC-01",
                cultureId = ipscCulture.id,
                occurredAt = now - 21 * dayMs,
                type = EventType.OBSERVATION,
                passageNumber = 6,
                confluencyPercentage = 85.0f,
                notes = "Initiated differentiation (Day 0) on dense pluripotency monolayer using 6µM CHIR99021 in RPMI/B27-minus media."
            ),
            CultureEvent(
                id = "EVT-IPSC-02",
                cultureId = ipscCulture.id,
                occurredAt = now - 16 * dayMs,
                type = EventType.MEDIA_CHANGE,
                passageNumber = 6,
                confluencyPercentage = 95.0f,
                notes = "Day 5 media change with RPMI/B27-plus. Wnt inhibition phase complete."
            ),
            CultureEvent(
                id = "EVT-IPSC-03",
                cultureId = ipscCulture.id,
                occurredAt = now - 2 * dayMs,
                type = EventType.OBSERVATION,
                passageNumber = 6,
                confluencyPercentage = 98.0f,
                notes = "Day 19: Spontaneous synchronized cardiac beating clusters observed visually under 10x phase contrast magnification!"
            )
        )
        ipscEvents.forEach { eventRepository.insertEvent(it) }
        samplePapers.getOrNull(0)?.let { publicationRepository.associateWithCulture(ipscCulture.id, it.id) }

        // 3. Vero E6 Viral Host
        val veroCulture = CellCulture(
            id = "CULTURE-VERO-HOST",
            name = "Vero E6 Viral Host",
            cellType = "Vero E6 (African Green Monkey Kidney)",
            startedAt = now - 45 * dayMs,
            status = CultureStatus.ARCHIVED,
            notes = "Host cell expansion stock reserved for viral neutralization assays and plaque forming units (PFU) quantification.",
            createdAt = now - 45 * dayMs,
            updatedAt = now - 10 * dayMs
        )
        cultureRepository.insertCulture(veroCulture)

        val veroEvents = listOf(
            CultureEvent(
                id = "EVT-VERO-01",
                cultureId = veroCulture.id,
                occurredAt = now - 45 * dayMs,
                type = EventType.THAWING,
                passageNumber = 22,
                confluencyPercentage = 25.0f,
                notes = "Thawed Vero E6 stock vial into T-175 flask in EMEM + 10% FBS."
            ),
            CultureEvent(
                id = "EVT-VERO-02",
                cultureId = veroCulture.id,
                occurredAt = now - 30 * dayMs,
                type = EventType.PASSAGE,
                passageNumber = 23,
                confluencyPercentage = 92.0f,
                notes = "Passaged culture into 96-well assay plates for virus titration experiment."
            ),
            CultureEvent(
                id = "EVT-VERO-03",
                cultureId = veroCulture.id,
                occurredAt = now - 10 * dayMs,
                type = EventType.FREEZING,
                passageNumber = 23,
                confluencyPercentage = 88.0f,
                notes = "Cryopreserved remaining cell stock (10 vials at 2x10^6 cells/mL in 90% FBS + 10% DMSO). Archived culture record."
            )
        )
        veroEvents.forEach { eventRepository.insertEvent(it) }
        samplePapers.getOrNull(1)?.let { publicationRepository.associateWithCulture(veroCulture.id, it.id) }
    }
}
