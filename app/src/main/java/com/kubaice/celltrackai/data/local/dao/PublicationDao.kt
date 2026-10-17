package com.kubaice.celltrackai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kubaice.celltrackai.data.local.entity.CulturePublicationCrossRef
import com.kubaice.celltrackai.data.local.entity.PublicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicationDao {

    @Query("SELECT * FROM publications WHERE title LIKE '%' || :query || '%' OR abstractText LIKE '%' || :query || '%' OR source LIKE '%' || :query || '%' OR authors LIKE '%' || :query || '%'")
    suspend fun searchPublications(query: String): List<PublicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPublication(publication: PublicationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: CulturePublicationCrossRef)

    @Query("DELETE FROM culture_publication_cross_ref WHERE cultureId = :cultureId AND publicationId = :publicationId")
    suspend fun deleteCrossRef(cultureId: String, publicationId: String)

    @Query("SELECT p.* FROM publications p INNER JOIN culture_publication_cross_ref ref ON p.id = ref.publicationId WHERE ref.cultureId = :cultureId")
    fun getPublicationsForCulture(cultureId: String): Flow<List<PublicationEntity>>
}
