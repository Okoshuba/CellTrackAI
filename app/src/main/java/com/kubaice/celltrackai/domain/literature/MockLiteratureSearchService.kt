package com.kubaice.celltrackai.domain.literature

import com.kubaice.celltrackai.domain.model.ScientificPublication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockLiteratureSearchService @Inject constructor() : LiteratureSearchService {

    private val samplePapers = listOf(
        ScientificPublication(
            id = "PUB-PUBMED-35891024",
            title = "Standardized Subculturing and Passage Thresholds for Human iPSCs in Chemically Defined Media",
            authors = listOf("Dr. Sarah Lin", "Prof. Marcus Thorne", "Elena Rostova"),
            publicationYear = 2023,
            source = "Nature Protocols & Stem Cell Reports",
            externalUrl = "https://pubmed.ncbi.nlm.nih.gov/35891024/",
            abstractText = "Human induced pluripotent stem cells (iPSCs) require rigorous monitoring of confluency and passage frequency. We evaluate EDTA-based enzymatic dissociation versus mechanical clump passaging in feeder-free mTeSR1 substrates, demonstrating that passaging at 80-85% confluency preserves pluripotency markers (OCT4, SOX2, NANOG) across >30 consecutive passages."
        ),
        ScientificPublication(
            id = "PUB-PUBMED-34120955",
            title = "Establishment and Long-term Propagation of Vero E6 Cell Lines for Viral Titer Assays",
            authors = listOf("Dr. Kenneth Vance", "Dr. Amara Okafor"),
            publicationYear = 2022,
            source = "Journal of Virological Methods",
            externalUrl = "https://pubmed.ncbi.nlm.nih.gov/34120955/",
            abstractText = "Vero E6 lineage maintenance protocols for high-throughput viral plaque assays. Detailed guidelines on cell seeding density (2x10^4 cells/cm2), media serum concentration (10% FBS growth, 2% FBS maintenance), and morphological indicators of senescence during viral host cell propagation."
        ),
        ScientificPublication(
            id = "PUB-PUBMED-33887102",
            title = "HeLa Line Passage Dynamics and Genomic Stability Across Extended Culture Cycles",
            authors = listOf("Dr. Hiroshi Tanaka", "Dr. Rebecca Vance"),
            publicationYear = 2021,
            source = "Cell Communication and Signaling",
            externalUrl = "https://pubmed.ncbi.nlm.nih.gov/33887102/",
            abstractText = "Comprehensive analysis of HeLa cell proliferation kinetics, doubling time variations (18-24 hours), and karyotypic drift at high passage numbers (>P50). Establishes best practices for banking cryopreserved master cell stocks at passage #10."
        ),
        ScientificPublication(
            id = "PUB-PUBMED-36771890",
            title = "Optimization of Transfection Efficiency in High-Density Suspension HEK293 Culture",
            authors = listOf("David Miller", "Dr. Claire Dupont"),
            publicationYear = 2023,
            source = "Biotechnology Progress",
            externalUrl = "https://pubmed.ncbi.nlm.nih.gov/36771890/",
            abstractText = "PEI and lipid-mediated transient gene expression optimization in suspension-adapted HEK293F cells. Examines cell viability, aggregation, and recombinant protein yields when transfecting at various confluencies and growth phases."
        ),
        ScientificPublication(
            id = "PUB-PUBMED-35229001",
            title = "Mesenchymal Stem Cell Senescence Markers and Secretome Changes During High-Passage Expansion",
            authors = listOf("Dr. Maria Santos", "Prof. Liam O'Connor"),
            publicationYear = 2022,
            source = "Stem Cells International",
            externalUrl = "https://pubmed.ncbi.nlm.nih.gov/35229001/",
            abstractText = "Human bone marrow-derived MSCs exhibit morphological broadening and reduced osteogenic differentiation capacity beyond passage 6. Provides quantitative cell image analysis criteria for early detection of replicative senescence."
        )
    )

    override suspend fun getPrepopulatedPublications(): List<ScientificPublication> {
        return samplePapers
    }

    override suspend fun searchPublications(query: String): List<ScientificPublication> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return samplePapers

        return samplePapers.filter { paper ->
            paper.title.lowercase().contains(q) ||
                    paper.abstractText?.lowercase()?.contains(q) == true ||
                    paper.source?.lowercase()?.contains(q) == true ||
                    paper.authors.any { it.lowercase().contains(q) }
        }
    }
}
