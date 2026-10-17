package com.kubaice.celltrackai.domain.literature

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiteratureSearchServiceTest {

    private val service = MockLiteratureSearchService()

    @Test
    fun testGetPrepopulatedPublications() = runBlocking {
        val publications = service.getPrepopulatedPublications()
        assertFalse(publications.isEmpty())
        assertTrue(publications.any { it.title.contains("iPSCs", ignoreCase = true) })
        assertTrue(publications.any { it.title.contains("Vero E6", ignoreCase = true) })
        assertTrue(publications.any { it.title.contains("HeLa", ignoreCase = true) })
    }

    @Test
    fun testSearchPublicationsByKeyword() = runBlocking {
        val iPscResults = service.searchPublications("iPSC")
        assertFalse(iPscResults.isEmpty())
        assertTrue(iPscResults.all { pub ->
            pub.title.contains("iPSC", ignoreCase = true) ||
                    pub.abstractText?.contains("iPSC", ignoreCase = true) == true
        })

        val veroResults = service.searchPublications("Vero")
        assertFalse(veroResults.isEmpty())
        assertTrue(veroResults.any { it.title.contains("Vero", ignoreCase = true) })
    }
}
