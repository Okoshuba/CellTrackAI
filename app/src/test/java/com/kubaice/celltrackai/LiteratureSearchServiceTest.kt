package com.kubaice.celltrackai

import com.kubaice.celltrackai.domain.literature.MockLiteratureSearchService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
        assertTrue(publications.any { it.title.contains("HEK293", ignoreCase = true) })
    }

    @Test
    fun testSearchPublicationsWithBlankQuery() = runBlocking {
        val allPublications = service.getPrepopulatedPublications()
        val emptySearchResults = service.searchPublications("")
        val whitespaceSearchResults = service.searchPublications("   ")

        assertEquals(allPublications.size, emptySearchResults.size)
        assertEquals(allPublications.size, whitespaceSearchResults.size)
    }

    @Test
    fun testSearchPublicationsByKeywordInTitleOrAbstract() = runBlocking {
        val ipscResults = service.searchPublications("iPSC")
        assertFalse(ipscResults.isEmpty())
        assertTrue(ipscResults.all { pub ->
            pub.title.contains("iPSC", ignoreCase = true) ||
                    pub.abstractText?.contains("iPSC", ignoreCase = true) == true
        })

        val veroResults = service.searchPublications("Vero")
        assertFalse(veroResults.isEmpty())
        assertTrue(veroResults.any { it.title.contains("Vero", ignoreCase = true) })
    }

    @Test
    fun testSearchPublicationsByAuthorName() = runBlocking {
        val authorResults = service.searchPublications("Tanaka")
        assertFalse(authorResults.isEmpty())
        assertTrue(authorResults.any { pub ->
            pub.authors.any { author -> author.contains("Tanaka", ignoreCase = true) }
        })
    }

    @Test
    fun testSearchPublicationsNonMatchingQuery() = runBlocking {
        val noResults = service.searchPublications("NonExistentXYZQuery123")
        assertTrue(noResults.isEmpty())
    }
}
